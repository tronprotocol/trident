package org.tron.trident.core;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.tron.trident.core.account.AccountPermissions;
import org.tron.trident.core.account.ActivePermissionOperationsUtils;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.transaction.TransactionBuilder;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType;
import org.tron.trident.proto.Common.Key;
import org.tron.trident.proto.Common.Permission;
import org.tron.trident.proto.Response.Account;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionInfo.code;

// this case cost 450 trx at least to run
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("add private key to enable this case")
class MultiSignTest extends BaseTest {

  private KeyPair accountKeyPair; //  owner account keypair
  private final List<KeyPair> ownerKeyPairs = new ArrayList<>();
  private final List<KeyPair> activeKeyPairs = new ArrayList<>();

  void transferTrx(String fromAddress, String toAddress, long amount, KeyPair signKeyPair)
      throws IllegalException, InterruptedException {
    TransactionExtention txnExt = client.transfer(fromAddress, toAddress, amount);
    Transaction signedTxn = client.signTransaction(txnExt, signKeyPair);
    String txId = client.broadcastTransaction(signedTxn);

    // Wait for the transaction to be confirmed
    sleep(10_000L);

    // Verify if the transaction is successful
    TransactionInfo info = client.getTransactionInfoById(txId);
    assertNotNull(info);
    assertEquals(code.SUCESS, info.getResult());
  }

  @BeforeAll
  void init() throws Exception {
    accountKeyPair =  KeyPair.generate();

    for (int i = 0; i < 2; i++) {
      ownerKeyPairs.add(KeyPair.generate());
    }

    for (int i = 0; i < 3; i++) {
      activeKeyPairs.add(KeyPair.generate());
    }
    transferTrx(testAddress, accountKeyPair.toBase58CheckAddress(), 400_000_000L, client.keyPair);

  }

  @Test
  @Order(1)
  void testCreateAccountPermissionUpdateContractForOwners() throws Exception {
    // Get existing account permissions
    AccountPermissions accountPermissions
        = client.getAccountPermissions(accountKeyPair.toBase58CheckAddress());

    /*
    * another way to get existing account permissions
    AccountPermissions accountPermissions1
        = new AccountPermissions(client.getAccount(accountKeyPair.toBase58CheckAddress()));
     */

    // Build owner permission requiring 1/2 signatures
    Map<String, Long> ownerKeyMap = new HashMap<String, Long>();
    for (KeyPair keyPair : ownerKeyPairs) {
      ownerKeyMap.put(keyPair.toBase58CheckAddress(), 1L);
    }

    // Build owner permission with threshold 1
    Permission ownerPermission
        = accountPermissions.createOwnerPermission("owner", 1, ownerKeyMap);

    // if not set, use existing owner permission
    accountPermissions.setOwnerPermission(ownerPermission);

    // witness and active permission remains unchanged

    TransactionExtention txnExt = client.accountPermissionUpdate(
        accountKeyPair.toBase58CheckAddress(),
        accountPermissions);

    // Sign with owner private key
    Transaction signedTxn = client.signTransaction(txnExt, accountKeyPair);
    String transaction = client.broadcastTransaction(signedTxn);

    // Wait for the transaction to be confirmed
    sleep(10_000L);

    // Verify if the transaction is successful
    TransactionInfo info = client.getTransactionInfoById(transaction);
    assertNotNull(info);
    assertEquals(code.SUCESS, info.getResult());

    Account account = client.getAccount(accountKeyPair.toBase58CheckAddress());

    assertEquals(account.getOwnerPermission().getKeysList().size(),
        accountPermissions.getOwnerPermission().getKeysList().size());
    assertEquals(account.getActivePermissionCount(),
        accountPermissions.getActivePermissions().size());
    assertEquals(account.getOwnerPermission(), accountPermissions.getOwnerPermission());
  }

