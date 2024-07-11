package sprintone.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    private static final long VALIDITY = TimeUnit.MINUTES.toMillis(30);
    private static final long VALIDITY_refresh = TimeUnit.MINUTES.toMillis(60);

    String secret = "BF87C4A6D0A4E51A237B7CE7ECA587FFAD456CDBACB950C73DCA8DE05D575078A63C1B3DDB4FCB41FC43256AC9D7A30D2F671730D668289798C6B931154B07F3";
    public String generateToken(UserDetails userDetails , long validity) {
        Map<String, String> claims = new HashMap<>();
       // claims.put("name", "https://secure.genuinecoder.com");
        return Jwts.builder()
                //.claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(validity)))
                .signWith(generateKey())
                .compact();
    }

    public Map<String, String> generateTokens(UserDetails userDetails) {
        String accessToken = generateToken(userDetails, VALIDITY);
        String refreshToken = generateToken(userDetails, VALIDITY_refresh);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    private SecretKey generateKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    private Claims getClaims(String jwt) {
        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }
    public String extractUsername(String jwt) {
        Claims claims = getClaims(jwt);
        return claims.getSubject();
    }
    public boolean isTokenValid(String jwt) {
        Claims claims = getClaims(jwt);
        return claims.getExpiration().after(Date.from(Instant.now()));
    }

}
