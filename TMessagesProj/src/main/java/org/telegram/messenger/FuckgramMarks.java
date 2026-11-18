package org.telegram.messenger;

/** fuckgram: small text marks that are drawn in front of the message time. */
public class FuckgramMarks {

    private static final java.util.regex.Pattern TIME = java.util.regex.Pattern.compile("\\d{1,2}[:.]\\d{2}");

    /** Seconds in the time and the message id, switched on in the settings (both are also in AyuGram). */
    public static CharSequence decorateTime(CharSequence time, MessageObject object) {
        try {
            if (time == null || object == null || object.messageOwner == null) {
                return time;
            }
            boolean seconds = FuckgramConfig.get(FuckgramConfig.SHOW_SECONDS);
            boolean id = FuckgramConfig.get(FuckgramConfig.SHOW_MSG_ID) && object.getId() > 0;
            if (!seconds && !id) {
                return time;
            }
            android.text.SpannableStringBuilder builder = new android.text.SpannableStringBuilder(time);
            if (seconds) {
                java.util.regex.Matcher matcher = TIME.matcher(builder);
                if (matcher.find()) {
                    int sec = (int) (object.messageOwner.date % 60);
                    builder.insert(matcher.end(), ":" + (sec < 10 ? "0" : "") + sec);
                }
            }
            if (id) {
                builder.insert(0, "#" + object.getId() + "  ");
            }
            return builder;
        } catch (Throwable e) {
            return time;
        }
    }

    public static CharSequence timePrefix(int account, MessageObject object) {
        if (object == null || object.messageOwner == null) {
            return null;
        }
        boolean deleted = FuckgramConfig.isKeepDeleted() && object.getId() > 0 && FuckgramDeleted.isDeleted(account, object.getDialogId(), object.getId());
        boolean enc = object.fgEncrypted;
        if (enc && !object.fgLocked && object.getId() > 0) {
            FuckgramDeleted.markEncrypted(account, object.getDialogId(), object.getId());
        }
        if (!deleted && !enc) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (deleted) {
            sb.append(LocaleController.getString(R.string.FuckgramDeletedMark));
        }
        if (enc) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(object.fgLocked ? FuckgramText.t("encrypted", "зашифровано") : FuckgramText.t("decrypted", "расшифровано"));
        }
        return sb;
    }
}
