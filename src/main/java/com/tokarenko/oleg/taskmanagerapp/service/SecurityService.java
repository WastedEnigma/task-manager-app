package com.tokarenko.oleg.taskmanagerapp.service;

import org.apache.commons.codec.binary.Base64;

public final class SecurityService {

    private SecurityService() { }

    public static String encode(String text) {
        byte[] encoded = Base64.encodeBase64(text.getBytes());
        return new String(encoded);
    }

    public static String decode(String encodedText) {
        byte[] decoded = Base64.decodeBase64(encodedText.getBytes());
        return new String(decoded);
    }
}
