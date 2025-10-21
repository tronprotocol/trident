package org.tron.trident.core.account;

import static org.tron.trident.core.ApiWrapper.parseAddress;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.core.utils.ActivePermissionOperationsUtils;
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType;
import org.tron.trident.proto.Common.Key;
import org.tron.trident.proto.Common.Permission;
import org.tron.trident.proto.Common.Permission.PermissionType;
import org.tron.trident.proto.Response.Account;
import org.tron.trident.utils.Base58Check;
import org.tron.trident.utils.Strings;

/**
 * Aggregates owner, witness and active permissions for a TRON account.
 *
 *  <p><b>Note:</b> This class is NOT thread-safe. If you need to share an instance
 *  across multiple threads, external synchronization is required.
 */
public class AccountPermissions {

  @Getter
  @Setter
  private String base58Address;

  @Getter
  private Permission ownerPermission;

  @Getter
  private Permission witnessPermission; // may be null if not present on-chain

  @Getter
  private List<Permission> activePermissions;

  public AccountPermissions(Account account) {
    this.base58Address = Base58Check.bytesToBase58(account.getAddress().toByteArray());
    this.ownerPermission = account.hasOwnerPermission() ? account.getOwnerPermission() : null;
    this.witnessPermission = account.hasWitnessPermission() ? account.getWitnessPermission() : null;
    this.activePermissions = new ArrayList<>();
    this.activePermissions.addAll(account.getActivePermissionList());
  }

  public AccountPermissions setOwnerPermission(Permission owner) {
    if (owner == null) {
      throw new IllegalArgumentException("owner permission cannot be null");
    }
    if (owner.getType() != PermissionType.Owner) {
      throw new IllegalArgumentException("owner permission type must be Owner");
    }
    this.ownerPermission = owner;
    return this;
  }

  public AccountPermissions setWitnessPermission(Permission witness) {
    if (witness != null && witness.getType() != PermissionType.Witness) {
      throw new IllegalArgumentException("witness permission type must be Witness");
    }
    this.witnessPermission = witness;
    return this;
  }

  public AccountPermissions setActivePermission(List<Permission> actives) {
    if (actives == null || actives.isEmpty()) {
      throw new IllegalArgumentException("active permission list is null or empty");
    }

    this.activePermissions.clear();
    for (Permission active : actives) {
      if (active.getType() != PermissionType.Active || active.getId() < 2) {
        throw new IllegalArgumentException("active permission must be Active and id >= 2");
      }
      this.activePermissions.add(active);
    }
    return this;
  }

  public AccountPermissions addActivePermission(Permission active) {
    if (active == null) {
      throw new IllegalArgumentException("active permission is null");
    }

    if (active.getType() != PermissionType.Active || active.getId() < 2) {
      throw new IllegalArgumentException("active permission must be Active and id >= 2");
    }
    this.activePermissions.add(active);
    return this;
  }

  public AccountPermissions removeActivePermission(int permissionId) {
    if (permissionId < 2) {
      throw new IllegalArgumentException("active permission id must be >= 2");
    }
    this.activePermissions.removeIf(p -> p.getId() == permissionId);
    return this;
  }

  /**
   * Create a Key object with address and weight
   *
   * @param address Base58Check address
   * @param weight Key weight
   * @return Key object
   */
  public Key createKey(String address, long weight) {
    try {
      return Key.newBuilder().setAddress(parseAddress(address)).setWeight(weight).build();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid key address: " + address);
    }
  }

  /**
   * Create a Permission object for Owner type, default name "owner"
   * @see #createOwnerPermission(String, long, Map)
   *
   */
  public Permission createOwnerPermission(long threshold, Map<String, Long> keys) {
    return createOwnerPermission("owner", threshold, keys);
  }

  /**
   * Create a Permission object for Owner type
   *
   * @param permissionName Permission name
   * @param threshold Threshold value
   * @param keys Map of address -> weight
   * @return Permission object
   */
  public Permission createOwnerPermission(String permissionName, long threshold,
      Map<String, Long> keys) {
    validatePermissionName(permissionName);
    validateKeysAndThreshold(keys, threshold);
    Permission.Builder builder =
        Permission.newBuilder()
            .setType(PermissionType.Owner)
            .setId(0)
            .setPermissionName(permissionName)
            .setThreshold(threshold)
            .setParentId(0);
    for (Map.Entry<String, Long> entry : keys.entrySet()) {
      builder.addKeys(createKey(entry.getKey(), entry.getValue()));
    }
    return builder.build();
  }

