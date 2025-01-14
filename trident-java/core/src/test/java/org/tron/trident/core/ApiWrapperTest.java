package org.tron.trident.core;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.ByteString;
import io.grpc.ClientInterceptor;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.api.GrpcAPI.EmptyMessage;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.utils.ByteArray;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Chain.Block;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Contract.TriggerSmartContract;
import org.tron.trident.proto.Response.BlockExtention;
import org.tron.trident.proto.Response.ExchangeList;
import org.tron.trident.proto.Response.MarketOrder;
import org.tron.trident.proto.Response.MarketOrderList;
import org.tron.trident.proto.Response.MarketOrderPairList;
import org.tron.trident.proto.Response.MarketPriceList;
import org.tron.trident.proto.Response.ProposalList;
import org.tron.trident.proto.Response.SmartContractDataWrapper;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionReturn;

class ApiWrapperTest extends BaseTest{
  @Test
  void testGetNowBlockQuery() {
    BlockExtention block = client.blockingStub.getNowBlock2(EmptyMessage.newBuilder().build());

    System.out.println(block.getBlockHeader());
    assertTrue(block.getBlockHeader().getRawDataOrBuilder().getNumber() > 0);
  }

  @Test
  void testGetNowBlockQueryWithTimeout() throws IllegalException {
    List<ClientInterceptor> clientInterceptors = new ArrayList<>();
    ApiWrapper client = new ApiWrapper(Constant.FULLNODE_NILE, Constant.FULLNODE_NILE_SOLIDITY,
        KeyPair.generate().toPrivateKey(), clientInterceptors,
        2000);
    Chain.Block block = client.getNowBlock();

    System.out.println(block.getBlockHeader());
    assertTrue(block.getBlockHeader().getRawDataOrBuilder().getNumber() > 0);
  }

  @Test
  void testSendTrc20Transaction() {
    // transfer(address,uint256) returns (bool)
    String usdtAddr = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf"; //nile
    String fromAddr = client.keyPair.toBase58CheckAddress();
    String toAddress = "TVjsyZ7fYF3qLF6BQgPmTEZy1xrNNyVAAA";
    Function trc20Transfer = new Function("transfer",
        Arrays.asList(new Address(toAddress),
            new Uint256(BigInteger.valueOf(10).multiply(BigInteger.valueOf(10).pow(6)))),
        Arrays.asList(new TypeReference<Bool>() {
        }));

    String encodedHex = FunctionEncoder.encode(trc20Transfer);

    TriggerSmartContract trigger =
        TriggerSmartContract.newBuilder()
            .setOwnerAddress(ApiWrapper.parseAddress(fromAddr))
            .setContractAddress(ApiWrapper.parseAddress(usdtAddr))
            .setData(ApiWrapper.parseHex(encodedHex))
            .build();

    System.out.println("trigger:\n" + trigger);

    TransactionExtention txnExt = client.blockingStub.triggerContract(trigger);
    System.out.println("txn id => " + Hex.toHexString(txnExt.getTxid().toByteArray()));

    Transaction signedTxn = client.signTransaction(txnExt);

    System.out.println(signedTxn.toString());
    TransactionReturn ret = client.blockingStub.broadcastTransaction(signedTxn);
    System.out.println("======== Result ========\n" + ret.toString());
  }

  @Test
  void testGenerateAddress() {
    ApiWrapper.generateAddress();
  }

  @Test
  void testGetBlock() {
    BlockExtention blockExtention = client.getBlock("53506161", true);
    assertEquals(1, blockExtention.getTransactionsList().size());

    blockExtention = client.getBlock("53506161", false);
    assertEquals(0, blockExtention.getTransactionsList().size());
  }

  @Test
  void testGetBlockByIdOrNum() {
    Block block1 = client.getBlockByIdOrNum("53506161");
    Block block2 = client.getBlockByIdOrNum(
        "00000000033070712deeda7d1e4d9ee89ba0ad083a7570a20ac6b5200c180259");
    assertNotNull(block1);
    assertNotNull(block2);
  }

  @Test
  void testGetPaginatedProposalList() {
    ProposalList proposalList = client.getPaginatedProposalList(0, 10);
    assertTrue(proposalList.getProposalsCount() > 0);
  }

  @Test
  void testGetPaginatedExchangeList() {
    ExchangeList exchangeList = client.getPaginatedExchangeList(0, 10);
    assertTrue(exchangeList.getExchangesCount() > 0);
  }

  @Test
  void testGetContractInfo() {
    String usdtAddr = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf"; //nile
    SmartContractDataWrapper smartContractDataWrapper = client.getContractInfo(usdtAddr);
    assertEquals(smartContractDataWrapper.getSmartContract().getName(), "TetherToken");
    assertTrue(smartContractDataWrapper.getSmartContract().getAbi().getEntrysCount() > 0);
    assertEquals(smartContractDataWrapper.getSmartContract().getContractAddress(),
        ApiWrapper.parseAddress(usdtAddr));
  }

