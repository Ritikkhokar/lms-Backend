package com.example.lmsProject.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class SecurityAccessTest {

    @Test
    void generateToken_and_extractUsername_roundTrips() {
        String username = "alice@example.com";
        String token = JwtUtil.generateToken(username);
        assertNotNull(token);
        String extracted = JwtUtil.extractUsername(token);
        assertEquals(username, extracted);
        assertTrue(JwtUtil.validateToken(token));
    }

    @Test
    void tamperedToken_isRejected() {
        String token = JwtUtil.generateToken("bob@example.com");
        String tampered = token + "x";
        assertFalse(JwtUtil.validateToken(tampered));
        assertThrows(JwtException.class, () -> JwtUtil.extractUsername(tampered));
    }

    @Test
    void tokenSignedWithDifferentSecret_isRejected() {
        SecretKey otherKey = Keys.hmacShaKeyFor(
                "DIFFERENT-SECRET-STRING-THAN-UTIL-KEY-AT-LEAST-32-BYTES!".getBytes(StandardCharsets.UTF_8)
        );
        String username = "teacher@example.com";
        long oneMinute = 60_000L;
        String foreignToken = Jwts.builder()
                .subject(username)
                .expiration(new Date(System.currentTimeMillis() + oneMinute))
                .signWith(otherKey, SignatureAlgorithm.HS256)
                .compact();
        assertFalse(JwtUtil.validateToken(foreignToken));
        assertThrows(JwtException.class, () -> JwtUtil.extractUsername(foreignToken));
    }
}
