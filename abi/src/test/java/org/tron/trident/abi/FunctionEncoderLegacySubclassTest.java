package org.tron.trident.abi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.Type;

/**
 * A FunctionEncoder subclass implementing only encodeFunction and
 * encodeParameters keeps serving those entry points; the selector/packed entry
 * points throw UnsupportedOperationException instead of AbstractMethodError.
 */
public class FunctionEncoderLegacySubclassTest {

  static class LegacyEncoder extends FunctionEncoder {
    @Override
    protected String encodeFunction(Function function) {
      return "legacy-function";
    }

    @Override
    protected String encodeParameters(List<Type> parameters) {
      return "legacy-parameters";
    }
  }

  @Test
  public void testOriginalContractStillServed() {
    LegacyEncoder encoder = new LegacyEncoder();

    assertEquals(
        "legacy-function",
        encoder.encodeFunction(
            new Function("f", Collections.emptyList(), Collections.emptyList())));
    assertEquals("legacy-parameters", encoder.encodeParameters(Collections.emptyList()));
  }

  @Test
  public void testNewEntryPointsThrowWithClearMessage() {
    LegacyEncoder encoder = new LegacyEncoder();

    UnsupportedOperationException selectorEx = assertThrows(
        UnsupportedOperationException.class,
        () -> encoder.encodeWithSelector("12345678", Collections.emptyList()));
    assertTrue(selectorEx.getMessage().contains("encodeWithSelector"),
        "expected method name in message, got: " + selectorEx.getMessage());
    assertTrue(selectorEx.getMessage().contains(LegacyEncoder.class.getName()),
        "expected subclass name in message, got: " + selectorEx.getMessage());

    UnsupportedOperationException packedEx = assertThrows(
        UnsupportedOperationException.class,
        () -> encoder.encodePackedParameters(Collections.emptyList()));
    assertTrue(packedEx.getMessage().contains("encodePackedParameters"),
        "expected method name in message, got: " + packedEx.getMessage());
  }
}
