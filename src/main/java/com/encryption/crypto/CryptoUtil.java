package com.encryption.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public final class CryptoUtil {

    private CryptoUtil() {}

    // 格式： Base64( MAGIC(4) + SALT(16) + IV(12) + CIPHERTEXT_AND_TAG(N) )
    private static final byte[] MAGIC = new byte[] { 'E', 'N', 'C', '1' };

    private static final int SALT_LEN = 16;
    private static final int IV_LEN = 12;          // GCM 推荐 12 bytes
    private static final int TAG_BITS = 128;       // 16 bytes tag
    private static final int KEY_BITS = 256;       // AES-256
    private static final int PBKDF2_ITERS = 120_000;

    private static final SecureRandom RNG = new SecureRandom();

    public static String encryptToBase64(String plaintext, char[] password) throws GeneralSecurityException {
        if (plaintext == null) plaintext = "";
        if (password == null || password.length == 0) {
            throw new GeneralSecurityException("密码不能为空");
        }

        byte[] salt = new byte[SALT_LEN];
        RNG.nextBytes(salt);

        SecretKey key = deriveKey(password, salt);

        byte[] iv = new byte[IV_LEN];
        RNG.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        ByteBuffer bb = ByteBuffer.allocate(MAGIC.length + SALT_LEN + IV_LEN + ciphertext.length);
        bb.put(MAGIC);
        bb.put(salt);
        bb.put(iv);
        bb.put(ciphertext);

        return Base64.getEncoder().encodeToString(bb.array());
    }

    public static String decryptFromBase64(String base64Payload, char[] password) throws GeneralSecurityException {
        if (base64Payload == null || base64Payload.isBlank()) {
            throw new GeneralSecurityException("密文不能为空");
        }
        if (password == null || password.length == 0) {
            throw new GeneralSecurityException("密码不能为空");
        }

        byte[] all;
        try {
            all = Base64.getDecoder().decode(base64Payload.trim());
        } catch (IllegalArgumentException e) {
            throw new GeneralSecurityException("密文不是合法的 Base64", e);
        }

        int minLen = MAGIC.length + SALT_LEN + IV_LEN + 1;
        if (all.length < minLen) {
            throw new GeneralSecurityException("密文长度不合法");
        }

        ByteBuffer bb = ByteBuffer.wrap(all);

        byte[] magic = new byte[MAGIC.length];
        bb.get(magic);
        if (!Arrays.equals(magic, MAGIC)) {
            throw new GeneralSecurityException("密文格式不正确（MAGIC 不匹配）");
        }

        byte[] salt = new byte[SALT_LEN];
        bb.get(salt);

        byte[] iv = new byte[IV_LEN];
        bb.get(iv);

        byte[] ciphertext = new byte[bb.remaining()];
        bb.get(ciphertext);

        SecretKey key = deriveKey(password, salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));

        byte[] plainBytes = cipher.doFinal(ciphertext);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    private static SecretKey deriveKey(char[] password, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERS, KEY_BITS);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}
