// 로컬 실행에서 Access Token 서명과 검증에 사용할 RSA 키를 제공함.
package com.fourpillars.backend.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtKeyConfig {

    @Bean
    KeyPair jwtKeyPair(
            @Value("${fourpillars.jwt.private-key-base64}") String privateKeyBase64,
            @Value("${fourpillars.jwt.public-key-base64}") String publicKeyBase64
    ) {
        try {
            var keyFactory = KeyFactory.getInstance("RSA");
            var privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64)));
            var publicKey = (RSAPublicKey) keyFactory.generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64)));
            return new KeyPair(publicKey, privateKey);
        } catch (Exception exception) {
            throw new IllegalStateException("JWT RSA 키를 불러오지 못했습니다.", exception);
        }
    }

    @Bean
    JwtEncoder jwtEncoder(KeyPair jwtKeyPair) {
        var publicKey = (RSAPublicKey) jwtKeyPair.getPublic();
        var privateKey = (RSAPrivateKey) jwtKeyPair.getPrivate();
        var rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new JWKSet(rsaKey)));
    }

    @Bean
    JwtDecoder jwtDecoder(KeyPair jwtKeyPair) {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) jwtKeyPair.getPublic()).build();
    }
}
