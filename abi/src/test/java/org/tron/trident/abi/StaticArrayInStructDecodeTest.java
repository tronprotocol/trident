package org.tron.trident.abi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.datatypes.DynamicStruct;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.StaticStruct;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.abi.datatypes.Utf8String;
import org.tron.trident.abi.datatypes.generated.StaticArray2;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.abi.datatypes.reflection.Parameterized;

/**
 * Regression coverage: decoding a struct that DIRECTLY contains a fixed-size array member (e.g. Solidity
 * {@code struct S { uint256 a; uint256[2] b; }}) crashes. When the struct TypeReference is
 * built the normal way ({@code new TypeReference<S>(){}}, i.e. {@code innerTypes == null}),
 * {@link DefaultFunctionReturnDecoder#build} dispatches to the reflection path
 * {@code TypeDecoder.decodeStaticStructElement} (and {@code decodeDynamicStructElements} for
 * dynamic structs), which has no {@code StaticArray} branch: the {@code StaticArrayN} field
 * falls into {@code decode(input.substring(...), 0, StaticArrayN.class)} which throws
 * {@code UnsupportedOperationException("Array types must be wrapped in a TypeReference")}.
 *
 * <p>The existing AbiV2TestFixture structs only cover scalar fields (Bar), nested-struct
 * fields (Fuzz) and dynamic-array fields (ArrayStruct) — never a fixed-size array member —
 * which is why this path was never exercised.
 */
public class StaticArrayInStructDecodeTest {

  /** Solidity: {@code struct StaticArrayInStruct { uint256 a; uint256[2] b; }} (all-static). */
  public static class StaticArrayInStruct extends StaticStruct {
    public StaticArrayInStruct(
        Uint256 a, @Parameterized(type = Uint256.class) StaticArray2<Uint256> b) {
      super(a, b);
    }
  }

  // ABI encoding of StaticArrayInStruct(a = 1, b = [2, 3]).
  // All-static struct => inline, three 32-byte words: [a, b[0], b[1]].
  private static final String ENCODED =
      "0000000000000000000000000000000000000000000000000000000000000001"  // a   = 1
          + "0000000000000000000000000000000000000000000000000000000000000002"  // b[0] = 2
          + "0000000000000000000000000000000000000000000000000000000000000003"; // b[1] = 3

  private static Function decodeFunction() {
    return new Function(
        "getStaticArrayInStruct",
        Collections.emptyList(),
        Arrays.asList(new TypeReference<StaticArrayInStruct>() {}));
  }

  /**
   * Regression guard: a static struct that directly contains a fixed-size array
   * member must decode via the reflection path ({@code new TypeReference<S>(){}},
   * {@code innerTypes == null}). Before the fix this threw
   * {@code UnsupportedOperationException("Array types must be wrapped in a TypeReference")}.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void decodesStaticArrayMemberViaReflectionPath() {
    Function function = decodeFunction();

    List<Type> decoded = FunctionReturnDecoder.decode(ENCODED, function.getOutputParameters());

    StaticArrayInStruct s = (StaticArrayInStruct) decoded.get(0);
    assertEquals(BigInteger.ONE, ((Uint256) s.getValue().get(0)).getValue());

    StaticArray2<Uint256> b = (StaticArray2<Uint256>) s.getValue().get(1);
    assertEquals(BigInteger.valueOf(2), b.getValue().get(0).getValue());
    assertEquals(BigInteger.valueOf(3), b.getValue().get(1).getValue());
  }

  /** Solidity: {@code struct DynStruct { string a; uint256[2] b; }} (dynamic, with static array). */
  public static class DynStruct extends DynamicStruct {
    public DynStruct(
        Utf8String a, @Parameterized(type = Uint256.class) StaticArray2<Uint256> b) {
      super(a, b);
    }
  }

