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

import java.util.List;
import org.tron.trident.abi.Utils;

/**
 * Dynamic array type.
 */
public class DynamicArray<T extends Type> extends Array<T> {

  @Deprecated
  @SafeVarargs
  @SuppressWarnings({"unchecked"})
  public DynamicArray(T... values) {
    super(
            StructType.class.isAssignableFrom(values[0].getClass())
                || Array.class.isAssignableFrom(values[0].getClass())
                    ? (Class<T>) values[0].getClass()
                    : (Class<T>) AbiTypes.getType(values[0].getTypeAsString()),
            values);
  }

  @Deprecated
  @SuppressWarnings("unchecked")
  public DynamicArray(List<T> values) {
    super(
            StructType.class.isAssignableFrom(values.get(0).getClass())
                || Array.class.isAssignableFrom(values.get(0).getClass())
                    ? (Class<T>) values.get(0).getClass()
                    : (Class<T>) AbiTypes.getType(values.get(0).getTypeAsString()),
            values);
  }

  @Deprecated
  @SuppressWarnings("unchecked")
  private DynamicArray(String type) {
    super((Class<T>) AbiTypes.getType(type));
  }

  @Deprecated
  public static DynamicArray empty(String type) {
    return new DynamicArray(type);
  }

  public DynamicArray(Class<T> type, List<T> values) {
    super(type, values);
  }

  @Override
  public int bytes32PaddedLength() {
    return super.bytes32PaddedLength() + MAX_BYTE_LENGTH;
  }

  @SafeVarargs
  public DynamicArray(Class<T> type, T... values) {
    super(type, values);
  }

  @Override
  public String getTypeAsString() {
    String type;
    if (value.isEmpty()) {
      Class<T> componentType = getComponentType();
      boolean genericStruct =
          componentType == DynamicStruct.class || componentType == StaticStruct.class;
      boolean rawArrayType =
          componentType != null
              && Array.class.isAssignableFrom(componentType)
              && !StructType.class.isAssignableFrom(componentType);
      if (componentType == null || genericStruct || rawArrayType) {
        throw new UnsupportedOperationException(
            "Cannot determine type string for empty array of generic struct "
                + "or nested array type. Either construct DynamicArray with a "
                + "concrete element type, or compute the type string externally "
                + "from a TypeReference.");
      }
      if (StructType.class.isAssignableFrom(componentType)) {
        type = Utils.getStructType(componentType);
      } else {
        type = AbiTypes.getTypeAString(componentType);
      }
    } else {
      if (StructType.class.isAssignableFrom(value.get(0).getClass())) {
        type = value.get(0).getTypeAsString();
      } else if (Array.class.isAssignableFrom(value.get(0).getClass())) {
        type = value.get(0).getTypeAsString();
      } else {
        type = AbiTypes.getTypeAString(getComponentType());
      }
    }
    return type + "[]";
  }
}
