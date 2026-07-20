package org.tron.trident.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.utils.Utils;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Response.BlockExtention;
import org.tron.trident.proto.Response.TransactionExtention;

/**
 * Functional tests verifying that ApiWrapper instances constructed via ApiWrapperBuilder
 * behave correctly under different configuration scenarios.
 */
class ApiWrapperBuilderFunctionalTest {
  KeyPair keyPair = KeyPair.generate();

  @Test
  void testConstructorWithThreeParameters() {
    // Test the deprecated constructor with three parameters
    ApiWrapper client = new ApiWrapperBuilder(Constant.FULLNODE_NILE,
        Constant.FULLNODE_NILE_SOLIDITY, keyPair.toPrivateKey()).build();

    assertNotNull(client);
    assertNotNull(client.keyPair);
    assertEquals(keyPair.toPrivateKey(), client.keyPair.toPrivateKey());
    assertNotNull(client.blockingStub);
    assertNotNull(client.blockingStubSolidity);

    BlockExtention blockExtention = client.getBlock(false);
    assertNotNull(blockExtention);
    assertTrue(blockExtention.getTransactionsCount() >= 0);

    blockExtention = client.getBlock(false, NodeType.SOLIDITY_NODE);
    assertNotNull(blockExtention);
    assertTrue(blockExtention.getTransactionsCount() >= 0);

    client.close();
  }

  @Test
  void testQueryWithFullNode() {
    ApiWrapper client = new ApiWrapperBuilder(Constant.FULLNODE_NILE).build();

    assertNotNull(client);
    assertNull(client.keyPair);
    assertNotNull(client.blockingStub);
    assertNull(client.blockingStubSolidity);

    BlockExtention blockExtention = client.getBlock(false);
    assertNotNull(blockExtention);
    assertTrue(blockExtention.getTransactionsCount() >= 0);

    try {
      blockExtention = client.getBlock(false, NodeType.SOLIDITY_NODE);
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalStateException);
    }

