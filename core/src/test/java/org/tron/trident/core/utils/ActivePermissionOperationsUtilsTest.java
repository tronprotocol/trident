package org.tron.trident.core.utils;

import com.google.protobuf.ByteString;
import java.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tron.trident.core.account.ActivePermissionOperationsUtils;
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType;

class ActivePermissionOperationsUtilsTest {

  public static final String transferOptions =
      "0600000000000000000000000000000000000000000000000000000000000000";

  @Test
  void testEncodeOperations() {
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
  void testDecodeOperations() {
    ContractType[] contracts = ActivePermissionOperationsUtils.decodeOperations(transferOptions);
    Assertions.assertEquals(2, contracts.length);
    Assertions.assertEquals("TransferContract", contracts[0].name());
    Assertions.assertEquals("TransferAssetContract", contracts[1].name());

    contracts = ActivePermissionOperationsUtils.decodeOperations(
        ActivePermissionOperationsUtils.NONE_OPERATIONS);
    Assertions.assertEquals(0, contracts.length);

    try {
      contracts = ActivePermissionOperationsUtils.decodeOperations(ByteString.EMPTY);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }

    try {
      contracts = ActivePermissionOperationsUtils.decodeOperations("");
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }

    try {
      contracts = ActivePermissionOperationsUtils.decodeOperations("xxx");
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }

  }

  @Test
  void testGetContractTypeById() {
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

  @Test
  void testSetOperations() {
    ByteString options = ActivePermissionOperationsUtils.buildOperations(
        ByteString.EMPTY,
        true,
        ContractType.TransferAssetContract,
        ContractType.CreateSmartContract);

    ContractType[] contracts =
        ActivePermissionOperationsUtils.decodeOperations(Hex.toHexString(options.toByteArray()));
    Assertions.assertEquals(2, contracts.length);
    Assertions.assertTrue(Arrays.asList(contracts).contains(ContractType.TransferAssetContract));
    Assertions.assertTrue(Arrays.asList(contracts).contains(ContractType.CreateSmartContract));

    options = ActivePermissionOperationsUtils.buildOperations(
        options,
        true,
        ContractType.TransferContract);
    contracts = ActivePermissionOperationsUtils.decodeOperations(
        Hex.toHexString(options.toByteArray()));
    Assertions.assertEquals(3, contracts.length);
    Assertions.assertTrue(Arrays.stream(contracts).allMatch(
        c -> c == ContractType.TransferAssetContract
            || c == ContractType.CreateSmartContract
            || c == ContractType.TransferContract
    ));

    options = ActivePermissionOperationsUtils.buildOperations(
        options,
        false,
        ContractType.CreateSmartContract);
    contracts = ActivePermissionOperationsUtils.decodeOperations(
        Hex.toHexString(options.toByteArray()));
    Assertions.assertEquals(2, contracts.length);
    Assertions.assertFalse(Arrays.asList(contracts).contains(ContractType.CreateSmartContract));

    // add a contract type already in options, should be no-op
    ByteString options1 = ActivePermissionOperationsUtils.buildOperations(
        options,
        true,
        ContractType.TransferContract);
    Assertions.assertEquals(options, options1);

    // add a contract type null, should be no-op
    ByteString optionsNull = ActivePermissionOperationsUtils.buildOperations(
        options,
        true,
        (ContractType[]) null);
    Assertions.assertEquals(options, optionsNull);

    // disable a contract type not in options, should be no-op
    ByteString options2 = ActivePermissionOperationsUtils.buildOperations(
        options,
        false,
        ContractType.VoteAssetContract);
    Assertions.assertEquals(options, options2);

    // disable a contract type null, should be no-op
    ByteString optionsNull2 = ActivePermissionOperationsUtils.buildOperations(
        options,
        false,
        (ContractType[]) null);
    Assertions.assertEquals(options, optionsNull2);
  }

  @Test
  void testGetContractTypeByName() {
    ContractType contract = ActivePermissionOperationsUtils.getContractTypeByName("TransferContract");
    Assertions.assertNotNull(contract);
    Assertions.assertEquals(ContractType.TransferContract, contract);

    contract = ActivePermissionOperationsUtils.getContractTypeByName("NonExistingContract");
    Assertions.assertNull(contract);
  }

  @Test
  void testIsValidOperations() {
    ByteString validOptions = ByteString.copyFrom(Hex.decode(transferOptions));
    Assertions.assertTrue(ActivePermissionOperationsUtils.isValidOperations(validOptions));

    ByteString invalidOptions = ByteString.copyFrom(Hex.decode("0000"));
    Assertions.assertFalse(ActivePermissionOperationsUtils.isValidOperations(invalidOptions));

    Assertions.assertFalse(ActivePermissionOperationsUtils.isValidOperations(ByteString.EMPTY));
    Assertions.assertFalse(ActivePermissionOperationsUtils.isValidOperations(null));
  }

}