  @Test
  void testGetMarketOrderByAccount() {
    String account = "TEqZpKG8cLquDHNVGcHXJhEQMoWE653nBH"; //nile
    MarketOrderList marketOrderList = client.getMarketOrderByAccount(account);
    System.out.println(marketOrderList.getOrdersCount());
    System.out.println(marketOrderList.getOrders(0).getOrderId());
    assertTrue(marketOrderList.getOrdersCount() > 0);
  }

  @Test
  void testGetMarketOrderListByPair() {
    MarketOrderList marketOrderList = client.getMarketOrderListByPair("1000012", "_");
    assertTrue(marketOrderList.getOrdersCount() > 0);
    //4503c83790b5f739b58b94c28f1e98357c3dc98f6b6877c8ee792d3ea3a4465a
    System.out.println("orderId: " +
        ByteArray.toHexString(marketOrderList.getOrders(0).getOrderId().toByteArray()));

    String addr = ByteArray.toHexString(
        marketOrderList.getOrders(0).getOwnerAddress().toByteArray());
    System.out.println("ownerAddress:" + addr);
    Address address = new Address(addr);
    //TEqZpKG8cLquDHNVGcHXJhEQMoWE653nBH
    System.out.println(address);
  }

  @Test
  void testGetMarketOrderById() throws IllegalException {
    String orderId = "4503c83790b5f739b58b94c28f1e98357c3dc98f6b6877c8ee792d3ea3a4465a";
    String ownerAddress = "TEqZpKG8cLquDHNVGcHXJhEQMoWE653nBH";
    MarketOrder marketOrder = client.getMarketOrderById(orderId);
    assertEquals(marketOrder.getOrderId(), ApiWrapper.parseHex(orderId));
    assertEquals(marketOrder.getOwnerAddress(), ApiWrapper.parseAddress(ownerAddress));
    assertEquals(marketOrder.getBuyTokenId(), ByteString.copyFrom("_".getBytes()));
    assertEquals(marketOrder.getSellTokenId(), ByteString.copyFrom("1000012".getBytes()));
  }

  @Test
  void testGetMarketPairList() {
    MarketOrderPairList marketOrderPairList = client.getMarketPairList();
    assertTrue(marketOrderPairList.getOrderPairCount() > 0);
    String buyTokenId = marketOrderPairList.getOrderPair(0).getBuyTokenId().toStringUtf8();
    String sellTokenId = marketOrderPairList.getOrderPair(0).getSellTokenId().toStringUtf8();
    System.out.println(buyTokenId);
    System.out.println(sellTokenId);
  }

  @Test
  void testGetMarketPriceByPair() {
    MarketPriceList marketPriceList = client.getMarketPriceByPair("1000012", "_");
    assertTrue(marketPriceList.getPricesCount() > 0);
    assertTrue(marketPriceList.getPrices(0).getBuyTokenQuantity() >= 0);
  }

  @Test
  void testGetTransactionCountByBlockNum() {
    assertTrue(client.getTransactionCountByBlockNum(53598255) > 0);
  }

  @Test
  void testTriggerContract() throws InterruptedException, IllegalException {
    // transfer(address,uint256) returns (bool)
    String usdtAddr = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf"; //nile
    String fromAddr = client.keyPair.toBase58CheckAddress();
    String toAddress = "TVjsyZ7fYF3qLF6BQgPmTEZy1xrNNyVAAA";
    Function trc20Transfer = new Function("transfer",
        Arrays.asList(new Address(toAddress),
            new Uint256(BigInteger.valueOf(10).multiply(BigInteger.valueOf(10).pow(6)))),
        Collections.singletonList(new TypeReference<Bool>() {
        }));
    TransactionExtention transactionExtention = client.triggerContract(fromAddr, usdtAddr,
        trc20Transfer);

    Transaction signedTxn = client.signTransaction(transactionExtention);

    System.out.println(signedTxn.toString());
    String ret = client.broadcastTransaction(signedTxn);
    System.out.println("======== Result ========\n" + ret);
    sleep(10_000L);
    TransactionInfo transactionInfo = client.getTransactionInfoById(ret);
    assertEquals(0, transactionInfo.getResult().getNumber());

  }

  @Test
  void testTriggerContractWithBroadcast() throws InterruptedException, IllegalException {
    //  function deposit() external payable returns (uint256 strxAmount);
    String strx = "TZ8du1HkatTWDbS6FLZei4dQfjfpSm9mxp"; //nile
    String fromAddr = client.keyPair.toBase58CheckAddress();
    Function depositFunction = new Function("deposit",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));
    String ret = client.triggerContractWithBroadcast(fromAddr, strx, depositFunction, 100, 500_000_000);
    System.out.println(ret);
    sleep(10_000L);
    TransactionInfo transactionInfo = client.getTransactionInfoById(ret);
    assertEquals(0, transactionInfo.getResult().getNumber());
  }

}
