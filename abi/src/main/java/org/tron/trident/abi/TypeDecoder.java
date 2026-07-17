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

import static org.tron.trident.abi.DefaultFunctionReturnDecoder.getDataOffset;
import static org.tron.trident.abi.TypeReference.makeTypeReference;
import static org.tron.trident.abi.Utils.findStructConstructor;
import static org.tron.trident.abi.Utils.getSimpleTypeName;
import static org.tron.trident.abi.Utils.staticStructNestedPublicFieldsFlatList;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import org.tron.trident.abi.datatypes.AbiTypes;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Array;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Bytes;
import org.tron.trident.abi.datatypes.BytesType;
import org.tron.trident.abi.datatypes.DynamicArray;
import org.tron.trident.abi.datatypes.DynamicBytes;
import org.tron.trident.abi.datatypes.DynamicStruct;
import org.tron.trident.abi.datatypes.Fixed;
import org.tron.trident.abi.datatypes.FixedPointType;
import org.tron.trident.abi.datatypes.Int;
import org.tron.trident.abi.datatypes.IntType;
import org.tron.trident.abi.datatypes.NumericType;
import org.tron.trident.abi.datatypes.StaticArray;
import org.tron.trident.abi.datatypes.StaticStruct;
import org.tron.trident.abi.datatypes.StructType;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.abi.datatypes.Ufixed;
import org.tron.trident.abi.datatypes.Uint;
import org.tron.trident.abi.datatypes.Utf8String;
import org.tron.trident.abi.datatypes.generated.Uint160;
import org.tron.trident.abi.datatypes.primitive.Double;
import org.tron.trident.abi.datatypes.primitive.Float;
import org.tron.trident.utils.Numeric;

/**
 * Ethereum Contract Application Binary Interface (ABI) decoding for types. Decoding is not
 * documented, but is the reverse of the encoding details located <a
 * href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">here</a>.
 *
 * <p>The public API is composed of "decode*" methods and provides backward-compatibility. See
 * https://github.com/hyperledger/web3j/issues/1591 for a discussion about decoding and possible
 * improvements.
 */
public class TypeDecoder {

  static final int MAX_BYTE_LENGTH_FOR_HEX_STRING = Type.MAX_BYTE_LENGTH << 1;

  public static Type instantiateType(String solidityType, Object value)
      throws InvocationTargetException,
      NoSuchMethodException,
      InstantiationException,
      IllegalAccessException,
      ClassNotFoundException {
    return instantiateType(makeTypeReference(solidityType), value);
  }

  public static Type instantiateType(TypeReference ref, Object value)
      throws NoSuchMethodException,
      IllegalAccessException,
      InvocationTargetException,
      InstantiationException,
      ClassNotFoundException {
    Class rc = ref.getClassType();
    if (Array.class.isAssignableFrom(rc)) {
      return instantiateArrayType(ref, value);
    }
    return instantiateAtomicType(rc, value);
  }

  /**
   * Broken as inherited from web3j — every call throws. {@code getRawType().getClass()}
   * below always yields {@code java.lang.Class} (the class of the Class object, not the
   * raw type itself), so both {@code isAssignableFrom} branches are unreachable and any
   * input falls through to {@code UnsupportedOperationException}. Nothing in this SDK
   * calls this overload; use {@link #decodeStaticArray} / {@link #decodeDynamicArray}
   * directly instead. Kept unfixed to stay aligned with upstream; the fix would be
   * {@code (Class) ...getRawType()} plus end-to-end verification of the StaticArray
   * branch (note its hardcoded length argument of 1).
   */
  public static <T extends Array> T decode(
      String input, int offset, TypeReference<T> typeReference) {
    Class cls = ((ParameterizedType) typeReference.getType()).getRawType().getClass();
    if (StaticArray.class.isAssignableFrom(cls)) {
      return decodeStaticArray(input, offset, typeReference, 1);
    } else if (DynamicArray.class.isAssignableFrom(cls)) {
      return decodeDynamicArray(input, offset, typeReference);
    } else {
      throw new UnsupportedOperationException(
          "Unsupported TypeReference: "
              + cls.getName()
              + ", only Array types can be passed as TypeReferences");
    }
  }

  @SuppressWarnings("unchecked")
  static <T extends Type> T decode(String input, int offset, Class<T> type) {
    if (NumericType.class.isAssignableFrom(type)) {
      return (T) decodeNumeric(
          input.substring(
              offset, Math.min(input.length(), offset + MAX_BYTE_LENGTH_FOR_HEX_STRING)),
          (Class<NumericType>) type);
    } else if (Address.class.isAssignableFrom(type)) {
      return (T) decodeAddress(
          input.substring(
              offset, Math.min(input.length(), offset + MAX_BYTE_LENGTH_FOR_HEX_STRING)));
    } else if (Bool.class.isAssignableFrom(type)) {
      return (T) decodeBool(input, offset);
    } else if (Bytes.class.isAssignableFrom(type)) {
      return (T) decodeBytes(input, offset, (Class<Bytes>) type);
    } else if (DynamicBytes.class.isAssignableFrom(type)) {
      return (T) decodeDynamicBytes(input, offset);
    } else if (Utf8String.class.isAssignableFrom(type)) {
      return (T) decodeUtf8String(input, offset);
    } else if (Array.class.isAssignableFrom(type)) {
      throw new UnsupportedOperationException(
          "Array types must be wrapped in a TypeReference");
    } else {
      throw new UnsupportedOperationException("Type cannot be encoded: " + type.getClass());
    }
  }

  static <T extends Type> T decode(String input, Class<T> type) {
    return decode(input, 0, type);
  }

