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
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.FuckgramColors;
import org.telegram.messenger.FuckgramConfig;
import org.telegram.messenger.FuckgramCrypto;
import org.telegram.messenger.FuckgramBotBlock;
import org.telegram.messenger.FuckgramDecoy;
import org.telegram.messenger.FuckgramProfiles;
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

    public static final int MENU = 0, PRIVACY = 1, MESSAGES = 2, LOOK = 3, PROFILE = 4, PACKS = 5, CLEAN = 6, COLORS = 7, ANIM = 8, GHOSTPASS = 9, BOTBLOCK = 10, BROADCAST = 11, PROFILES = 12, GUIDE = 13, LAYOUT = 14;

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
            case LOOK: return t("Appearance", "Оформление");
            case PROFILE: return t("Badge", "Значок fuckgram");
            case PACKS: return t("Settings packs", "Пакеты настроек");
            case CLEAN: return t("Clean-up", "Убрать лишнее");
            case BOTBLOCK: return t("Bot ban", "Запрет на ботов");
            case BROADCAST: return t("Broadcast", "Рассылка");
            case PROFILES: return t("Profile presets", "Конфиги профиля");
            case GUIDE: return t("Handbook", "Справочник");
            case LAYOUT: return t("Layout", "Расположение");
            case GHOSTPASS: return t("Second space", "Второе пространство");
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
                addInfo(context, t("The sender does not see that you read it.", "Собеседник не увидит, что вы прочитали."));
                break;
            case MESSAGES:
                addHeader(context, t("Deleted messages", "Удалённые сообщения"));
                addSwitch(context, FuckgramConfig.KEEP_DELETED, t("Keep messages deleted by others", "Сохранять сообщения, удалённые собеседником"), true);
                addSwitch(context, FuckgramConfig.SHOW_SECONDS, t("Seconds in message time", "Секунды во времени сообщений"), true);
                addSwitch(context, FuckgramConfig.SHOW_MSG_ID, t("Message ID next to the time", "ID сообщения рядом со временем"), false);
                addInfo(context, t("Deleted messages stay with a mark.", "Удалённое остаётся в чате с пометкой."));
                addHeader(context, t("fg encryption", "fg-шифрование"));
                addSwitch(context, FuckgramConfig.ENC_ENABLED, t("Enable fg encryption", "Включить fg-шифрование"), true);
                addValue(context, t("Default passphrase", "Общий пароль"), FuckgramCrypto.getDefaultPassphrase() == null ? t("built-in key", "встроенный ключ") : "••••", false, v -> askDefaultPassphrase(context));
                addInfo(context, t("Hold the send button → «Send fg-encrypted».", "Зажмите кнопку отправки → «Отправить fg-шифром»."));
                break;
            case LOOK:
                addHeader(context, t("Theme", "Тема"));
                addValue(context, t("Theme", "Тема"), FuckgramConfig.getInt(FuckgramConfig.THEME_MODE) == 0 ? t("Black", "Чёрная") : t("White", "Белая"), false,
                        v -> pick(context, t("Theme", "Тема"), new String[]{t("Black", "Чёрная"), t("White", "Белая")}, FuckgramConfig.THEME_MODE, 0, () -> applyTheme()));
                addHeader(context, t("Font", "Шрифт"));
                addSwitch(context, FuckgramConfig.TG_FONT, t("Telegram font (needs restart)", "Шрифт Telegram (после перезапуска)"), true);
                addSwitch(context, FuckgramConfig.SHOW_BADGE, t("Show fuckgram badges", "Значки fuckgram"), false);
                addButton(context, t("My colors", "Свои цвета"), true, v -> presentFragment(new FuckgramSettingsActivity(COLORS)));
                addButton(context, t("Animations", "Анимации"), false, v -> presentFragment(new FuckgramSettingsActivity(ANIM)));
                addHeader(context, t("Backgrounds (photo or video)", "Фоны (фото или видео)"));
                addBgArea(context, FuckgramBackground.AREA_CHAT, t("Chats", "Чаты"));
                addBgArea(context, FuckgramBackground.AREA_LIST, t("Chat list", "Список чатов"));
                addValue(context, t("Dimming", "Затемнение"), level(FuckgramConfig.getInt(FuckgramConfig.BG_DIM)), false,
                        v -> pick(context, t("Dimming", "Затемнение"), new String[]{t("Light", "Слабое"), t("Medium", "Среднее"), t("Strong", "Сильное")}, FuckgramConfig.BG_DIM, 1, null));
                break;
            case CLEAN:
                addHeader(context, t("Hide from the app", "Скрыть в приложении"));
                addSwitch(context, FuckgramConfig.HIDE_ADS, t("Official Telegram ads", "Официальную рекламу Telegram"), true);
                addSwitch(context, FuckgramConfig.HIDE_PREMIUM, t("Telegram Premium", "Telegram Premium"), true);
                addSwitch(context, FuckgramConfig.HIDE_BUSINESS, t("Telegram Business", "Telegram Business"), true);
                addSwitch(context, FuckgramConfig.HIDE_STARS, t("Stars and TON", "Звёзды и TON"), true);
                addSwitch(context, FuckgramConfig.HIDE_GIFTS, t("Gifts", "Подарки"), true);
                addSwitch(context, FuckgramConfig.HIDE_STORIES, t("Stories", "Истории"), true);
                addSwitch(context, FuckgramConfig.HIDE_WALLET, t("Wallet", "Кошелёк"), true);
                addSwitch(context, FuckgramConfig.HIDE_PROXY_SPONSOR, t("Proxy sponsor", "Спонсор прокси"), false);
                addInfo(context, t("On = hidden. Applies at once.", "Включено = скрыто. Всё применяется сразу."));
                break;
            case BOTBLOCK: {
                boolean active = FuckgramBotBlock.isActive();
                addHeader(context, t("Self-ban on bots", "Самозапрет на ботов"));
                addValue(context, t("Status", "Статус"), active ? t("until ", "до ") + java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(new java.util.Date(FuckgramBotBlock.until())) : t("off", "выключен"), true, v -> {});
                final long cancelLeft = FuckgramBotBlock.cancelLeft();
                if (cancelLeft > 0) {
                    addButton(context, t("Cancel the ban (", "Отменить запрет (") + ((cancelLeft + 59999) / 60000) + t(" min left)", " мин осталось)"), true, v -> {
                        if (FuckgramBotBlock.cancel()) {
                            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload);
                        }
                        build(context);
                    });
                }
                for (final String name : FuckgramBotBlock.names()) {
                    addValue(context, "@" + name, active ? t("locked", "заблокирован") : t("tap to remove", "нажмите, чтобы убрать"), true, v -> {
                        if (FuckgramBotBlock.remove(name)) {
                            build(context);
                        } else {
                            Toast.makeText(context, t("The ban is running", "Запрет идёт"), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                addButton(context, t("Add a bot (@username)", "Добавить бота (@username)"), true, v -> promptText(context, t("Bot username", "Юзернейм бота"), "@username", "", false, text -> addBot(context, text)));
                addHeader(context, t("Start the ban for", "Включить запрет на"));
                final String[] labels = {t("15 minutes", "15 минут"), t("1 hour", "1 час"), t("6 hours", "6 часов"), t("1 day", "1 день"), t("7 days", "7 дней"), t("30 days", "30 дней")};
                final long[] spans = {15L * 60000, 3600000L, 6 * 3600000L, 24 * 3600000L, 7 * 24 * 3600000L, 30 * 24 * 3600000L};
                for (int a = 0; a < labels.length; a++) {
                    final int index = a;
                    addButton(context, labels[a], a < labels.length - 1, v -> {
                        if (FuckgramBotBlock.names().isEmpty()) {
                            Toast.makeText(context, t("Add at least one bot first", "Сначала добавьте хотя бы одного бота"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(t("Start the ban?", "Включить запрет?"));
                        builder.setMessage(t("It can be cancelled only in the first 5 minutes.", "Отменить можно только в первые 5 минут."));
                        builder.setPositiveButton(t("Start", "Включить"), (d, w) -> {
                            FuckgramBotBlock.start(spans[index]);
                            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload);
                            build(context);
                        });
                        builder.setNegativeButton(t("Cancel", "Отмена"), null);
                        showDialog(builder.create());
                    });
                }
                addInfo(context, t("Bots only: people and channels are never touched. Cancel is possible for 5 minutes after the start.", "Только боты: людей и каналы запрет не трогает. Отмена — первые 5 минут после включения."));
                break;
            }
            case BROADCAST: {
                addHeader(context, t("Broadcast to chosen chats", "Рассылка по выбранным чатам"));
                addValue(context, t("Recipients", "Получатели"), String.valueOf(broadcastIds.size()), true, v -> pickRecipients(context));
                addValue(context, t("Text", "Текст"), broadcastText.isEmpty() ? t("not set", "не задан") : broadcastText, true, v -> promptText(context, t("Text ({name} = first name)", "Текст ({имя} = имя)"), "", broadcastText, true, text -> {
                    broadcastText = text;
                    build(context);
                }));
                addButton(context, t("Send", "Отправить"), false, v -> startBroadcast(context));
                addInfo(context, t("Up to 100 people from your chats, one message per 2–4 s. {name} = first name.", "До 100 человек из ваших чатов, одно сообщение раз в 2–4 с. {имя} = имя."));
                break;
            }
            case PROFILES: {
                addHeader(context, t("Your presets", "Ваши конфиги"));
                for (final FuckgramProfiles.Preset preset : FuckgramProfiles.list()) {
                    addValue(context, preset.title, preset.first + (preset.hasAvatar ? " · " + t("avatar", "аватар") : ""), true, v -> presetActions(context, preset));
                }
                addButton(context, t("New preset", "Новый конфиг"), false, v -> editPreset(context, null));
                addInfo(context, t("Name, description and avatar in one tap.", "Имя, описание и аватар одним нажатием."));
                break;
            }
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
                addInfo(context, t("Enter it instead of the real passcode to open the second space.", "Введите его вместо настоящего кода, чтобы открыть второе пространство."));
                addHeader(context, t("Space switch", "Переключатель пространств"));
                addInfo(context, t("Two quick taps on the «fuckgram» logo switch the spaces. No password needed.", "Два быстрых нажатия на логотип «fuckgram» переключают пространства. Пароль не нужен."));
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
                addInfo(context, t("Type a color as #RRGGBB.", "Введите цвет как #RRGGBB."));
                break;
            case ANIM:
                addHeader(context, t("Screen transitions", "Переходы между экранами"));
                addValue(context, t("Pack", "Пак"), transitionName(FuckgramConfig.getInt(FuckgramConfig.TRANSITION)), true,
                        v -> pick(context, t("Pack", "Пак"), new String[]{transitionName(0), transitionName(1), transitionName(2), transitionName(3), transitionName(4), transitionName(5)}, FuckgramConfig.TRANSITION, 0, null));
                addValue(context, t("Speed", "Скорость"), (FuckgramConfig.getInt(FuckgramConfig.TRANSITION_SPEED) <= 1 ? t("Fast", "Быстро") : FuckgramConfig.getInt(FuckgramConfig.TRANSITION_SPEED) == 2 ? t("Medium", "Средне") : t("Slow", "Медленно")), false,
                        v -> pick(context, t("Speed", "Скорость"), new String[]{t("Fast", "Быстро"), t("Medium", "Средне"), t("Slow", "Медленно")}, FuckgramConfig.TRANSITION_SPEED, 1, null));
                break;
            case PROFILE:
                addHeader(context, t("Badge", "Значок"));
                addSwitch(context, FuckgramConfig.SHOW_BADGE, t("Show fuckgram badges", "Показывать значки fuckgram"), false);
                break;
            case PACKS:
                addHeader(context, t("Settings pack", "Пакет настроек"));
                addButton(context, t("Share pack", "Отправить пакет другу"), true, v -> sharePack(context));
                addButton(context, t("Copy pack", "Скопировать пакет"), true, v -> exportPack(context));
                addButton(context, t("Import pack from clipboard", "Импорт пакета из буфера"), false, v -> importPack(context));
                break;
            case LAYOUT: {
                addHeader(context, t("Where things are", "Где что находится"));
                addValue(context, t("Chat list rows", "Строки списка чатов"), SharedConfig.useThreeLinesLayout ? t("3 lines", "3 строки") : t("2 lines (Telegram)", "2 строки (как в Telegram)"), true,
                        v -> choose(context, t("Chat list rows", "Строки списка чатов"), new String[]{t("2 lines (Telegram)", "2 строки (как в Telegram)"), t("3 lines", "3 строки")}, SharedConfig.useThreeLinesLayout ? 1 : 0, i -> SharedConfig.setUseThreeLinesLayout(i == 1)));
                final int[] radii = {3, 10, 17, 30};
                final String[] radiusNames = {t("Square", "Квадратные"), t("Soft", "Мягкие"), t("Telegram", "Как в Telegram"), t("Round", "Круглые")};
                int radiusIndex = 2;
                for (int a = 0; a < radii.length; a++) {
                    if (radii[a] == SharedConfig.bubbleRadius) {
                        radiusIndex = a;
                    }
                }
                addValue(context, t("Message corners", "Углы сообщений"), radiusNames[radiusIndex], true,
                        v -> choose(context, t("Message corners", "Углы сообщений"), radiusNames, -1, i -> {
                            SharedConfig.bubbleRadius = radii[i];
                            MessagesController.getGlobalMainSettings().edit().putInt("bubbleRadius", radii[i]).commit();
                        }));
                final int[] sizes = {14, 16, 18, 20, 23};
                final String[] sizeNames = {t("Small", "Мелкий"), t("Telegram", "Как в Telegram"), t("Large", "Крупный"), t("Very large", "Очень крупный"), t("Huge", "Огромный")};
                int sizeIndex = 1;
                for (int a = 0; a < sizes.length; a++) {
                    if (sizes[a] == SharedConfig.fontSize) {
                        sizeIndex = a;
                    }
                }
                addValue(context, t("Message text size", "Размер текста сообщений"), sizeNames[sizeIndex], true,
                        v -> choose(context, t("Message text size", "Размер текста сообщений"), sizeNames, -1, i -> {
                            SharedConfig.fontSize = sizes[i];
                            SharedConfig.fontSizeIsDefault = false;
                            ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Context.MODE_PRIVATE).edit().putInt("fons_size", sizes[i]).commit();
                            Theme.createCommonMessageResources();
                        }));
                final String[] fabNames = {t("Right (Telegram)", "Справа (как в Telegram)"), t("Left", "Слева"), t("Center", "По центру")};
                addValue(context, t("Write button", "Кнопка «Написать»"), fabNames[Math.min(2, FuckgramConfig.getInt(FuckgramConfig.FAB_POS))], true,
                        v -> choose(context, t("Write button", "Кнопка «Написать»"), fabNames, -1, i -> FuckgramConfig.setInt(FuckgramConfig.FAB_POS, i)));
                addHeader(context, t("Weak phone", "Слабый телефон"));
                addSwitchLite(context);
                addButton(context, t("Back to Telegram layout", "Вернуть как в Telegram"), false, v -> {
                    SharedConfig.setUseThreeLinesLayout(false);
                    SharedConfig.bubbleRadius = 17;
                    MessagesController.getGlobalMainSettings().edit().putInt("bubbleRadius", 17).commit();
                    SharedConfig.fontSize = AndroidUtilities.isTablet() ? 18 : 16;
                    ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Context.MODE_PRIVATE).edit().putInt("fons_size", SharedConfig.fontSize).commit();
                    Theme.createCommonMessageResources();
                    FuckgramConfig.setInt(FuckgramConfig.FAB_POS, 0);
                    refreshAll();
                    build(context);
                });
                break;
            }
            case GUIDE:
                addHeader(context, t("Everything fuckgram can do", "Всё, что умеет fuckgram"));
                addGuide(context, t("Ghost mode", "Режим призрака"), t("No read receipts, no «typing…», always offline.", "Не читать, не печатать, всегда не в сети."), PRIVACY);
                addGuide(context, t("Deleted messages", "Удалённые сообщения"), t("Messages deleted by others stay with a mark.", "Удалённые собеседником остаются с пометкой."), MESSAGES);
                addGuide(context, t("fg encryption", "fg-шифрование"), t("Hold the send button. Text, pictures and files.", "Зажмите кнопку отправки. Текст, фото, файлы."), MESSAGES);
                addGuide(context, t("Text in a picture", "Текст в картинке"), t("Hold the send button → hide text; friend reveals it.", "Зажмите отправку → спрятать; друг проявит."), GHOSTPASS);
                addGuide(context, t("Second space", "Второе пространство"), t("Two taps on the logo or a second passcode.", "Два тапа по логотипу или второй код."), GHOSTPASS);
                addGuide(context, t("Look: theme, colors, animations", "Вид: тема, цвета, анимации"), t("Black or white theme, your colors, backgrounds.", "Чёрная/белая тема, свои цвета, фоны."), LOOK);
                addGuide(context, t("Layout", "Расположение"), t("Rows, corners, text size, button place, weak-phone mode.", "Строки, углы, размер текста, место кнопки, режим слабого телефона."), LAYOUT);
                addGuide(context, t("Clean-up", "Убрать лишнее"), t("Hide ads, Premium, Stars, gifts, stories, proxy sponsor.", "Реклама, Premium, звёзды, подарки, истории, спонсор прокси."), CLEAN);
                addGuide(context, t("Bot ban", "Запрет на ботов"), t("Lock chosen bots for a time. Cancel in 5 minutes.", "Заблокировать ботов на время. Отмена 5 минут."), BOTBLOCK);
                addGuide(context, t("Broadcast", "Рассылка"), t("One text to several chats.", "Один текст в несколько чатов."), BROADCAST);
                addGuide(context, t("Profile presets", "Конфиги профиля"), t("Switch name, bio and avatar in one tap.", "Имя, описание и аватар одним нажатием."), PROFILES);
                addGuide(context, t("Settings packs", "Пакеты настроек"), t("Share all settings as text.", "Все настройки текстом."), PACKS);
                break;
            default:
                addHeader(context, "fuckgram");
                addButton(context, t("Handbook", "Справочник"), true, v -> presentFragment(new FuckgramSettingsActivity(GUIDE)));
                addButton(context, t("Privacy & ghost mode", "Приватность и призрак"), true, v -> presentFragment(new FuckgramSettingsActivity(PRIVACY)));
                addButton(context, t("Messages & encryption", "Сообщения и шифрование"), true, v -> presentFragment(new FuckgramSettingsActivity(MESSAGES)));
                addButton(context, t("Layout", "Расположение"), true, v -> presentFragment(new FuckgramSettingsActivity(LAYOUT)));
                addButton(context, t("Appearance", "Оформление"), true, v -> presentFragment(new FuckgramSettingsActivity(LOOK)));
                addButton(context, t("Second space", "Второе пространство"), true, v -> presentFragment(new FuckgramSettingsActivity(GHOSTPASS)));
                addButton(context, t("Bot ban", "Запрет на ботов"), true, v -> presentFragment(new FuckgramSettingsActivity(BOTBLOCK)));
                addButton(context, t("Broadcast", "Рассылка"), true, v -> presentFragment(new FuckgramSettingsActivity(BROADCAST)));
                addButton(context, t("Profile presets", "Конфиги профиля"), true, v -> presentFragment(new FuckgramSettingsActivity(PROFILES)));
                addButton(context, t("Clean-up", "Убрать лишнее"), true, v -> presentFragment(new FuckgramSettingsActivity(CLEAN)));
                addButton(context, t("Settings packs", "Пакеты настроек"), true, v -> presentFragment(new FuckgramSettingsActivity(PACKS)));
                addButton(context, t("About", "О fuckgram"), false, v -> presentFragment(new FuckgramAboutActivity()));
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode == 9720) {
            final String id = pendingAvatarPreset;
            final Context avatarContext = getParentActivity();
            if (resultCode == android.app.Activity.RESULT_OK && data != null && data.getData() != null && id != null && avatarContext != null) {
                final android.net.Uri uri = data.getData();
                org.telegram.messenger.Utilities.globalQueue.postRunnable(() -> {
                    boolean ok = false;
                    try {
                        java.io.File temp = new java.io.File(avatarContext.getCacheDir(), "fg_avatar_tmp");
                        try (java.io.InputStream in = avatarContext.getContentResolver().openInputStream(uri); java.io.FileOutputStream os = new java.io.FileOutputStream(temp)) {
                            byte[] buf = new byte[64 * 1024];
                            int n;
                            while (in != null && (n = in.read(buf)) > 0) {
                                os.write(buf, 0, n);
                            }
                        }
                        ok = FuckgramProfiles.setAvatar(id, temp);
                        temp.delete();
                    } catch (Throwable ignore) {
                    }
                    final boolean done = ok;
                    org.telegram.messenger.AndroidUtilities.runOnUIThread(() -> {
                        Toast.makeText(avatarContext, done ? t("Avatar saved", "Аватар сохранён") : t("Could not read the picture", "Не удалось прочитать картинку"), Toast.LENGTH_SHORT).show();
                        if (fragmentView != null) {
                            build(avatarContext);
                        }
                    });
                });
            }
            return;
        }
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

    private static java.util.ArrayList<Long> broadcastIds = new java.util.ArrayList<>();
    private static String broadcastText = "";
    private static String pendingAvatarPreset;

    private void promptText(Context context, String title, String hint, String initial, boolean multiline, org.telegram.messenger.Utilities.Callback<String> done) {
        final android.widget.EditText input = new android.widget.EditText(context);
        input.setHint(hint);
        input.setText(initial);
        input.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        if (multiline) {
            input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            input.setMinLines(3);
        } else {
            input.setSingleLine(true);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(input);
        builder.setPositiveButton("OK", (d, w) -> done.run(input.getText().toString().trim()));
        builder.setNegativeButton(t("Cancel", "Отмена"), null);
        showDialog(builder.create());
    }

    private void pickRecipients(Context context) {
        final java.util.ArrayList<org.telegram.tgnet.TLRPC.Dialog> all = getMessagesController().getAllDialogs();
        final java.util.ArrayList<Long> ids = new java.util.ArrayList<>();
        final java.util.ArrayList<String> names = new java.util.ArrayList<>();
        for (int i = 0; i < all.size() && ids.size() < 300; i++) {
            org.telegram.tgnet.TLRPC.Dialog dialog = all.get(i);
            if (dialog == null || dialog.id <= 0 || dialog.id == getUserConfig().getClientUserId()) {
                continue;
            }
            org.telegram.tgnet.TLRPC.User user = getMessagesController().getUser(dialog.id);
            if (user == null || user.bot || user.deleted) {
                continue;
            }
            ids.add(dialog.id);
            names.add(org.telegram.messenger.UserObject.getUserName(user));
        }
        final LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        final java.util.ArrayList<TextCheckCell> cells = new java.util.ArrayList<>();
        TextCheckCell allCell = new TextCheckCell(context);
        allCell.setTextAndCheck(t("All private chats (up to 100)", "Все личные чаты (до 100)"), false, true);
        allCell.setOnClickListener(v -> {
            boolean value = !allCell.isChecked();
            allCell.setChecked(value);
            broadcastIds.clear();
            for (int i = 0; i < cells.size(); i++) {
                boolean on = value && broadcastIds.size() < 100;
                cells.get(i).setChecked(on);
                if (on) {
                    broadcastIds.add(ids.get(i));
                }
            }
        });
        box.addView(allCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        for (int i = 0; i < ids.size(); i++) {
            final long id = ids.get(i);
            final TextCheckCell cell = new TextCheckCell(context);
            cell.setTextAndCheck(names.get(i), broadcastIds.contains(id), true);
            cell.setOnClickListener(v -> {
                boolean value = !cell.isChecked();
                if (value && broadcastIds.size() >= 100) {
                    Toast.makeText(context, t("No more than 100", "Не больше 100"), Toast.LENGTH_SHORT).show();
                    return;
                }
                cell.setChecked(value);
                if (value) {
                    broadcastIds.add(id);
                } else {
                    broadcastIds.remove(Long.valueOf(id));
                }
            });
            cells.add(cell);
            box.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }
        ScrollView scroll = new ScrollView(context);
        scroll.addView(box);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(t("Recipients", "Получатели"));
        builder.setView(scroll);
        builder.setPositiveButton("OK", (d, w) -> build(context));
        builder.setOnDismissListener(d -> build(context));
        showDialog(builder.create());
    }

    private void startBroadcast(Context context) {
        if (broadcastIds.isEmpty() || broadcastText.isEmpty()) {
            Toast.makeText(context, t("Choose recipients and write a text", "Выберите получателей и напишите текст"), Toast.LENGTH_SHORT).show();
            return;
        }
        final java.util.ArrayList<Long> queue = new java.util.ArrayList<>(broadcastIds);
        final String text = broadcastText;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(t("Send to " + queue.size() + " chats?", "Отправить в " + queue.size() + " чатов?"));
        builder.setMessage(text);
        builder.setPositiveButton(t("Send", "Отправить"), (d, w) -> {
            final boolean[] stop = {false};
            final AlertDialog.Builder progress = new AlertDialog.Builder(context);
            progress.setTitle(t("Broadcast", "Рассылка"));
            progress.setMessage("0 / " + queue.size());
            progress.setNegativeButton(t("Stop", "Стоп"), (dd, ww) -> stop[0] = true);
            final AlertDialog dialog = progress.create();
            showDialog(dialog);
            final java.util.Random random = new java.util.Random();
            final int[] index = {0};
            final Runnable[] step = new Runnable[1];
            step[0] = () -> {
                if (stop[0] || index[0] >= queue.size()) {
                    try {
                        dialog.dismiss();
                    } catch (Throwable ignore) {
                    }
                    Toast.makeText(context, t("Sent: ", "Отправлено: ") + index[0], Toast.LENGTH_LONG).show();
                    return;
                }
                long dialogId = queue.get(index[0]);
                org.telegram.tgnet.TLRPC.User user = getMessagesController().getUser(dialogId);
                String first = user == null || user.first_name == null ? "" : user.first_name;
                String message = text.replace("{имя}", first).replace("{name}", first);
                org.telegram.messenger.SendMessagesHelper.getInstance(currentAccount).sendMessage(org.telegram.messenger.SendMessagesHelper.SendMessageParams.of(message, dialogId, null, null, null, true, null, null, null, true, 0, 0, null, false));
                index[0]++;
                try {
                    dialog.setMessage(index[0] + " / " + queue.size());
                } catch (Throwable ignore) {
                }
                org.telegram.messenger.AndroidUtilities.runOnUIThread(step[0], 2000 + random.nextInt(2000));
            };
            step[0].run();
        });
        builder.setNegativeButton(t("Cancel", "Отмена"), null);
        showDialog(builder.create());
    }

    private void presetActions(Context context, final FuckgramProfiles.Preset preset) {
        String[] items = {t("Apply now", "Применить сейчас"), t("Edit", "Изменить"), t("Set avatar", "Выбрать аватар"), t("Delete", "Удалить")};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(preset.title);
        builder.setItems(items, (d, which) -> {
            if (which == 0) {
                FuckgramProfiles.apply(currentAccount, preset);
                Toast.makeText(context, t("Applied", "Применено"), Toast.LENGTH_SHORT).show();
            } else if (which == 1) {
                editPreset(context, preset);
            } else if (which == 2) {
                pendingAvatarPreset = preset.id;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, t("Choose a picture", "Выберите картинку")), 9720);
            } else {
                FuckgramProfiles.delete(preset.id);
                build(context);
            }
        });
        showDialog(builder.create());
    }

    private void editPreset(Context context, final FuckgramProfiles.Preset existing) {
        final FuckgramProfiles.Preset preset = existing != null ? existing : new FuckgramProfiles.Preset();
        org.telegram.tgnet.TLRPC.User me = getUserConfig().getCurrentUser();
        if (existing == null && me != null) {
            preset.first = me.first_name == null ? "" : me.first_name;
            String last = me.last_name == null ? "" : me.last_name;
            preset.last = last.replace(org.telegram.messenger.FuckgramMarker.MARK, "");
            org.telegram.tgnet.TLRPC.UserFull full = getMessagesController().getUserFull(me.id);
            preset.about = full != null && full.about != null ? full.about : "";
        }
        final android.widget.EditText[] fields = new android.widget.EditText[4];
        String[] hints = {t("Preset name", "Название конфига"), t("First name", "Имя"), t("Last name", "Фамилия"), t("Description", "Описание")};
        String[] values = {preset.title == null ? "" : preset.title, preset.first == null ? "" : preset.first, preset.last == null ? "" : preset.last, preset.about == null ? "" : preset.about};
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < 4; i++) {
            fields[i] = new android.widget.EditText(context);
            fields[i].setHint(hints[i]);
            fields[i].setText(values[i]);
            fields[i].setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            box.addView(fields[i], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }
        ScrollView scroll = new ScrollView(context);
        scroll.addView(box);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(existing == null ? t("New preset", "Новый конфиг") : t("Edit preset", "Изменить конфиг"));
        builder.setView(scroll);
        builder.setPositiveButton(t("Save", "Сохранить"), (d, w) -> {
            preset.title = fields[0].getText().toString().trim();
            preset.first = fields[1].getText().toString().trim();
            preset.last = fields[2].getText().toString().trim();
            preset.about = fields[3].getText().toString().trim();
            if (preset.title.isEmpty()) {
                preset.title = preset.first.isEmpty() ? t("Preset", "Конфиг") : preset.first;
            }
            FuckgramProfiles.put(preset);
            build(context);
        });
        builder.setNegativeButton(t("Cancel", "Отмена"), null);
        showDialog(builder.create());
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
            refreshAll();
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

    private void choose(Context context, String title, String[] items, int current, final java.util.function.IntConsumer action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setItems(items, (d, which) -> {
            action.accept(which);
            refreshAll();
            if (fragmentView != null) {
                build(context);
            }
        });
        builder.setNegativeButton(t("Cancel", "Отмена"), null);
        showDialog(builder.create());
    }

    private void addSwitchLite(Context context) {
        TextCheckCell cell = new TextCheckCell(context);
        cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        cell.setTextAndCheck(t("Weak phone mode (no heavy effects)", "Режим слабого телефона (без тяжёлых эффектов)"), org.telegram.messenger.FuckgramLite.isOn(), false);
        cell.setOnClickListener(v -> {
            boolean value = !cell.isChecked();
            org.telegram.messenger.FuckgramLite.apply(value);
            cell.setChecked(value);
            refreshAll();
        });
        list.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private static final Runnable refreshTask = () -> {
        LaunchActivity launch = LaunchActivity.instance;
        if (launch != null) {
            launch.rebuildAllFragments(true);
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.dialogsNeedReload, true);
    };

    /** Applies a changed setting at once: every screen is rebuilt, nothing has to be restarted. */
    public static void refreshAll() {
        AndroidUtilities.cancelRunOnUIThread(refreshTask);
        AndroidUtilities.runOnUIThread(refreshTask, 120);
    }

    /** Moves the round «write» button (and the story button above it) to the chosen side. */
    public static void placeFab(View... views) {
        int pos = FuckgramConfig.getInt(FuckgramConfig.FAB_POS);
        if (pos != 1 && pos != 2) {
            return;
        }
        for (View view : views) {
            if (view != null && view.getLayoutParams() instanceof android.widget.FrameLayout.LayoutParams) {
                android.widget.FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) view.getLayoutParams();
                lp.gravity = (lp.gravity & Gravity.VERTICAL_GRAVITY_MASK) | (pos == 1 ? Gravity.LEFT : Gravity.CENTER_HORIZONTAL);
                view.setLayoutParams(lp);
            }
        }
    }

    private void addGuide(Context context, String title, String desc, final int target) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(AndroidUtilities.dp(21), AndroidUtilities.dp(10), AndroidUtilities.dp(21), AndroidUtilities.dp(10));
        row.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        TextView name = new TextView(context);
        name.setText(title);
        name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        name.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        row.addView(name, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        TextView text = new TextView(context);
        text.setText(desc);
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        text.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        row.addView(text, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 0));
        row.setOnClickListener(v -> presentFragment(new FuckgramSettingsActivity(target)));
        list.addView(row, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    /** Only real bots can be added: the username is resolved first. */
    private void addBot(final Context context, String text) {
        final String name = FuckgramBotBlock.normalize(text);
        if (name.length() < 3) {
            Toast.makeText(context, t("Wrong username", "Неверный юзернейм"), Toast.LENGTH_SHORT).show();
            return;
        }
        org.telegram.tgnet.TLRPC.TL_contacts_resolveUsername req = new org.telegram.tgnet.TLRPC.TL_contacts_resolveUsername();
        req.username = name;
        getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            boolean isBot = false;
            if (response instanceof org.telegram.tgnet.TLRPC.TL_contacts_resolvedPeer) {
                org.telegram.tgnet.TLRPC.TL_contacts_resolvedPeer res = (org.telegram.tgnet.TLRPC.TL_contacts_resolvedPeer) response;
                getMessagesController().putUsers(res.users, false);
                for (int i = 0; i < res.users.size(); i++) {
                    org.telegram.tgnet.TLRPC.User user = res.users.get(i);
                    if (user != null && user.bot && res.peer != null && res.peer.user_id == user.id) {
                        isBot = true;
                    }
                }
            }
            if (isBot) {
                FuckgramBotBlock.add(name);
            } else {
                Toast.makeText(context, t("This is not a bot", "Это не бот"), Toast.LENGTH_SHORT).show();
            }
            if (fragmentView != null) {
                build(context);
            }
        }));
    }

    private void addSwitch(Context context, String key, String title, boolean divider) {
        TextCheckCell cell = new TextCheckCell(context);
        cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        cell.setTextAndCheck(title, FuckgramConfig.get(key), divider);
        cell.setOnClickListener(v -> {
            boolean value = !cell.isChecked();
            FuckgramConfig.set(key, value);
            cell.setChecked(value);
            refreshAll();
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
