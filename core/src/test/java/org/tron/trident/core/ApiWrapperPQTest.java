package org.tron.trident.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.tron.trident.core.crypto.pqc.PQSchemeRegistry;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.key.PQKeyPair;
import org.tron.trident.proto.Chain.PQAuthSig;
import org.tron.trident.proto.Chain.PQScheme;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Contract.CreateSmartContract;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.utils.Base58Check;

/**
 * Exercises the real {@link ApiWrapper#signTransactionPQ} entry points offline.
 * gRPC channels are created lazily, and PQ signing is pure local computation, so
 * no network is needed: we build an {@link ApiWrapper} against a dummy endpoint
 * and never issue an RPC.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiWrapperPQTest {

  private static ApiWrapper client;

  @BeforeAll
  static void setUp() {
    // Dummy endpoints — no RPC is performed, channels stay idle.
    client = new ApiWrapper("localhost:1", "localhost:1",
        KeyPair.generate().toPrivateKey());
  }

  @AfterAll
  static void tearDown() {
    if (client != null) {
      client.close();
    }
  }

  private static Transaction sampleTransaction() {
    return Transaction.newBuilder()
        .setRawData(Transaction.raw.newBuilder().setTimestamp(1234567890L).build())
        .build();
  }

  @Test
  void signTransactionPQByTransactionAppendsVerifiableEntry() {
    for (PQScheme scheme : new PQScheme[] {PQScheme.FN_DSA_512, PQScheme.ML_DSA_44}) {
      PQKeyPair kp = PQKeyPair.generate(scheme);
      Transaction txn = sampleTransaction();

      Transaction signed = client.signTransactionPQ(txn, kp);

      assertEquals(1, signed.getPqAuthSigCount());
      PQAuthSig entry = signed.getPqAuthSig(0);
      assertEquals(scheme, entry.getScheme());

      byte[] txId = ApiWrapper.calculateTransactionHash(txn);
      assertTrue(PQSchemeRegistry.verify(scheme,
          entry.getPublicKey().toByteArray(), txId,
          entry.getSignature().toByteArray()));
      // raw_data is untouched; only the PQ witness is appended.
      assertEquals(txn.getRawData(), signed.getRawData());
      assertEquals(0, signed.getSignatureCount());
    }
  }

  @Test
  void signTransactionPQByExtentionUsesProvidedTxid() {
    PQKeyPair kp = PQKeyPair.generate(PQScheme.FN_DSA_512);
    Transaction txn = sampleTransaction();
    byte[] txId = ApiWrapper.calculateTransactionHash(txn);

    TransactionExtention txnExt = TransactionExtention.newBuilder()
        .setTransaction(txn)
        .setTxid(ByteString.copyFrom(txId))
        .build();

    Transaction signed = client.signTransactionPQ(txnExt, kp);

    assertEquals(1, signed.getPqAuthSigCount());
    PQAuthSig entry = signed.getPqAuthSig(0);
    assertTrue(PQSchemeRegistry.verify(PQScheme.FN_DSA_512,
        entry.getPublicKey().toByteArray(), txId,
        entry.getSignature().toByteArray()));
  }

  @Test
  void ecdsaAndPqSignaturesCoexist() {
    // A transaction can carry both an ECDSA signature and a PQ witness.
    Transaction txn = sampleTransaction();
    Transaction ecdsaSigned = client.signTransaction(txn);
    Transaction dualSigned =
        client.signTransactionPQ(ecdsaSigned, PQKeyPair.generate(PQScheme.ML_DSA_44));

    assertEquals(1, dualSigned.getSignatureCount());
    assertEquals(1, dualSigned.getPqAuthSigCount());
  }

  @Test
  void createSmartContractHonorsExplicitPqOwner() throws Exception {
    // The deployContract(ownerAddress, ...) overload routes through
    // createSmartContract with the given owner, independent of the ECDSA keyPair.
    // Verify the owner placed on the contract is the PQ address, not the ECDSA one.
    PQKeyPair pq = PQKeyPair.generate(PQScheme.FN_DSA_512);
    String pqOwner = pq.toBase58CheckAddress();

    CreateSmartContract csc = client.createSmartContract(
        "pqOwned", pqOwner, "[]", "60806040", 0L, 100L, 1_000_000L, 0L, "");

    byte[] ownerOnContract = csc.getOwnerAddress().toByteArray();
    assertArrayEquals(pq.toBytesAddress(), ownerOnContract);
    // Sanity: the PQ owner differs from the client's ECDSA address.
    assertNotEquals(client.keyPair.toBase58CheckAddress(), pqOwner);
    assertEquals(pqOwner, Base58Check.bytesToBase58(ownerOnContract));
  }

  @Test
  void noArgOverloadUsesBoundKeypair() {
    // Use a separate client so the bound key does not leak into other tests.
    ApiWrapper bound = new ApiWrapper("localhost:1", "localhost:1",
        KeyPair.generate().toPrivateKey());
    try {
      // Unbound: the no-arg overload must fail clearly.
      assertThrows(IllegalStateException.class,
          () -> bound.signTransactionPQ(sampleTransaction()));

      PQKeyPair kp = PQKeyPair.generate(PQScheme.FN_DSA_512);
      assertSame(bound, bound.setPQKeyPair(kp));
      assertSame(kp, bound.getPQKeyPair());

      Transaction txn = sampleTransaction();
      Transaction signed = bound.signTransactionPQ(txn);

      assertEquals(1, signed.getPqAuthSigCount());
      PQAuthSig entry = signed.getPqAuthSig(0);
      assertEquals(PQScheme.FN_DSA_512, entry.getScheme());
      byte[] txId = ApiWrapper.calculateTransactionHash(txn);
      assertTrue(PQSchemeRegistry.verify(PQScheme.FN_DSA_512,
          entry.getPublicKey().toByteArray(), txId,
          entry.getSignature().toByteArray()));
    } finally {
      bound.close();
    }
  }
}
