package org.tron.trident.core.transaction;

import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.crypto.SECP256K1;
import org.tron.trident.crypto.tuwenitypes.Bytes;
import org.tron.trident.crypto.tuwenitypes.Bytes32;
import org.tron.trident.proto.Chain.Transaction;

import java.util.Arrays;

public class SignatureValidator {
    
    /**
     * Verify if a transction contains a valid signature.
     * @param txid the transaction hash
     * @param signature the signature message corresponding to the transaction hash
     * @param owner the owner of the transaction
     * @return true if the signature is valid
     */
    public static boolean verify(byte[] txid, byte[] signature, byte[] owner) {
        SECP256K1.Signature sig = SECP256K1.Signature.decode(Bytes.wrap(signature));
        //decode a public key from the signature
        SECP256K1.PublicKey pubKey = SECP256K1.PublicKey.recoverFromSignature(Bytes32.wrap(txid), sig).get();

        final byte[] addressFromPubKey = KeyPair.publicKeyToAddress(pubKey);

        return Arrays.equals(addressFromPubKey, owner);
    }

    public static boolean verify(String txid, String signature, String owner) {
        byte[] txidBytes = Hex.decode(txid);
        byte[] sig = Hex.decode(signature);
        byte[] ownerBytes = ApiWrapper.parseAddress(owner).toByteArray();

        return verify(txidBytes, sig, ownerBytes);
    }
}
