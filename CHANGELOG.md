# Changelog

## 1.4.0

### 🇷🇺 Русский
**Исправлено и ускорено**
- **Интерфейс больше не «залипает».** При включённом запрете на ботов список чатов пересобирался на каждый элемент и тормозил весь экран. Теперь список кэшируется, а настройки читаются из памяти.
- **Фон появляется сразу.** Свои обои (фото или первый кадр видео) подгружаются заранее и ставятся мгновенно, без задержки в секунду. Видео-фон не включается на слабых устройствах (вместо него — первый кадр).
- **Облегчено приложение:** включено сжатие кода (R8) и ресурсов, оставлены только русский, английский и украинский (остальные языки Telegram скачивает сам).
- **Удалённые сообщения:** пометка «удалено» больше не появляется у сообщений канала, чей номер совпал с номером удалённого личного сообщения.
- **Замок шифрования:** вместо эмодзи 🔒/🔐 (отображались криво) теперь слова «зашифровано» / «расшифровано» у времени; сообщение, которое не удалось расшифровать, всегда помечено «зашифровано».
- **Тёмная тема:** серый текст (бывший синий акцент: заголовки, ссылки, имена в ответах, кнопки) стал белым.

**Изменено**
- **Бан на ботов:** его можно отменить в первые 5 минут после включения (кнопка в настройках). Работает только на ботов: добавить можно лишь настоящего бота (юзернейм проверяется), людей и каналы функция не трогает.
- **«Убрать лишнее»:** вместо скрытия текста в уведомлениях — переключатель «Спонсор прокси».
- **Справочник** — все функции на одном экране, по нажатию открывается нужный раздел. Лишние тексты убраны, меню настроек упрощено, значки fuckgram перенесены в «Оформление».
- **«О fuckgram»** переделан: логотип, версия, карточка разработчика @scamchrome, ссылка на исходный код, честная пометка про Telegram и GPLv2.

**Удалено**
- Встроенный VPN (прокси) и скрытие текста в уведомлениях.

### 🇬🇧 English
**Fixed and faster**
- **The interface no longer freezes.** With the bot ban on, the chat list was rebuilt for every row and slowed the whole screen. The list is now cached and settings are read from memory.
- **Backgrounds appear instantly.** Your wallpapers (a photo or the first frame of a video) are decoded ahead of time and shown at once, without the one second delay. Video backgrounds are not started on weak devices (the first frame is shown instead).
- **Lighter app:** code and resource shrinking (R8) is on, only Russian, English and Ukrainian are bundled (Telegram downloads other languages itself).
- **Deleted messages:** the «deleted» mark no longer appears on channel messages whose number matched a deleted private message.
- **Encryption lock:** instead of the 🔒/🔐 emoji (drawn badly) the time now shows the words «encrypted» / «decrypted»; a message that could not be decrypted is always marked «encrypted».
- **Dark theme:** gray text (the former blue accent: headers, links, reply names, buttons) is now white.

**Changed**
- **Bot ban:** it can be cancelled during the first 5 minutes after it starts (a button in settings). Bots only: you can add only a real bot (the username is checked), people and channels are never touched.
- **Clean-up:** a «Proxy sponsor» switch instead of hiding the text in notifications.
- **Handbook:** every feature on one screen, a tap opens the right section. Extra texts are gone, the settings menu is simpler, fuckgram badges moved to «Appearance».
- **About** is redone: logo, version, developer card @scamchrome, source code link, an honest note about Telegram and GPLv2.

**Removed**
- The built-in VPN (proxies) and hiding the text in notifications.

## 1.3.4

### 🇷🇺 Русский
**Новое**
- **Встроенный VPN для Telegram** (Настройки → fuckgram → Встроенный VPN). fuckgram собирает бесплатные MTProto и SOCKS5 прокси из нескольких открытых списков, проверяет скорость ответа каждого, сортирует и включает самый быстрый. Одна кнопка «Найти самые быстрые и включить», показ хода проверки, отмена.
- **Свои ключи и списки:** вставляйте `tg://proxy`, `t.me/proxy`, `socks5://ip:port` и ссылки на свои списки прокси. Свои ключи проверяются всегда.
- **Автообновление** при запуске раз в 3 часа (переключатель).

