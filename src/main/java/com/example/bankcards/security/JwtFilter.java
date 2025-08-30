package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
        @NotNull HttpServletRequest request,
        @NotNull HttpServletResponse response,
        @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Если endpoint без необходимости авторизации - пропускаем запрос
        String uri = request.getRequestURI();
        if (uri.startsWith("/public/") || uri.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            User user = jwtService.validateToken(token);

            if (user == null) {
                throw new UnauthorizedException("Invalid token");
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(user, token);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } else {
            throw new UnauthorizedException("Invalid token");
        }
    }

    private static class JwtAuthenticationToken extends AbstractAuthenticationToken {
        private final String login;
        private final String jwt;

        public JwtAuthenticationToken(User user, String jwt) {
            super(mapRoleToAuthorities(user.getRole()));
            this.login = user.getLogin();
            this.jwt = jwt;
            setAuthenticated(true); // Указывает, что токен валиден и пользователь уже аутентифицирован
        }

        @Override
        public Object getCredentials() {
            return jwt;
        }

        @Override
        public Object getPrincipal() {
            return login;
        }

        public static Collection<? extends GrantedAuthority> mapRoleToAuthorities(User.Role role) {
            return switch (role) {
                case ADMIN -> List.of(new SimpleGrantedAuthority("ADMIN"));
                case USER -> List.of(new SimpleGrantedAuthority("USER"));
            };
        }
    }
}
