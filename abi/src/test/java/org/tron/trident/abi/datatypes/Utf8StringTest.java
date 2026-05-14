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

import org.junit.jupiter.api.Test;

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
    String string66Bytes = string33Bytes + string33Bytes;
    assertEquals(32, new Utf8String("").bytes32PaddedLength());
    assertEquals(32, new Utf8String("string").bytes32PaddedLength());
    assertEquals(32, new Utf8String(string32Bytes).bytes32PaddedLength());
    assertEquals(64, new Utf8String(string33Bytes).bytes32PaddedLength());
  }

  @Test
  public void testBytes32PaddedLengthWithNullValue() {
    // equals()/hashCode() already tolerate null value; bytes32PaddedLength
    // must too, otherwise reflective construction paths can NPE before
    // the value ever gets validated.
    assertEquals(32, new Utf8String(null).bytes32PaddedLength());
  }
}
