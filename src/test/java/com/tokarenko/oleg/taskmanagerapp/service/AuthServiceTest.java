package com.tokarenko.oleg.taskmanagerapp.service;

import com.tokarenko.oleg.taskmanagerapp.database.Database;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthServiceTest {

    @Test
    public void testLogin() {
        Database.connect();

        String username = "John Smith";
        String password = "abc123";

        assertTrue(AuthService.login(username, password));

        AuthService.logout();
        Database.disconnect();
    }

    @Test
    public void testEncodeDecode() {
        String password = "1234";

        String encodedPassword = SecurityService.encode(password);
        System.out.println(password + " -> " + encodedPassword);

        String decodedPassword = SecurityService.decode(encodedPassword);
        System.out.println(encodedPassword + " -> " + decodedPassword);

        assertEquals(password, SecurityService.decode(encodedPassword));
    }
}
