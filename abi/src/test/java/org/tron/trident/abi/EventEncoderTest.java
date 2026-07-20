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

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.datatypes.DynamicArray;
import org.tron.trident.abi.datatypes.DynamicStruct;
import org.tron.trident.abi.datatypes.Event;
import org.tron.trident.abi.datatypes.generated.StaticArray2;
import org.tron.trident.abi.datatypes.generated.Uint256;

public class EventEncoderTest {

  @Test
  public void testBuildEventSignature() {
    assertEquals(
        EventEncoder.buildEventSignature("Deposit(address,hash256,uint256)"),
        ("0x50cb9fe53daa9737b786ab3646f04d0150dc50ef4e75f59509d83667ad5adb20"));

    assertEquals(
        EventEncoder.buildEventSignature("Notify(uint256,uint256)"),
        ("0x71e71a8458267085d5ab16980fd5f114d2d37f232479c245d523ce8d23ca40ed"));
  }

  @Test
  public void testEncode() {
    Event event =
        new Event(
            "Notify",
            Arrays.<TypeReference<?>>asList(
                new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }));

    assertEquals(
        EventEncoder.encode(event),
        "0x71e71a8458267085d5ab16980fd5f114d2d37f232479c245d523ce8d23ca40ed");
  }

  @Test
  public void testBuildMethodSignature() {
    List<TypeReference<?>> parameters =
        Arrays.<TypeReference<?>>asList(
            new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            });

    assertEquals(
        EventEncoder.buildMethodSignature("Notify", Utils.convert(parameters)),
        "Notify(uint256,uint256)");
  }

  @Test
  void testBuildMethodSignatureWithDynamicStructs() {
    assertEquals(
        "nazzEvent((((string,string)[])[],uint256),(string,string))",
        EventEncoder.buildMethodSignature(
            AbiV2TestFixture.nazzEvent.getName(),
            AbiV2TestFixture.nazzEvent.getParameters()));
  }

  @Test
  void testBuildMethodSignatureWithDynamicArrays() {
    assertEquals(
        "nazzEvent2((((string,string)[])[],uint256)[])",
        EventEncoder.buildMethodSignature(
            AbiV2TestFixture.nazzEvent2.getName(),
            AbiV2TestFixture.nazzEvent2.getParameters()));
  }

  @Test
  void testBuildMethodSignatureWithNestedStaticArray() {
    List<TypeReference<?>> parameters =
        Arrays.asList(new TypeReference<DynamicArray<StaticArray2<Uint256>>>() {
        });

    assertEquals(
        "Nested(uint256[2][])",
        EventEncoder.buildMethodSignature("Nested", Utils.convert(parameters)));
  }

  @Test
  void testBuildMethodSignatureWithInnerTypesTuple() throws ClassNotFoundException {
    List<TypeReference<?>> tupleFields =
        Arrays.asList(
            TypeReference.makeTypeReference("uint256"),
            TypeReference.makeTypeReference("string"));
    List<TypeReference<?>> parameters =
        Arrays.asList(new TypeReference<DynamicStruct>(false, tupleFields) {
        });

    assertEquals(
        "E((uint256,string))",
        EventEncoder.buildMethodSignature("E", Utils.convert(parameters)));
  }

  @Test
  void testBuildMethodSignatureWithNestedInnerTypesTuple() throws ClassNotFoundException {
    List<TypeReference<?>> innerFields =
        Arrays.asList(
            TypeReference.makeTypeReference("uint256"),
            TypeReference.makeTypeReference("string"));
    List<TypeReference<?>> outerFields =
        Arrays.asList(
            new TypeReference<DynamicStruct>(false, innerFields) {
            },
            TypeReference.makeTypeReference("bool"));
    List<TypeReference<?>> parameters =
        Arrays.asList(new TypeReference<DynamicStruct>(false, outerFields) {
        });

    assertEquals(
        "E(((uint256,string),bool))",
        EventEncoder.buildMethodSignature("E", Utils.convert(parameters)));
  }

  @Test
  void testBuildMethodSignatureWithArrayOfInnerTypesTuple() throws ClassNotFoundException {
    List<TypeReference<?>> tupleFields =
        Arrays.asList(
            TypeReference.makeTypeReference("uint256"),
            TypeReference.makeTypeReference("string"));
    final TypeReference<DynamicStruct> tupleRef =
        new TypeReference<DynamicStruct>(false, tupleFields) {
        };
    List<TypeReference<?>> parameters =
        Arrays.asList(
            new TypeReference<DynamicArray>() {
              @Override
              public TypeReference getSubTypeReference() {
                return tupleRef;
              }
            });

    assertEquals(
        "E((uint256,string)[])",
        EventEncoder.buildMethodSignature("E", Utils.convert(parameters)));
  }

  @Test
  void testInnerTypesTupleSignatureMatchesGeneratedClass() throws ClassNotFoundException {
    // AbiV2TestFixture.Foo is a generated-class style struct of (string,string);
    // the runtime innerTypes style must produce the identical signature.
    List<TypeReference<?>> fooFields =
        Arrays.asList(
            TypeReference.makeTypeReference("string"),
            TypeReference.makeTypeReference("string"));
    List<TypeReference<?>> runtimeStyle =
        Arrays.asList(new TypeReference<DynamicStruct>(false, fooFields) {
        });
    List<TypeReference<?>> generatedStyle =
        Arrays.asList(new TypeReference<AbiV2TestFixture.Foo>() {
        });

    assertEquals(
        EventEncoder.buildMethodSignature("E", Utils.convert(generatedStyle)),
        EventEncoder.buildMethodSignature("E", Utils.convert(runtimeStyle)));
  }
}
