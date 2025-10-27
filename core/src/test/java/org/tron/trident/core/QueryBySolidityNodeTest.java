package org.tron.trident.core;

import com.google.protobuf.ByteString;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.api.GrpcAPI.NumberMessage;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.utils.ByteArray;
import org.tron.trident.proto.Chain.Block;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Contract.AssetIssueContract;
import org.tron.trident.proto.Response.Account;
import org.tron.trident.proto.Response.AssetIssueList;
import org.tron.trident.proto.Response.BlockExtention;
import org.tron.trident.proto.Response.DelegatedResourceAccountIndex;
import org.tron.trident.proto.Response.DelegatedResourceList;
import org.tron.trident.proto.Response.Exchange;
import org.tron.trident.proto.Response.ExchangeList;
import org.tron.trident.proto.Response.MarketOrder;
import org.tron.trident.proto.Response.MarketOrderList;
import org.tron.trident.proto.Response.MarketOrderPairList;
import org.tron.trident.proto.Response.MarketPriceList;
import org.tron.trident.proto.Response.PricesResponseMessage;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionInfoList;
import org.tron.trident.proto.Response.Witness;
import org.tron.trident.proto.Response.WitnessList;
import org.tron.trident.utils.Base58Check;

import static org.junit.jupiter.api.Assertions.*;

class QueryBySolidityNodeTest {
  private static ApiWrapper client;
  private static String testAddress;

  @BeforeAll
  static void setUp() {
    client = ApiWrapper.ofNile(ApiWrapper.generateAddress().toPrivateKey());
    testAddress = client.keyPair.toBase58CheckAddress();
  }

  @AfterAll
  static void tearDown() {
    if (client != null) {
      client.close();
    }
  }

  @Test
  void testGetAccount() {
    Account accountSolidity = client.getAccount(testAddress, NodeType.SOLIDITY_NODE);
    assertTrue(accountSolidity.getAssetCount() >= 0);
  }

  @Test
  void testGetAvailableUnfreezeCount() {
    long countSolidity = client.getAvailableUnfreezeCount(testAddress, NodeType.SOLIDITY_NODE);
    assertTrue(countSolidity >= 0);
  }

  @Test
  void testGetCanWithdrawUnfreezeAmount() {
    // Test query from latest block
    long amountSolidity = client.getCanWithdrawUnfreezeAmount(testAddress, NodeType.SOLIDITY_NODE);
    assertTrue(amountSolidity >= 0);

    // Test query with timestamp
    long timestamp = System.currentTimeMillis();
    long amountWithTimeSolidity
        = client.getCanWithdrawUnfreezeAmount(testAddress, timestamp, NodeType.SOLIDITY_NODE);
    assertTrue(amountWithTimeSolidity >= 0);
  }

  @Test
  void testGetCanDelegatedMaxSize() {
    long maxSizeSolidity = client.getCanDelegatedMaxSize(testAddress, 0, NodeType.SOLIDITY_NODE);
    assertTrue(maxSizeSolidity >= 0);
  }

  @Test
  void testGetDelegatedResourceV2() {
    DelegatedResourceList resourceListSolidity = client.getDelegatedResourceV2(
        testAddress, testAddress, NodeType.SOLIDITY_NODE);
    assertNotNull(resourceListSolidity);
    assertTrue(resourceListSolidity.getDelegatedResourceCount() >= 0);
  }

  @Test
  void testGetDelegatedResourceAccountIndexV2() throws IllegalException {
    DelegatedResourceAccountIndex indexSolidity = client.getDelegatedResourceAccountIndexV2(
        testAddress, NodeType.SOLIDITY_NODE);
    assertNotNull(indexSolidity);
    assertTrue(indexSolidity.getToAccountsCount() >= 0);
  }

  @Test
  void testGetNowBlock() throws IllegalException {
    Block blockSolidity = client.getNowBlock(NodeType.SOLIDITY_NODE);
    assertNotNull(blockSolidity);
    assertTrue(blockSolidity.getBlockHeader().getRawData().getNumber() > 0);
  }

  @Test
  void testGetBlockByNum() throws IllegalException {
    BlockExtention blockSolidity = client.getBlockByNum(55157371, NodeType.SOLIDITY_NODE);
    assertNotNull(blockSolidity);
    assertEquals(55157371, blockSolidity.getBlockHeader().getRawData().getNumber());
  }

