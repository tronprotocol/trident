package org.tron.trident.core.key;

import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.core.crypto.pqc.PQSchemeRegistry;
import org.tron.trident.core.crypto.pqc.PQSignature;
import org.tron.trident.proto.Chain.PQScheme;
import org.tron.trident.utils.Base58Check;

/**
 * Wrapper class for a post-quantum keypair, analogous to {@link KeyPair} for
 * ECDSA secp256k1. Backed by a {@link PQSignature} bound to a concrete
 * {@link PQScheme} (FN-DSA-512 or ML-DSA-44). Used to sign transactions whose
 * authentication is carried in {@code Transaction.pq_auth_sig} rather than the
 * legacy {@code Transaction.signature} field.
 */
public class PQKeyPair {

  /** Default scheme when callers do not specify one. */
  public static final PQScheme DEFAULT_SCHEME = PQScheme.FN_DSA_512;

  private final PQSignature signer;

  public PQKeyPair(PQSignature signer) {
    if (signer == null) {
      throw new IllegalArgumentException("signer must not be null");
    }
    this.signer = signer;
  }

  /** Generate a fresh keypair for the default scheme (FN-DSA-512). */
  public static PQKeyPair generate() {
    return generate(DEFAULT_SCHEME);
  }

  /** Generate a fresh keypair for the given scheme using a system-seeded RNG. */
  public static PQKeyPair generate(PQScheme scheme) {
    byte[] seed = new byte[PQSchemeRegistry.getSeedLength(scheme)];
    new java.security.SecureRandom().nextBytes(seed);
    return fromSeed(scheme, seed);
  }

  /** Deterministically derive a keypair from a scheme-sized seed. */
  public static PQKeyPair fromSeed(PQScheme scheme, byte[] seed) {
    return new PQKeyPair(PQSchemeRegistry.fromSeed(scheme, seed));
  }

  /**
   * Rebuild a keypair from the persisted private-key form (see
   * {@link PQSignature#getPersistedPrivateKey()}): the extended
   * {@code f ‖ g ‖ F ‖ h} form for FN-DSA-512, or the encoded private key for
   * ML-DSA-44.
   */
  public static PQKeyPair fromPersistedPrivateKey(PQScheme scheme, byte[] persistedPrivateKey) {
    return new PQKeyPair(PQSchemeRegistry.fromPersistedPrivateKey(scheme, persistedPrivateKey));
  }

  /** Build a keypair from already-derived private and public key bytes. */
  public static PQKeyPair fromKeypair(PQScheme scheme, byte[] privateKey, byte[] publicKey) {
    return new PQKeyPair(PQSchemeRegistry.fromKeypair(scheme, privateKey, publicKey));
  }

  public PQScheme getScheme() {
    return signer.getScheme();
  }

  public PQSignature getPQSignature() {
    return signer;
  }

  public byte[] getPublicKey() {
    return signer.getPublicKey();
  }

  /** Returns the keystore-persistable private key form for this scheme. */
  public byte[] getPersistedPrivateKey() {
    return signer.getPersistedPrivateKey();
  }

  /** The raw 21-byte TRON address ({@code 0x41 ‖ deriveHash(pubkey)[12..32]}). */
  public byte[] toBytesAddress() {
    return signer.getAddress();
  }

  public String toBase58CheckAddress() {
    return Base58Check.bytesToBase58(signer.getAddress());
  }

  public String toHexAddress() {
    return Hex.toHexString(signer.getAddress());
  }

  public String toPublicKey() {
    return Hex.toHexString(signer.getPublicKey());
  }

  /**
   * Return a post-quantum signature over {@code txid} in byte[].
   *
   * @param txid the transaction hash waiting for signature
   * @return the signature message in byte[]
   */
  public static byte[] signTransaction(byte[] txid, PQKeyPair keyPair) {
    return keyPair.getPQSignature().sign(txid);
  }
}
