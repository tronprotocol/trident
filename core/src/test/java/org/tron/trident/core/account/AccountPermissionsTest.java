package org.tron.trident.core.account;

import static org.tron.trident.core.ApiWrapper.parseAddress;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType;
import org.tron.trident.proto.Common.Permission;
import org.tron.trident.proto.Common.Permission.PermissionType;
import org.tron.trident.proto.Response.Account;

class AccountPermissionsTest {

  public static String address;
  public static AccountPermissions accountPermissions;
  public static Map<String, Long> activeKeyMap = new HashMap<String, Long>();
  public static final ContractType[] contracts = new ContractType[] {
      ContractType.TransferContract,
      ContractType.TransferAssetContract
  };

  public static final ByteString transferOperations =
      ActivePermissionOperationsUtils.buildOperations(ByteString.EMPTY, true, contracts);

  @BeforeAll
  public static void init() {
    KeyPair ownerKeyPair = ApiWrapper.generateAddress();
    address = ownerKeyPair.toBase58CheckAddress();
    Account account = Account.newBuilder()
        .setAddress(parseAddress(address))
        .build();
    accountPermissions
        = new AccountPermissions(account);
    for (int i = 0; i < 3; i++) {
      activeKeyMap.put(KeyPair.generate().toBase58CheckAddress(), 1L);
    }
  }

  @AfterAll
  public static void tearDown() {
    address = null;
    accountPermissions = null;
    activeKeyMap.clear();
  }

  @Test
  void testSetOwnerPermission() {
    Permission permission = accountPermissions.getOwnerPermission();
    Assertions.assertNull(permission);

    Map<String, Long> ownerKeyMap = new HashMap<>();
    ownerKeyMap.put(address, 1L);

    permission
        = accountPermissions.createOwnerPermission("testOwnerPermissionName", 1, ownerKeyMap);

    accountPermissions.setOwnerPermission(permission);

    Assertions.assertEquals("testOwnerPermissionName",
        accountPermissions.getOwnerPermission().getPermissionName());
    Assertions.assertEquals(PermissionType.Owner,
        accountPermissions.getOwnerPermission().getType());
    Assertions.assertEquals(0, accountPermissions.getOwnerPermission().getId());
    Assertions.assertEquals(1, accountPermissions.getOwnerPermission().getThreshold());
    Assertions.assertEquals(1, accountPermissions.getOwnerPermission().getKeysCount());

    try {
      accountPermissions.setOwnerPermission(null);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("owner permission cannot be null", e.getMessage());
    }
  }

  @Test
  void testSetWitnessPermission() {
    Permission permission = accountPermissions.getWitnessPermission();
    Assertions.assertNull(permission);

    Map<String, Long> witnessKeyMap = new HashMap<>();
    witnessKeyMap.put(address, 1L);

    permission
        = accountPermissions.createWitnessPermission(1, witnessKeyMap);
    accountPermissions.setWitnessPermission(permission);
    Assertions.assertEquals("witness",
        accountPermissions.getWitnessPermission().getPermissionName());
    Assertions.assertEquals(PermissionType.Witness,
        accountPermissions.getWitnessPermission().getType());
    Assertions.assertEquals(1, accountPermissions.getWitnessPermission().getId());
    Assertions.assertEquals(1, accountPermissions.getWitnessPermission().getThreshold());
    Assertions.assertEquals(1, accountPermissions.getWitnessPermission().getKeysCount());

    accountPermissions.setWitnessPermission(null);
    Assertions.assertNull(accountPermissions.getWitnessPermission());
  }

  @Test
  void testSetActivePermission() {
    List<Permission> permissions = accountPermissions.getActivePermissions();
    Assertions.assertEquals(0, permissions.size());

    Permission activePermission2
        = accountPermissions.createActivePermission("active2", 2,
        2, transferOperations, activeKeyMap);

    List<Permission> activePermissions = new ArrayList<>();
    activePermissions.add(activePermission2);
    accountPermissions.setActivePermission(activePermissions);
    Assertions.assertEquals(1,
        accountPermissions.getActivePermissions().size());
    Assertions.assertEquals(activePermission2,
        accountPermissions.getActivePermissions().get(0));
    Assertions.assertEquals(PermissionType.Active,
        accountPermissions.getActivePermissions().get(0).getType());
    Assertions.assertEquals(2, accountPermissions.getActivePermissions().get(0).getId());
    Assertions.assertEquals(activeKeyMap.size(),
        accountPermissions.getActivePermissions().get(0).getKeysCount());

    Permission activePermission3
        = activePermission2.toBuilder().setPermissionName("active3").setId(3).build();
    accountPermissions.addActivePermission(activePermission3);
    Assertions.assertEquals(2,
        accountPermissions.getActivePermissions().size());
    Assertions.assertEquals(activePermission3,
        accountPermissions.getActivePermissions().get(1));
    accountPermissions.removeActivePermission(2);
    Assertions.assertEquals(1,
        accountPermissions.getActivePermissions().size());
    Assertions.assertEquals("active3",
        accountPermissions.getActivePermissions().get(0).getPermissionName());
    Assertions.assertEquals(3,
        accountPermissions.getActivePermissions().get(0).getId());

    accountPermissions.removeActivePermission(5); // non-exist id
    Assertions.assertEquals(1,
        accountPermissions.getActivePermissions().size());

    accountPermissions.removeActivePermission(3); // only exist id
    Assertions.assertEquals(0,
        accountPermissions.getActivePermissions().size());

    Permission activePermission4
        = accountPermissions.createActivePermission(4,
        2, transferOperations, activeKeyMap);
    accountPermissions.addActivePermission(activePermission4);
    Assertions.assertEquals(1,
        accountPermissions.getActivePermissions().size());
    Assertions.assertEquals(activePermission4,
        accountPermissions.getActivePermissions().get(0));

  }

