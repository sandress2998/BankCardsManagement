package com.example.bankcards.security;

import com.example.bankcards.dto.UserAuthInfo;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
        @NotNull HttpServletRequest request,
        @NotNull HttpServletResponse response,
        @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // Если endpoint без необходимости авторизации - пропускаем запрос
            String uri = request.getRequestURI();
            if (uri.startsWith("/public/") || uri.startsWith("/api/auth/")
                || uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) {
                filterChain.doFilter(request, response);
                return;
            }

            String header = request.getHeader("Authorization");
            if (header == null) {
                throw new UnauthorizedException("Jwt missed");
            }

            if (header.startsWith("Bearer ")) {
                String token = header.substring(7);

                // Что у нас есть: мы в базе данных ищем пользователя. Если не находим - ошибка
                // Что должно быть: если Claims извлекаются нормально, то все хорошо. Если нет - кидается ошибка
                UserAuthInfo user = jwtService.extractUserAuthInfo(token);

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    JwtAuthenticationToken authentication = new JwtAuthenticationToken(user, token);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

                filterChain.doFilter(request, response);
            } else {
                throw new UnauthorizedException("Invalid token");
            }
        } catch (UnauthorizedException e) {
            authenticationEntryPoint.commence(request, response, e);
        }
    }

    private static class JwtAuthenticationToken extends AbstractAuthenticationToken {
        private final String id;
        private final String jwt;

        public JwtAuthenticationToken(UserAuthInfo user, String jwt) {
            super(mapRoleToAuthorities(user.role()));
            this.id = user.id().toString();
            this.jwt = jwt;
            setAuthenticated(true); // Указывает, что токен валиден и пользователь уже аутентифицирован
        }

        @Override
        public Object getCredentials() {
            return jwt;
        }

        @Override
        public Object getPrincipal() {
            return id;
        }

        public static Collection<? extends GrantedAuthority> mapRoleToAuthorities(User.Role role) {
            return switch (role) {
                case ADMIN -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
                case USER -> List.of(new SimpleGrantedAuthority("ROLE_USER"));
            };
        }
    }
}
