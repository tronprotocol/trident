package org.tron.trident.core;

import com.google.protobuf.Message;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.api.GrpcAPI;
import org.tron.trident.api.GrpcAPI.NumberMessage;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.transaction.TransactionBuilder;
import org.tron.trident.proto.Chain.Block;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Common.SmartContract;
import org.tron.trident.proto.Contract.AccountPermissionUpdateContract;
import org.tron.trident.proto.Contract.AssetIssueContract;
import org.tron.trident.proto.Contract.CreateSmartContract;
import org.tron.trident.proto.Response;
import org.tron.trident.proto.Response.Account;
import org.tron.trident.proto.Response.AccountNetMessage;
import org.tron.trident.proto.Response.AccountResourceMessage;
import org.tron.trident.proto.Response.AssetIssueList;
import org.tron.trident.proto.Response.BlockBalanceTrace;
import org.tron.trident.proto.Response.BlockExtention;
import org.tron.trident.proto.Response.BlockListExtention;
import org.tron.trident.proto.Response.ChainParameters;
import org.tron.trident.proto.Response.DelegatedResourceAccountIndex;
import org.tron.trident.proto.Response.DelegatedResourceList;
import org.tron.trident.proto.Response.Exchange;
import org.tron.trident.proto.Response.ExchangeList;
import org.tron.trident.proto.Response.MarketOrder;
import org.tron.trident.proto.Response.MarketOrderList;
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
import org.tron.trident.proto.Response.TransactionSignWeight;
import org.tron.trident.proto.Response.WitnessList;

public interface Api {

  Transaction signTransaction(TransactionExtention txnExt, KeyPair keyPair);

  Transaction signTransaction(Transaction txn, KeyPair keyPair);

  Transaction signTransaction(TransactionExtention txnExt);

  Transaction signTransaction(Transaction txn);

  TransactionExtention createTransactionExtention(Message request,
      Transaction.Contract.ContractType contractType) throws IllegalException;

  long estimateBandwidth(Transaction txn);

  String broadcastTransaction(Transaction txn) throws RuntimeException;

  TransactionExtention transfer(String fromAddress, String toAddress, long amount)
      throws IllegalException;

  TransactionExtention transferTrc10(String fromAddress, String toAddress, int tokenId, long amount)
      throws IllegalException;

  TransactionExtention freezeBalance(String ownerAddress, long frozenBalance, int frozenDuration,
      int resourceCode) throws IllegalException;

  TransactionExtention freezeBalance(String ownerAddress, long frozenBalance, int frozenDuration,
      int resourceCode, String receiveAddress) throws IllegalException;

  TransactionExtention freezeBalanceV2(String ownerAddress, long frozenBalance, int resourceCode)
      throws IllegalException;

  TransactionExtention unfreezeBalance(String ownerAddress, int resourceCode)
      throws IllegalException;

  TransactionExtention unfreezeBalance(String ownerAddress, int resourceCode, String receiveAddress)
      throws IllegalException;

  TransactionExtention unfreezeBalanceV2(String ownerAddress, long unfreezeBalance,
      int resourceCode) throws IllegalException;

  TransactionExtention cancelAllUnfreezeV2(String ownerAddress) throws IllegalException;

  TransactionExtention delegateResource(String ownerAddress, long balance, int resourceCode,
      String receiverAddress, boolean lock) throws IllegalException;

  TransactionExtention delegateResourceV2(String ownerAddress, long balance, int resourceCode,
      String receiverAddress, boolean lock, long lockPeriod) throws IllegalException;

  TransactionExtention undelegateResource(String ownerAddress, long balance, int resourceCode,
      String receiverAddress) throws IllegalException;

  TransactionExtention withdrawExpireUnfreeze(String ownerAddress) throws IllegalException;

  long getAvailableUnfreezeCount(String ownerAddress);

  long getCanWithdrawUnfreezeAmount(String ownerAddress);

  long getCanWithdrawUnfreezeAmount(String ownerAddress, long timestamp);

  long getCanDelegatedMaxSize(String ownerAddress, int type);

  DelegatedResourceList getDelegatedResourceV2(String fromAddress, String toAddress);

  DelegatedResourceAccountIndex getDelegatedResourceAccountIndexV2(String address)
      throws IllegalException;

  TransactionExtention voteWitness(String ownerAddress, HashMap<String, String> votes)
      throws IllegalException;

  TransactionExtention createAccount(String ownerAddress, String accountAddress)
      throws IllegalException;

  //only if account.getAccountName() == null can update name
  TransactionExtention updateAccount(String address, String accountName) throws IllegalException;

  Block getNowBlock() throws IllegalException;

