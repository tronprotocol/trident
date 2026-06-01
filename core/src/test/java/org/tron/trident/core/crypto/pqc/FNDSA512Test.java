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

class FNDSA512Test {

  private static byte[] seed() {
    byte[] s = new byte[FNDSA512.SEED_LENGTH];
    new SecureRandom().nextBytes(s);
    return s;
  }

  @Test
  void keygenProducesCanonicalLengths() {
    FNDSA512 kp = new FNDSA512();
    assertEquals(FNDSA512.PRIVATE_KEY_LENGTH, kp.getPrivateKey().length);
    assertEquals(FNDSA512.PUBLIC_KEY_LENGTH, kp.getPublicKey().length);
    assertEquals(FNDSA512.PRIVATE_KEY_WITH_PUBLIC_KEY_LENGTH,
        kp.getPersistedPrivateKey().length);
    assertEquals(PQScheme.FN_DSA_512, kp.getScheme());
  }

  @Test
  void seedIsDeterministic() {
    byte[] seed = seed();
    FNDSA512 a = new FNDSA512(seed);
    FNDSA512 b = new FNDSA512(seed);
    assertArrayEquals(a.getPrivateKey(), b.getPrivateKey());
    assertArrayEquals(a.getPublicKey(), b.getPublicKey());
  }

  @Test
  void rejectsBadSeedLength() {
    assertThrows(IllegalArgumentException.class, () -> new FNDSA512(new byte[10]));
  }

  @Test
  void signVerifyRoundTrip() {
    FNDSA512 kp = new FNDSA512(seed());
    byte[] msg = "hello pq".getBytes(StandardCharsets.UTF_8);
    byte[] sig = kp.sign(msg);
    assertTrue(sig.length >= FNDSA512.SIGNATURE_MIN_LENGTH
        && sig.length <= FNDSA512.SIGNATURE_MAX_LENGTH);
    assertEquals(FNDSA512.SIGNATURE_HEADER, sig[0]);
    assertTrue(kp.verify(msg, sig));
  }

  @Test
  void verifyFailsOnTamperedMessage() {
    FNDSA512 kp = new FNDSA512(seed());
    byte[] msg = "original".getBytes(StandardCharsets.UTF_8);
    byte[] sig = kp.sign(msg);
    assertFalse(kp.verify("tampered".getBytes(StandardCharsets.UTF_8), sig));
  }

  @Test
  void verifyRejectsNonCanonicalHeader() {
    FNDSA512 kp = new FNDSA512(seed());
    byte[] msg = "m".getBytes(StandardCharsets.UTF_8);
    byte[] sig = kp.sign(msg);
    sig[0] = 0x49; // padded encoding header — must be rejected
    assertFalse(kp.verify(msg, sig));
  }

  @Test
  void extendedFormRoundTrips() {
    FNDSA512 kp = new FNDSA512(seed());
    byte[] extended = kp.getPrivateKeyWithPublicKey();
    assertEquals(FNDSA512.PRIVATE_KEY_WITH_PUBLIC_KEY_LENGTH, extended.length);
    FNDSA512 restored = FNDSA512.fromPrivateKeyWithPublicKey(extended);
    assertArrayEquals(kp.getPublicKey(), restored.getPublicKey());
    byte[] msg = "x".getBytes(StandardCharsets.UTF_8);
    assertTrue(kp.verify(msg, restored.sign(msg)));
  }

  @Test
  void derivePublicKeyFromBareFormUnsupported() {
    FNDSA512 kp = new FNDSA512(seed());
    assertThrows(UnsupportedOperationException.class,
        () -> FNDSA512.derivePublicKey(kp.getPrivateKey()));
    assertArrayEquals(kp.getPublicKey(),
        FNDSA512.derivePublicKey(kp.getPrivateKeyWithPublicKey()));
  }

  @Test
  void addressIsCanonical() {
    FNDSA512 kp = new FNDSA512(seed());
    byte[] addr = kp.getAddress();
    assertEquals(21, addr.length);
    assertEquals(0x41, addr[0] & 0xff);
  }
}
