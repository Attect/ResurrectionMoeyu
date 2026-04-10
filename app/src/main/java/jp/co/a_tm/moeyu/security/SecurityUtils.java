package jp.co.a_tm.moeyu.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 安全工具类，提供加密和签名功能
 * 
 * <p>主要功能包括：</p>
 * <ul>
 *   <li>SHA-256 签名生成（推荐）</li>
 *   <li>SHA-1 签名生成（向后兼容）</li>
 *   <li>Base64 编码支持</li>
 *   <li>签名验证</li>
 * </ul>
 * 
 * @author code-builder
 * @version 1.0
 * @since 2026-03-27
 */
public class SecurityUtils {

    /** SHA-256 算法标识 */
    public static final String ALGORITHM_SHA_256 = "SHA-256";
    
    /** SHA-1 算法标识（向后兼容） */
    public static final String ALGORITHM_SHA_1 = "SHA-1";
    
    /** UTF-8 字符集 */
    private static final String CHARSET_UTF_8 = StandardCharsets.UTF_8.name();
    
    /** Base64 编码器 */
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    
    /** Base64 解码器 */
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    /**
     * 私有的默认构造函数，防止实例化
     */
    private SecurityUtils() {
        throw new UnsupportedOperationException("SecurityUtils is a utility class");
    }

    /**
     * 生成 SHA-256 签名（推荐）
     * 
     * <p>使用 SHA-256 算法对输入数据进行签名，返回十六进制格式的签名字符串。</p>
     * 
     * @param data 待签名的数据
     * @return SHA-256 签名字符串（十六进制格式）
     * @throws SecurityException 当签名生成失败时抛出
     */
    public static String generateSignatureSHA256(String data) throws SecurityException {
        return generateSignature(data, ALGORITHM_SHA_256);
    }

    /**
     * 生成 SHA-1 签名（向后兼容）
     * 
     * <p>使用 SHA-1 算法对输入数据进行签名，用于与旧系统保持兼容。</p>
     * 
     * @param data 待签名的数据
     * @return SHA-1 签名字符串（十六进制格式）
     * @throws SecurityException 当签名生成失败时抛出
     */
    public static String generateSignatureSHA1(String data) throws SecurityException {
        return generateSignature(data, ALGORITHM_SHA_1);
    }

    /**
     * 通用签名生成方法
     * 
     * <p>支持多种加密算法的签名生成，可根据需要选择 SHA-256 或 SHA-1。</p>
     * 
     * @param data 待签名的数据
     * @param algorithm 加密算法（SHA-256 或 SHA-1）
     * @return 签名字符串（十六进制格式）
     * @throws SecurityException 当算法不支持或签名生成失败时抛出
     */
    public static String generateSignature(String data, String algorithm) throws SecurityException {
        if (data == null || data.isEmpty()) {
            throw new SecurityException("待签名的数据不能为空");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(data.getBytes(CHARSET_UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("不支持的加密算法：" + algorithm, e);
        } catch (Exception e) {
            throw new SecurityException("签名生成失败", e);
        }
    }

    /**
     * 生成 Base64 编码的 SHA-256 签名
     * 
     * <p>使用 SHA-256 算法生成签名后进行 Base64 编码，适用于需要传输的场景。</p>
     * 
     * @param data 待签名的数据
     * @return Base64 编码的 SHA-256 签名
     * @throws SecurityException 当签名生成失败时抛出
     */
    public static String generateSignatureSHA256Base64(String data) throws SecurityException {
        String hexSignature = generateSignatureSHA256(data);
        return BASE64_ENCODER.encodeToString(hexSignature.getBytes());
    }

    /**
     * 生成 Base64 编码的 SHA-1 签名
     * 
     * @param data 待签名的数据
     * @return Base64 编码的 SHA-1 签名
     * @throws SecurityException 当签名生成失败时抛出
     */
    public static String generateSignatureSHA1Base64(String data) throws SecurityException {
        String hexSignature = generateSignatureSHA1(data);
        return BASE64_ENCODER.encodeToString(hexSignature.getBytes());
    }

    /**
     * 验证 SHA-256 签名
     * 
     * <p>重新生成签名并与提供的签名进行比对，验证数据完整性。</p>
     * 
     * @param data 原始数据
     * @param signature 待验证的签名
     * @return 签名是否有效
     */
    public static boolean verifySignatureSHA256(String data, String signature) {
        try {
            String expectedSignature = generateSignatureSHA256(data);
            return expectedSignature.equals(signature);
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * 验证 SHA-1 签名
     * 
     * @param data 原始数据
     * @param signature 待验证的签名
     * @return 签名是否有效
     */
    public static boolean verifySignatureSHA1(String data, String signature) {
        try {
            String expectedSignature = generateSignatureSHA1(data);
            return expectedSignature.equals(signature);
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * 通用签名验证方法
     * 
     * @param data 原始数据
     * @param signature 待验证的签名
     * @param algorithm 加密算法
     * @return 签名是否有效
     */
    public static boolean verifySignature(String data, String signature, String algorithm) {
        try {
            String expectedSignature = generateSignature(data, algorithm);
            return expectedSignature.equals(signature);
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 将十六进制字符串转换为字节数组
     * 
     * @param hexString 十六进制字符串
     * @return 字节数组
     */
    public static byte[] hexToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Base64 编码
     * 
     * @param data 待编码的数据
     * @return Base64 编码后的字符串
     */
    public static String encodeBase64(String data) {
        return BASE64_ENCODER.encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64 解码
     * 
     * @param encodedData Base64 编码的数据
     * @return 解码后的字符串
     */
    public static String decodeBase64(String encodedData) {
        byte[] decodedBytes = BASE64_DECODER.decode(encodedData);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    /**
     * 安全异常类
     * 
     * <p>用于处理加密和签名过程中的异常情况。</p>
     */
    public static class SecurityException extends Exception {
        
        public SecurityException(String message) {
            super(message);
        }

        public SecurityException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
