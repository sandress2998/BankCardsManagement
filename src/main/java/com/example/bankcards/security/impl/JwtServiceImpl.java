package com.example.bankcards.security.impl;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;



@Service
public class JwtServiceImpl implements JwtService {

    private final UserService userService;

    private final SecretKey secretKey;

    private final Long duration;

    public JwtServiceImpl(
        UserService userService,
        @Value("${security.jwt.secret}") String secretString,
        @Value("${security.jwt.duration}") Long duration
    ) {
        this.userService = userService;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretString));
        this.duration = duration;
    }

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + duration * 60 * 1000);

        JwtBuilder builder = Jwts.builder()
            .claim("sub", user.getLogin())
            .claim("role", user.getRole())
            .claim("iat", now.getTime() / 1000)
            .claim("exp", expiration.getTime() / 1000)
            .signWith(secretKey);

        return builder.compact();
    }

    @Override
    public User validateToken(String token) {
        Claims claims = extractClaims(token);
        String login = claims.getSubject();
        User user = userService.findByLogin(login);

        if (user == null) {
            throw new UnauthorizedException("User with login " + login + " not found");
        }

        if (claims.getExpiration().before(new Date())) {
            throw new UnauthorizedException("Token is expired");
        }

        return user;
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}

