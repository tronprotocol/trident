package org.tron.trident.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.bouncycastle.util.encoders.Hex;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Base58CheckTest {

    @Test
    public void testAddressConverting() {
        byte[] rawAddr = Hex.decode("4159d3ad9d126e153b9564417d3a05cf51c1964edf");
        Assertions.assertArrayEquals(rawAddr, Base58Check.base58ToBytes("TJAAinkKN2h9KxtBZXw6SyL7HwCQXnzFsE"));
        Assertions.assertEquals(Base58Check.bytesToBase58(rawAddr), "TJAAinkKN2h9KxtBZXw6SyL7HwCQXnzFsE");
    }
}
