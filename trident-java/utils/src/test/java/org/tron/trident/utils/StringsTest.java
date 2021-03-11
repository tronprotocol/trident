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

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringsTest {

    @Test
    public void testToCsv() {
        Assertions.assertEquals(Strings.toCsv(Collections.<String>emptyList()), (""));
        Assertions.assertEquals(Strings.toCsv(Collections.singletonList("a")), ("a"));
        Assertions.assertEquals(Strings.toCsv(Arrays.asList("a", "b", "c")), ("a, b, c"));
    }

    @Test
    public void testJoin() {
        Assertions.assertEquals(Strings.join(Arrays.asList("a", "b"), "|"), ("a|b"));
        assertNull(Strings.join(null, "|"));
        Assertions.assertEquals(Strings.join(Collections.singletonList("a"), "|"), ("a"));
    }

    @Test
    public void testCapitaliseFirstLetter() {
        Assertions.assertEquals(Strings.capitaliseFirstLetter(""), (""));
        Assertions.assertEquals(Strings.capitaliseFirstLetter("a"), ("A"));
        Assertions.assertEquals(Strings.capitaliseFirstLetter("aa"), ("Aa"));
        Assertions.assertEquals(Strings.capitaliseFirstLetter("A"), ("A"));
        Assertions.assertEquals(Strings.capitaliseFirstLetter("Ab"), ("Ab"));
    }

    @Test
    public void testLowercaseFirstLetter() {
        Assertions.assertEquals(Strings.lowercaseFirstLetter(""), (""));
        Assertions.assertEquals(Strings.lowercaseFirstLetter("A"), ("a"));
        Assertions.assertEquals(Strings.lowercaseFirstLetter("AA"), ("aA"));
        Assertions.assertEquals(Strings.lowercaseFirstLetter("a"), ("a"));
        Assertions.assertEquals(Strings.lowercaseFirstLetter("aB"), ("aB"));
    }

    @Test
    public void testRepeat() {
        Assertions.assertEquals(Strings.repeat('0', 0), (""));
        Assertions.assertEquals(Strings.repeat('1', 3), ("111"));
    }

    @Test
    public void testZeros() {
        Assertions.assertEquals(Strings.zeros(0), (""));
        Assertions.assertEquals(Strings.zeros(3), ("000"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testEmptyString() {
        Assertions.assertTrue(Strings.isEmpty(null));
        Assertions.assertTrue(Strings.isEmpty(""));
        Assertions.assertFalse(Strings.isEmpty("hello world"));
    }
}
