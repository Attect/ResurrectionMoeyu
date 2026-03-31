package jp.co.a_tm.moeyu.security;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * SecurityUtils 单元测试类
 * 
 * <p>测试范围包括：</p>
 * <ul>
 *   <li>SHA-256 签名生成</li>
 *   <li>SHA-1 签名生成（向后兼容）</li>
 *   <li>Base64 编码/解码</li>
 *   <li>签名验证</li>
 *   <li>异常处理</li>
 * </ul>
 * 
 * @author code-builder
 * @version 1.0
 * @since 2026-03-27
 */
public class SecurityUtilsTest {

    private static final String TEST_DATA = "test_signature_data_2024";
    private static final String EMPTY_DATA = "";
    private static final String NULL_DATA = null;

    /**
     * 测试 SHA-256 签名生成
     */
    @Test
    public void testGenerateSignatureSHA256_Success() throws SecurityUtils.SecurityException {
        // Given
        String testData = TEST_DATA;

        // When
        String signature = SecurityUtils.generateSignatureSHA256(testData);

        // Then
        assertNotNull("签名不应为 null", signature);
        assertEquals("SHA-256 签名长度应为 64 字符", 64, signature.length());
        assertTrue("签名应只包含十六进制字符", signature.matches("[0-9a-f]+"));
    }

    /**
     * 测试 SHA-1 签名生成（向后兼容）
     */
    @Test
    public void testGenerateSignatureSHA1_Success() throws SecurityUtils.SecurityException {
        // Given
        String testData = TEST_DATA;

        // When
        String signature = SecurityUtils.generateSignatureSHA1(testData);

        // Then
        assertNotNull("签名不应为 null", signature);
        assertEquals("SHA-1 签名长度应为 40 字符", 40, signature.length());
        assertTrue("签名应只包含十六进制字符", signature.matches("[0-9a-f]+"));
    }

    /**
     * 测试通用签名生成方法
     */
    @Test
    public void testGenerateSignature_WithAlgorithm_Success() throws SecurityUtils.SecurityException {
        // Given
        String testData = TEST_DATA;

        // When & Then - SHA-256
        String sha256Signature = SecurityUtils.generateSignature(testData, SecurityUtils.ALGORITHM_SHA_256);
        assertNotNull("SHA-256 签名不应为 null", sha256Signature);
        assertEquals("SHA-256 签名长度应为 64 字符", 64, sha256Signature.length());

        // When & Then - SHA-1
        String sha1Signature = SecurityUtils.generateSignature(testData, SecurityUtils.ALGORITHM_SHA_1);
        assertNotNull("SHA-1 签名不应为 null", sha1Signature);
        assertEquals("SHA-1 签名长度应为 40 字符", 40, sha1Signature.length());

        // SHA-256 和 SHA-1 的签名应该不同
        assertNotEquals("SHA-256 和 SHA-1 签名应不相同", sha256Signature, sha1Signature);
    }

    /**
     * 测试空数据签名生成（边界条件）
     */
    @Test
    public void testGenerateSignatureSHA256_EmptyData() throws SecurityUtils.SecurityException {
        // Given
        String emptyData = "";

        // When
        String signature = SecurityUtils.generateSignatureSHA256(emptyData);

        // Then
        assertNotNull("空数据的签名不应为 null", signature);
        assertEquals("签名长度应为 64 字符", 64, signature.length());
    }

    /**
     * 测试 null 数据签名生成（异常处理）
     */
    @Test
    public void testGenerateSignatureSHA256_NullData_ThrowsException() {
        // Given
        String nullData = null;

        // When & Then
        try {
            SecurityUtils.generateSignatureSHA256(nullData);
            fail("应抛出 SecurityException");
        } catch (SecurityUtils.SecurityException e) {
            assertNotNull("异常消息不应为 null", e.getMessage());
            assertTrue("异常消息应包含'不能为空'", e.getMessage().contains("不能为空"));
        }
    }

