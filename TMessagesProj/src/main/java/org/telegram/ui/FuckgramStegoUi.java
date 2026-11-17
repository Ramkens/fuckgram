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
    private static final int REQUEST_FILE = 9711;
    private static final long MAX_FILE = 30L * 1024 * 1024;

    private static String t(String en, String ru) {
        return FuckgramText.t(en, ru);
    }

    /** Telegram refuses to send files from the private app folder, so the work folder is in its normal cache. */
    private static File workDir() {
        File dir = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), "fg_stego");
        dir.mkdirs();
        return dir;
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

    /** Encrypts any picture or file as a whole; the file looks like noise to everybody else. */
    public static void startFile(ChatActivity chat) {
        if (chat == null || chat.getParentActivity() == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        chat.startActivityForResult(Intent.createChooser(intent, t("Choose a picture or file", "Выберите фото или файл")), REQUEST_FILE);
    }

    private static void encryptFile(ChatActivity chat, Uri uri) {
        final Context context = chat.getParentActivity();
        final long dialogId = chat.getDialogId();
        Toast.makeText(context, t("Encrypting…", "Шифрую…"), Toast.LENGTH_SHORT).show();
        Utilities.globalQueue.postRunnable(() -> {
            File out = null;
            String error = null;
            try {
                String name = "file";
                try (android.database.Cursor c = context.getContentResolver().query(uri, new String[]{android.provider.OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                    if (c != null && c.moveToFirst() && c.getString(0) != null) {
                        name = c.getString(0);
                    }
                } catch (Throwable ignore) {
                }
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                    byte[] buf = new byte[64 * 1024];
                    int n;
                    while (in != null && (n = in.read(buf)) > 0) {
                        buffer.write(buf, 0, n);
                        if (buffer.size() > MAX_FILE) {
                            error = t("The file is bigger than 30 MB", "Файл больше 30 МБ");
                            break;
                        }
                    }
                }
                if (error == null) {
                    byte[] encrypted = FuckgramCrypto.encryptBytes(buffer.toByteArray(), name, FuckgramCrypto.passphraseForSending(dialogId));
                    if (encrypted == null) {
                        error = t("Could not encrypt", "Не удалось зашифровать");
                    } else {
                        out = new File(workDir(), "IMG_" + System.currentTimeMillis() + ".fgx");
                        try (FileOutputStream os = new FileOutputStream(out)) {
                            os.write(encrypted);
                        }
                    }
                }
            } catch (Throwable e) {
                error = t("Could not encrypt", "Не удалось зашифровать");
            }
            final File outFinal = out;
            final String errorFinal = error;
            AndroidUtilities.runOnUIThread(() -> {
                if (errorFinal == null && outFinal != null) {
                    chat.fgSendHiddenImage(outFinal.getAbsolutePath());
                } else {
                    Toast.makeText(context, errorFinal, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    public static boolean canDecryptFile(MessageObject object) {
        if (object == null || object.messageOwner == null) {
            return false;
        }
        TLRPC.Document document = object.getDocument();
        if (document == null) {
            return false;
        }
        String name = org.telegram.messenger.FileLoader.getDocumentFileName(document);
        return name != null && name.toLowerCase().endsWith(".fgx");
    }

    public static void decryptFile(ChatActivity chat, MessageObject object) {
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
            File out = null;
            String name = null;
            try {
                byte[] raw = new byte[(int) file.length()];
                try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                    int off = 0;
                    while (off < raw.length) {
                        int n = in.read(raw, off, raw.length - off);
                        if (n <= 0) {
                            break;
                        }
                        off += n;
                    }
                }
                Object[] result = FuckgramCrypto.decryptBytesFor(dialogId, raw);
                if (result != null) {
                    name = (String) result[0];
                    String safe = name == null || name.isEmpty() ? "file" : name.replaceAll("[\\\\/:*?\"<>|]", "_");
                    out = new File(workDir(), "dec_" + System.currentTimeMillis() + "_" + safe);
                    try (FileOutputStream os = new FileOutputStream(out)) {
                        os.write((byte[]) result[1]);
                    }
                }
            } catch (Throwable ignore) {
            }
            final File outFinal = out;
            final String nameFinal = name;
            AndroidUtilities.runOnUIThread(() -> {
                if (outFinal == null) {
                    Toast.makeText(context, t("Could not decrypt: wrong key or not a fuckgram file", "Не расшифровалось: другой ключ или это не файл fuckgram"), Toast.LENGTH_LONG).show();
                    return;
                }
                showDecrypted(chat, outFinal, nameFinal);
            });
        });
    }

    private static void showDecrypted(ChatActivity chat, File file, String name) {
        Context context = chat.getParentActivity();
        if (context == null) {
            return;
        }
        android.graphics.Bitmap bitmap = null;
        try {
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inSampleSize = 2;
            bitmap = android.graphics.BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        } catch (Throwable ignore) {
        }
        final boolean image = bitmap != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(name == null || name.isEmpty() ? t("Decrypted", "Расшифровано") : name);
        if (image) {
            android.widget.ImageView view = new android.widget.ImageView(context);
            view.setAdjustViewBounds(true);
            view.setImageBitmap(bitmap);
            builder.setView(view);
        } else {
            builder.setMessage(t("The file is decrypted. Save it to open it.", "Файл расшифрован. Сохраните его, чтобы открыть."));
        }
        final String saveName = name == null || name.isEmpty() ? file.getName() : name;
        builder.setPositiveButton(t("Save", "Сохранить"), (d, w) -> org.telegram.messenger.MediaController.saveFile(file.getAbsolutePath(), context, image ? 0 : 2, saveName, null));
        builder.setNegativeButton(t("Close", "Закрыть"), null);
        chat.showDialog(builder.create());
    }

    public static boolean onResult(ChatActivity chat, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FILE) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null && chat.getParentActivity() != null) {
                encryptFile(chat, data.getData());
            }
            return true;
        }
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
                    File dir = workDir();
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
        if (document == null) {
            return false;
        }
        String name = FileLoader.getDocumentFileName(document);
        return "image/png".equals(document.mime_type) || name != null && name.toLowerCase().endsWith(".png");
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
