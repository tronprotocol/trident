package org.tron.trident.abi.datatypes.generated;

import java.math.BigInteger;
import org.tron.trident.abi.datatypes.Int;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.tron.trident.codegen.AbiTypesGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 */
public class Int72 extends Int {
    public static final Int72 DEFAULT = new Int72(BigInteger.ZERO);

    public Int72(BigInteger value) {
        super(72, value);
    }

    public Int72(long value) {
        this(BigInteger.valueOf(value));
    }
}