  /**
   * Create a Permission object for Witness type, default name "witness"
   * @see #createWitnessPermission(String, long, Map)
   *
   */
  public Permission createWitnessPermission(long threshold, Map<String, Long> keys) {
    return createWitnessPermission("witness", threshold, keys);
  }

  /**
   * Create a Permission object for Witness type
   *
   * @param permissionName Permission name
   * @param threshold Threshold value
   * @param keys Map of address -> weight
   * @return Permission object
   */
  public Permission createWitnessPermission(String permissionName, long threshold,
      Map<String, Long> keys) {
    validatePermissionName(permissionName);
    validateKeysAndThreshold(keys, threshold);
    Permission.Builder builder = Permission.newBuilder()
        .setType(PermissionType.Witness)
        .setId(1)
        .setPermissionName(permissionName)
        .setThreshold(threshold)
        .setParentId(0);
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
   * @param operations Operation bytes (can't be null, size must be 32)
   * @param keys Map of address -> weight
   * @return Permission object
   */
  public Permission createActivePermission(String permissionName, int permissionId,
      long threshold, ByteString operations, Map<String, Long> keys) {
    validatePermissionName(permissionName);
    validateActivePermissionId(permissionId);
    validateKeysAndThreshold(keys, threshold);
    validateActivePermissionOperations(operations);
    Permission.Builder builder =
        Permission.newBuilder()
            .setType(PermissionType.Active)
            .setId(permissionId)
            .setPermissionName(permissionName)
            .setThreshold(threshold)
            .setParentId(0)
            .setOperations(operations);
    for (Map.Entry<String, Long> entry : keys.entrySet()) {
      builder.addKeys(createKey(entry.getKey(), entry.getValue()));
    }
    return builder.build();
  }

  /**
   * Create a Permission object for Active type, default name "active"
   * @see #createActivePermission(String, int, long, ByteString, Map)
   */
  public Permission createActivePermission(int permissionId, long threshold,
      ByteString operations, Map<String, Long> keys) {
    return createActivePermission("active", permissionId, threshold, operations, keys);
  }

  /**
   * Validate permission name
   * @param permissionName Permission name
   * @throws IllegalArgumentException if invalid
   */
  private void validatePermissionName(String permissionName) {
    if (Strings.isEmpty(permissionName) || permissionName.length() > 32) {
      throw new IllegalArgumentException("Permission name cannot be null or empty or length > 32");
    }
  }

  /**
   * Validate keys and threshold
   * @param keys Map of address -> weight
   * @param threshold Threshold value
   * @throws IllegalArgumentException if invalid
   */
  private void validateKeysAndThreshold(Map<String, Long> keys, long threshold) {
    if (keys == null || keys.isEmpty()) {
      throw new IllegalArgumentException("Keys cannot be null or empty");
    }
    if (threshold <= 0) {
      throw new IllegalArgumentException("Threshold must be greater than 0");
    }
    long totalWeight = 0;
    for (Map.Entry<String, Long> entry : keys.entrySet()) {
      if (Strings.isEmpty(entry.getKey())) {
        throw new IllegalArgumentException("Key address cannot be null or empty");
      }
      if (entry.getValue() == null || entry.getValue() <= 0) {
        throw new IllegalArgumentException("Key weight must be greater than 0");
      }
      totalWeight += entry.getValue();
    }
    if (totalWeight < threshold) {
      throw new IllegalArgumentException("Sum of all key's weight should >= threshold");
    }
  }

  /**
   * Validate active permission ID
   * @param permissionId Permission ID
   * @throws IllegalArgumentException if permissionId < 2
   */
  private void validateActivePermissionId(int permissionId) {
    if (permissionId < 2) {
      throw new IllegalArgumentException("Active permission ID must be greater than or equal to 2");
    }
  }

  /**
   * Validate operations for active permission
   * @param operations Operation bytes
   * @throws IllegalArgumentException if operations are null or size != 32
   */

  private void validateActivePermissionOperations(ByteString operations) {
    //check operations
    if (operations.isEmpty() || operations.size() != 32) {
      throw new IllegalArgumentException("Operations size must 32");
    }
  }

  public static ByteString operationsFromHex(String operationsHex) {
    ByteString operations = null;
    if (operationsHex != null && !operationsHex.isEmpty()) {
      if (!ActivePermissionOperationsUtils.isValidOperations(operationsHex)) {
        throw new IllegalArgumentException("Invalid operations hex string: " + operationsHex);
      }
      operations = ByteString.copyFrom(Hex.decode(operationsHex));
    }
    return  operations;
  }

  public static ByteString operationsFromContractTypes(ContractType[] contractTypes) {
    String operationsHex = ActivePermissionOperationsUtils.encodeOperations(contractTypes);
    return operationsFromHex(operationsHex);
  }

}