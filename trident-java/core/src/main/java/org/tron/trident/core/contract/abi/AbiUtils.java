package org.tron.trident.core.contract.abi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.tron.trident.proto.Common.SmartContract.ABI;

public class AbiUtils {

  public static void loadAbiFromJson(String abiString, ABI.Builder builder) throws Exception {
    if (abiString == null || abiString.isEmpty()) {
      return;
    }

    JsonElement jsonElement = JsonParser.parseString(abiString);

    if (jsonElement.isJsonObject()) {
      JsonObject jsonObject = jsonElement.getAsJsonObject();

      if (jsonObject.has("entrys")) {
        abiString = jsonObject.get("entrys").toString();
      }
    }

    ABI abi = jsonStr2ABI(abiString);
    builder.mergeFrom(abi);
  }

  public static ABI jsonStr2ABI(String jsonStr) throws Exception {
    if (jsonStr == null) {
      throw new IllegalArgumentException("ABI json string cannot be null");
    }

    JsonArray jsonRoot = parseJsonToArray(jsonStr);
    if (jsonRoot == null) {
      throw new IllegalArgumentException("Invalid ABI json format");
    }

    ABI.Builder abiBuilder = ABI.newBuilder();
    for (JsonElement element : jsonRoot) {
      ABI.Entry entry = parseAbiEntry(element.getAsJsonObject());
      abiBuilder.addEntrys(entry);
    }
    return abiBuilder.build();
  }

  private static JsonArray parseJsonToArray(String jsonStr) throws Exception {
    JsonElement jsonElement = JsonParser.parseString(jsonStr);
    if (jsonElement.isJsonObject()) {
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      if (!jsonObject.has("entrys")) {
        throw new IllegalArgumentException("Missing entrys field in ABI json object");
      }
      return jsonObject.getAsJsonArray("entrys");
    } else if (jsonElement.isJsonArray()) {
      return jsonElement.getAsJsonArray();
    }
    throw new IllegalArgumentException("Invalid ABI json: must be array or object with entrys");
  }

  private static ABI.Entry parseAbiEntry(JsonObject abiObject) throws Exception {
    // Get basic fields
    String type = getString(abiObject, "type", null);
    if (type == null) {
      throw new IllegalArgumentException("Missing type in ABI entry");
    }

    // Build basic Entry
    ABI.Entry.Builder entryBuilder = buildBaseEntry(abiObject);
    
    // Process input parameters
    processInputs(abiObject, entryBuilder, type);

    // Process output parameters
    processOutputs(abiObject, entryBuilder);

    // Set type-related properties
    setTypeRelatedProperties(entryBuilder, abiObject, type);

    return entryBuilder.build();
  }

  private static ABI.Entry.Builder buildBaseEntry(JsonObject abiObject) {
    ABI.Entry.Builder builder = ABI.Entry.newBuilder()
        .setAnonymous(getBoolean(abiObject, "anonymous", false))
        .setConstant(getBoolean(abiObject, "constant", false));

    String name = getString(abiObject, "name", null);
    if (name != null) {
      builder.setName(name);
    }

    return builder;
  }

  private static void processInputs(JsonObject abiObject,
      ABI.Entry.Builder entryBuilder, String type) throws Exception {

    JsonArray inputs = getJsonArray(abiObject, "inputs");
    if (inputs == null) {
      // Inputs are optional for fallback and receive functions
      if (!("fallback".equalsIgnoreCase(type) || "receive".equalsIgnoreCase(type))) {
        throw new IllegalArgumentException("Missing inputs for non-fallback/receive function");
      }
      return;
    }

    for (JsonElement input : inputs) {
      entryBuilder.addInputs(parseParam(input.getAsJsonObject()));
    }
  }

  private static void processOutputs(JsonObject abiObject,
      ABI.Entry.Builder entryBuilder) throws Exception {
    JsonArray outputs = getJsonArray(abiObject, "outputs");
    if (outputs != null) {
      for (JsonElement output : outputs) {
        entryBuilder.addOutputs(parseParam(output.getAsJsonObject()));
      }
    }
  }

  private static ABI.Entry.Param parseParam(JsonObject paramObject) throws Exception {
    String type = getString(paramObject, "type", null);
    String name =  getString(paramObject, "name", null);
    if (type == null || name == null) {
      throw new IllegalArgumentException("Missing type or name in parameter");
    }

    return ABI.Entry.Param.newBuilder()
        .setIndexed(getBoolean(paramObject, "indexed", false))
        .setName(name)
        .setType(type)
        .build();
  }

  private static void setTypeRelatedProperties(ABI.Entry.Builder entryBuilder, 
                                             JsonObject abiObject, 
                                             String type) {
    entryBuilder.setType(getEntryType(type))
        .setPayable(getBoolean(abiObject, "payable", false));

    String stateMutability = getString(abiObject, "stateMutability", null);
    if (stateMutability != null) {
      entryBuilder.setStateMutability(getStateMutability(stateMutability));
    }
  }

  // Utility methods
  private static boolean getBoolean(JsonObject obj, String key, boolean defaultValue) {
    return obj.has(key) ? obj.get(key).getAsBoolean() : defaultValue;
  }

  private static String getString(JsonObject obj, String key, String defaultValue) {
    return obj.has(key) ? obj.get(key).getAsString() : defaultValue;
  }

  private static JsonArray getJsonArray(JsonObject obj, String key) {
    return obj.has(key) ? obj.get(key).getAsJsonArray() : null;
  }

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
