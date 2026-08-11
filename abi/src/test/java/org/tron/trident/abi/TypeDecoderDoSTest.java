/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.tron.trident.abi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.tron.trident.abi.datatypes.DynamicArray;
import org.tron.trident.abi.datatypes.DynamicBytes;
import org.tron.trident.abi.datatypes.DynamicStruct;
import org.tron.trident.abi.datatypes.StaticStruct;
import org.tron.trident.abi.datatypes.Utf8String;
import org.tron.trident.abi.datatypes.generated.Int256;
import org.tron.trident.abi.datatypes.generated.Uint256;

/**
 * Regression tests for decoder hardening against malicious ABI inputs.
 */
public class TypeDecoderDoSTest {

  /** A 32-byte slot whose value is 2^255 — far above Integer.MAX_VALUE. */
  private static final String HUGE_UINT_SLOT =
      "8000000000000000000000000000000000000000000000000000000000000000";

  @Test
  public void decodeUintAsInt_rejectsValueExceedingIntMax() {
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeUintAsInt(HUGE_UINT_SLOT, 0));
  }

  @Test
  public void decodeDynamicArray_rejectsHugeLength() {
    // Length prefix = 2^255. Without the bound, intValue() truncates to
    // a negative int and new ArrayList<>(negative) throws — or worse, a
    // value just above Int.MAX would OOM the JVM on pre-allocation.
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicArray(
            HUGE_UINT_SLOT, 0, new TypeReference<DynamicArray<Uint256>>() {}));
  }

  @Test
  public void decodeDynamicArray_rejectsLengthExceedingRemainingInput() {
    // Length = 1000 fits in int (passes the bitLength check) but cannot
    // possibly fit in this 1-slot input. The decodeArrayElements cap
    // rejects before new ArrayList<>(1000) allocates.
    String malicious =
        "00000000000000000000000000000000000000000000000000000000000003e8"; // length=1000
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicArray(
            malicious, 0, new TypeReference<DynamicArray<Uint256>>() {}));
  }

  @Test
  public void decodeDynamicBytes_rejectsHugePayloadLength() {
    // Length prefix = 2^255 — without the bound, substring tries to read
    // 2^255 hex chars and throws SIOOBE.
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicBytes(HUGE_UINT_SLOT, 0));
  }

  @Test
  public void decodeUtf8String_rejectsHugePayloadLength() {
    // Inherits the same protection via decodeDynamicBytes.
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeUtf8String(HUGE_UINT_SLOT, 0));
  }

  @Test
  public void decodeDynamicArray_acceptsLegitimateArray() {
    String legit =
        "0000000000000000000000000000000000000000000000000000000000000002"
            + "0000000000000000000000000000000000000000000000000000000000000001"
            + "0000000000000000000000000000000000000000000000000000000000000002";
    DynamicArray<Uint256> result = TypeDecoder.decodeDynamicArray(
        legit, 0, new TypeReference<DynamicArray<Uint256>>() {});
    assertEquals(2, result.getValue().size());
  }

  @Test
  public void decodeDynamicBytes_acceptsLegitimateBytes() {
    String legit =
        "0000000000000000000000000000000000000000000000000000000000000004"
            + "deadbeef00000000000000000000000000000000000000000000000000000000";
    DynamicBytes result = TypeDecoder.decodeDynamicBytes(legit, 0);
    assertEquals(4, result.getValue().length);
    assertEquals((byte) 0xde, result.getValue()[0]);
    assertEquals((byte) 0xef, result.getValue()[3]);
  }

  @Test
  public void decodeUtf8String_acceptsLegitimateString() {
    String legit =
        "0000000000000000000000000000000000000000000000000000000000000005"
            + "68656c6c6f000000000000000000000000000000000000000000000000000000";
    Utf8String result = TypeDecoder.decodeUtf8String(legit, 0);
    assertEquals("hello", result.getValue());
  }

  @Test
  public void decodeDynamicBytes_rejectsLengthExceedingRemainingInput() {
    String malicious =
        "00000000000000000000000000000000000000000000000000000000000003e8"; // length=1000
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicBytes(malicious, 0));
  }

  @Test
  public void decodeDynamicBytes_rejectsLengthCausingShiftOverflow() {
    // length = 2^30, shifting left by 1 produces 2^31 (= Integer.MIN_VALUE).
    String malicious =
        "0000000000000000000000000000000000000000000000000000000040000000";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicBytes(malicious, 0));
  }

  @Test
  public void decodeUtf8String_rejectsLengthExceedingRemainingInput() {
    String malicious =
        "00000000000000000000000000000000000000000000000000000000000003e8";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeUtf8String(malicious, 0));
  }

  public static class TwoStrings extends DynamicStruct {
    public String a;
    public String b;

    public TwoStrings(Utf8String a, Utf8String b) {
      super(a, b);
      this.a = a.getValue();
      this.b = b.getValue();
    }
  }

  @Test
  public void decodeDynamicStruct_rejectsParameterOffsetCausingMulOverflow() {
    // First dynamic-parameter offset = 0x40000001 (slightly above 2^30);
    // 0x40000001 * 2 + 64 overflows into a negative int.
    String malicious =
        "0000000000000000000000000000000000000000000000000000000040000001"
            + "00000000000000000000000000000000000000000000000000000000000000c0"
            + "0000000000000000000000000000000000000000000000000000000000000001"
            + "6100000000000000000000000000000000000000000000000000000000000000";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicStruct(
            malicious, 0, new TypeReference<TwoStrings>() {}));
  }

  @Test
  public void decodeDynamicArray_rejectsElementDataOffsetCausingMulOverflow() {
    // length=1; element0's dynamic head word (the data-offset pointer) = 0x40000000
    // (= 2^30, bitLength 31, so it passes the decodeUintAsInt cap). getDataOffset then
    // computes `pointer * 2`, which overflows int — without the multiplyExact guard it
    // would wrap to a negative offset and silently alias earlier bytes.
    String malicious =
        "0000000000000000000000000000000000000000000000000000000000000001"
            + "0000000000000000000000000000000000000000000000000000000040000000";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicArray(
            malicious, 0, new TypeReference<DynamicArray<DynamicBytes>>() {}));
  }

  @Test
  public void decodeDynamicArray_arrayLengthBoundRespectsOffset() {
    String malicious =
        "0000000000000000000000000000000000000000000000000000000000000003"  // length=3
            + "0000000000000000000000000000000000000000000000000000000000000001"  // elem0
            + "0000000000000000000000000000000000000000000000000000000000000002"; // elem1 (no elem2!)
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicArray(
            malicious, 0, new TypeReference<DynamicArray<Uint256>>() {}));
  }

  /** Solidity: {@code struct TwoUints { uint256 a; uint256 b; }} (all-static, 2 slots). */
  public static class TwoUints extends StaticStruct {
    public BigInteger a;
    public BigInteger b;

    public TwoUints(Uint256 a, Uint256 b) {
      super(a, b);
      this.a = a.getValue();
      this.b = b.getValue();
    }
  }

  @Test
  public void decodeDynamicArray_advancesMultiSlotElementsToExactInputEnd() {
    // A well-formed array of 2-slot static structs. This routes through
    // advanceArrayElementOffset's static-struct site with a multi-slot stride
    // (bytes32PaddedLength/32 = 2 slots per element), and the final element ends
    // exactly at input.length() — locking the strict `>` boundary so a legitimate
    // last element is not falsely rejected.
    String legit =
        "0000000000000000000000000000000000000000000000000000000000000002"  // length=2
            + "0000000000000000000000000000000000000000000000000000000000000001"  // elem0.a=1
            + "0000000000000000000000000000000000000000000000000000000000000002"  // elem0.b=2
            + "0000000000000000000000000000000000000000000000000000000000000003"  // elem1.a=3
            + "0000000000000000000000000000000000000000000000000000000000000004"; // elem1.b=4
    DynamicArray<TwoUints> result = TypeDecoder.decodeDynamicArray(
        legit, 0, new TypeReference<DynamicArray<TwoUints>>() {});
    assertEquals(2, result.getValue().size());
  }

  /** A nested TwoUints (128 hex chars) + trailing uint256 (64) = 192 hex chars expected. */
  public static class NestedTwoUints extends StaticStruct {
    public TwoUints inner;
    public BigInteger c;

    public NestedTwoUints(TwoUints inner, Uint256 c) {
      super(inner, c);
      this.inner = inner;
      this.c = c.getValue();
    }
  }

  @Test
  public void decodeUintAsInt_rejectsInputShorterThanSlot() {
    String malicious = "00";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeUintAsInt(malicious, 0));
  }

  @Test
  public void decodeUintAsInt_rejectsOffsetPastInput() {
    // Caller-supplied offset already past the end of input.
    String malicious =
        "0000000000000000000000000000000000000000000000000000000000000001";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeUintAsInt(malicious, 64));
  }

  @Test
  public void decodeBool_rejectsInputShorterThanSlot() {
    String malicious = "01";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeBool(malicious, 0));
  }

  @Test
  public void decodeBool_rejectsOffsetPastInput() {
    // Caller-supplied offset already past the end of input.
    String malicious =
        "0000000000000000000000000000000000000000000000000000000000000001";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeBool(malicious, 64));
  }

  @Test
  public void decodeDynamicStruct_rejectsInputShorterThanStaticHead() {
    String malicious =
        "0000000000000000000000000000000000000000000000000000000000000020";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicStruct(
            malicious, 0, new TypeReference<TwoStringsAndBytes>() {}));
  }

  public static class TwoStringsAndBytes extends DynamicStruct {
    public String a;
    public String b;
    public byte[] c;

    public TwoStringsAndBytes(Utf8String a, Utf8String b, DynamicBytes c) {
      super(a, b, c);
      this.a = a.getValue();
      this.b = b.getValue();
      this.c = c.getValue();
    }
  }

  /** Dynamic struct whose first field is a static uint256 — exercises the static-field path. */
  public static class StaticThenDynamic extends DynamicStruct {
    public BigInteger a;
    public String b;

    public StaticThenDynamic(Uint256 a, Utf8String b) {
      super(a, b);
      this.a = a.getValue();
      this.b = b.getValue();
    }
  }

  @Test
  public void decodeDynamicStruct_rejectsShortInputForStaticField() {
    // Static head needs at least 64 hex chars for the first uint256 slot,
    // but the input only has 60. Without a strict bounds check this slips past
    // L537's `beginIndex > input.length()` guard and trips an
    // ArrayIndexOutOfBoundsException inside decodeNumeric / arraycopy.
    String malicious = "000000000000000000000000000000000000000000000000000000000000";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicStruct(
            malicious, 0, new TypeReference<StaticThenDynamic>() {}));
  }

  @Test
  public void decodeStaticStruct_rejectsInputShorterThanField() {
    // TwoUints expects 128 hex chars (two 32-byte slots) but input has only 64.
    // Without a bounds check the second `input.substring(64, 128)` throws SIOOBE.
    String malicious =
        "0000000000000000000000000000000000000000000000000000000000000001";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeStaticStruct(
            malicious, 0, new TypeReference<TwoUints>() {}));
  }

  @Test
  public void decodeStaticStruct_rejectsInputShorterThanNestedStruct() {
    // NestedTwoUints expects 192 hex chars (nested 128 + tail 64) but only 64 provided.
    // Exercises the nested-StaticStruct bounds branch.
    String malicious =
        "0000000000000000000000000000000000000000000000000000000000000001";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeStaticStruct(
            malicious, 0, new TypeReference<NestedTwoUints>() {}));
  }

  @Test
  public void decodeDynamicStruct_rejectsParameterOffsetExceedingInputLength() {
    String malicious =
        // p0 byte offset = 0x80 → hex offset 320 (> input.length() = 256)
        "0000000000000000000000000000000000000000000000000000000000000080"
            // p1 byte offset = 0x90 → hex offset 352 (also > input.length(),
            //                                  and > p0 so parameterLength > 0)
            + "0000000000000000000000000000000000000000000000000000000000000090"
            // two filler slots so input.length() == 256 hex chars
            + "0000000000000000000000000000000000000000000000000000000000000001"
            + "6100000000000000000000000000000000000000000000000000000000000000";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicStruct(
            malicious, 0, new TypeReference<TwoStrings>() {}));
  }

  @Test
  public void decodeStaticStruct_innerTypes_rejectsShortInput() throws Exception {
    TypeReference<StaticStruct> ref =
        new TypeReference<StaticStruct>(
            false,
            Arrays.asList(
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"))) {};
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeStaticStruct("00", 0, ref));
  }

  @Test
  public void decodeDynamicStruct_innerTypes_rejectsShortHead() throws Exception {
    TypeReference<DynamicStruct> ref =
        new TypeReference<DynamicStruct>(
            false,
            Arrays.asList(
                TypeReference.makeTypeReference("string"),
                TypeReference.makeTypeReference("uint256"))) {};
    // One 64-hex-char slot: enough for the string head, nothing left for the
    // uint256 static head field.
    String oneSlot =
        "0000000000000000000000000000000000000000000000000000000000000040";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicStruct(oneSlot, 0, ref));
  }

  @Test
  public void decodeNumeric_rejectsInputShorterThanSlot() {
    // ABI numerics are always padded to 32 bytes (64 hex chars). The guard throws
    // IndexOutOfBoundsException: decodeNumeric's multi-catch swallows
    // IllegalArgumentException (for reflective newInstance) and would wrap it.
    String malicious = "00";
    assertThrows(
        IndexOutOfBoundsException.class,
        () -> TypeDecoder.decodeNumeric(malicious, Uint256.class));
  }
}
