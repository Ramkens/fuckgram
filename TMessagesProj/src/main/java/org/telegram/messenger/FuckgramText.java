package org.telegram.messenger;

/** fuckgram: tiny helper for the settings texts: Russian when the app language is Russian, otherwise English. */
public class FuckgramText {
    public static String t(String en, String ru) {
        try {
            String lang = LocaleController.getInstance().getCurrentLocale().getLanguage();
            return "ru".equals(lang) || "uk".equals(lang) || "be".equals(lang) ? ru : en;
        } catch (Throwable e) {
            return en;
        }
    }
}
