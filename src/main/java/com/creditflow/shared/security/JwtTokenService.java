package com.creditflow.shared.security;

import com.creditflow.identity.domain.UserAccount;
import com.creditflow.identity.domain.UserRole;
import com.creditflow.shared.application.exception.AuthenticationFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

/**
 * Minimal JWT implementation for the laboratory environment.
 *
 * <p>The project intentionally keeps token handling inside the codebase so students can reason
 * about claims, signature validation and tenant scoping without introducing an external identity
 * platform. The implementation is deliberately small but still validates expiration and HMAC
 * signatures.</p>
 */
@Service
public class JwtTokenService {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final JwtProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public JwtTokenService(JwtProperties properties, ObjectMapper objectMapper, Clock clock) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public String generateToken(UserAccount userAccount) {
        Instant now = Instant.now(clock);
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", userAccount.getEmail());
        payload.put("userId", userAccount.getId());
        payload.put("email", userAccount.getEmail());
        payload.put("role", userAccount.getRole().name());
        payload.put("institutionId", userAccount.getInstitutionId());
        payload.put("branchId", userAccount.getBranchId());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plusSeconds(properties.getExpirationSeconds()).getEpochSecond());

        try {
            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(payload);
            String signature = sign(encodedHeader + "." + encodedPayload);
            return encodedHeader + "." + encodedPayload + "." + signature;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize JWT payload", ex);
        }
    }

    public AuthenticatedUser parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new AuthenticationFailedException("Malformed JWT token");
            }
            String signedContent = parts[0] + "." + parts[1];
            String expectedSignature = sign(signedContent);
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new AuthenticationFailedException("Invalid JWT signature");
            }

            Map<String, Object> payload = objectMapper.readValue(URL_DECODER.decode(parts[1]), MAP_TYPE);
            long expirationEpoch = numberClaim(payload, "exp");
            if (Instant.now(clock).isAfter(Instant.ofEpochSecond(expirationEpoch))) {
                throw new AuthenticationFailedException("JWT token has expired");
            }
            return new AuthenticatedUser(
                    numberClaim(payload, "userId"),
                    stringClaim(payload, "email"),
                    UserRole.valueOf(stringClaim(payload, "role")),
                    nullableNumberClaim(payload, "institutionId"),
                    nullableNumberClaim(payload, "branchId"));
        } catch (AuthenticationFailedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AuthenticationFailedException("Invalid JWT token");
        }
    }

    private String encodeJson(Map<String, Object> source) throws JsonProcessingException {
        return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(source));
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign JWT token", ex);
        }
    }

    private long numberClaim(Map<String, Object> payload, String claim) {
        Object value = payload.get(claim);
        if (!(value instanceof Number number)) {
            throw new AuthenticationFailedException("JWT claim " + claim + " is missing");
        }
        return number.longValue();
    }

    private Long nullableNumberClaim(Map<String, Object> payload, String claim) {
        Object value = payload.get(claim);
        if (value == null) {
            return null;
        }
        if (!(value instanceof Number number)) {
            throw new AuthenticationFailedException("JWT claim " + claim + " is invalid");
        }
        return number.longValue();
    }

    private String stringClaim(Map<String, Object> payload, String claim) {
        Object value = payload.get(claim);
        if (!(value instanceof String stringValue) || stringValue.isBlank()) {
            throw new AuthenticationFailedException("JWT claim " + claim + " is missing");
        }
        return stringValue;
    }
}
