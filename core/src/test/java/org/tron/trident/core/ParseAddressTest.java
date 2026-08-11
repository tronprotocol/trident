package org.tron.trident.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.utils.ByteArray;
import org.tron.trident.utils.Base58Check;

/**
 * Unit tests for ApiWrapper.parseAddress address validation
 */
class ParseAddressTest {

  private static final String VALID_BASE58 = KeyPair.generate().toBase58CheckAddress();
  private static final String VALID_HEX =
      ByteArray.toHexString(Base58Check.base58ToBytes(VALID_BASE58));

  @Test
  void testParseValidAddresses() {
    ByteString fromHex = ApiWrapper.parseAddress(VALID_HEX);
    assertEquals(21, fromHex.size());
    assertEquals(VALID_HEX, ByteArray.toHexString(fromHex.toByteArray()));

    // "0x" prefix is accepted, and base58 decodes to the same bytes
    assertEquals(fromHex, ApiWrapper.parseAddress("0x" + VALID_HEX));
    assertEquals(fromHex, ApiWrapper.parseAddress(VALID_BASE58));
  }

  @Test
  void testParseRejectsInvalidLengthOrPrefix() {
    // too short: a bare prefix byte is not an address
    assertThrows(IllegalArgumentException.class, () -> ApiWrapper.parseAddress("41"));
    assertThrows(IllegalArgumentException.class, () -> ApiWrapper.parseAddress("0x41"));

    // 22 bytes: one trailing byte too many
    assertThrows(IllegalArgumentException.class,
        () -> ApiWrapper.parseAddress(VALID_HEX + "00"));

    // 20 bytes: 0x41 prefix missing
    assertThrows(IllegalArgumentException.class,
        () -> ApiWrapper.parseAddress(VALID_HEX.substring(2)));

    // 21 bytes but wrong prefix byte
    assertThrows(IllegalArgumentException.class,
        () -> ApiWrapper.parseAddress("42" + VALID_HEX.substring(2)));

    assertThrows(NullPointerException.class, () -> ApiWrapper.parseAddress(null));

    // undecodable input surfaces as IllegalArgumentException per the javadoc,
    // not as Bouncy Castle's DecoderException (an IllegalStateException)
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
        () -> ApiWrapper.parseAddress("zz" + VALID_HEX.substring(2)));
    assertTrue(e.getMessage().contains("invalid address"));

    // oversized input is rejected before decoding, echoing only the length
    StringBuilder oversized = new StringBuilder(VALID_HEX);
    for (int i = 0; i < 1000; i++) {
      oversized.append("00");
    }
    IllegalArgumentException tooLong = assertThrows(IllegalArgumentException.class,
        () -> ApiWrapper.parseAddress(oversized.toString()));
    assertTrue(tooLong.getMessage().contains("invalid address length"));
  }

  @Test
  void testParseEmptyMeansUnsetOptionalField() {
    // "" maps to an unset optional protobuf field (freeze receiver, constant-call owner)
    assertEquals(ByteString.EMPTY, ApiWrapper.parseAddress(""));
  }
}