  @Test
  @Order(2)
  void testCreateAccountPermissionUpdateContractWithActives() throws Exception {
    // Get existing account permissions
    AccountPermissions accountPermissions
        = client.getAccountPermissions(accountKeyPair.toBase58CheckAddress());

    // Build active permission requiring 2/3 signatures
    Map<String, Long> activeKeyMap = new HashMap<String, Long>();
    for (KeyPair keyPair : activeKeyPairs) {
      activeKeyMap.put(keyPair.toBase58CheckAddress(), 1L);
    }

    // Build active permission with permissionId 2, threshold 2, transfer TRX operations only
    ByteString trxTransferOperations = ActivePermissionOperationsUtils.buildOperations(
        ByteString.EMPTY, true, ContractType.TransferContract);
    Permission activePermission
        = accountPermissions.createActivePermission("active", 2,
        2, trxTransferOperations, activeKeyMap);

    List<Permission> activePermissions = new ArrayList<>();
    activePermissions.add(activePermission);

    // owner permission remains unchanged
    // if not set, use existing active permission
    accountPermissions.setActivePermission(activePermissions);

    // Build active permission requiring 2/3 signatures
    TransactionExtention txnExt = client.accountPermissionUpdate(
        accountKeyPair.toBase58CheckAddress(),
        accountPermissions);

    // Sign with owner multiSign private key

    Transaction signedTxn = client.signTransaction(txnExt, ownerKeyPairs.get(0));
    String transaction = client.broadcastTransaction(signedTxn);

    // Wait for the transaction to be confirmed
    sleep(10_000L);

    // Verify if the transaction is successful
    TransactionInfo info = client.getTransactionInfoById(transaction);
    assertNotNull(info);
    assertEquals(code.SUCESS, info.getResult());

    Account account = client.getAccount(accountKeyPair.toBase58CheckAddress());

    assertEquals(account.getOwnerPermission().getKeysList().size(),
        accountPermissions.getOwnerPermission().getKeysList().size());
    assertEquals(account.getActivePermissionCount(), activePermissions.size());
    assertEquals(account.getOwnerPermission(), accountPermissions.getOwnerPermission());
    assertEquals(account.getActivePermission(0), activePermission);
  }

  @Test
  @Order(3)
  void testMultiSignTransferWithActive() throws Exception {
    // 1. Create transfer transaction, need setPermissionId in contract
    // transfer 1 TRX
    TransactionExtention txnExt
        = client.transfer(accountKeyPair.toBase58CheckAddress(), testAddress, 1_000_000);

    // set permission id to 2 for active permission, which was created in previous test
    TransactionBuilder transactionBuilder = new TransactionBuilder(txnExt.getTransaction());

    Transaction transaction = transactionBuilder.setContractPermissionId(2).build();

    // 2. First account signs
    Transaction signedTxn1 = client.signTransaction(transaction, activeKeyPairs.get(0));

    // 3. Second account signs
    Transaction signedTxn2 = client.signTransaction(signedTxn1, activeKeyPairs.get(1));

    // 4. Broadcast transaction
    String txId = client.broadcastTransaction(signedTxn2);

    // Wait for the transaction to be confirmed
    sleep(10_000L);

    // Verify if the transaction is successful
    TransactionInfo info = client.getTransactionInfoById(txId);
    assertNotNull(info);
    assertEquals(code.SUCESS, info.getResult());
  }

  @Test
  @Order(4)
  void testAddOwnerWithMultiOwnerSign() throws Exception {

    // change owner permission from 1/2 to require 2/3 signatures
    KeyPair newKeyPair = ApiWrapper.generateAddress();

    AccountPermissions accountPermissions
        = new AccountPermissions(client.getAccount(accountKeyPair.toBase58CheckAddress()));


    // Build new owner permission requiring 2/3 signatures
    Key newOwnerKey1 = accountPermissions.createKey(newKeyPair.toBase58CheckAddress(), 1L);

    Permission ownerPermission
        = accountPermissions.getOwnerPermission().toBuilder()
        .setThreshold(2) // change the threshold to 2
        .setPermissionName("newOwner")
        .addKeys(newOwnerKey1) // keep existing keys
        .build();


    // set new owner permission
    accountPermissions.setOwnerPermission(ownerPermission);


    // Create transaction
    TransactionExtention txnExt
        = client.accountPermissionUpdate(accountKeyPair.toBase58CheckAddress(), accountPermissions);

    // Sign with one owner
    Transaction signedTxn = client.signTransaction(txnExt, ownerKeyPairs.get(1));

    String txId = client.broadcastTransaction(signedTxn);

    // Wait for the transaction to be confirmed
    sleep(10_000L);

    // Verify if the transaction is successful
    TransactionInfo info = client.getTransactionInfoById(txId);
    assertNotNull(info);
    assertEquals(code.SUCESS, info.getResult());

    Account account = client.getAccount(accountKeyPair.toBase58CheckAddress());

    assertEquals(3, account.getOwnerPermission().getKeysList().size());
    assertEquals(2, accountPermissions.getOwnerPermission().getThreshold());
    assertEquals("newOwner", accountPermissions.getOwnerPermission().getPermissionName());
    assertEquals(accountPermissions.getActivePermissions().size(),
        account.getActivePermissionCount());
  }

}