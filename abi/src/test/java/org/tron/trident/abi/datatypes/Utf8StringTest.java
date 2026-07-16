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

package org.tron.trident.abi.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.TypeEncoder;
import org.tron.trident.abi.datatypes.generated.StaticArray2;

public class Utf8StringTest {

  @Test
  public void testToString() {
    assertEquals(new Utf8String("").toString(), (""));
    assertEquals(new Utf8String("string").toString(), ("string"));
  }
  @Test
  public void testBytes32PaddedLength() {
    String string32Bytes = "12345678901234567890123456789012";
    String string33Bytes = "123456789012345678901234567890123";
    assertEquals(32, new Utf8String("").bytes32PaddedLength());
    assertEquals(64, new Utf8String("string").bytes32PaddedLength());
    assertEquals(64, new Utf8String(string32Bytes).bytes32PaddedLength());
    assertEquals(96, new Utf8String(string33Bytes).bytes32PaddedLength());
  }

  @Test
  public void testBytes32PaddedLengthWithNullValue() {
    // equals()/hashCode() already tolerate null value; bytes32PaddedLength
    // must too, otherwise reflective construction paths can NPE before
    // the value ever gets validated.
    assertEquals(32, new Utf8String(null).bytes32PaddedLength());
  }

  @Test
  public void testUtf8StringArrayChinese() {
    // "你好" is 2 Chinese characters. Each is 3 bytes in UTF-8. Total 6 bytes.
    Utf8String s1 = new Utf8String("\u4f60\u597d");
    // "世界" is 2 Chinese characters. Each is 3 bytes in UTF-8. Total 6 bytes.
    Utf8String s2 = new Utf8String("\u4e16\u754c");

    DynamicArray<Utf8String> array =
        new DynamicArray<>(Utf8String.class, Arrays.asList(s1, s2));

    String encoded = TypeEncoder.encode(array);

    String expectedLength = "0000000000000000000000000000000000000000000000000000000000000002";
    String expectedOffset1 = "0000000000000000000000000000000000000000000000000000000000000040";
    String expectedOffset2 = "0000000000000000000000000000000000000000000000000000000000000080";

    assertEquals(
        expectedLength + expectedOffset1 + expectedOffset2, encoded.substring(0, 64 * 3));
  }

  @Test
  public void testUtf8StringLongChinese() {
    // 14 characters * 3 bytes = 42 bytes.
    String longString =
        "\u4f60\u597d\u4e16\u754c\u4f60\u597d\u4e16\u754c\u4f60\u597d\u4e16\u754c\u4f60\u597d";
    Utf8String s1 = new Utf8String(longString);

    DynamicArray<Utf8String> array =
        new DynamicArray<>(Utf8String.class, Collections.singletonList(s1));
    String encoded = TypeEncoder.encode(array);

    String expectedLength = "0000000000000000000000000000000000000000000000000000000000000001";
    String expectedOffset1 = "0000000000000000000000000000000000000000000000000000000000000020";
    String expectedS1Length =
        "000000000000000000000000000000000000000000000000000000000000002a";

    assertEquals(
        expectedLength + expectedOffset1 + expectedS1Length, encoded.substring(0, 64 * 3));
  }

  @Test
  public void testUtf8String33Bytes() {
    // 10 Chinese characters = 30 bytes. + 3 chars = 33 bytes.
    String s33 = "\u4f60\u597d\u4e16\u754c\u4f60\u597d\u4e16\u754c\u4f60\u597d" + "aaa";
    Utf8String string33 = new Utf8String(s33);
    assertEquals(33, s33.getBytes(StandardCharsets.UTF_8).length);

    assertEquals(96, string33.bytes32PaddedLength());
  }

  @Test
  public void testUtf8StringEmpty() {
    Utf8String string = new Utf8String("");
    assertEquals(32, string.bytes32PaddedLength());
  }

  @Test
  public void testStaticUtf8StringArray() {
    Utf8String s1 = new Utf8String("a");
    Utf8String s2 = new Utf8String("b");

    StaticArray2<Utf8String> array = new StaticArray2<>(Utf8String.class, s1, s2);

    String encoded = TypeEncoder.encode(array);

    String expectedOffset1 = "0000000000000000000000000000000000000000000000000000000000000040";
    String expectedOffset2 = "0000000000000000000000000000000000000000000000000000000000000080";

    assertEquals(expectedOffset1 + expectedOffset2, encoded.substring(0, 64 * 2));
  }

  @Test
  public void testStaticUtf8StringArrayInStruct() {
    Utf8String s1 = new Utf8String("a");
    Utf8String s2 = new Utf8String("b");
    StaticArray2<Utf8String> array = new StaticArray2<>(Utf8String.class, s1, s2);

    DynamicStruct struct = new DynamicStruct(array);

    String encoded = TypeEncoder.encode(struct);

    String expectedOffsetArray =
        "0000000000000000000000000000000000000000000000000000000000000020";
    assertEquals(expectedOffsetArray, encoded.substring(0, 64));
  }
}
