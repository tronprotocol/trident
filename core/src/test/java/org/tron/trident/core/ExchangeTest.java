package org.tron.trident.core;


import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Response.Exchange;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionInfo.code;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("add private key to enable this case")
public class ExchangeTest extends BaseTest {

  private long exchangeID;

  @Order(1)
  @Test
  void testExchangeCreate() throws InterruptedException, IllegalException {
    // change the secondToken as necessary
    // this will cost 1024 TRX,
    // unit of TRX is sun, the decimal of token 1000587 is 6.
    TransactionExtention transactionExtention = client.exchangeCreate(testAddress, "_",
        200_000_000L, tokenId, 100_000_000L);
    Transaction transaction = client.signTransaction(transactionExtention);
    String txId = client.broadcastTransaction(transaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());

    exchangeID = transactionInfo.getExchangeId();
    //System.out.println("exchangeID:" + exchangeID);
    // visit this for more info: https://nile.trongrid.io/wallet/getexchangebyid?id={changeid}
    Exchange exchange = client.getExchangeById(String.valueOf(exchangeID));
    assertEquals(200_000_000L, exchange.getFirstTokenBalance());
    assertEquals(100_000_000L, exchange.getSecondTokenBalance());
  }

  @Order(2)
  @Test
  void testExchangeInject() throws InterruptedException, IllegalException {
    TransactionExtention transactionExtention = client.exchangeInject(testAddress, exchangeID, "_",
        100_000_000L);

    Transaction transaction = client.signTransaction(transactionExtention);
    String txId = client.broadcastTransaction(transaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());
    //System.out.println(
    //    "exchange_inject_another_amount:" + transactionInfo.getExchangeInjectAnotherAmount());
    assertTrue(transactionInfo.getExchangeInjectAnotherAmount() > 0);
  }

  @Order(3)
  @Test
  void testExchangeWithdraw() throws IllegalException, InterruptedException {
    TransactionExtention transactionExtention = client.exchangeWithdraw(testAddress, exchangeID,
        tokenId, 10_000_000L);

    Transaction transaction = client.signTransaction(transactionExtention);
    String txId = client.broadcastTransaction(transaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());
    //System.out.println(
    //    "exchange_withdraw_another_amount:" + transactionInfo.getExchangeWithdrawAnotherAmount());
    assertTrue(transactionInfo.getExchangeWithdrawAnotherAmount() > 0);
  }

  @Order(4)
  @Test
  void testExchangeTransaction() throws IllegalException, InterruptedException {
    //expected should be smaller than left value in that exchange.
    TransactionExtention transactionExtention = client.exchangeTransaction(testAddress, exchangeID,
        "_", 20_000_000L, 1_000_000L);

    Transaction transaction = client.signTransaction(transactionExtention);
    String txId = client.broadcastTransaction(transaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());
    //System.out.println(
    //    "exchange_received_amount:" + transactionInfo.getExchangeReceivedAmount());
    assertTrue(transactionInfo.getExchangeReceivedAmount() > 0);
  }
}