  @Test
  void testGetTransactionInfoByBlockNum() throws IllegalException {
    TransactionInfoList infoListSolidity
        = client.getTransactionInfoByBlockNum(55157371, NodeType.SOLIDITY_NODE);
    assertNotNull(infoListSolidity);
    assertTrue(infoListSolidity.getTransactionInfoCount() > 0);
  }

  @Test
  void testGetTransactionInfoById() throws IllegalException {
    //usdt transfer tx
    String txId = "d3c0afaf7db3ca7a6713d15331b397be781d6e57356ced46324ad1dc66b6c4c0";
    TransactionInfo infoSolidity = client.getTransactionInfoById(txId, NodeType.SOLIDITY_NODE);
    assertNotNull(infoSolidity);
    assertTrue(infoSolidity.getFee() > 0);
    assertTrue(infoSolidity.getLogCount() > 0);
  }

  @Test
  void testGetTransactionById() throws IllegalException {
    String txId = "d3c0afaf7db3ca7a6713d15331b397be781d6e57356ced46324ad1dc66b6c4c0";
    Transaction txnSolidity = client.getTransactionById(txId, NodeType.SOLIDITY_NODE);
    assertNotNull(txnSolidity);
    assertTrue(txnSolidity.getRawData().getTimestamp() > 0);
  }

  @Test
  void testGetAccountById() {
    String accountId = "test1743388741490"; //TFzqPiME2TSY9akvpPbFijt7QMrU2y2Jaz
    Account accountSolidity = client.getAccountById(accountId, NodeType.SOLIDITY_NODE);
    assertNotNull(accountSolidity);
    assertTrue(accountSolidity.isInitialized());
  }

  @Test
  void testGetDelegatedResource() {
    DelegatedResourceList resourceListSolidity = client.getDelegatedResource(
        testAddress, testAddress, NodeType.SOLIDITY_NODE);
    assertNotNull(resourceListSolidity);
    assertTrue(resourceListSolidity.getDelegatedResourceCount() >= 0);
  }

  @Test
  void testGetDelegatedResourceAccountIndex() {
    DelegatedResourceAccountIndex indexSolidity = client.getDelegatedResourceAccountIndex(
        testAddress, NodeType.SOLIDITY_NODE);
    assertNotNull(indexSolidity);
    assertTrue(indexSolidity.getToAccountsCount() >= 0);
  }

//  @Test
//  void testGetAssetIssueList() {
//    AssetIssueList assetIssueList = client.getAssetIssueList(NodeType.SOLIDITY_NODE);
//    assertTrue(assetIssueList.getAssetsCount() > 0);
//  }

  @Test
  void testGetPaginatedAssetIssueList() {
    AssetIssueList assetIssueList = client.getPaginatedAssetIssueList(0,10,NodeType.SOLIDITY_NODE);
    assertTrue(assetIssueList.getAssetsCount() > 0);

  }

  @Test
  void testGetAssetIssueById() {
    AssetIssueContract assetIssueContract
        = client.getAssetIssueById("1000587", NodeType.SOLIDITY_NODE);
    assertEquals(assetIssueContract.getId(), "1000587");

  }

  @Test
  void testGetAssetIssueListByName() {
    AssetIssueList assetIssueList
        = client.getAssetIssueListByName("KKK", NodeType.SOLIDITY_NODE);
    assertTrue(assetIssueList.getAssetsCount() > 0);
  }

  @Test
  void testListWitnesses() {
    WitnessList witnessList = client.listWitnesses(NodeType.SOLIDITY_NODE);
    assertTrue(witnessList.getWitnessesCount() >= 0);
  }

  @Disabled("only enable this after java-tron v4.8.1 has been released")
  @Test
  void testGetPaginatedNowWitnessList() throws InterruptedException {
    int retryCount = 0;
    int maxRetries = 1;
    do {
      try {
        WitnessList witnessList
                = client.getPaginatedNowWitnessList(0, 10, NodeType.SOLIDITY_NODE);
        assertNotNull(witnessList);
        assertTrue(witnessList.getWitnessesCount() >= 0);
        break;
      } catch (Exception e) {
        retryCount++;
        Thread.sleep(6500);  //maintenance period is 6s, sleep
      }
    } while (retryCount <= maxRetries);

    if (retryCount > maxRetries) {
      fail("getPaginatedNowWitnessList failed after " + maxRetries + " retries");
    }
  }

