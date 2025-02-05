package org.tron.trident.core.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.tron.trident.core.ApiWrapper.generateAddress;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.exceptions.ContractCreateException;
import org.tron.trident.crypto.Hash;

public class UtilsTest {

  @Test
  public void testAddressValid() {
    // test null address
    assertFalse(Utils.addressValid(null));

    // test empty address
    assertFalse(Utils.addressValid(new byte[0]));

    // test address with incorrect length
    assertFalse(Utils.addressValid(new byte[20]));

    // test valid address
    byte[] validAddress = new byte[21];
    validAddress[0] = Utils.ADD_PRE_FIX_BYTE_MAINNET;
    assertTrue(Utils.addressValid(validAddress));

    // test address with invalid prefix
    byte[] invalidPrefixAddress = new byte[21];
    invalidPrefixAddress[0] = 0x00;
    assertFalse(Utils.addressValid(invalidPrefixAddress));
  }

  @Test
  public void testEncode58CheckAndDecode() {
    byte[] input = new byte[21];
    input[0] = Utils.ADD_PRE_FIX_BYTE_MAINNET;
    for (int i = 1; i < input.length; i++) {
      input[i] = (byte) i;
    }

    // test encode and decode
    String encoded = Utils.encode58Check(input);
    byte[] decoded = Utils.decodeFromBase58Check(encoded);
    assertArrayEquals(input, decoded);

    // test empty address
    assertNull(Utils.decodeFromBase58Check(""));

    // test invalid address
    assertNull(Utils.decodeFromBase58Check("invalid_address"));
  }

  @Test
  public void testEncodeParameter() throws ContractCreateException {
    List<org.tron.trident.abi.datatypes.Type<?>> params = new ArrayList<>();
    String testAddress = generateAddress().toBase58CheckAddress();
    params.add(new Address(testAddress));
    params.add(new Uint256(256));

    ByteString result = Utils.encodeParameter(params);
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  public void testReplaceLibraryAddress() {
    // test with v5 compiler version

    String libraryName = "TestLibrary";
    String libraryNameKeccak256 =
        ByteArray.toHexString(
                Hash.sha3(libraryName.getBytes()))
            .substring(0, 34);
    String code = "__$" + libraryNameKeccak256 + "$__";
    String testAddress = generateAddress().toBase58CheckAddress();
    String libraryAddressPair =  libraryName + ":" + testAddress;

    try {
      byte[] result = Utils.replaceLibraryAddress(code, libraryAddressPair, "v5");
      assertNotNull(result);
      assertTrue(result.length > 0);
    } catch (Exception e) {
      //System.out.println(e.getMessage());
      fail("Should not throw exception");
    }

    // test with invalid libraryAddressPair format
    try {
      Utils.replaceLibraryAddress(code, "invalid_format", "v5");
      fail("Should throw exception");
    } catch (Exception e) {
      assertEquals("libraryAddress delimit by ':'", e.getMessage());
    }

    // test with invalid compiler version
    try {
      Utils.replaceLibraryAddress(code, libraryAddressPair, "invalid_version");
      fail("Should throw exception");
    } catch (Exception e) {
      assertEquals("unknown compiler version.", e.getMessage());
    }
  }
}
