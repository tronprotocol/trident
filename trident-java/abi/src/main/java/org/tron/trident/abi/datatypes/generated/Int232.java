package org.tron.trident.abi.datatypes.generated;

import java.math.BigInteger;
import org.tron.trident.abi.datatypes.Int;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.tron.trident.codegen.AbiTypesGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 */
public class Int232 extends Int {
    public static final Int232 DEFAULT = new Int232(BigInteger.ZERO);

    public Int232(BigInteger value) {
        super(232, value);
    }

    public Int232(long value) {
        this(BigInteger.valueOf(value));
    }
}
