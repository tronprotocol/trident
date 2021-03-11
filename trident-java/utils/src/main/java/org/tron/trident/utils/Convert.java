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

/** Ethereum unit conversion functions. */
public final class Convert {
    private Convert() {}

    public static BigDecimal fromSun(String number, Unit unit) {
        return fromSun(new BigDecimal(number), unit);
    }

    public static BigDecimal fromSun(BigDecimal number, Unit unit) {
        return number.divide(unit.getSunFactor());
    }

    public static BigDecimal toSun(String number, Unit unit) {
        return toSun(new BigDecimal(number), unit);
    }

    public static BigDecimal toSun(BigDecimal number, Unit unit) {
        return number.multiply(unit.getSunFactor());
    }

    public enum Unit {
        SUN("sun", 0),
        TRX("trx", 6);

        private String name;
        private BigDecimal sunFactor;

        Unit(String name, int factor) {
            this.name = name;
            this.sunFactor = BigDecimal.TEN.pow(factor);
        }

        public BigDecimal getSunFactor() {
            return sunFactor;
        }

        @Override
        public String toString() {
            return name;
        }

        public static Unit fromString(String name) {
            if (name != null) {
                for (Unit unit : Unit.values()) {
                    if (name.equalsIgnoreCase(unit.name)) {
                        return unit;
                    }
                }
            }
            return Unit.valueOf(name);
        }
    }
}
