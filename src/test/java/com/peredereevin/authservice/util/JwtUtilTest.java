package com.peredereevin.authservice.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "c3ByaW5nLWJvb3Qtand0LXNlY3JldC1rZXktZm9yLWhzMjU2LWFuZC1sb25n";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        UserDetails userDetails = new User("test@example.com", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(userDetails);
        assertNotNull(token);
    }

    @Test
    void extractEmail_ShouldReturnCorrectEmail() {
        UserDetails userDetails = new User("test@example.com", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(userDetails);
        String email = jwtUtil.extractEmail(token);
        assertEquals("test@example.com", email);
    }

    @Test
    void validateToken_ShouldReturnTrue_ForValidToken() {
        UserDetails userDetails = new User("test@example.com", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(userDetails);
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }
}