package org.telegram.messenger;

/** fuckgram: small text marks that are drawn in front of the message time. */
public class FuckgramMarks {

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
            sb.append(object.fgLocked ? "🔒" : "🔐");
        }
        return sb;
    }
}
