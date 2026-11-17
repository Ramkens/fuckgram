package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;

import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestTimeDelegate;
import org.telegram.utils.proxy.ProxySettings;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * fuckgram: "built-in VPN" for Telegram. It collects free MTProto and SOCKS5 proxies from many public lists plus the keys
 * you paste, checks how fast each one answers, sorts them and switches Telegram to the fastest. It carries only Telegram's
 * own traffic (not the whole phone) and understands MTProto / SOCKS5 links, not vless / vmess / trojan keys.
 */
public class FuckgramVpn {

    public interface Progress {
        void update(String text);
    }

    /** t.me/proxy links, one per line. */
    private static final String[] MTPROTO_SOURCES = {
            "https://raw.githubusercontent.com/SoliSpirit/mtproto/master/all_proxies.txt",
            "https://raw.githubusercontent.com/Grim1313/mtproto-for-telegram/master/all_proxies.txt",
            "https://raw.githubusercontent.com/ALIILAPRO/MTProtoProxy/main/mtproto.txt",
    };

    /** socks5://ip:port or ip:port, one per line. */
    private static final String[] SOCKS_SOURCES = {
            "https://raw.githubusercontent.com/proxifly/free-proxy-list/main/proxies/protocols/socks5/data.txt",
            "https://raw.githubusercontent.com/TheSpeedX/PROXY-List/master/socks5.txt",
            "https://raw.githubusercontent.com/hookzof/socks5_list/master/proxy.txt",
    };

    private static final int MTPROTO_SAMPLE = 160;
    private static final int SOCKS_SAMPLE = 120;
    private static final int BATCH = 40;
    private static final int KEEP = 15;