  // ABI encoding of a function returning DynStruct(a = "hi", b = [2, 3]).
  // Dynamic struct => outer offset pointer, then struct head [a-offset, b[0], b[1]], then a's tail.
  private static final String ENCODED_DYNAMIC =
      "0000000000000000000000000000000000000000000000000000000000000020"  // outer offset -> struct
          + "0000000000000000000000000000000000000000000000000000000000000060"  // a: offset 0x60 (3 words)
          + "0000000000000000000000000000000000000000000000000000000000000002"  // b[0] = 2
          + "0000000000000000000000000000000000000000000000000000000000000003"  // b[1] = 3
          + "0000000000000000000000000000000000000000000000000000000000000002"  // a: length = 2
          + "6869000000000000000000000000000000000000000000000000000000000000"; // "hi"

  /**
   * Regression guard on the dynamic-struct reflection path
   * ({@code decodeDynamicStructElements}): a DynamicStruct with a static array member must decode.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void decodesStaticArrayMemberInDynamicStruct() {
    Function function =
        new Function(
            "getDynStruct",
            Collections.emptyList(),
            Arrays.asList(new TypeReference<DynStruct>() {}));

    List<Type> decoded =
        FunctionReturnDecoder.decode(ENCODED_DYNAMIC, function.getOutputParameters());

    DynStruct s = (DynStruct) decoded.get(0);
    assertEquals("hi", ((Utf8String) s.getValue().get(0)).getValue());

    StaticArray2<Uint256> b = (StaticArray2<Uint256>) s.getValue().get(1);
    assertEquals(BigInteger.valueOf(2), b.getValue().get(0).getValue());
    assertEquals(BigInteger.valueOf(3), b.getValue().get(1).getValue());
  }

  /**
   * Solidity: {@code struct StringArrayStruct { uint256 a; string[2] b; }}.
   * A fixed-size array of dynamic elements is itself ABI-dynamic: the struct head stores
   * an offset for {@code b}, and b's tail is head/tail encoded (per-element offsets).
   */
  public static class StringArrayStruct extends DynamicStruct {
    public StringArrayStruct(
        Uint256 a, @Parameterized(type = Utf8String.class) StaticArray2<Utf8String> b) {
      super(a, b);
    }
  }

  // ABI encoding of StringArrayStruct(a = 1, b = ["hi", "world"]).
  private static final String ENCODED_STRING_ARRAY =
      "0000000000000000000000000000000000000000000000000000000000000020"  // outer offset -> struct
          + "0000000000000000000000000000000000000000000000000000000000000001"  // a = 1
          + "0000000000000000000000000000000000000000000000000000000000000040"  // b: offset 0x40
          + "0000000000000000000000000000000000000000000000000000000000000040"  // b[0]: offset 0x40 (rel. to b)
          + "0000000000000000000000000000000000000000000000000000000000000080"  // b[1]: offset 0x80 (rel. to b)
          + "0000000000000000000000000000000000000000000000000000000000000002"  // b[0]: length = 2
          + "6869000000000000000000000000000000000000000000000000000000000000"  // "hi"
          + "0000000000000000000000000000000000000000000000000000000000000005"  // b[1]: length = 5
          + "776f726c64000000000000000000000000000000000000000000000000000000"; // "world"

  /**
   * A DynamicStruct whose fixed-size array member has DYNAMIC elements ({@code string[2]})
   * must decode on the constructor-reflection path ({@code new TypeReference<S>(){}}).
   * Before the fix, {@code decodeDynamicStructElements} misclassified the field as static
   * (Class-level isDynamic check) and {@code decodeStaticArrayStructField} threw
   * {@code UnsupportedOperationException}.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void decodesDynamicElementStaticArrayMemberInDynamicStruct() {
    Function function =
        new Function(
            "getStringArrayStruct",
            Collections.emptyList(),
            Arrays.asList(new TypeReference<StringArrayStruct>() {}));

    List<Type> decoded =
        FunctionReturnDecoder.decode(ENCODED_STRING_ARRAY, function.getOutputParameters());

    StringArrayStruct s = (StringArrayStruct) decoded.get(0);
    assertEquals(BigInteger.ONE, ((Uint256) s.getValue().get(0)).getValue());

    StaticArray2<Utf8String> b = (StaticArray2<Utf8String>) s.getValue().get(1);
    assertEquals("hi", b.getValue().get(0).getValue());
    assertEquals("world", b.getValue().get(1).getValue());
  }

  /** The hand-written hex above must match what the encoder actually produces (round-trip). */
  @Test
  public void stringArrayStructEncodingMatchesHandWrittenLayout() {
    StringArrayStruct s =
        new StringArrayStruct(
            new Uint256(BigInteger.ONE),
            new StaticArray2<>(Utf8String.class, new Utf8String("hi"), new Utf8String("world")));

    // Struct encoding without the outer offset word (64 hex chars).
    assertEquals(ENCODED_STRING_ARRAY.substring(64), TypeEncoder.encode(s));
  }

