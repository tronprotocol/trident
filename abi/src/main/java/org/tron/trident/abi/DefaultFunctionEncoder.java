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

package org.tron.trident.abi;

import java.math.BigInteger;
import java.util.List;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.StaticArray;
import org.tron.trident.abi.datatypes.StaticStruct;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.abi.datatypes.Uint;

public class DefaultFunctionEncoder extends FunctionEncoder {

  @Override
  public String encodeFunction(final Function function) {
    final List<Type> parameters = function.getInputParameters();

    final String methodSignature = buildMethodSignature(function.getName(), parameters);
    final String methodId = buildMethodId(methodSignature);

    final StringBuilder result = new StringBuilder();
    result.append(methodId);

    return encodeParameters(parameters, result);
  }

  @Override
  public String encodeParameters(final List<Type> parameters) {
    return encodeParameters(parameters, new StringBuilder());
  }

  private static String encodeParameters(
      final List<Type> parameters, final StringBuilder result) {

    int dynamicDataOffset = getLength(parameters) * Type.MAX_BYTE_LENGTH;
    final StringBuilder dynamicData = new StringBuilder();

    for (Type parameter : parameters) {
      final String encodedValue = TypeEncoder.encode(parameter);

      if (TypeEncoder.isDynamic(parameter)) {
        final String encodedDataOffset =
            TypeEncoder.encodeNumeric(new Uint(BigInteger.valueOf(dynamicDataOffset)));
        result.append(encodedDataOffset);
        dynamicData.append(encodedValue);
        dynamicDataOffset += encodedValue.length() >> 1;
      } else {
        result.append(encodedValue);
      }
    }
    result.append(dynamicData);

    return result.toString();
  }

  /**
   * Encodes a function call including its method signature (selector) and its parameters.
   *
   * @param methodId   the 4-byte selector (8 hex chars)
   * @param parameters the list of parameters to encode
   * @return the complete ABI-encoded hex string representing the function call
   */
  public String encodeWithSelector(String methodId, List<Type> parameters) {
    final StringBuilder result = new StringBuilder(methodId);

    return encodeParameters(parameters, result);
  }

  /**
   * Encodes parameters using tight packing (abi.encodePacked).
   * This is a non-standard ABI encoding used primarily for computing hashes,
   * where padding is omitted and dynamic types are concatenated without length prefixes.
   *
   * @param parameters the list of parameters to pack
   * @return the packed ABI-encoded hex string
   */
  @Override
  protected String encodePackedParameters(List<Type> parameters) {
    final StringBuilder result = new StringBuilder();
    for (Type parameter : parameters) {
      result.append(TypeEncoder.encodePacked(parameter));
    }
    return result.toString();
  }

  /**
   * Calculates the length of the tuple head (in 32-byte slots) required for the given parameters.
   * Crucially, all dynamic types (including StaticArrays containing dynamic elements) always occupy exactly 
   * 1 slot in the head (for the offset pointer). Pure static arrays and structs are flattened to calculate 
   * their total inline slot requirement.
   *
   * @param parameters the list of types to be encoded.
   * @return the total number of 32-byte slots required for the tuple head.
   */
  @SuppressWarnings("unchecked")
  private static int getLength(final List<Type> parameters) {
    int count = 0;
    for (final Type type : parameters) {
      if (TypeEncoder.isDynamic(type)) {
        count++;
      } else if (type instanceof StaticArray || type instanceof StaticStruct) {
        count += type.bytes32PaddedLength() / Type.MAX_BYTE_LENGTH;
      } else {
        count++;
      }
    }
    return count;
  }
}
