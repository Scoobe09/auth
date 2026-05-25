package com.peredereevin.authservice.security;

import com.peredereevin.authservice.config.JwtProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class KeyPairProvider {
    private static final Logger log = LoggerFactory.getLogger(KeyPairProvider.class);
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public KeyPairProvider(JwtProperties jwtProperties) {
        Path privateKeyPath = Path.of(jwtProperties.getRsa().getPrivateKeyPath());
        Path publicKeyPath = Path.of(jwtProperties.getRsa().getPublicKeyPath());

        // Автоматическая генерация, если ключи отсутствуют
        if (!Files.exists(privateKeyPath) || !Files.exists(publicKeyPath)) {
            log.info("RSA keys not found at {} / {}, generating new pair...", privateKeyPath, publicKeyPath);
            generateAndSaveKeys(privateKeyPath, publicKeyPath);
        } else {
            log.info("Loading existing RSA keys from {} and {}", privateKeyPath, publicKeyPath);
        }

        this.privateKey = loadPrivateKey(privateKeyPath);
        this.publicKey = loadPublicKey(publicKeyPath);
        log.info("RSA key pair loaded successfully");
    }

    private void generateAndSaveKeys(Path privateKeyPath, Path publicKeyPath) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            // Создаём директорию keys/, если её нет
            Files.createDirectories(privateKeyPath.getParent());

            // Сохраняем приватный ключ в PKCS#8 PEM
            String privatePem = "-----BEGIN PRIVATE KEY-----\n" +
                    Base64.getMimeEncoder().encodeToString(keyPair.getPrivate().getEncoded()) +
                    "\n-----END PRIVATE KEY-----";
            Files.writeString(privateKeyPath, privatePem);

            // Сохраняем публичный ключ в X.509 PEM
            String publicPem = "-----BEGIN PUBLIC KEY-----\n" +
                    Base64.getMimeEncoder().encodeToString(keyPair.getPublic().getEncoded()) +
                    "\n-----END PUBLIC KEY-----";
            Files.writeString(publicKeyPath, publicPem);

            log.info("New RSA key pair generated and saved to {} and {}", privateKeyPath, publicKeyPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA keys", e);
        }
    }

    private RSAPrivateKey loadPrivateKey(Path path) {
        try {
            String key = Files.readString(path)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA private key from " + path, e);
        }
    }

    private RSAPublicKey loadPublicKey(Path path) {
        try {
            String key = Files.readString(path)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA public key from " + path, e);
        }
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }
}