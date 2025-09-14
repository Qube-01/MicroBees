package org.qube.microbeesapplication.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.qube.microbeesapplication.models.jpa.UserInfoJpa;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class TokenUtils {

    private static final String MONGO_AUTH_SECRET_KEY = "jwt*_mongo-auth*private-#key_template";

    private static final long EXPIRATION_TIME = 1800000L;

    public String getToken (UserInfoJpa userInfo, String tenantId) {
        return Jwts.builder()
                .setSubject(userInfo.getId())
                .claim("tenantId", tenantId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(Keys.hmacShaKeyFor(MONGO_AUTH_SECRET_KEY.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateToken (String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(MONGO_AUTH_SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
