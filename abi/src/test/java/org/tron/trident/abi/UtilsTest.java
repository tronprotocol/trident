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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.DynamicArray;
import org.tron.trident.abi.datatypes.DynamicBytes;
import org.tron.trident.abi.datatypes.Fixed;
import org.tron.trident.abi.datatypes.Int;
import org.tron.trident.abi.datatypes.StaticArray;
import org.tron.trident.abi.datatypes.StaticStruct;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.abi.datatypes.Ufixed;
import org.tron.trident.abi.datatypes.Uint;
import org.tron.trident.abi.datatypes.Utf8String;
import org.tron.trident.abi.datatypes.generated.Int64;
import org.tron.trident.abi.datatypes.generated.StaticArray2;
import org.tron.trident.abi.datatypes.generated.StaticArray3;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.abi.datatypes.generated.Uint64;
import org.tron.trident.abi.datatypes.reflection.Parameterized;

public class UtilsTest {

  @Test
  public void testGetTypeName() {
    assertEquals(Utils.getTypeName(new TypeReference<Uint>() {
    }), ("uint256"));
    assertEquals(Utils.getTypeName(new TypeReference<Int>() {
    }), ("int256"));
    assertEquals(Utils.getTypeName(new TypeReference<Ufixed>() {
    }), ("ufixed256"));
    assertEquals(Utils.getTypeName(new TypeReference<Fixed>() {
    }), ("fixed256"));

    assertEquals(Utils.getTypeName(new TypeReference<Uint64>() {
    }), ("uint64"));
    assertEquals(Utils.getTypeName(new TypeReference<Int64>() {
    }), ("int64"));
    assertEquals(Utils.getTypeName(new TypeReference<Bool>() {
    }), ("bool"));
    assertEquals(Utils.getTypeName(new TypeReference<Utf8String>() {
    }), ("string"));
    assertEquals(Utils.getTypeName(new TypeReference<DynamicBytes>() {
    }), ("bytes"));

    assertEquals(
        Utils.getTypeName(
            new TypeReference.StaticArrayTypeReference<StaticArray<Uint>>(5) {
            }),
        ("uint256[5]"));
    assertEquals(Utils.getTypeName(new TypeReference<DynamicArray<Uint>>() {
    }), ("uint256[]"));
  }

  @Test
  public void testGetTypeNameNestedArrayUsesCanonicalAbiName() throws Exception {
    assertEquals(
        "uint256[2][]",
        Utils.getTypeName(new TypeReference<DynamicArray<StaticArray2<Uint256>>>() {
        }));
    assertEquals(
        "uint256[2][]",
        Utils.getTypeName(TypeReference.makeTypeReference("uint256[2][]")));
    assertEquals(
        "uint256[2][2]",
        Utils.getTypeName(new TypeReference<StaticArray2<StaticArray2<Uint256>>>() {
        }));
  }

  @Test
  public void testTypeMap() {
    final List<BigInteger> input =
        Arrays.asList(BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN);

    Assertions.assertEquals(
        Utils.typeMap(input, Uint256.class),
        (Arrays.asList(
            new Uint256(BigInteger.ZERO),
            new Uint256(BigInteger.ONE),
            new Uint256(BigInteger.TEN))));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testTypeMapNested() {
    List<BigInteger> innerList1 = Arrays.asList(BigInteger.valueOf(1), BigInteger.valueOf(2));
    List<BigInteger> innerList2 = Arrays.asList(BigInteger.valueOf(3), BigInteger.valueOf(4));

    final List<List<BigInteger>> input = Arrays.asList(innerList1, innerList2);

    StaticArray2<Uint256> staticArray1 =
        new StaticArray2<>(Uint256.class, new Uint256(1), new Uint256(2));

    StaticArray2<Uint256> staticArray2 =
        new StaticArray2<>(Uint256.class, new Uint256(3), new Uint256(4));

    List<StaticArray2> actual = Utils.typeMap(input, StaticArray2.class, Uint256.class);

    assertEquals(actual.get(0), (staticArray1));
    assertEquals(actual.get(1), (staticArray2));
  }

  @Test
  public void testTypeMapEmpty() {
    Assertions.assertEquals(Utils.typeMap(new ArrayList<>(), Uint256.class),
        (new ArrayList<Uint256>()));
  }

  @Test
  public void testValidateTypeReferenceDepthAcceptsNull() {
    // Null root is a no-op (defensive); should not throw.
    Assertions.assertDoesNotThrow(() -> Utils.validateTypeReferenceDepth(null));
  }

  @Test
  public void testValidateTypeReferenceDepthAcceptsRealisticNesting() {
    // DynamicArray<DynamicArray<Uint256>> — depth 3, well below the limit.
    TypeReference<?> ref = new TypeReference<DynamicArray<DynamicArray<Uint256>>>() {};
    Assertions.assertDoesNotThrow(() -> Utils.validateTypeReferenceDepth(ref));
  }

  @Test
  public void testValidateTypeReferenceDepthRejectsExcessiveNesting() {
    // Build a chain of innerTypes 15 levels deep — beyond MAX_TYPEREF_DEPTH (10).
    TypeReference<?> chain = TypeReference.create(Uint256.class);
    for (int i = 0; i < 15; i++) {
      final TypeReference<?> child = chain;
      chain = new TypeReference<Type>(false, Arrays.asList(child)) { };
    }
    final TypeReference<?> tooDeep = chain;
    UnsupportedOperationException ex = Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> Utils.validateTypeReferenceDepth(tooDeep));
    Assertions.assertTrue(ex.getMessage().contains("depth"),
        "expected depth-related message, got: " + ex.getMessage());
  }

