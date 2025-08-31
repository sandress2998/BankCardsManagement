package com.example.bankcards;

import com.example.bankcards.util.BCryptEncoder;
import org.junit.jupiter.api.Test;

public class PasswordForAdminGenerator {
    @Test
    public void passwordForAdminGenerator() {
        BCryptEncoder encoder = new BCryptEncoder();

        System.out.println(encoder.encodePassword("d337d192-4421-46fc-b81a-cb3152f3f328"));
    }
}