**Важно знать**
- Это прокси только для трафика Telegram, а не VPN на весь телефон.
- Ключи vless / vmess / trojan / Happ не поддерживаются: им нужен отдельный движок. Такие ключи пропускаются.
- Бесплатные прокси медленные и быстро умирают, их владельцы видят сам факт использования Telegram (но не ваши сообщения).

### 🇬🇧 English
**New**
- **Built-in VPN for Telegram** (Settings → fuckgram → Built-in VPN). fuckgram collects free MTProto and SOCKS5 proxies from several public lists, checks how fast each one answers, sorts them and switches on the fastest. One button "Find the fastest and switch on", live progress, cancel.
- **Your own keys and lists:** paste `tg://proxy`, `t.me/proxy`, `socks5://ip:port` and links to your own proxy lists. Your keys are always checked.
- **Auto refresh** at start every 3 hours (a switch).

**Good to know**
- These are proxies for Telegram's traffic only, not a VPN for the whole phone.
- vless / vmess / trojan / Happ keys are not supported: they need a separate engine. Such keys are skipped.
- Free proxies are slow and die fast, and their owners see the fact that you use Telegram (not your messages).

## 1.3.3

### 🇷🇺 Русский
**Новое**
- **Самозапрет на ботов с таймером** (Настройки → fuckgram → Запрет на ботов). Добавьте юзернеймы ботов и включите запрет на 15 минут, час, 6 часов, день, 7 или 30 дней. Пока запрет идёт: чаты этих ботов скрыты и не открываются (в том числе из поиска и по ссылкам), их мини-приложения не запускаются, уведомления молчат. Досрочно снять нельзя, можно только добавить ботов или продлить срок.
- **Рассылка** (Настройки → fuckgram → Рассылка): выберите получателей из своего списка чатов (до 100), напишите текст с подстановкой `{имя}`, отправка идёт по одному сообщению раз в 2–4 секунды, есть кнопка «Стоп».
- **Конфиги профиля**: сохраните набор «имя, фамилия, описание, аватар» и применяйте к аккаунту одним нажатием. Значок fuckgram у имени сохраняется.
- **Из AyuGram:** секунды во времени сообщения и ID сообщения рядом со временем (оба в «Сообщения», выключены по умолчанию).

**Важно знать**
- Запрет работает внутри fuckgram и по часам телефона. Это барьер для импульса, а не замок.
- Если азартные игры отнимают контроль над жизнью, поговорите со специалистом: запрет в приложении только помогает.

### 🇬🇧 English
**New**
- **Self-ban on bots with a timer** (Settings → fuckgram → Bot ban). Add bot usernames and start the ban for 15 minutes, 1 hour, 6 hours, 1 day, 7 or 30 days. While it runs: the bots' chats are hidden and cannot be opened (also from search and links), their mini apps do not start, and notifications are silent. It cannot be cancelled early, only extended or made stricter with more bots.
- **Broadcast** (Settings → fuckgram → Broadcast): pick recipients from your chat list (up to 100), write a text with `{name}`, messages go out one every 2–4 seconds, with a Stop button.
- **Profile presets**: save a set of first name, last name, description and avatar and apply it to your account in one tap. The fuckgram badge next to the name is kept.
- **From AyuGram:** seconds in the message time and the message ID next to the time (both in "Messages", off by default).

**Good to know**
- The ban works inside fuckgram and follows the phone's clock. It is a barrier for an impulse, not a lock.
- If gambling takes control of your life, talk to a professional: the in-app ban only helps.

## 1.3.2

### 🇷🇺 Русский
**Новое**
- **Всегда шифровать в этом чате.** Зажмите кнопку отправки → «Всегда шифровать в этом чате»: всё, что вы пишете, уходит шифром само, без выбора в меню. У вас текст виден как обычно, у друга с fuckgram тоже, у остальных мешанина символов. Выключается там же.
- **Без текста в уведомлениях.** Переключатель в «Убрать лишнее»: уведомление пишет «новое сообщение» вместо текста (в том числе вместо шифра).

**Исправления**
- Кнопки «Расшифровать файл» и «Проявить скрытый текст» не появлялись в меню сообщения у файлов и картинок. Теперь они есть у любого сообщения.

