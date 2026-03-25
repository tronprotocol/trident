package org.tron.trident.abi.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
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
  public void testDynamicArrayWithAbiType() {
    final DynamicArray<Uint> array = new DynamicArray<>(Uint.class, arrayOfUints(1));

    assertEquals(Uint.TYPE_NAME + "[]", array.getTypeAsString());
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

  private Uint[] arrayOfUints(int length) {
    return IntStream.rangeClosed(1, length).mapToObj(Uint8::new).toArray(Uint[]::new);
  }

}
