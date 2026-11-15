package org.telegram.messenger;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * fuckgram: catches uncaught Java exceptions and shows the full stack trace on a separate screen
 * (it runs in its own process, so it still works when the main app crashes on start).
 */
public class FuckgramCrashHandler implements Thread.UncaughtExceptionHandler {

    public static final String CRASH_ACTIVITY = "org.telegram.ui.FuckgramCrashActivity";
    public static final String EXTRA_REPORT = "report";
    private static final String CRASH_PROCESS_SUFFIX = ":fgcrash";

    private final Context context;

    private FuckgramCrashHandler(Context context) {
        this.context = context;
    }

    public static void install(Context context) {
        try {
            Thread.UncaughtExceptionHandler current = Thread.getDefaultUncaughtExceptionHandler();
            if (current instanceof FuckgramCrashHandler) {
                return;
            }
            Thread.setDefaultUncaughtExceptionHandler(new FuckgramCrashHandler(context));
        } catch (Throwable ignore) {
        }
    }

    /** True inside the dedicated crash-screen process: the Telegram client must not start there. */
    public static boolean isCrashProcess() {
        try (FileInputStream in = new FileInputStream("/proc/self/cmdline")) {
            byte[] buffer = new byte[256];
            int read = in.read(buffer);
            if (read <= 0) {
                return false;
            }
            int end = 0;
            while (end < read && buffer[end] != 0) {
                end++;
            }
            return new String(buffer, 0, end, "UTF-8").endsWith(CRASH_PROCESS_SUFFIX);
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Java crashes are caught by the handler below. Native crashes (SIGSEGV...) are not, so on the next start we ask
     * the system why the previous process died (Android 11+) and show that.
     */
    public static void reportPreviousNativeCrash(Context context) {
        if (Build.VERSION.SDK_INT < 30) {
            return;
        }
        try {
            android.app.ActivityManager am = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            android.content.SharedPreferences prefs = context.getSharedPreferences("fuckgram_crash", Context.MODE_PRIVATE);
            long seen = prefs.getLong("last_exit_ts", 0);
            long newest = seen;
            String report = null;
            for (android.app.ApplicationExitInfo info : am.getHistoricalProcessExitReasons(context.getPackageName(), 0, 6)) {
                if (info.getTimestamp() <= seen) {
                    continue;
                }
                newest = Math.max(newest, info.getTimestamp());
                int reason = info.getReason();
                if (report == null && (reason == android.app.ApplicationExitInfo.REASON_CRASH_NATIVE || reason == android.app.ApplicationExitInfo.REASON_INITIALIZATION_FAILURE)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("fuckgram previous exit report\n");
                    sb.append("Device: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL).append('\n');
                    sb.append("Android: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
                    sb.append("ABIs: ").append(android.text.TextUtils.join(",", Build.SUPPORTED_ABIS)).append('\n');
                    sb.append("Reason: ").append(reason == android.app.ApplicationExitInfo.REASON_CRASH_NATIVE ? "NATIVE CRASH" : "INITIALIZATION FAILURE").append('\n');
                    sb.append("Process: ").append(info.getProcessName()).append('\n');
                    sb.append("Status/signal: ").append(info.getStatus()).append('\n');
                    sb.append("Description: ").append(info.getDescription()).append("\n\n");
                    try (java.io.InputStream trace = info.getTraceInputStream()) {
                        if (trace != null) {
                            byte[] data = new byte[200000];
                            int total = 0;
                            int n;
                            while (total < data.length && (n = trace.read(data, total, data.length - total)) > 0) {
                                total += n;
                            }
                            // tombstone is binary: keep only readable strings (library and function names)
                            StringBuilder run = new StringBuilder();
                            StringBuilder out = new StringBuilder();
                            for (int i = 0; i < total && out.length() < 6000; i++) {
                                char c = (char) (data[i] & 0xff);
                                if (c >= 32 && c < 127) {
                                    run.append(c);
                                } else {
                                    if (run.length() >= 5) {
                                        out.append(run).append('\n');
                                    }
                                    run.setLength(0);
                                }
                            }
                            sb.append("Trace strings:\n").append(out);
                        }
                    } catch (Throwable ignore) {
                    }
                    report = sb.toString();
                }
            }
            prefs.edit().putLong("last_exit_ts", newest).apply();
            if (report != null) {
                Intent intent = new Intent();
                intent.setClassName(context.getPackageName(), CRASH_ACTIVITY);
                intent.putExtra(EXTRA_REPORT, report.length() > 50000 ? report.substring(0, 50000) : report);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            StringWriter writer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(writer));
            String report = "fuckgram crash report\n"
                    + "Device: " + Build.MANUFACTURER + " " + Build.MODEL + "\n"
                    + "Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\n"
                    + "ABIs: " + android.text.TextUtils.join(",", Build.SUPPORTED_ABIS) + "\n"
                    + "Thread: " + thread.getName() + "\n\n"
                    + writer;
            if (report.length() > 50000) {
                report = report.substring(0, 50000);
            }
            try (FileOutputStream out = new FileOutputStream(new File(context.getFilesDir(), "last_crash.txt"))) {
                out.write(report.getBytes("UTF-8"));
            } catch (Throwable ignore) {
            }
            Intent intent = new Intent();
            intent.setClassName(context.getPackageName(), CRASH_ACTIVITY);
            intent.putExtra(EXTRA_REPORT, report);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            Thread.sleep(400);
        } catch (Throwable ignore) {
        }
        Process.killProcess(Process.myPid());
        System.exit(10);
    }
}
