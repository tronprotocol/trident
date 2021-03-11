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

import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;

public class MutableByteBufferWrappingBytes extends ByteBufferWrappingBytes implements MutableBytes {

  MutableByteBufferWrappingBytes(ByteBuffer byteBuffer) {
    super(byteBuffer);
  }

  MutableByteBufferWrappingBytes(ByteBuffer byteBuffer, int offset, int length) {
    super(byteBuffer, offset, length);
  }

  @Override
  public void setInt(int i, int value) {
    byteBuffer.putInt(offset + i, value);
  }

  @Override
  public void setLong(int i, long value) {
    byteBuffer.putLong(offset + i, value);
  }

  @Override
  public void set(int i, byte b) {
    byteBuffer.put(offset + i, b);
  }

  @Override
  public MutableBytes mutableSlice(int i, int length) {
    if (i == 0 && length == this.length) {
      return this;
    }
    if (length == 0) {
      return MutableBytes.EMPTY;
    }

    checkElementIndex(i, this.length);
    checkArgument(
        i + length <= this.length,
        "Provided length %s is too big: the value has size %s and has only %s bytes from %s",
        length,
        this.length,
        this.length - i,
        i);

    return new MutableByteBufferWrappingBytes(byteBuffer, offset + i, length);
  }

  @Override
  public Bytes copy() {
    return new ArrayWrappingBytes(toArray());
  }
}