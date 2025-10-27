package org.tron.trident.core.account;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType;

/**
 * Utility class for encoding and decoding operations for Account Active permissions
 *
 * <p>Each bit in the 32-byte operations field corresponds to a {@link ContractType},
 * allowing up to 256 different contract types to be represented.
 */
public class ActivePermissionOperationsUtils {
  public static final String NONE_OPERATIONS
          = "0000000000000000000000000000000000000000000000000000000000000000";

  /**
   * Encode contract types to operations hex string
   *
   * @param contractTypes Array of contract types to encode
   * @return Hex string representation of operations
   */
  public static String encodeOperations(ContractType[] contractTypes) {
    if (contractTypes == null || contractTypes.length == 0) {
      return NONE_OPERATIONS;
    }
    int[] contractIds = new int[contractTypes.length];
    for (int i = 0; i < contractTypes.length; i++) {
      contractIds[i] = contractTypes[i].getNumber();
    }
    return encodeOperations(contractIds);
  }

  /**
   * Encode contract types to operations hex string using contract IDs.
   * <p>
   * Each bit in the 32-byte operations array represents a {@link ContractType}.
   * Up to 256 different contract types are supported (8 bits × 32 bytes).
   *
   * @param contractIds Array of contract IDs to encode
   * @return Hex string representation of operations
   */
  public static String encodeOperations(int[] contractIds) {
    if (contractIds == null || contractIds.length == 0) {
      return NONE_OPERATIONS;
    }
    byte[] operations = new byte[32];
    for (int contractId : contractIds) {
      if (contractId < 0 || contractId >= 256) {
        throw new IllegalArgumentException("Invalid contractId: " + contractId);
      }
      operations[contractId / 8] |= (byte) (1 << (contractId % 8));
    }

    return Hex.toHexString(operations);
  }

  /**
   * Encode contract types to operations hex string using contract names
   *
   * @param contractNames Array of contract names to encode
   * @return Hex string representation of operations
   */
  public static String encodeOperations(String[] contractNames) {
    if (contractNames == null || contractNames.length == 0) {
      return NONE_OPERATIONS;
    }
    int[] contractIds = new int[contractNames.length];
    for (int i = 0; i < contractNames.length; i++) {
      ContractType contractType = getContractTypeByName(contractNames[i]);
      if (contractType == null) {
        throw  new IllegalArgumentException("Invalid contract name: " + contractNames[i]);
      }
      contractIds[i] = contractType.getNumber();
    }
    return encodeOperations(contractIds);
  }

  /**
   * Decode operations to list of contract types
   *
   * @param operations ByteString representation of operations
   * @return Array of contractType
   */
  public static ContractType[] decodeOperations(ByteString operations) {
    if (!isValidOperations(operations)) {
      throw new IllegalArgumentException("operations string is invalid");
    }

    List<ContractType> contractTypeList = new ArrayList<>();
    try {
      byte[] opArray = operations.toByteArray();
      for (int i = 0; i < 32; i++) { // 32 bytes
        for (int j = 0; j < 8; j++) {
          if (((opArray[i] >> j) & 0x1) == 1) {
            ContractType contractType = getContractTypeById(i * 8 + j);
            if (contractType == null) {
              throw  new IllegalArgumentException("not found contract type for id: " + (i * 8 + j));
            }
            contractTypeList.add(contractType);
          }
        }
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("operations decode failed: " + e.getMessage());
    }

    return contractTypeList.toArray(new ContractType[0]);
  }

  /**
   * Decode operations hex string to list of contract type names
   *
   * @param hexOperations Hex string representation of operations
   * @return Array of contractType
   */
  public static ContractType[] decodeOperations(String hexOperations) {
    if (hexOperations == null || hexOperations.length() != 64) { // 32 bytes * 2 hex chars
      throw new IllegalArgumentException("hexOperations string must be 64 chars");
    }
    return decodeOperations(ByteString.copyFrom(Hex.decode(hexOperations)));
  }

  /**
   * Validate if operations string is valid
   *
   * @param operations ByteString representation of operations
   * @return true if valid, false otherwise
   */
  public static boolean isValidOperations(ByteString operations) {
    if (operations == null || operations.isEmpty()) {
      return false;
    }
    try {
      byte[] opArray = operations.toByteArray();
      return opArray.length == 32;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Get contractType by name
   *
   * @param contractName Name of the contract
   * @return ContractType or null if not found
   */
  public static ContractType getContractTypeByName(String contractName) {
    try {
      return ContractType.valueOf(contractName);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Get contractType by ID
   *
   * @param contractId ID of the contract
   * @return ContractType
   */
  public static ContractType getContractTypeById(int contractId) {
    try {
      return ContractType.forNumber(contractId);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Build a new operations ByteString by enabling or disabling specified contract types.
   * <p><b>Note:</b> This method does not modify the input {@code currentOperations}, it returns a new ByteString.
   * If currentOperations not contain any of the specified contract types to disable, or already contains those to enable,
   * no changes will be made, but a new ByteString which is equal to currentOperations will be returned.
   * </p>
   * Example usage:
   * <pre>
   * // enable TransferContract and disable VoteContract from account current operations
   * ByteString currentOps = account.getActivePermission(0).getOperations();
   * ByteString updatedOps = buildOperations(currentOps, true, ContractType.TransferContract);
   * updatedOps = buildOperations(updatedOps, false, ContractType.VoteContract);
   *
   * // buildOperations from scratch by enabling TransferAssetContract and TransferContract
   * ByteString options = ActivePermissionOperationsUtils.buildOperations(
   *     ByteString.EMPTY,
   *     true,
   *     ContractType.TransferAssetContract,
   *     ContractType.TransferContract);
   * </pre>
   *
   * @param currentOperations current operations ByteString, use ByteString.EMPTY to start from scratch
   * @param enable {@code true} to enable the specified contract types, {@code false} to disable
   * @param contractTypes contract types to update, if null or empty, no changes will be made
   * @return New operations ByteString with updated permissions, never null
   */
  public static ByteString buildOperations(ByteString currentOperations,
      boolean enable, ContractType... contractTypes) {
    byte[] operations;
    if (currentOperations == null || currentOperations.isEmpty()) {
      operations = new byte[32];
    } else {
      // validate currentOperations
      if (!isValidOperations(currentOperations)) {
        throw new IllegalArgumentException("currentOperations must be 32 bytes");
      }
      operations = currentOperations.toByteArray();
    }

    // contractTypes is null or empty, no changes
    if (contractTypes == null || contractTypes.length == 0) {
      return ByteString.copyFrom(operations);
    }

    // update operations
    for (ContractType contractType : contractTypes) {
      int contractId = contractType.getNumber();
      if (contractId < 0 || contractId >= 256) {
        throw new IllegalArgumentException("Invalid contractId: " + contractId);
      }
      if (enable) {
        operations[contractId / 8] |= (byte) (1 << (contractId % 8));
      } else {
        operations[contractId / 8] &= (byte) ~(1 << (contractId % 8));
      }
    }
    return ByteString.copyFrom(operations);
  }

}
