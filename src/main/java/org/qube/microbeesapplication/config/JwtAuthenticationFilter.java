package org.qube.microbeesapplication.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Generated;
import org.qube.microbeesapplication.models.jpa.UserInfoJpa;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenUtils tokenUtils;

    private final MultiTenantMongoTemplate mongoTemplate;

    public JwtAuthenticationFilter(TokenUtils tokenUtils, MultiTenantMongoTemplate mongoTemplate) {
        this.tokenUtils = tokenUtils;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = tokenUtils.validateToken(token);
                String tenantId = claims.get("tenantId", String.class);
                String userId = claims.getSubject();

                UserInfoJpa userJpa = this.mongoTemplate.getMongoTemplate(tenantId).findById(userId, UserInfoJpa.class);

                if(userJpa != null) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
