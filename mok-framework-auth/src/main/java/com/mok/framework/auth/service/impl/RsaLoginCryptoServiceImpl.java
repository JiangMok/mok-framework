package com.mok.framework.auth.service.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.mok.framework.auth.config.LoginCryptoProperties;
import com.mok.framework.auth.service.LoginCryptoService;
import com.mok.framework.common.BusinessException;
import com.mok.framework.model.dto.LoginChallengeResponse;
import com.mok.framework.model.dto.LoginCredentials;
import com.mok.framework.model.dto.LoginRequest;
import jakarta.validation.Validator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** RSA-OAEP SHA-256 登录凭据解密与一次性 challenge 校验。 */
@Service
public class RsaLoginCryptoServiceImpl implements LoginCryptoService {

    private static final String CHALLENGE_PREFIX = "security:login:challenge:";
    private static final int RSA_BITS = 3072;
    private static final int MAX_PLAINTEXT_BYTES = RSA_BITS / 8 - 2 * 32 - 2;
    private static final OAEPParameterSpec OAEP_PARAMETERS = new OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
    private static final RedisScript<Long> CONSUME_CHALLENGE = RedisScript.of(
            "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('DEL', KEYS[1]); end; return 0;", Long.class);

    private final StringRedisTemplate redisTemplate;
    private final Validator validator;
    private final ObjectReader credentialsReader;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, RSAPrivateCrtKey> privateKeys;
    private final String activeKeyId;
    private final String publicKey;
    private final long challengeTtlSeconds;

    public RsaLoginCryptoServiceImpl(LoginCryptoProperties properties,
                                     StringRedisTemplate redisTemplate,
                                     ObjectMapper objectMapper,
                                     Validator validator) {
        this.redisTemplate = redisTemplate;
        this.validator = validator;
        this.credentialsReader = objectMapper.copy()
                .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .readerFor(LoginCredentials.class);
        this.activeKeyId = properties.getKeyId();
        this.challengeTtlSeconds = properties.getChallengeTtlSeconds();
        if (challengeTtlSeconds < 30 || challengeTtlSeconds > 300) {
            throw new IllegalStateException("登录凭证有效期必须在30-300秒之间");
        }

        Map<String, RSAPrivateCrtKey> keys = new HashMap<>();
        RSAPrivateCrtKey activeKey = loadPrivateKey(activeKeyId, properties.getPrivateKeyPath());
        keys.put(activeKeyId, activeKey);
        if (properties.getPreviousPrivateKeyPaths() != null) {
            properties.getPreviousPrivateKeyPaths().forEach((keyId, path) -> {
                if (keys.containsKey(keyId)) {
                    throw new IllegalStateException("登录加密密钥标识重复");
                }
                keys.put(keyId, loadPrivateKey(keyId, path));
            });
        }
        this.privateKeys = Map.copyOf(keys);
        try {
            this.publicKey = Base64.getEncoder().encodeToString(KeyFactory.getInstance("RSA")
                    .generatePublic(new RSAPublicKeySpec(
                            activeKey.getModulus(), activeKey.getPublicExponent())).getEncoded());
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("无法导出登录公钥，请检查RSA密钥配置");
        }
    }

    @Override
    public LoginChallengeResponse createChallenge() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String challengeId = HexFormat.of().formatHex(randomBytes);
        Boolean created = redisTemplate.opsForValue().setIfAbsent(
                CHALLENGE_PREFIX + challengeId, activeKeyId, challengeTtlSeconds, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(created)) {
            throw new BusinessException("登录凭证生成失败，请重试");
        }
        return new LoginChallengeResponse(activeKeyId, publicKey, challengeId, challengeTtlSeconds);
    }

    @Override
    public LoginCredentials decryptAndConsume(LoginRequest request) {
        if (request == null || !validator.validate(request).isEmpty()) {
            throw invalidCredentials();
        }
        RSAPrivateCrtKey privateKey = privateKeys.get(request.getKeyId());
        if (privateKey == null) {
            throw invalidCredentials();
        }

        byte[] plaintext = null;
        LoginCredentials credentials;
        try {
            byte[] ciphertext = Base64.getDecoder().decode(request.getEncryptedCredentials());
            if (ciphertext.length != RSA_BITS / 8) {
                throw invalidCredentials();
            }
            // Cipher 非线程安全，每次请求独立创建，两个摘要参数均显式指定。
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP_PARAMETERS);
            plaintext = cipher.doFinal(ciphertext);
            if (plaintext.length > MAX_PLAINTEXT_BYTES) {
                throw invalidCredentials();
            }
            credentials = credentialsReader.readValue(plaintext);
            if (credentials == null || !validator.validate(credentials).isEmpty()
                    || !request.getCaptchaKey().equals(credentials.getCaptchaKey())) {
                throw invalidCredentials();
            }
        } catch (GeneralSecurityException | IOException | IllegalArgumentException exception) {
            // 不传递解析异常，避免 JSON 异常或密钥信息把解密后的凭据带入日志。
            throw invalidCredentials();
        } finally {
            if (plaintext != null) {
                Arrays.fill(plaintext, (byte) 0);
            }
        }

        Long consumed = redisTemplate.execute(CONSUME_CHALLENGE,
                List.of(CHALLENGE_PREFIX + credentials.getChallengeId()), request.getKeyId());
        if (!Long.valueOf(1L).equals(consumed)) {
            throw invalidCredentials();
        }
        return credentials;
    }

    private RSAPrivateCrtKey loadPrivateKey(String keyId, String privateKeyPath) {
        if (keyId == null || !keyId.matches("[A-Za-z0-9_-]{1,64}")
                || privateKeyPath == null || privateKeyPath.isBlank()) {
            throw new IllegalStateException(
                    "请配置登录RSA密钥标识和外部私钥路径 LOGIN_RSA_PRIVATE_KEY_PATH");
        }
        byte[] pemBytes = null;
        byte[] derBytes = null;
        try {
            // 仅允许本地绝对路径，不通过 URL 加载密钥。
            Path path = Path.of(privateKeyPath);
            if (!path.isAbsolute()) {
                throw new IllegalStateException("登录RSA私钥必须使用本地绝对路径");
            }
            try (InputStream input = Files.newInputStream(path)) {
                pemBytes = input.readNBytes(16385);
            }
            if (pemBytes.length > 16384) {
                throw new IllegalStateException("登录RSA私钥文件过大");
            }
            String pem = new String(pemBytes, StandardCharsets.US_ASCII).trim();
            String header = "-----BEGIN PRIVATE KEY-----";
            String footer = "-----END PRIVATE KEY-----";
            if (!pem.startsWith(header) || !pem.endsWith(footer)) {
                throw new IllegalStateException("登录RSA私钥必须为PKCS#8 PEM格式");
            }
            derBytes = Base64.getDecoder().decode(
                    pem.substring(header.length(), pem.length() - footer.length())
                            .replaceAll("\\s", ""));
            var key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(derBytes));
            if (!(key instanceof RSAPrivateCrtKey rsaKey)
                    || rsaKey.getModulus().bitLength() != RSA_BITS) {
                throw new IllegalStateException("登录RSA私钥必须为3072位");
            }
            return rsaKey;
        } catch (IOException | GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("无法加载登录RSA私钥，请检查外部文件及PKCS#8格式");
        } finally {
            if (pemBytes != null) {
                Arrays.fill(pemBytes, (byte) 0);
            }
            if (derBytes != null) {
                Arrays.fill(derBytes, (byte) 0);
            }
        }
    }

    private BusinessException invalidCredentials() {
        return new BusinessException(1003, "登录凭据无效或已过期，请重新登录");
    }
}
