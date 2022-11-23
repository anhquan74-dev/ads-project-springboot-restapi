package edu.dac.springbootrestapibackend.security.jwt;

import edu.dac.springbootrestapibackend.security.userprincipal.UserPrincipal;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    private String jwtSecret;
    private int jwtExpirationMs;

    @Value("${jwt.jwtSecret}")
    public void setSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @Value("${jwt.jwtExpirationMs}")
    public void setJwtExpirationMs(int jwtExpirationMs) {
        this.jwtExpirationMs = jwtExpirationMs;
    }

    // create token
    public String generateJwtToken(UserPrincipal userPrincipal) {
        return generateTokenFromEmail(userPrincipal.getUsername());
    }

    public String generateTokenFromEmail(String email) {
        return Jwts.builder().setSubject(email).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    // get email from jwt token
    public String getEmailFromJwtToken(String token) {
        String email = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
        return email;
    }
    // refresh token
    // public String doGenerateRefreshToken(Map<String, Object> claims, String
    // subject) {
    // return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new
    // Date())
    // .setExpiration(new Date(new Date().getTime() + jwtRefreshExpirationDateInMs))
    // .signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
    // }

    public boolean validateToken(String token) {
        // try {
        // Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
        // return true;
        // } catch (SignatureException | MalformedJwtException | UnsupportedJwtException
        // | IllegalArgumentException ex) {
        // throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        // } catch (ExpiredJwtException ex) {
        // throw ex;
        // }

        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;

    }

}
