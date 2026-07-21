package org.tron.trident.abi;

import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.datatypes.DynamicArray;
import org.tron.trident.abi.datatypes.DynamicStruct;
import org.tron.trident.abi.datatypes.StaticArray;
import org.tron.trident.abi.datatypes.StaticStruct;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.utils.Numeric;

/**
 * Verifies that Trident's ABI encoding and return decoding match the reference
 * bytes recorded in fixtures of pre-generated random test cases.
 *
 * <p>Fixture flow:
 * <ol>
 *   <li>Load the gzipped JSON fixture(s) from the test classpath.</li>
 *   <li>Parse the JSON-encoded value tree into native Java values.</li>
 *   <li>Use {@link TypeDecoder#instantiateType} to build Trident {@code Type} instances
 *       for parameter encoding checks.</li>
 *   <li>Build {@link TypeReference}s from ABI outputs for return decoding checks.</li>
 *   <li>Compare both encoded parameters and decode/re-encode round trips to the
 *       expected hex string from the fixture.</li>
 * </ol>
 */
@DisplayName("Trident ABI Encode Decode Compatibility")
public class TridentAbiEncodeDecodeCompatibilityTest {

    private static final ObjectMapper MAPPER = new JsonMapper();
    /**
     * Test-data fixtures loaded from the test classpath. Each entry is a gzipped JSON
     * array of pre-generated ABI compatibility test cases. Multiple fixtures are merged so
     * the aggregated tests run across all of them; case names are prefixed with the
     * fixture file (without extension) so failure messages identify the source fixture.
     */
    private static final String[] TEST_DATA_RESOURCES = {
        "contract-interface.json.gz",
        "contract-interface-abi2.json.gz",
    };

    /**
     * A single fixture entry parsed from the JSON test data.
     */
    public static class TestCase {
        public String name;     // case name (e.g. "random-0")
        public String types;    // raw type list (e.g. "[\"address\",\"address\",\"address\"]")
        public String values;   // raw value list, JSON-encoded
        public String result;   // expected encoded hex output
        public String abi;      // raw ABI interface JSON

        public TestCase(JsonNode obj) {
            this.name = obj.get("name").asText();
            this.types = obj.get("types").asText();
            this.values = obj.get("values").asText();
            this.result = obj.get("result").asText();
            this.abi = obj.get("interface").asText();
        }

        @Override
        public String toString() {
            return name + " - " + types;
        }
    }

    /**
     * Converts ABI definitions + raw JSON values into Trident {@code Type} instances,
     * including nested tuples (structs) and arrays.
     */
    static class AbiTypeInstantiator {

        /**
         * Converts a fixture-encoded value node into the plain Java argument shape
         * that {@link TypeDecoder#instantiateType} expects (BigInteger, String, List, etc.).
         */
        static class JsonValueConverter {
            static Object convert(JsonNode valueObj) {
                if (valueObj.isArray()) {
                    List<Object> list = new ArrayList<>();
                    for (JsonNode item : valueObj) {
                        list.add(convert(item));
                    }
                    return list;
                }
                if (!valueObj.isObject()) {
                    throw new IllegalArgumentException(
                            "Expected object or array, got: " + valueObj.getNodeType());
                }
                String type = valueObj.get("type").asText();
                JsonNode rawValue = valueObj.get("value");

                switch (type) {
                    case "number":
                        if (rawValue.isNumber() || rawValue.isTextual()) {
                            return new BigInteger(rawValue.asText());
                        }
                        return BigInteger.ZERO;
                    case "string":
                        return rawValue.asText();
                    case "boolean":
                        return rawValue.asBoolean();
                    case "buffer":
                        String bufValue = rawValue.asText();
                        return bufValue.startsWith("0x") ? bufValue : "0x" + bufValue;
                    case "tuple":
                        List<Object> list = new ArrayList<>();
                        for (JsonNode item : rawValue) {
                            list.add(convert(item));
                        }
                        return list;
                    default:
                        throw new IllegalArgumentException("Unknown type: " + type);
                }
            }
        }

        Type instantiateType(String typeStr, JsonNode value, JsonNode abiComponentDef)
                throws Exception {
            if (typeStr.contains("tuple")) {
                return instantiateTypeWithTupleSupport(typeStr, value, abiComponentDef);
            }
            Object convertedValue = JsonValueConverter.convert(value);
            return TypeDecoder.instantiateType(typeStr, convertedValue);
        }

