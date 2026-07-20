package org.tron.trident.abi.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.AbiV2TestFixture;
import org.tron.trident.abi.datatypes.generated.Uint256;

public class AbiTypesTest {

  @Test
  public void testGetType_rejectsNonTypeClass() {
    UnsupportedOperationException ex =
        assertThrows(
            UnsupportedOperationException.class,
            () -> AbiTypes.getType("java.lang.System"));
    // sanity: error message names the offending input
    assert ex.getMessage().contains("java.lang.System");
  }

  @Test
  public void testGetType_rejectsRuntimeClass() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> AbiTypes.getType("java.lang.Runtime"));
  }

  @Test
  public void testGetType_rejectsNonexistentClass() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> AbiTypes.getType("does.not.exist.Foo"));
  }

  @Test
  public void testGetType_rejectsEmptyString() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> AbiTypes.getType(""));
  }

  @Test
  public void testGetType_acceptsBuiltinPrimitive() {
    // primitive type names still hit the switch, never the reflective fallback
    assertEquals(Address.class, AbiTypes.getType("address"));
    assertEquals(Bool.class, AbiTypes.getType("bool"));
    assertEquals(Utf8String.class, AbiTypes.getType("string"));
  }

  @Test
  public void testGetType_acceptsStructSubclassFqcn() {
    // AbiV2TestFixture.Foo extends DynamicStruct extends ... extends Type,
    // so the reflective fallback must accept it.
    Class<?> resolved = AbiTypes.getType(AbiV2TestFixture.Foo.class.getName());
    assertEquals(AbiV2TestFixture.Foo.class, resolved);
  }

  @Test
  public void testGetTypeAString_trcTokenIsCamelCase() {
    assertEquals("trcToken", AbiTypes.getTypeAString(TrcToken.class));
  }

  @Test
  public void testTrcTokenInstanceTypeString() {
    assertEquals("trcToken", new TrcToken(BigInteger.valueOf(1000016)).getTypeAsString());
  }

  @Test
  public void testGetTypeAString_trcTokenRoundTrip() {
    assertEquals(
        TrcToken.class, AbiTypes.getType(AbiTypes.getTypeAString(TrcToken.class)));
  }

  @Test
  public void testTrcTokenCompositeTypeStrings() {
    StaticStruct struct =
        new StaticStruct(
            new TrcToken(BigInteger.ONE), new Uint256(BigInteger.valueOf(2)));
    assertEquals("(trcToken,uint256)", struct.getTypeAsString());

    DynamicArray<TrcToken> array =
        new DynamicArray<>(
            TrcToken.class,
            Arrays.asList(new TrcToken(BigInteger.ONE), new TrcToken(BigInteger.TEN)));
    assertEquals("trcToken[]", array.getTypeAsString());

    DynamicArray<TrcToken> empty =
        new DynamicArray<>(TrcToken.class, Arrays.<TrcToken>asList());
    assertEquals("trcToken[]", empty.getTypeAsString());
  }

  @Test
  public void testGetTypeAString_bareBaseClassesCanonicalized() {
    assertEquals("uint256", AbiTypes.getTypeAString(Uint.class));
    assertEquals("int256", AbiTypes.getTypeAString(Int.class));
    assertEquals("ufixed256", AbiTypes.getTypeAString(Ufixed.class));
    assertEquals("fixed256", AbiTypes.getTypeAString(Fixed.class));
  }

  @Test
  public void testBareBaseClassCompositeTypeStrings() {
    DynamicArray<Uint> array =
        new DynamicArray<>(Uint.class, new Uint(BigInteger.ONE));
    assertEquals("uint256[]", array.getTypeAsString());

    StaticStruct struct =
        new StaticStruct(new Uint(BigInteger.ONE), new Int(BigInteger.TEN));
    assertEquals("(uint256,int256)", struct.getTypeAsString());
  }
}
