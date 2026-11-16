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
import org.telegram.messenger.FuckgramColors;
import org.telegram.messenger.FuckgramConfig;
import org.telegram.messenger.FuckgramCrypto;
import org.telegram.messenger.FuckgramDecoy;
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

    public static final int MENU = 0, PRIVACY = 1, MESSAGES = 2, LOOK = 3, PROFILE = 4, PACKS = 5, CLEAN = 6, COLORS = 7, ANIM = 8, GHOSTPASS = 9;

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
            case CLEAN: return t("Clean-up", "Убрать лишнее");
            case GHOSTPASS: return t("Ghost password & spaces", "Пароль-призрак и пространства");
            case COLORS: return t("My colors", "Свои цвета");
            case ANIM: return t("Animations", "Анимации");
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
                addHeader(context, t("Font", "Шрифт"));
                addSwitch(context, FuckgramConfig.TG_FONT, t("Telegram font (Roboto)", "Шрифт Telegram (Roboto)"), false);
                addInfo(context, t("Instead of Inter. The font changes after restarting the app.", "Вместо Inter. Шрифт сменится после перезапуска приложения."));
                addButton(context, t("My colors", "Свои цвета"), true, v -> presentFragment(new FuckgramSettingsActivity(COLORS)));
                addButton(context, t("Animations", "Анимации"), false, v -> presentFragment(new FuckgramSettingsActivity(ANIM)));
                addHeader(context, t("Backgrounds (photo or video)", "Фоны (фото или видео)"));
                addBgArea(context, FuckgramBackground.AREA_CHAT, t("Chats", "Чаты"));
                addBgArea(context, FuckgramBackground.AREA_LIST, t("Chat list", "Список чатов"));
                addValue(context, t("Dimming", "Затемнение"), level(FuckgramConfig.getInt(FuckgramConfig.BG_DIM)), false,
                        v -> pick(context, t("Dimming", "Затемнение"), new String[]{t("Light", "Слабое"), t("Medium", "Среднее"), t("Strong", "Сильное")}, FuckgramConfig.BG_DIM, 1, null));
                addInfo(context, t("Each screen has its own photo or video (video is muted and pauses when the screen is closed). Reopen the screen to see a change.",
                        "У каждого экрана свой фон — фото или видео (видео без звука, на паузе, когда экран закрыт). Чтобы увидеть изменения, откройте экран заново."));
                break;
            case CLEAN:
                addHeader(context, t("Hide from the app", "Скрыть в приложении"));
                addSwitch(context, FuckgramConfig.HIDE_ADS, t("Official Telegram ads", "Официальную рекламу Telegram"), true);
                addSwitch(context, FuckgramConfig.HIDE_PREMIUM, t("Telegram Premium", "Telegram Premium"), true);
                addSwitch(context, FuckgramConfig.HIDE_BUSINESS, t("Telegram Business", "Telegram Business"), true);
                addSwitch(context, FuckgramConfig.HIDE_STARS, t("Stars and TON", "Звёзды и TON"), true);
                addSwitch(context, FuckgramConfig.HIDE_GIFTS, t("Gifts", "Подарки"), true);
                addSwitch(context, FuckgramConfig.HIDE_STORIES, t("Stories", "Истории"), true);
                addSwitch(context, FuckgramConfig.HIDE_WALLET, t("Wallet", "Кошелёк"), false);
                addInfo(context, t("Each switch hides only its own part. Some screens update after the app is restarted.",
                        "Каждый переключатель скрывает только свою часть. Часть экранов обновится после перезапуска приложения."));
                break;
            case GHOSTPASS:
                addHeader(context, t("Ghost password", "Пароль-призрак"));
                addValue(context, t("Ghost password", "Пароль-призрак"), FuckgramDecoy.hasCode() ? t("set", "задан") : t("not set", "не задан"), true, v -> askGhostCode(context));
                if (FuckgramDecoy.hasCode()) {
                    addButton(context, t("Remove ghost password", "Удалить пароль-призрак"), true, v -> {
                        FuckgramDecoy.clearCode();
                        build(context);
                    });
                }
                addButton(context, t("Chats shown in quiet mode", "Чаты, видимые в тихом режиме"), false, v -> pickQuietChats(context));
                addInfo(context, t("Enter the ghost password on the lock screen instead of your real code: the app opens quietly, only Saved Messages and the chats you allowed are visible, and notifications from the others are silent. Your real code brings everything back. A passcode must be turned on (Settings → Privacy).",
                        "Если висит код-пароль, введите пароль-призрак вместо настоящего: откроется второе пространство — видны только «Избранное» и разрешённые вами чаты, уведомления от остальных не приходят. Настоящий код открывает основное. Код-пароль должен быть включён (Настройки → Конфиденциальность). Без пароля пространства переключаются двумя тапами по логотипу."));
                addHeader(context, t("Space switch", "Переключатель пространств"));
                addInfo(context, t("Tap the «fuckgram» logo in the chat list three times quickly: open chats close, the app switches to the quiet mode and locks. Enter your real code to come back. Needs a passcode.",
                        "Быстро нажмите два раза на логотип «fuckgram» в списке чатов: открытые чаты закроются и приложение переключится между основным и вторым пространством (тихим режимом). Пароль не спрашивается, код-пароль для этого не нужен. Ещё два нажатия вернут обратно."));
                addHeader(context, t("Text in pictures", "Текст в картинках"));
                addInfo(context, t("Long-press the send button → «Hide text in a picture». The picture goes as a file (Telegram would squeeze a photo and erase the hidden text). A friend with fuckgram long-presses the file → «Reveal hidden text». The text is encrypted with the chat passphrase or the built-in key.",
                        "Зажмите кнопку отправки → «Спрятать текст в картинке». Картинка уходит файлом (Telegram пережимает фото и стирает скрытый текст). Друг с fuckgram зажимает файл → «Проявить скрытый текст». Текст шифруется паролем чата или встроенным ключом."));
                break;
            case COLORS:
                addHeader(context, t("Your own colors", "Свои цвета"));
                addColorRow(context, FuckgramColors.ACCENT, t("Accent (buttons, links, headers)", "Акцент (кнопки, ссылки, заголовки)"));
                addColorRow(context, FuckgramColors.OUT, t("My messages", "Мои сообщения"));
                addColorRow(context, FuckgramColors.IN, t("Their messages", "Сообщения собеседника"));
                addColorRow(context, FuckgramColors.BG, t("Background", "Фон"));
                addColorRow(context, FuckgramColors.TEXT, t("Text", "Текст"));
                addButton(context, t("Reset colors", "Сбросить цвета"), false, v -> {
                    FuckgramColors.reset();
                    applyTheme();
                    build(context);
                });
                addInfo(context, t("Tap a color and type it as #RRGGBB. A color that is not set follows the theme.",
                        "Нажмите на цвет и введите его как #RRGGBB. Не заданный цвет берётся из темы."));
                break;
            case ANIM:
                addHeader(context, t("Screen transitions", "Переходы между экранами"));
                addValue(context, t("Pack", "Пак"), transitionName(FuckgramConfig.getInt(FuckgramConfig.TRANSITION)), true,
                        v -> pick(context, t("Pack", "Пак"), new String[]{transitionName(0), transitionName(1), transitionName(2), transitionName(3), transitionName(4), transitionName(5)}, FuckgramConfig.TRANSITION, 0, null));
                addValue(context, t("Speed", "Скорость"), (FuckgramConfig.getInt(FuckgramConfig.TRANSITION_SPEED) <= 1 ? t("Fast", "Быстро") : FuckgramConfig.getInt(FuckgramConfig.TRANSITION_SPEED) == 2 ? t("Medium", "Средне") : t("Slow", "Медленно")), false,
                        v -> pick(context, t("Speed", "Скорость"), new String[]{t("Fast", "Быстро"), t("Medium", "Средне"), t("Slow", "Медленно")}, FuckgramConfig.TRANSITION_SPEED, 1, null));
                addInfo(context, t("Applies to opening and closing screens everywhere in the app.", "Работает при открытии и закрытии экранов во всём приложении."));
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
                addButton(context, t("Ghost password & spaces", "Пароль-призрак и пространства"), true, v -> presentFragment(new FuckgramSettingsActivity(GHOSTPASS)));
                addButton(context, t("Clean-up (hide ads, Premium…)", "Убрать лишнее (реклама, Premium…)"), true, v -> presentFragment(new FuckgramSettingsActivity(CLEAN)));
                addButton(context, t("Badge", "Значок fuckgram"), true, v -> presentFragment(new FuckgramSettingsActivity(PROFILE)));
                addButton(context, t("Settings packs", "Пакеты настроек"), true, v -> presentFragment(new FuckgramSettingsActivity(PACKS)));
                addButton(context, t("About fuckgram", "О fuckgram"), false, v -> presentFragment(new FuckgramAboutActivity()));
                addInfo(context, "");
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode < 9701 || requestCode > 9703 || resultCode != android.app.Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        final int area = requestCode - 9701;
        final android.net.Uri uri = data.getData();
        final Context context = getParentActivity();
        if (context == null) {
            return;
        }
        String type = context.getContentResolver().getType(uri);
        final boolean video = type != null && type.startsWith("video");
        org.telegram.messenger.Utilities.globalQueue.postRunnable(() -> {
            boolean ok = FuckgramBackground.importFrom(context, uri, video, area);
            AndroidUtilities.runOnUIThread(() -> {
                Toast.makeText(context, ok ? t("Background set", "Фон установлен") : t("Could not read the file", "Не удалось прочитать файл"), Toast.LENGTH_SHORT).show();
                if (fragmentView != null) {
                    build(context);
                }
            });
        });
    }

    private void askGhostCode(Context context) {
        if (org.telegram.messenger.SharedConfig.passcodeHash == null || org.telegram.messenger.SharedConfig.passcodeHash.length() == 0) {
            Toast.makeText(context, t("Turn on the passcode first (Settings → Privacy)", "Сначала включите код-пароль (Настройки → Конфиденциальность)"), Toast.LENGTH_LONG).show();
            return;
        }
        final android.widget.EditText input = new android.widget.EditText(context);
        input.setSingleLine(true);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint(t("New ghost password", "Новый пароль-призрак"));
        input.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(t("Ghost password", "Пароль-призрак"));
        builder.setView(input);
        builder.setPositiveButton("OK", (d, w) -> {
            String code = input.getText().toString();
            if (code.length() < 2) {
                Toast.makeText(context, t("Too short", "Слишком короткий"), Toast.LENGTH_SHORT).show();
                return;
            }
            if (org.telegram.messenger.SharedConfig.checkPasscode(code)) {
                Toast.makeText(context, t("It must differ from your real code", "Он должен отличаться от настоящего кода"), Toast.LENGTH_LONG).show();
                return;
            }
            FuckgramDecoy.setCode(code);
            if (fragmentView != null) {
                build(context);
            }
        });
        builder.setNegativeButton(t("Cancel", "Отмена"), null);
        showDialog(builder.create());
    }

    private void pickQuietChats(Context context) {
        final java.util.ArrayList<org.telegram.tgnet.TLRPC.Dialog> all = getMessagesController().getAllDialogs();
        final java.util.ArrayList<Long> ids = new java.util.ArrayList<>();
        final java.util.ArrayList<String> names = new java.util.ArrayList<>();
        for (int i = 0; i < all.size() && ids.size() < 60; i++) {
            org.telegram.tgnet.TLRPC.Dialog dialog = all.get(i);
            if (dialog == null || dialog.id == 0 || dialog.id == getUserConfig().getClientUserId()) {
                continue;
            }
            String name = org.telegram.messenger.DialogObject.getName(currentAccount, dialog.id);
            if (name == null || name.isEmpty()) {
                continue;
            }
            ids.add(dialog.id);
            names.add(name);
        }
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < ids.size(); i++) {
            final long id = ids.get(i);
            TextCheckCell cell = new TextCheckCell(context);
            cell.setTextAndCheck(names.get(i), FuckgramDecoy.isAllowedChat(id), true);
            cell.setOnClickListener(v -> {
                boolean value = !cell.isChecked();
                FuckgramDecoy.setAllowedChat(id, value);
                cell.setChecked(value);
            });
            box.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }
        ScrollView scroll = new ScrollView(context);
        scroll.addView(box);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(t("Chats shown in quiet mode", "Чаты, видимые в тихом режиме"));
        builder.setView(scroll);
        builder.setPositiveButton("OK", null);
        showDialog(builder.create());
    }

    private void addColorRow(Context context, int role, String title) {
        int color = FuckgramColors.role(role);
        addValue(context, title, color == 0 ? t("theme", "из темы") : String.format("#%06X", color & 0xFFFFFF), true, v -> {
            final android.widget.EditText input = new android.widget.EditText(context);
            input.setSingleLine(true);
            input.setHint("#RRGGBB");
            int cur = FuckgramColors.role(role);
            input.setText(cur == 0 ? "" : String.format("#%06X", cur & 0xFFFFFF));
            input.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setView(input);
            builder.setPositiveButton("OK", (d, w) -> {
                String text = input.getText().toString().trim();
                try {
                    if (text.isEmpty()) {
                        FuckgramColors.setRole(role, 0);
                    } else {
                        if (!text.startsWith("#")) {
                            text = "#" + text;
                        }
                        FuckgramColors.setRole(role, android.graphics.Color.parseColor(text) | 0xFF000000);
                    }
                } catch (Throwable e) {
                    Toast.makeText(context, t("Wrong color", "Неверный цвет"), Toast.LENGTH_SHORT).show();
                    return;
                }
                applyTheme();
                if (fragmentView != null) {
                    build(context);
                }
            });
            builder.setNegativeButton(t("Cancel", "Отмена"), null);
            showDialog(builder.create());
        });
    }

    private void addBgArea(Context context, int area, String title) {
        String now = FuckgramBackground.isActive(area) ? (FuckgramBackground.kind(area) == FuckgramBackground.VIDEO ? t("Video", "Видео") : t("Photo", "Фото")) : t("None", "Нет");
        addValue(context, title, now, true, v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, t("Choose photo or video", "Выбрать фото или видео")), 9701 + area);
        });
        if (FuckgramBackground.isActive(area)) {
            addButton(context, t("Remove: ", "Убрать: ") + title, true, v -> {
                FuckgramBackground.remove(area);
                build(context);
            });
        }
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
            case 4: return t("Wide slide", "Широкий сдвиг");
            case 5: return t("Drop", "Сверху вниз");
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