  @Test
  public void testValidateTypeReferenceDepthAcceptsSharedNodes() {
    TypeReference<?> shared = TypeReference.create(Uint256.class);
    TypeReference<?> left = new TypeReference<Type>(false, Arrays.asList(shared)) { };
    TypeReference<?> right = new TypeReference<Type>(false, Arrays.asList(shared)) { };
    TypeReference<?> root = new TypeReference<Type>(false, Arrays.asList(left, right)) { };

    Assertions.assertDoesNotThrow(() -> Utils.validateTypeReferenceDepth(root));
  }

  @Test
  public void testValidateTypeReferenceDepthRejectsCycle() {
    List<TypeReference<?>> rootChildren = new ArrayList<>();
    TypeReference<?> root = new TypeReference<Type>(false, rootChildren) { };
    TypeReference<?> child = new TypeReference<Type>(false, Arrays.asList(root)) { };
    rootChildren.add(child);

    // A cycle keeps increasing depth along the cyclic path, so it is rejected by
    // the depth cap rather than a dedicated cycle check.
    UnsupportedOperationException ex = Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> Utils.validateTypeReferenceDepth(root));
    Assertions.assertTrue(ex.getMessage().contains("cycle"),
        "expected cycle-related message, got: " + ex.getMessage());
  }

  @Test
  public void testGetStructTypeWithParameterizedStaticArray() {
    // @Parameterized on a generated StaticArrayN field should emit the static
    // array suffix (e.g. "uint256[3]"), not collapse to "uint256[]".
    String result = Utils.getStructType(TestStructWithParameterizedStaticArray.class);
    assertEquals("(address,uint256[3])", result);
  }

  @Test
  public void testGetStructTypeWithParameterizedDynamicArrayUnchanged() {
    // Regression guard: @Parameterized on a DynamicArray field must still
    // produce the dynamic-array suffix "[]" — the StaticArray fix must not
    // accidentally rewrite this case.
    String result = Utils.getStructType(TestStructWithParameterizedDynamicArray.class);
    assertEquals("(address,uint256[])", result);
  }

  @Test
  public void testExtractStaticArraySizeRejectsBareStaticArrayClass() {
    // The bare StaticArray base class has no size suffix; using it as a
    // field type for @Parameterized must fail loudly rather than silently
    // producing a nonsense type string.
    IllegalArgumentException ex = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> Utils.getTypeReferenceForParameterizedField(StaticArray.class, Uint256.class));
    Assertions.assertTrue(ex.getMessage().contains("StaticArrayN"),
        "expected guidance toward StaticArrayN, got: " + ex.getMessage());
  }

  @Test
  public void testGetStructTypeRejectsNestedStaticArrayField() {
    // The current @Parameterized-based constructor-reflection path represents only
    // one element-type level, so nested arrays are explicitly unsupported. It must
    // fail loudly instead of emitting an invalid signature like "(staticarray2[2])"
    // or silently misreading tail data during decode.
    UnsupportedOperationException ex = Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> Utils.getStructType(TestStructWithNestedStaticArray.class));
    Assertions.assertTrue(ex.getMessage().contains("Nested arrays"),
        "expected nested-array rejection, got: " + ex.getMessage());
  }

  @Test
  public void testGetTypeReferenceForParameterizedFieldRejectsNestedDynamicArray() {
    // uint256[2][] field shape: DynamicArray field whose @Parameterized element
    // is itself an array class.
    UnsupportedOperationException ex = Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> Utils.getTypeReferenceForParameterizedField(
            DynamicArray.class, StaticArray2.class));
    Assertions.assertTrue(ex.getMessage().contains("Nested arrays"),
        "expected nested-array rejection, got: " + ex.getMessage());
  }

  @Test
  public void testGetTypeReferenceForParameterizedFieldAllowsStructElements() {
    // Struct classes extend StaticArray/DynamicArray for encoding purposes but
    // are legitimate array element types — the nested-array guard must not
    // reject arrays of structs.
    Assertions.assertDoesNotThrow(
        () -> Utils.getTypeReferenceForParameterizedField(
            StaticArray3.class, TestStructWithParameterizedStaticArray.class));
  }

  @Test
  public void testGetStructTypeRejectsUnannotatedDynamicArrayField() {
    // A DynamicArray field without @Parameterized used to fall through to the
    // generic class-name fallback and silently emit the invalid token
    // "dynamicarray" into the signature — producing a wrong selector. It must
    // fail loudly, naming the struct, the parameter and the missing annotation.
    UnsupportedOperationException ex = Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> Utils.getStructType(TestStructWithUnannotatedDynamicArray.class));
    Assertions.assertTrue(ex.getMessage().contains("@Parameterized"),
        "expected missing-annotation message, got: " + ex.getMessage());
    Assertions.assertTrue(
        ex.getMessage().contains(TestStructWithUnannotatedDynamicArray.class.getName()),
        "expected struct class name in message, got: " + ex.getMessage());
    Assertions.assertTrue(ex.getMessage().contains("parameter 1"),
        "expected parameter index in message, got: " + ex.getMessage());
  }

  @Test
  public void testGetStructTypeRejectsUnannotatedStaticArrayField() {
    // Same guard for generated StaticArrayN fields, which used to emit
    // e.g. "staticarray2" as a signature token.
    UnsupportedOperationException ex = Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> Utils.getStructType(TestStructWithUnannotatedStaticArray.class));
    Assertions.assertTrue(ex.getMessage().contains("@Parameterized"),
        "expected missing-annotation message, got: " + ex.getMessage());
    Assertions.assertTrue(ex.getMessage().contains("StaticArray2"),
        "expected field class name in message, got: " + ex.getMessage());
  }

  @Test
  public void testGetStructTypeRejectsAnnotatedScalarField() {
    // The inverse misuse of the missing-annotation case: @Parameterized on a
    // non-array field used to fall into the DynamicArray branch of
    // getTypeReferenceForParameterizedField, silently rewriting a "uint256"
    // field into "uint256[]" in the signature.
    UnsupportedOperationException ex = Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> Utils.getStructType(TestStructWithAnnotatedScalarField.class));
    Assertions.assertTrue(ex.getMessage().contains("only applicable to array-typed"),
        "expected non-array rejection message, got: " + ex.getMessage());
    Assertions.assertTrue(ex.getMessage().contains("Uint256"),
        "expected field class name in message, got: " + ex.getMessage());
  }

  @Test
  public void testGetTypeReferenceForParameterizedFieldRejectsNonArrayFieldType() {
    UnsupportedOperationException ex = Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> Utils.getTypeReferenceForParameterizedField(Uint256.class, Uint256.class));
    Assertions.assertTrue(ex.getMessage().contains("only applicable to array-typed"),
        "expected non-array rejection message, got: " + ex.getMessage());
  }
}

