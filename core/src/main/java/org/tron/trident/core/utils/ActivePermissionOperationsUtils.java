package org.tron.trident.core.utils;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType;
import org.tron.trident.utils.Strings;

/**
 * Utility class for encoding and decoding operations for Account Active permissions
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

    List<ContractType> list = new ArrayList<>(Arrays.asList(contractTypes));
    int[] contractIds = new int[contractTypes.length];
    list.forEach(contractType -> {
      contractIds[list.indexOf(contractType)] = contractType.getNumber();
    });
    return encodeOperations(contractIds);
  }

  /**
   * Encode contract types to operations hex string using contract IDs
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
      if (contractId >= 0 && contractId < 256) {
        operations[contractId / 8] |= (byte) (1 << (contractId % 8));
      }
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
    List<String> list = new ArrayList<>(Arrays.asList(contractNames));
    list.forEach(contractName -> {
      ContractType contractType = getContractTypeByName(contractName);
      if (contractType == null) {
        throw  new IllegalArgumentException("Invalid contract name: " + contractName);
      }
      contractIds[list.indexOf(contractName)] = contractType.getNumber();
    });

    return encodeOperations(contractIds);
  }

  /**
   * Decode operations hex string to list of contract type names
   *
   * @param hexOperations Hex string representation of operations
   * @return Array of contractType
   */
  public static ContractType[] decodeOperations(String hexOperations) {
    List<ContractType> ContractType = new ArrayList<>();
    if (Strings.isEmpty(hexOperations)) {
      return ContractType.toArray(new ContractType[0]);
    }
    try {
      byte[] opArray = Hex.decode(hexOperations);
      for (int i = 0; i < 32; i++) { // 32 bytes
        for (int j = 0; j < 8; j++) {
          if (((opArray[i] >> j) & 0x1) == 1) {
            ContractType contractType = getContractTypeById(i * 8 + j);
            if (contractType == null) {
              throw  new IllegalArgumentException("not found contract type for id: " + (i * 8 + j));
            }
            ContractType.add(contractType);
          }
        }
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("operations decode failed: " + e.getMessage());
    }

    return ContractType.toArray(new ContractType[0]);
  }

  /**
   * Decode operations hex string to list of contract type names
   *
   * @param operations ByteString representation of operations
   * @return Array of contractType
   */
  public static ContractType[] decodeOperations(ByteString operations) {
    if (operations == null || operations.isEmpty()) {
      return decodeOperations(NONE_OPERATIONS);
    }
    return decodeOperations(Hex.toHexString(operations.toByteArray()));
  }

  /**
   * Validate if operations string is valid
   *
   * @param operations Hex string representation of operations
   * @return true if valid, false otherwise
   */
  public static boolean isValidOperations(String operations) {
    if (operations == null || operations.isEmpty()) {
      return false;
    }
    try {
      byte[] opArray = Hex.decode(operations);
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
   * Build operations ByteString by enabling or disabling specified contract types.
   * @example enable TransferContract and disable VoteContract from account current operations
   * <pre>
   * ByteString currentOps = account.getActivePermission(0).getOperations();
   * ByteString updatedOps = buildOperations(currentOps, true, ContractType.TransferContract);
   * updatedOps = buildOperations(updatedOps, false, ContractType.VoteContract);
   * </pre>
   * @example buildOperations from scratch by enabling TransferContract
   * <pre>
   * ByteString operations = buildOperations(ByteString.EMPTY, true, ContractType.TransferContract);
   * </pre>
   * @param currentOperations current operations ByteString, use ByteString.EMPTY to start from scratch
   * @param enable true to enable, false to disable
   * @param contractTypes contract types to update, if null or empty, no changes will be made
   * @return New operations ByteString with updated permissions
   */
  public static ByteString buildOperations(ByteString currentOperations,
      boolean enable, ContractType... contractTypes) {
    if (contractTypes == null || contractTypes.length == 0) {
      return currentOperations;
    }
    byte[] operations;
    if (currentOperations == null || currentOperations.isEmpty()) {
      operations = new byte[32];
    } else {
      operations = currentOperations.toByteArray();
    }
    for (ContractType contractType : contractTypes) {
      int contractId = contractType.getNumber();
      if (contractId >= 0 && contractId < 256) {
        if (enable) {
          operations[contractId / 8] |= (byte) (1 << (contractId % 8));
        } else {
          operations[contractId / 8] &= (byte) ~(1 << (contractId % 8));
        }
      }
    }
    return ByteString.copyFrom(operations);
  }

}
