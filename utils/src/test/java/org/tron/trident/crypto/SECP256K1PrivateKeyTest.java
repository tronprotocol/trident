package org.tron.trident.crypto;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.tron.trident.crypto.tuwenitypes.Bytes32;
import org.tron.trident.crypto.tuwenitypes.UInt256;

/**
 * Unit tests for the secp256k1 private key scalar range check
 */
class SECP256K1PrivateKeyTest {

  private static final BigInteger N = SECP256K1.CURVE.getN();

  private static Bytes32 scalar(BigInteger value) {
    return UInt256.valueOf(value).toBytes();
  }

  @Test
  void testCreateRejectsOutOfRangeScalars() {
    for (BigInteger invalid : new BigInteger[] {BigInteger.ZERO, N, N.add(BigInteger.ONE)}) {
      IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
          () -> SECP256K1.PrivateKey.create(scalar(invalid)));
      assertTrue(e.getMessage().contains("[1, n - 1]"));
    }
  }

  @Test
  void testCreateAcceptsRangeBoundaries() {
    // 1 and n - 1 are the inclusive bounds of the valid range
    assertNotNull(SECP256K1.PrivateKey.create(scalar(BigInteger.ONE)));
    assertNotNull(SECP256K1.PrivateKey.create(scalar(N.subtract(BigInteger.ONE))));
    // generated keys pass their own validation
    assertNotNull(SECP256K1.KeyPair.generate());
  }
}
