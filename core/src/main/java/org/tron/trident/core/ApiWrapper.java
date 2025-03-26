package org.tron.trident.core;

import static org.tron.trident.core.Constant.TRANSACTION_DEFAULT_EXPIRATION_TIME;
import static org.tron.trident.core.utils.TokenValidator.validateCallValue;
import static org.tron.trident.core.utils.TokenValidator.validateTokenId;
import static org.tron.trident.core.utils.TokenValidator.validateTokenValue;
import static org.tron.trident.core.utils.Utils.encodeParameter;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.api.GrpcAPI;
import org.tron.trident.api.GrpcAPI.AccountAddressMessage;
import org.tron.trident.api.GrpcAPI.AccountIdMessage;
import org.tron.trident.api.GrpcAPI.BlockLimit;
import org.tron.trident.api.GrpcAPI.BlockReq;
import org.tron.trident.api.GrpcAPI.BytesMessage;
import org.tron.trident.api.GrpcAPI.EmptyMessage;
import org.tron.trident.api.GrpcAPI.NumberMessage;
import org.tron.trident.api.GrpcAPI.PaginatedMessage;
import org.tron.trident.api.WalletGrpc;
import org.tron.trident.api.WalletSolidityGrpc;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.ContractFunction;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.interceptor.TimeoutInterceptor;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.transaction.BlockId;
import org.tron.trident.core.transaction.TransactionBuilder;
import org.tron.trident.core.transaction.TransactionCapsule;
import org.tron.trident.core.utils.ByteArray;
import org.tron.trident.core.utils.Sha256Hash;
import org.tron.trident.core.utils.Utils;
import org.tron.trident.proto.Chain.Block;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType;
import org.tron.trident.proto.Common.SmartContract;
import org.tron.trident.proto.Contract.AccountCreateContract;
import org.tron.trident.proto.Contract.AccountPermissionUpdateContract;
import org.tron.trident.proto.Contract.AccountUpdateContract;
import org.tron.trident.proto.Contract.AssetIssueContract;
import org.tron.trident.proto.Contract.CancelAllUnfreezeV2Contract;
import org.tron.trident.proto.Contract.ClearABIContract;
import org.tron.trident.proto.Contract.CreateSmartContract;
import org.tron.trident.proto.Contract.DelegateResourceContract;
import org.tron.trident.proto.Contract.ExchangeCreateContract;
import org.tron.trident.proto.Contract.ExchangeInjectContract;
import org.tron.trident.proto.Contract.ExchangeTransactionContract;
import org.tron.trident.proto.Contract.ExchangeWithdrawContract;
import org.tron.trident.proto.Contract.FreezeBalanceContract;
import org.tron.trident.proto.Contract.FreezeBalanceV2Contract;
import org.tron.trident.proto.Contract.MarketCancelOrderContract;
import org.tron.trident.proto.Contract.MarketSellAssetContract;
import org.tron.trident.proto.Contract.ParticipateAssetIssueContract;
import org.tron.trident.proto.Contract.ProposalApproveContract;
import org.tron.trident.proto.Contract.ProposalCreateContract;
import org.tron.trident.proto.Contract.ProposalDeleteContract;
import org.tron.trident.proto.Contract.SetAccountIdContract;
import org.tron.trident.proto.Contract.TransferAssetContract;
import org.tron.trident.proto.Contract.TransferContract;
import org.tron.trident.proto.Contract.TriggerSmartContract;
import org.tron.trident.proto.Contract.UnDelegateResourceContract;
import org.tron.trident.proto.Contract.UnfreezeAssetContract;
import org.tron.trident.proto.Contract.UnfreezeBalanceContract;
import org.tron.trident.proto.Contract.UnfreezeBalanceV2Contract;
import org.tron.trident.proto.Contract.UpdateAssetContract;
import org.tron.trident.proto.Contract.UpdateBrokerageContract;
import org.tron.trident.proto.Contract.UpdateEnergyLimitContract;
import org.tron.trident.proto.Contract.UpdateSettingContract;
import org.tron.trident.proto.Contract.VoteWitnessContract;
import org.tron.trident.proto.Contract.WithdrawBalanceContract;
import org.tron.trident.proto.Contract.WithdrawExpireUnfreezeContract;
import org.tron.trident.proto.Contract.WitnessCreateContract;
import org.tron.trident.proto.Contract.WitnessUpdateContract;
import org.tron.trident.proto.Response;
import org.tron.trident.proto.Response.Account;
import org.tron.trident.proto.Response.AccountNetMessage;
import org.tron.trident.proto.Response.AccountResourceMessage;
import org.tron.trident.proto.Response.AssetIssueList;
import org.tron.trident.proto.Response.BlockBalanceTrace;
import org.tron.trident.proto.Response.BlockExtention;
import org.tron.trident.proto.Response.BlockIdentifier;
import org.tron.trident.proto.Response.BlockListExtention;
import org.tron.trident.proto.Response.ChainParameters;
import org.tron.trident.proto.Response.DelegatedResourceAccountIndex;
import org.tron.trident.proto.Response.DelegatedResourceList;
import org.tron.trident.proto.Response.DelegatedResourceMessage;
import org.tron.trident.proto.Response.Exchange;
import org.tron.trident.proto.Response.ExchangeList;
import org.tron.trident.proto.Response.MarketOrder;
import org.tron.trident.proto.Response.MarketOrderList;
import org.tron.trident.proto.Response.MarketOrderPair;
import org.tron.trident.proto.Response.MarketOrderPairList;
import org.tron.trident.proto.Response.MarketPriceList;
import org.tron.trident.proto.Response.NodeInfo;
import org.tron.trident.proto.Response.NodeList;
import org.tron.trident.proto.Response.Proposal;
import org.tron.trident.proto.Response.ProposalList;
import org.tron.trident.proto.Response.SmartContractDataWrapper;
import org.tron.trident.proto.Response.TransactionApprovedList;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionInfoList;
import org.tron.trident.proto.Response.TransactionReturn;
import org.tron.trident.proto.Response.TransactionSignWeight;
import org.tron.trident.proto.Response.WitnessList;
import org.tron.trident.utils.Base58Check;
import org.tron.trident.utils.Numeric;

/**
 * A {@code ApiWrapper} object is the entry point for calling the functions.
 *
 * <p>A {@code ApiWrapper} object is bind with a private key and a full node.
 * {@link #broadcastTransaction}, {@link #signTransaction} and other transaction related
 * operations can be done via a {@code ApiWrapper} object.</p>
 *
 * @see org.tron.trident.core.contract.Contract
 * @see org.tron.trident.proto.Chain.Transaction
 * @see org.tron.trident.proto.Contract
 * @since java version 1.8.0_231
 */

public class ApiWrapper implements Api {

  public final WalletGrpc.WalletBlockingStub blockingStub;
  public final WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity;
  public final KeyPair keyPair;
  public final ManagedChannel channel;
  public final ManagedChannel channelSolidity;

  /**
   * Specify whether to createTransaction locally (default false) without grpc request. If false, we
   * need to query referHeadBlockId and head block time through grpc api in method
   * {@link #createTransaction}. {@link #referHeadBlockId} and {@link #expireTimeStamp} must be
   * valid when it is true.
   */
  @Getter
  private boolean enableLocalCreateTx = false;
  /**
   * Used to set refer block number and hash when {@link #createTransaction} only if
   * {@link #enableLocalCreateTx} = true. If false, use the highest solidity BlockId instead.
   */
  @Getter
  private BlockId referHeadBlockId;
  /**
   * Used to set transaction's expiration timestamp (milliseconds) when {@link #createTransaction} only if
   * {@link #enableLocalCreateTx} = true. If false, use the timestamp of latest head BlockId +
   * TRANSACTION_DEFAULT_EXPIRATION_TIME instead.
   */
  @Getter
  private long expireTimeStamp = -1;

  public ApiWrapper(String grpcEndpoint, String grpcEndpointSolidity, String hexPrivateKey) {
    channel = ManagedChannelBuilder.forTarget(grpcEndpoint).usePlaintext().build();
    channelSolidity = ManagedChannelBuilder.forTarget(grpcEndpointSolidity).usePlaintext().build();
    blockingStub = WalletGrpc.newBlockingStub(channel);
    blockingStubSolidity = WalletSolidityGrpc.newBlockingStub(channelSolidity);
    keyPair = new KeyPair(hexPrivateKey);
  }

