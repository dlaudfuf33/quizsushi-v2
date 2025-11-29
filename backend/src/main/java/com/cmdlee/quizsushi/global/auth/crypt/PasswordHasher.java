package com.cmdlee.quizsushi.global.auth.crypt;

public interface PasswordHasher {
    String hash(String raw);
    boolean matches(String raw, String hashed);
}
