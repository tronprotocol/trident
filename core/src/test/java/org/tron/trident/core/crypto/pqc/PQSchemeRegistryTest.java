package org.tron.trident.core.crypto.pqc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import org.junit.jupiter.api.Test;
import org.tron.trident.crypto.Hash;
import org.tron.trident.proto.Chain.PQScheme;

class PQSchemeRegistryTest {

  @Test
  void unknownSchemeNotRegistered() {
    assertFalse(PQSchemeRegistry.contains(PQScheme.UNKNOWN_PQ_SCHEME));
    assertFalse(PQSchemeRegistry.contains(null));
    assertTrue(PQSchemeRegistry.contains(PQScheme.FN_DSA_512));
    assertTrue(PQSchemeRegistry.contains(PQScheme.ML_DSA_44));
  }

  @Test
  void requireRejectsUnregistered() {
    assertThrows(IllegalArgumentException.class,
        () -> PQSchemeRegistry.getPublicKeyLength(PQScheme.UNKNOWN_PQ_SCHEME));
  }

  @Test
  void lengthsMatchSchemes() {
    assertEquals(FNDSA512.PUBLIC_KEY_LENGTH,
        PQSchemeRegistry.getPublicKeyLength(PQScheme.FN_DSA_512));
    assertEquals(MLDSA44.SIGNATURE_LENGTH,
        PQSchemeRegistry.getSignatureLength(PQScheme.ML_DSA_44));
    assertEquals(FNDSA512.SEED_LENGTH,
        PQSchemeRegistry.getSeedLength(PQScheme.FN_DSA_512));
  }

  @Test
  void signatureLengthPredicate() {
    assertTrue(PQSchemeRegistry.isValidSignatureLength(
        PQScheme.FN_DSA_512, FNDSA512.SIGNATURE_MIN_LENGTH));
    assertTrue(PQSchemeRegistry.isValidSignatureLength(
        PQScheme.FN_DSA_512, FNDSA512.SIGNATURE_MAX_LENGTH));
    assertFalse(PQSchemeRegistry.isValidSignatureLength(
        PQScheme.FN_DSA_512, FNDSA512.SIGNATURE_MAX_LENGTH + 1));
    assertTrue(PQSchemeRegistry.isValidSignatureLength(
        PQScheme.ML_DSA_44, MLDSA44.SIGNATURE_LENGTH));
    assertFalse(PQSchemeRegistry.isValidSignatureLength(
        PQScheme.ML_DSA_44, MLDSA44.SIGNATURE_LENGTH - 1));
  }

  @Test
  void dispatchSignVerifyFalcon() {
    byte[] seed = new byte[FNDSA512.SEED_LENGTH];
    new SecureRandom().nextBytes(seed);
    PQSignature signer = PQSchemeRegistry.fromSeed(PQScheme.FN_DSA_512, seed);
    byte[] msg = "dispatch".getBytes(StandardCharsets.UTF_8);
    byte[] sig = PQSchemeRegistry.sign(
        PQScheme.FN_DSA_512, signer.getPrivateKey(), msg);
    assertTrue(PQSchemeRegistry.verify(
        PQScheme.FN_DSA_512, signer.getPublicKey(), msg, sig));
  }

  @Test
  void dispatchSignVerifyMlDsa() {
    byte[] seed = new byte[MLDSA44.SEED_LENGTH];
    new SecureRandom().nextBytes(seed);
    PQSignature signer = PQSchemeRegistry.fromSeed(PQScheme.ML_DSA_44, seed);
    byte[] msg = "dispatch".getBytes(StandardCharsets.UTF_8);
    byte[] sig = PQSchemeRegistry.sign(
        PQScheme.ML_DSA_44, signer.getPrivateKey(), msg);
    assertTrue(PQSchemeRegistry.verify(
        PQScheme.ML_DSA_44, signer.getPublicKey(), msg, sig));
  }

  @Test
  void persistedRoundTripBothSchemes() {
    byte[] fSeed = new byte[FNDSA512.SEED_LENGTH];
    new SecureRandom().nextBytes(fSeed);
    PQSignature falcon = PQSchemeRegistry.fromSeed(PQScheme.FN_DSA_512, fSeed);
    PQSignature falconRestored = PQSchemeRegistry.fromPersistedPrivateKey(
        PQScheme.FN_DSA_512, falcon.getPersistedPrivateKey());
    assertArrayEquals(falcon.getPublicKey(), falconRestored.getPublicKey());

    byte[] mSeed = new byte[MLDSA44.SEED_LENGTH];
    new SecureRandom().nextBytes(mSeed);
    PQSignature mldsa = PQSchemeRegistry.fromSeed(PQScheme.ML_DSA_44, mSeed);
    PQSignature mldsaRestored = PQSchemeRegistry.fromPersistedPrivateKey(
        PQScheme.ML_DSA_44, mldsa.getPersistedPrivateKey());
    assertArrayEquals(mldsa.getPublicKey(), mldsaRestored.getPublicKey());
  }

  @Test
  void computeAddressMatchesEcdsaShape() {
    byte[] seed = new byte[FNDSA512.SEED_LENGTH];
    new SecureRandom().nextBytes(seed);
    PQSignature signer = PQSchemeRegistry.fromSeed(PQScheme.FN_DSA_512, seed);
    byte[] pub = signer.getPublicKey();
    byte[] addr = PQSchemeRegistry.computeAddress(PQScheme.FN_DSA_512, pub);
    // 0x41 || Keccak-256(pubkey)[12..32]
    byte[] h = Hash.sha3(pub);
    byte[] expected = new byte[21];
    expected[0] = 0x41;
    System.arraycopy(h, h.length - 20, expected, 1, 20);
    assertArrayEquals(expected, addr);
    assertArrayEquals(addr, signer.getAddress());
  }

  @Test
  void deriveHashRejectsWrongPublicKeyLength() {
    assertThrows(IllegalArgumentException.class,
        () -> PQSchemeRegistry.deriveHash(PQScheme.FN_DSA_512, new byte[10]));
  }
}
