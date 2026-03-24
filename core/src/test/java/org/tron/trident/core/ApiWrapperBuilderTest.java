package org.tron.trident.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
    assertEquals(0, builder.getInterceptors().size());

    // set tls/ApiKey/Timeout
    builder = builder.withTLS()
        .withApiKey(TEST_API_KEY)
        .withTimeout(TEST_TIMEOUT_MS);

    assertTrue(builder.isUseTLS());
    assertEquals(2, builder.getInterceptors().size());
    assertEquals("HeaderAttachingClientInterceptor",
        builder.getInterceptors().get(0).getClass().getSimpleName());
    assertEquals("TimeoutInterceptor",
        builder.getInterceptors().get(1).getClass().getSimpleName());

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
    assertTrue(toStringResult.contains("useTLS=false"));
    assertTrue(toStringResult.contains("trustCert=null"));

    builder.withTLS(testCertFile);
    toStringResult = builder.toString();
    assertTrue(toStringResult.contains("useTLS=true"));
    assertTrue(toStringResult.contains("trustCert=" + testCertFile.getAbsolutePath()));
    }

}
