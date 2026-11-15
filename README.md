<p align="center"><img src="TMessagesProj/src/main/res/drawable-nodpi/fg_logo.png" width="120" alt="fuckgram"></p>

<h1 align="center">fuckgram</h1>
<p align="center">Telegram mod client for Android — ghost mode, deleted messages, fg-encryption, black &amp; white themes.</p>

## Download
Get the latest APK from [**Releases**](../../releases/latest) and install it (allow "install unknown apps").
fuckgram has its own package id, so it lives next to the official Telegram. Your account is the same: log in with your phone number.

## Features
- **Ghost mode** — no read receipts, no "typing…", always offline
- **Deleted messages stay** in the chat with a "deleted" mark
- **fg-encryption** — long-press send → *Send fg-encrypted*. fuckgram users read normal text, others see a pile of symbols
- **Black / white themes**, Inter font, no clutter
- **Up to 16 accounts**
- **Settings packs** — share your setup with a friend in one tap
- Everything is configured in *Profile → fuckgram settings*

See [CHANGELOG.md](CHANGELOG.md) for what's new.

## Honest notes
- Unofficial client, not affiliated with Telegram. Use at your own risk.
- The built-in encryption key is public (the app is open source): it hides text from people without fuckgram. Set a chat passphrase for real protection.
- Based on Telegram for Android 13.2.2 (GPLv2). Source of every release = this repository at the release tag.

Creator: **@scamchrome** (Telegram)

## Build
`JDK 17`, Android SDK 36, NDK 27.2.12479018, then `./gradlew :TMessagesProj_App:assembleAfatRelease` (clone with `--recursive`). CI does it for you: see `.github/workflows/build-fuckgram.yml`; the api id/hash and signing key come from repository secrets, nothing secret is stored in the code.
