package org.tron.trident.core;


import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;

class ContractTest {

  @Test
  @Disabled
  void testTransferTrc10() throws InterruptedException {
    ApiWrapper client = ApiWrapper.ofNile(
        "the private key of from TEPRbQxXQEpHpeEx8tK5xHVs7NWudAAZgu");
    //specify your private key yourself
    TransactionExtention transactionExtention;
    try {
      transactionExtention = client.transferTrc10("TEPRbQxXQEpHpeEx8tK5xHVs7NWudAAZgu",
          "TAB1TVw5N8g1FLcKxPD17h2A3eEpSXvMQd", 1000587, 100);
    } catch (IllegalException e) {
      assert false;
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
    assertEquals(0, transactionInfo.getResult().getNumber());
  }
}