    /**
     * 测试 Base64 编码的 SHA-256 签名
     */
    @Test
    public void testGenerateSignatureSHA256Base64_Success() throws SecurityUtils.SecurityException {
        // Given
        String testData = TEST_DATA;

        // When
        String base64Signature = SecurityUtils.generateSignatureSHA256Base64(testData);

        // Then
        assertNotNull("Base64 签名不应为 null", base64Signature);
        assertTrue("Base64 签名长度应大于等于 64", base64Signature.length() >= 64);
    }

    /**
     * 测试 Base64 编码的 SHA-1 签名
     */
    @Test
    public void testGenerateSignatureSHA1Base64_Success() throws SecurityUtils.SecurityException {
        // Given
        String testData = TEST_DATA;

        // When
        String base64Signature = SecurityUtils.generateSignatureSHA1Base64(testData);

        // Then
        assertNotNull("Base64 签名不应为 null", base64Signature);
        assertTrue("Base64 签名长度应大于等于 40", base64Signature.length() >= 40);
    }

    /**
     * 测试 SHA-256 签名验证 - 有效签名
     */
    @Test
    public void testVerifySignatureSHA256_Valid() throws SecurityUtils.SecurityException {
        // Given
        String testData = TEST_DATA;
        String expectedSignature = SecurityUtils.generateSignatureSHA256(testData);

        // When
        boolean isValid = SecurityUtils.verifySignatureSHA256(testData, expectedSignature);

        // Then
        assertTrue("有效签名应返回 true", isValid);
    }

    /**
     * 测试 SHA-256 签名验证 - 无效签名
     */
    @Test
    public void testVerifySignatureSHA256_Invalid() throws SecurityUtils.SecurityException {
        // Given
        String testData = TEST_DATA;
        String invalidSignature = "invalid_signature_12345";

        // When
        boolean isValid = SecurityUtils.verifySignatureSHA256(testData, invalidSignature);

        // Then
        assertFalse("无效签名应返回 false", isValid);
    }

    /**
     * 测试 SHA-1 签名验证 - 有效签名（向后兼容）
     */
    @Test
    public void testVerifySignatureSHA1_Valid() throws SecurityUtils.SecurityException {
        // Given
        String testData = TEST_DATA;
        String expectedSignature = SecurityUtils.generateSignatureSHA1(testData);

        // When
        boolean isValid = SecurityUtils.verifySignatureSHA1(testData, expectedSignature);

        // Then
        assertTrue("有效签名应返回 true", isValid);
    }

    /**
     * 测试通用签名验证方法
     */
    @Test
    public void testVerifySignature_WithAlgorithm_Success() throws SecurityUtils.SecurityException {
        // Given - SHA-256
        String testData = TEST_DATA;
        String sha256Signature = SecurityUtils.generateSignatureSHA256(testData);

        // When & Then - SHA-256 验证
        boolean sha256Valid = SecurityUtils.verifySignature(testData, sha256Signature, SecurityUtils.ALGORITHM_SHA_256);
        assertTrue("SHA-256 签名验证应返回 true", sha256Valid);

        // Given - SHA-1
        String sha1Signature = SecurityUtils.generateSignatureSHA1(testData);

        // When & Then - SHA-1 验证
        boolean sha1Valid = SecurityUtils.verifySignature(testData, sha1Signature, SecurityUtils.ALGORITHM_SHA_1);
        assertTrue("SHA-1 签名验证应返回 true", sha1Valid);
    }

    /**
     * 测试字节数组转十六进制字符串
     */
    @Test
    public void testBytesToHex_Success() {
        // Given
        byte[] testData = new byte[] {0, 1, 15, 16, 255};

        // When
        String hexString = SecurityUtils.bytesToHex(testData);

        // Then
        assertNotNull("十六进制字符串不应为 null", hexString);
        assertEquals("长度应为字节数的 2 倍", testData.length * 2, hexString.length());
        assertTrue("应只包含十六进制字符", hexString.matches("[0-9a-f]+"));
    }