  BlockExtention getBlockByNum(long blockNum) throws IllegalException;

  BlockListExtention getBlockByLatestNum(long num) throws IllegalException;

  BlockListExtention getBlockByLimitNext(long startNum, long endNum) throws IllegalException;

  NodeInfo getNodeInfo() throws IllegalException;

  NodeList listNodes() throws IllegalException;

  TransactionInfoList getTransactionInfoByBlockNum(long blockNum) throws IllegalException;

  TransactionInfo getTransactionInfoById(String txID) throws IllegalException;

  Transaction getTransactionById(String txID) throws IllegalException;

  Account getAccount(String address);

  AccountResourceMessage getAccountResource(String address);

  AccountNetMessage getAccountNet(String address);

  long getAccountBalance(String address);

  Account getAccountById(String id);

  Transaction setAccountId(String id, String address) throws IllegalException;

  //use this method instead of setAccountId
  TransactionExtention setAccountId2(String id, String address) throws IllegalException;

  ChainParameters getChainParameters() throws IllegalException;

  DelegatedResourceList getDelegatedResource(String fromAddress, String toAddress);

  DelegatedResourceAccountIndex getDelegatedResourceAccountIndex(String address);

  AssetIssueList getAssetIssueList();

  AssetIssueList getPaginatedAssetIssueList(long offset, long limit);

  AssetIssueList getAssetIssueByAccount(String address);

  AssetIssueContract getAssetIssueById(String assetId);

  AssetIssueContract getAssetIssueByName(String name);

  AssetIssueList getAssetIssueListByName(String name);

  TransactionExtention participateAssetIssue(String toAddress, String ownerAddress,
      String assertName, long amount) throws IllegalException;

  ProposalList listProposals();

  //1-17
  Proposal getProposalById(String id);

  WitnessList listWitnesses();

  ExchangeList listExchanges();

  Exchange getExchangeById(String id) throws IllegalException;

  TransactionExtention createAssetIssue(String ownerAddress, String name, String abbr,
      long totalSupply, int trxNum, int icoNum, long startTime, long endTime, String url,
      long freeAssetNetLimit, long publicFreeAssetNetLimit, int precision,
      HashMap<String, String> frozenSupply, String description) throws IllegalException;

  TransactionExtention createAssetIssue(String ownerAddress, String name, String abbr,
      long totalSupply, int trxNum, int icoNum, long startTime, long endTime, String url,
      long freeAssetNetLimit, long publicFreeAssetNetLimit, int precision, String description)
      throws IllegalException;

  AssetIssueContract.Builder assetIssueContractBuilder(String ownerAddress, String name,
      String abbr, long totalSupply, int trxNum, int icoNum, long startTime, long endTime,
      String url, long freeAssetNetLimit, long publicFreeAssetNetLimit, int precision,
      String description);

  TransactionExtention updateAsset(String ownerAddress, String description, String url,
      long newLimit, long newPublicLimit) throws IllegalException;

  TransactionExtention unfreezeAsset(String ownerAddress) throws IllegalException;

  TransactionExtention accountPermissionUpdate(AccountPermissionUpdateContract contract)
      throws IllegalException;

  TransactionSignWeight getTransactionSignWeight(Transaction trx);

  TransactionApprovedList getTransactionApprovedList(Transaction trx);

  Account getAccountSolidity(String address);

  TransactionInfoList getTransactionInfoByBlockNumSolidity(long blockNum) throws IllegalException;

  BlockExtention getNowBlockSolidity() throws IllegalException;

  Transaction getTransactionByIdSolidity(String txID) throws IllegalException;

  NumberMessage getRewardSolidity(String address);

  TransactionExtention updateBrokerage(String address, int brokerage) throws IllegalException;

  long getBrokerageInfo(String address);

  Contract getContract(String contractAddress);

  SmartContract getSmartContract(String contractAddress);

  @Deprecated
  TransactionExtention constantCall(String ownerAddress, String contractAddress, Function function);

  @Deprecated
  TransactionBuilder triggerCall(String ownerAddress, String contractAddress, Function function);

  TransactionExtention triggerContract(String ownerAddress, String contractAddress,
      String callData, long callValue, long tokenValue, String tokenId, long feeLimit)
      throws Exception;

  BlockBalanceTrace getBlockBalance(String blockId, long blockNum);

  long getBurnTRX();

  TransactionExtention createWitness(String ownerAddress, String url) throws IllegalException;

  TransactionExtention updateWitness(String ownerAddress, String updateUrl) throws IllegalException;

  TransactionExtention withdrawBalance(String ownerAddress) throws IllegalException;

