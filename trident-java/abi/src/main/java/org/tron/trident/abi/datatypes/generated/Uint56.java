package org.tron.trident.abi.datatypes.generated;

import java.math.BigInteger;
import org.tron.trident.abi.datatypes.Uint;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.tron.trident.codegen.AbiTypesGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 */
public class Uint56 extends Uint {
    public static final Uint56 DEFAULT = new Uint56(BigInteger.ZERO);

    public Uint56(BigInteger value) {
        super(56, value);
    }

    public Uint56(long value) {
        this(BigInteger.valueOf(value));
    }
}
