package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FuckgramCrypto;
import org.telegram.messenger.FuckgramStego;
import org.telegram.messenger.FuckgramText;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/** fuckgram: hide a text inside a picture / reveal it. */
public class FuckgramStegoUi {

    private static final int REQUEST = 9710;

    private static String t(String en, String ru) {
        return FuckgramText.t(en, ru);
    }

    public static void start(ChatActivity chat) {
        if (chat == null || chat.getParentActivity() == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        chat.startActivityForResult(Intent.createChooser(intent, t("Choose a picture", "Выберите картинку")), REQUEST);
    }

    public static boolean onResult(ChatActivity chat, int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST) {
            return false;
        }
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null && chat.getParentActivity() != null) {
            askText(chat, data.getData());
        }
        return true;
    }

    private static void askText(ChatActivity chat, Uri uri) {
        Context context = chat.getParentActivity();
        final EditText input = new EditText(context);
        input.setHint(t("Hidden text", "Скрытый текст"));
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        input.setMinLines(3);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(t("Text inside the picture", "Текст внутри картинки"));
        builder.setView(input);
        builder.setPositiveButton(t("Send", "Отправить"), (d, w) -> {
            final String text = input.getText().toString().trim();
            if (text.isEmpty()) {
                return;
            }
            final long dialogId = chat.getDialogId();
            Toast.makeText(context, t("Preparing…", "Готовлю…"), Toast.LENGTH_SHORT).show();
            Utilities.globalQueue.postRunnable(() -> {
                File source = null;
                File out = null;
                boolean ok = false;
                try {
                    File dir = new File(ApplicationLoader.applicationContext.getCacheDir(), "fg_stego");
                    dir.mkdirs();
                    source = new File(dir, "src_" + System.currentTimeMillis());
                    try (InputStream in = context.getContentResolver().openInputStream(uri); FileOutputStream os = new FileOutputStream(source)) {
                        byte[] buf = new byte[64 * 1024];
                        int n;
                        while (in != null && (n = in.read(buf)) > 0) {
                            os.write(buf, 0, n);
                        }
                    }
                    String cipher = FuckgramCrypto.encrypt(text, FuckgramCrypto.passphraseForSending(dialogId));
                    out = new File(dir, "IMG_" + System.currentTimeMillis() + ".png");
                    ok = cipher != null && FuckgramStego.hide(source.getAbsolutePath(), cipher, out);
                } catch (Throwable ignore) {
                }
                if (source != null) {
                    source.delete();
                }
                final File outFinal = out;
                final boolean okFinal = ok;
                AndroidUtilities.runOnUIThread(() -> {
                    if (okFinal && outFinal != null) {
                        chat.fgSendHiddenImage(outFinal.getAbsolutePath());
                    } else {
                        Toast.makeText(context, t("The picture is too small for this text", "Картинка слишком мала для этого текста"), Toast.LENGTH_LONG).show();
                    }
                });
            });
        });
        builder.setNegativeButton(t("Cancel", "Отмена"), null);
        chat.showDialog(builder.create());
    }

    /** Only PNG files can carry the text (that is what fuckgram sends). */
    public static boolean canReveal(MessageObject object) {
        if (object == null || object.messageOwner == null) {
            return false;
        }
        TLRPC.Document document = object.getDocument();
        return document != null && "image/png".equals(document.mime_type);
    }

    public static void reveal(ChatActivity chat, MessageObject object) {
        if (chat == null || object == null || chat.getParentActivity() == null) {
            return;
        }
        final Context context = chat.getParentActivity();
        final File file = FileLoader.getInstance(object.currentAccount).getPathToMessage(object.messageOwner);
        if (file == null || !file.exists() || file.length() == 0) {
            Toast.makeText(context, t("Download the file first", "Сначала загрузите файл"), Toast.LENGTH_LONG).show();
            return;
        }
        final long dialogId = chat.getDialogId();
        Utilities.globalQueue.postRunnable(() -> {
            String cipher = FuckgramStego.reveal(file.getAbsolutePath());
            String plain = cipher == null ? null : FuckgramCrypto.decryptFor(dialogId, cipher);
            AndroidUtilities.runOnUIThread(() -> {
                if (plain == null) {
                    Toast.makeText(context, t("Nothing hidden here, or the key is different", "Здесь ничего не спрятано или другой ключ"), Toast.LENGTH_LONG).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(t("Hidden text", "Скрытый текст"));
                builder.setMessage(plain);
                builder.setPositiveButton("OK", null);
                builder.setNeutralButton(t("Copy", "Копировать"), (d, w) -> AndroidUtilities.addToClipboard(plain));
                chat.showDialog(builder.create());
            });
        });
    }
}
