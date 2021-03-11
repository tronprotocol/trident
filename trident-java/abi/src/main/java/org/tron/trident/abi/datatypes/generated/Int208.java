package org.tron.trident.abi.datatypes.generated;

import java.math.BigInteger;
import org.tron.trident.abi.datatypes.Int;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.tron.trident.codegen.AbiTypesGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 */
public class Int208 extends Int {
    public static final Int208 DEFAULT = new Int208(BigInteger.ZERO);

    public Int208(BigInteger value) {
        super(208, value);
    }

    public Int208(long value) {
        this(BigInteger.valueOf(value));
    }
}
