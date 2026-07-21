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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.tron.trident.abi.datatypes.DynamicArray;
import org.tron.trident.abi.datatypes.DynamicBytes;
import org.tron.trident.abi.datatypes.Fixed;
import org.tron.trident.abi.datatypes.Int;
import org.tron.trident.abi.datatypes.StaticArray;
import org.tron.trident.abi.datatypes.StaticStruct;
import org.tron.trident.abi.datatypes.StructType;
import org.tron.trident.abi.datatypes.TrcToken;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.abi.datatypes.Ufixed;
import org.tron.trident.abi.datatypes.Uint;
import org.tron.trident.abi.datatypes.Utf8String;
import org.tron.trident.abi.datatypes.reflection.Parameterized;

/**
 * Utility functions.
 */
public class Utils {

  private Utils() {
  }

  /**
   * Reflectively loads a class by FQN and verifies it is an ABI {@link Type}
   * subtype. Uses {@code initialize=false} so loading never triggers the
   * target class's static initializer — defense-in-depth against side
   * effects from unexpected class names sneaking into a TypeReference graph.
   *
   * @throws ClassNotFoundException if the named class cannot be located
   * @throws UnsupportedOperationException if the named class is not a Type subtype
   */
  @SuppressWarnings("unchecked")
  static <T extends Type> Class<T> safeLoadTypeClass(String fqcn)
      throws ClassNotFoundException {
    Class<?> loaded = Class.forName(fqcn, false, Utils.class.getClassLoader());
    if (!Type.class.isAssignableFrom(loaded)) {
      throw new UnsupportedOperationException(
          "Resolved class is not a subtype of " + Type.class.getName()
              + ": " + fqcn);
    }
    return (Class<T>) loaded;
  }

  static <T extends Type> String getTypeName(TypeReference<T> typeReference) {
    try {
      java.lang.reflect.Type reflectedType = typeReference.getType();

      Class<?> type;
      if (reflectedType instanceof ParameterizedType) {
        type = (Class<?>) ((ParameterizedType) reflectedType).getRawType();
        return getParameterizedTypeName(typeReference, type);
      } else if (typeReference.getSubTypeReference() != null) {
        return getParameterizedTypeName(typeReference, typeReference.getClassType());
      } else if (typeReference.getInnerTypes() != null) {
        List<TypeReference<?>> innerTypes = typeReference.getInnerTypes();
        return convert(innerTypes).stream()
            .map(Utils::getTypeName)
            .collect(Collectors.joining(",", "(", ")"));
      } else {
        type = safeLoadTypeClass(getTypeName(reflectedType));
        if (StructType.class.isAssignableFrom(type)) {
          return getStructType(type);
        }
        return getSimpleTypeName(type);
      }
    } catch (ClassNotFoundException e) {
      throw new UnsupportedOperationException("Invalid class reference provided", e);
    }
  }

  /** Ports {@link java.lang.reflect.Type#getTypeName()}. */
  public static String getTypeName(java.lang.reflect.Type type) {
    try {
      return type.getTypeName();
    } catch (NoSuchMethodError e) {
      return getClassName((Class) type);
    }
  }

