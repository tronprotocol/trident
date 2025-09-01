package org.tron.trident.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.util.encoders.Hex;

/**
 * Utility class for encoding and decoding operations for Account Active permissions
 */
public class ActivePermissionOperationsUtils {

  /**
   * Encode contract types to operations hex string
   *
   * @param contractTypes Array of contract types to encode
   * @return Hex string representation of operations
   */
  public static String encodeOperations(ContractType[] contractTypes) {
    if (contractTypes == null || contractTypes.length == 0) {
      return "0000000000000000000000000000000000000000000000000000000000000000";
    }

    List<ContractType> list = new ArrayList<>(Arrays.asList(contractTypes));
    byte[] operations = new byte[32];

    list.forEach(contractType -> {
      int num = contractType.getNum();
      if (num >= 0 && num < 256) {
        operations[num / 8] |= (byte) (1 << (num % 8));
      }
    });

    return Hex.toHexString(operations);
  }

  /**
   * Encode contract types to operations hex string using contract IDs
   *
   * @param contractIds Array of contract IDs to encode
   * @return Hex string representation of operations
   */
  public static String encodeOperations(int[] contractIds) {
    if (contractIds == null || contractIds.length == 0) {
      return "0000000000000000000000000000000000000000000000000000000000000000";
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
      return "0000000000000000000000000000000000000000000000000000000000000000";
    }

    List<ContractType> contractTypes = new ArrayList<>();
    for (String contractName : contractNames) {
      try {
        ContractType contractType = ContractType.valueOf(contractName);
        if (contractType != ContractType.UndefinedType) {
          contractTypes.add(contractType);
        }
      } catch (IllegalArgumentException e) {
        // Skip invalid contract names
        throw new IllegalArgumentException("Invalid contract name: " + contractName);
      }
    }

    return encodeOperations(contractTypes.toArray(new ContractType[0]));
  }

  /**
   * Decode operations hex string to list of contract type names
   *
   * @param operations Hex string representation of operations
   * @return List of contract type names
   */
  public static List<String> decodeOperations(String operations) {
    List<String> contractNames = new ArrayList<>();

    if (operations == null || operations.isEmpty()) {
      return contractNames;
    }

    try {
      byte[] opArray = Hex.decode(operations);
      for (int i = 0; i < 32; i++) { // 32 bytes
        for (int j = 0; j < 8; j++) {
          if (((opArray[i] >> j) & 0x1) == 1) {
            ContractType contractType = ContractType.getContractTypeByNum(i * 8 + j);
            if (contractType != ContractType.UndefinedType) {
              contractNames.add(contractType.name());
            }
          }
        }
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("operations decode failed: " + e.getMessage());
    }

    return contractNames;
  }

  /**
   * Decode operations hex string to list of contract IDs
   *
   * @param operations Hex string representation of operations
   * @return List of contract IDs
   */
  public static List<Integer> decodeOperationsToIds(String operations) {
    List<Integer> contractIds = new ArrayList<>();

    if (operations == null || operations.isEmpty()) {
      return contractIds;
    }

    try {
      byte[] opArray = Hex.decode(operations);
      for (int i = 0; i < 32; i++) { // 32 bytes
        for (int j = 0; j < 8; j++) {
          if (((opArray[i] >> j) & 0x1) == 1) {
            int contractId = i * 8 + j;
            ContractType contractType
                = ContractType.getContractTypeByNum(contractId);
            if (contractType != ContractType.UndefinedType) {
              contractIds.add(contractId);
            }
          }
        }
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("operations decode failed: " + e.getMessage());
    }

    return contractIds;
  }

  /**
   * get operations for all Available Active ContractType (excluding UndefinedType
   * *  and AccountPermissionUpdateContract)
   *
   * @return Hex string of operations
   */
  public static String getAllAvailableActiveOperations() {
    ContractType[] allContracts = getAllAvailableActiveContractTypes();
    return encodeOperations(allContracts);
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
   * Get all available contract types for active permission
   *
   * @return Array of all contract types (excluding UndefinedType
   * and AccountPermissionUpdateContract)
   */
  public static ContractType[] getAllAvailableActiveContractTypes() {
    ContractType[] allTypes = ContractType.values();
    // Filter out UndefinedType (-1) and AccountPermissionUpdateContract(46)
    return Arrays.stream(allTypes)
        .filter(type -> type != ContractType.UndefinedType
            && type != ContractType.AccountPermissionUpdateContract)
        .toArray(ContractType[]::new);
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
    } catch (IllegalArgumentException e) {
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
    return ContractType.getContractTypeByNum(contractId);
  }

  /**
   * Contract types supported by TRON network
   */
  public enum ContractType {
    UndefinedType(-1),
    AccountCreateContract(0),
    TransferContract(1),
    TransferAssetContract(2),
    VoteAssetContract(3),
    VoteWitnessContract(4),
    WitnessCreateContract(5),
    AssetIssueContract(6),
    WitnessUpdateContract(8),
    ParticipateAssetIssueContract(9),
    AccountUpdateContract(10),
    FreezeBalanceContract(11),
    UnfreezeBalanceContract(12),
    WithdrawBalanceContract(13),
    UnfreezeAssetContract(14),
    UpdateAssetContract(15),
    ProposalCreateContract(16),
    ProposalApproveContract(17),
    ProposalDeleteContract(18),
    SetAccountIdContract(19),
    CustomContract(20),
    CreateSmartContract(30),
    TriggerSmartContract(31),
    GetContract(32),
    UpdateSettingContract(33),
    ExchangeCreateContract(41),
    ExchangeInjectContract(42),
    ExchangeWithdrawContract(43),
    ExchangeTransactionContract(44),
    UpdateEnergyLimitContract(45),
    AccountPermissionUpdateContract(46),
    ClearABIContract(48),
    UpdateBrokerageContract(49),
    ShieldedTransferContract(51),
    MarketSellAssetContract(52),
    MarketCancelOrderContract(53),
    FreezeBalanceV2Contract(54),
    UnfreezeBalanceV2Contract(55),
    WithdrawExpireUnfreezeContract(56),
    DelegateResourceContract(57),
    UnDelegateResourceContract(58),
    CancelAllUnfreezeV2Contract(59);

    private final int num;

    ContractType(int num) {
      this.num = num;
    }

    public static ContractType getContractTypeByNum(int num) {
      for (ContractType type : ContractType.values()) {
        if (type.getNum() == num) {
          return type;
        }
      }
      return ContractType.UndefinedType;
    }

    public int getNum() {
      return num;
    }
  }
}
