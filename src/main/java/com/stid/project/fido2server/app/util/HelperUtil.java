package com.stid.project.fido2server.app.util;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;
import com.webauthn4j.util.Base64UrlUtil;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class HelperUtil {
    private static final SecureRandom random = new SecureRandom();

    public static Optional<byte[]> getHash(DigestHash hash, byte[] rawBytes) {
        try {
            byte[] bytes = MessageDigest.getInstance(hash.name()).digest(rawBytes);
            return Optional.of(bytes);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static byte[] randomUUIDAsBytes() {
        UUID uuid = UUID.randomUUID();
        long hi = uuid.getMostSignificantBits();
        long lo = uuid.getLeastSignificantBits();
        return ByteBuffer.allocate(16).putLong(hi).putLong(lo).array();
    }

    public static String randomSecret() {
        return Base64UrlUtil.encodeToString(randomBytes(48));
    }

    public static byte[] randomUserHandle() {
        return randomUUIDAsBytes();
    }

    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[Math.max(length, 0)];
        random.nextBytes(bytes);
        return bytes;
    }

    public static String toBase64Url(byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    public static String toHex(byte[] bytes) {
        return BaseEncoding.base16().encode(bytes).toLowerCase();
    }

    public static Optional<byte[]> base64UrlToByteArray(String base64Url) {
        try {
            return Optional.of(Base64.getDecoder().decode(base64Url));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<byte[]> hexToByteArray(String hex) {
        try {
            return Optional.of(BaseEncoding.base16().decode(hex.toUpperCase()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static byte[] concat(byte[]... bytes) {
        return Bytes.concat(bytes);
    }

    public enum DigestHash {
        MD5, SHA1, SHA256, SHA512
    }

}