    /**
     * 测试十六进制字符串转字节数组
     */
    @Test
    public void testHexToBytes_Success() {
        // Given
        String hexString = "00010f10ff";

        // When
        byte[] bytes = SecurityUtils.hexToBytes(hexString);

        // Then
        assertNotNull("字节数组不应为 null", bytes);
        assertEquals("长度应为字符串长度的一半", hexString.length() / 2, bytes.length);
    }

    /**
     * 测试 Base64 编码
     */
    @Test
    public void testEncodeBase64_Success() {
        // Given
        String testData = "Hello, World!";

        // When
        String encoded = SecurityUtils.encodeBase64(testData);

        // Then
        assertNotNull("编码结果不应为 null", encoded);
        assertTrue("编码结果长度应大于等于原始数据", encoded.length() >= testData.length());
    }

    /**
     * 测试 Base64 解码
     */
    @Test
    public void testDecodeBase64_Success() {
        // Given
        String testData = "Hello, World!";
        String encoded = SecurityUtils.encodeBase64(testData);

        // When
        String decoded = SecurityUtils.decodeBase64(encoded);

        // Then
        assertEquals("解码结果应与原始数据一致", testData, decoded);
    }

    /**
     * 测试签名的确定性（相同输入产生相同输出）
     */
    @Test
    public void testSignatureDeterminism() throws SecurityUtils.SecurityException {
        // Given
        String testData = "deterministic_test_data";

        // When - 多次生成签名
        String signature1 = SecurityUtils.generateSignatureSHA256(testData);
        String signature2 = SecurityUtils.generateSignatureSHA256(testData);
        String signature3 = SecurityUtils.generateSignatureSHA256(testData);

        // Then - 所有签名应相同
        assertEquals("签名应具有确定性", signature1, signature2);
        assertEquals("签名应具有确定性", signature2, signature3);
    }

    /**
     * 测试特殊字符数据签名
     */
    @Test
    public void testGenerateSignatureSHA256_SpecialCharacters() throws SecurityUtils.SecurityException {
        // Given - 包含特殊字符、中文、数字等
        String testData = "测试数据 Test@Data#2024!";

        // When
        String signature = SecurityUtils.generateSignatureSHA256(testData);

        // Then
        assertNotNull("签名不应为 null", signature);
        assertEquals("签名长度应为 64 字符", 64, signature.length());
    }

    /**
     * 测试长数据签名性能
     */
    @Test
    public void testGenerateSignatureSHA256_LongData() throws SecurityUtils.SecurityException {
        // Given - 生成较长的测试数据（10KB）
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("test_data_segment_").append(i).append("_");
        }
        String longTestData = sb.toString();

        // When - 记录开始时间
        long startTime = System.currentTimeMillis();
        String signature = SecurityUtils.generateSignatureSHA256(longTestData);
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull("签名不应为 null", signature);
        assertEquals("签名长度应为 64 字符", 64, signature.length());
        
        // 性能检查：10KB 数据签名应在 100ms 内完成
        long duration = endTime - startTime;
        assertTrue("签名生成应在合理时间内完成 (实际：" + duration + "ms)", duration < 100);
    }

    /**
     * 测试不支持的算法异常处理
     */
    @Test
    public void testGenerateSignature_UnsupportedAlgorithm() {
        // Given
        String testData = TEST_DATA;
        String unsupportedAlgorithm = "UNSUPPORTED_ALGORITHM";

        // When & Then
        try {
            SecurityUtils.generateSignature(testData, unsupportedAlgorithm);
            fail("应抛出 SecurityException");
        } catch (SecurityUtils.SecurityException e) {
            assertNotNull("异常消息不应为 null", e.getMessage());
            assertTrue("异常消息应包含算法名称", 
                      e.getMessage().contains(unsupportedAlgorithm));
        }
    }

    /**
     * 测试 SecurityUtils 是工具类（不可实例化）
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testSecurityUtils_UtilityClass() {
        // When & Then - 尝试实例化应抛出异常
        new SecurityUtils();
    }
}
