package org.tron.trident.abi.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.tron.trident.abi.AbiV2TestFixture;

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
}
