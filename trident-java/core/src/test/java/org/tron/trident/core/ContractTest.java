package org.tron.trident.core;


import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.protobuf.ByteString;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Common.SmartContract;
import org.tron.trident.proto.Common.SmartContract.ABI.Entry;
import org.tron.trident.proto.Common.SmartContract.ABI.Entry.Param;
import org.tron.trident.proto.Contract.CreateSmartContract;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionInfo.code;
import org.tron.trident.utils.Base58Check;

class ContractTest {

  private static ApiWrapper client;
  private static String testAddress = "TEPRbQxXQEpHpeEx8tK5xHVs7NWudAAZgu";

  @BeforeAll
  static void setUp() {
    //specify your private key yourself
    client = ApiWrapper.ofNile(
        "private key of TEPRbQxXQEpHpeEx8tK5xHVs7NWudAAZgu");
  }

  @Test
  @Disabled
  void testTransferTrc10() throws InterruptedException {
    TransactionExtention transactionExtention;
    try {
      transactionExtention = client.transferTrc10(testAddress,
          "TAB1TVw5N8g1FLcKxPD17h2A3eEpSXvMQd", 1000587, 100);
    } catch (IllegalException e) {
      fail();
      return;
    }
    Transaction transaction = client.signTransaction(transactionExtention);
    String txId = client.broadcastTransaction(transaction);

    sleep(10_000L);

    TransactionInfo transactionInfo;
    try {
      transactionInfo = client.getTransactionInfoById(txId);
    } catch (IllegalException e) {
      assert false;
      return;
    }
    assertEquals(code.SUCESS, transactionInfo.getResult());
  }

  @Test
  void testDeployAndClearABI() throws InterruptedException {
    //first CreateSmartContract
    /**
     contract testConstantContract {
     function testPayable() public view returns (int z) {
     return 1;
     }
     }
     */

    Param param = Param.newBuilder()
        .setIndexed(true)
        .setName("testPayable")
        .build();
    SmartContract.ABI.Entry entry = Entry.newBuilder()
        .addInputs(param)
        .build();
    SmartContract.ABI abi = SmartContract.ABI.newBuilder()
        .addEntrys(entry)
        .build();
    SmartContract smartContract = SmartContract.newBuilder()
        .setName("test")
        .setOriginAddress(ApiWrapper.parseAddress(testAddress))
        .setOriginEnergyLimit(10)
        .setAbi(abi)
        .build();
    CreateSmartContract createSmartContract = CreateSmartContract.newBuilder()
        .setOwnerAddress(ApiWrapper.parseAddress(testAddress))
        .setNewContract(smartContract)
        .build();
    TransactionExtention txExt = client.blockingStub.deployContract(createSmartContract);

    Transaction transaction = client.signTransaction(txExt);
    String txId = client.broadcastTransaction(transaction);
    System.out.println("txId1:" + txId);
    sleep(10_000L);

    TransactionInfo transactionInfo;
    try {
      transactionInfo = client.getTransactionInfoById(txId);
    } catch (IllegalException e) {
      fail();
      return;
    }
    assertEquals(code.SUCESS, transactionInfo.getResult());

    //verify abi not null
    ByteString contractAddress = transactionInfo.getContractAddress();
    String contractWithT = Base58Check.bytesToBase58(contractAddress.toByteArray());
    System.out.println("contract:" + contractWithT);

    Contract contract = client.getContract(Hex.toHexString(contractAddress.toByteArray()));
    assertFalse(contract.getAbi().toString().isEmpty());

    //clear abi
    try {
      txExt = client.clearContractABI(testAddress, contractWithT);
    } catch (IllegalException e) {
      fail();
      return;
    }

    transaction = client.signTransaction(txExt);
    txId = client.broadcastTransaction(transaction);
    System.out.println("txId2:" + txId);
    sleep(10_000L);

    try {
      transactionInfo = client.getTransactionInfoById(txId);
    } catch (IllegalException e) {
      fail();
      return;
    }
    assertEquals(code.SUCESS, transactionInfo.getResult());

    contract = client.getContract(contractWithT);
    assertTrue(contract.getAbi().toString().isEmpty());
  }

  @AfterAll
  public static void close() {
    client.close();
  }
}
