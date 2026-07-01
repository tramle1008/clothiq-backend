package commerce.sbEcommerce.security.jwt;

import commerce.sbEcommerce.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${jwt.expiration.access}")
    private long accessTokenExpirationMs;

    @Value("${jwt.expiration.refresh}")
    private long refreshTokenExpirationMs;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    //Lấy JWT từ Header Authorization trong request

    public String getJWTFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


    private String generateTokenFromUserName(String username, String tokenType, long expirationMs) {
        return Jwts.builder()
                .subject(username)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    public String generateAccessToken(UserDetails userDetails) {
        return generateTokenFromUserName(userDetails.getUsername(), ACCESS_TOKEN_TYPE, accessTokenExpirationMs);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateTokenFromUserName(userDetails.getUsername(), REFRESH_TOKEN_TYPE, refreshTokenExpirationMs);
    }

    public String generateJwtToken(UserDetails userDetails) {
        return generateAccessToken(userDetails);
    }

    private boolean validateJwtToken(String authToken, String expectedTokenType) {
        try {
            String tokenType = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authToken)
                    .getPayload()
                    .get(TOKEN_TYPE_CLAIM, String.class);
            return expectedTokenType.equals(tokenType);
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

    public boolean validateAccessToken(String authToken) {
        return validateJwtToken(authToken, ACCESS_TOKEN_TYPE);
    }

    public boolean validateRefreshToken(String authToken) {
        return validateJwtToken(authToken, REFRESH_TOKEN_TYPE);
    }

    public boolean validateJwtToken(String authToken) {
        return validateAccessToken(authToken);
    }
}
