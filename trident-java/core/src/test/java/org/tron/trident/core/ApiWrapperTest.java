package org.tron.trident.core;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Chain.Block;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Contract.TriggerSmartContract;
import org.tron.trident.proto.Response.BlockExtention;
import org.tron.trident.proto.Response.ExchangeList;
import org.tron.trident.proto.Response.ProposalList;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionReturn;

class ApiWrapperTest {

  private static final String CONFIG_FILE = "application-test.properties";
  private static ApiWrapper client;
  private static Properties properties;

  @BeforeAll
  static void setUp() {
    try {
      // load config
      properties = loadConfig();
      String privateKey = properties.getProperty("tron.private-key");
      String network = properties.getProperty("tron.network", "nile"); // default

      // init client
      if ("mainnet".equals(network)) {
        client = ApiWrapper.ofMainnet(privateKey);
      } else {
        client = ApiWrapper.ofNile(privateKey);
      }
    } catch (IOException e) {
      throw new RuntimeException("load config failed", e);
    }
  }

  private static Properties loadConfig() throws IOException {
    Properties props = new Properties();
    try (InputStream input = ApiWrapperTest.class.getClassLoader()
        .getResourceAsStream(CONFIG_FILE)) {
      if (input == null) {
        throw new IOException("can't find config file : " + CONFIG_FILE);
      }
      props.load(input);
    }
    return props;
  }

  @AfterAll
  static void tearDown() {
    if (client != null) {
      client.close();
    }
  }

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
}