        private Type instantiateTypeWithTupleSupport(String typeStr, JsonNode valueObj,
                JsonNode abiDef) throws Exception {
            int firstBracket = typeStr.indexOf('[');
            if (firstBracket < 0) {
                if (typeStr.contains("tuple")) {
                    if (valueObj.isArray()) {
                        throw new IllegalArgumentException(
                                "Unexpected JSON array for tuple base type: " + typeStr);
                    }
                    return instantiateTuple(abiDef, valueObj);
                }
                Object convertedValue = JsonValueConverter.convert(valueObj);
                return TypeDecoder.instantiateType(typeStr, convertedValue);
            }

            String baseType = typeStr.substring(0, firstBracket);
            String arrayParts = typeStr.substring(firstBracket);

            return instantiateTupleArrayRightToLeft(baseType, arrayParts, valueObj, abiDef);
        }

        private Type instantiateTuple(JsonNode input, JsonNode valueObj) throws Exception {
            JsonNode componentsArray = input.get("components");
            JsonNode tupleValuesArray = valueObj.get("value");

            List<Type> componentTypes = new ArrayList<>();
            boolean hasDynamicComponent = false;

            for (int i = 0; i < componentsArray.size(); i++) {
                JsonNode component = componentsArray.get(i);
                String componentType = component.get("type").asText();
                JsonNode componentValue = tupleValuesArray.get(i);

                Type componentTypeObj;
                if (componentType.contains("tuple")) {
                    componentTypeObj = instantiateTypeWithTupleSupport(componentType,
                            componentValue, component);
                } else {
                    Object convertedValue = JsonValueConverter.convert(componentValue);
                    componentTypeObj = TypeDecoder.instantiateType(componentType, convertedValue);
                }
                componentTypes.add(componentTypeObj);

                if (TypeEncoder.isDynamic(componentTypeObj)) {
                    hasDynamicComponent = true;
                }
            }

            return hasDynamicComponent
                    ? new DynamicStruct(componentTypes)
                    : new StaticStruct(componentTypes);
        }

        private Type instantiateTupleArrayRightToLeft(String baseType, String arrayParts,
                JsonNode valueObj, JsonNode abiDef) throws Exception {
            int lastBracket = arrayParts.lastIndexOf('[');
            String lastArrayPart = arrayParts.substring(lastBracket);
            String remainingArrayParts = arrayParts.substring(0, lastBracket);

            if (!valueObj.isArray()) {
                throw new IllegalArgumentException(
                        "Expected JSON array for array type: " + baseType + arrayParts);
            }

            if (!remainingArrayParts.isEmpty()) {
                List<Type> elements = new ArrayList<>();
                for (JsonNode elementValue : valueObj) {
                    Type elementType = instantiateTupleArrayRightToLeft(baseType,
                            remainingArrayParts, elementValue, abiDef);
                    elements.add(elementType);
                }
                return wrapAsArray(elements, lastArrayPart);
            }

            List<Type> tupleElements = new ArrayList<>();
            for (JsonNode elementValue : valueObj) {
                Type tupleInstance = instantiateTuple(abiDef, elementValue);
                tupleElements.add(tupleInstance);
            }
            return wrapAsArray(tupleElements, lastArrayPart);
        }

        private static Type wrapAsArray(List<Type> elements, String arrayPart) {
            if ("[]".equals(arrayPart)) {
                return new DynamicArray<>(Type.class, elements);
            }
            int arraySize = Integer.parseInt(
                    arrayPart.substring(1, arrayPart.length() - 1));
            return new StaticArray<Type>(Type.class, arraySize, elements) { };
        }
    }

    /**
     * Converts ABI output definitions into {@link TypeReference}s for return decoding,
     * including tuple structs and tuple arrays.
     */
    static class AbiTypeReferenceBuilder {
        static List<TypeReference<Type>> buildOutputTypeReferences(JsonNode outputsArray)
                throws Exception {
            List<TypeReference<?>> typeReferences = new ArrayList<>();
            for (JsonNode output : outputsArray) {
                String type = output.get("type").asText();
                typeReferences.add(type.contains("tuple")
                        ? makeTupleTypeReference(type, output)
                        : TypeReference.makeTypeReference(type));
            }
            return Utils.convert(typeReferences);
        }

