package org.tron.trident.abi.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.AbiV2TestFixture;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.abi.datatypes.generated.Uint8;

public class DynamicArrayTest {

  @Test
  public void testEmptyDynamicArray() {
    final DynamicArray<Address> array =
        new DynamicArray<>(Address.class, Collections.emptyList());

    assertEquals(Address.TYPE_NAME + "[]", array.getTypeAsString());
  }

  @Test
  public void testDynamicArrayWithDynamicStruct() {
    final List<DynamicStruct> list = Collections.singletonList(new DynamicStruct());
    final DynamicArray<DynamicStruct> array = new DynamicArray<>(DynamicStruct.class, list);

    assertEquals("()[]", array.getTypeAsString());
  }

  @Test
  public void testEmptyDynamicArrayOfConcreteStruct() {
    final DynamicArray<AbiV2TestFixture.Nazzy> array =
        new DynamicArray<>(AbiV2TestFixture.Nazzy.class, Collections.emptyList());

    assertEquals("((string,string)[])[]", array.getTypeAsString());
  }

  @Test
  public void testDynamicArrayWithAbiType() {
    final DynamicArray<Uint> array = new DynamicArray<>(Uint.class, arrayOfUints(1));

    assertEquals("uint256[]", array.getTypeAsString());
  }

  @Test
  public void testMultidimensionalDynamicArray() {
    DynamicArray<DynamicArray> array =
        new DynamicArray<>(
            DynamicArray.class,
            Collections.singletonList(
                new DynamicArray<>(
                    DynamicArray.class,
                    Collections.singletonList(
                        new DynamicArray<>(
                            Uint256.class, new ArrayList<>())))));
    assertEquals("uint256[][][]", array.getTypeAsString());
  }

  @Test
  public void testEmptyDynamicArrayOfRawArrayTypeThrows() {
    // Nested empty arrays cannot recover the inner element type via reflection
    // (Java type erasure); componentType is the raw DynamicArray.class, so
    // emitting an ABI type string like "dynamicarray[]" would be malformed.
    // Must fail loudly, same fail-fast policy as the empty generic struct case.
    final DynamicArray<DynamicArray> empty =
        new DynamicArray<>(DynamicArray.class, Collections.emptyList());

    UnsupportedOperationException ex = assertThrows(
        UnsupportedOperationException.class,
        empty::getTypeAsString);
    assertTrue(ex.getMessage().contains("nested array"),
        "expected nested-array guidance, got: " + ex.getMessage());
  }

  @Test
  public void testEmptyDynamicArrayOfGenericStructTypeThrows() {
    final DynamicArray<DynamicStruct> empty =
        new DynamicArray<>(DynamicStruct.class, Collections.emptyList());

    UnsupportedOperationException ex = assertThrows(
        UnsupportedOperationException.class,
        empty::getTypeAsString);
    assertTrue(ex.getMessage().contains("generic struct"),
        "expected generic-struct guidance, got: " + ex.getMessage());
  }

  private Uint[] arrayOfUints(int length) {
    return IntStream.rangeClosed(1, length).mapToObj(Uint8::new).toArray(Uint[]::new);
  }

}