    public static volatile boolean running;
    public static volatile boolean cancelled;

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences("fuckgram_vpn", Context.MODE_PRIVATE);
    }

    public static String getCustomKeys() {
        return prefs().getString("keys", "");
    }

    public static void setCustomKeys(String text) {
        prefs().edit().putString("keys", text == null ? "" : text).apply();
    }

    public static String getCustomSources() {
        return prefs().getString("sources", "");
    }

    public static void setCustomSources(String text) {
        prefs().edit().putString("sources", text == null ? "" : text).apply();
    }

    public static int lastUnsupported;

    /** Proxies found in a block of text (links of all kinds, ip:port). */
    public static List<ProxySettings> parse(String text) {
        List<ProxySettings> result = new ArrayList<>();
        if (text == null) {
            return result;
        }
        String[] lines = text.split("[\\r\\n\\s,;]+");
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                String lower = line.toLowerCase(Locale.ROOT);
                if (lower.startsWith("vless://") || lower.startsWith("vmess://") || lower.startsWith("trojan://") || lower.startsWith("ss://")
                        || lower.startsWith("hy2://") || lower.startsWith("hysteria") || lower.startsWith("tuic://")) {
                    lastUnsupported++;
                    continue;
                }
                ProxySettings settings = null;
                if (lower.contains("t.me/proxy") || lower.contains("t.me/socks") || lower.startsWith("tg://proxy") || lower.startsWith("tg://socks")
                        || lower.contains("telegram.me/proxy") || lower.contains("telegram.me/socks")) {
                    settings = ProxySettings.fromUri(Uri.parse(line.replace("&amp;", "&")));
                } else {
                    String hostPort = lower.startsWith("socks5://") ? line.substring(9) : lower.startsWith("socks://") ? line.substring(8) : line;
                    int colon = hostPort.lastIndexOf(':');
                    if (colon > 0 && hostPort.indexOf('/') < 0 && hostPort.indexOf('@') < 0) {
                        int port = Integer.parseInt(hostPort.substring(colon + 1));
                        String host = hostPort.substring(0, colon);
                        if (port > 0 && port < 65536 && host.matches("[0-9a-zA-Z.\\-]+")) {
                            settings = ProxySettings.builder().setType(ProxySettings.Type.SOCKS5).setAddress(host).setPort(port).build();
                        }
                    }
                }
                if (settings != null && settings.isValid() && settings.getType() != ProxySettings.Type.WEB) {
                    result.add(settings);
                }
            } catch (Throwable ignore) {
            }
        }
        return result;
    }

    private static String fetch(String address) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(address).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("User-Agent", "fuckgram");
            if (connection.getResponseCode() != 200) {
                return "";
            }
            try (InputStream in = connection.getInputStream()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[32 * 1024];
                int n;
                int total = 0;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                    total += n;
                    if (total > 4 * 1024 * 1024) {
                        break;
                    }
                }
                return out.toString("UTF-8");
            }
        } catch (Throwable e) {
            return "";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void report(Progress progress, String text) {
        if (progress != null) {
            AndroidUtilities.runOnUIThread(() -> progress.update(text));
        }
    }

    public static void autoRefreshIfNeeded(int account) {
        try {
            if (!FuckgramConfig.get(FuckgramConfig.VPN_AUTO) || running) {
                return;
            }
            long last = prefs().getLong("last", 0);
            if (System.currentTimeMillis() - last < 3 * 3600 * 1000L) {
                return;
            }
            refresh(account, null, null);
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    /** Collects, checks, sorts and switches. Runs in the background; progress and the end message come on the UI thread. */
    public static void refresh(final int account, final Progress progress, final Utilities.Callback<String> done) {
        if (running) {
            return;
        }
        running = true;
        cancelled = false;
        Thread thread = new Thread(() -> {
            String message;
            try {
                message = doRefresh(account, progress);
            } catch (Throwable e) {
                FileLog.e(e);
                message = FuckgramText.t("Something went wrong", "Что-то пошло не так");
            }
            running = false;
            final String finalMessage = message;
            if (done != null) {
                AndroidUtilities.runOnUIThread(() -> done.run(finalMessage));
            }
        }, "fuckgram-vpn");
        thread.start();
    }

    private static String doRefresh(int account, Progress progress) {
        lastUnsupported = 0;
        List<ProxySettings> mtproto = new ArrayList<>();
        List<ProxySettings> socks = new ArrayList<>();
        Set<ProxySettings> seen = new HashSet<>();
        int sources = 0;
        int sourceCount = MTPROTO_SOURCES.length + SOCKS_SOURCES.length;
        List<String> extra = new ArrayList<>();
        for (String s : getCustomSources().split("[\\r\\n\\s]+")) {
            if (s.startsWith("http")) {
                extra.add(s);
            }
        }
        sourceCount += extra.size();
        List<String> all = new ArrayList<>();
        Collections.addAll(all, MTPROTO_SOURCES);
        Collections.addAll(all, SOCKS_SOURCES);
        all.addAll(extra);
        for (String source : all) {
            if (cancelled) {
                return FuckgramText.t("Cancelled", "Отменено");
            }
            sources++;
            report(progress, FuckgramText.t("Loading lists ", "Загружаю списки ") + sources + " / " + sourceCount);
            for (ProxySettings p : parse(fetch(source))) {
                if (seen.add(p)) {
                    if (p.getType() == ProxySettings.Type.MTPROTO) {
                        mtproto.add(p);
                    } else {
                        socks.add(p);
                    }
                }
            }
        }
        List<ProxySettings> custom = new ArrayList<>();
        for (ProxySettings p : parse(getCustomKeys())) {
            if (!custom.contains(p)) {
                custom.add(p);
            }
        }
        int found = mtproto.size() + socks.size();
        Collections.shuffle(mtproto);
        Collections.shuffle(socks);
        List<ProxySettings> candidates = new ArrayList<>(custom);
        for (int i = 0; i < mtproto.size() && i < MTPROTO_SAMPLE; i++) {
            if (!candidates.contains(mtproto.get(i))) {
                candidates.add(mtproto.get(i));
            }
        }
        for (int i = 0; i < socks.size() && i < SOCKS_SAMPLE; i++) {
            if (!candidates.contains(socks.get(i))) {
                candidates.add(socks.get(i));
            }
        }
        if (candidates.isEmpty()) {
            return FuckgramText.t("No proxies found. Check the internet connection.", "Прокси не найдены. Проверьте интернет.");
        }
        final HashMap<ProxySettings, Long> times = new HashMap<>();
        int checked = 0;
        for (int start = 0; start < candidates.size(); start += BATCH) {
            if (cancelled) {
                return FuckgramText.t("Cancelled", "Отменено");
            }
            List<ProxySettings> batch = candidates.subList(start, Math.min(candidates.size(), start + BATCH));
            final CountDownLatch latch = new CountDownLatch(batch.size());
            for (final ProxySettings settings : batch) {
                try {
                    ConnectionsManager.getInstance(account).checkProxy(settings, time -> {
                        if (time > 0) {
                            synchronized (times) {
                                times.put(settings, time);
                            }
                        }
                        latch.countDown();
                    });
                } catch (Throwable e) {
                    latch.countDown();
                }
            }
            try {
                latch.await(12, TimeUnit.SECONDS);
            } catch (InterruptedException ignore) {
            }
            checked += batch.size();
            int ok;
            synchronized (times) {
                ok = times.size();
            }
            report(progress, FuckgramText.t("Checked ", "Проверено ") + checked + " / " + candidates.size() + FuckgramText.t(", working: ", ", рабочих: ") + ok);
        }
        final List<ProxySettings> good;
        synchronized (times) {
            good = new ArrayList<>(times.keySet());
        }
        Collections.sort(good, (a, b) -> Long.compare(times.get(a), times.get(b)));
        List<ProxySettings> keep = new ArrayList<>();
        for (ProxySettings p : good) {
            if (keep.size() < KEEP || custom.contains(p)) {
                keep.add(p);
            }
        }
        if (keep.isEmpty()) {
            return FuckgramText.t("Found " + found + " proxies, none answered. Try again later.", "Найдено " + found + " прокси, ни один не ответил. Попробуйте позже.");
        }
        final List<ProxySettings> finalKeep = keep;
        final HashMap<ProxySettings, Long> finalTimes = times;
        final CountDownLatch applied = new CountDownLatch(1);
        AndroidUtilities.runOnUIThread(() -> {
            try {
                apply(finalKeep, finalTimes);
            } catch (Throwable e) {
                FileLog.e(e);
            }
            applied.countDown();
        });
        try {
            applied.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException ignore) {
        }
        prefs().edit().putLong("last", System.currentTimeMillis()).apply();
        ProxySettings best = keep.get(0);
        String extraNote = lastUnsupported > 0 ? FuckgramText.t(" Skipped keys of unsupported types: ", " Пропущено ключей неподдерживаемых типов: ") + lastUnsupported + "." : "";
        return FuckgramText.t("Found " + found + ", working " + good.size() + ". On: " + best.getAddress() + " (" + times.get(best) + " ms).",
                "Найдено " + found + ", рабочих " + good.size() + ". Включён: " + best.getAddress() + " (" + times.get(best) + " мс).") + extraNote;
    }

    /** UI thread: replaces the proxies fuckgram added before, adds the new sorted ones and switches to the fastest. */
    private static void apply(List<ProxySettings> keep, HashMap<ProxySettings, Long> times) {
        SharedConfig.loadProxyList();
        Set<String> old = new HashSet<>();
        for (String link : prefs().getString("added", "").split("\n")) {
            if (!link.isEmpty()) {
                old.add(link);
            }
        }
        for (SharedConfig.ProxyInfo info : new ArrayList<>(SharedConfig.proxyList)) {
            if (old.contains(info.settings.getLink())) {
                SharedConfig.deleteProxy(info);
            }
        }
        StringBuilder added = new StringBuilder();
        SharedConfig.ProxyInfo best = null;
        for (ProxySettings p : keep) {
            SharedConfig.ProxyInfo info = SharedConfig.addProxy(new SharedConfig.ProxyInfo(p));
            Long time = times.get(p);
            info.ping = time == null ? 0 : time;
            info.available = true;
            info.availableCheckTime = SystemClock.elapsedRealtime();
            added.append(p.getLink()).append('\n');
            if (best == null) {
                best = info;
            }
        }
        prefs().edit().putString("added", added.toString()).apply();
        if (best != null) {
            SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
            best.settings.toSharedPreferences(editor);
            editor.putBoolean("proxy_enabled", true);
            editor.commit();
            SharedConfig.currentProxy = best;
            ConnectionsManager.setProxySettings(true, best.settings);
        }
        SharedConfig.saveProxyList();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged);
    }

    public static void turnOff() {
        SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
        editor.putBoolean("proxy_enabled", false);
        editor.commit();
        if (SharedConfig.currentProxy != null) {
            ConnectionsManager.setProxySettings(false, SharedConfig.currentProxy.settings);
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged);
    }

    public static String currentText() {
        if (!SharedConfig.isProxyEnabled() || SharedConfig.currentProxy == null) {
            return FuckgramText.t("off", "выключен");
        }
        SharedConfig.ProxyInfo info = SharedConfig.currentProxy;
        String ping = info.ping > 0 ? " · " + info.ping + " " + FuckgramText.t("ms", "мс") : "";
        return (info.settings.getType() == ProxySettings.Type.MTPROTO ? "MTProto " : "SOCKS5 ") + info.settings.getAddress() + ping;
    }
}
