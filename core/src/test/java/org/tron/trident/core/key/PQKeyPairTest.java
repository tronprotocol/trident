package org.tron.trident.core.key;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.ByteString;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import org.junit.jupiter.api.Test;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.crypto.pqc.PQSchemeRegistry;
import org.tron.trident.proto.Chain.PQAuthSig;
import org.tron.trident.proto.Chain.PQScheme;
import org.tron.trident.proto.Chain.Transaction;

class PQKeyPairTest {

  private static byte[] seed(PQScheme scheme) {
    byte[] s = new byte[PQSchemeRegistry.getSeedLength(scheme)];
    new SecureRandom().nextBytes(s);
    return s;
  }

  @Test
  void generateDefaultsToFalcon() {
    PQKeyPair kp = PQKeyPair.generate();
    assertEquals(PQScheme.FN_DSA_512, kp.getScheme());
  }

  @Test
  void addressFormatsConsistent() {
    PQKeyPair kp = PQKeyPair.generate(PQScheme.ML_DSA_44);
    byte[] raw = kp.toBytesAddress();
    assertEquals(21, raw.length);
    assertEquals(0x41, raw[0] & 0xff);
    // hex address is the lowercase hex of the 21-byte raw address
    assertEquals(42, kp.toHexAddress().length());
    assertTrue(kp.toBase58CheckAddress().startsWith("T"));
  }

  @Test
  void fromSeedRoundTrip() {
    byte[] seed = seed(PQScheme.FN_DSA_512);
    PQKeyPair a = PQKeyPair.fromSeed(PQScheme.FN_DSA_512, seed);
    PQKeyPair b = PQKeyPair.fromSeed(PQScheme.FN_DSA_512, seed);
    assertArrayEquals(a.getPublicKey(), b.getPublicKey());
    assertArrayEquals(a.toBytesAddress(), b.toBytesAddress());
  }

  @Test
  void fromPersistedPrivateKeyRecoversKeypair() {
    PQKeyPair original = PQKeyPair.generate(PQScheme.FN_DSA_512);
    PQKeyPair restored = PQKeyPair.fromPersistedPrivateKey(
        PQScheme.FN_DSA_512, original.getPersistedPrivateKey());
    assertArrayEquals(original.getPublicKey(), restored.getPublicKey());
    assertArrayEquals(original.toBytesAddress(), restored.toBytesAddress());
  }

  @Test
  void signTransactionAppendsValidPqAuthSig() {
    for (PQScheme scheme : new PQScheme[] {PQScheme.FN_DSA_512, PQScheme.ML_DSA_44}) {
      PQKeyPair kp = PQKeyPair.generate(scheme);
      Transaction txn = Transaction.newBuilder()
          .setRawData(Transaction.raw.newBuilder()
              .setTimestamp(1234567890L)
              .build())
          .build();

      byte[] txId = ApiWrapper.calculateTransactionHash(txn);
      byte[] sig = PQKeyPair.signTransaction(txId, kp);
      Transaction signed = txn.toBuilder()
          .addPqAuthSig(PQAuthSig.newBuilder()
              .setScheme(scheme)
              .setPublicKey(ByteString.copyFrom(kp.getPublicKey()))
              .setSignature(ByteString.copyFrom(sig))
              .build())
          .build();

      assertEquals(1, signed.getPqAuthSigCount());
      PQAuthSig entry = signed.getPqAuthSig(0);
      assertEquals(scheme, entry.getScheme());
      assertArrayEquals(kp.getPublicKey(), entry.getPublicKey().toByteArray());
      // signature verifies over SHA256(raw_data), i.e. the same txId
      assertTrue(PQSchemeRegistry.verify(
          scheme, entry.getPublicKey().toByteArray(), txId,
          entry.getSignature().toByteArray()));
    }
  }

  @Test
  void signMessageVerifies() {
    PQKeyPair kp = PQKeyPair.generate(PQScheme.ML_DSA_44);
    byte[] msg = "trident pq".getBytes(StandardCharsets.UTF_8);
    byte[] sig = PQKeyPair.signTransaction(msg, kp);
    assertTrue(kp.getPQSignature().verify(msg, sig));
  }
}
