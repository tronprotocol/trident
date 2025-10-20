package org.tron.trident.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType;

public class ActivePermissionOperationsUtilsTest {

  public static final String transferOptions =
      "0600000000000000000000000000000000000000000000000000000000000000";
  public static final String allOptions =
      "7fff1fc0033efb0f000000000000000000000000000000000000000000000000";

  @Test
  public void testEncodeOperations() {
    String[] contracts = new String[] {
        "TransferContract",
        "TransferAssetContract"
    };
    String options = ActivePermissionOperationsUtils.encodeOperations(contracts);
    Assertions.assertEquals(transferOptions, options);

    options = ActivePermissionOperationsUtils.encodeOperations(new String[] {});
    Assertions.assertEquals(ActivePermissionOperationsUtils.NONE_OPERATIONS, options);

    ContractType[] contractTypes = new ContractType[] {
        ContractType.TransferContract,
        ContractType.TransferAssetContract
    };
    options = ActivePermissionOperationsUtils.encodeOperations(contractTypes);
    Assertions.assertEquals(transferOptions, options);

    int [] contractIds = new int[] {
        ContractType.TransferContract.getNumber(),
        ContractType.TransferAssetContract.getNumber()
    };
    options = ActivePermissionOperationsUtils.encodeOperations(contractIds);
    Assertions.assertEquals(transferOptions, options);

  }

  @Test
  public void testGetAllAvailableActiveOperations() {
    String options = ActivePermissionOperationsUtils.getAllAvailableActiveOperations();
    Assertions.assertEquals(allOptions, options);
  }

  @Test
  public void testDecodeOperations() {
    ContractType[] contracts = ActivePermissionOperationsUtils.decodeOperations(transferOptions);
    Assertions.assertEquals(2, contracts.length);
    Assertions.assertEquals("TransferContract", contracts[0].name());
    Assertions.assertEquals("TransferAssetContract", contracts[1].name());

    contracts = ActivePermissionOperationsUtils.decodeOperations(
        ActivePermissionOperationsUtils.NONE_OPERATIONS);
    Assertions.assertEquals(0, contracts.length);

    contracts = ActivePermissionOperationsUtils.decodeOperations(allOptions);
    Assertions.assertEquals(40, contracts.length);

    contracts = ActivePermissionOperationsUtils.decodeOperations("");
    Assertions.assertEquals(0, contracts.length);

    contracts = ActivePermissionOperationsUtils.decodeOperations(null);
    Assertions.assertEquals(0, contracts.length);


    try {
      contracts = ActivePermissionOperationsUtils.decodeOperations("xxx");
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }

  }

  @Test
  public void testGetContractTypeById() {

    ContractType contract = ActivePermissionOperationsUtils.getContractTypeById(0);
    Assertions.assertNotNull(contract);
    Assertions.assertEquals("AccountCreateContract", contract.name());

    contract = ActivePermissionOperationsUtils.getContractTypeById(1);
    Assertions.assertNotNull(contract);
    Assertions.assertEquals("TransferContract", contract.name());

    contract = ActivePermissionOperationsUtils.getContractTypeById(2);
    Assertions.assertNotNull(contract);
    Assertions.assertEquals("TransferAssetContract", contract.name());

    contract = ActivePermissionOperationsUtils.getContractTypeById(59);
    Assertions.assertNotNull(contract);
    Assertions.assertEquals("CancelAllUnfreezeV2Contract", contract.name());

    contract = ActivePermissionOperationsUtils.getContractTypeById(-1);
    Assertions.assertNull(contract);

    contract = ActivePermissionOperationsUtils.getContractTypeById(10000);
    Assertions.assertNull(contract);

  }
}