  @Test
  void testEnableDisableActivePermissionOperation() {

    Permission activePermission
        = accountPermissions.createActivePermission(4,
        2, transferOperations, activeKeyMap);

    List<Permission> activePermissions = new ArrayList<>();
    activePermissions.add(activePermission);
    accountPermissions.setActivePermission(activePermissions);
    Assertions.assertEquals(1,
        accountPermissions.getActivePermissions().size());

    accountPermissions.enableActivePermissionOperation(4, ContractType.AccountCreateContract);
    Permission updatedPermission = accountPermissions.getActivePermissionByPermissionId(4);
    Assertions.assertNotNull(updatedPermission);

    ContractType[] contractTypes = ActivePermissionOperationsUtils.decodeOperations(
        updatedPermission.getOperations());
    Assertions.assertEquals(3, contractTypes.length);
    Assertions.assertTrue(Arrays.stream(contractTypes).allMatch(
        c -> c == ContractType.TransferAssetContract
            || c == ContractType.TransferContract
            || c == ContractType.AccountCreateContract
    ));

    accountPermissions.disableActivePermissionOperation(4, ContractType.TransferContract);
    updatedPermission = accountPermissions.getActivePermissionByPermissionId(4);
    Assertions.assertNotNull(updatedPermission);
    contractTypes = ActivePermissionOperationsUtils.decodeOperations(
        updatedPermission.getOperations());
    Assertions.assertEquals(2, contractTypes.length);
    Assertions.assertTrue(Arrays.stream(contractTypes).allMatch(
        c -> c == ContractType.TransferAssetContract
            || c == ContractType.AccountCreateContract
    ));

    //recover to original
    accountPermissions
        .enableActivePermissionOperation(4, ContractType.TransferContract)
        .disableActivePermissionOperation(4, ContractType.AccountCreateContract);
    updatedPermission = accountPermissions.getActivePermissionByPermissionId(4);
    Assertions.assertNotNull(updatedPermission);
    Assertions.assertEquals(transferOperations, updatedPermission.getOperations());

  }

  @Test
  void testInvalidActivePermission() {
    Map<String, Long> invalidActiveKeyMap = new HashMap<>();
    invalidActiveKeyMap.put("testInvalidAddress", 1L);

    //set a null or empty active permission list
    try {
      accountPermissions.setActivePermission(null);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("active permission list is null or empty", e.getMessage());
    }

    try {
      accountPermissions.setActivePermission(new ArrayList<>());
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("active permission list is null or empty", e.getMessage());
    }

    // add null
    try {
      accountPermissions.addActivePermission(null);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("active permission is null", e.getMessage());
    }

    // invalid permission id, threshold, options, keys
    try {
      accountPermissions.createActivePermission("invalidActive", 1,
          2, transferOperations, activeKeyMap);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("Active permission ID must be greater than or equal to 2",
          e.getMessage());
    }

    try {
      accountPermissions.createActivePermission("invalidActive", 10,
          100, transferOperations, activeKeyMap);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("Sum of all key's weight should >= threshold", e.getMessage());
    }

    try {
      accountPermissions.createActivePermission("invalidActive", 10,
          2, ByteString.copyFrom("invalidOptions".getBytes()), activeKeyMap);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("Operations size must 32", e.getMessage());
    }

    try {
      accountPermissions.createActivePermission("invalidActive", 10,
          2, ByteString.EMPTY, activeKeyMap);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("Operations size must 32", e.getMessage());
    }

    try {
      accountPermissions.createActivePermission("invalidActive", 10,
          2, ByteString.copyFrom("test".getBytes()), activeKeyMap);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("Operations size must 32", e.getMessage());
    }

    try {
      accountPermissions.createActivePermission("invalidActive", 10,
          1, transferOperations, invalidActiveKeyMap);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assertions.assertTrue(e.getMessage().startsWith("Invalid key address"));
    }
  }

}
