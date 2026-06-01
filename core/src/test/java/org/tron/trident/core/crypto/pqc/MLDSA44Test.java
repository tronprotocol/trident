package org.tron.trident.core.crypto.pqc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import org.junit.jupiter.api.Test;
import org.tron.trident.proto.Chain.PQScheme;

class MLDSA44Test {

  private static byte[] seed() {
    byte[] s = new byte[MLDSA44.SEED_LENGTH];
    new SecureRandom().nextBytes(s);
    return s;
  }

  @Test
  void keygenProducesCanonicalLengths() {
    MLDSA44 kp = new MLDSA44();
    assertEquals(MLDSA44.PRIVATE_KEY_LENGTH, kp.getPrivateKey().length);
    assertEquals(MLDSA44.PUBLIC_KEY_LENGTH, kp.getPublicKey().length);
    // ML-DSA persists just the encoded private key.
    assertEquals(MLDSA44.PRIVATE_KEY_LENGTH, kp.getPersistedPrivateKey().length);
    assertEquals(PQScheme.ML_DSA_44, kp.getScheme());
  }

  @Test
  void seedIsDeterministic() {
    byte[] seed = seed();
    MLDSA44 a = new MLDSA44(seed);
    MLDSA44 b = new MLDSA44(seed);
    assertArrayEquals(a.getPrivateKey(), b.getPrivateKey());
    assertArrayEquals(a.getPublicKey(), b.getPublicKey());
  }

  @Test
  void rejectsBadSeedLength() {
    assertThrows(IllegalArgumentException.class, () -> new MLDSA44(new byte[10]));
  }

  @Test
  void signVerifyRoundTrip() {
    MLDSA44 kp = new MLDSA44(seed());
    byte[] msg = "hello pq".getBytes(StandardCharsets.UTF_8);
    byte[] sig = kp.sign(msg);
    assertEquals(MLDSA44.SIGNATURE_LENGTH, sig.length);
    assertTrue(kp.verify(msg, sig));
  }

  @Test
  void verifyFailsOnTamperedMessage() {
    MLDSA44 kp = new MLDSA44(seed());
    byte[] sig = kp.sign("original".getBytes(StandardCharsets.UTF_8));
    assertFalse(kp.verify("tampered".getBytes(StandardCharsets.UTF_8), sig));
  }

  @Test
  void derivePublicKeyMatches() {
    MLDSA44 kp = new MLDSA44(seed());
    assertArrayEquals(kp.getPublicKey(), MLDSA44.derivePublicKey(kp.getPrivateKey()));
  }

  @Test
  void mismatchedKeypairRejected() {
    MLDSA44 a = new MLDSA44(seed());
    MLDSA44 b = new MLDSA44(seed());
    assertThrows(IllegalArgumentException.class,
        () -> new MLDSA44(a.getPrivateKey(), b.getPublicKey()));
  }

  @Test
  void addressIsCanonical() {
    MLDSA44 kp = new MLDSA44(seed());
    byte[] addr = kp.getAddress();
    assertEquals(21, addr.length);
    assertEquals(0x41, addr[0] & 0xff);
  }
}
