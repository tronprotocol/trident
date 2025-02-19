package org.tron.trident.core.contract.abi;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.tron.trident.proto.Common.SmartContract.ABI;

public class AbiUtils {

  public static void loadAbiFromJson(String abiString, ABI.Builder builder) {
    if (abiString == null || abiString.isEmpty()) {
      return;
    }

    Object jsonElement = JSON.parse(abiString);
    
    if (jsonElement instanceof JSONObject) {
      JSONObject jsonObject = (JSONObject) jsonElement;
      if (jsonObject.containsKey("entrys")) {
        abiString = jsonObject.getString("entrys");
      }
    }

    ABI abi = jsonStr2ABI(abiString);
    builder.mergeFrom(abi);
  }

  public static ABI jsonStr2ABI(String jsonStr) {
    if (jsonStr == null) {
      throw new IllegalArgumentException("ABI json string cannot be null");
    }

    JSONArray abiEntryArray = getABIEntryArray(jsonStr);

    ABI.Builder abiBuilder = ABI.newBuilder();
    for (Object abiEntryObject : abiEntryArray) {
      ABI.Entry entry = parseAbiEntry((JSONObject) abiEntryObject);
      abiBuilder.addEntrys(entry);
    }
    return abiBuilder.build();
  }

  private static JSONArray getABIEntryArray(String jsonStr) {
    Object jsonElement = JSON.parse(jsonStr);
    if (jsonElement instanceof JSONArray) {
      return (JSONArray) jsonElement;
    }

    if (!(jsonElement instanceof JSONObject)) {
      throw new IllegalArgumentException("Invalid ABI json: must be array or object with entrys");
    }

    JSONObject jsonObject = (JSONObject) jsonElement;
    if (!jsonObject.containsKey("entrys")) {
      throw new IllegalArgumentException("Missing entrys field in ABI json object");
    }

    Object entryObject = jsonObject.get("entrys");
    if (!(entryObject instanceof JSONArray)) {
      throw new IllegalArgumentException("entrys field must be an array");
    }
    
    return (JSONArray) entryObject;
  }

  private static ABI.Entry parseAbiEntry(JSONObject abiObject) {
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

  private static ABI.Entry.Builder buildBaseEntry(JSONObject abiObject) {
    ABI.Entry.Builder builder = ABI.Entry.newBuilder()
        .setAnonymous(getBoolean(abiObject, "anonymous", false))
        .setConstant(getBoolean(abiObject, "constant", false));

    String name = getString(abiObject, "name", null);
    if (name != null) {
      builder.setName(name);
    }

    return builder;
  }

  private static void processInputs(JSONObject abiObject,
      ABI.Entry.Builder entryBuilder, String type) {

    JSONArray inputs = getJsonArray(abiObject, "inputs");
    if (inputs == null) {
      // Inputs are optional for fallback and receive functions
      if (!("fallback".equalsIgnoreCase(type) || "receive".equalsIgnoreCase(type))) {
        throw new IllegalArgumentException("Missing inputs for non-fallback/receive function");
      }
      return;
    }

    for (Object input : inputs) {
      entryBuilder.addInputs(parseParam((JSONObject) input));
    }
  }

  private static void processOutputs(JSONObject abiObject,
      ABI.Entry.Builder entryBuilder) {
    JSONArray outputs = getJsonArray(abiObject, "outputs");
    if (outputs != null) {
      for (Object output : outputs) {
        entryBuilder.addOutputs(parseParam((JSONObject) output));
      }
    }
  }

  private static ABI.Entry.Param parseParam(JSONObject paramObject) {
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
                                             JSONObject abiObject, 
                                             String type) {
    entryBuilder.setType(getEntryType(type))
        .setPayable(getBoolean(abiObject, "payable", false));

    String stateMutability = getString(abiObject, "stateMutability", null);
    if (stateMutability != null) {
      entryBuilder.setStateMutability(getStateMutability(stateMutability));
    }
  }

  // Utility methods
  private static boolean getBoolean(JSONObject obj, String key, boolean defaultValue) {
    return obj.containsKey(key) ? obj.getBoolean(key) : defaultValue;
  }

  private static String getString(JSONObject obj, String key, String defaultValue) {
    return obj.containsKey(key) ? obj.getString(key) : defaultValue;
  }

  private static JSONArray getJsonArray(JSONObject obj, String key) {
    return obj.containsKey(key) ? obj.getJSONArray(key) : null;
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
