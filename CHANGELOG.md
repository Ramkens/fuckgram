# Changelog

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