  public static String getStructType(Class type) {
    final StringBuilder sb = new StringBuilder("(");
    Constructor constructor = findStructConstructor(type);
    Class[] itemTypes = constructor.getParameterTypes();
    for (int i = 0; i < itemTypes.length; ++i) {
      final Class cls = itemTypes[i];
      if (StructType.class.isAssignableFrom(cls)) {
        sb.append(getStructType(cls));
      } else {
        Class parameterAnnotation =
                extractParameterFromAnnotation(constructor.getParameterAnnotations()[i]);
        if (parameterAnnotation != null) {
          try {
            TypeReference typeRef = getTypeReferenceForParameterizedField(cls, parameterAnnotation);
            sb.append(getTypeName(typeRef));
          } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "Failed to build TypeReference for @Parameterized field of type "
                    + cls.getName() + " with element type "
                    + parameterAnnotation.getName(), e);
          }
        } else {
          // An array-typed field without @Parameterized cannot recover its element
          // type by reflection; the generic fallback below would lowercase the class
          // simple name into an invalid signature token (e.g. "dynamicarray"),
          // silently yielding a wrong function/event selector.
          if (StaticArray.class.isAssignableFrom(cls)
              || DynamicArray.class.isAssignableFrom(cls)) {
            throw new UnsupportedOperationException(
                "Missing @Parameterized annotation: constructor parameter " + i
                    + " of struct " + type.getName() + " is the array type "
                    + cls.getSimpleName() + ", whose element type cannot be recovered"
                    + " reflectively. Annotate it with @Parameterized(type ="
                    + " <ElementType>.class) so a valid ABI signature can be built.");
          }
          sb.append(getTypeName(TypeReference.create(cls)));
        }
      }
      if (i < itemTypes.length - 1) {
        sb.append(",");
      }
    }
    sb.append(")");
    return sb.toString();
  }

  public static TypeReference<DynamicArray> getDynamicArrayTypeReference(Class parameter) {
    return new TypeReference<DynamicArray>() {
      @Override
      public TypeReference getSubTypeReference() {
        return TypeReference.create(parameter);
      }
    };
  }

  /**
   * Builds the element TypeReference for an array-typed struct field declared via
   * {@code @Parameterized} on a constructor parameter.
   *
   * <p><b>Limitation:</b> this annotation-based constructor-reflection path represents
   * only one element-type level. Nested array fields (e.g. {@code uint256[2][2]} declared
   * as {@code StaticArray2<StaticArray2<Uint256>>}) are therefore not supported by the
   * current implementation and are rejected explicitly. Nested arrays remain supported
   * through runtime type references, for example by placing
   * {@code TypeReference.makeTypeReference("uint256[2][2]")} in the {@code innerTypes}
   * list of a runtime struct reference.
   */
  public static TypeReference getTypeReferenceForParameterizedField(
      Class fieldType, Class elementType) throws ClassNotFoundException {
    // @Parameterized recovers the erased element type of an array field; on a
    // non-array field it is meaningless.
    if (!StaticArray.class.isAssignableFrom(fieldType)
        && !DynamicArray.class.isAssignableFrom(fieldType)) {
      throw new UnsupportedOperationException(
          "@Parameterized is only applicable to array-typed struct fields: "
              + fieldType.getSimpleName() + " is not a StaticArray/DynamicArray"
              + " subtype. Remove the annotation from this field.");
    }
    // Struct classes extend StaticArray/DynamicArray for encoding purposes but are
    // legitimate element types; only genuine array-of-array nesting is rejected.
    if ((StaticArray.class.isAssignableFrom(elementType)
            || DynamicArray.class.isAssignableFrom(elementType))
        && !StructType.class.isAssignableFrom(elementType)) {
      throw new UnsupportedOperationException(
          "Nested arrays are not supported in constructor-reflected struct classes: "
              + "@Parameterized element type " + elementType.getSimpleName()
              + " on a " + fieldType.getSimpleName() + " field exceeds the single"
              + " element-type level represented by the current @Parameterized-based path."
              + " Describe the struct with runtime innerTypes instead, placing e.g."
              + " TypeReference.makeTypeReference(\"uint256[2][2]\") in the field list.");
    }
    if (StaticArray.class.isAssignableFrom(fieldType)) {
      int size = extractStaticArraySize(fieldType);
      TypeReference elementRef = TypeReference.create(elementType);
      // Built directly from the classes at hand, mirroring the reference shape
      // makeTypeReference produces. Round-tripping through a Solidity name string
      // is lossy for element types whose simple name is not a valid ABI token
      // (Fixed/Ufixed, struct classes).
      return new TypeReference.StaticArrayTypeReference<StaticArray>(size) {
        @Override
        public TypeReference getSubTypeReference() {
          return elementRef;
        }

        @Override
        public java.lang.reflect.Type getType() {
          return new ParameterizedType() {
            @Override
            public java.lang.reflect.Type[] getActualTypeArguments() {
              return new java.lang.reflect.Type[] {elementType};
            }

            @Override
            public java.lang.reflect.Type getRawType() {
              return fieldType;
            }

            @Override
            public java.lang.reflect.Type getOwnerType() {
              return Class.class;
            }
          };
        }
      };
    } else {
      return getDynamicArrayTypeReference(elementType);
    }
  }

  /**
   * Extracts the array size from a generated {@code StaticArrayN} subclass.
   * The bare {@link StaticArray} base class carries no size information and is rejected.
   * Single source of truth for StaticArrayN size parsing — decoder paths that hold a
   * {@code StaticArrayTypeReference} should prefer its {@code getSize()} and fall back here.
   */
  static int extractStaticArraySize(Class<?> staticArrayClass) {
    String className = staticArrayClass.getSimpleName();
    String prefix = "StaticArray";
    if (!className.startsWith(prefix) || className.length() == prefix.length()) {
      throw new IllegalArgumentException(
          "Cannot determine static array size from class " + staticArrayClass.getName()
              + "; expected a generated StaticArrayN subclass (e.g. StaticArray2).");
    }
    String sizeStr = className.substring(prefix.length());
    try {
      return Integer.parseInt(sizeStr);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "Invalid static array size suffix in class " + staticArrayClass.getName(), e);
    }
  }

  public static <T extends Type> Class<T> extractParameterFromAnnotation(
          Annotation[] parameterAnnotation) {
    for (Annotation a : parameterAnnotation) {
      if (Parameterized.class.isInstance(a)) {
        return (Class<T>) ((Parameterized) a).type();
      }
    }
    return null;
  }

  public static Constructor findStructConstructor(Class classType) {
    return Arrays.stream(classType.getDeclaredConstructors())
            .filter(
              declaredConstructor ->
                  Arrays.stream(declaredConstructor.getParameterTypes())
                    .allMatch(Type.class::isAssignableFrom))
            .findAny()
            .orElseThrow(
              () ->
                  new RuntimeException(
                      "TypeReferenced struct must contain "
                              + "a constructor with types that extend Type"));
  }


  static String getSimpleTypeName(Class<?> type) {
    String simpleName = type.getSimpleName().toLowerCase(Locale.ROOT);

    if (type.equals(Uint.class)
        || type.equals(Int.class)
        || type.equals(Ufixed.class)
        || type.equals(Fixed.class)) {
      return simpleName + "256";
    } else if (type.equals(Utf8String.class)) {
      return "string";
    } else if (type.equals(DynamicBytes.class)) {
      return "bytes";
    } else if (type.equals(TrcToken.class)) {
      // TVM extension type; the ABI token is camelCase and AbiTypes' lookup is
      // case-sensitive, so the generic toLowerCase() would corrupt it.
      return "trcToken";
    } else if (StructType.class.isAssignableFrom(type)) {
      return type.getName();
    } else {
      return simpleName;
    }
  }

  static <T extends Type, U extends Type> String getParameterizedTypeName(
      TypeReference<T> typeReference, Class<?> type) {

    try {
      if (type.equals(DynamicArray.class)) {
        String parameterizedTypeName = getArrayElementTypeName(typeReference);
        return parameterizedTypeName + "[]";
      } else if (StaticArray.class.isAssignableFrom(type)) {
        String parameterizedTypeName = getArrayElementTypeName(typeReference);
        final int length;
        if (TypeReference.StaticArrayTypeReference.class.isAssignableFrom(
                typeReference.getClass())) {
          length = ((TypeReference.StaticArrayTypeReference) typeReference).getSize();
        } else {
          length = extractStaticArraySize(type);
        }
        return parameterizedTypeName + "[" + length + "]";
      } else {
        throw new UnsupportedOperationException("Invalid type provided " + type.getName());
      }
    } catch (ClassNotFoundException e) {
      throw new UnsupportedOperationException("Invalid class reference provided", e);
    }
  }

  @SuppressWarnings("unchecked")
  private static String getArrayElementTypeName(TypeReference<?> typeReference)
      throws ClassNotFoundException {
    TypeReference<?> subTypeReference = typeReference.getSubTypeReference();
    if (subTypeReference != null) {
      return getTypeName((TypeReference<Type>) subTypeReference);
    }

    java.lang.reflect.Type reflectedType = typeReference.getType();
    if (!(reflectedType instanceof ParameterizedType)) {
      throw new UnsupportedOperationException("Invalid array type provided " + reflectedType);
    }

    java.lang.reflect.Type elementType =
        ((ParameterizedType) reflectedType).getActualTypeArguments()[0];
    if (elementType instanceof ParameterizedType) {
      final java.lang.reflect.Type parameterizedElementType = elementType;
      return getTypeName(
          new TypeReference<Type>() {
            @Override
            public java.lang.reflect.Type getType() {
              return parameterizedElementType;
            }
          });
    }

    Class<?> elementClass;
    if (elementType instanceof Class) {
      elementClass = (Class<?>) elementType;
    } else {
      elementClass = safeLoadTypeClass(elementType.getTypeName());
    }
    if (!Type.class.isAssignableFrom(elementClass)) {
      throw new UnsupportedOperationException(
          "Resolved array element is not a subtype of " + Type.class.getName()
              + ": " + elementType);
    }
    return simpleNameOrStruct((Class<? extends Type>) elementClass);
  }

  private static <U extends Type> String simpleNameOrStruct(Class<U> parameterizedType) {
    if (StructType.class.isAssignableFrom(parameterizedType)) {
      return getStructType(parameterizedType);
    }
    return getSimpleTypeName(parameterizedType);
  }


  /**
   * Resolves the element TypeReference for a DynamicArray during decoding.
   * Prefers the explicit {@code subTypeReference} (set by ABI-JSON path); falls back to
   * reflecting on {@code getType()}; last resort synthesizes via
   * {@link #getFullParameterizedTypeFromArray}.
   */
  @SuppressWarnings("unchecked")
  static TypeReference<?> resolveDynamicArrayElementTypeReference(TypeReference<?> outerRef)
      throws ClassNotFoundException {
    TypeReference<?> sub = outerRef.getSubTypeReference();
    if (sub != null) {
      return sub;
    }
    final java.lang.reflect.Type elementType =
        ((ParameterizedType) outerRef.getType()).getActualTypeArguments()[0];
    if (elementType instanceof ParameterizedType) {
      return new TypeReference<Type>() {
        @Override
        public java.lang.reflect.Type getType() {
          return elementType;
        }
      };
    }
    return getDynamicArrayTypeReference(getFullParameterizedTypeFromArray(outerRef));
  }

  /**
   * Resolves the inner element TypeReference for a StaticArray during decoding,
   * drilling two levels (outer-of-static-array, then element-of-static-array).
   */
  @SuppressWarnings("unchecked")
  static TypeReference<?> resolveStaticArrayInnerTypeReference(TypeReference<?> outerRef) {
    TypeReference<?> sub = outerRef.getSubTypeReference();
    if (sub != null && sub.getSubTypeReference() != null) {
      return sub.getSubTypeReference();
    }
    final java.lang.reflect.Type elementType =
        ((ParameterizedType) outerRef.getType()).getActualTypeArguments()[0];
    final java.lang.reflect.Type innerReflectType =
        ((ParameterizedType) elementType).getActualTypeArguments()[0];
    if (innerReflectType instanceof ParameterizedType) {
      return new TypeReference<Type>() {
        @Override
        public java.lang.reflect.Type getType() {
          return innerReflectType;
        }
      };
    }
    return TypeReference.create((Class) innerReflectType);
  }

  @SuppressWarnings("unchecked")
  static <T extends Type> Class<T> getParameterizedTypeFromArray(TypeReference typeReference)
      throws ClassNotFoundException {

    if (typeReference.getSubTypeReference() != null) {
      return typeReference.getSubTypeReference().getClassType();
    }


    java.lang.reflect.Type type = typeReference.getType();
    java.lang.reflect.Type[] typeArguments =
        ((ParameterizedType) type).getActualTypeArguments();

    if (typeArguments[0] instanceof ParameterizedType) {
      return safeLoadTypeClass(
          getTypeName(((ParameterizedType) typeArguments[0]).getRawType()));
    }

    return safeLoadTypeClass(typeArguments[0].getTypeName());
  }

  static <T extends Type> Class<T> getFullParameterizedTypeFromArray(TypeReference typeReference)
          throws ClassNotFoundException {

    TypeReference<?> subRef = typeReference.getSubTypeReference();
    if (subRef != null && subRef.getSubTypeReference() != null) {
      return subRef.getSubTypeReference().getClassType();
    }

    java.lang.reflect.Type type = typeReference.getType();

    java.lang.reflect.Type typeArgument =
            ((ParameterizedType) type).getActualTypeArguments()[0];

    java.lang.reflect.Type innerType =
            ((ParameterizedType) typeArgument).getActualTypeArguments()[0];

    // For 3D+ arrays, innerType may be a ParameterizedType (e.g. DynamicArray<Uint256>).
    // Use getRawType() to extract only the class name without generic parameters,
    // since Class.forName() cannot parse parameterized type strings.
    if (innerType instanceof ParameterizedType) {
      return safeLoadTypeClass(
          getTypeName(((ParameterizedType) innerType).getRawType()));
    }
    return safeLoadTypeClass(innerType.getTypeName());
  }

  @SuppressWarnings("unchecked")
  public static List<TypeReference<Type>> convert(List<TypeReference<?>> input) {
    List<TypeReference<Type>> result = new ArrayList<>(input.size());
    result.addAll(
        input.stream()
            .map(typeReference -> (TypeReference<Type>) typeReference)
            .collect(Collectors.toList()));
    return result;
  }

  public static <T, R extends Type<T>, E extends Type<T>> List<E> typeMap(
      List<List<T>> input, Class<E> outerDestType, Class<R> innerType) {
    List<E> result = new ArrayList<>();
    try {
      Constructor<E> constructor =
          outerDestType.getDeclaredConstructor(Class.class, List.class);
      for (List<T> ts : input) {
        E e = constructor.newInstance(innerType, typeMap(ts, innerType));
        result.add(e);
      }
    } catch (NoSuchMethodException
             | IllegalAccessException
             | InstantiationException
             | InvocationTargetException e) {
      throw new TypeMappingException(e);
    }
    return result;
  }

  public static <T, R extends Type<T>> List<R> typeMap(List<T> input, Class<R> destType)
      throws TypeMappingException {

    List<R> result = new ArrayList<>(input.size());

    if (!input.isEmpty()) {
      try {
        Constructor<R> constructor =
            destType.getDeclaredConstructor(input.get(0).getClass());
        for (T value : input) {
          result.add(constructor.newInstance(value));
        }
      } catch (NoSuchMethodException
               | IllegalAccessException
               | InvocationTargetException
               | InstantiationException e) {
        throw new TypeMappingException(e);
      }
    }
    return result;
  }

  /**
   * Returns flat list of canonical fields in a static struct. Example: struct Baz { Struct Bar {
   * int a, int b }, int c } will return {a, b, c}.
   *
   * @param classType Static struct type
   * @return Flat list of canonical fields in a nested struct
   */
  public static List<Field> staticStructNestedPublicFieldsFlatList(Class<Type> classType) {
    return staticStructsNestedFieldsFlatList(classType).stream()
            .filter(field -> Modifier.isPublic(field.getModifiers()))
            .collect(Collectors.toList());
  }

  /**
   * Goes over a static structs and enumerates all of its fields and nested structs fields
   * recursively.
   *
   * @param classType Static struct type
   * @return Flat list of all the fields nested in the struct
   */
  @SuppressWarnings("unchecked")
  public static List<Field> staticStructsNestedFieldsFlatList(Class<Type> classType) {
    List<Field> canonicalFields =
            Arrays.stream(classType.getDeclaredFields())
                    .filter(field -> !StaticStruct.class.isAssignableFrom(field.getType()))
                    .collect(Collectors.toList());
    List<Field> nestedFields =
            Arrays.stream(classType.getDeclaredFields())
                    .filter(field -> StaticStruct.class.isAssignableFrom(field.getType()))
                    .map(
                      field ->
                        staticStructsNestedFieldsFlatList(
                          (Class<Type>) field.getType()))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
    return Stream.concat(canonicalFields.stream(), nestedFields.stream())
            .collect(Collectors.toList());
  }

  /** Support java version < 8 Copied from {@link Class#getTypeName()}. */
  private static String getClassName(Class type) {
    if (type.isArray()) {
      try {
        Class<?> cl = type;
        int dimensions = 0;
        while (cl.isArray()) {
          dimensions++;
          cl = cl.getComponentType();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(cl.getName());
        for (int i = 0; i < dimensions; i++) {
          sb.append("[]");
        }
        return sb.toString();
      } catch (Throwable e) {
        /*FALLTHRU*/
      }
    }

    return type.getName();
  }

  /**
   * Upper bound on TypeReference graph depth accepted by the decoder.
   * Real ABIs rarely exceed 6-8 levels of nesting (e.g. DeFi aggregator structs);
   * 10 covers realistic use with a small margin while rejecting obviously
   * malicious or malformed inputs quickly. Bump if a legitimate case trips this.
   */
  private static final int MAX_TYPEREF_DEPTH = 10;

  /**
   * Validates a TypeReference graph before decoding by capping its depth at
   * {@link #MAX_TYPEREF_DEPTH}. A cycle keeps increasing the depth along the cyclic
   * path, so cycles are rejected by the same check; shared (diamond) nodes at legal
   * depths pass. Recursion is bounded by the cap itself, so this method cannot blow
   * the JVM stack on malicious input.
   *
   * <p>Called from {@code DefaultFunctionReturnDecoder.build} for each output
   * parameter (direct {@code TypeDecoder} entry points are not guarded); bounds
   * every downstream recursive
   * traversal of subTypeReference / innerTypes (in {@code TypeDecoder.isDynamic},
   * {@code Utils.getTypeName}, {@code decodeStaticStruct},
   * {@code decodeDynamicStruct}, etc.). All local state, no shared mutable
   * fields — safe for concurrent invocation on the same TypeReference.
   */
  public static void validateTypeReferenceDepth(TypeReference<?> root) {
    validateTypeReferenceDepth(root, 0);
  }

  private static void validateTypeReferenceDepth(TypeReference<?> node, int depth) {
    if (node == null) {
      return;
    }
    if (depth > MAX_TYPEREF_DEPTH) {
      throw new UnsupportedOperationException(
          "TypeReference depth exceeds " + MAX_TYPEREF_DEPTH
              + " — possible cycle or excessively nested type");
    }
    validateTypeReferenceDepth(node.getSubTypeReference(), depth + 1);
    List<TypeReference<?>> inners = node.getInnerTypes();
    if (inners != null) {
      for (TypeReference<?> inner : inners) {
        validateTypeReferenceDepth(inner, depth + 1);
      }
    }
  }
}