  public ApiWrapper(String grpcEndpoint, String grpcEndpointSolidity, String hexPrivateKey,
      String apiKey) {
    channel = ManagedChannelBuilder.forTarget(grpcEndpoint).usePlaintext().build();
    channelSolidity = ManagedChannelBuilder.forTarget(grpcEndpointSolidity).usePlaintext().build();

    //attach api key
    Metadata header = new Metadata();
    Metadata.Key<String> key = Metadata.Key.of("TRON-PRO-API-KEY",
        Metadata.ASCII_STRING_MARSHALLER);
    header.put(key, apiKey);

    //create a client to interceptor to attach the custom metadata headers
    blockingStub = WalletGrpc.newBlockingStub(channel)
        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(header));
    blockingStubSolidity = WalletSolidityGrpc.newBlockingStub(channelSolidity)
        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(header));

    keyPair = new KeyPair(hexPrivateKey);
  }

  public ApiWrapper(String grpcEndpoint, String grpcEndpointSolidity, String hexPrivateKey,
      List<ClientInterceptor> clientInterceptors) {
    channel = ManagedChannelBuilder.forTarget(grpcEndpoint)
        .intercept(clientInterceptors)
        .usePlaintext()
        .build();
    channelSolidity = ManagedChannelBuilder.forTarget(grpcEndpointSolidity).usePlaintext().build();
    blockingStub = WalletGrpc.newBlockingStub(channel);
    blockingStubSolidity = WalletSolidityGrpc.newBlockingStub(channelSolidity);
    keyPair = new KeyPair(hexPrivateKey);
  }

  /*
     constructor enable setting timeout
   */
  public ApiWrapper(String grpcEndpoint, String grpcEndpointSolidity, String hexPrivateKey,
      int timeout) {
    channel = ManagedChannelBuilder
        .forTarget(grpcEndpoint)
        .usePlaintext()
        .intercept(new TimeoutInterceptor(timeout))
        .build();
    channelSolidity = ManagedChannelBuilder
        .forTarget(grpcEndpointSolidity)
        .usePlaintext()
        .intercept(new TimeoutInterceptor(timeout))
        .build();
    blockingStub = WalletGrpc.newBlockingStub(channel);
    blockingStubSolidity = WalletSolidityGrpc.newBlockingStub(channelSolidity);
    keyPair = new KeyPair(hexPrivateKey);
  }

  /*
     constructor enable setting timeout and custom interceptors
   */
  public ApiWrapper(String grpcEndpoint, String grpcEndpointSolidity, String hexPrivateKey,
      List<ClientInterceptor> clientInterceptors, int timeout) {

    List<ClientInterceptor> clientInterceptorList = new ArrayList<>();
    // first set timeout to ensure the configuration takes effect
    clientInterceptorList.add(new TimeoutInterceptor(timeout));

    if (clientInterceptors != null) {
      clientInterceptorList.addAll(
          clientInterceptors.stream()
              .filter(Objects::nonNull)
              .collect(Collectors.toList())
      );
    }

    channel =
        ManagedChannelBuilder.forTarget(grpcEndpoint)
            .usePlaintext()
            .intercept(clientInterceptorList)
            .build();
    channelSolidity =
        ManagedChannelBuilder.forTarget(grpcEndpointSolidity)
            .usePlaintext()
            .intercept(clientInterceptorList)
            .build();
    blockingStub = WalletGrpc.newBlockingStub(channel);
    blockingStubSolidity = WalletSolidityGrpc.newBlockingStub(channelSolidity);
    keyPair = new KeyPair(hexPrivateKey);
  }

  /**
   * The constructor for main net. Use TronGrid as default
   *
   * @param hexPrivateKey the binding private key. Operations require private key will all use this unless the private key is specified elsewhere.
   * @param apiKey this function works with TronGrid, an API key is required.
   * @return a ApiWrapper object
   */
  public static ApiWrapper ofMainnet(String hexPrivateKey, String apiKey) {
    return new ApiWrapper(Constant.TRONGRID_MAIN_NET, Constant.TRONGRID_MAIN_NET_SOLIDITY,
        hexPrivateKey, apiKey);
  }

  /**
   * The constructor for main net.
   *
   * @param hexPrivateKey the binding private key. Operations require private key will all use this unless the private key is specified elsewhere.
   * @return a ApiWrapper object
   * @deprecated Since 0.9.2, scheduled for removal in future versions.
   * This method will only be available before TronGrid prohibits the use without API key
   */
  @Deprecated
  public static ApiWrapper ofMainnet(String hexPrivateKey) {
    return new ApiWrapper(Constant.TRONGRID_MAIN_NET, Constant.TRONGRID_MAIN_NET_SOLIDITY,
        hexPrivateKey);
  }

  /**
   * The constructor for Shasta test net. Use TronGrid as default.
   *
   * @param hexPrivateKey the binding private key. Operations require private key will all use this unless the private key is specified elsewhere.
   * @return a ApiWrapper object
   */
  public static ApiWrapper ofShasta(String hexPrivateKey) {
    return new ApiWrapper(Constant.TRONGRID_SHASTA, Constant.TRONGRID_SHASTA_SOLIDITY,
        hexPrivateKey);
  }

  /**
   * The constructor for Nile test net.
   *
   * @param hexPrivateKey the binding private key. Operations require private key will all use this unless the private key is specified elsewhere.
   * @return a ApiWrapper object
   */
  public static ApiWrapper ofNile(String hexPrivateKey) {
    return new ApiWrapper(Constant.FULLNODE_NILE, Constant.FULLNODE_NILE_SOLIDITY, hexPrivateKey);
  }

  /**
   * enable local create transaction.
   * @param blockId refer blockId used in createTransaction. It will be invalid after 65535 blocks
   * so remember to update it timely.
   * @param expireTime transaction's absolute expire timestamp in createTransaction, milliseconds.
   */
  public synchronized void enableLocalCreate(BlockId blockId, long expireTime) {
    this.enableLocalCreateTx = true;
    this.referHeadBlockId = blockId;
    this.expireTimeStamp = expireTime;
  }

  public synchronized void disableLocalCreate() {
    this.enableLocalCreateTx = false;
    this.referHeadBlockId = null;
    this.expireTimeStamp = -1;
  }

  public synchronized void setReferHeadBlockId(BlockId blockId) {
    if (!enableLocalCreateTx) {
      throw new RuntimeException(
          "Must enable local create transaction before set referHeadBlockId");
    }
    referHeadBlockId = blockId;
  }

  public synchronized void setExpireTimeStamp(long expireTime) {
    if (!enableLocalCreateTx) {
      throw new RuntimeException(
          "Must enable local create transaction before set expireTimeStamp");
    }
    expireTimeStamp = expireTime;
  }

  /**
   * Generate random address
   *
   * @return A list, inside are the public key and private key
   */
  public static KeyPair generateAddress() {
    // generate random address
    return KeyPair.generate();
  }

  /**
   * The function receives addresses in any formats.
   *
   * @param address account or contract address in any allowed formats.
   * @return hex address
   */
  public static ByteString parseAddress(String address) {
    byte[] raw;
    if (address.startsWith("T")) {
      raw = Base58Check.base58ToBytes(address);
    } else {
      raw = ByteArray.fromHexString(address);
    }
    return ByteString.copyFrom(raw);
  }

  public static byte[] calculateTransactionHash(Transaction txn) {
    SHA256.Digest digest = new SHA256.Digest();
    digest.update(txn.getRawData().toByteArray());
    return digest.digest();
  }

  public static ByteString parseHex(String hexString) {
    return ByteString.copyFrom(ByteArray.fromHexString(hexString));
  }

  public static String toHex(byte[] raw) {
    return ByteArray.toHexString(raw);
  }

  public static String toHex(ByteString raw) {
    return toHex(raw.toByteArray());
  }

  public static VoteWitnessContract createVoteWitnessContract(ByteString ownerAddress,
      Map<String, String> votes) {
    VoteWitnessContract.Builder builder = VoteWitnessContract.newBuilder();
    builder.setOwnerAddress(ownerAddress);
    for (String addressBase58 : votes.keySet()) {
      String voteCount = votes.get(addressBase58);
      long count = Long.parseLong(voteCount);
      VoteWitnessContract.Vote.Builder voteBuilder = VoteWitnessContract.Vote.newBuilder();
      ByteString voteAddress = parseAddress(addressBase58);
      voteBuilder.setVoteAddress(voteAddress);
      voteBuilder.setVoteCount(count);
      builder.addVotes(voteBuilder.build());
    }
    return builder.build();
  }

  public static AccountUpdateContract createAccountUpdateContract(ByteString accountName,
      ByteString address) {
    AccountUpdateContract.Builder builder = AccountUpdateContract.newBuilder();

    builder.setAccountName(accountName);
    builder.setOwnerAddress(address);

    return builder.build();
  }

  public static AccountCreateContract createAccountCreateContract(
      ByteString owner, ByteString address) {
    AccountCreateContract.Builder builder = AccountCreateContract.newBuilder();
    builder.setOwnerAddress(owner);
    builder.setAccountAddress(address);

    return builder.build();
  }

  public static SetAccountIdContract createSetAccountIdContract(
      ByteString accountId, ByteString address) {
    SetAccountIdContract.Builder builder = SetAccountIdContract.newBuilder();
    builder.setAccountId(accountId);
    builder.setOwnerAddress(address);

    return builder.build();
  }

  public static UpdateAssetContract createUpdateAssetContract(
      ByteString address, ByteString description, ByteString url, long newLimit,
      long newPublicLimit) {
    UpdateAssetContract.Builder builder = UpdateAssetContract.newBuilder();
    builder.setDescription(description);
    builder.setUrl(url);
    builder.setNewLimit(newLimit);
    builder.setNewPublicLimit(newPublicLimit);
    builder.setOwnerAddress(address);

    return builder.build();
  }

  public static UnfreezeAssetContract createUnfreezeAssetContract(ByteString address) {

    UnfreezeAssetContract.Builder builder = UnfreezeAssetContract.newBuilder();
    builder.setOwnerAddress(address);
    return builder.build();
  }

  public void close() {
    channel.shutdown();
    channelSolidity.shutdown();
  }

  @Override
  public Transaction signTransaction(TransactionExtention txnExt, KeyPair keyPair) {
    byte[] txId = txnExt.getTxid().toByteArray();
    byte[] signature = KeyPair.signTransaction(txId, keyPair);
    return txnExt.getTransaction().toBuilder().addSignature(ByteString.copyFrom(signature)).build();
  }

  @Override
  public Transaction signTransaction(Transaction txn, KeyPair keyPair) {
    byte[] txId = calculateTransactionHash(txn);
    byte[] signature = KeyPair.signTransaction(txId, keyPair);
    return txn.toBuilder().addSignature(ByteString.copyFrom(signature)).build();
  }

  @Override
  public Transaction signTransaction(TransactionExtention txnExt) {
    return signTransaction(txnExt, keyPair);
  }

  @Override
  public Transaction signTransaction(Transaction txn) {
    return signTransaction(txn, keyPair);
  }

  private TransactionCapsule createTransactionCapsuleWithoutValidate(
      Message message, Transaction.Contract.ContractType contractType,
      BlockId solidHeadBlockId, long expireTimeStamp) throws Exception {
    TransactionCapsule trx = new TransactionCapsule(message, contractType);

    if (contractType == Transaction.Contract.ContractType.CreateSmartContract) {
      //trx.setTransactionCreate(true);
      CreateSmartContract contract = Utils.getSmartContractFromTransaction(
          trx.getTransaction());
      if (contract == null) {
        throw new Exception("contract is null");
      }
      long percent = contract.getNewContract().getConsumeUserResourcePercent();
      if (percent < 0 || percent > 100) {
        throw new Exception("percent must be >= 0 and <= 100");
      }
    }
    //build transaction
    trx.setTransactionCreate(false);
    //get solid head blockId
    trx.setReference(solidHeadBlockId.getNum(), solidHeadBlockId.getBytes());
    trx.setExpiration(expireTimeStamp);
    trx.setTimestamp();

    return trx;
  }

  private TransactionCapsule createTransaction(
      Message message, Transaction.Contract.ContractType contractType) throws Exception {
    BlockId solidHeadBlockId;
    long transactionExpireTimeStamp;
    if (enableLocalCreateTx) {
      if (referHeadBlockId == null) {
        throw new RuntimeException("referHeadBlockId must not be null");
      }
      if (expireTimeStamp <= 0) {
        throw new RuntimeException("expireTimeStamp must be > 0");
      }
      solidHeadBlockId = referHeadBlockId;
      transactionExpireTimeStamp = expireTimeStamp;
    } else {
      BlockReq blockReq = BlockReq.newBuilder().setDetail(false).build();
      BlockExtention solidHeadBlock = blockingStubSolidity.getBlock(blockReq);
      solidHeadBlockId = Utils.getBlockId(solidHeadBlock);

      BlockExtention headBlock = blockingStub.getBlock(blockReq);
      transactionExpireTimeStamp = headBlock.getBlockHeader().getRawData().getTimestamp()
          + TRANSACTION_DEFAULT_EXPIRATION_TIME;
    }
    return createTransactionCapsuleWithoutValidate(message, contractType,
        solidHeadBlockId, transactionExpireTimeStamp);
  }

  /**
   * build Transaction Extention in local.
   *
   * @param contractType transaction type.
   * @param request transaction message object.
   */
  @Override
  public TransactionExtention createTransactionExtention(Message request,
      Transaction.Contract.ContractType contractType) throws IllegalException {

    return createTransactionExtention(request, contractType, 0L);
  }

  /**
   * build Transaction Extention in local.
   *
   * @param contractType transaction type.
   * @param request transaction message object.
   * @param feeLimit fee unit:SUN, only used in CreateSmartContract and TriggerSmartContract
   */
  private TransactionExtention createTransactionExtention(Message request,
      Transaction.Contract.ContractType contractType, long feeLimit) throws IllegalException {
    TransactionExtention.Builder trxExtBuilder = TransactionExtention.newBuilder();
    TransactionReturn.Builder retBuilder = TransactionReturn.newBuilder();
    try {
      TransactionCapsule trx = createTransaction(request, contractType);

      if (contractType != Transaction.Contract.ContractType.CreateSmartContract
          && contractType != ContractType.TriggerSmartContract) {
        trxExtBuilder.setTransaction(trx.getTransaction());
      } else {
        if (feeLimit <= 0L) {
          throw new IllegalException("feeLimit must be > 0");
        } else {
          Transaction transaction = trx.getTransaction();
          Transaction newTransaction = transaction.toBuilder()
              .setRawData(transaction.getRawData().toBuilder().setFeeLimit(feeLimit).build())
              .build();
          trxExtBuilder.setTransaction(newTransaction);
        }
      }

      trxExtBuilder.setTxid(ByteString.copyFrom(
          Sha256Hash.hash(true, trxExtBuilder.getTransaction().getRawData().toByteArray())));
      retBuilder.setResult(true).setCode(TransactionReturn.response_code.SUCCESS);
    } catch (Exception e) {
      throw new IllegalException("createTransactionExtention error," + e.getMessage());
    }
    trxExtBuilder.setResult(retBuilder);
    return trxExtBuilder.build();
  }

  /**
   * Estimate the bandwidth consumption of the transaction.
   * Please note that bandwidth estimations are based on signed transactions.
   *
   * @param txn the transaction to be estimated.
   */
  @Override
  public long estimateBandwidth(Transaction txn) {
    return txn.toBuilder().clearRet().build().getSerializedSize() + 64L;
  }

  /**
   * Resolve the result code from TransactionReturn objects.
   *
   * @param code the result code.
   * @return the corresponding message.
   */
  private String resolveResultCode(int code) {
    TransactionReturn.response_code responseCode = TransactionReturn.response_code.forNumber(code);
    return responseCode != null ? responseCode.name() : "";
  }

  /**
   * broadcast a transaction with the binding account.
   *
   * @param txn a signed transaction ready to be broadcasted
   * @return a TransactionReturn object contains the broadcasting result
   * @throws RuntimeException if broadcastin fails
   */
  @Override
  public String broadcastTransaction(Transaction txn) throws RuntimeException {
    TransactionReturn ret = blockingStub.broadcastTransaction(txn);
    if (!ret.getResult()) {
      String errorMessage = new String(ret.getMessage().toByteArray());
      String message = resolveResultCode(ret.getCodeValue()) + ", " + errorMessage;
      //System.out.println(message);
      throw new RuntimeException(message);
    } else {
      byte[] txId = calculateTransactionHash(txn);
      return ByteArray.toHexString(txId);
    }
  }

  /**
   * Transfer TRX. amount in SUN
   *
   * @param fromAddress owner address
   * @param toAddress receive balance
   * @param amount transfer amount
   * @return TransactionExtention
   * @throws IllegalException if fail to transfer
   */
  @Override
  public TransactionExtention transfer(String fromAddress, String toAddress, long amount)
      throws IllegalException {

    ByteString rawFrom = parseAddress(fromAddress);
    ByteString rawTo = parseAddress(toAddress);

    TransferContract transferContract = TransferContract.newBuilder()
        .setOwnerAddress(rawFrom)
        .setToAddress(rawTo)
        .setAmount(amount)
        .build();
    return createTransactionExtention(transferContract,
        Transaction.Contract.ContractType.TransferContract);
  }

  /**
   * Transfers TRC10 Asset
   *
   * @param fromAddress owner address
   * @param toAddress receive balance
   * @param tokenId asset name
   * @param amount transfer amount
   * @return TransactionExtention
   * @throws IllegalException if fail to transfer trc10
   */
  @Override
  public TransactionExtention transferTrc10(String fromAddress, String toAddress, int tokenId,
      long amount) throws IllegalException {

    ByteString rawFrom = parseAddress(fromAddress);
    ByteString rawTo = parseAddress(toAddress);
    byte[] rawTokenId = Integer.toString(tokenId).getBytes();

    TransferAssetContract transferAssetContract = TransferAssetContract.newBuilder()
        .setOwnerAddress(rawFrom)
        .setToAddress(rawTo)
        .setAssetName(ByteString.copyFrom(rawTokenId))
        .setAmount(amount)
        .build();

    return createTransactionExtention(transferAssetContract,
        Transaction.Contract.ContractType.TransferAssetContract);
  }

  /**
   * Freeze balance to get energy or bandwidth, for 3 days
   *
   * @param ownerAddress owner address
   * @param frozenBalance frozen balance
   * @param frozenDuration frozen duration
   * @param resourceCode Resource type, can be 0("BANDWIDTH") or 1("ENERGY")
   * @return TransactionExtention
   * @throws IllegalException if fail to freeze balance
   */
  @Override
  public TransactionExtention freezeBalance(String ownerAddress, long frozenBalance,
      int frozenDuration, int resourceCode) throws IllegalException {

    return freezeBalance(ownerAddress, frozenBalance, frozenDuration, resourceCode, "");
  }

  /**
   * Freeze balance to get energy or bandwidth, for 3 days
   *
   * @param ownerAddress owner address
   * @param frozenBalance frozen balance
   * @param frozenDuration frozen duration
   * @param resourceCode Resource type, can be 0("BANDWIDTH") or 1("ENERGY")
   * @param receiveAddress the address that will receive the resource, default hexString
   * @return TransactionExtention
   * @throws IllegalException if fail to freeze balance
   */
  @Override
  public TransactionExtention freezeBalance(String ownerAddress, long frozenBalance,
      int frozenDuration, int resourceCode, String receiveAddress) throws IllegalException {
    ByteString rawFrom = parseAddress(ownerAddress);
    ByteString rawReceiveFrom = parseAddress(receiveAddress);
    FreezeBalanceContract freezeBalanceContract =
        FreezeBalanceContract.newBuilder()
            .setOwnerAddress(rawFrom)
            .setFrozenBalance(frozenBalance)
            .setFrozenDuration(frozenDuration)
            .setResourceValue(resourceCode)
            .setReceiverAddress(rawReceiveFrom)
            .build();
    return createTransactionExtention(freezeBalanceContract,
        Transaction.Contract.ContractType.FreezeBalanceContract);
  }

  /**
   * Stake2.0 API
   * Stake an amount of TRX to obtain bandwidth or energy, and obtain equivalent TRON Power(TP) according to the staked amount
   *
   * @param ownerAddress owner address
   * @param frozenBalance TRX stake amount, the unit is sun
   * @param resourceCode resource type, can be 0("BANDWIDTH") or 1("ENERGY")
   * @return TransactionExtention
   * @throws IllegalException if fail to freeze balance
   */
  @Override
  public TransactionExtention freezeBalanceV2(String ownerAddress, long frozenBalance,
      int resourceCode) throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    FreezeBalanceV2Contract freezeBalanceV2Contract =
        FreezeBalanceV2Contract.newBuilder()
            .setOwnerAddress(rawOwner)
            .setFrozenBalance(frozenBalance)
            .setResourceValue(resourceCode)
            .build();
    return createTransactionExtention(freezeBalanceV2Contract,
        Transaction.Contract.ContractType.FreezeBalanceV2Contract);
  }

  /**
   * Unfreeze balance to get TRX back
   *
   * @param ownerAddress owner address
   * @param resourceCode Resource type, can be 0("BANDWIDTH") or 1("ENERGY")
   * @return TransactionExtention
   * @throws IllegalException if fail to unfreeze balance
   */
  @Override
  public TransactionExtention unfreezeBalance(String ownerAddress, int resourceCode)
      throws IllegalException {

    return unfreezeBalance(ownerAddress, resourceCode, "");
  }

  /**
   * Unfreeze balance to get TRX back
   *
   * @param ownerAddress owner address
   * @param resourceCode Resource type, can be 0("BANDWIDTH") or 1("ENERGY")
   * @param receiveAddress the address that will lose the resource, default hexString
   * @return TransactionExtention
   * @throws IllegalException if fail to unfreeze balance
   */
  @Override
  public TransactionExtention unfreezeBalance(String ownerAddress, int resourceCode,
      String receiveAddress) throws IllegalException {

    UnfreezeBalanceContract unfreezeBalanceContract =
        UnfreezeBalanceContract.newBuilder()
            .setOwnerAddress(parseAddress(ownerAddress))
            .setResourceValue(resourceCode)
            .setReceiverAddress(parseAddress(receiveAddress))
            .build();

    return createTransactionExtention(unfreezeBalanceContract,
        Transaction.Contract.ContractType.UnfreezeBalanceContract);
  }

  /**
   * Stake2.0 API
   * Unstake some TRX, release the corresponding amount of bandwidth or energy, and voting rights (TP)
   *
   * @param ownerAddress owner address
   * @param unfreezeBalance the amount of TRX to unstake, in sun
   * @param resourceCode Resource type, can be 0("BANDWIDTH") or 1("ENERGY")
   * @return TransactionExtention
   * @throws IllegalException if fail to unfreeze balance
   */
  @Override
  public TransactionExtention unfreezeBalanceV2(String ownerAddress, long unfreezeBalance,
      int resourceCode) throws IllegalException {

    UnfreezeBalanceV2Contract unfreezeBalanceV2Contract =
        UnfreezeBalanceV2Contract.newBuilder()
            .setOwnerAddress(parseAddress(ownerAddress))
            .setResourceValue(resourceCode)
            .setUnfreezeBalance(unfreezeBalance)
            .build();

    return createTransactionExtention(unfreezeBalanceV2Contract,
        Transaction.Contract.ContractType.UnfreezeBalanceV2Contract);
  }

  /**
   * Stake2.0 API
   * Cancel all the unstaking transactions in the waiting period
   *
   * @param ownerAddress owner address
   * @return TransactionExtention
   * @throws IllegalException if fail to delegate resource
   */
  @Override
  public TransactionExtention cancelAllUnfreezeV2(String ownerAddress) throws IllegalException {

    CancelAllUnfreezeV2Contract cancelUnfreezeV2Contract =
        CancelAllUnfreezeV2Contract.newBuilder()
            .setOwnerAddress(parseAddress(ownerAddress))
            .build();

    return createTransactionExtention(cancelUnfreezeV2Contract,
        Transaction.Contract.ContractType.CancelAllUnfreezeV2Contract);
  }

  /**
   * Stake2.0 API
   * Delegate bandwidth or energy resources to other accounts
   *
   * @param ownerAddress owner address
   * @param balance Amount of TRX staked for resources to be delegated, unit is sun
   * @param resourceCode Resource type, can be 0("BANDWIDTH") or 1("ENERGY")
   * @param receiverAddress Resource receiver address
   * @param lock Whether it is locked, if it is set to true,
   * the delegated resources cannot be undelegated within 3 days.
   * When the lock time is not over, if the owner delegates the same type of resources using the lock to the same address,
   * the lock time will be reset to 3 days
   * @return TransactionExtention
   * @throws IllegalException if fail to delegate resource
   */
  @Override
  public TransactionExtention delegateResource(String ownerAddress, long balance, int resourceCode,
      String receiverAddress, boolean lock) throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    ByteString rawReceiver = parseAddress(receiverAddress);
    DelegateResourceContract delegateResourceContract =
        DelegateResourceContract.newBuilder()
            .setOwnerAddress(rawOwner)
            .setBalance(balance)
            .setReceiverAddress(rawReceiver)
            .setLock(lock)
            .setResourceValue(resourceCode)
            .build();
    return createTransactionExtention(delegateResourceContract,
        Transaction.Contract.ContractType.DelegateResourceContract);
  }

  /**
   * Stake2.0 API
   * Delegate bandwidth or energy resources to other accounts
   *
   * @param ownerAddress owner address
   * @param balance Amount of TRX staked for resources to be delegated, unit is sun
   * @param resourceCode Resource type, can be 0("BANDWIDTH") or 1("ENERGY")
   * @param receiverAddress Resource receiver address
   * @param lock Whether it is locked, if it is set to true,
   * the delegated resources cannot be undelegated within 3 days.
   * When the lock time is not over, if the owner delegates the same type of resources using the lock to the same address,
   * the lock time will be reset to 3 days
   * @param lockPeriod The lockup period, unit is blocks, data type is int256,
   * It indicates how many blocks the resource delegating is locked before it can be undelegated.
   * @return TransactionExtention
   * @throws IllegalException if fail to delegate resource
   */
  @Override
  public TransactionExtention delegateResourceV2(String ownerAddress, long balance,
      int resourceCode, String receiverAddress, boolean lock, long lockPeriod)
      throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    ByteString rawReceiver = parseAddress(receiverAddress);
    DelegateResourceContract delegateResourceContract =
        DelegateResourceContract.newBuilder()
            .setOwnerAddress(rawOwner)
            .setBalance(balance)
            .setReceiverAddress(rawReceiver)
            .setLock(lock)
            .setResourceValue(resourceCode)
            .build();
    if (lock) {
      delegateResourceContract = delegateResourceContract.toBuilder().setLockPeriod(lockPeriod)
          .build();
    }
    return createTransactionExtention(delegateResourceContract,
        Transaction.Contract.ContractType.DelegateResourceContract);
  }

  /**
   * Stake2.0 API
   * unDelegate resource
   *
   * @param ownerAddress owner address
   * @param balance Amount of TRX staked for resources to be delegated, unit is sun
   * @param resourceCode Resource type, can be 0("BANDWIDTH") or 1("ENERGY")
   * @param receiverAddress Resource receiver address
   * @return TransactionExtention
   * @throws IllegalException if fail to undelegate resource
   */
  @Override
  public TransactionExtention undelegateResource(String ownerAddress, long balance,
      int resourceCode, String receiverAddress) throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    ByteString rawReceiver = parseAddress(receiverAddress);
    UnDelegateResourceContract unDelegateResourceContract =
        UnDelegateResourceContract.newBuilder()
            .setOwnerAddress(rawOwner)
            .setBalance(balance)
            .setReceiverAddress(rawReceiver)
            .setResourceValue(resourceCode)
            .build();
    return createTransactionExtention(unDelegateResourceContract,
        Transaction.Contract.ContractType.UnDelegateResourceContract);
  }

  /**
   * Stake2.0 API
   * withdraw unfrozen balance
   *
   * @param ownerAddress owner address
   * @return TransactionExtention
   * @throws IllegalException if fail to withdraw
   */
  @Override
  public TransactionExtention withdrawExpireUnfreeze(String ownerAddress) throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    WithdrawExpireUnfreezeContract withdrawExpireUnfreezeContract =
        WithdrawExpireUnfreezeContract.newBuilder()
            .setOwnerAddress(rawOwner)
            .build();
    return createTransactionExtention(withdrawExpireUnfreezeContract,
        Transaction.Contract.ContractType.WithdrawExpireUnfreezeContract);
  }

  /**
   * Stake2.0 API
   * query remaining times of executing unstake operation
   *
   * @param ownerAddress owner address
   */
  @Override
  public long getAvailableUnfreezeCount(String ownerAddress) {
    ByteString rawOwner = parseAddress(ownerAddress);
    GrpcAPI.GetAvailableUnfreezeCountRequestMessage getAvailableUnfreezeCountRequestMessage =
        GrpcAPI.GetAvailableUnfreezeCountRequestMessage.newBuilder()
            .setOwnerAddress(rawOwner)
            .build();
    GrpcAPI.GetAvailableUnfreezeCountResponseMessage responseMessage =
        blockingStub.getAvailableUnfreezeCount(getAvailableUnfreezeCountRequestMessage);

    return responseMessage.getCount();
  }

  /**
   * Stake2.0 API
   * query the withdrawable balance at the latest block timestamp
   *
   * @param ownerAddress owner address
   */
  @Override
  public long getCanWithdrawUnfreezeAmount(String ownerAddress) {
    ByteString rawOwner = parseAddress(ownerAddress);
    GrpcAPI.CanWithdrawUnfreezeAmountRequestMessage getAvailableUnfreezeCountRequestMessage =
        GrpcAPI.CanWithdrawUnfreezeAmountRequestMessage.newBuilder()
            .setOwnerAddress(rawOwner)
            .build();
    GrpcAPI.CanWithdrawUnfreezeAmountResponseMessage responseMessage =
        blockingStub.getCanWithdrawUnfreezeAmount(
            getAvailableUnfreezeCountRequestMessage);

    return responseMessage.getAmount();
  }

  /**
   * Stake2.0 API
   * query the withdrawable balance at the specified timestamp
   *
   * @param ownerAddress owner address
   * @param timestamp specified timestamp, milliseconds
   */
  @Override
  public long getCanWithdrawUnfreezeAmount(String ownerAddress, long timestamp) {
    ByteString rawOwner = parseAddress(ownerAddress);
    GrpcAPI.CanWithdrawUnfreezeAmountRequestMessage getAvailableUnfreezeCountRequestMessage =
        GrpcAPI.CanWithdrawUnfreezeAmountRequestMessage.newBuilder()
            .setOwnerAddress(rawOwner)
            .setTimestamp(timestamp)
            .build();
    GrpcAPI.CanWithdrawUnfreezeAmountResponseMessage responseMessage =
        blockingStub.getCanWithdrawUnfreezeAmount(
            getAvailableUnfreezeCountRequestMessage);

    return responseMessage.getAmount();
  }

  /**
   * Stake2.0 API
   * query the amount of delegatable resources share of the specified resource type for an address, unit is sun.
   *
   * @param ownerAddress owner address
   * @param type resource type, 0 is bandwidth, 1 is energy
   */
  @Override
  public long getCanDelegatedMaxSize(String ownerAddress, int type) {
    ByteString rawFrom = parseAddress(ownerAddress);
    GrpcAPI.CanDelegatedMaxSizeRequestMessage getAvailableUnfreezeCountRequestMessage =
        GrpcAPI.CanDelegatedMaxSizeRequestMessage.newBuilder()
            .setOwnerAddress(rawFrom)
            .setType(type)
            .build();
    GrpcAPI.CanDelegatedMaxSizeResponseMessage responseMessage =
        blockingStub.getCanDelegatedMaxSize(getAvailableUnfreezeCountRequestMessage);

    return responseMessage.getMaxSize();
  }

  /**
   * Stake2.0 API
   * query the detail of resource share delegated from fromAddress to toAddress
   *
   * @param fromAddress from address
   * @param toAddress to address
   * @return DelegatedResourceList
   */
  @Override
  public DelegatedResourceList getDelegatedResourceV2(String fromAddress, String toAddress) {
    ByteString rawFrom = parseAddress(fromAddress);
    ByteString rawTo = parseAddress(toAddress);
    DelegatedResourceMessage delegatedResourceMessage =
        DelegatedResourceMessage.newBuilder()
            .setFromAddress(rawFrom)
            .setToAddress(rawTo)
            .build();
    return blockingStub.getDelegatedResourceV2(delegatedResourceMessage);
  }

  /**
   * Stake2.0 API
   * query the resource delegation index by an account.
   *
   * @param address address
   * @return DelegatedResourceAccountIndex
   * @throws IllegalException if fail to freeze balance
   */
  @Override
  public DelegatedResourceAccountIndex getDelegatedResourceAccountIndexV2(String address)
      throws IllegalException {
    ByteString rawAddress = parseAddress(address);
    BytesMessage request = BytesMessage.newBuilder()
        .setValue(rawAddress)
        .build();
    return blockingStub.getDelegatedResourceAccountIndexV2(
        request);
  }

  /**
   * Vote for witnesses
   *
   * @param ownerAddress owner address
   * @param votes map of vote address -> vote count
   * @return TransactionExtention
   * IllegalNumException if fail to vote witness
   */
  @Override
  public TransactionExtention voteWitness(String ownerAddress, HashMap<String, String> votes)
      throws IllegalException {
    ByteString rawFrom = parseAddress(ownerAddress);
    VoteWitnessContract voteWitnessContract = createVoteWitnessContract(rawFrom, votes);
    return createTransactionExtention(voteWitnessContract,
        Transaction.Contract.ContractType.VoteWitnessContract);
  }

  /**
   * Create an account. Uses an already activated account to create a new account
   *
   * @param ownerAddress owner address, an activated account
   * @param accountAddress the address of the new account
   * @return TransactionExtention
   * IllegalNumException if fail to create account
   */
  @Override
  public TransactionExtention createAccount(String ownerAddress, String accountAddress)
      throws IllegalException {
    ByteString bsOwnerAddress = parseAddress(ownerAddress);
    ByteString bsAccountAddress = parseAddress(accountAddress);

    AccountCreateContract accountCreateContract = createAccountCreateContract(bsOwnerAddress,
        bsAccountAddress);

    return createTransactionExtention(accountCreateContract,
        Transaction.Contract.ContractType.AccountCreateContract);
  }

  /**
   * Modify account name
   *
   * @param address owner address
   * @param accountName the name of the account
   * @return TransactionExtention
   * IllegalNumException if fail to update account name
   */
  //only if account.getAccountName() == null can update name
  @Override
  public TransactionExtention updateAccount(String address, String accountName)
      throws IllegalException {
    ByteString bsAddress = parseAddress(address);
    byte[] bytesAccountName = accountName.getBytes();
    ByteString bsAccountName = ByteString.copyFrom(bytesAccountName);

    AccountUpdateContract accountUpdateContract = createAccountUpdateContract(bsAccountName,
        bsAddress);

    return createTransactionExtention(accountUpdateContract,
        Transaction.Contract.ContractType.AccountUpdateContract);
  }

  /**
   * Query the latest block information
   *
   * @return Block
   * @throws IllegalException if fail to get now block
   */
  @Override
  public Block getNowBlock() throws IllegalException {
    Block block = blockingStub.getNowBlock(EmptyMessage.newBuilder().build());
    if (!block.hasBlockHeader()) {
      throw new IllegalException("Fail to get latest block.");
    }
    return block;
  }

  /**
   * Returns the Block Object corresponding to the 'Block Height' specified (number of blocks preceding it)
   *
   * @param blockNum The block height
   * @return BlockExtention block details
   * @throws IllegalException if the parameters are not correct
   */
  @Override
  public BlockExtention getBlockByNum(long blockNum) throws IllegalException {
    NumberMessage.Builder builder = NumberMessage.newBuilder();
    builder.setNum(blockNum);
    BlockExtention block = blockingStub.getBlockByNum2(builder.build());

    if (!block.hasBlockHeader()) {
      throw new IllegalException();
    }
    return block;
  }

  /**
   * Get some latest blocks
   *
   * @param num Number of latest blocks
   * @return BlockListExtention
   * @throws IllegalException if the parameters are not correct
   */
  @Override
  public BlockListExtention getBlockByLatestNum(long num) throws IllegalException {
    NumberMessage numberMessage = NumberMessage.newBuilder().setNum(num).build();
    BlockListExtention blockListExtention = blockingStub.getBlockByLatestNum2(numberMessage);

    if (blockListExtention.getBlockCount() == 0) {
      throw new IllegalException(
          "The number of latest blocks must be between 1 and 99, please check it.");
    }
    return blockListExtention;
  }

  /**
   * Returns the list of Block Objects included in the 'Block Height' range specified
   *
   * @param startNum Number of start block height, including this block
   * @param endNum Number of end block height, excluding this block
   * @return BlockListExtention
   * @throws IllegalException if the parameters are not correct
   */
  @Override
  public BlockListExtention getBlockByLimitNext(long startNum, long endNum)
      throws IllegalException {
    BlockLimit blockLimit = BlockLimit.newBuilder()
        .setStartNum(startNum)
        .setEndNum(endNum)
        .build();
    BlockListExtention blockListExtention = blockingStub.getBlockByLimitNext2(blockLimit);

    if (endNum - startNum > 100) {
      throw new IllegalException("The difference between startNum and endNum cannot be greater "
          + "than 100, please check it.");
    }
    if (blockListExtention.getBlockCount() == 0) {
      throw new IllegalException();
    }
    return blockListExtention;
  }

  /**
   * Get current API node info
   *
   * @return NodeInfo
   * @throws IllegalException if fail to get nodeInfo
   */
  @Override
  public NodeInfo getNodeInfo() throws IllegalException {
    NodeInfo nodeInfo = blockingStub.getNodeInfo(EmptyMessage.newBuilder().build());

    if (nodeInfo.getBlock().isEmpty()) {
      throw new IllegalException("Fail to get node info.");
    }
    return nodeInfo;
  }

  /**
   * List all nodes that current API node is connected to
   *
   * @return NodeList
   * @throws IllegalException if fail to get node list
   */
  @Override
  public NodeList listNodes() throws IllegalException {
    NodeList nodeList = blockingStub.listNodes(EmptyMessage.newBuilder().build());

    if (nodeList.getNodesCount() == 0) {
      throw new IllegalException("Fail to get node list.");
    }
    return nodeList;
  }

  /**
   * Get transactionInfo from block number
   *
   * @param blockNum The block height
   * @return TransactionInfoList
   * @throws IllegalException no transactions or the blockNum is incorrect
   */
  @Override
  public TransactionInfoList getTransactionInfoByBlockNum(long blockNum) throws IllegalException {
    if (blockNum < 0) {
      throw new IllegalException("blockNum must be >= 0");
    }
    NumberMessage numberMessage = NumberMessage.newBuilder().setNum(blockNum).build();
    return blockingStub.getTransactionInfoByBlockNum(numberMessage);
  }

  /**
   * Query the transaction fee, block height by transaction id
   *
   * @param txID Transaction hash, i.e. transaction id
   * @return TransactionInfo
   * @throws IllegalException if the parameters are not correct
   */
  @Override
  public TransactionInfo getTransactionInfoById(String txID) throws IllegalException {
    ByteString bsTxId = ByteString.copyFrom(ByteArray.fromHexString(txID));
    BytesMessage request = BytesMessage.newBuilder()
        .setValue(bsTxId)
        .build();
    TransactionInfo transactionInfo = blockingStub.getTransactionInfoById(request);

    if (transactionInfo.getBlockTimeStamp() == 0) {
      throw new IllegalException();
    }
    return transactionInfo;
  }

  /**
   * Query transaction information by transaction id
   *
   * @param txID Transaction hash, i.e. transaction id
   * @return Transaction
   * @throws IllegalException if the parameters are not correct
   */
  @Override
  public Transaction getTransactionById(String txID) throws IllegalException {
    ByteString bsTxId = ByteString.copyFrom(ByteArray.fromHexString(txID));
    BytesMessage request = BytesMessage.newBuilder()
        .setValue(bsTxId)
        .build();
    Transaction transaction = blockingStub.getTransactionById(request);

    if (transaction.getRetCount() == 0) {
      throw new IllegalException();
    }
    return transaction;
  }

  /**
   * Get account info by address
   *
   * @param address address, default hexString
   * @return Account
   */
  @Override
  public Account getAccount(String address) {
    ByteString bsAddress = parseAddress(address);
    AccountAddressMessage accountAddressMessage = AccountAddressMessage.newBuilder()
        .setAddress(bsAddress)
        .build();
    return blockingStub.getAccount(accountAddressMessage);
  }

  /**
   * Query the resource information of an account(bandwidth,energy,etc)
   *
   * @param address address, default hexString
   * @return AccountResourceMessage
   */
  @Override
  public AccountResourceMessage getAccountResource(String address) {
    ByteString bsAddress = parseAddress(address);
    AccountAddressMessage account = AccountAddressMessage.newBuilder()
        .setAddress(bsAddress)
        .build();
    return blockingStub.getAccountResource(account);
  }

  /**
   * Query bandwidth information
   *
   * @param address address, default hexString
   * @return AccountResourceMessage
   */
  @Override
  public AccountNetMessage getAccountNet(String address) {
    ByteString bsAddress = parseAddress(address);
    AccountAddressMessage account = AccountAddressMessage.newBuilder()
        .setAddress(bsAddress)
        .build();
    return blockingStub.getAccountNet(account);
  }

  @Override
  public long getAccountBalance(String address) {
    Account account = getAccount(address);
    return account.getBalance();
  }

  @Override
  public Account getAccountById(String id) {
    ByteString bsId = ByteString.copyFrom(id.getBytes());
    AccountIdMessage accountId = AccountIdMessage.newBuilder()
        .setId(bsId)
        .build();
    return blockingStub.getAccountById(accountId);
  }

  @Override
  public Transaction setAccountId(String id, String address) throws IllegalException {
    ByteString bsId = ByteString.copyFrom(id.getBytes());
    ByteString bsAddress = parseAddress(address);

    SetAccountIdContract setAccountIdContract = createSetAccountIdContract(bsId, bsAddress);

    return createTransactionExtention(setAccountIdContract,
        Transaction.Contract.ContractType.SetAccountIdContract).getTransaction();
  }

  //use this method instead of setAccountId
  @Override
  public TransactionExtention setAccountId2(String id, String address) throws IllegalException {
    ByteString bsId = ByteString.copyFrom(id.getBytes());
    ByteString bsAddress = parseAddress(address);

    SetAccountIdContract setAccountIdContract = createSetAccountIdContract(bsId, bsAddress);

    return createTransactionExtention(setAccountIdContract,
        Transaction.Contract.ContractType.SetAccountIdContract);
  }

  /**
   * All parameters that the blockchain committee can set
   *
   * @return ChainParameters
   * @throws IllegalException if fail to get chain parameters
   */
  @Override
  public ChainParameters getChainParameters() throws IllegalException {
    ChainParameters chainParameters = blockingStub.getChainParameters(
        EmptyMessage.newBuilder().build());

    if (chainParameters.getChainParameterCount() == 0) {
      throw new IllegalException("Fail to get chain parameters.");
    }
    return chainParameters;
  }

  /**
   * Returns all resources delegations from an account to another account. The fromAddress can be retrieved from the GetDelegatedResourceAccountIndex API
   *
   * @param fromAddress energy from address,, default hexString
   * @param toAddress energy delegation information, default hexString
   * @return DelegatedResourceList
   */
  @Override
  public DelegatedResourceList getDelegatedResource(String fromAddress,
      String toAddress) {

    ByteString fromAddressBS = parseAddress(fromAddress);
    ByteString toAddressBS = parseAddress(toAddress);

    DelegatedResourceMessage request = DelegatedResourceMessage.newBuilder()
        .setFromAddress(fromAddressBS)
        .setToAddress(toAddressBS)
        .build();
    return blockingStub.getDelegatedResource(request);
  }

  /**
   * Query the energy delegation by an account. i.e. list all addresses that have delegated resources to an account
   *
   * @param address address,, default hexString
   * @return DelegatedResourceAccountIndex
   */
  @Override
  public DelegatedResourceAccountIndex getDelegatedResourceAccountIndex(String address) {

    ByteString addressBS = parseAddress(address);

    BytesMessage bytesMessage = BytesMessage.newBuilder()
        .setValue(addressBS)
        .build();

    return blockingStub.getDelegatedResourceAccountIndex(
        bytesMessage);
  }

  /**
   * Query the list of all the TRC10 tokens
   *
   * @return AssetIssueList
   */
  @Override
  public AssetIssueList getAssetIssueList() {
    return blockingStub.getAssetIssueList(
        EmptyMessage.newBuilder().build());
  }

  /**
   * Query the list of all the tokens by pagination.
   *
   * @param offset the index of the start token
   * @param limit the amount of tokens per page
   * @return AssetIssueList, a list of Tokens that succeed the Token located at offset
   */
  @Override
  public AssetIssueList getPaginatedAssetIssueList(long offset, long limit) {
    PaginatedMessage pageMessage = PaginatedMessage.newBuilder()
        .setOffset(offset)
        .setLimit(limit)
        .build();

    return blockingStub.getPaginatedAssetIssueList(pageMessage);
  }

  /**
   * Query the TRC10 token information issued by an account
   *
   * @param address the Token Issuer account address
   * @return AssetIssueList, a list of Tokens that succeed the Token located at offset
   */
  @Override
  public AssetIssueList getAssetIssueByAccount(String address) {
    ByteString addressBS = parseAddress(address);
    AccountAddressMessage request = AccountAddressMessage.newBuilder()
        .setAddress(addressBS)
        .build();
    return blockingStub.getAssetIssueByAccount(request);
  }

  /**
   * Query a token by token id
   *
   * @param assetId the ID of the TRC10 token
   * @return AssetIssueContract, the token object, which contains the token name
   */
  @Override
  public AssetIssueContract getAssetIssueById(String assetId) {
    ByteString assetIdBs = ByteString.copyFrom(assetId.getBytes());
    BytesMessage request = BytesMessage.newBuilder()
        .setValue(assetIdBs)
        .build();

    return blockingStub.getAssetIssueById(request);
  }

  /**
   * Query a token by token name
   *
   * @param name the name of the TRC10 token
   * @return AssetIssueContract, the token object, which contains the token name
   */
  @Override
  public AssetIssueContract getAssetIssueByName(String name) {
    ByteString assetNameBs = ByteString.copyFrom(name.getBytes());
    BytesMessage request = BytesMessage.newBuilder()
        .setValue(assetNameBs)
        .build();

    return blockingStub.getAssetIssueByName(request);
  }

  /**
   * Query the list of all the TRC10 tokens by token name
   *
   * @param name the name of the TRC10 token
   * @return AssetIssueList
   */
  @Override
  public AssetIssueList getAssetIssueListByName(String name) {
    ByteString assetNameBs = ByteString.copyFrom(name.getBytes());
    BytesMessage request = BytesMessage.newBuilder()
        .setValue(assetNameBs)
        .build();

    return blockingStub.getAssetIssueListByName(request);
  }

  /**
   * Participate a token
   *
   * @param toAddress the issuer address of the token, default hexString
   * @param ownerAddress the participant address, default hexString
   * @param assertName token id, default hexString
   * @param amount participate token amount
   * @return TransactionExtention
   * @throws IllegalException if fail to participate AssetIssue
   */
  @Override
  public TransactionExtention participateAssetIssue(String toAddress, String ownerAddress,
      String assertName, long amount) throws IllegalException {

    ByteString bsTo = parseAddress(toAddress);
    ByteString bsOwner = parseAddress(ownerAddress);
    ByteString bsName = ByteString.copyFrom(assertName.getBytes());

    ParticipateAssetIssueContract participateAssetIssueContract =
        ParticipateAssetIssueContract.newBuilder()
            .setToAddress(bsTo)
            .setAssetName(bsName)
            .setOwnerAddress(bsOwner)
            .setAmount(amount)
            .build();

    return createTransactionExtention(participateAssetIssueContract,
        Transaction.Contract.ContractType.ParticipateAssetIssueContract);
  }

  /**
   * List all proposals
   *
   * @return ProposalList
   */
  @Override
  public ProposalList listProposals() {
    return blockingStub.listProposals(EmptyMessage.newBuilder().build());
  }

  /**
   * Query proposal based on ID
   *
   * @param id proposal id
   * @return Proposal, proposal details
   */
  //1-17
  @Override
  public Proposal getProposalById(String id) {
    ByteString bsTxId = ByteString.copyFrom(
        ByteArray.fromLong(Long.parseLong(id)));

    BytesMessage request = BytesMessage.newBuilder()
        .setValue(bsTxId)
        .build();
    return blockingStub.getProposalById(request);
  }

  /**
   * List all witnesses that current API node is connected to
   *
   * @return WitnessList
   */
  @Override
  public WitnessList listWitnesses() {
    return blockingStub
        .listWitnesses(EmptyMessage.newBuilder().build());
  }

  /**
   * List all exchange pairs
   *
   * @return ExchangeList
   */
  @Override
  public ExchangeList listExchanges() {
    return blockingStub.listExchanges(EmptyMessage.newBuilder().build());
  }

  /**
   * Query exchange pair based on id
   *
   * @param id transaction pair id
   * @return Exchange
   * @throws IllegalException if fail to get exchange pair
   */
  @Override
  public Exchange getExchangeById(String id) throws IllegalException {
    ByteString bsTxId = ByteString.copyFrom(
        ByteArray.fromLong(Long.parseLong(id)));

    BytesMessage request = BytesMessage.newBuilder()
        .setValue(bsTxId)
        .build();
    Exchange exchange = blockingStub.getExchangeById(request);

    if (exchange.getSerializedSize() == 0) {
      throw new IllegalException();
    }
    return exchange;
  }

  /**
   * Issue a token
   *
   * @param ownerAddress Owner address, default hexString
   * @param name Token name, default hexString
   * @param abbr Token name abbreviation, default hexString
   * @param totalSupply Token total supply
   * @param trxNum Define the price by the ratio of trx_num/num
   * @param icoNum Define the price by the ratio of trx_num/num
   * @param startTime ICO start time
   * @param endTime ICO end time
   * @param url Token official website url, default hexString
   * @param freeAssetNetLimit Token free asset net limit
   * @param publicFreeAssetNetLimit Token public free asset net limit
   * @param frozenSupply HashMap of frozenDay -> frozenAmount
   * @param description Token description, default hexString
   * @return TransactionExtention
   * @throws IllegalException if fail to create AssetIssue
   */
  @Override
  public TransactionExtention createAssetIssue(String ownerAddress, String name, String abbr,
      long totalSupply, int trxNum, int icoNum, long startTime, long endTime,
      String url, long freeAssetNetLimit,
      long publicFreeAssetNetLimit, int precision, HashMap<String, String> frozenSupply,
      String description) throws IllegalException {

    AssetIssueContract.Builder builder = assetIssueContractBuilder(ownerAddress, name, abbr,
        totalSupply, trxNum, icoNum, startTime, endTime, url, freeAssetNetLimit,
        publicFreeAssetNetLimit, precision, description);

    for (Entry<String, String> entry : frozenSupply.entrySet()) {
      String daysStr = entry.getKey();
      String amountStr = entry.getValue();
      long amount = Long.parseLong(amountStr);
      long days = Long.parseLong(daysStr);
      AssetIssueContract.FrozenSupply.Builder frozenBuilder = AssetIssueContract.FrozenSupply
          .newBuilder();
      frozenBuilder.setFrozenAmount(amount);
      frozenBuilder.setFrozenDays(days);
      builder.addFrozenSupply(frozenBuilder.build());
    }
    AssetIssueContract assetIssueContract = builder.build();
    return createTransactionExtention(assetIssueContract,
        Transaction.Contract.ContractType.AssetIssueContract);
  }

  /**
   * Issue a token
   *
   * @param ownerAddress Owner address, default hexString
   * @param name Token name, default hexString
   * @param abbr Token name abbreviation, default hexString
   * @param totalSupply Token total supply
   * @param trxNum Define the price by the ratio of trx_num/num
   * @param icoNum Define the price by the ratio of trx_num/num
   * @param startTime ICO start time
   * @param endTime ICO end time
   * @param url Token official website url, default hexString
   * @param freeAssetNetLimit Token free asset net limit
   * @param publicFreeAssetNetLimit Token public free asset net limit
   * @param description Token description, default hexString
   * @return TransactionExtention
   * @throws IllegalException if fail to create AssetIssue
   */
  @Override
  public TransactionExtention createAssetIssue(String ownerAddress, String name, String abbr,
      long totalSupply, int trxNum, int icoNum, long startTime, long endTime,
      String url, long freeAssetNetLimit,
      long publicFreeAssetNetLimit, int precision, String description) throws IllegalException {

    AssetIssueContract.Builder builder = assetIssueContractBuilder(ownerAddress, name, abbr,
        totalSupply, trxNum, icoNum, startTime, endTime, url, freeAssetNetLimit,
        publicFreeAssetNetLimit, precision, description);
    AssetIssueContract assetIssueContract = builder.build();
    return createTransactionExtention(assetIssueContract,
        Transaction.Contract.ContractType.AssetIssueContract);
  }

  //All other solidified APIs start

  @Override
  public AssetIssueContract.Builder assetIssueContractBuilder(String ownerAddress, String name,
      String abbr, long totalSupply, int trxNum, int icoNum, long startTime, long endTime,
      String url, long freeAssetNetLimit,
      long publicFreeAssetNetLimit, int precision, String description) {

    ByteString bsAddress = parseAddress(ownerAddress);

    return AssetIssueContract.newBuilder()
        .setOwnerAddress(bsAddress)
        .setName(ByteString.copyFrom(name.getBytes()))
        .setAbbr(ByteString.copyFrom(abbr.getBytes()))
        .setTotalSupply(totalSupply)
        .setTrxNum(trxNum)
        .setNum(icoNum)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setUrl(ByteString.copyFrom(url.getBytes()))
        .setFreeAssetNetLimit(freeAssetNetLimit)
        .setPublicFreeAssetNetLimit(publicFreeAssetNetLimit)
        .setPrecision(precision)
        .setDescription(ByteString.copyFrom(description.getBytes()));
  }

  /**
   * Update basic TRC10 token information
   *
   * @param ownerAddress Owner address, default hexString
   * @param description The description of token, default hexString
   * @param url The token's website url, default hexString
   * @param newLimit Each token holder's free bandwidth
   * @param newPublicLimit The total free bandwidth of the token
   * @return TransactionExtention
   * @throws IllegalException if fail to update asset
   */
  @Override
  public TransactionExtention updateAsset(String ownerAddress, String description, String url,
      long newLimit, long newPublicLimit) throws IllegalException {
    ByteString bsOwnerAddress = parseAddress(ownerAddress);
    ByteString bsDescription = ByteString.copyFrom(description.getBytes());
    ByteString bsUrl = ByteString.copyFrom(url.getBytes());

    UpdateAssetContract updateAssetContract = createUpdateAssetContract(bsOwnerAddress,
        bsDescription, bsUrl, newLimit, newPublicLimit);

    return createTransactionExtention(updateAssetContract,
        Transaction.Contract.ContractType.UpdateAssetContract);
  }

  /**
   * Unfreeze a token that has passed the minimum freeze duration
   *
   * @param ownerAddress Owner address, default hexString
   * @return TransactionExtention
   * @throws IllegalException if fail to unfreeze asset
   */
  @Override
  public TransactionExtention unfreezeAsset(String ownerAddress) throws IllegalException {
    ByteString bsOwnerAddress = parseAddress(ownerAddress);

    UnfreezeAssetContract unfreezeAssetContract = createUnfreezeAssetContract(bsOwnerAddress);

    return createTransactionExtention(unfreezeAssetContract,
        Transaction.Contract.ContractType.UnfreezeAssetContract);
  }

  /**
   * Unfreeze a token that has passed the minimum freeze duration
   *
   * @param accountPermissionUpdateContract AccountPermissionUpdateContract
   * @return TransactionExtention
   * @throws IllegalException if fail to update account permission
   */
  @Override
  public TransactionExtention accountPermissionUpdate(AccountPermissionUpdateContract
      accountPermissionUpdateContract)
      throws IllegalException {

    return createTransactionExtention(accountPermissionUpdateContract,
        Transaction.Contract.ContractType.AccountPermissionUpdateContract);
  }
  //All other solidified APIs end

  /**
   * Query transaction sign weight
   *
   * @param trx transaction object
   * @return TransactionSignWeight
   */
  @Override
  public TransactionSignWeight getTransactionSignWeight(Transaction trx) {

    return blockingStub.getTransactionSignWeight(trx);
  }

  /**
   * Query transaction approvedList
   *
   * @param trx transaction object
   * @return TransactionApprovedList
   */
  @Override
  public TransactionApprovedList getTransactionApprovedList(Transaction trx) {

    return blockingStub.getTransactionApprovedList(trx);
  }

  /**
   * Get solid account info by address
   *
   * @param address address, default hexString
   * @return Account
   */
  @Override
  public Account getAccountSolidity(String address) {
    ByteString bsAddress = parseAddress(address);
    AccountAddressMessage accountAddressMessage = AccountAddressMessage.newBuilder()
        .setAddress(bsAddress)
        .build();
    return blockingStubSolidity.getAccount(accountAddressMessage);
  }

  /**
   * Get transactionInfo from block number
   *
   * @param blockNum The block height
   * @return TransactionInfoList
   * @throws IllegalException no transactions or the blockNum is incorrect
   */
  @Override
  public TransactionInfoList getTransactionInfoByBlockNumSolidity(long blockNum)
      throws IllegalException {
    if (blockNum < 0) {
      throw new IllegalException("blockNum must be >= 0");
    }
    NumberMessage numberMessage = NumberMessage.newBuilder().setNum(blockNum).build();
    return blockingStubSolidity.getTransactionInfoByBlockNum(numberMessage);
  }

  /**
   * Query the latest solid block information
   *
   * @return BlockExtention
   * @throws IllegalException if fail to get now block
   */
  @Override
  public BlockExtention getNowBlockSolidity() throws IllegalException {
    BlockExtention blockExtention = blockingStubSolidity.getNowBlock2(
        EmptyMessage.newBuilder().build());

    if (!blockExtention.hasBlockHeader()) {
      throw new IllegalException("Fail to get latest block.");
    }
    return blockExtention;
  }

  /**
   * Get transaction receipt info from a transaction id, must be in solid block
   *
   * @param txID Transaction hash, i.e. transaction id
   * @return Transaction
   * @throws IllegalException if the parameters are not correct
   */
  @Override
  public Transaction getTransactionByIdSolidity(String txID) throws IllegalException {
    ByteString bsTxId = ByteString.copyFrom(ByteArray.fromHexString(txID));
    BytesMessage request = BytesMessage.newBuilder()
        .setValue(bsTxId)
        .build();
    Transaction transaction = blockingStubSolidity.getTransactionById(request);

    if (transaction.getRetCount() == 0) {
      throw new IllegalException();
    }
    return transaction;
  }

  /**
   * Get the rewards that the voter has not received
   *
   * @param address address, default hexString
   * @return NumberMessage
   */
  @Override
  public NumberMessage getRewardSolidity(String address) {
    ByteString bsAddress = parseAddress(address);
    BytesMessage bytesMessage = BytesMessage.newBuilder()
        .setValue(bsAddress)
        .build();
    return blockingStubSolidity.getRewardInfo(bytesMessage);
  }

  public TransactionExtention updateBrokerage(String address, int brokerage)
      throws IllegalException {
    ByteString ownerAddress = parseAddress(address);
    UpdateBrokerageContract updateBrokerageContract =
        UpdateBrokerageContract.newBuilder()
            .setOwnerAddress(ownerAddress)
            .setBrokerage(brokerage)
            .build();
    return createTransactionExtention(updateBrokerageContract,
        Transaction.Contract.ContractType.UpdateBrokerageContract);
  }

  @Override
  public long getBrokerageInfo(String address) {
    ByteString sr = parseAddress(address);
    BytesMessage param =
        BytesMessage.newBuilder()
            .setValue(sr)
            .build();
    return blockingStub.getBrokerageInfo(param).getNum();
  }

  /**
   * Obtain a {@code Contract} object via an address
   *
   * @param contractAddress smart contract address
   * @return the smart contract obtained from the address
   */
  @Override
  public Contract getContract(String contractAddress) {
    ByteString rawAddress = parseAddress(contractAddress);
    BytesMessage param =
        BytesMessage.newBuilder()
            .setValue(rawAddress)
            .build();

    SmartContract smartContract = blockingStub.getContract(param);

    return new Contract.Builder()
        .setOriginAddr(smartContract.getOriginAddress())
        .setCntrAddr(smartContract.getContractAddress())
        .setAbi(smartContract.getAbi())
        .setBytecode(smartContract.getBytecode())
        .setCallValue(smartContract.getCallValue())
        .setConsumeUserResourcePercent(smartContract.getConsumeUserResourcePercent())
        .setName(smartContract.getName())
        .setOriginEnergyLimit(smartContract.getOriginEnergyLimit())
        .setCodeHash(smartContract.getCodeHash())
        .setTrxHash(smartContract.getTrxHash())
        .setVersion(smartContract.getVersion())
        .build();
  }

  @Override
  public SmartContract getSmartContract(String contractAddress) {
    ByteString rawAddress = parseAddress(contractAddress);
    BytesMessage param =
        BytesMessage.newBuilder()
            .setValue(rawAddress)
            .build();
    return blockingStub.getContract(param);
  }

  /**
   * Check whether a given method is in the contract.
   *
   * @param contract the smart contract.
   * @param function the smart contract function.
   * @return ture if function exists in the contract.
   */
  private boolean isFuncInContract(Contract contract, Function function) {
    List<ContractFunction> functions = contract.getFunctions();
    for (ContractFunction contractFunction : functions) {
      if (contractFunction.getName().equalsIgnoreCase(function.getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * make a constant call - no broadcasting, no need to broadcast
   *
   * @param ownerAddress the current caller.
   * @param contractAddress smart contract address.
   * @param function contract function.
   * @return TransactionExtention.
   * @deprecated Since 0.9.2, scheduled for removal in future versions.
   * Use {@link #triggerConstantContract(String, String, Function)} instead.
   */
  @Deprecated
  @Override
  public TransactionExtention constantCall(String ownerAddress, String contractAddress,
      Function function) {
    return triggerConstantContract(ownerAddress, contractAddress, function);
  }

  /**
   * make a constant call - no broadcasting, no need to broadcast
   *
   * @param ownerAddress the current caller.
   * @param contractAddress smart contract address.
   * @param callData The data passed along with a transaction that allows us to interact with smart contracts.
   * @return TransactionExtention.
   * @deprecated Since 0.9.2, scheduled for removal in future versions.
   * Use {@link #triggerConstantContract(String, String, String)} instead.
   */
  @Deprecated
  @Override
  public TransactionExtention constantCallV2(String ownerAddress, String contractAddress,
      String callData) {
    return triggerConstantContract(ownerAddress, contractAddress, callData);
  }

  /**
   * @see #triggerConstantContract(String, String, String)
   */
  @Override
  public TransactionExtention triggerConstantContract(String ownerAddress, String contractAddress,
      Function function) {
    String callData = FunctionEncoder.encode(function);
    return triggerConstantContract(ownerAddress, contractAddress, callData);
  }

  /**
   * @see #triggerConstantContract(String, String, String, long, long, String)
   */
  @Override
  public TransactionExtention triggerConstantContract(String ownerAddress, String contractAddress,
      String callData) {
    return triggerConstantContract(ownerAddress, contractAddress, callData, 0L, 0L, null);
  }

  /**
   * make a constant call - no broadcasting, no need to broadcast
   *
   * @param ownerAddress the current caller.
   * @param contractAddress smart contract address.
   * @param callData The data passed along with a transaction that allows us to interact with smart
   * contracts. It can be obtained by using {@link FunctionEncoder#encode}.
   * @param callValue call Value. If TRX not used, use 0.
   * @param tokenValue token Value, If token10 not used, use 0.
   * @param tokenId token10 ID, If token10 not used, use null.
   * @return TransactionExtention.
   */
  @Override
  public TransactionExtention triggerConstantContract(String ownerAddress, String contractAddress,
      String callData, long callValue, long tokenValue, String tokenId) {
    TriggerSmartContract trigger = buildTrigger(ownerAddress, contractAddress, callData, callValue,
        tokenValue, tokenId);
    return blockingStub.triggerConstantContract(trigger);
  }

  /**
   * make a constant call - no broadcasting, no need to broadcast
   *
   * @param ownerAddress the current caller
   * @param contractAddress smart contract address
   * @param function contract function
   * @return transaction builder. Users may set other fields, e.g. feeLimit
   * @deprecated Since 0.9.2, scheduled for removal in future versions.
   * Use {@link #triggerConstantContract(String, String, Function)} instead.
   */
  @Deprecated
  @Override
  public TransactionBuilder triggerCall(String ownerAddress, String contractAddress,
      Function function) {
    TransactionExtention txnExt = triggerConstantContract(ownerAddress, contractAddress, function);
    return new TransactionBuilder(txnExt.getTransaction());
  }

  /**
   * make a constant call - no broadcasting, no need to broadcast
   *
   * @param ownerAddress the current caller
   * @param contractAddress smart contract address
   * @param callData The data passed along with a transaction that allows us to interact with smart contracts.
   * @return transaction builder. TransactionExtention detail.
   * @deprecated Since 0.9.2, scheduled for removal in future versions.
   * Use {@link #triggerConstantContract(String, String, String)} instead.
   */
  @Deprecated
  @Override
  public TransactionBuilder triggerCallV2(String ownerAddress, String contractAddress,
      String callData) {
    TransactionExtention txnExt = triggerConstantContract(ownerAddress, contractAddress, callData);
    return new TransactionBuilder(txnExt.getTransaction());
  }

  /**
   * make a TriggerSmartContract, - no broadcasting. it can be broadcast later.
   *
   * @param ownerAddress the current caller
   * @param contractAddress smart contract address
   * @param callData the encoded function call data
   * @param callValue the amount of sun send to contract. If not used, set 0
   * @param tokenValue the amount of tokenId. If not used, set 0
   * @param tokenId tokenId. If not used, set null
   * @param feeLimit fee unit:SUN
   * @return TransactionExtention
   * @throws Exception if fail
   */
  @Override
  public TransactionExtention triggerContract(String ownerAddress, String contractAddress,
      String callData, long callValue, long tokenValue, String tokenId, long feeLimit)
      throws Exception {
    TriggerSmartContract trigger = buildTrigger(ownerAddress, contractAddress, callData,
        callValue, tokenValue, tokenId);

    return createTransactionExtention(trigger, ContractType.TriggerSmartContract, feeLimit);
  }

  /**
   * GetBlockBalance
   * Get all balance change operations in a block(Note: At present, the interface data can only be queried through the following official nodes
   * 47.241.20.47 & 161.117.85.97 &161.117.224.116 &161.117.83.38)
   *
   * @param blockId tx Id.eg:"000000000309c3c40be03c04615856fc6672b08af6d2cdbbf500a7cf9920fbdb"
   * @param blockNum block number
   * @return BlockBalanceTrace
   */
  @Override
  public BlockBalanceTrace getBlockBalance(String blockId, long blockNum) {
    ByteString bsId = ByteString.copyFrom(ByteArray.fromHexString(blockId));
    BlockIdentifier blockIdentifier =
        BlockIdentifier.newBuilder()
            .setHash(bsId)
            .setNumber(blockNum)
            .build();
    return blockingStub.getBlockBalanceTrace(blockIdentifier);
  }

  /**
   * GetBurnTRX
   * Query the amount of TRX burned due to on-chain transaction fees since No. 54 Committee Proposal took effect
   *
   * @return burn trx amount
   */
  @Override
  public long getBurnTRX() {
    GrpcAPI.NumberMessage numberMessage = blockingStub.getBurnTrx(
        EmptyMessage.getDefaultInstance());
    return numberMessage.getNum();
  }

  /**
   * CreateWitness
   * Apply to become a witness.
   *
   * @param ownerAddress owner address
   * @param url The website URL of the SR node
   * @return TransactionExtention
   * @throws IllegalException if fail to create witness
   */
  @Override
  public TransactionExtention createWitness(String ownerAddress, String url)
      throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    WitnessCreateContract witnessCreateContract =
        WitnessCreateContract.newBuilder()
            .setOwnerAddress(rawOwner)
            .setUrl(ByteString.copyFromUtf8(url))
            .build();
    return createTransactionExtention(witnessCreateContract,
        Transaction.Contract.ContractType.WitnessCreateContract);
  }


  /**
   * UpdateWitness
   * Edit the URL of the witness's official website.
   *
   * @param ownerAddress owner address
   * @param updateUrl Updated URL
   * @return TransactionExtention
   * @throws IllegalException if fail to update witness
   */
  @Override
  public TransactionExtention updateWitness(String ownerAddress, String updateUrl)
      throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    WitnessUpdateContract witnessUpdateContract =
        WitnessUpdateContract.newBuilder()
            .setOwnerAddress(rawOwner)
            .setUpdateUrl(ByteString.copyFromUtf8(updateUrl))
            .build();
    return createTransactionExtention(witnessUpdateContract,
        Transaction.Contract.ContractType.WitnessUpdateContract);
  }

  /**
   * WithdrawBalance
   * Super Representative or user withdraw rewards, usable every 24 hours.
   * Super representatives can withdraw the balance from the account allowance into the account balance,
   * Users can claim the voting reward from the SRs and deposit into his account balance.
   *
   * @param ownerAddress owner address
   * @return TransactionExtention
   * @throws IllegalException if fail to withdraw balance
   */
  @Override
  public TransactionExtention withdrawBalance(String ownerAddress) throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    WithdrawBalanceContract withdrawBalanceContract =
        WithdrawBalanceContract.newBuilder()
            .setOwnerAddress(rawOwner)
            .build();
    return createTransactionExtention(withdrawBalanceContract,
        Transaction.Contract.ContractType.WithdrawBalanceContract);
  }


  /**
   * GetNextMaintenanceTime
   * Returns the timestamp of the next voting time in milliseconds.
   *
   * @return get next maintenance time
   */
  @Override
  public long getNextMaintenanceTime() {
    GrpcAPI.NumberMessage numberMessage = blockingStub.getNextMaintenanceTime(
        EmptyMessage.getDefaultInstance());
    return numberMessage.getNum();
  }


  /**
   * ProposalCreate
   * Creates a proposal transaction.
   *
   * @param ownerAddress owner address
   * @param parameters Parameters proposed to be modified and their values
   * @return TransactionExtention
   * @throws IllegalException if fail to proposal create
   */
  @Override
  public TransactionExtention proposalCreate(String ownerAddress, Map<Long, Long> parameters)
      throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    ProposalCreateContract proposalCreateContract =
        ProposalCreateContract.newBuilder()
            .setOwnerAddress(rawOwner)
            .putAllParameters(parameters)
            .build();
    return createTransactionExtention(proposalCreateContract,
        Transaction.Contract.ContractType.ProposalCreateContract);
  }

  /**
   * ProposalApprove
   * Approves proposed transaction.
   *
   * @param ownerAddress owner address
   * @param proposalId Proposal id
   * @param isAddApproval Whether to agree with the proposal
   * @return TransactionExtention
   * @throws IllegalException if fail to approve proposal
   */
  @Override
  public TransactionExtention approveProposal(String ownerAddress, long proposalId,
      boolean isAddApproval) throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    ProposalApproveContract proposalApproveContract =
        ProposalApproveContract.newBuilder()
            .setOwnerAddress(rawOwner)
            .setIsAddApproval(isAddApproval)
            .setProposalId(proposalId)
            .build();
    return createTransactionExtention(proposalApproveContract,
        Transaction.Contract.ContractType.ProposalApproveContract);
  }

  /**
   * ProposalDelete
   * Deletes Proposal Transaction.
   *
   * @param ownerAddress owner address
   * @param proposalId Proposal id
   * @return TransactionExtention
   * @throws IllegalException if fail to delete proposal
   */
  @Override
  public TransactionExtention deleteProposal(String ownerAddress, long proposalId)
      throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    ProposalDeleteContract proposalDeleteContract =
        ProposalDeleteContract.newBuilder()
            .setOwnerAddress(rawOwner)
            .setProposalId(proposalId)
            .build();
    return createTransactionExtention(proposalDeleteContract,
        Transaction.Contract.ContractType.ProposalDeleteContract);
  }

  /**
   * GetTransactionListFromPending
   * Get transaction list information from pending pool
   *
   * @return transaction list information from pending pool
   */
  @Override
  public GrpcAPI.TransactionIdList getTransactionListFromPending() {
    return blockingStub.getTransactionListFromPending(
        EmptyMessage.getDefaultInstance());
  }

  /**
   * GetPendingSize
   * Get the size of the pending pool queue
   *
   * @return the size of the pending pool queue
   */
  @Override
  public long getPendingSize() {
    GrpcAPI.NumberMessage pendingSize = blockingStub.getPendingSize(
        EmptyMessage.getDefaultInstance());
    return pendingSize.getNum();
  }


  /**
   * GetTransactionFromPending
   * Get transaction details from the pending pool
   *
   * @param txId Transaction ID
   * @return Transaction
   * @throws IllegalException if fail to get transaction from pending
   */
  @Override
  public Transaction getTransactionFromPending(String txId) throws IllegalException {
    ByteString bsTxId = ByteString.copyFrom(ByteArray.fromHexString(txId));
    BytesMessage request = BytesMessage.newBuilder()
        .setValue(bsTxId)
        .build();

    return blockingStub.getTransactionFromPending(request);
  }


  /**
   * GetBlockById
   * Query block by ID(block hash).
   *
   * @param blockID block hash.eg:"00000000000f424013e51b18e0782a32fa079ddafdb2f4c343468cf8896dc887"
   * @return the size of the pending pool queue
   */
  @Override
  public Block getBlockById(String blockID) {
    ByteString bsBlockId = ByteString.copyFrom(ByteArray.fromHexString(blockID));
    BytesMessage request = BytesMessage.newBuilder()
        .setValue(bsBlockId)
        .build();
    return blockingStub.getBlockById(request);
  }


  /**
   * Estimate the energy required for the successful execution of smart contract transactions
   * This API is closed by default in tron node.
   * To open this interface, the two configuration items vm.estimateEnergy and vm.supportConstant
   * must be enabled in the node configuration file at the same time.
   *
   * @param ownerAddress Owner address that triggers the contract. If visible=true, use base58check
   * format, otherwise use hex format. For constant call you can use the all-zero address.
   * @param contractAddress Smart contract address.
   * @param function contract function
   * @return EstimateEnergyMessage. Estimated energy to run the contract
   */
  @Override
  public Response.EstimateEnergyMessage estimateEnergy(String ownerAddress, String contractAddress,
      Function function) {
    String encodedHex = FunctionEncoder.encode(function);
    TriggerSmartContract trigger = buildTrigger(ownerAddress, contractAddress, encodedHex, 0L, 0L,
        null);
    return blockingStub.estimateEnergy(trigger);
  }

  /**
   * Estimate the energy required for the successful execution of smart contract transactions
   * This API is closed by default in tron node. To open this interface, the two configuration
   * items vm.estimateEnergy and vm.supportConstant must be enabled in the node configuration file
   * at the same time.
   *
   * @param ownerAddress Owner address that triggers the contract. If visible=true, use base58check
   * format, otherwise use hex format. For constant call you can use the all-zero address.
   * @param contractAddress Smart contract address.
   * @param callData The data passed along with a transaction that allows us to interact with smart contracts.
   * @param callValue call Value. If TRX not used, use 0.
   * @param tokenValue token Value, If token10 not used, use 0.
   * @param tokenId token10 ID, If token10 not used, use null.
   * @return EstimateEnergyMessage. Estimated energy to run the contract
   */
  @Override
  public Response.EstimateEnergyMessage estimateEnergy(String ownerAddress,
      String contractAddress, String callData, long callValue, long tokenValue, String tokenId) {
    TriggerSmartContract trigger = buildTrigger(ownerAddress, contractAddress, callData, callValue,
        tokenValue, tokenId);
    return blockingStub.estimateEnergy(trigger);
  }

  /**
   * Estimate the energy required for the successful execution of smart contract transactions
   * This API is closed by default in tron node. To open this interface, the two configuration
   * items vm.estimateEnergy and vm.supportConstant must be enabled in the node configuration file
   * at the same time.
   *
   * @param ownerAddress Owner address that triggers the contract. If visible=true, use base58check
   * format, otherwise use hex format.
   * For constant call you can use the all-zero address.
   * @param contractAddress Smart contract address.
   * @param callData The data passed along with a transaction that allows us to interact with smart contracts.
   * @return EstimateEnergyMessage. Estimated energy to run the contract
   * @deprecated Since 0.9.2, scheduled for removal in future versions.
   * Use {@link #estimateEnergy(String, String, String, long, long, String)} instead.
   */
  @Override
  public Response.EstimateEnergyMessage estimateEnergyV2(String ownerAddress,
      String contractAddress, String callData) {
    TriggerSmartContract trigger =
        buildTrigger(ownerAddress, contractAddress, callData, 0L, 0L, null);
    return blockingStub.estimateEnergy(trigger);
  }

  /**
   * construct TriggerSmartContract
   */
  private TriggerSmartContract buildTrigger(String ownerAddress, String contractAddress,
      String callData, long callValue, long tokenValue, String tokenId) {
    validateCallValue(callValue);
    validateTokenId(tokenId);
    validateTokenValue(tokenValue);
    TriggerSmartContract.Builder builder =
        TriggerSmartContract.newBuilder()
            .setOwnerAddress(parseAddress(ownerAddress))
            .setContractAddress(parseAddress(contractAddress))
            .setData(ByteString.copyFrom(ByteArray.fromHexString(callData)))
            .setCallValue(callValue);
    if (tokenId != null && !tokenId.isEmpty()) {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    return builder.build();
  }

  /**
   * GetBandwidthPrices
   * Query historical bandwidth unit price.
   *
   * @return prices string: All historical bandwidth unit price information.
   * Each unit price change is separated by a comma.
   * Before the colon is the millisecond timestamp,
   * and after the colon is the bandwidth unit price in sun.
   */
  @Override
  public Response.PricesResponseMessage getBandwidthPrices() {
    return blockingStub.getBandwidthPrices(EmptyMessage.getDefaultInstance());
  }


  /**
   * GetEnergyPrices
   * Query historical energy unit price.
   *
   * @return prices string: All historical bandwidth unit price information.
   * Each unit price change is separated by a comma.
   * Before the colon is the millisecond timestamp,
   * and after the colon is the bandwidth unit price in sun.
   */
  @Override
  public Response.PricesResponseMessage getEnergyPrices() {
    return blockingStub.getEnergyPrices(EmptyMessage.getDefaultInstance());
  }


  /**
   * GetMemoFee
   * Query historical memo fee.
   *
   * @return prices string: All historical bandwidth unit price information.
   * Each unit price change is separated by a comma.
   * Before the colon is the millisecond timestamp,
   * and after the colon is the bandwidth unit price in sun.
   */
  @Override
  public Response.PricesResponseMessage getMemoFee() {
    return blockingStub.getMemoFee(EmptyMessage.getDefaultInstance());
  }


  /**
   * GetBandwidthPricesOnSolidity
   * Query historical bandwidth unit price.
   *
   * @return prices string: All historical bandwidth unit price information.
   * Each unit price change is separated by a comma.
   * Before the colon is the millisecond timestamp,
   * and after the colon is the bandwidth unit price in sun.
   */
  @Override
  public Response.PricesResponseMessage getBandwidthPricesOnSolidity() {
    return blockingStubSolidity.getBandwidthPrices(EmptyMessage.getDefaultInstance());
  }


  /**
   * GetEnergyPricesOnSolidity
   * Query historical energy unit price.
   *
   * @return prices string: All historical bandwidth unit price information.
   * Each unit price change is separated by a comma.
   * Before the colon is the millisecond timestamp,
   * and after the colon is the bandwidth unit price in sun.
   */
  @Override
  public Response.PricesResponseMessage getEnergyPricesOnSolidity() {
    return blockingStubSolidity.getEnergyPrices(EmptyMessage.getDefaultInstance());
  }

  /**
   * ClearABIContract
   *
   * @param ownerAddress owner address
   * @param contractAddress contract address
   * @return TransactionExtention
   */
  @Override
  public TransactionExtention clearContractABI(String ownerAddress, String contractAddress)
      throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    ByteString rawContract = parseAddress(contractAddress);
    ClearABIContract clearABIContract = ClearABIContract.newBuilder()
        .setOwnerAddress(rawOwner)
        .setContractAddress(rawContract)
        .build();
    return createTransactionExtention(clearABIContract,
        ContractType.ClearABIContract);
  }

  /**
   * get paginated exchange list
   *
   * @param offset offset
   * @param limit limit
   * @return exchange list
   */
  @Override
  public ExchangeList getPaginatedExchangeList(long offset, long limit) {
    PaginatedMessage paginatedMessage = PaginatedMessage.newBuilder()
        .setOffset(offset)
        .setLimit(limit)
        .build();
    return blockingStub.getPaginatedExchangeList(paginatedMessage);
  }

  /**
   * get paginated proposal list
   *
   * @param offset offset
   * @param limit limit
   * @return proposal list
   */
  @Override
  public ProposalList getPaginatedProposalList(long offset, long limit) {
    PaginatedMessage paginatedMessage = PaginatedMessage.newBuilder()
        .setOffset(offset)
        .setLimit(limit)
        .build();
    return blockingStub.getPaginatedProposalList(paginatedMessage);
  }

  /**
   * get block of one specified block
   *
   * @param blockIDOrNum block Id or block num
   * @param detail if false, no transactions are contained.
   * @return BlockExtention
   */
  @Override
  public BlockExtention getBlock(String blockIDOrNum, boolean detail) {
    BlockReq blockReq = BlockReq.newBuilder()
        .setIdOrNum(blockIDOrNum)
        .setDetail(detail)
        .build();
    return blockingStub.getBlock(blockReq);
  }

  /**
   * get latest block extension
   *
   * @param detail specify whether to contains transaction in BlockExtention
   */
  @Override
  public BlockExtention getBlock(boolean detail) {
    BlockReq blockReq = BlockReq.newBuilder()
        .setDetail(detail)
        .build();
    return blockingStub.getBlock(blockReq);
  }

  /**
   * GetBlockByIdOrNum
   *
   * @param blockIDOrNum block Id with hex or block num with long
   * @return Block
   */
  @Override
  public Block getBlockByIdOrNum(String blockIDOrNum) {
    if (Numeric.isNumericString(blockIDOrNum)) {
      NumberMessage numberMessage = NumberMessage.newBuilder()
          .setNum(Long.parseLong(blockIDOrNum))
          .build();
      return blockingStub.getBlockByNum(numberMessage);
    } else if (ByteArray.isHexString(blockIDOrNum)) {
      BytesMessage bytesMessage = BytesMessage.newBuilder()
          .setValue(ByteString.copyFrom(ByteArray.fromHexString(blockIDOrNum)))
          .build();
      return blockingStub.getBlockById(bytesMessage);
    } else {
      throw new IllegalArgumentException("Invalid blockIDOrNum: " + blockIDOrNum);
    }
  }

  /**
   * getContractInfo
   *
   * @param contractAddress contract address
   * @return SmartContractDataWrapper
   */
  @Override
  public SmartContractDataWrapper getContractInfo(String contractAddress) {
    ByteString rawAddress = parseAddress(contractAddress);
    BytesMessage param =
        BytesMessage.newBuilder()
            .setValue(rawAddress)
            .build();
    return blockingStub.getContractInfo(param);
  }

  /**
   * getMarketOrderByAccount
   *
   * @param address account address
   * @return MarketOrderList
   */
  @Override
  public MarketOrderList getMarketOrderByAccount(String address) {
    ByteString rawAddress = parseAddress(address);
    BytesMessage param =
        BytesMessage.newBuilder()
            .setValue(rawAddress)
            .build();
    return blockingStub.getMarketOrderByAccount(param);
  }

  /**
   * getMarketOrderById
   *
   * @param txn market transactionId
   * @return MarketOrder
   */
  @Override
  public MarketOrder getMarketOrderById(String txn) {
    ByteString rawAddress = ByteString.copyFrom(ByteArray.fromHexString(txn));
    BytesMessage param =
        BytesMessage.newBuilder()
            .setValue(rawAddress)
            .build();
    return blockingStub.getMarketOrderById(param);
  }

  /**
   * getMarketOrderListByPair
   *
   * @param sellTokenId market sell token id
   * @param buyTokenId market buy token id
   * @return MarketOrderList
   */
  @Override
  public MarketOrderList getMarketOrderListByPair(String sellTokenId, String buyTokenId) {
    MarketOrderPair param =
        MarketOrderPair.newBuilder()
            .setSellTokenId(ByteString.copyFrom(sellTokenId.getBytes()))
            .setBuyTokenId(ByteString.copyFrom(buyTokenId.getBytes()))
            .build();
    return blockingStub.getMarketOrderListByPair(param);
  }

  /**
   * getMarketPairList
   *
   * @return MarketOrderPairList
   */
  @Override
  public MarketOrderPairList getMarketPairList() {
    return blockingStub.getMarketPairList(EmptyMessage.getDefaultInstance());
  }

  /**
   * getMarketPriceByPair
   *
   * @param sellTokenId market sell token id
   * @param buyTokenId market buy token id
   * @return MarketPriceList
   */
  @Override
  public MarketPriceList getMarketPriceByPair(String sellTokenId, String buyTokenId) {
    MarketOrderPair param =
        MarketOrderPair.newBuilder()
            .setSellTokenId(ByteString.copyFrom(sellTokenId.getBytes()))
            .setBuyTokenId(ByteString.copyFrom(buyTokenId.getBytes()))
            .build();
    return blockingStub.getMarketPriceByPair(param);
  }

  /**
   * exchangeCreate
   *
   * @param ownerAddress address
   * @param firstToken first token id. TRX is "_", else token10 ID
   * @param firstBalance first token id balance
   * @param secondToken second token id. TRX is "_", else token10 ID
   * @param secondBalance second token id balance
   * @return TransactionExtention
   */
  @Override
  public TransactionExtention exchangeCreate(String ownerAddress, String firstToken,
      long firstBalance, String secondToken, long secondBalance)
      throws IllegalException {

    ExchangeCreateContract exchangeCreateContract = ExchangeCreateContract.newBuilder()
        .setOwnerAddress(parseAddress(ownerAddress))
        .setFirstTokenId(ByteString.copyFrom(firstToken.getBytes()))
        .setFirstTokenBalance(firstBalance)
        .setSecondTokenId(ByteString.copyFrom(secondToken.getBytes()))
        .setSecondTokenBalance(secondBalance).build();
    return createTransactionExtention(exchangeCreateContract,
        ContractType.ExchangeCreateContract);
  }

  /**
   * exchangeInject
   *
   * @param ownerAddress owner
   * @param exchangeId exchange id
   * @param tokenId token id
   * @param amount inject the amount of tokenId to exchangeId
   * @return TransactionExtention
   */
  @Override
  public TransactionExtention exchangeInject(String ownerAddress, long exchangeId, String tokenId,
      long amount) throws IllegalException {

    ExchangeInjectContract exchangeInjectContract = ExchangeInjectContract.newBuilder()
        .setOwnerAddress(parseAddress(ownerAddress))
        .setExchangeId(exchangeId)
        .setTokenId(ByteString.copyFrom(tokenId.getBytes()))
        .setQuant(amount).build();
    return createTransactionExtention(exchangeInjectContract,
        ContractType.ExchangeInjectContract);
  }

  /**
   * create exchangeTransaction. alias is bancor transaction.
   *
   * @param ownerAddress owner
   * @param exchangeId exchange id
   * @param tokenId sell token id
   * @param amount inject the amount of tokenId to exchangeId
   * @param expected amount of buyTokenId
   * @return TransactionExtention
   */
  @Override
  public TransactionExtention exchangeTransaction(String ownerAddress, long exchangeId,
      String tokenId, long amount, long expected)
      throws IllegalException {

    ExchangeTransactionContract exchangeTransactionContract =
        ExchangeTransactionContract.newBuilder()
            .setOwnerAddress(parseAddress(ownerAddress))
            .setExchangeId(exchangeId)
            .setTokenId(ByteString.copyFrom(tokenId.getBytes()))
            .setQuant(amount)
            .setExpected(expected)
            .build();
    return createTransactionExtention(exchangeTransactionContract,
        ContractType.ExchangeTransactionContract);
  }

  /**
   * create ExchangeWithdrawContract with parameters
   *
   * @param ownerAddress owner address
   * @param exchangeId exchangeId
   * @param tokenId tokenId
   * @param quant quant
   * @return ExchangeWithdrawContract
   */
  @Override
  public TransactionExtention exchangeWithdraw(String ownerAddress, long exchangeId,
      String tokenId, long quant) throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    ExchangeWithdrawContract exchangeWithdrawContract = ExchangeWithdrawContract.newBuilder()
        .setOwnerAddress(rawOwner)
        .setExchangeId(exchangeId)
        .setTokenId(ByteString.copyFrom(tokenId.getBytes()))
        .setQuant(quant)
        .build();
    return createTransactionExtention(exchangeWithdrawContract,
        ContractType.ExchangeWithdrawContract);
  }

  /**
   * getTransactionCountByBlockNum
   *
   * @param blockNum block num
   * @return the transaction count in block
   */
  @Override
  public long getTransactionCountByBlockNum(long blockNum) {
    NumberMessage message = NumberMessage.newBuilder().setNum(blockNum).build();
    return blockingStub.getTransactionCountByBlockNum(message).getNum();
  }

  /**
   * crete MarketCancelOrderContract with parameters
   *
   * @param ownerAddress owner address
   * @param orderId existing order Id
   * @return MarketCancelOrderContract
   */
  @Override
  public TransactionExtention marketCancelOrder(String ownerAddress, String orderId)
      throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    ByteString rawOrderId = ByteString.copyFrom(ByteArray.fromHexString(orderId));
    MarketCancelOrderContract marketCancelOrderContract = MarketCancelOrderContract.newBuilder()
        .setOwnerAddress(rawOwner)
        .setOrderId(rawOrderId)
        .build();
    return createTransactionExtention(marketCancelOrderContract,
        ContractType.MarketCancelOrderContract);
  }

  /**
   * create MarketSellAssetContract with parameters
   *
   * @param ownerAddress owner address
   * @param sellTokenId sell token Id, "_" or all digit with 0~9
   * @param sellTokenQuantity sell token quantity
   * @param buyTokenId buy token Id, "_" or all digit with 0~9
   * @param buyTokenQuantity buy token quantity
   * @return MarketSellAssetContract
   */
  @Override
  public TransactionExtention marketSellAsset(String ownerAddress, String sellTokenId,
      long sellTokenQuantity, String buyTokenId, long buyTokenQuantity) throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    validateTokenId(sellTokenId);
    validateTokenId(buyTokenId);

    MarketSellAssetContract marketSellAssetContract = MarketSellAssetContract.newBuilder()
        .setOwnerAddress(rawOwner)
        .setSellTokenId(ByteString.copyFrom(sellTokenId.getBytes()))
        .setSellTokenQuantity(sellTokenQuantity)
        .setBuyTokenId(ByteString.copyFrom(buyTokenId.getBytes()))
        .setBuyTokenQuantity(buyTokenQuantity)
        .build();
    return createTransactionExtention(marketSellAssetContract,
        ContractType.MarketSellAssetContract);
  }

  /**
   * create UpdateEnergyLimitContract with parameters
   *
   * @param ownerAddress owner address
   * @param contractAddress contract address
   * @param originEnergyLimit origin energy limit, must be > 0
   * @return UpdateEnergyLimitContract
   * @throws IllegalException if originEnergyLimit is invalid
   */
  @Override
  public TransactionExtention updateEnergyLimit(String ownerAddress, String contractAddress,
      long originEnergyLimit) throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    ByteString rawContract = parseAddress(contractAddress);
    if (originEnergyLimit <= 0) {
      throw new IllegalException("origin energy limit must be > 0");
    }
    UpdateEnergyLimitContract updateEnergyLimitContract = UpdateEnergyLimitContract.newBuilder()
        .setOwnerAddress(rawOwner)
        .setContractAddress(rawContract)
        .setOriginEnergyLimit(originEnergyLimit)
        .build();
    return createTransactionExtention(updateEnergyLimitContract,
        ContractType.UpdateEnergyLimitContract);
  }

  /**
   * create UpdateSettingContract with parameters
   *
   * @param ownerAddress owner address
   * @param contractAddress contract address
   * @param consumeUserResourcePercent consume user resource percent if user trigger this contract, must be [0,100]
   * @return UpdateSettingContract
   * @throws IllegalException if consumeUserResourcePercent is invalid
   */
  @Override
  public TransactionExtention updateSetting(String ownerAddress, String contractAddress,
      long consumeUserResourcePercent) throws IllegalException {
    ByteString rawOwner = parseAddress(ownerAddress);
    ByteString rawContract = parseAddress(contractAddress);
    if (consumeUserResourcePercent < 0 || consumeUserResourcePercent > 100) {
      throw new IllegalException("percent not in [0, 100]");
    }
    UpdateSettingContract updateSettingContract = UpdateSettingContract.newBuilder()
        .setOwnerAddress(rawOwner)
        .setContractAddress(rawContract)
        .setConsumeUserResourcePercent(consumeUserResourcePercent)
        .build();
    return createTransactionExtention(updateSettingContract,
        ContractType.UpdateSettingContract);
  }

  /**
   * @param contractName contractName
   * @param address ownerAddress
   * @param ABI abiString
   * @param code bytecode
   * @param callValue the amount of deposit TRX(unit sun), default is 0
   * @param consumeUserResourcePercent consumeUserResourcePercent,range 0-100
   * @param originEnergyLimit originEnergyLimit
   * @param tokenValue the amount of deposit token 10, default is 0
   * @param tokenId the ID of token 10
   * @return CreateSmartContract
   * @throws Exception exception
   */
  @Override
  public CreateSmartContract createSmartContract(String contractName, String address, String ABI,
      String code, long callValue, long consumeUserResourcePercent, long originEnergyLimit,
      long tokenValue, String tokenId) throws Exception {
    validateCallValue(callValue);
    validateTokenId(tokenId);
    validateTokenValue(tokenValue);
    //abi
    SmartContract.ABI.Builder abiBuilder = SmartContract.ABI.newBuilder();
    Contract.loadAbiFromJson(ABI, abiBuilder);
    SmartContract.ABI abi = abiBuilder.build();

    SmartContract.Builder builder = SmartContract.newBuilder()
        .setName(contractName)
        .setOriginAddress(parseAddress(address))
        .setAbi(abi)
        .setConsumeUserResourcePercent(consumeUserResourcePercent)
        .setOriginEnergyLimit(originEnergyLimit)
        .setCallValue(callValue)
        .setBytecode(ByteString.copyFrom(ByteArray.fromHexString(code)));
    CreateSmartContract.Builder createSmartContractBuilder = CreateSmartContract.newBuilder()
        .setOwnerAddress(parseAddress(address))
        .setNewContract(builder.build());
    if (tokenId != null && !tokenId.equalsIgnoreCase("")) {
      createSmartContractBuilder.setCallTokenValue(tokenValue)
          .setTokenId(Long.parseLong(tokenId));
    }
    return createSmartContractBuilder.build();
  }

  /**
   * @param contractName contractName
   * @param address ownerAddress
   * @param ABI abiString
   * @param code bytecode
   * @param callValue the amount of deposit TRX(unit sun), default is 0
   * @param consumeUserResourcePercent consumeUserResourcePercent,range 0-100
   * @param originEnergyLimit originEnergyLimit
   * @param tokenValue the amount of deposit token 10, default is 0
   * @param tokenId the ID of token 10
   * @param libraryAddressPair walletCli compatible
   * @param compilerVersion walletCli compatible
   * @return CreateSmartContract
   * @throws Exception exception
   */
  @Override
  public CreateSmartContract createSmartContract(String contractName, String address, String ABI,
      String code, long callValue, long consumeUserResourcePercent, long originEnergyLimit,
      long tokenValue, String tokenId, String libraryAddressPair, String compilerVersion)
      throws Exception {

    if (null != libraryAddressPair) {
      byte[] byteCode = Utils.replaceLibraryAddress(code, libraryAddressPair, compilerVersion);
      code = ByteArray.toHexString(byteCode);
    }
    return createSmartContract(contractName, address, ABI, code, callValue,
        consumeUserResourcePercent, originEnergyLimit, tokenValue, tokenId);
  }

  /**
   * Deploy a smart contract - no broadcasting
   *
   * @param contractName contract name
   * @param abiStr abi
   * @param bytecode bytecode
   * @param constructorParams constructorParams, no Params set null or empty list
   * @param feeLimit feeLimit
   * @param consumeUserResourcePercent consumeUserResourcePercent,range 0-100
   * @param originEnergyLimit originEnergyLimit
   * @param callValue TRX value
   * @param tokenValue token value of token10
   * @param tokenId token10 ID, no use set null or ""
   * @return TransactionExtention
   * @throws Exception exception if fail
   */
  @Override
  public TransactionExtention deployContract(String contractName, String abiStr, String bytecode,
      List<Type<?>> constructorParams,
      long feeLimit, long consumeUserResourcePercent, long originEnergyLimit, long callValue,
      String tokenId, long tokenValue)
      throws Exception {
    validateCallValue(callValue);
    validateTokenId(tokenId);
    validateTokenValue(tokenValue);
    if (constructorParams != null && !constructorParams.isEmpty()) {
      ByteString constructorParamsByteString = encodeParameter(constructorParams);
      ByteString newByteCode = ByteString.copyFrom(ByteArray.fromHexString(bytecode))
          .concat(constructorParamsByteString);
      bytecode = ByteArray.toHexString(newByteCode.toByteArray());
    }
    CreateSmartContract createSmartContract = createSmartContract(
        contractName, keyPair.toBase58CheckAddress(), abiStr, bytecode, callValue,
        consumeUserResourcePercent, originEnergyLimit, tokenValue, tokenId);

    return createTransactionExtention(createSmartContract,
        ContractType.CreateSmartContract, feeLimit);
  }
}