  @Test
  void testListExchanges() {
    ExchangeList exchangeList = client.listExchanges(NodeType.SOLIDITY_NODE);
    assertTrue(exchangeList.getExchangesCount() > 0);
  }

  @Test
  void testGetExchangeById() throws IllegalException {
    Exchange exchange = client.getExchangeById("1", NodeType.SOLIDITY_NODE);
    assertTrue(exchange.getFirstTokenBalance() >= 0);
  }

  @Test
  void testGetBrokerageInfo() {
    WitnessList witnessList = client.listWitnesses(NodeType.SOLIDITY_NODE);
    Witness witness = witnessList.getWitnessesList().get(0);
    String address = Base58Check.bytesToBase58(witness.getAddress().toByteArray());
    long ratio = client.getBrokerageInfo(address, NodeType.SOLIDITY_NODE);
    assertTrue(ratio >= 0);
  }

  @Test
  void testGetBurnTRX() {
    long num = client.getBurnTRX(NodeType.SOLIDITY_NODE);
    assertTrue(num > 0);
  }

  @Test
  void testTriggerConstantContract() {
    // transfer(address,uint256) returns (bool)
    String usdtAddr = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf"; //nile
    String fromAddr = client.keyPair.toBase58CheckAddress();
    String toAddress = "TVjsyZ7fYF3qLF6BQgPmTEZy1xrNNyVAAA";
    Function trc20Transfer = new Function("transfer",
        Arrays.asList(new Address(toAddress),
            new Uint256(BigInteger.valueOf(1).multiply(BigInteger.valueOf(10).pow(6)))),
        Collections.singletonList(new TypeReference<Bool>() {
        }));
    String encodedHex = FunctionEncoder.encode(trc20Transfer);
    try {
      client.triggerConstantContract(fromAddr, usdtAddr,
          encodedHex, -1L, 0L, null, NodeType.SOLIDITY_NODE);
      assert false;
    } catch (Exception e) {
      assert e instanceof IllegalArgumentException;
    }
    try {
      client.triggerConstantContract(fromAddr, usdtAddr,
          encodedHex, 0L, 0L, "999999", NodeType.SOLIDITY_NODE);
      assert false;
    } catch (Exception e) {
      assert e instanceof IllegalArgumentException;
    }
    TransactionExtention transactionExtention = client.triggerConstantContract(fromAddr, usdtAddr,
        encodedHex, 0L, 0L, null, NodeType.SOLIDITY_NODE);
    long energy = transactionExtention.getEnergyUsed();
    assertTrue(energy > 0);

    TransactionExtention transactionExtention2 = client.triggerConstantContract(fromAddr, usdtAddr,
        trc20Transfer, NodeType.SOLIDITY_NODE);
    long energy2 = transactionExtention2.getEnergyUsed();
    assertTrue(energy2 > 0);

    TransactionExtention transactionExtention3 = client.triggerConstantContract(fromAddr, usdtAddr,
        encodedHex, NodeType.SOLIDITY_NODE);
    long energy3 = transactionExtention3.getEnergyUsed();
    assertTrue(energy3 > 0);

  }

  @Test
  void testGetBandwidthPrices() {
    PricesResponseMessage pricesResponseMessage = client.getBandwidthPrices(NodeType.SOLIDITY_NODE);
    assertNotNull(pricesResponseMessage);
    assertNotNull(pricesResponseMessage.getPrices());
  }

  @Test
  void testGetEnergyPrices() {
    PricesResponseMessage pricesResponseMessage = client.getEnergyPrices(NodeType.SOLIDITY_NODE);
    assertNotNull(pricesResponseMessage);
    assertNotNull(pricesResponseMessage.getPrices());
  }