  public static <T extends Type> T decode(String input, TypeReference<?> type)
      throws ClassNotFoundException {
    return decode(input, 0, ((TypeReference<T>) type).getClassType());
  }

  public static Address decodeAddress(String input) {
    return new Address(decodeNumeric(input, Uint160.class));
  }

  public static <T extends NumericType> T decodeNumeric(String input, Class<T> type) {
    try {
      byte[] inputByteArray = Numeric.hexStringToByteArray(input);
      int typeLengthAsBytes = getTypeLengthInBytes(type);
      int valueOffset = Type.MAX_BYTE_LENGTH - typeLengthAsBytes;
      if (inputByteArray.length < Type.MAX_BYTE_LENGTH) {
        throw new IndexOutOfBoundsException(
            "Input is too short to decode " + type.getSimpleName() + ": needs "
                + Type.MAX_BYTE_LENGTH + " bytes, found "
                + inputByteArray.length);
      }
      byte[] slice =
          Arrays.copyOfRange(inputByteArray, valueOffset, Type.MAX_BYTE_LENGTH);

      BigInteger numericValue;
      if (Uint.class.isAssignableFrom(type) || Ufixed.class.isAssignableFrom(type)) {
        numericValue = new BigInteger(1, slice);
      } else {
        numericValue = new BigInteger(slice);
      }
      return type.getConstructor(BigInteger.class).newInstance(numericValue);

    } catch (NoSuchMethodException
             | SecurityException
             | InstantiationException
             | IllegalAccessException
             | IllegalArgumentException
             | InvocationTargetException e) {
      throw new UnsupportedOperationException(
          "Unable to create instance of " + type.getName(), e);
    }
  }

  static <T extends NumericType> int getTypeLengthInBytes(Class<T> type) {
    return getTypeLength(type) >> 3; // divide by 8
  }

  static <T extends NumericType> int getTypeLength(Class<T> type) {
    if (IntType.class.isAssignableFrom(type)) {
      String regex = "(" + Uint.class.getSimpleName() + "|" + Int.class.getSimpleName() + ")";
      String[] splitName = type.getSimpleName().split(regex);
      if (splitName.length == 2) {
        return Integer.parseInt(splitName[1]);
      }
    } else if (FixedPointType.class.isAssignableFrom(type)) {
      String regex =
          "(" + Ufixed.class.getSimpleName() + "|" + Fixed.class.getSimpleName() + ")";
      String[] splitName = type.getSimpleName().split(regex);
      if (splitName.length == 2) {
        String[] bitsCounts = splitName[1].split("x");
        return Integer.parseInt(bitsCounts[0]) + Integer.parseInt(bitsCounts[1]);
      }
    }
    return Type.MAX_BIT_LENGTH;
  }

  static Type instantiateArrayType(TypeReference ref, Object value)
      throws NoSuchMethodException,
      IllegalAccessException,
      InvocationTargetException,
      InstantiationException,
      ClassNotFoundException {
    List values;
    if (value instanceof List) {
      values = (List) value;
    } else if (value.getClass().isArray()) {
      values = arrayToList(value);
    } else {
      throw new ClassCastException(
          "Arg of type "
              + value.getClass()
              + " should be a list to instantiate trident Array");
    }
    Constructor listcons;
    int arraySize =
        ref instanceof TypeReference.StaticArrayTypeReference
            ? ((TypeReference.StaticArrayTypeReference) ref).getSize()
            : -1;
    if (arraySize < 0) {
      listcons = DynamicArray.class.getConstructor(Class.class, List.class);
    } else {
      Class<?> arrayClass =
          Class.forName("org.tron.trident.abi.datatypes.generated.StaticArray" + arraySize);
      listcons = arrayClass.getConstructor(Class.class, List.class);
    }
    // create a list of arguments coerced to the correct type of sub-TypeReference
    ArrayList<Type> transformedList = new ArrayList<Type>(values.size());
    TypeReference subTypeReference = ref.getSubTypeReference();
    for (Object o : values) {
      transformedList.add(instantiateType(subTypeReference, o));
    }
    return (Type) listcons.newInstance(subTypeReference.getClassType(), transformedList);
  }

  static Type instantiateAtomicType(Class<?> referenceClass, Object value)
      throws NoSuchMethodException,
      IllegalAccessException,
      InvocationTargetException,
      InstantiationException,
      ClassNotFoundException {
    Object constructorArg = null;
    if (NumericType.class.isAssignableFrom(referenceClass)) {
      constructorArg = asBigInteger(value);
    } else if (BytesType.class.isAssignableFrom(referenceClass)) {
      if (value instanceof byte[]) {
        constructorArg = value;
      } else if (value instanceof BigInteger) {
        constructorArg = ((BigInteger) value).toByteArray();
      } else if (value instanceof String) {
        constructorArg = Numeric.hexStringToByteArray((String) value);
      }
    } else if (Utf8String.class.isAssignableFrom(referenceClass)) {
      constructorArg = value.toString();
    } else if (Address.class.isAssignableFrom(referenceClass)) {
      if (value instanceof BigInteger || value instanceof Uint160) {
        constructorArg = value;
      } else {
        constructorArg = value.toString();
      }
    } else if (Bool.class.isAssignableFrom(referenceClass)) {
      if (value instanceof Boolean) {
        constructorArg = value;
      } else {
        BigInteger bival = asBigInteger(value);
        constructorArg = bival == null ? null : !bival.equals(BigInteger.ZERO);
      }
    }
    if (constructorArg == null) {
      throw new InstantiationException(
          "Could not create type "
              + referenceClass
              + " from arg "
              + value.toString()
              + " of type "
              + value.getClass());
    }
    Class<?>[] types = new Class[] {constructorArg.getClass()};
    Constructor cons = referenceClass.getConstructor(types);
    return (Type) cons.newInstance(constructorArg);
  }

