package org.telegram.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FuckgramConfig;
import org.telegram.messenger.FuckgramCrypto;
import org.telegram.messenger.FuckgramText;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;

/**
 * fuckgram: settings. The first screen is a menu of categories, every category is its own screen.
 */
public class FuckgramSettingsActivity extends BaseFragment {

    public static final int MENU = 0, PRIVACY = 1, MESSAGES = 2, LOOK = 3, PROFILE = 4, PACKS = 5;

    private final int category;
    private LinearLayout list;

    public FuckgramSettingsActivity() {
        this(MENU);
    }

    public FuckgramSettingsActivity(int category) {
        this.category = category;
    }

    private static String t(String en, String ru) {
        return FuckgramText.t(en, ru);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(titleOf(category));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = scrollView;

        list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(list, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        build(context);
        return fragmentView;
    }

    private static String titleOf(int c) {
        switch (c) {
            case PRIVACY: return t("Privacy & ghost mode", "Приватность и призрак");
            case MESSAGES: return t("Messages & encryption", "Сообщения и шифрование");
            case LOOK: return t("Appearance & animations", "Оформление и анимации");
            case PROFILE: return t("Badge", "Значок fuckgram");
            case PACKS: return t("Settings packs", "Пакеты настроек");
            default: return t("fuckgram settings", "Настройки fuckgram");
        }
    }

    private void build(Context context) {
        list.removeAllViews();
        switch (category) {
            case PRIVACY:
                addHeader(context, t("Ghost mode", "Режим призрака"));
                addSwitch(context, FuckgramConfig.GHOST_NO_READ, t("Don't send read receipts", "Не отмечать сообщения прочитанными"), true);
                addSwitch(context, FuckgramConfig.GHOST_NO_TYPING, t("Don't show «typing…»", "Не показывать «печатает…»"), true);
                addSwitch(context, FuckgramConfig.GHOST_OFFLINE, t("Always appear offline", "Всегда быть «не в сети»"), false);
                addInfo(context, t("Messages stay unread for the sender until you turn this off. Works on this device.",
                        "Собеседник не увидит, что вы прочитали сообщение, пока вы это не выключите. Работает на этом устройстве."));
                break;
            case MESSAGES:
                addHeader(context, t("Deleted messages", "Удалённые сообщения"));
                addSwitch(context, FuckgramConfig.KEEP_DELETED, t("Keep messages deleted by others", "Сохранять сообщения, удалённые собеседником"), false);
                addInfo(context, t("A deleted message stays in the chat with a «deleted» mark next to the time (it appears after the chat is reopened).",
                        "Удалённое сообщение остаётся в чате с пометкой «удалено» у времени (видна после повторного открытия чата)."));
                addHeader(context, t("fg encryption", "fg-шифрование"));
                addSwitch(context, FuckgramConfig.ENC_ENABLED, t("Enable fg encryption", "Включить fg-шифрование"), true);
                addValue(context, t("Default passphrase", "Общий пароль"), FuckgramCrypto.getDefaultPassphrase() == null ? t("built-in key", "встроенный ключ") : "••••", false, v -> askDefaultPassphrase(context));
                addInfo(context, t("Long-press the send button → «Send fg-encrypted». fuckgram users see normal text with a small mark, everyone else sees a pile of symbols. Without a passphrase a built-in key is used (not secret, because the app is open source). For real protection set a passphrase in the same menu for a chat, and give it to your friend outside Telegram.",
                        "Зажмите кнопку отправки → «Отправить fg-шифром». Пользователи fuckgram увидят обычный текст с маленькой меткой, остальные — набор символов. Без пароля используется встроенный ключ (он не секретный, ведь приложение с открытым кодом). Для настоящей защиты задайте пароль чата в том же меню и передайте его другу вне Telegram."));
                break;
            case LOOK:
                addHeader(context, t("Theme", "Тема"));
                addValue(context, t("Theme", "Тема"), FuckgramConfig.getInt(FuckgramConfig.THEME_MODE) == 0 ? t("Black", "Чёрная") : t("White", "Белая"), false,
                        v -> pick(context, t("Theme", "Тема"), new String[]{t("Black", "Чёрная"), t("White", "Белая")}, FuckgramConfig.THEME_MODE, 0, () -> applyTheme()));
                addHeader(context, t("Chat background", "Фон чатов"));
                addValue(context, t("Current", "Сейчас"), FuckgramBackground.isActive() ? (FuckgramConfig.getInt(FuckgramConfig.BG_KIND) == FuckgramBackground.VIDEO ? t("Video", "Видео") : t("Photo", "Фото")) : t("None", "Нет"), true, v -> {});
                addButton(context, t("Choose photo or video", "Выбрать фото или видео"), true, v -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(Intent.createChooser(intent, t("Choose photo or video", "Выбрать фото или видео")), 9701);
                });
                addButton(context, t("Remove background", "Убрать фон"), true, v -> {
                    FuckgramBackground.remove();
                    build(context);
                });
                addValue(context, t("Dimming", "Затемнение"), level(FuckgramConfig.getInt(FuckgramConfig.BG_DIM)), false,
                        v -> pick(context, t("Dimming", "Затемнение"), new String[]{t("Light", "Слабое"), t("Medium", "Среднее"), t("Strong", "Сильное")}, FuckgramConfig.BG_DIM, 1, null));
                addInfo(context, t("The photo or video is shown behind the messages in every chat (video is muted and pauses when the chat is not on screen). Reopen the chat to see a change.",
                        "Фото или видео показывается за сообщениями во всех чатах (видео без звука и ставится на паузу, когда чат закрыт). Чтобы увидеть изменения, откройте чат заново."));
                addInfo(context, t("Live backgrounds and custom transitions are coming in the next versions.", "Живые фоны и свои переходы появятся в следующих версиях."));
                break;
            case PROFILE:
                addHeader(context, t("Badge", "Значок"));
                addSwitch(context, FuckgramConfig.SHOW_BADGE, t("Show fuckgram badges", "Показывать значки fuckgram"), false);
                addInfo(context, t("Shows the fuckgram badge next to your name and next to the names of other fuckgram users (in profiles). This switch only changes what YOU see: other fuckgram users still see your badge. To let clients recognise each other, fuckgram adds invisible characters at the end of your last name.",
                        "Показывает значок fuckgram у вашего имени и у имён других пользователей fuckgram (в профилях). Переключатель меняет только то, что видите ВЫ: другие пользователи fuckgram по-прежнему видят ваш значок. Чтобы клиенты узнавали друг друга, fuckgram добавляет в конец фамилии невидимые символы."));
                break;
            case PACKS:
                addHeader(context, t("Settings pack", "Пакет настроек"));
                addButton(context, t("Share pack", "Отправить пакет другу"), true, v -> sharePack(context));
                addButton(context, t("Copy pack", "Скопировать пакет"), true, v -> exportPack(context));
                addButton(context, t("Import pack from clipboard", "Импорт пакета из буфера"), false, v -> importPack(context));
                addInfo(context, t("A pack contains all fuckgram settings (theme, animations, ghost mode…) as text. Passphrases are never included. Your friend copies the text and taps «Import».",
                        "Пакет содержит все настройки fuckgram (тема, анимации, призрак…) в виде текста. Пароли в пакет не попадают. Друг копирует текст и нажимает «Импорт»."));
                break;
            default:
                addHeader(context, "fuckgram");
                addButton(context, t("Privacy & ghost mode", "Приватность и призрак"), true, v -> presentFragment(new FuckgramSettingsActivity(PRIVACY)));
                addButton(context, t("Messages & encryption", "Сообщения и шифрование"), true, v -> presentFragment(new FuckgramSettingsActivity(MESSAGES)));
                addButton(context, t("Appearance & animations", "Оформление и анимации"), true, v -> presentFragment(new FuckgramSettingsActivity(LOOK)));
                addButton(context, t("Badge", "Значок fuckgram"), true, v -> presentFragment(new FuckgramSettingsActivity(PROFILE)));
                addButton(context, t("Settings packs", "Пакеты настроек"), true, v -> presentFragment(new FuckgramSettingsActivity(PACKS)));
                addButton(context, t("About fuckgram", "О fuckgram"), false, v -> presentFragment(new FuckgramAboutActivity()));
                addInfo(context, "");
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode != 9701 || resultCode != android.app.Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        final android.net.Uri uri = data.getData();
        final Context context = getParentActivity();
        if (context == null) {
            return;
        }
        String type = context.getContentResolver().getType(uri);
        final boolean video = type != null && type.startsWith("video");
        org.telegram.messenger.Utilities.globalQueue.postRunnable(() -> {
            boolean ok = FuckgramBackground.importFrom(context, uri, video);
            AndroidUtilities.runOnUIThread(() -> {
                Toast.makeText(context, ok ? t("Background set", "Фон установлен") : t("Could not read the file", "Не удалось прочитать файл"), Toast.LENGTH_SHORT).show();
                if (fragmentView != null) {
                    build(context);
                }
            });
        });
    }

    private String[] levels() {
        return new String[]{t("Low", "Мало / медленно"), t("Medium", "Средне"), t("High", "Много / быстро")};
    }

    private static String level(int v) {
        return v <= 1 ? t("Low", "Мало") : v == 2 ? t("Medium", "Средне") : t("High", "Много");
    }

    private static String liveName(int v) {
        return v == 0 ? t("Off", "Выключен") : v == 1 ? t("Gradient", "Градиент") : t("Gradient + particles", "Градиент + частицы");
    }

    private static String transitionName(int v) {
        switch (v) {
            case 1: return t("Fade", "Затухание");
            case 2: return t("Zoom", "Масштаб");
            case 3: return t("Slide up", "Снизу вверх");
            default: return t("Default", "Стандартный");
        }
    }

    /** Applies the black / white theme chosen in the settings. */
    public static void applyTheme() {
        Theme.ThemeInfo info = Theme.getTheme(FuckgramConfig.getInt(FuckgramConfig.THEME_MODE) == 0 ? "Night" : "Day");
        if (info != null) {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, info, false, null, -1);
        }
    }

