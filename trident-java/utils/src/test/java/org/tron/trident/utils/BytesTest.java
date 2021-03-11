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
package org.tron.trident.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class BytesTest {

    @Test
    public void testTrimLeadingZeroes() {
        Assertions.assertArrayEquals(Bytes.trimLeadingZeroes(new byte[] {}), (new byte[] {}));
        Assertions.assertArrayEquals(Bytes.trimLeadingZeroes(new byte[] {0}), (new byte[] {0}));
        Assertions.assertArrayEquals(Bytes.trimLeadingZeroes(new byte[] {1}), (new byte[] {1}));
        Assertions.assertArrayEquals(Bytes.trimLeadingZeroes(new byte[] {0, 1}), (new byte[] {1}));
        Assertions.assertArrayEquals(Bytes.trimLeadingZeroes(new byte[] {0, 0, 1}), (new byte[] {1}));
        Assertions.assertArrayEquals(Bytes.trimLeadingZeroes(new byte[] {0, 0, 1, 0}), (new byte[] {1, 0}));
    }
}