  @SuppressWarnings("unchecked")
  static <T extends Type> int getSingleElementLength(String input, int offset, Class<T> type) {
    if (input.length() == offset) {
      return 0;
    } else if (DynamicBytes.class.isAssignableFrom(type)
        || Utf8String.class.isAssignableFrom(type)) {
      // length field + data value
      return (decodeUintAsInt(input, offset) / Type.MAX_BYTE_LENGTH) + 2;
    } else if (StaticStruct.class.isAssignableFrom(type)) {
      return staticStructNestedPublicFieldsFlatList((Class<Type>) type).size();
    } else {
      return 1;
    }
  }

  static int decodeUintAsInt(String rawInput, int offset) {
    String input = rawInput.substring(offset, offset + MAX_BYTE_LENGTH_FOR_HEX_STRING);
    return decode(input, 0, Uint.class).getValue().intValue();
  }

  public static Bool decodeBool(String rawInput, int offset) {
    String input = rawInput.substring(offset, offset + MAX_BYTE_LENGTH_FOR_HEX_STRING);
    BigInteger numericValue = Numeric.toBigInt(input);
    boolean value = numericValue.equals(BigInteger.ONE);
    return new Bool(value);
  }

  public static <T extends Bytes> T decodeBytes(String input, Class<T> type) {
    return decodeBytes(input, 0, type);
  }

  public static <T extends Bytes> T decodeBytes(String input, int offset, Class<T> type) {
    try {
      String simpleName = type.getSimpleName();
      String[] splitName = simpleName.split(Bytes.class.getSimpleName());
      int length = Integer.parseInt(splitName[1]);
      int hexStringLength = length << 1;

      byte[] bytes =
          Numeric.hexStringToByteArray(input.substring(offset, offset + hexStringLength));
      return type.getConstructor(byte[].class).newInstance(bytes);
    } catch (NoSuchMethodException
             | SecurityException
             | InstantiationException
             | IllegalAccessException
             | IllegalArgumentException
             | InvocationTargetException e) {
      throw new UnsupportedOperationException(
          "Unable to create instance of " + type.getName(), e);
    }
  }

  public static DynamicBytes decodeDynamicBytes(String input, int offset) {
    int encodedLength = decodeUintAsInt(input, offset);
    int hexStringEncodedLength = encodedLength << 1;

    int valueOffset = offset + MAX_BYTE_LENGTH_FOR_HEX_STRING;

    String data = input.substring(valueOffset, valueOffset + hexStringEncodedLength);
    byte[] bytes = Numeric.hexStringToByteArray(data);

    return new DynamicBytes(bytes);
  }

  public static Utf8String decodeUtf8String(String input, int offset) {
    DynamicBytes dynamicBytesResult = decodeDynamicBytes(input, offset);
    byte[] bytes = dynamicBytesResult.getValue();

    return new Utf8String(new String(bytes, StandardCharsets.UTF_8));
  }

  /**
   * Static array length cannot be passed as a type.
   */
  @SuppressWarnings("unchecked")
  static <T extends Type> T decodeStaticArray(
      String input, int offset, TypeReference<T> typeReference, int length) {

    BiFunction<List<T>, String, T> function =
        (elements, typeName) -> {
          if (elements.isEmpty()) {
            throw new UnsupportedOperationException(
                "Zero length fixed array is invalid type");
          } else {
            return instantiateStaticArray(elements, length);
          }
        };

    return decodeArrayElements(input, offset, typeReference, length, function);
  }

  public static <T extends Type> T decodeStaticStruct(
      final String input, final int offset, final TypeReference<T> typeReference) {
    BiFunction<List<T>, String, T> function =
        (elements, typeName) -> {
          if (elements.isEmpty()) {
            throw new UnsupportedOperationException(
                "Zero length fixed array is invalid type");
          } else {
            return instantiateStruct(typeReference, elements);
          }
        };

    if (typeReference.getInnerTypes() != null) {
      return decodeStaticStructElementFromInnerTypes(input, offset, typeReference, function);
    }

    return decodeStaticStructElement(input, offset, typeReference, function);
  }

