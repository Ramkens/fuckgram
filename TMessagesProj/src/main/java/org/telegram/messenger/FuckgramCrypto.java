package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.tgnet.TLRPC;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * fuckgram: text encryption between fuckgram users.
 * AES-256-GCM, key = PBKDF2-HMAC-SHA256(passphrase). The ciphertext is written as a string of 256 different
 * symbols (shuffled alphabet) so that other clients just see a pile of signs. There is no fixed header: the
 * receiver tries to decrypt anything that is made only of those symbols and is long enough.
 */
public class FuckgramCrypto {

    private static final int ITERATIONS = 60000;
    private static final byte[] SALT = "fuckgram.v1.salt".getBytes(StandardCharsets.UTF_8);
    /**
     * Used when the chat has no passphrase, so encrypted messages just work between two fuckgram clients.
     * The client is open source, so this key is NOT secret: it only hides the text from people without fuckgram.
     * A per-chat passphrase gives real protection.
     */
    private static final String BUILTIN_PASSPHRASE = "fuckgram.builtin.key.v1";
    private static final int MIN_LEN = 12 + 16 + 2;

    private static char[] alphabet;
    private static final Map<Character, Integer> reverse = new HashMap<>();
    private static final Map<String, byte[]> keyCache = new HashMap<>();
    private static final Map<String, String> plainCache = Collections.synchronizedMap(new HashMap<String, String>());
    private static final Map<TLRPC.Message, Boolean> encryptedMessages = Collections.synchronizedMap(new WeakHashMap<TLRPC.Message, Boolean>());
    private static final SecureRandom random = new SecureRandom();

    private static synchronized void ensureAlphabet() {
        if (alphabet != null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int c = 0xA1; c <= 0xBF; c++) {
            if (c != 0xA9 && c != 0xAD && c != 0xAE) {
                sb.append((char) c);
            }
        }
        sb.append((char) 0xD7).append((char) 0xF7);
        for (int c = 0x391; c <= 0x3A9; c++) {
            if (c != 0x3A2) {
                sb.append((char) c);
            }
        }
        for (int c = 0x3B1; c <= 0x3C9; c++) {
            sb.append((char) c);
        }
        for (int c = 0x20A0; c <= 0x20BF; c++) {
            sb.append((char) c);
        }
        for (int c = 0x2190; c <= 0x2193; c++) {
            sb.append((char) c);
        }
        for (int c = 0x2200; sb.length() < 256; c++) {
            sb.append((char) c);
        }
        char[] chars = sb.toString().toCharArray();
        Random rnd = new Random(0xF0CC6A3L); // java.util.Random is specified, the same order on every device
        for (int i = chars.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            char t = chars[i];
            chars[i] = chars[j];
            chars[j] = t;
        }
        for (int i = 0; i < 256; i++) {
            reverse.put(chars[i], i);
        }
        alphabet = chars;
    }