  long getNextMaintenanceTime();

  TransactionExtention proposalCreate(String ownerAddress, Map<Long, Long> parameters)
      throws IllegalException;

  TransactionExtention approveProposal(String ownerAddress, long proposalId, boolean isAddApproval)
      throws IllegalException;

  TransactionExtention deleteProposal(String ownerAddress, long proposalId) throws IllegalException;

  GrpcAPI.TransactionIdList getTransactionListFromPending();

  long getPendingSize();

  Transaction getTransactionFromPending(String txId) throws IllegalException;

  Block getBlockById(String blockID);

  Response.EstimateEnergyMessage estimateEnergy(String ownerAddress, String contractAddress,
      Function function);

  Response.EstimateEnergyMessage estimateEnergy(String ownerAddress,
      String contractAddress, String callData, long callValue, long tokenValue, String tokenId);

  @Deprecated
  Response.EstimateEnergyMessage estimateEnergyV2(String ownerAddress, String contractAddress,
      String callData);

  @Deprecated
  TransactionBuilder triggerCallV2(String ownerAddress, String contractAddress, String callData);

  @Deprecated
  TransactionExtention constantCallV2(String ownerAddress, String contractAddress, String callData);

  TransactionExtention triggerConstantContract(String ownerAddress, String contractAddress,
      Function function);

  TransactionExtention triggerConstantContract(String ownerAddress, String contractAddress,
      String callData);

  TransactionExtention triggerConstantContract(String ownerAddress, String contractAddress,
      String callData, long callValue, long tokenValue, String tokenId);

  Response.PricesResponseMessage getBandwidthPrices();

  Response.PricesResponseMessage getEnergyPrices();

  Response.PricesResponseMessage getMemoFee();

  Response.PricesResponseMessage getBandwidthPricesOnSolidity();

  Response.PricesResponseMessage getEnergyPricesOnSolidity();

  TransactionExtention clearContractABI(String ownerAddress, String contractAddress)
      throws IllegalException;

  ExchangeList getPaginatedExchangeList(long offset, long limit);

  ProposalList getPaginatedProposalList(long offset, long limit);

  BlockExtention getBlock(String blockIDOrNum, boolean detail);

  BlockExtention getBlock(boolean detail);

  Block getBlockByIdOrNum(String blockIDOrNum);

  SmartContractDataWrapper getContractInfo(String contractAddr);

  MarketOrderList getMarketOrderByAccount(String account);

  MarketOrder getMarketOrderById(String txn);

  MarketOrderList getMarketOrderListByPair(String sellTokenId, String buyTokenId);

  MarketOrderPairList getMarketPairList();

  MarketPriceList getMarketPriceByPair(String sellTokenId, String buyTokenId);

  TransactionExtention exchangeCreate(String ownerAddress, String firstToken, long firstBalance,
      String secondToken, long secondBalance) throws IllegalException;

  TransactionExtention exchangeInject(String ownerAddress, long exchangeId, String tokenId,
      long amount) throws IllegalException;

  TransactionExtention exchangeTransaction(String ownerAddress, long exchangeId, String tokenId,
      long amount, long expected) throws IllegalException;

  TransactionExtention exchangeWithdraw(String ownerAddress, long exchangeId, String tokenId,
      long quant) throws IllegalException;

  long getTransactionCountByBlockNum(long blockNum);

  TransactionExtention marketCancelOrder(String ownerAddress, String orderId)
      throws IllegalException;

  TransactionExtention marketSellAsset(String ownerAddress, String sellTokenId,
      long sellTokenQuantity, String buyTokenId, long buyTokenQuantity) throws IllegalException;

  TransactionExtention updateEnergyLimit(String ownerAddress, String contractAddress,
      long originEnergyLimit) throws IllegalException;

  TransactionExtention updateSetting(String ownerAddress, String contractAddress,
      long consumeUserResourcePercent) throws IllegalException;

  CreateSmartContract createSmartContract(String contractName, String address, String ABI,
      String code, long callValue, long consumeUserResourcePercent, long originEnergyLimit,
      long tokenValue, String tokenId) throws Exception;

  CreateSmartContract createSmartContract(String contractName, String address, String ABI,
      String code, long callValue, long consumeUserResourcePercent, long originEnergyLimit,
      long tokenValue, String tokenId, String libraryAddressPair, String compilerVersion)
      throws Exception;

  TransactionExtention deployContract(String contractName, String abiStr, String bytecode,
      List<Type<?>> constructorParams, long feeLimit, long consumeUserResourcePercent,
      long originEnergyLimit, long callValue, String tokenId, long tokenValue) throws Exception;
}
