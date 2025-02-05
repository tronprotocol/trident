package org.tron.trident.core;


import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.ByteString;
import java.util.Random;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("add private key to enable this case")
public class Contract2Test extends BaseTest {

  String contractWithT;

  @Order(1)
  @Test
  void testDeployContract() throws InterruptedException, IllegalException {
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
    //System.out.println("txId1:" + txId);
    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());

    //verify abi not null
    ByteString contractAddress = transactionInfo.getContractAddress();
    contractWithT = Base58Check.bytesToBase58(contractAddress.toByteArray());
    //System.out.println("contract:" + contractWithT);

    Contract contract = client.getContract(Hex.toHexString(contractAddress.toByteArray()));
    assertFalse(contract.getAbi().toString().isEmpty());
  }

  @Order(2)
  @Test
  void testClearContractABI() throws InterruptedException, IllegalException {
    TransactionExtention txExt = client.clearContractABI(testAddress, contractWithT);

    Transaction transaction = client.signTransaction(txExt);
    String txId = client.broadcastTransaction(transaction);
    //System.out.println("txId2:" + txId);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());

    Contract contract = client.getContract(contractWithT);
    assertTrue(contract.getAbi().toString().isEmpty());
  }

  @Order(3)
  @Test
  void testUpdateEnergyLimit() throws InterruptedException, IllegalException {
    long originEnergyLimit = new Random().nextInt(10_000);
    TransactionExtention txExt = client.updateEnergyLimit(testAddress, contractWithT,
        originEnergyLimit);

    Transaction transaction = client.signTransaction(txExt);
    String txId = client.broadcastTransaction(transaction);
    //System.out.println("txId3:" + txId);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());
    Contract contract = client.getContract(contractWithT);
    assertEquals(originEnergyLimit, contract.getOriginEnergyLimit());
  }

  @Order(4)
  @Test
  void testUpdateSettingContract() throws IllegalException, InterruptedException {
    int consumeUserResourcePercent = new Random().nextInt(100);
    TransactionExtention txExt = client.updateSetting(testAddress, contractWithT,
        consumeUserResourcePercent);

    Transaction transaction = client.signTransaction(txExt);
    String txId = client.broadcastTransaction(transaction);
    //System.out.println("txId4:" + txId);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());
    Contract contract = client.getContract(contractWithT);
    assertEquals(consumeUserResourcePercent, contract.getConsumeUserResourcePercent());
  }
}
