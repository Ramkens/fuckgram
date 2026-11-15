package org.telegram.ui;

import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FuckgramCrypto;
import org.telegram.messenger.FuckgramText;
import org.telegram.ui.ActionBar.AlertDialog;

/** fuckgram: passphrase dialog for the encryption of one chat. */
public class FuckgramEncryptionUi {

    public static void showPassphraseDialog(Context context, long dialogId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(FuckgramText.t("Chat passphrase", "Пароль шифрования чата"));
        builder.setMessage(FuckgramText.t(
                "Both of you must enter the same passphrase. Empty = the built-in key (works automatically, but it is not secret).",
                "Вы оба должны ввести один и тот же пароль. Пусто = встроенный ключ (работает сам, но не секретный)."));
        FrameLayout frame = new FrameLayout(context);
        EditText edit = new EditText(context);
        edit.setSingleLine(true);
        edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        String current = FuckgramCrypto.getPassphrase(dialogId);
        if (current != null) {
            edit.setText(current);
        }
        edit.setHint(FuckgramText.t("Passphrase", "Пароль"));
        frame.addView(edit, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        frame.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(4), AndroidUtilities.dp(22), 0);
        builder.setView(frame);
        builder.setPositiveButton(FuckgramText.t("Save", "Сохранить"), (d, w) -> {
            FuckgramCrypto.setPassphrase(dialogId, edit.getText().toString().trim());
            Toast.makeText(context, FuckgramText.t("Saved", "Сохранено"), Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(FuckgramText.t("Cancel", "Отмена"), null);
        builder.show();
    }

    public static void showDefaultPassphraseDialog(Context context, Runnable done) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(FuckgramText.t("Default passphrase", "Общий пароль"));
        builder.setMessage(FuckgramText.t("Used in chats without their own passphrase. Empty = built-in key.",
                "Используется в чатах без своего пароля. Пусто = встроенный ключ."));
        FrameLayout frame = new FrameLayout(context);
        EditText edit = new EditText(context);
        edit.setSingleLine(true);
        edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        String current = FuckgramCrypto.getDefaultPassphrase();
        if (current != null) {
            edit.setText(current);
        }
        frame.addView(edit, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        frame.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(4), AndroidUtilities.dp(22), 0);
        builder.setView(frame);
        builder.setPositiveButton(FuckgramText.t("Save", "Сохранить"), (d, w) -> {
            FuckgramCrypto.setDefaultPassphrase(edit.getText().toString().trim());
            if (done != null) {
                done.run();
            }
        });
        builder.setNegativeButton(FuckgramText.t("Cancel", "Отмена"), null);
        builder.show();
    }
}