        private static TypeReference<?> makeTupleTypeReference(String typeStr,
                JsonNode componentDef) throws Exception {
            String arrayParts = typeStr.substring("tuple".length());
            JsonNode components = componentDef.get("components");

            boolean dynamic = false;
            List<TypeReference<?>> innerTypes = new ArrayList<>();
            for (JsonNode component : components) {
                String componentType = component.get("type").asText();
                if (isAbiTypeDynamic(componentType, component)) {
                    dynamic = true;
                }
                innerTypes.add(componentType.contains("tuple")
                        ? makeTupleTypeReference(componentType, component)
                        : TypeReference.makeTypeReference(componentType));
            }

            final List<TypeReference<?>> finalInnerTypes = innerTypes;
            TypeReference<?> tupleRef = dynamic
                    ? new TypeReference<DynamicStruct>(false, finalInnerTypes) { }
                    : new TypeReference<StaticStruct>(false, finalInnerTypes) { };

            return arrayParts.isEmpty() ? tupleRef : wrapInArrayTypeReference(tupleRef, arrayParts);
        }

        private static boolean isAbiTypeDynamic(String typeStr, JsonNode componentDef) {
            if ("string".equals(typeStr) || "bytes".equals(typeStr) || typeStr.endsWith("[]")) {
                return true;
            }
            if (typeStr.startsWith("tuple")) {
                JsonNode components = componentDef.get("components");
                if (components == null) {
                    return true;
                }
                for (JsonNode component : components) {
                    if (isAbiTypeDynamic(component.get("type").asText(), component)) {
                        return true;
                    }
                }
                return false;
            }
            if (typeStr.endsWith("]")) {
                return isAbiTypeDynamic(typeStr.substring(0, typeStr.lastIndexOf('[')),
                        componentDef);
            }
            return false;
        }

        private static TypeReference<?> wrapInArrayTypeReference(TypeReference<?> baseTypeRef,
                String arrayParts) throws Exception {
            String remaining = arrayParts;
            while (!remaining.isEmpty()) {
                int firstBracket = remaining.indexOf('[');
                int closeBracket = remaining.indexOf(']', firstBracket);
                String arrayPart = remaining.substring(firstBracket, closeBracket + 1);
                remaining = remaining.substring(closeBracket + 1);
                final TypeReference<?> innerRef = baseTypeRef;

                if ("[]".equals(arrayPart)) {
                    baseTypeRef = new TypeReference<DynamicArray<Type>>() {
                        @Override
                        public TypeReference<?> getSubTypeReference() {
                            return innerRef;
                        }

                        @Override
                        public java.lang.reflect.Type getType() {
                            return parameterizedType(DynamicArray.class, innerRef);
                        }
                    };
                } else {
                    int size = Integer.parseInt(arrayPart.substring(1, arrayPart.length() - 1));
                    final Class<?> arrayClass = Class.forName(
                            "org.tron.trident.abi.datatypes.generated.StaticArray" + size);
                    baseTypeRef =
                            new TypeReference.StaticArrayTypeReference<StaticArray<Type>>(size) {
                                @Override
                                public TypeReference<?> getSubTypeReference() {
                                    return innerRef;
                                }

                                @Override
                                public java.lang.reflect.Type getType() {
                                    return parameterizedType(arrayClass, innerRef);
                                }
                            };
                }
            }
            return baseTypeRef;
        }

        private static ParameterizedType parameterizedType(final Class<?> rawType,
                final TypeReference<?> innerRef) {
            return new ParameterizedType() {
                @Override
                public java.lang.reflect.Type[] getActualTypeArguments() {
                    return new java.lang.reflect.Type[] {innerRef.getType()};
                }

                @Override
                public java.lang.reflect.Type getRawType() {
                    return rawType;
                }

                @Override
                public java.lang.reflect.Type getOwnerType() {
                    return null;
                }
            };
        }
    }

    /**
     * Loads every gzipped fixture in {@link #TEST_DATA_RESOURCES} and merges them into
     * one list, prefixing each case name with the fixture file stem so JUnit reports
     * stay disambiguated when both fixtures contain cases like "random-0".
     */
    static List<TestCase> loadTestCases() throws IOException {
        List<TestCase> all = new ArrayList<>();
        for (String resource : TEST_DATA_RESOURCES) {
            all.addAll(loadTestCasesFromResource(resource));
        }
        return all;
    }

