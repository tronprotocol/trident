package org.tron.trident.abi.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.tron.trident.abi.datatypes.generated.StaticArray4;
import org.tron.trident.abi.datatypes.generated.Uint256;

public class StaticStructTest {
  @Test
  public void testStaticStruct() {
    Address address1 = Address.DEFAULT;
    Address address2 = Address.DEFAULT;
    StaticArray4<Uint256> array4 =
        new StaticArray4<>(
            Uint256.class,
            Uint256.DEFAULT,
            Uint256.DEFAULT,
            Uint256.DEFAULT,
            Uint256.DEFAULT);
    StaticStruct struct = new StaticStruct(address1, address2, array4);

    // (address,address,uint256[4])
    String expected =
        "("
            + address1.getTypeAsString()
            + ","
            + address2.getTypeAsString()
            + ","
            + array4.getTypeAsString()
            + ")";
    assertEquals(expected, struct.getTypeAsString());
  }

}
