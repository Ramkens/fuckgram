# fuckgram

Android mod client based on the open-source Telegram for Android (v13.2.2), GPLv2.
Creator: **@scamchrome** (Telegram).

## What's changed
- App name / package id: `fuckgram` / `org.fuckgram.messenger`
- New launcher icon (chains logo)
- Black AMOLED "Night" theme with neon-red accent, dark by default on a fresh install
- Fonts: Onest (UI bold / extra-bold), JetBrains Mono (monospace), Unbounded (display) — all with Cyrillic
- Settings -> Help -> "About fuckgram" screen with the developers list

## Build
Locally: JDK 17, Android SDK 36, build-tools 36.0.0, NDK 27.2.12479018, CMake 3.22.1, then
`./gradlew :TMessagesProj_App:assembleAfatRelease`

Or push this repo to GitHub and run the "Build fuckgram APK" workflow (Actions tab); the APK is attached as an artifact.

## Before you publish
- **Get your own api_id / api_hash** at https://my.telegram.org and put them in
  `TMessagesProj/src/main/java/org/telegram/messenger/BuildVars.java` (`APP_ID`, `APP_HASH`).
  The ones in the repo belong to the official app.
- **Push notifications** will not work: `google-services.json` is Telegram's Firebase project.
  Create your own Firebase project for `org.fuckgram.messenger` and replace the file.
- **Signing**: the repo ships Telegram's public sample keystore. Use your own for real releases.
- **GPLv2**: if you distribute the APK you must also publish the source of your changes.
- The paper-plane in the logo is Telegram's trademark; consider replacing it before wide distribution.
