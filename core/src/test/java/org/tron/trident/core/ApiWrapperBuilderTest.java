package org.tron.trident.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.grpc.ClientInterceptor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tron.trident.core.interceptor.TimeoutInterceptor;
import org.tron.trident.core.key.KeyPair;

/**
 * Unit tests for ApiWrapperBuilder class
 */
class ApiWrapperBuilderTest {

  private static final String TEST_PRIVATE_KEY = KeyPair.generate().toPrivateKey();
  private static final String TEST_API_KEY = "test-api-key-12345";
  private static final long TEST_TIMEOUT_MS = 5000L;

  @TempDir
  static Path tempDir;

  private static File testCertFile;

  @BeforeAll
  static void setUp() throws IOException {
    // Create a temporary certificate file for TLS testing
    testCertFile = tempDir.resolve("test-cert.pem").toFile();
    Files.write(testCertFile.toPath(), ("-----BEGIN CERTIFICATE-----\nTEST "
        + "CERTIFICATE\n-----END CERTIFICATE-----").getBytes());
  }

  @Test
  void testConstructorWithAllParameters() {
    // Test the main constructor with all three parameters
    ApiWrapperBuilder builder = new ApiWrapperBuilder(
        Constant.FULLNODE_NILE,
        Constant.FULLNODE_NILE_SOLIDITY,
        TEST_PRIVATE_KEY
    );
    assertNotNull(builder);
    assertEquals(Constant.FULLNODE_NILE, builder.getGrpcEndpoint());
    assertEquals(Constant.FULLNODE_NILE_SOLIDITY, builder.getGrpcEndpointSolidity());
    assertEquals(TEST_PRIVATE_KEY, builder.getHexPrivateKey());
    assertFalse(builder.isUseTLS());
    assertEquals(0, builder.getCustomInterceptors().size());
    assertEquals(0, builder.buildInterceptors().size());

    // set tls/ApiKey/Timeout
    builder = builder.withTLS()
        .withApiKey(TEST_API_KEY)
        .withTimeout(TEST_TIMEOUT_MS);

    assertTrue(builder.isUseTLS());
    assertEquals(TEST_API_KEY, builder.getApiKey());
    assertEquals(TEST_TIMEOUT_MS, builder.getTimeoutMs());

    // TimeoutInterceptor is assembled first (innermost) so the configured deadline
    // cannot be overridden by the API-key header or custom interceptors
    List<ClientInterceptor> interceptors = builder.buildInterceptors();
    assertEquals(2, interceptors.size());
    assertTrue(interceptors.get(0) instanceof TimeoutInterceptor);
    assertFalse(interceptors.get(1) instanceof TimeoutInterceptor);

    // Verify that the builder can be built successfully
    ApiWrapper wrapper = builder.build();
    assertNotNull(wrapper);
    assertNotNull(wrapper.keyPair);
    assertEquals(TEST_PRIVATE_KEY, wrapper.keyPair.toPrivateKey());
    wrapper.close();

    // set builder only with grpcEndpoint
    builder = new ApiWrapperBuilder(
        Constant.FULLNODE_NILE
    );
    assertNotNull(builder);
    assertEquals(Constant.FULLNODE_NILE, builder.getGrpcEndpoint());
    assertNull(builder.getGrpcEndpointSolidity());
    builder = builder.withGrpcEndpointSolidity(Constant.FULLNODE_NILE_SOLIDITY);
    assertEquals(Constant.FULLNODE_NILE_SOLIDITY, builder.getGrpcEndpointSolidity());
    builder = builder.withTLS(testCertFile);
    assertTrue(builder.isUseTLS());
    assertEquals(testCertFile, builder.getTrustCert());
  }

  @Test
  void testPrivateKeyAcceptsHexPrefix() {
    // "0x"-prefixed keys are accepted and normalized to the plain 64-char form
    ApiWrapperBuilder builder = new ApiWrapperBuilder(Constant.FULLNODE_NILE)
        .withPrivateKey("0x" + TEST_PRIVATE_KEY);
    assertEquals(TEST_PRIVATE_KEY, builder.getHexPrivateKey());

    // too-short keys are rejected
    assertThrows(IllegalArgumentException.class, () -> {
      new ApiWrapperBuilder(Constant.FULLNODE_NILE)
          .withPrivateKey(TEST_PRIVATE_KEY.substring(2));
    });
  }

