package ch.goodone.angularai.backend.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "mySecretKeyWithAtLeast32CharactersLongForTesting";
    private final long expiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", expiration);
    }

    @Test
    void generateToken_shouldGenerateValidToken() {
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertEquals("testuser", jwtService.extractUsername(token));
    }

    @Test
    void isTokenValid_shouldReturnTrue_forValidToken() {
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_shouldReturnFalse_forWrongUser() {
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        UserDetails otherUser = new User("otheruser", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void isTokenExpired_shouldThrowException_forExpiredToken() {
        // We need to set a very short expiration or mock time, but JwtService uses System.currentTimeMillis()
        // Let's set expiration to a negative value for this test
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
        
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        // JwtService.isTokenValid checks both username and expiration
        // it calls isTokenExpired which calls extractExpiration which calls extractAllClaims
        // extractAllClaims will throw ExpiredJwtException if token is expired during parsing
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void extractClaim_shouldWorkForCustomClaims() {
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        String token = jwtService.generateToken(Collections.singletonMap("role", "ADMIN"), userDetails);

        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        assertEquals("ADMIN", role);
    }
}
