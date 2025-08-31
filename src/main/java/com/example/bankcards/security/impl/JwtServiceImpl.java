package com.example.bankcards.security.impl;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
public class JwtServiceImpl implements JwtService {

    private final UserRepository userRepository;

    private final SecretKey secretKey;

    private final Long duration;

    public JwtServiceImpl(
        UserRepository userRepository,
        @Value("${security.jwt.secret}") String secretString,
        @Value("${security.jwt.duration}") Long duration
    ) {
        this.userRepository = userRepository;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretString));
        this.duration = duration;
    }

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + duration * 60 * 1000);

        JwtBuilder builder = Jwts.builder()
            .claim("sub", user.getId().toString())
            .claim("role", user.getRole())
            .claim("iat", now.getTime() / 1000)
            .claim("exp", expiration.getTime() / 1000)
            .signWith(secretKey);

        return builder.compact();
    }

    @Override
    public User validateToken(String token) {
        Claims claims = extractClaims(token);

        String id = claims.getSubject();
        User user = userRepository.findUserById(UUID.fromString(id));

        if (user == null) {
            throw new UnauthorizedException("User not found");
        }

        return user;
    }

    @Override
    public String changeRoleInJwt(String token, User.Role role) {
        Claims originalClaims = extractClaims(token);
        Map<String, Object> claimMap = new HashMap<>(originalClaims);
        claimMap.put("role", role.toString());

        return Jwts.builder()
                .claims(claimMap)
                .signWith(secretKey)
                .compact();
    }

    private Claims extractClaims(String token) {
        Claims claims;

        try {
            claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new UnauthorizedException("Token is expired");
        } catch (UnsupportedJwtException ex) {
            throw new UnauthorizedException("Token is not supported");
        } catch (MalformedJwtException ex) {
            throw new UnauthorizedException("Token is malformed");
        } catch (SignatureException ex) {
            throw new UnauthorizedException("Token signature is invalid");
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Token invalid");
        } catch (io.jsonwebtoken.io.DecodingException ex) {
            throw new UnauthorizedException("Token decoding error");
        }

        return claims;
    }
}

