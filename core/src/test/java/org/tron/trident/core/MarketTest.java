package org.tron.trident.core;


import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.utils.ByteArray;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Response.MarketOrder;
import org.tron.trident.proto.Response.MarketOrder.State;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionInfo.code;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("add private key to enable this case")
public class MarketTest extends BaseTest {

  private String orderId;

  @Order(1)
  @Test
  void testMarketSellAsset() throws IllegalException, InterruptedException {
    //test MarketSellAssetContract
    TransactionExtention txExt = client.marketSellAsset(testAddress, "_",
        100_000_000L, tokenId, 50_000_000L);

    Transaction transaction = client.signTransaction(txExt);
    String txId = client.broadcastTransaction(transaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());
    orderId = ByteArray.toHexString(transactionInfo.getOrderId().toByteArray());

    //System.out.println("orderId: " + orderId);
    MarketOrder marketOrder = client.getMarketOrderById(orderId);
    assertEquals(State.ACTIVE, marketOrder.getState());
  }

  @Order(2)
  @Test
  void testMarketCancelOrder() throws IllegalException, InterruptedException {
    // test MarketCancelOrderContract
    TransactionExtention txExt = client.marketCancelOrder(testAddress, orderId);

    Transaction transaction = client.signTransaction(txExt);
    String txId = client.broadcastTransaction(transaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());

    MarketOrder marketOrder = client.getMarketOrderById(orderId);
    assertEquals(State.CANCELED, marketOrder.getState());
  }

}
