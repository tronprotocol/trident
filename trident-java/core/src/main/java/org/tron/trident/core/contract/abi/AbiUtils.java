package org.tron.trident.core.contract.abi;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.tron.trident.proto.Common.SmartContract.ABI;

/**
 * Utility class for handling Tron smart contract ABI (Application Binary Interface).
 * Provides functionality to parse and convert between JSON ABI representations and protobuf ABI objects.
 * 
 * <p>Example usage:
 * <pre>
 * String jsonAbi = "{\"entrys\": [{\"name\": \"transfer\",\"type\": \"function\",...}]}";
 * 
 * // Convert JSON string to ABI object
 * ABI abi = AbiUtils.jsonStr2ABI(jsonAbi);
 * 
 * // Or use builder pattern
 * ABI.Builder builder = ABI.newBuilder();
 * AbiUtils.loadAbiFromJson(jsonAbi, builder);
 * </pre>
 */
public class AbiUtils {

  /**
   * Loads ABI information from a JSON string into an existing ABI builder.
   *
   * @param abiString The JSON string containing the ABI definition
   * @param builder The ABI builder to populate
   * @throws IllegalArgumentException if the JSON format is invalid or required fields are missing
   */
  public static void loadAbiFromJson(String abiString, ABI.Builder builder) {
    if (abiString == null || abiString.isEmpty()) {
      return;
    }

    ABI abi = jsonStr2ABI(abiString);
    builder.mergeFrom(abi);
  }

  /**
   * Converts a JSON string representation of an ABI to ABI protobuf object.
   * Supports both direct array format and object format with "entrys" field.
   *
   * @param jsonStr The JSON string containing the ABI definition
   * @return ABI protobuf object
   * @throws IllegalArgumentException if the input JSON string is invalid or cannot be parsed
   */
  public static ABI jsonStr2ABI(String jsonStr) {
    if (jsonStr == null) {
      throw new IllegalArgumentException("ABI json string cannot be null");
    }

    JSONArray abiEntryArray = getABIEntryArray(jsonStr);

    ABI.Builder abiBuilder = ABI.newBuilder();
    for (Object objectABIEntry : abiEntryArray) {
      ABI.Entry entry = parseAbiEntry((JSONObject) objectABIEntry);
      abiBuilder.addEntrys(entry);
    }
    return abiBuilder.build();
  }

  /**
   * Extracts the ABI entry array from a JSON string.
   *
   * @param jsonStr The JSON string to parse
   * @return JSONArray containing ABI entries
   * @throws IllegalArgumentException if the JSON format is invalid
   */
  private static JSONArray getABIEntryArray(String jsonStr) {
    try {
      Object object = JSON.parse(jsonStr);
      if (object == null) {
        throw new IllegalArgumentException("Invalid JSON format: parsing result is null");
      }

      if (object instanceof JSONArray) {
        return (JSONArray) object;
      }

      if (object instanceof JSONObject) {
        JSONArray entries = ((JSONObject) object).getJSONArray("entrys");
        if (entries == null) {
          throw new IllegalArgumentException("Missing entrys field in JSON object");
        }
        return entries;
      }

      throw new IllegalArgumentException("Invalid JSON format: expected JSONArray or JSONObject");
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to parse JSON: " + e.getMessage(), e);
    }
  }

  /**
   * Parses a single ABI entry from a JSON object.
   *
   * @param entryObj JSON object representing a single ABI entry
   * @return ABI.Entry object containing the parsed information
   * @throws IllegalArgumentException if required fields are missing or invalid
   */
  private static ABI.Entry parseAbiEntry(JSONObject entryObj) {
    if (entryObj == null) {
      throw new IllegalArgumentException("entryObj can not be null");
    }
    // Get basic fields
    String entryType = entryObj.getString("type");
    if (entryType == null) {
      throw new IllegalArgumentException("Missing type in ABI entry");
    }

    // Build basic Entry
    ABI.Entry.Builder entryBuilder = buildBaseEntry(entryObj, entryType);
    
    // Process input parameters
    processInputs(entryObj, entryBuilder, entryType);

    // Process output parameters
    processOutputs(entryObj, entryBuilder);

    return entryBuilder.build();
  }

