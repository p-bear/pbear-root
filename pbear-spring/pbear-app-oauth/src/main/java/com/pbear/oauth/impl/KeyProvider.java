package com.pbear.oauth.impl;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Getter
@Component
public class KeyProvider {
  private static final String PRIVATE_KEY_NAME = "pbear-oauth-private.pem";
  private KeyPair keyPair;

  @PostConstruct
  private void loadKeyPair() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    // Read privateKey Data from file
    Resource pbearKeyResource = new ClassPathResource(PRIVATE_KEY_NAME);
    StringBuilder stringBuilder = new StringBuilder();
    try (Reader reader = new BufferedReader(new InputStreamReader(pbearKeyResource.getInputStream(), StandardCharsets.UTF_8))) {
      int c;
      while ((c = reader.read()) != -1) {
        stringBuilder.append((char) c);
      }
    }

    // extract keyData to byte[]
    byte[] keyData = Base64.getDecoder().decode(stringBuilder.toString()
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replaceAll(System.lineSeparator(), "")
        .replace("-----END PRIVATE KEY-----", ""));

    // load public, private key
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");

    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyData);
    PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

    RSAPrivateCrtKey crt = (RSAPrivateCrtKey) privateKey;
    RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(crt.getModulus(), crt.getPublicExponent());
    PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

    this.keyPair = new KeyPair(publicKey, privateKey);
  }
}
