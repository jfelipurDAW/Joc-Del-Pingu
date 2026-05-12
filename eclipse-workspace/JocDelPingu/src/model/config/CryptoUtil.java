package model.config;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


/**
 * Lightweight symmetric encryption helper used to keep sensitive save-data
 * (in particular, player passwords) confidential when persisted to disk.
 *
 * <p>The implementation wraps the JCE {@link Cipher} API and exposes two
 * convenience methods, {@link #encrypt(String)} and {@link #decrypt(String)},
 * that operate on UTF-8 text and return a Base64-encoded payload that is
 * safe to embed in YAML/JSON save files.</p>
 *
 * <p>Algorithm: AES-128 in its default JCE mode (ECB with PKCS#5 padding),
 * using a fixed 16-byte key. This is intentionally a simple obfuscation
 * scheme for a single-machine board game — not a hardened security boundary.</p>
 */
public class CryptoUtil {


    /////////////////////////////
    ///       FIELDS          ///
    /////////////////////////////

    // JCE transformation name. "AES" alone resolves to the provider's default
    // mode/padding (typically AES/ECB/PKCS5Padding), which is enough for our
    // purpose of obfuscating short strings such as passwords.
    private static final String ALGORITHM = "AES";

    // 16-byte key for AES-128. The key length must be exactly 16 bytes;
    // changing this literal will break decryption of any previously saved data.
    private static final byte[] KEY = "PinguGameKey1234".getBytes();


    /////////////////////////////
    ///   STATIC METHODS      ///
    /////////////////////////////

    /**
     * Encrypts a plaintext UTF-8 string and returns a Base64 representation
     * of the resulting ciphertext, suitable for storing in text-based save files.
     *
     * @param value the plaintext to encrypt (must not be {@code null})
     * @return the Base64-encoded ciphertext, or {@code null} if encryption failed
     */
    public static String encrypt(String value) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // doFinal returns raw bytes — encode them to Base64 so they can be
            // safely stored in plain-text files without escaping issues.
            byte[] encryptedBytes = cipher.doFinal(value.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            // Any failure (bad key length, missing provider, etc.) is logged
            // and surfaced as null so callers can react without crashing.
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Reverses {@link #encrypt(String)}: takes a Base64 ciphertext produced
     * by this class and returns the original UTF-8 plaintext.
     *
     * @param encryptedValue the Base64 ciphertext previously produced by {@link #encrypt(String)}
     * @return the recovered plaintext, or {@code null} if decryption failed
     */
    public static String decrypt(String encryptedValue) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Reverse the pipeline: Base64-decode the stored string first,
            // then run AES decryption on the resulting raw bytes.
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