  /**
   * Builds a base ABI entry with common fields.
   *
   * @param entryObj Source JSON object containing entry information
   * @param entryType The entry type (function, event, etc.)
   * @return ABI.Entry.Builder object with common fields，including:
   *         - anonymous
   *         - constant
   *         - name
   *         - entryType
   *         - payable
   *         - stateMutability
   * @throws IllegalArgumentException if required fields are missing or invalid
   */
  private static ABI.Entry.Builder buildBaseEntry(JSONObject entryObj, String entryType) {
    // Set fields in the order defined in protobuf schema
    ABI.Entry.Builder builder = ABI.Entry.newBuilder()
        .setAnonymous(entryObj.getBooleanValue("anonymous", false))
        .setConstant(entryObj.getBooleanValue("constant", false));

    String name = entryObj.getString("name");
    if (name != null) {
      builder.setName(name);
    }

    builder.setType(getEntryType(entryType));

    builder.setPayable(entryObj.getBooleanValue("payable", false));

    String stateMutability = entryObj.getString("stateMutability");
    if (stateMutability != null) {
      builder.setStateMutability(getStateMutability(stateMutability));
    }

    return builder;
  }

  /**
   * Processes input parameters for an ABI entry.
   * For fallback and receive functions, inputs are optional.
   *
   * @param abiObject JSON object containing the ABI entry information
   * @param entryBuilder Builder to populate with input parameters
   * @param type The entry type (function, event, etc.)
   * @throws IllegalArgumentException if inputs are missing for not-fallback/receive functions
   */
  private static void processInputs(JSONObject abiObject,
      ABI.Entry.Builder entryBuilder, String type) {

    JSONArray inputs = abiObject.getJSONArray("inputs");
    if (inputs == null) {
      // Inputs are optional for fallback and receive functions
      if (!("fallback".equalsIgnoreCase(type) || "receive".equalsIgnoreCase(type))) {
        throw new IllegalArgumentException("Missing inputs for not-fallback/receive function");
      }
      return;
    }

    for (Object input : inputs) {
      entryBuilder.addInputs(parseParam((JSONObject) input));
    }
  }

  /**
   * Processes output parameters for an ABI entry.
   *
   * @param abiObject JSON object containing the ABI entry information
   * @param entryBuilder Builder to populate with output parameters
   */
  private static void processOutputs(JSONObject abiObject,
      ABI.Entry.Builder entryBuilder) {
    JSONArray outputs = abiObject.getJSONArray("outputs");
    if (outputs != null) {
      for (Object output : outputs) {
        entryBuilder.addOutputs(parseParam((JSONObject) output));
      }
    }
  }

  /**
   * Parses a parameter definition from a JSON object.
   *
   * @param paramObject JSON object containing parameter information
   * @return ABI.Entry.Param object representing the parameter
   * @throws IllegalArgumentException if type or name is missing
   */
  private static ABI.Entry.Param parseParam(JSONObject paramObject) {
    String type = paramObject.getString("type");
    String name = paramObject.getString("name");
    if (type == null || name == null) {
      throw new IllegalArgumentException("Missing type or name in parameter");
    }

    return ABI.Entry.Param.newBuilder()
        .setIndexed(paramObject.getBooleanValue("indexed", false))
        .setName(name)
        .setType(type)
        .build();
  }

  /**
   * Converts a string type to the corresponding EntryType enum value.
   *
   * @param type The type string to convert
   * @return The corresponding EntryType enum value
   * UnknownEntryType if the type string doesn't match any known type
   */
  private static ABI.Entry.EntryType getEntryType(String type) {
    switch (type.toLowerCase()) {
      case "constructor":
        return ABI.Entry.EntryType.Constructor;
      case "function":
        return ABI.Entry.EntryType.Function;
      case "event":
        return ABI.Entry.EntryType.Event;
      case "fallback":
        return ABI.Entry.EntryType.Fallback;
      case "receive":
        return ABI.Entry.EntryType.Receive;
      case "error":
        return ABI.Entry.EntryType.Error;
      default:
        return ABI.Entry.EntryType.UnknownEntryType;
    }
  }

  /**
   * Converts a string state mutability value to the corresponding StateMutabilityType enum value.
   *
   * @param stateMutability The state mutability string to convert
   * @return The corresponding StateMutabilityType enum value
   * UnknownMutabilityType if the string doesn't match any known type
   */
  private static ABI.Entry.StateMutabilityType getStateMutability(String stateMutability) {
    switch (stateMutability.toLowerCase()) {
      case "pure":
        return ABI.Entry.StateMutabilityType.Pure;
      case "view":
        return ABI.Entry.StateMutabilityType.View;
      case "nonpayable":
        return ABI.Entry.StateMutabilityType.Nonpayable;
      case "payable":
        return ABI.Entry.StateMutabilityType.Payable;
      default:
        return ABI.Entry.StateMutabilityType.UnknownMutabilityType;
    }
  }
}
