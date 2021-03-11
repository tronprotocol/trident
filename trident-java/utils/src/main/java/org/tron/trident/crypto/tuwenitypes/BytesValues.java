/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.tron.trident.crypto.tuwenitypes;

import static com.google.common.base.Preconditions.checkArgument;

final class BytesValues {
  private BytesValues() {}

  static final int MAX_UNSIGNED_SHORT = (1 << 16) - 1;
  static final long MAX_UNSIGNED_INT = (1L << 32) - 1;
  static final long MAX_UNSIGNED_LONG = Long.MAX_VALUE;

  static Bytes fromHexString(CharSequence str, int destSize, boolean lenient) {
    return Bytes.wrap(fromRawHexString(str, destSize, lenient));
  }

  static byte[] fromRawHexString(CharSequence str, int destSize, boolean lenient) {
    int len = str.length();
    CharSequence hex = str;
    if (len >= 2 && str.charAt(0) == '0' && str.charAt(1) == 'x') {
      hex = str.subSequence(2, len);
      len -= 2;
    }

    int idxShift = 0;
    if (len % 2 != 0) {
      if (!lenient) {
        throw new IllegalArgumentException("Invalid odd-length hex binary representation");
      }

      hex = "0" + hex;
      len += 1;
      idxShift = 1;
    }

    int size = len / 2;
    if (destSize < 0) {
      destSize = size;
    } else {
      checkArgument(size <= destSize, "Hex value is too large: expected at most %s bytes but got %s", destSize, size);
    }

    byte[] out = new byte[destSize];

    int destOffset = (destSize - size);
    for (int i = 0; i < len; i += 2) {
      int h = hexToBin(hex.charAt(i));
      int l = hexToBin(hex.charAt(i + 1));
      if (h == -1) {
        throw new IllegalArgumentException(
            String
                .format(
                    "Illegal character '%c' found at index %d in hex binary representation",
                    hex.charAt(i),
                    i - idxShift));
      }
      if (l == -1) {
        throw new IllegalArgumentException(
            String
                .format(
                    "Illegal character '%c' found at index %d in hex binary representation",
                    hex.charAt(i + 1),
                    i + 1 - idxShift));
      }

      out[destOffset + (i / 2)] = (byte) (h * 16 + l);
    }
    return out;
  }

  private static int hexToBin(char ch) {
    if ('0' <= ch && ch <= '9') {
      return ch - 48;
    } else if ('A' <= ch && ch <= 'F') {
      return ch - 65 + 10;
    } else {
      return 'a' <= ch && ch <= 'f' ? ch - 97 + 10 : -1;
    }
  }
}