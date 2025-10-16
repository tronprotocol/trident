package org.tron.trident.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType;

import java.util.List;

public class ActivePermissionOperationsUtilsTest {

    public static final String transferOptions = "0600000000000000000000000000000000000000000000000000000000000000";
    public static final String allOptions = "7fff1fc0033efb0f000000000000000000000000000000000000000000000000";
    @Test
    public void testEncodeOperations() {
        String[] contracts = new String[] {
                "TransferContract",
                "TransferAssetContract"
        };
        String options = ActivePermissionOperationsUtils.encodeOperations(contracts);
        Assertions.assertEquals(transferOptions, options);

        options = ActivePermissionOperationsUtils.encodeOperations(new String[]{});
        Assertions.assertEquals(ActivePermissionOperationsUtils.NONE_OPERATIONS, options);
    }

    @Test
    public void testGetAllAvailableActiveOperations() {
        String options = ActivePermissionOperationsUtils.getAllAvailableActiveOperations();
        Assertions.assertEquals(allOptions, options);
    }

    @Test
    public void testDecodeOperations() {
        List<ContractType> contracts = ActivePermissionOperationsUtils.decodeOperations(transferOptions);
        Assertions.assertEquals(2, contracts.size());
        Assertions.assertEquals("TransferContract", contracts.get(0).name());
        Assertions.assertEquals("TransferAssetContract", contracts.get(1).name());

        contracts = ActivePermissionOperationsUtils.decodeOperations(ActivePermissionOperationsUtils.NONE_OPERATIONS);
        Assertions.assertEquals(0, contracts.size());

        contracts = ActivePermissionOperationsUtils.decodeOperations(allOptions);
        Assertions.assertEquals(40, contracts.size());
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