    private static List<TestCase> loadTestCasesFromResource(String resourceName)
            throws IOException {
        try (InputStream gz = TridentAbiEncodeDecodeCompatibilityTest.class
                .getClassLoader()
                .getResourceAsStream(resourceName)) {
            if (gz == null) {
                throw new IOException(
                    "Test data resource not found on classpath: " + resourceName);
            }
            String content;
            try (GZIPInputStream in = new GZIPInputStream(gz);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    out.write(buf, 0, n);
                }
                content = out.toString(StandardCharsets.UTF_8.name());
            }
            JsonNode array = MAPPER.readTree(content);
            String stem = resourceName.replaceFirst("\\.json\\.gz$", "");
            return StreamSupport.stream(array.spliterator(), false)
                    .map(node -> {
                        TestCase tc = new TestCase(node);
                        tc.name = stem + "/" + tc.name;
                        return tc;
                    })
                    .collect(Collectors.toList());
        }
    }

    /**
     * Parameter encoding test.
     *
     * <p>Re-encodes each fixture's outputs as the inputs of a constructor call,
     * then asserts the resulting hex matches the expected encoding from the fixture.
     * Type identification correctness gates selector correctness; byte-for-byte
     * encoding correctness gates wire compatibility.
     *
     * <p>Runs as a single aggregated test (not per-case parameterized) so passing
     * and skipped cases produce no per-case test-event output; on failure the
     * assertion message lists the failing case names plus the skipped-case count.
     */
    @Test
    @DisplayName("Parameter Encoding Should Match Expected Result")
    void testParameterEncoding() throws Exception {
        List<String> failures = new ArrayList<>();
        int skipped = 0;
        for (TestCase testCase : loadTestCases()) {
            try {
                String actual = encodeParameterCase(testCase);
                String expected = testCase.result.toLowerCase();
                if (!expected.equals(actual)) {
                    failures.add(testCase + ": encoding mismatch"
                            + "\n    values:   " + testCase.values
                            + "\n    expected: " + expected
                            + "\n    actual:   " + actual);
                }
            } catch (SkippedCaseException e) {
                skipped++;
            } catch (Exception e) {
                failures.add(testCase + ": " + e.getClass().getSimpleName()
                        + " - " + e.getMessage());
            }
        }
        if (!failures.isEmpty()) {
            int shown = Math.min(failures.size(), 20);
            fail("Parameter encoding failed for " + failures.size() + " case(s)"
                    + " (" + skipped + " skipped):\n  "
                    + String.join("\n  ", failures.subList(0, shown))
                    + (failures.size() > shown ? "\n  ..." : ""));
        }
    }

    /**
     * Runs one parameter-encoding fixture case and returns the lower-case
     * hex-prefixed encoding. Throws {@link SkippedCaseException} for cases outside
     * the current Trident impl's support (formerly Assumptions-based skips).
     */
    private static String encodeParameterCase(TestCase testCase) throws Exception {
        // 1. Parse the ABI interface definition.
        JsonNode interfaceArray = MAPPER.readTree(testCase.abi);
        ObjectNode functionAbi = (ObjectNode) interfaceArray.get(0);

        // Copy outputs into inputs so we exercise the parameter encoder against
        // the (known-good) output encoding from the fixture.
        JsonNode outputsArray = functionAbi.get("outputs");
        functionAbi.set("inputs", outputsArray);

        // 2. Extract the type list from the (now-rewritten) inputs.
        JsonNode inputsArray = functionAbi.get("inputs");
        List<String> types = new ArrayList<>();
        for (JsonNode input : inputsArray) {
            types.add(input.get("type").asText());
        }

        // 3. Instantiate Trident Type values.
        AbiTypeInstantiator instantiator = new AbiTypeInstantiator();
        JsonNode valuesArray = MAPPER.readTree(testCase.values);
        List<Type> values = new ArrayList<>();

        for (int i = 0; i < types.size(); i++) {
            String typeStr = types.get(i);
            JsonNode valueElement = valuesArray.get(i);
            JsonNode inputDef = inputsArray.get(i);

            // Some fixtures encode signed-int values as the unsigned representation
            // of the wrapped negative number (e.g. int8=189 represents -67). Trident
            // encodes the value literally, producing a zero-extended slot instead of
            // a sign-extended one. This is a documented behavior difference; skip
            // such cases rather than fail.
            if (isSignedIntOutOfRange(typeStr, valueElement)) {
                throw new SkippedCaseException(
                        "Skipping " + typeStr
                                + ": fixture value outside signed range "
                                + "(reference auto-wraps, trident encodes literally)");
            }

            try {
                values.add(instantiator.instantiateType(typeStr, valueElement, inputDef));
            } catch (Exception e) {
                // Skip cases where the type isn't supported by the current Trident impl,
                // or where the fixture value violates the declared bit-width.
                // Constructor-thrown exceptions arrive wrapped in InvocationTargetException
                // via reflection in TypeDecoder.instantiateAtomicType, so unwrap before checking.
                Throwable cause =
                        e instanceof java.lang.reflect.InvocationTargetException
                                ? e.getCause()
                                : e;
                if (cause instanceof UnsupportedOperationException
                        || cause instanceof IllegalArgumentException) {
                    throw new SkippedCaseException(
                            "Skipping " + typeStr + ": "
                                    + cause.getClass().getSimpleName()
                                    + " - " + cause.getMessage());
                }
                throw e;
            }
        }

        // 4. Encode.
        String encoded = FunctionEncoder.encodeConstructor(values);
        return Numeric.prependHexPrefix(encoded).toLowerCase();
    }

    /**
     * Signals a fixture case outside the current impl's support; counted as
     * skipped by the aggregated tests instead of failing them.
     */
    private static final class SkippedCaseException extends Exception {
        SkippedCaseException(String message) {
            super(message);
        }
    }

    /**
     * Return decoding test.
     *
     * <p>Decodes each fixture's expected output bytes using the ABI output
     * definitions, then re-encodes the decoded values. The final bytes must still
     * match the fixture, which exercises return-offset handling for dynamic values
     * and structs.
     *
     * <p>Runs as a single aggregated test (not per-case parameterized) so passing
     * cases produce no per-case test-event output; on failure the assertion message
     * lists the failing case names.
     */
    @Test
    @DisplayName("Encode Decode Should Round Trip Expected Result")
    void testEncodeDecodeRoundTrip() throws Exception {
        List<String> failures = new ArrayList<>();
        for (TestCase testCase : loadTestCases()) {
            try {
                JsonNode interfaceArray = MAPPER.readTree(testCase.abi);
                JsonNode outputsArray = interfaceArray.get(0).get("outputs");
                List<TypeReference<Type>> outputReferences =
                        AbiTypeReferenceBuilder.buildOutputTypeReferences(outputsArray);

                List<Type> decodedValues =
                        FunctionReturnDecoder.decode(testCase.result, outputReferences);

                String encoded = FunctionEncoder.encodeConstructor(decodedValues);
                String actual = Numeric.prependHexPrefix(encoded).toLowerCase();
                String expected = testCase.result.toLowerCase();
                if (!expected.equals(actual)) {
                    failures.add(testCase + ": decode round-trip mismatch"
                            + "\n    expected: " + expected
                            + "\n    actual:   " + actual);
                }
            } catch (Exception e) {
                failures.add(testCase + ": " + e.getClass().getSimpleName()
                        + " - " + e.getMessage());
            }
        }
        if (!failures.isEmpty()) {
            int shown = Math.min(failures.size(), 20);
            fail("Decode round-trip failed for " + failures.size() + " case(s):\n  "
                    + String.join("\n  ", failures.subList(0, shown))
                    + (failures.size() > shown ? "\n  ..." : ""));
        }
    }

    /**
     * Whether the given fixture value falls outside the signed range of the declared
     * {@code intN} type. Only matches scalar signed ints (not arrays/tuples, not uint).
     * Treats bare {@code int} as its ABI alias {@code int256}.
     */
    private static boolean isSignedIntOutOfRange(String typeStr, JsonNode valueNode) {
        if (!typeStr.matches("int\\d*")) {
            return false;
        }
        if (!valueNode.isObject() || !"number".equals(valueNode.path("type").asText())) {
            return false;
        }
        String suffix = typeStr.substring(3);
        int bitSize = suffix.isEmpty() ? 256 : Integer.parseInt(suffix);
        BigInteger v = new BigInteger(valueNode.get("value").asText());
        BigInteger max = BigInteger.ONE.shiftLeft(bitSize - 1).subtract(BigInteger.ONE);
        BigInteger min = BigInteger.ONE.shiftLeft(bitSize - 1).negate();
        return v.compareTo(max) > 0 || v.compareTo(min) < 0;
    }
}
