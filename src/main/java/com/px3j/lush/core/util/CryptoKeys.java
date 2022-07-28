package com.px3j.lush.core.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Class that holds required keys for Lush to encrypt/decrypt information as needed.  The keys are defined by the
 * following properties:
 * <pre>
 *  lush.crypto.secret-key - the secret key for encrypting/decrypting
 *  lush.crypto.access-key - the secret key for encrypting/decrypting
 * </pre>
 *
 */
@Component
@Profile("!clear-ticket")
public class CryptoKeys {
    public final SecretKey secretKey;
    public final IvParameterSpec accessKey;

    @Autowired
    public CryptoKeys(
            @Value("${lush.crypto.secret-key}") final String secretKey,
            @Value("${lush.crypto.access-key}") final String accessKey
    ) {
        byte[] decodedSecret = Base64.getDecoder().decode(secretKey);
        byte[] decodedAccess = Base64.getDecoder().decode(accessKey);

        this.secretKey = new SecretKeySpec(decodedSecret, 0, decodedSecret.length, "AES");
        this.accessKey = new IvParameterSpec(decodedAccess);
    }
}
