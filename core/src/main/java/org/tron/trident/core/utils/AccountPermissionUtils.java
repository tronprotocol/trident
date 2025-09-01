package org.tron.trident.core.utils;

import com.google.protobuf.ByteString;
import java.util.Map;
import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.utils.ActivePermissionOperationsUtils.ContractType;
import org.tron.trident.proto.Common.Key;
import org.tron.trident.proto.Common.Permission;

/**
 * Utility class for creating and managing TRON account permissions
 */
public class AccountPermissionUtils {

  /**
   * Create a Key object with address and weight
   *
   * @param address Base58Check address
   * @param weight Key weight
   * @return Key object
   */
  public static Key createKey(String address, long weight) {
    return Key.newBuilder()
        .setAddress(ApiWrapper.parseAddress(address))
        .setWeight(weight)
        .build();
  }

  /**
   * Create a Permission object for Owner type
   *
   * @param permissionName Permission name
   * @param threshold Threshold value
   * @param keys Map of address -> weight
   * @return Permission object
   */
  public static Permission createOwnerPermission(String permissionName, long threshold,
      Map<String, Long> keys) {
    validatePermissionName(permissionName);
    validateThreshold(threshold);
    validateKeys(keys);

    Permission.Builder builder = Permission.newBuilder()
        .setType(Permission.PermissionType.Owner)
        .setId(0)
        .setPermissionName(permissionName)
        .setThreshold(threshold);

    for (Map.Entry<String, Long> entry : keys.entrySet()) {
      builder.addKeys(createKey(entry.getKey(), entry.getValue()));
    }

    return builder.build();
  }

  /**
   * Create a Permission object for Witness type
   *
   * @param permissionName Permission name
   * @param threshold Threshold value
   * @param keys Map of address -> weight
   * @return Permission object
   */
  public static Permission createWitnessPermission(String permissionName, long threshold,
      Map<String, Long> keys) {
    validatePermissionName(permissionName);
    validateThreshold(threshold);
    validateKeys(keys);

    Permission.Builder builder = Permission.newBuilder()
        .setType(Permission.PermissionType.Witness)
        .setId(1)
        .setPermissionName(permissionName)
        .setThreshold(threshold);

    for (Map.Entry<String, Long> entry : keys.entrySet()) {
      builder.addKeys(createKey(entry.getKey(), entry.getValue()));
    }

    return builder.build();
  }

  /**
   * Create a Permission object for Active type
   *
   * @param permissionName Permission name
   * @param permissionId Permission ID (must be >= 2)
   * @param threshold Threshold value
   * @param operations Operation bytes (can be null for no restrictions)
   * @param keys Map of address -> weight
   * @return Permission object
   */
  public static Permission createActivePermission(String permissionName, int permissionId,
      long threshold,
      ByteString operations, Map<String, Long> keys) {
    validatePermissionName(permissionName);
    validateActivePermissionId(permissionId);
    validateThreshold(threshold);
    validateKeys(keys);

    Permission.Builder builder = Permission.newBuilder()
        .setType(Permission.PermissionType.Active)
        .setId(permissionId)
        .setPermissionName(permissionName)
        .setThreshold(threshold)
        .setParentId(0);

    if (operations != null) {
      builder.setOperations(operations);
    }

    for (Map.Entry<String, Long> entry : keys.entrySet()) {
      builder.addKeys(createKey(entry.getKey(), entry.getValue()));
    }

    return builder.build();
  }

  /**
   * Create Active permission with operations from OperationsUtils
   *
   * @param permissionName Permission name
   * @param permissionId Permission ID (must be >= 2)
   * @param threshold Threshold value
   * @param operationsHex Operation hex string from OperationsUtils
   * @param keys Map of address -> weight
   * @return Permission object
   */
  public static Permission createActivePermission(String permissionName, int permissionId,
      long threshold,
      String operationsHex, Map<String, Long> keys) {
    ByteString operations = null;
    if (operationsHex != null && !operationsHex.isEmpty()) {
      if (!ActivePermissionOperationsUtils.isValidOperations(operationsHex)) {
        throw new IllegalArgumentException("Invalid operations hex string: " + operationsHex);
      }
      operations = ByteString.copyFrom(Hex.decode(operationsHex));
    }
    return createActivePermission(permissionName, permissionId, threshold, operations, keys);
  }

  /**
   * Create Active permission with predefined operations
   *
   * @param permissionName Permission name
   * @param permissionId Permission ID (must be >= 2)
   * @param threshold Threshold value
   * @param contractTypes contractType array for operations
   * @param keys Map of address -> weight
   * @return Permission object
   */
  public static Permission createActivePermission(String permissionName, int permissionId,
      long threshold,
      ContractType[] contractTypes, Map<String, Long> keys) {
    String operations = ActivePermissionOperationsUtils.encodeOperations(contractTypes);
    return createActivePermission(permissionName, permissionId, threshold, operations, keys);
  }


  // Validation methods
  private static void validatePermissionName(String permissionName) {
    if (permissionName == null || permissionName.trim().isEmpty()) {
      throw new IllegalArgumentException("Permission name cannot be null or empty");
    }
  }

  private static void validateThreshold(long threshold) {
    if (threshold <= 0) {
      throw new IllegalArgumentException("Threshold must be greater than 0");
    }
  }

  private static void validateKeys(Map<String, Long> keys) {
    if (keys == null || keys.isEmpty()) {
      throw new IllegalArgumentException("Keys cannot be null or empty");
    }

    long totalWeight = 0;
    for (Map.Entry<String, Long> entry : keys.entrySet()) {
      if (entry.getKey() == null || entry.getKey().trim().isEmpty()) {
        throw new IllegalArgumentException("Key address cannot be null or empty");
      }
      if (entry.getValue() == null || entry.getValue() <= 0) {
        throw new IllegalArgumentException("Key weight must be greater than 0");
      }
      totalWeight += entry.getValue();
    }

    if (totalWeight <= 0) {
      throw new IllegalArgumentException("Total key weight must be greater than 0");
    }
  }

  private static void validateActivePermissionId(int permissionId) {
    if (permissionId < 2) {
      throw new IllegalArgumentException("Active permission ID must be greater than or equal to 2");
    }
  }
}
