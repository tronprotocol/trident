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

import static org.tron.trident.abi.TypeDecoder.MAX_BYTE_LENGTH_FOR_HEX_STRING;
import static org.tron.trident.abi.TypeDecoder.isDynamic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.tron.trident.abi.datatypes.Array;
import org.tron.trident.abi.datatypes.Bytes;
import org.tron.trident.abi.datatypes.BytesType;
import org.tron.trident.abi.datatypes.DynamicArray;
import org.tron.trident.abi.datatypes.DynamicStruct;
import org.tron.trident.abi.datatypes.StaticArray;
import org.tron.trident.abi.datatypes.StaticStruct;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.abi.datatypes.Utf8String;
import org.tron.trident.abi.datatypes.generated.Bytes32;
import org.tron.trident.utils.Numeric;
import org.tron.trident.utils.Strings;

/**
 * Ethereum Contract Application Binary Interface (ABI) decoding for functions. Further details are
 * available <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">here</a>.
 */
public class DefaultFunctionReturnDecoder extends FunctionReturnDecoder {

  public List<Type> decodeFunctionResult(
      String rawInput, List<TypeReference<Type>> outputParameters) {

    String input = Numeric.cleanHexPrefix(rawInput);

    if (Strings.isEmpty(input)) {
      return Collections.emptyList();
    } else {
      return build(input, outputParameters);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Type> Type decodeEventParameter(
      String rawInput, TypeReference<T> typeReference) {

    String input = Numeric.cleanHexPrefix(rawInput);

    try {
      Class<T> type = typeReference.getClassType();

      if (Bytes.class.isAssignableFrom(type)) {
        Class<Bytes> bytesClass = (Class<Bytes>) Class.forName(type.getName());
        return TypeDecoder.decodeBytes(input, bytesClass);
      } else if (Array.class.isAssignableFrom(type)
          || BytesType.class.isAssignableFrom(type)
          || Utf8String.class.isAssignableFrom(type)) {
        return TypeDecoder.decodeBytes(input, Bytes32.class);
      } else {
        return TypeDecoder.decode(input, type);
      }
    } catch (ClassNotFoundException e) {
      throw new UnsupportedOperationException("Invalid class reference provided", e);
    }
  }

  private static List<Type> build(String input, List<TypeReference<Type>> outputParameters) {
    // Reject cyclic or excessively nested TypeReference graphs up front; any
    // downstream recursion through subTypeReference / innerTypes is then bounded
    // by what passed validation here.
    for (TypeReference<?> typeReference : outputParameters) {
      Utils.validateTypeReferenceDepth(typeReference);
    }

    List<Type> results = new ArrayList<>(outputParameters.size());

    int offset = 0;
    for (TypeReference<?> typeReference : outputParameters) {
      try {
        int hexStringDataOffset = getDataOffset(input, offset, typeReference);

        @SuppressWarnings("unchecked")
        Class<Type> classType = (Class<Type>) typeReference.getClassType();

        Type result;
        if (DynamicStruct.class.isAssignableFrom(classType)) {
          result =
                  TypeDecoder.decodeDynamicStruct(
                          input, hexStringDataOffset, typeReference);
          offset += MAX_BYTE_LENGTH_FOR_HEX_STRING;

        } else if (DynamicArray.class.isAssignableFrom(classType)) {
          result =
                  TypeDecoder.decodeDynamicArray(
                          input, hexStringDataOffset, typeReference);
          offset += MAX_BYTE_LENGTH_FOR_HEX_STRING;

        } else if (StaticStruct.class.isAssignableFrom(classType)) {
          result =
                  TypeDecoder.decodeStaticStruct(
                          input, hexStringDataOffset, typeReference);
          offset += (result.bytes32PaddedLength() / Type.MAX_BYTE_LENGTH)
                          * MAX_BYTE_LENGTH_FOR_HEX_STRING;

        } else if (StaticArray.class.isAssignableFrom(classType)) {
          int length;
          if (typeReference instanceof TypeReference.StaticArrayTypeReference) {
            length = ((TypeReference.StaticArrayTypeReference) typeReference).getSize();
          } else {
            length = Utils.extractStaticArraySize(classType);
          }
          result =
                  TypeDecoder.decodeStaticArray(
                          input, hexStringDataOffset, typeReference, length);
          if (isDynamic(typeReference)) {
            offset += MAX_BYTE_LENGTH_FOR_HEX_STRING;
          } else {
            offset +=
                (result.bytes32PaddedLength() / Type.MAX_BYTE_LENGTH)
                    * MAX_BYTE_LENGTH_FOR_HEX_STRING;
          }
        } else {
          result = TypeDecoder.decode(input, hexStringDataOffset, classType);
          offset += MAX_BYTE_LENGTH_FOR_HEX_STRING;
        }
        results.add(result);

      } catch (ClassNotFoundException e) {
        throw new UnsupportedOperationException("Invalid class reference provided", e);
      }
    }
    return results;
  }

  public static <T extends Type> int getDataOffset(
          String input, int offset, TypeReference<?> typeReference)
          throws ClassNotFoundException {
    if (isDynamic(typeReference)) {
      return TypeDecoder.decodeUintAsInt(input, offset) << 1;
    } else {
      return offset;
    }
  }
}