    // ---- keys -------------------------------------------------------------------------------------------------

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences("fuckgram_keys", Context.MODE_PRIVATE);
    }

    public static String getPassphrase(long dialogId) {
        try {
            return prefs().getString("chat_" + dialogId, null);
        } catch (Throwable e) {
            return null;
        }
    }

    public static void setPassphrase(long dialogId, String passphrase) {
        try {
            SharedPreferences.Editor editor = prefs().edit();
            if (passphrase == null || passphrase.isEmpty()) {
                editor.remove("chat_" + dialogId);
            } else {
                editor.putString("chat_" + dialogId, passphrase);
            }
            editor.apply();
        } catch (Throwable ignore) {
        }
        plainCache.clear();
    }

    public static String getDefaultPassphrase() {
        try {
            return prefs().getString("default", null);
        } catch (Throwable e) {
            return null;
        }
    }

    public static void setDefaultPassphrase(String passphrase) {
        try {
            SharedPreferences.Editor editor = prefs().edit();
            if (passphrase == null || passphrase.isEmpty()) {
                editor.remove("default");
            } else {
                editor.putString("default", passphrase);
            }
            editor.apply();
        } catch (Throwable ignore) {
        }
        plainCache.clear();
    }

    private static byte[] deriveKey(String passphrase) throws Exception {
        synchronized (keyCache) {
            byte[] cached = keyCache.get(passphrase);
            if (cached != null) {
                return cached;
            }
        }
        // PBKDF2-HMAC-SHA256, one 32-byte block
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(passphrase.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        mac.update(SALT);
        mac.update(new byte[]{0, 0, 0, 1});
        byte[] u = mac.doFinal();
        byte[] result = u.clone();
        for (int i = 1; i < ITERATIONS; i++) {
            u = mac.doFinal(u);
            for (int j = 0; j < result.length; j++) {
                result[j] ^= u[j];
            }
        }
        synchronized (keyCache) {
            keyCache.put(passphrase, result);
        }
        return result;
    }

    // ---- encode / decode --------------------------------------------------------------------------------------

    /** Passphrase to encrypt with in this chat: the chat one, else the default one, else the built-in key. */
    public static String passphraseForSending(long dialogId) {
        String pass = getPassphrase(dialogId);
        if (pass == null) {
            pass = getDefaultPassphrase();
        }
        return pass != null ? pass : BUILTIN_PASSPHRASE;
    }

    public static String encrypt(String text, String passphrase) {
        try {
            ensureAlphabet();
            byte[] plain = text.getBytes(StandardCharsets.UTF_8);
            byte[] data = new byte[plain.length + 1];
            data[0] = 1;
            System.arraycopy(plain, 0, data, 1, plain.length);
            byte[] iv = new byte[12];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(deriveKey(passphrase), "AES"), new GCMParameterSpec(128, iv));
            byte[] ct = cipher.doFinal(data);
            StringBuilder sb = new StringBuilder(iv.length + ct.length);
            for (byte b : iv) {
                sb.append(alphabet[b & 0xff]);
            }
            for (byte b : ct) {
                sb.append(alphabet[b & 0xff]);
            }
            return sb.toString();
        } catch (Throwable e) {
            FileLog.e(e);
            return null;
        }
    }

    public static boolean looksEncrypted(CharSequence s) {
        if (s == null || s.length() < MIN_LEN || s.length() > 8000) {
            return false;
        }
        ensureAlphabet();
        for (int i = 0, n = s.length(); i < n; i++) {
            if (!reverse.containsKey(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static String decrypt(String s, String passphrase) {
        try {
            ensureAlphabet();
            byte[] raw = new byte[s.length()];
            for (int i = 0; i < raw.length; i++) {
                raw[i] = (byte) (int) reverse.get(s.charAt(i));
            }
            byte[] iv = new byte[12];
            System.arraycopy(raw, 0, iv, 0, 12);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(deriveKey(passphrase), "AES"), new GCMParameterSpec(128, iv));
            byte[] data = cipher.doFinal(raw, 12, raw.length - 12);
            if (data.length < 1 || data[0] != 1) {
                return null;
            }
            return new String(data, 1, data.length - 1, StandardCharsets.UTF_8);
        } catch (Throwable e) {
            return null; // wrong key or not ours
        }
    }

    /** Tries the chat passphrase, then the default one. */
    public static String decryptFor(long dialogId, String s) {
        String cacheKey = dialogId + ":" + s;
        String cached = plainCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        String pass = getPassphrase(dialogId);
        String result = pass != null ? decrypt(s, pass) : null;
        if (result == null) {
            String def = getDefaultPassphrase();
            if (def != null && !def.equals(pass)) {
                result = decrypt(s, def);
            }
        }
        if (result == null) {
            result = decrypt(s, BUILTIN_PASSPHRASE);
        }
        if (result != null) {
            plainCache.put(cacheKey, result);
        }
        return result;
    }

    // ---- hook for MessageObject -------------------------------------------------------------------------------

    /**
     * Called when a MessageObject is created. If the text is a fuckgram cipher it is replaced by the plain text (when
     * we have the key) and the message is remembered as encrypted so the cell can draw the lock.
     */
    public static void onMessage(MessageObject object, TLRPC.Message message, long dialogId) {
        if (message == null || message.message == null) {
            return;
        }
        FuckgramMarker.onIncoming(message, dialogId, false);
        // the mark must survive restarts, the cipher text itself stays in the message and is decrypted every time
        if (message.id > 0 && FuckgramDeleted.isEncrypted(object.currentAccount, dialogId, message.id)) {
            object.fgEncrypted = true;
        }
        if (encryptedMessages.containsKey(message)) {
            object.fgEncrypted = true;
        }
        if (!FuckgramConfig.isEncryptionEnabled() || !looksEncrypted(message.message)) {
            return;
        }
        object.fgEncrypted = true;
        encryptedMessages.put(message, Boolean.TRUE);
        String plain = decryptFor(dialogId, message.message);
        if (plain != null) {
            message.message = plain;
            FuckgramMarker.onIncoming(message, dialogId, true);
        } else {
            object.fgLocked = true;
        }
    }
}