  /**
   * Solidity: {@code struct MixedStruct { uint256 a; string[2] b; string c; }} — the
   * dynamic-element fixed array is NOT the last dynamic member, exercising the head-slot
   * bookkeeping (two offset slots) and the tail-boundary length math between b and c.
   */
  public static class MixedStruct extends DynamicStruct {
    public MixedStruct(
        Uint256 a,
        @Parameterized(type = Utf8String.class) StaticArray2<Utf8String> b,
        Utf8String c) {
      super(a, b, c);
    }
  }

  // ABI encoding of MixedStruct(a = 1, b = ["hi", "world"], c = "x").
  private static final String ENCODED_MIXED =
      "0000000000000000000000000000000000000000000000000000000000000020"  // outer offset -> struct
          + "0000000000000000000000000000000000000000000000000000000000000001"  // a = 1
          + "0000000000000000000000000000000000000000000000000000000000000060"  // b: offset 0x60
          + "0000000000000000000000000000000000000000000000000000000000000120"  // c: offset 0x120
          + "0000000000000000000000000000000000000000000000000000000000000040"  // b[0]: offset 0x40 (rel. to b)
          + "0000000000000000000000000000000000000000000000000000000000000080"  // b[1]: offset 0x80 (rel. to b)
          + "0000000000000000000000000000000000000000000000000000000000000002"  // b[0]: length = 2
          + "6869000000000000000000000000000000000000000000000000000000000000"  // "hi"
          + "0000000000000000000000000000000000000000000000000000000000000005"  // b[1]: length = 5
          + "776f726c64000000000000000000000000000000000000000000000000000000"  // "world"
          + "0000000000000000000000000000000000000000000000000000000000000001"  // c: length = 1
          + "7800000000000000000000000000000000000000000000000000000000000000"; // "x"

  @Test
  @SuppressWarnings("unchecked")
  public void decodesDynamicElementStaticArrayFollowedByDynamicField() {
    Function function =
        new Function(
            "getMixedStruct",
            Collections.emptyList(),
            Arrays.asList(new TypeReference<MixedStruct>() {}));

    List<Type> decoded =
        FunctionReturnDecoder.decode(ENCODED_MIXED, function.getOutputParameters());

    MixedStruct s = (MixedStruct) decoded.get(0);
    assertEquals(BigInteger.ONE, ((Uint256) s.getValue().get(0)).getValue());

    StaticArray2<Utf8String> b = (StaticArray2<Utf8String>) s.getValue().get(1);
    assertEquals("hi", b.getValue().get(0).getValue());
    assertEquals("world", b.getValue().get(1).getValue());

    assertEquals("x", ((Utf8String) s.getValue().get(2)).getValue());
  }

  /** The hand-written hex above must match what the encoder actually produces (round-trip). */
  @Test
  public void mixedStructEncodingMatchesHandWrittenLayout() {
    MixedStruct s =
        new MixedStruct(
            new Uint256(BigInteger.ONE),
            new StaticArray2<>(Utf8String.class, new Utf8String("hi"), new Utf8String("world")),
            new Utf8String("x"));

    assertEquals(ENCODED_MIXED.substring(64), TypeEncoder.encode(s));
  }
}