### 🇬🇧 English
**New**
- **Always encrypt in this chat.** Long-press the send button → "Always encrypt in this chat": everything you type leaves as a cipher, no menu needed. You see plain text, so does a friend with fuckgram, everybody else sees a pile of symbols. Turn it off in the same place.
- **No text in notifications.** A switch in "Clean-up": the notification says "new message" instead of the text (including ciphers).

**Fixes**
- The "Decrypt file" and "Reveal hidden text" buttons did not show up in the menu of files and pictures. They are now there for any message.

## 1.3.1

### 🇷🇺 Русский
**Новое**
- **Шифрование любого фото или файла.** Зажмите кнопку отправки → «Зашифровать фото или файл»: любой формат (до 30 МБ) шифруется целиком и уходит файлом `.fgx`, для остальных это просто шум. У друга с fuckgram: зажать файл → «Расшифровать файл», откроется превью с кнопкой «Сохранить».
- **Два пространства.** Два быстрых нажатия на логотип fuckgram в списке чатов переключают основное и второе пространство без пароля и без блокировки (код-пароль для этого не нужен). Если на входе висит код-пароль, пароль-призрак открывает второе пространство, настоящий — основное.

**Изменено**
- Убраны готовые цветовые палитры и тема Telegram. Остались свои цвета (ввод #RRGGBB) и отдельный переключатель шрифта Telegram (Roboto).

**Исправления**
- «Вложение не поддерживается» при отправке картинки со скрытым текстом: файл теперь готовится в разрешённой папке.

### 🇬🇧 English
**New**
- **Encrypt any picture or file.** Long-press the send button → "Encrypt a picture or file": any format (up to 30 MB) is encrypted as a whole and sent as a `.fgx` file, which looks like noise to everybody else. A friend with fuckgram long-presses the file → "Decrypt file" and gets a preview with a "Save" button.
- **Two spaces.** Two quick taps on the fuckgram logo in the chat list switch between the main and the second space, without a password and without locking (no passcode needed). If a passcode is shown at start, the ghost password opens the second space and the real one opens the main space.

**Changed**
- The ready color palettes and the Telegram theme are removed. Your own colors (#RRGGBB input) stay, and the Telegram font (Roboto) is a separate switch.

**Fixes**
- "Attachment is not supported" when sending a picture with hidden text: the file is now prepared in an allowed folder.

## 1.3.0

### 🇷🇺 Русский
**Новое**
- **Тайный текст в картинке.** Зажмите кнопку отправки → «Спрятать текст в картинке»: выбираете картинку, пишете текст, он шифруется и прячется внутри. Картинка уходит файлом (PNG), потому что Telegram пережимает обычные фото и стирает скрытое. Друг с fuckgram зажимает файл → «Проявить скрытый текст». Ключ — пароль чата или встроенный.
- **Пароль-призрак.** Второй код на экране блокировки: приложение открывается «тихо» — видны только «Избранное» и разрешённые вами чаты, уведомления от остальных молчат, закрытые чаты не открываются даже из поиска. Настоящий код возвращает всё (Настройки → fuckgram → Пароль-призрак и тревога).
- **Тревожная кнопка.** Три быстрых нажатия на логотип fuckgram в списке чатов: открытые чаты закрываются, включается тихий режим, приложение блокируется. Нужен включённый код-пароль.

**Важно знать**
- Скрытый текст в картинке виден как обычный файл-картинка, но не как «фото» в ленте: так нужно, чтобы текст не пропал.
- Тихий режим скрывает чаты в приложении, а не на сервере Telegram; на других устройствах всё как обычно.

### 🇬🇧 English
**New**
- **Hidden text in a picture.** Long-press the send button → "Hide text in a picture": pick a picture, type the text, it is encrypted and hidden inside. The picture is sent as a PNG file, because Telegram recompresses ordinary photos and erases the hidden bits. A friend with fuckgram long-presses the file → "Reveal hidden text". The key is the chat passphrase or the built-in one.
- **Ghost password.** A second code on the lock screen: the app opens quietly. Only Saved Messages and the chats you allowed are visible, notifications from the rest are silent, and hidden chats cannot be opened even from search. Your real code brings everything back (Settings → fuckgram → Ghost password & panic).
- **Panic button.** Tap the fuckgram logo in the chat list three times quickly: open chats close, the quiet mode turns on and the app locks. Needs a passcode.

**Good to know**
- The hidden text travels in a picture file, not in a "photo" message: that is needed so the text survives.
- The quiet mode hides chats inside the app only, not on Telegram's servers; other devices stay as they are.

## 1.2.2

### 🇷🇺 Русский
**Новое**
- **Убрать лишнее** (Настройки → fuckgram → Убрать лишнее): отдельные переключатели скрывают официальную рекламу Telegram, Premium, Business, Звёзды и TON, подарки, истории и кошелёк.
- **Свои цвета**: готовые палитры и ручной ввод цвета для акцента, своих и чужих сообщений, фона и текста.
- **Оригинальный Telegram**: переключатель возвращает официальные светлую и тёмную темы и шрифт Roboto (шрифт меняется после перезапуска).
- **Паки анимаций**: «Классика», «Затухание», «Масштаб», «Снизу вверх», «Широкий сдвиг», «Сверху вниз» + скорость. Работают при открытии и закрытии любых экранов.
- Иконка в уведомлениях теперь fuckgram, а не синий самолётик.

**Исправления**
- Метка «удалено» появляется сразу, без повторного входа в чат.
- Убран фон для экрана настроек (остались фоны для чатов и списка чатов).

### 🇬🇧 English
**New**
- **Clean-up** (Settings → fuckgram → Clean-up): separate switches hide official Telegram ads, Premium, Business, Stars & TON, gifts, stories and the wallet.
- **My colors**: ready palettes and manual color input for accent, outgoing and incoming messages, background and text.
- **Original Telegram**: a switch brings back the official light and dark themes and the Roboto font (the font changes after a restart).
- **Animation packs**: Classic, Fade, Zoom, Slide up, Wide slide, Drop + speed. They apply to opening and closing any screen.
- The notification icon is now fuckgram's instead of the blue plane.

**Fixes**
- The "deleted" mark shows up instantly, no need to reopen the chat.
- The settings screen background was removed (chats and chat list keep their own).

## 1.2.1
**New**
- Separate photo or video backgrounds for chats, the chat list and Settings (Settings → fuckgram → Appearance). Video is muted and pauses when the screen is closed.
- The "deleted" mark appears right away, without reopening the chat.

**Fixes**
- fg-encrypted messages are decrypted again after reopening the chat or restarting the app (the 🔐 mark no longer shows with cipher text).
- Dark theme: gray text (previews, dates, hints, file info, tabs) is much brighter and readable.

## 1.2
**Fixes**
- fuckgram settings are now in the main Settings list (first item) as well as in the profile.
- Messages deleted by the other side are kept by default (switch in Messages & encryption).
- The 🔐 mark of encrypted messages no longer disappears after leaving the chat.
- Intro / login logo now fills the circle properly; version label shows fuckgram's own version.
- Buttons on bots' messages, send button and file icons are readable again (no more white-on-white); red error colors restored.
- Light theme: no more leftover blue.

**Features**
- fuckgram users recognise each other more reliably: plain outgoing messages carry an invisible marker, the badge appears in the profile and in the chat header of other fuckgram users.

**Note**: the live background and transition options were removed from the settings until they actually work.

## 1.1
**Look**
- New black-and-white themes: pure black with white text, and a clean white one (switch in fuckgram settings). Graphite buttons with white text, no more blue accents.
- Inter font everywhere instead of the phone's system font.
- fuckgram name and logo on the splash screen, intro and chat list header (no more Telegram plane).
- Fixed the launcher icon showing the official Telegram plane.

**Features**
- fg-encryption: long-press the send button → "Send fg-encrypted". fuckgram users see normal text with a 🔐 mark, everyone else sees a pile of symbols. Works without a password (built-in key); optional chat passphrase for real protection.
- Deleted messages stay in the chat with a "deleted" mark.
- fuckgram badge is recognised between fuckgram users (shown in profiles). The switch only controls what you see.
- Settings are now grouped by categories: Privacy, Messages & encryption, Appearance, Badge, Packs.
- Settings packs can be shared in one tap (Share) or copied / imported.

**Under the hood**
- Up to 16 accounts, fixed a native bug where accounts after the 4th shared one connection.
- Crash screen with a copyable error text instead of a silent close.
- Native libraries are 16 KB page-size compatible.

**Not done yet**: live chat backgrounds and custom screen transitions (the options exist in settings but do nothing yet).

## 1.0
- First public build: ghost mode, keep deleted messages, fuckgram badge next to own name, settings pack export / import.
