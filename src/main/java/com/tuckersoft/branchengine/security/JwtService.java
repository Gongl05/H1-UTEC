package com.tuckersoft.branchengine.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Firma y lectura de los JWT.
 *
 * El token lleva SOLO la identidad del usuario (su email) en el subject. El rol no
 * viaja dentro: se carga de la base de datos en cada peticion a traves del
 * UserDetailsService. Asi, cuando un administrador promueve a alguien, el token que
 * esa persona ya tenia en la mano sirve inmediatamente con los permisos nuevos.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.expiration-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    public String generar(String email) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusMillis(expiracionMs)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el email del token. Lanza JwtException si la firma no cuadra o si vencio. */
    public String emailDe(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
