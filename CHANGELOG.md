# Changelog

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