    client.close();
  }

  @Test
  void testQueryWithFullNodeAndOtherParams() {
    ApiWrapper client = new ApiWrapperBuilder(Constant.FULLNODE_NILE)
        .withGrpcEndpointSolidity(Constant.FULLNODE_NILE_SOLIDITY)
        .withTimeout(5000)
        .build();

    assertNotNull(client);
    assertNull(client.keyPair);
    assertNotNull(client.blockingStub);
    assertNotNull(client.blockingStubSolidity);

    BlockExtention blockExtention = client.getBlock(false);
    assertNotNull(blockExtention);
    assertTrue(blockExtention.getTransactionsCount() >= 0);

    blockExtention = client.getBlock(false, NodeType.SOLIDITY_NODE);
    assertNotNull(blockExtention);
    assertTrue(blockExtention.getTransactionsCount() >= 0);

    client.close();
  }

  @Test
  void testQueryOfNile() {
    ApiWrapper client = ApiWrapper.ofNile(keyPair.toPrivateKey());

    BlockExtention blockExtention = client.getBlock(false);
    assertNotNull(blockExtention);
    assertTrue(blockExtention.getTransactionsCount() >= 0);

    blockExtention = client.getBlock(false, NodeType.SOLIDITY_NODE);
    assertNotNull(blockExtention);
    assertTrue(blockExtention.getTransactionsCount() >= 0);

    client.close();
  }

  @Test
  @Disabled("server need support ssl")
  void testTls() {
    ApiWrapper client = new ApiWrapperBuilder("localhost:443").withTLS().build();

    BlockExtention blockExtention = client.getBlock(false);
    assertNotNull(blockExtention);
    assertTrue(blockExtention.getTransactionsCount() >= 0);
    client.close();
  }

  @Test
  void testWithOutSolidityNode() {
    String toAddress = KeyPair.generate().toBase58CheckAddress();
    ApiWrapper client = new ApiWrapperBuilder(Constant.FULLNODE_NILE).build();
    assertNotNull(client);

    try {
      client.getNowBlockSolidity();
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalStateException);
      assertEquals("the channelSolidity is null or closed", e.getMessage());
    }

    try {
      client.getRewardSolidity(keyPair.toBase58CheckAddress());
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalStateException);
      assertEquals("the channelSolidity is null or closed", e.getMessage());
    }

    try {
      client.getEnergyPricesOnSolidity();
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalStateException);
      assertEquals("the channelSolidity is null or closed", e.getMessage());
    }

    BlockExtention blockExtention = client.getBlock(false);
    assertNotNull(blockExtention);

    try {
      client.transfer(keyPair.toBase58CheckAddress(), toAddress, 10);
      fail();
    } catch (Exception e) {
      assertEquals("createTransactionExtention error,the channelSolidity is null or closed",
          e.getMessage());
    }

    client.enableLocalCreate(Utils.getBlockId(blockExtention),
        blockExtention.getBlockHeader().getRawData().getTimestamp() + 60);
    try {
      TransactionExtention transactionExtention
          = client.transfer(keyPair.toBase58CheckAddress(), toAddress, 10);
      client.signTransaction(transactionExtention, keyPair);
    } catch (IllegalException e) {
      throw new RuntimeException(e);
    }
    client.disableLocalCreate();
    client.close();
  }


  @Test
  void testWithOutPrivateKey() throws IllegalException {
    ApiWrapper client = new ApiWrapperBuilder(Constant.FULLNODE_NILE)
        .withGrpcEndpointSolidity(Constant.FULLNODE_NILE_SOLIDITY).build();
    assertNotNull(client);
    TransactionExtention transactionExtention
        = client.transfer(keyPair.toBase58CheckAddress(),
        KeyPair.generate().toBase58CheckAddress(), 10);
    try {
      client.signTransaction(transactionExtention);
      fail();
    } catch (Exception e) {
      assertEquals("keyPair is null, should set privateKey", e.getMessage());
    }

    // passing an explicit null keyPair is an argument error, not a missing config
    try {
      client.signTransaction(transactionExtention, null);
      fail();
    } catch (Exception e) {
      assertEquals("keyPair is null", e.getMessage());
    }
    Transaction signTransaction = client.signTransaction(transactionExtention, keyPair);
    assertNotNull(signTransaction);

    // for deployContract, should set privateKey before
    try {
      client.deployContract("testDeployContract", "",
          "bytecode", null, 150_000_000L,
          0, 1_000_000L,
          0, null, 0);
      fail();

    } catch (Exception e) {
      assertEquals("keyPair is null, should set privateKey", e.getMessage());
    }

    client.close();
  }

  @Test
  @SuppressWarnings("deprecation")
  void testDeprecatedConstructors() {
    // a null interceptor list fails fast at construction
    try {
      new ApiWrapper(Constant.FULLNODE_NILE,
          Constant.FULLNODE_NILE_SOLIDITY, keyPair.toPrivateKey(), null, 5000);
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
      assertEquals("interceptors is null", e.getMessage());
    }

    // an empty apiKey fails fast at construction
    try {
      new ApiWrapper(Constant.FULLNODE_NILE,
          Constant.FULLNODE_NILE_SOLIDITY, keyPair.toPrivateKey(), "");
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
      assertEquals("apiKey is empty", e.getMessage());
    }

    // "0x"-prefixed keys are accepted
    ApiWrapper clientPrefixedKey = new ApiWrapper(Constant.FULLNODE_NILE,
        Constant.FULLNODE_NILE_SOLIDITY, "0x" + keyPair.toPrivateKey());
    assertEquals(keyPair.toPrivateKey(), clientPrefixedKey.keyPair.toPrivateKey());
    clientPrefixedKey.close();
  }

  @Test
  void testWithPrivateKey() throws IllegalException {
    ApiWrapper client = new ApiWrapperBuilder(Constant.FULLNODE_NILE)
        .withGrpcEndpointSolidity(Constant.FULLNODE_NILE_SOLIDITY)
        .withPrivateKey(keyPair.toPrivateKey()).build();
    assertNotNull(client);
    TransactionExtention transactionExtention
        = client.transfer(keyPair.toBase58CheckAddress(),
        KeyPair.generate().toBase58CheckAddress(), 10);

    Transaction signTransaction = client.signTransaction(transactionExtention);
    assertNotNull(signTransaction);

    client.close();
  }

}


