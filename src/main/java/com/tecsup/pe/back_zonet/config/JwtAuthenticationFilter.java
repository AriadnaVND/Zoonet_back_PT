package com.tecsup.pe.back_zonet.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String jwtSecret = "Zoonet_Admin_Super_Secret_Key_2026_Secure_String_For_HS512_Algorithm_Requirement_Zoonet_Project";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. Solo procesamos rutas que requieren administración
        if (!path.startsWith("/api/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        // 2. Solo intentamos validar si el header existe y comienza con Bearer
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String email = claims.getSubject();
                String role = claims.get("role", String.class);

                System.out.println("DEBUG JWT: Email=" + email + ", Rol en Token=" + role);

                if (email != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            email, null, Collections.singletonList(new SimpleGrantedAuthority(role)));

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                // Si el token falla, limpiamos el contexto y dejamos que Spring Security decida (403 si es admin)
                SecurityContextHolder.clearContext();
            }
        }

        // Pasamos al siguiente filtro de la cadena
        filterChain.doFilter(request, response);
    }
}