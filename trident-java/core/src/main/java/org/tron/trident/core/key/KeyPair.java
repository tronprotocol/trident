package org.tron.trident.core.key;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.crypto.SECP256K1;
import org.tron.trident.crypto.tuwenitypes.Bytes32;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.utils.Base58Check;

/**
 * This is a wrapper class for the raw SECP256K1 keypair.
 */

public class KeyPair {
    private SECP256K1.KeyPair rawPair;

    public KeyPair(SECP256K1.KeyPair keyPair) {
        this.rawPair = keyPair;
    }

    public KeyPair(String hexPrivateKey) {
        this.rawPair = SECP256K1.KeyPair.create(SECP256K1.PrivateKey.create(Bytes32.fromHexString(hexPrivateKey)));
    }

    public SECP256K1.KeyPair getRawPair() {
        return rawPair;
    }

    public static KeyPair generate() {
        return new KeyPair(SECP256K1.KeyPair.generate());
    }

    public String toPrivateKey() {
        return Hex.toHexString(rawPair.getPrivateKey().getEncoded());
    }

    public String toPublicKey() {
        return Hex.toHexString(rawPair.getPublicKey().getEncoded());
    }

    public String toBase58CheckAddress() {
        SECP256K1.PublicKey pubKey = rawPair.getPublicKey();
        
        return publicKeyToBase58CheckAddress(pubKey);
    }

    public String toHexAddress() {
        SECP256K1.PublicKey pubKey = rawPair.getPublicKey();
        
        return publicKeyToHexAddress(pubKey);
    }

    public static byte[] publicKeyToAddress(final SECP256K1.PublicKey pubKey) {
        Keccak.Digest256 digest = new Keccak.Digest256();
        digest.update(pubKey.getEncoded(), 0, 64);
        byte[] raw = digest.digest();
        byte[] rawAddr = new byte[21];
        rawAddr[0] = 0x41;
        System.arraycopy(raw, 12, rawAddr, 1, 20);

        return rawAddr;
    }

    public static String publicKeyToBase58CheckAddress(final SECP256K1.PublicKey pubKey) {
        byte[] rawAddr = publicKeyToAddress(pubKey);

        return Base58Check.bytesToBase58(rawAddr);
    }

    public static String publicKeyToHexAddress(final SECP256K1.PublicKey pubKey) {
        byte[] rawAddr = publicKeyToAddress(pubKey);

        return Hex.toHexString(rawAddr);
    }

    /**
     * Return a signature message in byte[]
     * @param txnExt the transaction waiting for signature
     * @param keyPair 
     * @return the signature message in byte[]
     */
    public static byte[] signTransaction(TransactionExtention txnExt, KeyPair keyPair) {
        SECP256K1.KeyPair kp = keyPair.getRawPair();
        SECP256K1.Signature signature = SECP256K1.sign(Bytes32.wrap(txnExt.getTxid().toByteArray()), kp);
        
        return signature.encodedBytes().toArray();
    }

    /**
     * Return a signature message in byte[]
     * @param txn the transaction waiting for signature
     * @param keyPair 
     * @return the signature message in byte[]
     */
    public static byte[] signTransaction(Transaction txn, KeyPair keyPair) {
        SHA256.Digest digest = new SHA256.Digest();
        digest.update(txn.getRawData().toByteArray());
        byte[] txid = digest.digest();

        SECP256K1.KeyPair kp = keyPair.getRawPair();
        SECP256K1.Signature sig = SECP256K1.sign(Bytes32.wrap(txid), kp);

        return sig.encodedBytes().toArray();
    }
}