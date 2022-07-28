package com.px3j.lush.core.ticket;

import com.google.gson.Gson;
import com.px3j.lush.core.util.CryptoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Default implementation of TicketUtil.  Encryption/decryption is done using the Crypto helper from Lush Core.
 *
 * @see CryptoHelper
 */
@Component
@Profile("!clear-ticket")
public class EncryptedTicketUtil implements TicketUtil {
    private final CryptoHelper cryptoHelper;

    private final Gson gson;

    @Autowired
    public EncryptedTicketUtil(CryptoHelper cryptoHelper) {
        this.cryptoHelper = cryptoHelper;
        this.gson = new Gson();
    }

    public String encrypt( LushTicket ticket) {
        String asJson = gson.toJson(ticket, LushTicket.class);
        return cryptoHelper.encrypt(asJson);
    }

    public LushTicket decrypt(final String encryptedJson ) {
        String decryptedJson = cryptoHelper.decrypt(encryptedJson);
        return gson.fromJson( decryptedJson, LushTicket.class);
    }
}
