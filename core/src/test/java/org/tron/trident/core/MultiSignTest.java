package org.tron.trident.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.utils.AccountPermissionUtils;
import org.tron.trident.core.utils.ActivePermissionOperationsUtils;
import org.tron.trident.core.utils.TransactionUtils;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Common.Permission;
import org.tron.trident.proto.Response.Account;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionInfo.code;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("add private key to enable this case")
public class MultiSignTest extends BaseTest {

  private KeyPair accountKeyPair ;     //  owner account keypair
  private final List<KeyPair> ownerKeyPairs = new ArrayList<>();  // List of multi-sign account keypair
  private final List<KeyPair> activeKeyPairs = new ArrayList<>();  // List of multi-sign account keypair


  void transferTrx(String fromAddress, String toAddress, long amount, KeyPair signKeyPair)
      throws IllegalException, InterruptedException {
    TransactionExtention txnExt = client.transfer(fromAddress, toAddress, amount);
    Transaction signedTxn = client.signTransaction(txnExt, signKeyPair);
    String txId = client.broadcastTransaction(signedTxn);

    // Wait for the transaction to be confirmed
    Thread.sleep(3000);

    // Verify if transaction is successful
    TransactionInfo info = client.getTransactionInfoById(txId);
    assertNotNull(info);
    assertEquals(code.SUCESS, info.getResult());
  }

  @Test
  @Order(1)
  void SetUp() throws Exception {
    accountKeyPair =  KeyPair.generate();

    for (int i = 0; i < 2; i++) {
      ownerKeyPairs.add(KeyPair.generate());
    }

    for (int i = 0; i < 3; i++) {
      activeKeyPairs.add(KeyPair.generate());
    }
    transferTrx(testAddress, accountKeyPair.toBase58CheckAddress(), 300_000_000L, client.keyPair);

  }

  @Test
  @Order(2)
  void testCreateAccountPermissionUpdateContract() throws Exception {

    Account account = client.getAccount(accountKeyPair.toBase58CheckAddress());

    // Build owner permission requiring 1/2 signatures
    Map <String, Long> ownerKeyMap = new HashMap<String, Long>();
    for (KeyPair keyPair : ownerKeyPairs) {
      ownerKeyMap.put(keyPair.toBase58CheckAddress(), 1L);
    }

    // Build owner permission with threshold 1
    Permission ownerPermission
        = AccountPermissionUtils.createOwnerPermission("owner", 1, ownerKeyMap);


    // Build active permission requiring 2/3 signatures
    Map <String, Long> activeKeyMap = new HashMap<String, Long>();
    for (KeyPair keyPair : activeKeyPairs) {
      activeKeyMap.put(keyPair.toBase58CheckAddress(), 1L);
    }

    // Build active permission with permissionId 2, threshold 2, all operations
    String allAvailableActiveOperations = ActivePermissionOperationsUtils.getAllAvailableActiveOperations();
    Permission activePermission
        = AccountPermissionUtils.createActivePermission("active", 2,
        2, allAvailableActiveOperations, activeKeyMap);

    List<Permission> activePermissions = new ArrayList<>();
    activePermissions.add(activePermission);

    // Build active permission requiring 2/3 signatures
    TransactionExtention txnExt = client.accountPermissionUpdate(
        accountKeyPair.toBase58CheckAddress(),
        ownerPermission,
        null,
        activePermissions);

    // Sign with owner private key
    Transaction signedTxn = client.signTransaction(txnExt, accountKeyPair);
    String transaction = client.broadcastTransaction(signedTxn);

    // Wait for the transaction to be confirmed
    Thread.sleep(5000);

    // Verify if transaction is successful
    TransactionInfo info = client.getTransactionInfoById(transaction);
    assertNotNull(info);
    assertEquals(code.SUCESS, info.getResult());

    account = client.getAccount(accountKeyPair.toBase58CheckAddress());

    assertEquals(account.getOwnerPermission().getKeysList().size(), ownerKeyMap.size());
    assertEquals(account.getActivePermissionCount(), activePermissions.size());
    assertEquals(account.getOwnerPermission(), ownerPermission);
    assertEquals(account.getActivePermission(0), activePermission);

  }

  @Test
  @Order(3)
  void testMultiSignTransfer() throws Exception {

    String toAddress = testAddress;

    // 1. Create transfer transaction, need setPermissionId in contract
    // transfer 1 TRX
    TransactionExtention txnExt = client.transfer(accountKeyPair.toBase58CheckAddress(), testAddress, 1_000_000);

    TransactionExtention transactionUpdate = TransactionUtils.setPermissionId(txnExt, 2);

    // 2. First account signs
    Transaction signedTxn1 = client.signTransaction(transactionUpdate, activeKeyPairs.get(0));

    // 3. Second account signs
    Transaction signedTxn2 = client.signTransaction(signedTxn1, activeKeyPairs.get(1));


    // 4. Broadcast transaction
    String txId = client.broadcastTransaction(signedTxn2);


    // Wait for the transaction to be confirmed
    Thread.sleep(5000);

    // Verify if transaction is successful
    TransactionInfo info = client.getTransactionInfoById(txId);
    assertNotNull(info);
    assertEquals(code.SUCESS, info.getResult());
  }

  @Test
  @Order(4)
  void testMultiOwnerSign() throws Exception {

    KeyPair owner1 = ownerKeyPairs.get(0);
    KeyPair owner2 = ownerKeyPairs.get(1);

    // Build owner permission requiring 1/2 signatures
    Map <String, Long> ownerKeyMap = new HashMap<String, Long>();
    ownerKeyMap.put(accountKeyPair.toBase58CheckAddress(), 1L);


    Permission ownerPermission
        = AccountPermissionUtils.createOwnerPermission("owner", 1, ownerKeyMap);

    String allAvailableActiveOperations = ActivePermissionOperationsUtils.getAllAvailableActiveOperations();

    Permission activePermission
        = AccountPermissionUtils.createActivePermission("active", 2, 1, allAvailableActiveOperations, ownerKeyMap);

    List<Permission> activePermissions = new ArrayList<>();
    activePermissions.add(activePermission);

    // Create transaction
    TransactionExtention txnExt = client.accountPermissionUpdate(accountKeyPair.toBase58CheckAddress(),
        ownerPermission, null, activePermissions);


    // Sign with owner private key
    Transaction signedTxn = client.signTransaction(txnExt, owner1);
    Transaction signedTxn2 = client.signTransaction(signedTxn, owner2);

    String txId = client.broadcastTransaction(signedTxn2);

    // Wait for the transaction to be confirmed
    Thread.sleep(5000);

    // Verify if transaction is successful
    TransactionInfo info = client.getTransactionInfoById(txId);
    assertNotNull(info);
    assertEquals(code.SUCESS, info.getResult());

    Account account = client.getAccount(accountKeyPair.toBase58CheckAddress());

    assertEquals(1, account.getOwnerPermission().getKeysList().size());
    assertEquals(1, account.getActivePermissionCount());
    assertEquals(1, account.getActivePermissionList().get(0).getKeysList().size());

  }

  @Test
  @Order(5)
  void TearDown() throws Exception {
    transferTrx(accountKeyPair.toBase58CheckAddress(), client.keyPair.toBase58CheckAddress(),50_000_000L, accountKeyPair);
  }

}