  @Test
  void testRepeatedSettersLastWins() {
    ApiWrapperBuilder builder = new ApiWrapperBuilder(Constant.FULLNODE_NILE)
        .withTimeout(1000L)
        .withTimeout(TEST_TIMEOUT_MS)
        .withApiKey("first-key")
        .withApiKey(TEST_API_KEY);

    assertEquals(TEST_TIMEOUT_MS, builder.getTimeoutMs());
    assertEquals(TEST_API_KEY, builder.getApiKey());
    // exactly one TimeoutInterceptor and one header interceptor, not stacked copies
    assertEquals(2, builder.buildInterceptors().size());
  }

  @Test
  void testAddInterceptors() {
    ClientInterceptor interceptor = new TimeoutInterceptor(1000L);
    ApiWrapperBuilder builder = new ApiWrapperBuilder(Constant.FULLNODE_NILE)
        .addInterceptors(Arrays.asList(interceptor, null));

    // null elements are filtered
    assertEquals(1, builder.getCustomInterceptors().size());
    assertEquals(1, builder.buildInterceptors().size());

    assertThrows(IllegalArgumentException.class, () -> {
      new ApiWrapperBuilder(Constant.FULLNODE_NILE).addInterceptors(null);
    });

    // the getter must not expose the internal list for mutation
    assertThrows(UnsupportedOperationException.class, () -> {
      builder.getCustomInterceptors().add(null);
    });
  }

  @Test
  void testWithTlsUsesSystemTrustCerts() {
    // withTLS() means "system trust certificates", so it clears a custom one
    ApiWrapperBuilder builder = new ApiWrapperBuilder(Constant.FULLNODE_NILE)
        .withTLS(testCertFile)
        .withTLS();
    assertTrue(builder.isUseTLS());
    assertNull(builder.getTrustCert());

    // and the reverse order keeps the custom certificate
    builder.withTLS(testCertFile);
    assertEquals(testCertFile, builder.getTrustCert());
  }

  @Test
  void testConstructorWithNullParameters() {
    // Test constructor with null parameters
    assertThrows(IllegalArgumentException.class, () -> {
      new ApiWrapperBuilder(null,
          Constant.FULLNODE_NILE_SOLIDITY, TEST_PRIVATE_KEY);
    });

    assertThrows(IllegalArgumentException.class, () -> {
      new ApiWrapperBuilder(Constant.FULLNODE_NILE,
          null, TEST_PRIVATE_KEY);
    });

    assertThrows(IllegalArgumentException.class, () -> {
      new ApiWrapperBuilder(Constant.FULLNODE_NILE,
          Constant.FULLNODE_NILE_SOLIDITY, null);
    });

    assertThrows(IllegalArgumentException.class, () -> {
      new ApiWrapperBuilder(Constant.FULLNODE_NILE,
          Constant.FULLNODE_NILE_SOLIDITY, "123");
    });

    // empty endpoints are rejected like null ones
    assertThrows(IllegalArgumentException.class, () -> {
      new ApiWrapperBuilder("");
    });

    assertThrows(IllegalArgumentException.class, () -> {
      new ApiWrapperBuilder(Constant.FULLNODE_NILE).withGrpcEndpointSolidity("");
    });
  }

  @Test
  void testToString() {
    ApiWrapperBuilder builder = new ApiWrapperBuilder(
        Constant.FULLNODE_NILE,
        Constant.FULLNODE_NILE_SOLIDITY,
        TEST_PRIVATE_KEY
    );
    String toStringResult = builder.toString();
    assertTrue(toStringResult.contains("grpcEndpoint=" + Constant.FULLNODE_NILE));
    assertTrue(toStringResult.contains("grpcEndpointSolidity=" + Constant.FULLNODE_NILE_SOLIDITY));
    assertTrue(toStringResult.contains("hexPrivateKey=****"));
    assertFalse(toStringResult.contains(TEST_PRIVATE_KEY));
    assertTrue(toStringResult.contains("useTLS=false"));
    assertTrue(toStringResult.contains("trustCert=null"));

    builder.withTLS(testCertFile).withApiKey(TEST_API_KEY);
    toStringResult = builder.toString();
    assertTrue(toStringResult.contains("useTLS=true"));
    assertTrue(toStringResult.contains("trustCert=" + testCertFile.getAbsolutePath()));
    // the API key must never appear in logs
    assertTrue(toStringResult.contains("apiKey=****"));
    assertFalse(toStringResult.contains(TEST_API_KEY));
  }

}
