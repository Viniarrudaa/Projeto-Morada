package br.com.morada.security;

import br.com.morada.domain.User;
import br.com.morada.domain.UserRole;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expirationMinutes;

    public JwtTokenService(
        ObjectMapper objectMapper,
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMinutes = expirationMinutes;
    }

    public String createToken(User user) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = Map.of(
                "sub", user.getId(),
                "cpf", user.getCpf(),
                "role", user.getRole().name(),
                "exp", Instant.now().plusSeconds(expirationMinutes * 60).getEpochSecond()
            );

            String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
            return unsignedToken + "." + sign(unsignedToken);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create authentication token.", exception);
        }
    }

    public Optional<TokenClaims> parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }

            String unsignedToken = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(unsignedToken).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                return Optional.empty();
            }

            Map<String, Object> payload = objectMapper.readValue(
                BASE64_URL_DECODER.decode(parts[1]),
                new TypeReference<>() {
                }
            );

            long expiration = ((Number) payload.get("exp")).longValue();
            if (Instant.now().isAfter(Instant.ofEpochSecond(expiration))) {
                return Optional.empty();
            }

            Long userId = ((Number) payload.get("sub")).longValue();
            String cpf = (String) payload.get("cpf");
            UserRole role = UserRole.valueOf((String) payload.get("role"));
            return Optional.of(new TokenClaims(userId, cpf, role));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    public record TokenClaims(Long userId, String cpf, UserRole role) {
    }
}
