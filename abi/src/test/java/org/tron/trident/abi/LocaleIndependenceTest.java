package org.tron.trident.abi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.datatypes.AbiTypes;
import org.tron.trident.abi.datatypes.DynamicArray;
import org.tron.trident.abi.datatypes.generated.Int256;

/**
 * ABI signature tokens are built by lowercasing Java class simple names; that
 * conversion must not depend on the JVM default locale. The canonical failure
 * mode is Turkish ("tr"), whose case mapping turns 'I' into dotless 'ı' —
 * corrupting e.g. "Int256" into "ınt256" and silently changing selectors.
 */
public class LocaleIndependenceTest {

  @Test
  public void testAbiTokensStableUnderTurkishLocale() throws Exception {
    Locale original = Locale.getDefault();
    try {
      Locale.setDefault(new Locale("tr", "TR"));
      // Sanity: this locale really maps 'I' to dotless 'ı' by default.
      assertEquals("ı", "I".toLowerCase());

      // Class-name → token via the TypeReference path (Utils.getSimpleTypeName).
      assertEquals("int256", Utils.getTypeName(TypeReference.create(Int256.class)));

      // Class-name → token via the instance path (AbiTypes.getTypeAString),
      // as used by Array/Struct getTypeAsString.
      assertEquals("int256", AbiTypes.getTypeAString(Int256.class));
      assertEquals(
          "int256[]",
          new DynamicArray<>(Int256.class, new Int256(BigInteger.ONE)).getTypeAsString());

      // Primitive wrapper type names (PrimitiveType constructor).
      assertEquals(
          "int", new org.tron.trident.abi.datatypes.primitive.Int(1).getTypeAsString());
    } finally {
      Locale.setDefault(original);
    }
  }
}
