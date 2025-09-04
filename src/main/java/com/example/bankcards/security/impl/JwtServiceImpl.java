package com.example.bankcards.security.impl;

import com.example.bankcards.dto.UserAuthInfo;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.security.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
public class JwtServiceImpl implements JwtService {
    private final SecretKey secretKey;

    private final Long duration; // in minutes

    public JwtServiceImpl(Environment env) {
        String b64 = env.getRequiredProperty("security.jwt.secret");
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(b64));
        this.duration = env.getProperty("security.jwt.duration", Long.class, 60L);
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
    public UserAuthInfo extractUserAuthInfo(String token) {
        Claims claims = extractClaims(token);

        try {
            UUID id = UUID.fromString(claims.getSubject());
            User.Role role = switch (claims.get("role", String.class)) {
                case "ADMIN" -> User.Role.ADMIN;
                case "USER" -> User.Role.USER;
                default -> throw new UnauthorizedException("Invalid role");
            };

            return new UserAuthInfo(id, role);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid headers");
        }
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