    private void pick(Context context, String title, String[] items, String key, int firstValue, Runnable after) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setItems(items, (d, which) -> {
            FuckgramConfig.setInt(key, which + firstValue);
            if (after != null) {
                after.run();
            }
            if (fragmentView != null) {
                build(context);
            }
        });
        builder.setNegativeButton(t("Cancel", "Отмена"), null);
        showDialog(builder.create());
    }

    private void askDefaultPassphrase(Context context) {
        FuckgramEncryptionUi.showDefaultPassphraseDialog(context, () -> build(context));
    }

    private void addHeader(Context context, String text) {
        TextView header = new TextView(context);
        header.setText(text);
        header.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        header.setTypeface(AndroidUtilities.bold());
        header.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        header.setPadding(AndroidUtilities.dp(21), AndroidUtilities.dp(16), AndroidUtilities.dp(21), AndroidUtilities.dp(8));
        list.addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void addInfo(Context context, String text) {
        TextView info = new TextView(context);
        info.setText(text);
        info.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        info.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        info.setGravity(Gravity.LEFT);
        info.setPadding(AndroidUtilities.dp(21), AndroidUtilities.dp(8), AndroidUtilities.dp(21), AndroidUtilities.dp(16));
        list.addView(info, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void addSwitch(Context context, String key, String title, boolean divider) {
        TextCheckCell cell = new TextCheckCell(context);
        cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        cell.setTextAndCheck(title, FuckgramConfig.get(key), divider);
        cell.setOnClickListener(v -> {
            boolean value = !cell.isChecked();
            FuckgramConfig.set(key, value);
            cell.setChecked(value);
        });
        list.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void addValue(Context context, String title, String value, boolean divider, View.OnClickListener listener) {
        TextCell cell = new TextCell(context);
        cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        cell.setTextAndValue(title, value, divider);
        cell.setOnClickListener(listener);
        list.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void addButton(Context context, String title, boolean divider, View.OnClickListener listener) {
        TextCell cell = new TextCell(context);
        cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        cell.setText(title, divider);
        cell.setOnClickListener(listener);
        list.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void sharePack(Context context) {
        String json = FuckgramConfig.exportPack();
        if (json == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, json);
        context.startActivity(Intent.createChooser(intent, t("Share pack", "Отправить пакет")));
    }

    private void exportPack(Context context) {
        String json = FuckgramConfig.exportPack();
        if (json == null) {
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("fuckgram", json));
            Toast.makeText(context, t("Copied", "Скопировано"), Toast.LENGTH_SHORT).show();
        }
    }

    private void importPack(Context context) {
        String text = null;
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
                CharSequence cs = clipboard.getPrimaryClip().getItemAt(0).coerceToText(context);
                text = cs != null ? cs.toString() : null;
            }
        } catch (Throwable ignore) {
        }
        boolean ok = text != null && FuckgramConfig.importPack(text);
        Toast.makeText(context, ok ? t("Imported", "Импортировано") : t("No valid pack in clipboard", "В буфере нет пакета"), Toast.LENGTH_SHORT).show();
        if (ok) {
            applyTheme();
            build(context);
        }
    }
}
