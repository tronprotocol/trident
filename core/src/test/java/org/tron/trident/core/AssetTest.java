package org.tron.trident.core;


import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Contract.AssetIssueContract;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionInfo.code;
import org.tron.trident.utils.Base58Check;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("add private key to enable this case")
class AssetTest extends BaseTest {
  private String assetName;
  private KeyPair account;

  @Order(1)
  @Test
  void genAccount() throws IllegalException, InterruptedException {
    account = ApiWrapper.generateAddress();
    // createAsset need 1024 TRX
    TransactionExtention transaction
        = client.transfer(testAddress, account.toBase58CheckAddress(), 1050_000_000);
    Transaction signTransaction = client.signTransaction(transaction);
    String txId = client.broadcastTransaction(signTransaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());
  }

  @Order(2)
  @Test
  void createAsset() throws IllegalException, InterruptedException {

    ApiWrapper client2 = ApiWrapper.ofNile(account.toPrivateKey());

    assetName = "Trident" + System.currentTimeMillis();
    String abbr = assetName;
    long startTime = System.currentTimeMillis() + 24 * 60 * 60 * 1000L;
    long endTime = startTime + 365 * 24 * 60 * 60 * 1000L;
    TransactionExtention transactionExtention
        = client2.createAssetIssue(account.toBase58CheckAddress(), assetName, abbr, 10_000_000, 10,
        10, startTime, endTime, "http://test.trident.com",
        0, 0, 6, "trident test");
    Transaction signTransaction = client2.signTransaction(transactionExtention);
    String txId = client2.broadcastTransaction(signTransaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client2.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());

    client2.close();
  }

  @Order(3)
  @Test
  void testGetAssetIssueByName() {
    AssetIssueContract assetIssueContract = client.getAssetIssueByName(assetName);
    assertEquals(assetIssueContract.getPrecision(), 6);
    assertEquals(Base58Check.bytesToBase58(assetIssueContract.getOwnerAddress().toByteArray()),
        account.toBase58CheckAddress());
  }

  @Order(4)
  @Test
  void testGetAssetIssueByNameBySolidityNode() throws InterruptedException {
    sleep(60 * 1000L); //wait for finality
    AssetIssueContract assetIssueContract
        = client.getAssetIssueByName(assetName, NodeType.SOLIDITY_NODE);
    assertEquals(assetIssueContract.getPrecision(), 6);
    assertEquals(Base58Check.bytesToBase58(assetIssueContract.getOwnerAddress().toByteArray()),
        account.toBase58CheckAddress());
  }

}
