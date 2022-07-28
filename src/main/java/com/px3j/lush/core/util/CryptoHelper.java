package com.px3j.lush.core.util;

import com.px3j.lush.core.exception.LushException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Profile("!clear-ticket")
public class CryptoHelper {
    private final String algorithm = "AES/CBC/PKCS5Padding";
    private final CryptoKeys cryptoKeys;

    @Autowired
    public CryptoHelper(CryptoKeys cryptoKeys) {
        this.cryptoKeys = cryptoKeys;
    }

    public String encrypt(String input) {
        try {
            SecretKey key = cryptoKeys.secretKey;
            IvParameterSpec iv = cryptoKeys.accessKey;

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] cipherText = cipher.doFinal(input.getBytes());
            return Base64.getEncoder().encodeToString(cipherText);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new LushException( "Failed to encrypt", e );
        }
    }

    public String decrypt(String cipherText)  {
        try {
            SecretKey key = cryptoKeys.secretKey;
            IvParameterSpec iv = cryptoKeys.accessKey;

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] plainText = cipher.doFinal(Base64.getDecoder()
                    .decode(cipherText));
            return new String(plainText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new LushException( "Failed to decrypt", e);
        }
    }

    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        return keyGenerator.generateKey();
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    // TODO: Document this
    public static void main(String[] args) throws Exception {
        SecretKey secretKey = CryptoHelper.generateKey(256);
        IvParameterSpec ivParameterSpec = CryptoHelper.generateIv();

        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        String encodedIv  = Base64.getEncoder().encodeToString(ivParameterSpec.getIV());

        System.out.println( "Lush :: Key Generator" );
        System.out.println( "Lush :: Generated keys below" );
        System.out.println();

        System.out.println("         lush.crypto.secret-key: " + encodedKey);
        System.out.println("         lush.crypto.access-key: " + encodedIv);

        System.out.println();
        System.out.println( "Lush :: be sure to use these keys in any services that use Lush to encrypt/decrypt");
    }
}