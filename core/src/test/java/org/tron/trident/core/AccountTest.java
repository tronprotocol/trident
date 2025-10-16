package org.tron.trident.core;


import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Response.Account;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionInfo.code;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("add private key to enable this case")
class AccountTest extends BaseTest {

  private KeyPair account;
  private String accountId;

  @Order(1)
  @Test
  void genAccount() throws IllegalException, InterruptedException {
    account = ApiWrapper.generateAddress();
    TransactionExtention transaction
        = client.transfer(testAddress, account.toBase58CheckAddress(), 10_000_000);
    Transaction signTransaction = client.signTransaction(transaction);
    String txId = client.broadcastTransaction(signTransaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());
  }

  @Order(2)
  @Test
  void setAccountId() throws IllegalException, InterruptedException {
    String id = "test" + System.currentTimeMillis();

    accountId = id;
    ApiWrapper client2 = ApiWrapper.ofNile(account.toPrivateKey());
    Transaction transaction = client2.setAccountId(id, account.toBase58CheckAddress());
    Transaction signTransaction = client2.signTransaction(transaction);
    String txId = client2.broadcastTransaction(signTransaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client2.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());

    client2.close();
  }

  @Order(3)
  @Test
  void getAccountById() {
    Account account1 = client.getAccountById(accountId);
    assertEquals(account1.getAccountId(), ByteString.copyFrom(accountId.getBytes()));
  }

}
