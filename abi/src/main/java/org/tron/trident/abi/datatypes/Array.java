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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.tron.trident.abi.Utils;

/**
 * Fixed size array.
 */
public abstract class Array<T extends Type> implements Type<List<T>> {

  private final Class<T> type;
  protected final List<T> value;

  @Deprecated
  @SafeVarargs
  Array(String type, T... values) {
    this(type, Arrays.asList(values));
  }

  @Deprecated
  @SuppressWarnings("unchecked")
  Array(String type, List<T> values) {
    this((Class<T>) AbiTypes.getType(type), values);
  }

  @Deprecated
  Array(String type) {
    this(type, new ArrayList<>());
  }

  @SafeVarargs
  Array(Class<T> type, T... values) {
    this(type, Arrays.asList(values));
  }

  Array(Class<T> type, List<T> values) {
    checkValid(type, values);

    this.type = type;
    this.value = values;
  }

  @Override
  public int bytes32PaddedLength() {
    int length = 0;
    for (T t : value) {
      int valueLength = t.bytes32PaddedLength();
      length += valueLength;
    }
    return length;
  }

  @Override
  public List<T> getValue() {
    return value;
  }

  public Class<T> getComponentType() {
    return type;
  }

  @Override
  public abstract String getTypeAsString();

  /**
   * Computes the Solidity type string of this array's elements; the dynamic and static
   * subclasses append their respective {@code []} / {@code [N]} suffix to it.
   */
  String getElementTypeAsString() {
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
                + "or nested array type. Either construct the array with a "
                + "concrete element type, or compute the type string externally "
                + "from a TypeReference.");
      }
      if (StructType.class.isAssignableFrom(componentType)) {
        return Utils.getStructType(componentType);
      }
      return AbiTypes.getTypeAString(componentType);
    }
    if (StructType.class.isAssignableFrom(value.get(0).getClass())
        || Array.class.isAssignableFrom(value.get(0).getClass())) {
      return value.get(0).getTypeAsString();
    }
    return AbiTypes.getTypeAString(getComponentType());
  }

  private void checkValid(Class<T> type, List<T> values) {
    Objects.requireNonNull(type);
    Objects.requireNonNull(values);
  }

  /**
   * Infers the component type from the first array element, for the deprecated subclass
   * constructors that take values without an explicit element class.
   */
  @SuppressWarnings("unchecked")
  static <T extends Type> Class<T> inferComponentType(T firstValue) {
    if (StructType.class.isAssignableFrom(firstValue.getClass())
        || Array.class.isAssignableFrom(firstValue.getClass())) {
      return (Class<T>) firstValue.getClass();
    }
    return (Class<T>) AbiTypes.getType(firstValue.getTypeAsString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Array<?> array = (Array<?>) o;

    if (!type.equals(array.type)) {
      return false;
    }
    return Objects.equals(value, array.value);
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }
}
