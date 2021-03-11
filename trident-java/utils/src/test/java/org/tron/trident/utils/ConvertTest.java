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

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConvertTest {

    @Test
    public void testFromSum() {
        assertEquals(
                Convert.fromSun("21000000000000", Convert.Unit.SUN),
                (new BigDecimal("21000000000000")));

        assertEquals(
                Convert.fromSun("21000000000000", Convert.Unit.TRX),
                (new BigDecimal("21000000")));
    }

    @Test
    public void testToWei() {
        assertEquals(Convert.toSun("21", Convert.Unit.SUN), (new BigDecimal("21")));
        assertEquals(
                Convert.toSun("21", Convert.Unit.TRX), (new BigDecimal("21000000")));
    }

    @Test
    public void testUnit() {
        assertEquals(Convert.Unit.fromString("trx"), (Convert.Unit.TRX));
        assertEquals(Convert.Unit.fromString("TRX"), (Convert.Unit.TRX));
        assertEquals(Convert.Unit.fromString("sun"), (Convert.Unit.SUN));
    }
}