class TestStructWithParameterizedStaticArray extends StaticStruct {
  public TestStructWithParameterizedStaticArray(
      Address address, @Parameterized(type = Uint256.class) StaticArray3<Uint256> uint256Array) {
    super(address, uint256Array);
  }
}

class TestStructWithParameterizedDynamicArray extends StaticStruct {
  public TestStructWithParameterizedDynamicArray(
      Address address, @Parameterized(type = Uint256.class) DynamicArray<Uint256> uint256Array) {
    super(address, uint256Array);
  }
}

class TestStructWithNestedStaticArray extends StaticStruct {
  public TestStructWithNestedStaticArray(
      @Parameterized(type = StaticArray2.class)
      StaticArray2<StaticArray2<Uint256>> matrix) {
    super(matrix);
  }
}

class TestStructWithUnannotatedDynamicArray extends StaticStruct {
  public TestStructWithUnannotatedDynamicArray(
      Address address, DynamicArray<Uint256> uint256Array) {
    super(address, uint256Array);
  }
}

class TestStructWithUnannotatedStaticArray extends StaticStruct {
  public TestStructWithUnannotatedStaticArray(
      Address address, StaticArray2<Uint256> uint256Array) {
    super(address, uint256Array);
  }
}

class TestStructWithAnnotatedScalarField extends StaticStruct {
  public TestStructWithAnnotatedScalarField(
      Address address, @Parameterized(type = Uint256.class) Uint256 value) {
    super(address, value);
  }
}
