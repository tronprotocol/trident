package org.tron.trident.core.utils;

import static org.tron.trident.core.ApiWrapper.calculateTransactionHash;

import com.google.protobuf.ByteString;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Response.TransactionExtention;

public class TransactionUtils {

  /**
   * Set permission id for transaction contract
   * This is a helper method for multi-sign transactions
   */
  public static Transaction setPermissionId(Transaction transaction, int permissionId) {
    if (transaction == null || !transaction.hasRawData()) {
      throw new IllegalArgumentException("Transaction or raw data is null");
    }

    Transaction.raw rawData = transaction.getRawData();
    if (rawData.getContractCount() == 0) {
      throw new IllegalArgumentException("Transaction has no contracts");
    }

    // Get the first contract and set permission id
    Transaction.Contract contract = rawData.getContract(0);
    contract = contract.toBuilder()
        .setPermissionId(permissionId)
        .build();

    // Update raw data with modified contract
    rawData = rawData.toBuilder()
        .setContract(0, contract)
        .build();

    // Return updated transaction
    return transaction.toBuilder()
        .setRawData(rawData)
        .build();
  }

  /**
   * Set permission id for transaction extension
   * This is a helper method for multi-sign transactions
   */
  public static TransactionExtention setPermissionId(TransactionExtention transactionExt,
      int permissionId) {
    if (transactionExt == null || !transactionExt.hasTransaction()) {
      throw new IllegalArgumentException("Transaction extension or transaction is null");
    }

    Transaction updatedTransaction = setPermissionId(transactionExt.getTransaction(), permissionId);
    byte[] txId = calculateTransactionHash(updatedTransaction);
    return transactionExt.toBuilder()
        .setTransaction(updatedTransaction)
        .setTxid(ByteString.copyFrom(txId))
        .build();
  }

}