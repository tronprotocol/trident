package org.tron.trident.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.grpc.ClientInterceptor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.util.encoders.Hex;
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
import org.tron.trident.proto.Response.TransactionReturn;

public class ApiWrapperTest {

  @Test
  public void testGetNowBlockQuery() {
    ApiWrapper client = ApiWrapper.ofShasta(KeyPair.generate().toPrivateKey());
    BlockExtention block = client.blockingStub.getNowBlock2(EmptyMessage.newBuilder().build());

    System.out.println(block.getBlockHeader());
    assertTrue(block.getBlockHeader().getRawDataOrBuilder().getNumber() > 0);
  }

  @Test
  public void testGetNowBlockQueryWithTimeout() throws IllegalException {
    List<ClientInterceptor> clientInterceptors = new ArrayList<>();
    ApiWrapper client = new ApiWrapper(Constant.FULLNODE_NILE, Constant.FULLNODE_NILE_SOLIDITY,
        KeyPair.generate().toPrivateKey(), clientInterceptors,
        2000);
    Chain.Block block = client.getNowBlock();

    System.out.println(block.getBlockHeader());
    assertTrue(block.getBlockHeader().getRawDataOrBuilder().getNumber() > 0);
  }

  @Test
  public void testSendTrc20Transaction() {
    ApiWrapper client = ApiWrapper.ofNile(KeyPair.generate().toPrivateKey());

    // transfer(address,uint256) returns (bool)
    Function trc20Transfer = new Function("transfer",
        Arrays.asList(new Address("TVjsyZ7fYF3qLF6BQgPmTEZy1xrNNyVAAA"),
            new Uint256(BigInteger.valueOf(10).multiply(BigInteger.valueOf(10).pow(18)))),
        Arrays.asList(new TypeReference<Bool>() {
        }));

    String encodedHex = FunctionEncoder.encode(trc20Transfer);

    TriggerSmartContract trigger =
        TriggerSmartContract.newBuilder()
            .setOwnerAddress(ApiWrapper.parseAddress("TJRabPrwbZy45sbavfcjinPJC18kjpRTv8"))
            .setContractAddress(ApiWrapper.parseAddress("TF17BgPaZYbz8oxbjhriubPDsA7ArKoLX3"))
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
  public void testGenerateAddress() {
    ApiWrapper.generateAddress();
  }

  @Test
  void testGetBlock() {
    ApiWrapper client = ApiWrapper.ofNile(KeyPair.generate().toPrivateKey());
    BlockExtention blockExtention = client.getBlock("53506161", true);
    assertEquals(1, blockExtention.getTransactionsList().size());

    blockExtention = client.getBlock("53506161", false);
    assertEquals(0, blockExtention.getTransactionsList().size());

    client.close();
  }

  @Test
  void testGetBlockByIdOrNum() {
    ApiWrapper client = ApiWrapper.ofNile(KeyPair.generate().toPrivateKey());
    Block block1 = client.getBlockByIdOrNum("53506161");
    Block block2 = client.getBlockByIdOrNum(
        "00000000033070712deeda7d1e4d9ee89ba0ad083a7570a20ac6b5200c180259");
    assertNotNull(block1);
    assertNotNull(block2);
    assertArrayEquals(block1.getBlockHeader().getRawData().toByteArray(),
        block2.getBlockHeader().getRawData().toByteArray());

    client.close();
  }

  @Test
  void testGetPaginatedProposalList() {
    ApiWrapper client = ApiWrapper.ofNile(KeyPair.generate().toPrivateKey());
    ProposalList proposalList = client.getPaginatedProposalList(0, 10);
    assertTrue(proposalList.getProposalsCount() > 0);
    client.close();
  }

  @Test
  void testGetPaginatedExchangeList() {
    ApiWrapper client = ApiWrapper.ofNile(KeyPair.generate().toPrivateKey());
    ExchangeList exchangeList = client.getPaginatedExchangeList(0, 10);
    assertTrue(exchangeList.getExchangesCount() > 0);
    client.close();
  }
}
