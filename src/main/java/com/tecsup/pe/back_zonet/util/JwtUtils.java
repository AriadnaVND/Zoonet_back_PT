package com.tecsup.pe.back_zonet.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    private final String jwtSecret = "Zoonet_Admin_Super_Secret_Key_2026_Secure_String_For_HS512_Algorithm_Requirement_Zoonet_Project";
    private final int jwtExpirationMs = 86400000; // 24 horas

    public String generateToken(String email, String role) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role) // Inyección explícita del rol en los Claims corporativos
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}