  private static int extractStaticArrayLength(TypeReference<?> typeReference) {
    if (typeReference instanceof TypeReference.StaticArrayTypeReference) {
      return ((TypeReference.StaticArrayTypeReference<?>) typeReference).getSize();
    }
    try {
      return Utils.extractStaticArraySize(typeReference.getClassType());
    } catch (Exception e) {
      throw new UnsupportedOperationException(
          "Cannot determine StaticArray length from " + typeReference.getType(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T extends Type> T decodeStaticStructElementFromInnerTypes(
      final String input,
      final int offset,
      final TypeReference<T> typeReference,
      final BiFunction<List<T>, String, T> consumer) {
    try {
      final List<TypeReference<?>> innerTypes = typeReference.getInnerTypes();
      List<T> elements = new ArrayList<>(innerTypes.size());

      for (int i = 0, currOffset = offset; i < innerTypes.size(); i++) {
        T value;
        final TypeReference<T> innerType = (TypeReference<T>) innerTypes.get(i);
        final Class<T> declaredField = innerType.getClassType();

        if (StaticStruct.class.isAssignableFrom(declaredField)) {
          value = decodeStaticStruct(input, currOffset, innerType);
          currOffset += (value.bytes32PaddedLength() / Type.MAX_BYTE_LENGTH)
              * MAX_BYTE_LENGTH_FOR_HEX_STRING;
        } else if (StaticArray.class.isAssignableFrom(declaredField)) {
          int staticLength = extractStaticArrayLength(innerType);
          value = (T) decodeStaticArray(input, currOffset, innerType, staticLength);
          currOffset += (value.bytes32PaddedLength() / Type.MAX_BYTE_LENGTH)
              * MAX_BYTE_LENGTH_FOR_HEX_STRING;
        } else {
          value = decode(input.substring(currOffset, currOffset + 64), 0, declaredField);
          currOffset += MAX_BYTE_LENGTH_FOR_HEX_STRING;
        }
        elements.add(value);
      }

      return consumer.apply(elements, getSimpleTypeName(typeReference.getClassType()));
    } catch (ClassNotFoundException e) {
      throw new UnsupportedOperationException(
          "Unable to access parameterized type "
              + Utils.getTypeName(typeReference.getType()),
          e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T extends Type> T decodeStaticStructElement(
      final String input,
      final int offset,
      final TypeReference<T> typeReference,
      final BiFunction<List<T>, String, T> consumer) {
    try {
      Class<T> classType = typeReference.getClassType();
      Constructor<?> constructor = findStructConstructor(classType);
      final int length = constructor.getParameterCount();
      List<T> elements = new ArrayList<>(length);

      for (int i = 0, currOffset = offset; i < length; i++) {
        T value;
        final Class<T> declaredField = (Class<T>) constructor.getParameterTypes()[i];

        if (StaticStruct.class.isAssignableFrom(declaredField)) {
          value = decodeStaticStruct(input, currOffset, TypeReference.create(declaredField));
          currOffset += value.bytes32PaddedLength() * 2;
        } else if (StaticArray.class.isAssignableFrom(declaredField)) {
          value = decodeStaticArrayStructField(
              input, currOffset, classType, constructor, i, declaredField);
          currOffset += value.bytes32PaddedLength() * 2;
        } else {
          value = decode(input.substring(currOffset, currOffset + 64), 0, declaredField);
          currOffset += 64;
        }
        elements.add(value);
      }

      String typeName = getSimpleTypeName(classType);

      return consumer.apply(elements, typeName);
    } catch (ClassNotFoundException e) {
      throw new UnsupportedOperationException(
          "Unable to access parameterized type "
              + Utils.getTypeName(typeReference.getType()),
          e);
    }
  }

  /**
   * Decodes a {@code StaticArrayN}-typed struct field on the constructor-reflection
   * path. The element type comes from the {@code @Parameterized} annotation on the
   * constructor parameter — the same contract signature generation
   * ({@code Utils.getStructType}) relies on.
   */
  @SuppressWarnings("unchecked")
  private static <T extends Type> T decodeStaticArrayStructField(
      final String input,
      final int offset,
      final Class<?> structClass,
      final Constructor<?> constructor,
      final int parameterIndex,
      final Class<T> declaredField) throws ClassNotFoundException {
    final Class<T> elementType =
        Utils.extractParameterFromAnnotation(
            constructor.getParameterAnnotations()[parameterIndex]);
    if (elementType == null) {
      throw new UnsupportedOperationException(
          "Array-typed struct field " + declaredField.getSimpleName()
              + " of " + structClass.getName()
              + " requires the @Parameterized annotation on the constructor parameter"
              + " to declare its element type");
    }
    if (isDynamic(elementType)) {
      throw new UnsupportedOperationException(
          "Field " + declaredField.getSimpleName() + " of " + structClass.getName()
              + " is a static array of dynamic elements, which makes the struct ABI-dynamic;"
              + " the struct class must extend DynamicStruct, not StaticStruct");
    }
    TypeReference<T> arrayRef =
        (TypeReference<T>)
            Utils.getTypeReferenceForParameterizedField(declaredField, elementType);
    int size = ((TypeReference.StaticArrayTypeReference) arrayRef).getSize();
    return decodeStaticArray(input, offset, arrayRef, size);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Type> T instantiateStruct(
      final TypeReference<T> typeReference, final List<T> parameters) {
    try {
      Class<T> classType = typeReference.getClassType();
      if (classType.isAssignableFrom(DynamicStruct.class)) {
        return (T) new DynamicStruct((List<Type>) parameters);
      } else if (classType.isAssignableFrom(StaticStruct.class)) {
        return (T) new StaticStruct((List<Type>) parameters);
      } else {
        Constructor ctor = findStructConstructor(classType);
        ctor.setAccessible(true);
        return (T) ctor.newInstance(parameters.toArray());
      }
    } catch (ReflectiveOperationException e) {
      throw new UnsupportedOperationException(
          "Constructor cannot accept" + Arrays.toString(parameters.toArray()), e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends Type> T decodeDynamicArray(
      String input, int offset, TypeReference<T> typeReference) {

    int length = decodeUintAsInt(input, offset);

    BiFunction<List<T>, String, T> function =
        (elements, typeName) -> (T) new DynamicArray(AbiTypes.getType(typeName), elements);

    int valueOffset = offset + MAX_BYTE_LENGTH_FOR_HEX_STRING;

    return decodeArrayElements(input, valueOffset, typeReference, length, function);
  }

  public static <T extends Type> T decodeDynamicStruct(
      String input, int offset, TypeReference<T> typeReference)
      throws ClassNotFoundException {

    BiFunction<List<T>, String, T> function =
        (elements, typeName) -> {
          if (elements.isEmpty()) {
            throw new UnsupportedOperationException(
                "Zero length fixed array is invalid type");
          }
          return instantiateStruct(typeReference, elements);
        };

    if (typeReference.getClassType().isAssignableFrom(DynamicStruct.class)
        && typeReference.getInnerTypes() != null) {
      return decodeDynamicStructElementsFromInnerTypes(
          input, offset, typeReference, function);
    }

    return decodeDynamicStructElements(input, offset, typeReference, function);
  }

  private static class ParameterOffsetTracker<T extends Type> {
    public final Map<Integer, T> parameters;
    public final List<Integer> parameterOffsets;
    public int staticOffset;
    public int dynamicParametersToProcess;

    ParameterOffsetTracker(
        final Map<Integer, T> parametersIn,
        final List<Integer> parameterOffsetsIn,
        int staticOffsetIn,
        int dynamicParametersToProcessIn) {
      this.parameters = parametersIn;
      this.parameterOffsets = parameterOffsetsIn;
      this.staticOffset = staticOffsetIn;
      this.dynamicParametersToProcess = dynamicParametersToProcessIn;
    }
  }

  private static <T extends Type>
      ParameterOffsetTracker<T> getDynamicOffsetsAndNonDynamicParameters(
        final String input, final int offset, final TypeReference<T> typeReference)
        throws ClassNotFoundException {
    ParameterOffsetTracker<T> tracker =
        new ParameterOffsetTracker<T>(new HashMap<>(), new ArrayList<>(), 0, 0);

    final List<TypeReference<?>> innerTypes = typeReference.getInnerTypes();
    for (int i = 0; i < innerTypes.size(); ++i) {
      final TypeReference<T> innerType = (TypeReference<T>) innerTypes.get(i);
      final Class<T> declaredField = innerType.getClassType();
      final T value;
      final int beginIndex = offset + tracker.staticOffset;
      if (isDynamic(innerType)) {
        final int parameterOffset =
            decodeDynamicStructDynamicParameterOffset(
                input.substring(beginIndex, beginIndex + 64))
                + offset;
        tracker.parameterOffsets.add(parameterOffset);
        tracker.staticOffset += 64;
        tracker.dynamicParametersToProcess += 1;
      } else {
        if (StaticStruct.class.isAssignableFrom(declaredField)) {
          value = decodeStaticStruct(input, beginIndex, innerType);
          tracker.staticOffset += (value.bytes32PaddedLength() / Type.MAX_BYTE_LENGTH)
              * MAX_BYTE_LENGTH_FOR_HEX_STRING;
        } else if (StaticArray.class.isAssignableFrom(declaredField)) {
          int staticLength = extractStaticArrayLength(innerType);
          value = (T) decodeStaticArray(input, beginIndex, innerType, staticLength);
          tracker.staticOffset += (value.bytes32PaddedLength() / Type.MAX_BYTE_LENGTH)
              * MAX_BYTE_LENGTH_FOR_HEX_STRING;
        } else {
          value = decode(input, beginIndex, declaredField);
          tracker.staticOffset += value.bytes32PaddedLength() * 2;
        }
        tracker.parameters.put(i, value);
      }
    }

    return tracker;
  }

  private static <T extends Type> List<T> getDynamicParametersWithTracker(
      final String input,
      final TypeReference<T> typeReference,
      final ParameterOffsetTracker<T> tracker)
      throws ClassNotFoundException {

    final List<TypeReference<?>> innerTypes = typeReference.getInnerTypes();
    int dynamicParametersProcessed = 0;
    for (int i = 0; i < innerTypes.size(); ++i) {
      final TypeReference<T> parameterTypeReference = (TypeReference<T>) innerTypes.get(i);
      if (isDynamic(parameterTypeReference)) {
        final boolean isLastParameterInStruct =
            dynamicParametersProcessed == (tracker.dynamicParametersToProcess - 1);
        final int parameterLength =
            isLastParameterInStruct
                ? input.length()
                - tracker.parameterOffsets.get(dynamicParametersProcessed)
                : tracker.parameterOffsets.get(dynamicParametersProcessed + 1)
                    - tracker.parameterOffsets.get(dynamicParametersProcessed);

        tracker.parameters.put(
            i,
            decodeDynamicParameterFromStructWithTypeReference(
                input,
                tracker.parameterOffsets.get(dynamicParametersProcessed),
                parameterLength,
                parameterTypeReference));
        dynamicParametersProcessed++;
      }
    }

    final List<T> elements = new ArrayList<>();
    for (int i = 0; i < innerTypes.size(); ++i) {
      elements.add(tracker.parameters.get(i));
    }

    return elements;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Type> T decodeDynamicStructElementsFromInnerTypes(
      final String input,
      final int offset,
      final TypeReference<T> typeReference,
      final BiFunction<List<T>, String, T> consumer)
      throws ClassNotFoundException {
    ParameterOffsetTracker<T> tracker =
        getDynamicOffsetsAndNonDynamicParameters(input, offset, typeReference);
    final List<T> parameters = getDynamicParametersWithTracker(input, typeReference, tracker);
    String typeName = getSimpleTypeName(typeReference.getClassType());
    return consumer.apply(parameters, typeName);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Type> T decodeDynamicStructElements(
      final String input,
      final int offset,
      final TypeReference<T> typeReference,
      final BiFunction<List<T>, String, T> consumer) {
    try {
      final Class<T> classType = typeReference.getClassType();
      Constructor<?> constructor = findStructConstructor(classType);
      final int length = constructor.getParameterCount();
      final Map<Integer, T> parameters = new HashMap<>();
      int staticOffset = 0;
      final List<Integer> parameterOffsets = new ArrayList<>();
      for (int i = 0; i < length; ++i) {
        final Class<T> declaredField = (Class<T>) constructor.getParameterTypes()[i];
        final T value;
        final int beginIndex = offset + staticOffset;
        if (isDynamicStructField(constructor, i)) {
          final int parameterOffset =
              decodeDynamicStructDynamicParameterOffset(
                  input.substring(beginIndex, beginIndex + 64))
                  + offset;
          parameterOffsets.add(parameterOffset);
          staticOffset += 64;
        } else {
          if (StaticStruct.class.isAssignableFrom(declaredField)) {
            value =
                decodeStaticStruct(
                    input,
                    beginIndex,
                    TypeReference.create(declaredField));
            staticOffset += value.bytes32PaddedLength() * 2;
          } else if (StaticArray.class.isAssignableFrom(declaredField)) {
            value = decodeStaticArrayStructField(
                input, beginIndex, classType, constructor, i, declaredField);
            staticOffset += value.bytes32PaddedLength() * 2;
          } else {
            value = decode(input, beginIndex, declaredField);
            staticOffset += value.bytes32PaddedLength() * 2;
          }
          parameters.put(i, value);
        }
      }
      int dynamicParametersProcessed = 0;
      int dynamicParametersToProcess =
          getDynamicStructDynamicParametersCount(constructor);
      for (int i = 0; i < length; ++i) {
        final Class<T> declaredField = (Class<T>) constructor.getParameterTypes()[i];
        if (isDynamicStructField(constructor, i)) {
          final boolean isLastParameterInStruct =
              dynamicParametersProcessed == (dynamicParametersToProcess - 1);
          final int parameterLength =
              isLastParameterInStruct
                  ? input.length()
                  - parameterOffsets.get(dynamicParametersProcessed)
                  : parameterOffsets.get(dynamicParametersProcessed + 1)
                      - parameterOffsets.get(dynamicParametersProcessed);
          final Class<T> parameterFromAnnotation =
              Utils.extractParameterFromAnnotation(
                  constructor.getParameterAnnotations()[i]);
          parameters.put(
              i,
              decodeDynamicParameterFromStruct(
                  input,
                  parameterOffsets.get(dynamicParametersProcessed),
                  parameterLength,
                  declaredField,
                  parameterFromAnnotation));
          dynamicParametersProcessed++;
        }
      }

      String typeName = getSimpleTypeName(classType);

      final List<T> elements = new ArrayList<>();
      for (int i = 0; i < length; ++i) {
        elements.add(parameters.get(i));
      }

      return consumer.apply(elements, typeName);
    } catch (ClassNotFoundException e) {
      throw new UnsupportedOperationException(
          "Unable to access parameterized type "
              + Utils.getTypeName(typeReference.getType()),
          e);
    }
  }

  private static int getDynamicStructDynamicParametersCount(final Constructor<?> constructor) {
    int count = 0;
    for (int i = 0; i < constructor.getParameterCount(); i++) {
      if (isDynamicStructField(constructor, i)) {
        count++;
      }
    }
    return count;
  }

  /**
   * ABI-dynamic check for a constructor-reflected struct field. A field is dynamic if its
   * declared class is dynamic, or if it is a {@code StaticArrayN} whose {@code @Parameterized}
   * element type is dynamic (e.g. {@code string[2]}) — such arrays are encoded with a head
   * offset and tail data, exactly like any other dynamic field.
   */
  @SuppressWarnings("unchecked")
  private static boolean isDynamicStructField(
      final Constructor<?> constructor, final int parameterIndex) {
    final Class<Type> declaredField =
        (Class<Type>) constructor.getParameterTypes()[parameterIndex];
    if (isDynamic(declaredField)) {
      return true;
    }
    // Struct classes extend StaticArray for encoding purposes but are not arrays;
    // a StaticStruct field is static by definition.
    if (StaticArray.class.isAssignableFrom(declaredField)
        && !StructType.class.isAssignableFrom(declaredField)) {
      final Class<Type> elementType =
          Utils.extractParameterFromAnnotation(
              constructor.getParameterAnnotations()[parameterIndex]);
      return elementType != null && isDynamic(elementType);
    }
    return false;
  }

  private static <T extends Type> T decodeDynamicParameterFromStruct(
      final String input,
      final int parameterOffset,
      final int parameterLength,
      final Class<T> declaredField,
      final Class<T> parameter)
      throws ClassNotFoundException {
    final String dynamicElementData =
        input.substring(parameterOffset, parameterOffset + parameterLength);

    final T value;
    if (DynamicStruct.class.isAssignableFrom(declaredField)) {
      value = decodeDynamicStruct(dynamicElementData, 0, TypeReference.create(declaredField));
    } else if (DynamicArray.class.isAssignableFrom(declaredField)) {
      if (parameter == null) {
        throw new RuntimeException(
            "parameter can not be null, try to use annotation @Parameterized "
                + "to specify the parameter type");
      }
      value =
          (T)
              decodeDynamicArray(
                  dynamicElementData,
                  0,
                  // Same shape as getDynamicArrayTypeReference for a DynamicArray field,
                  // but routed through the nested-array rejection guard.
                  Utils.getTypeReferenceForParameterizedField(declaredField, parameter));
    } else if (StaticArray.class.isAssignableFrom(declaredField)) {
      // StaticArrayN of dynamic elements (e.g. string[2]): ABI-dynamic, decoded from its
      // tail data. isDynamicStructField only routes here when @Parameterized is present.
      final TypeReference<T> arrayRef =
          (TypeReference<T>)
              Utils.getTypeReferenceForParameterizedField(declaredField, parameter);
      final int size = ((TypeReference.StaticArrayTypeReference) arrayRef).getSize();
      value = decodeStaticArray(dynamicElementData, 0, arrayRef, size);
    } else {
      value = decode(dynamicElementData, declaredField);
    }
    return value;
  }

  private static <T extends Type> T decodeDynamicParameterFromStructWithTypeReference(
      final String input,
      final int parameterOffset,
      final int parameterLength,
      final TypeReference<T> parameterTypeReference)
      throws ClassNotFoundException {
    final String dynamicElementData =
        input.substring(parameterOffset, parameterOffset + parameterLength);
    final Class<T> declaredField = parameterTypeReference.getClassType();

    final T value;
    if (DynamicStruct.class.isAssignableFrom(declaredField)) {
      value = decodeDynamicStruct(dynamicElementData, 0, parameterTypeReference);
    } else if (DynamicArray.class.isAssignableFrom(declaredField)) {
      value = (T) decodeDynamicArray(dynamicElementData, 0, parameterTypeReference);
    } else if (StaticArray.class.isAssignableFrom(declaredField)) {
      int staticLength = extractStaticArrayLength(parameterTypeReference);
      value = (T) decodeStaticArray(dynamicElementData, 0, parameterTypeReference, staticLength);
    } else {
      value = decode(dynamicElementData, declaredField);
    }
    return value;
  }

  private static int decodeDynamicStructDynamicParameterOffset(final String input) {
    return (decodeUintAsInt(input, 0) * 2);
  }

  /**
   * Class-level dynamic check: whether the class ITSELF is one of the four dynamic types.
   * Only valid for leaf/element classes — for struct fields or type references, where a
   * {@code StaticArrayN} class may hide dynamic elements, use
   * {@link #isDynamicStructField(Constructor, int)} (constructor-reflection path) or
   * {@link #isDynamic(TypeReference)} (innerTypes path) instead.
   */
  static <T extends Type> boolean isDynamic(Class<T> parameter) {
    return DynamicBytes.class.isAssignableFrom(parameter)
        || Utf8String.class.isAssignableFrom(parameter)
        || DynamicArray.class.isAssignableFrom(parameter)
        || DynamicStruct.class.isAssignableFrom(parameter);
  }

  /**
   * Recursive ABI-dynamic check: a type is ABI-dynamic if it is itself a dynamic class
   * (per {@link #isDynamic(Class)}) OR it is a StaticArray whose element type is ABI-dynamic.
   * E.g. {@code string[3]} is ABI-dynamic because {@code string} is dynamic, even though
   * its outer Java class is StaticArray.
   */
  @SuppressWarnings("unchecked")
  static boolean isDynamic(TypeReference<?> typeReference) throws ClassNotFoundException {
    Class<Type> cls = (Class<Type>) typeReference.getClassType();
    if (isDynamic(cls)) {
      return true;
    }
    // Struct classes extend StaticArray/DynamicArray for encoding purposes but are
    // not arrays: a StaticStruct is static by definition (dynamic structs are already
    // caught by isDynamic(cls) above), so keep them out of the element-type probe.
    if (StaticArray.class.isAssignableFrom(cls)
        && !StructType.class.isAssignableFrom(cls)) {
      TypeReference<?> subRef = typeReference.getSubTypeReference();
      if (subRef != null) {
        return isDynamic(subRef);
      }
      java.lang.reflect.Type type = typeReference.getType();
      if (type instanceof ParameterizedType) {
        final java.lang.reflect.Type elementType =
            ((ParameterizedType) type).getActualTypeArguments()[0];
        return isDynamic(new TypeReference<Type>() {
          @Override
          public java.lang.reflect.Type getType() {
            return elementType;
          }
        });
      }
      try {
        Class<Type> paramType = Utils.getParameterizedTypeFromArray(typeReference);
        return isDynamic(paramType);
      } catch (Exception e) {
        throw new UnsupportedOperationException(
            "Unable to determine element type of static array "
                + Utils.getTypeName(typeReference.getType()),
            e);
      }
    }
    return false;
  }

  static BigInteger asBigInteger(Object arg) {
    if (arg instanceof BigInteger) {
      return (BigInteger) arg;
    } else if (arg instanceof BigDecimal) {
      return ((BigDecimal) arg).toBigInteger();
    } else if (arg instanceof String) {
      return Numeric.toBigInt((String) arg);
    } else if (arg instanceof byte[]) {
      return Numeric.toBigInt((byte[]) arg);
    } else if (arg instanceof Double
        || arg instanceof Float
        || arg instanceof java.lang.Double
        || arg instanceof java.lang.Float) {
      return BigDecimal.valueOf(((Number) arg).doubleValue()).toBigInteger();
    } else if (arg instanceof Number) {
      return BigInteger.valueOf(((Number) arg).longValue());
    }
    return null;
  }

  static List arrayToList(Object array) {
    int len = java.lang.reflect.Array.getLength(array);
    ArrayList<Object> rslt = new ArrayList<Object>(len);
    for (int i = 0; i < len; i++) {
      rslt.add(java.lang.reflect.Array.get(array, i));
    }
    return rslt;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Type> T instantiateStaticArray(List<T> elements, int length) {
    try {
      Class<? extends StaticArray> arrayClass =
          (Class<? extends StaticArray>)
              Class.forName("org.tron.trident.abi.datatypes.generated.StaticArray" + length);

      return (T) arrayClass
          .getConstructor(Class.class, List.class)
          .newInstance(elements.get(0).getClass(), elements);
    } catch (ReflectiveOperationException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  private static <T extends Type> T decodeArrayElements(
      String input,
      int offset,
      TypeReference<T> typeReference,
      int length,
      BiFunction<List<T>, String, T> consumer) {
    try {
      Class<T> cls = Utils.getParameterizedTypeFromArray(typeReference);
      List<T> elements = new ArrayList<>(length);
      if (StructType.class.isAssignableFrom(cls)) {
        int currOffset = offset;
        for (int i = 0; i < length; i++) {
          T value;
          if (DynamicStruct.class.isAssignableFrom(cls)) {
            if (Optional.ofNullable(typeReference)
                .map(x -> x.getSubTypeReference())
                .map(x -> x.getInnerTypes())
                .isPresent()) {
              value =
                  TypeDecoder.decodeDynamicStruct(
                      input,
                      offset + getDataOffset(input, currOffset, typeReference),
                      (TypeReference<T>) new TypeReference<DynamicStruct>(
                          typeReference.isIndexed(),
                          typeReference.getSubTypeReference().getInnerTypes()) {});
              currOffset +=
                  getSingleElementLength(input, currOffset, cls)
                      * MAX_BYTE_LENGTH_FOR_HEX_STRING;
            } else {
              value =
                  TypeDecoder.decodeDynamicStruct(
                      input,
                      offset
                          + getDataOffset(
                          input, currOffset, typeReference),
                      TypeReference.create(cls));
              currOffset +=
                  getSingleElementLength(input, currOffset, cls)
                      * MAX_BYTE_LENGTH_FOR_HEX_STRING;
            }
          } else {
            if (Optional.ofNullable(typeReference)
                .map(x -> x.getSubTypeReference())
                .map(x -> x.getInnerTypes())
                .isPresent()) {
              value = TypeDecoder.decodeStaticStruct(
                  input,
                  currOffset,
                  (TypeReference<T>) typeReference.getSubTypeReference());
              currOffset +=
                  (value.bytes32PaddedLength() / Type.MAX_BYTE_LENGTH)
                      * MAX_BYTE_LENGTH_FOR_HEX_STRING;
            } else {
              value =
                  TypeDecoder.decodeStaticStruct(
                      input, currOffset, TypeReference.create(cls));
              currOffset +=
                  (value.bytes32PaddedLength() / Type.MAX_BYTE_LENGTH)
                      * MAX_BYTE_LENGTH_FOR_HEX_STRING;
            }
          }
          elements.add(value);
        }

        String typeName = getSimpleTypeName(cls);

        return consumer.apply(elements, typeName);
      } else if (Array.class.isAssignableFrom(cls)) {
        for (int i = 0, currOffset = offset; i < length; i++) {
          T value;
          if (DynamicArray.class.isAssignableFrom(cls)) {
            TypeReference<?> dynamicTypeRef =
                Utils.resolveDynamicArrayElementTypeReference(typeReference);
            value =
                (T)
                    TypeDecoder.decodeDynamicArray(
                        input,
                        offset
                            + getDataOffset(
                            input, currOffset, typeReference),
                        dynamicTypeRef);
            currOffset +=
                getSingleElementLength(input, currOffset, cls)
                    * MAX_BYTE_LENGTH_FOR_HEX_STRING;
          } else {
            // Prefer the size carried by the element's StaticArrayTypeReference: for
            // sizes with no generated class (e.g. uint256[33]) cls is the bare
            // StaticArray, whose name parses to nothing.
            TypeReference<?> elementRef = typeReference.getSubTypeReference();
            int staticLength =
                elementRef instanceof TypeReference.StaticArrayTypeReference
                    ? ((TypeReference.StaticArrayTypeReference<?>) elementRef).getSize()
                    : Utils.extractStaticArraySize(cls);
            final TypeReference innerType =
                Utils.resolveStaticArrayInnerTypeReference(typeReference);

            TypeReference.StaticArrayTypeReference staticReference =
                new TypeReference.StaticArrayTypeReference<StaticArray>(
                    staticLength) {

                  @Override
                  public TypeReference getSubTypeReference() {
                    return innerType;
                  }

                  @Override
                  public boolean isIndexed() {
                    return false;
                  }

                  @Override
                  public java.lang.reflect.Type getType() {
                    return new ParameterizedType() {
                      @Override
                      public java.lang.reflect.Type[] getActualTypeArguments() {
                        return new java.lang.reflect.Type[] {
                            innerType.getType()
                        };
                      }

                      @Override
                      public java.lang.reflect.Type getRawType() {
                        return cls;
                      }

                      @Override
                      public java.lang.reflect.Type getOwnerType() {
                        return Class.class;
                      }
                    };
                  }
                };
            if (isDynamic(staticReference)) {
              // StaticArray with dynamic elements (e.g. string[3]) is ABI-dynamic:
              // outer container stores an offset pointer (1 slot); actual data
              // is at offset + pointerValue, in head/tail form.
              int hexStringDataOffset =
                  getDataOffset(input, currOffset, staticReference);
              value =
                  (T)
                      TypeDecoder.decodeStaticArray(
                          input,
                          offset + hexStringDataOffset,
                          staticReference,
                          staticLength);
              currOffset += MAX_BYTE_LENGTH_FOR_HEX_STRING;
            } else {
              // All-static StaticArray: inline, no length prefix.
              value =
                  (T)
                      TypeDecoder.decodeStaticArray(
                          input, currOffset, staticReference, staticLength);
              currOffset +=
                  (value.bytes32PaddedLength() / Type.MAX_BYTE_LENGTH)
                      * MAX_BYTE_LENGTH_FOR_HEX_STRING;
            }
          }
          elements.add(value);
        }
        return consumer.apply(elements, cls.getName());
      } else {
        int currOffset = offset;
        for (int i = 0; i < length; i++) {
          T value;
          if (isDynamic(cls)) {
            int hexStringDataOffset = getDataOffset(input, currOffset, typeReference);
            value = decode(input, offset + hexStringDataOffset, cls);
            currOffset += MAX_BYTE_LENGTH_FOR_HEX_STRING;
          } else {
            value = decode(input, currOffset, cls);
            currOffset +=
                getSingleElementLength(input, currOffset, cls)
                    * MAX_BYTE_LENGTH_FOR_HEX_STRING;
          }
          elements.add(value);
        }

        String typeName = getSimpleTypeName(cls);

        return consumer.apply(elements, typeName);
      }
    } catch (ClassNotFoundException e) {
      throw new UnsupportedOperationException(
          "Unable to access parameterized type "
              + Utils.getTypeName(typeReference.getType()),
          e);
    }
  }
}
