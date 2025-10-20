package org.tron.trident.core.account;

import static org.tron.trident.core.ApiWrapper.parseAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.utils.ActivePermissionOperationsUtils;
import org.tron.trident.proto.Common.Permission;
import org.tron.trident.proto.Common.Permission.PermissionType;
import org.tron.trident.proto.Response.Account;

public class AccountPermissionsTest {

  public static String address;
  public static AccountPermissions accountPermissions;

  @BeforeAll
  public static void init() {
    KeyPair ownerKeyPair = ApiWrapper.generateAddress();
    address = ownerKeyPair.toBase58CheckAddress();
    Account account = Account.newBuilder()
        .setAddress(parseAddress(address))
        .build();
    accountPermissions
        = new AccountPermissions(account);
  }

  @AfterAll
  public static void tearDown() {
    address = null;
    accountPermissions = null;
  }

  @Test
  public void testSetOwnerPermission() {
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
  public void testSetWitnessPermission() {
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
  public void testSetActivePermission() {
    // test setActivePermission
    List<KeyPair> activeKeyPairs = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      activeKeyPairs.add(KeyPair.generate());
    }

    Map<String, Long> activeKeyMap = new HashMap<String, Long>();
    for (KeyPair keyPair : activeKeyPairs) {
      activeKeyMap.put(keyPair.toBase58CheckAddress(), 1L);
    }

    String options = ActivePermissionOperationsUtils.getAllAvailableActiveOperations();
    Permission activePermission
        = accountPermissions.createActivePermission("active", 2,
        2, options, activeKeyMap);

    List<Permission> activePermissions = new ArrayList<>();
    activePermissions.add(activePermission);

    accountPermissions.setActivePermission(activePermissions);

    Assertions.assertEquals(1,
        accountPermissions.getActivePermissions().size());

    Assertions.assertEquals(activePermission,
        accountPermissions.getActivePermissions().get(0));
    Assertions.assertEquals(PermissionType.Active,
        accountPermissions.getActivePermissions().get(0).getType());
    Assertions.assertEquals(2, accountPermissions.getActivePermissions().get(0).getId());

    Assertions.assertEquals(activeKeyMap.size(),
        accountPermissions.getActivePermissions().get(0).getKeysCount());

    Permission activePermission2
        = activePermission.toBuilder().setPermissionName("active2").setId(3).build();

    accountPermissions.addActivePermission(activePermission2);

    Assertions.assertEquals(2,
        accountPermissions.getActivePermissions().size());
    Assertions.assertEquals(activePermission2,
        accountPermissions.getActivePermissions().get(1));

    accountPermissions.removeActivePermission(2);
    Assertions.assertEquals(1,
        accountPermissions.getActivePermissions().size());
    Assertions.assertEquals("active2",
        accountPermissions.getActivePermissions().get(0).getPermissionName());
    Assertions.assertEquals(3,
        accountPermissions.getActivePermissions().get(0).getId());

    try {
      accountPermissions.setActivePermission(null);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("active permission list is null or empty", e.getMessage());
    }

    try {
      accountPermissions.addActivePermission(null);
      Assertions.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("active permission is null", e.getMessage());
    }

  }

}