  @Test
  void testGetBlock() {
    BlockExtention blockExtention = client.getBlock("53506161", true, NodeType.SOLIDITY_NODE);
    assertEquals(1, blockExtention.getTransactionsList().size());

    blockExtention = client.getBlock("53506161", false, NodeType.SOLIDITY_NODE);
    assertEquals(0, blockExtention.getTransactionsList().size());

    blockExtention = client.getBlock(false, NodeType.SOLIDITY_NODE);
    assertEquals(0, blockExtention.getTransactionsList().size());
  }

  @Test
  void testGetMarketOrderByAccount() {
    String account = "TEqZpKG8cLquDHNVGcHXJhEQMoWE653nBH"; //nile
    MarketOrderList marketOrderList
        = client.getMarketOrderByAccount(account, NodeType.SOLIDITY_NODE);
    assertTrue(marketOrderList.getOrdersCount() > 0);
  }

  @Test
  void testGetMarketOrderListByPair() {
    MarketOrderList marketOrderList
        = client.getMarketOrderListByPair("1000012", "_", NodeType.SOLIDITY_NODE);
    assertTrue(marketOrderList.getOrdersCount() > 0);
  }

  @Test
  void testGetMarketOrderById() {
    String orderId = "4503c83790b5f739b58b94c28f1e98357c3dc98f6b6877c8ee792d3ea3a4465a";
    String ownerAddress = "TEqZpKG8cLquDHNVGcHXJhEQMoWE653nBH";
    MarketOrder marketOrder = client.getMarketOrderById(orderId, NodeType.SOLIDITY_NODE);
    assertEquals(marketOrder.getOrderId(), ByteString.copyFrom(ByteArray.fromHexString(orderId)));
    assertEquals(marketOrder.getOwnerAddress(), ApiWrapper.parseAddress(ownerAddress));
    assertEquals(marketOrder.getBuyTokenId(), ByteString.copyFrom("_".getBytes()));
    assertEquals(marketOrder.getSellTokenId(), ByteString.copyFrom("1000012".getBytes()));
  }

  @Test
  void testGetMarketPairList() {
    MarketOrderPairList marketOrderPairList = client.getMarketPairList(NodeType.SOLIDITY_NODE);
    assertTrue(marketOrderPairList.getOrderPairCount() > 0);
  }

  @Test
  void testGetMarketPriceByPair() {
    MarketPriceList marketPriceList
        = client.getMarketPriceByPair("1000012", "_", NodeType.SOLIDITY_NODE);
    assertTrue(marketPriceList.getPricesCount() > 0);
    assertTrue(marketPriceList.getPrices(0).getBuyTokenQuantity() >= 0);
  }

  @Test
  void testGetTransactionCountByBlockNum() {
    assertTrue(client.getTransactionCountByBlockNum(53598255, NodeType.SOLIDITY_NODE) > 0);
  }

  @Test
  void testGetNowBlock2() throws IllegalException {
    BlockExtention blockExtention = client.getNowBlock2(NodeType.SOLIDITY_NODE);
    assertTrue(blockExtention.getBlockHeader().getRawData().getNumber() > 0);
  }

  @Test
  void testGetRewardInfo() {
    NumberMessage numberMessage = client.getRewardInfo(testAddress, NodeType.SOLIDITY_NODE);
    assertTrue(numberMessage.getNum() >= 0);
  }

  @Test
  void testNodeType() {

    BlockExtention blockExtention = client.getBlock(false);
    assertNotNull(blockExtention);
    blockExtention = client.getBlock(false, NodeType.FULL_NODE);
    assertNotNull(blockExtention);
    blockExtention = client.getBlock(false, NodeType.SOLIDITY_NODE);
    assertNotNull(blockExtention);
//    try {
//      blockExtention = client.getBlock(false, null);
//    } catch (Exception exception) {
//      assertEquals(exception.getMessage(), "nodeType should not be null");
//    }

    try {
      blockExtention = client.getBlock(false, new NodeType[]{null});
    } catch (Exception exception) {
      assertEquals(exception.getMessage(), "nodeType element should not be null");
    }

    blockExtention = client.getBlock(false, new NodeType[]{});
    assertNotNull(blockExtention);

    try {
      blockExtention = client.getBlock(false, NodeType.SOLIDITY_NODE, NodeType.FULL_NODE);
    } catch (Exception exception) {
      assertEquals(exception.getMessage(), "only one nodeType is allowed");
    }

  }

}
