package org.tron.trident.core.transaction;

/**
 * The {@code TransactionBuilder} class provides mutator methods
 * for common used attributes.
 *
 * <p>The {@code TransactionBuilder} object are mostly used before signing a
 * transaction, for setting attributes values like {@link #setFeeLimit}, {@link
 * #setMemo}, {@link #setPermissionId}Etc.</p>
 *
 * @see org.tron.trident.proto.Chain.Transaction;
 * @since java version 1.8.0_231
 */

import com.google.protobuf.ByteString;
import lombok.Getter;
import lombok.Setter;
import org.tron.trident.proto.Chain.Transaction;

public class TransactionBuilder {

  @Getter
  @Setter
  private Transaction transaction;

  public TransactionBuilder(Transaction transaction) {
    this.transaction = transaction;
  }

  public TransactionBuilder setFeeLimit(long feeLimit) {
    transaction = transaction.toBuilder()
        .setRawData(transaction.getRawData().toBuilder().setFeeLimit(feeLimit))
        .build();
    return this;
  }

  public TransactionBuilder setMemo(byte[] memo) {
    transaction = transaction.toBuilder()
        .setRawData(transaction.getRawData().toBuilder().setData(ByteString.copyFrom(memo)))
        .build();
    return this;
  }

  public TransactionBuilder setMemo(String memo) {
    transaction = transaction.toBuilder()
        .setRawData(transaction.getRawData().toBuilder().setData(ByteString.copyFromUtf8(memo)))
        .build();
    return this;
  }

  /**
   * Set permission id for Transaction.Contract
   * This is a helper method for multi-sign transactions
   */
  public TransactionBuilder setContractPermissionId(int permissionId) {
    // Get the first contract and set permission id
    Transaction.Contract contract = transaction.getRawData().getContract(0).toBuilder()
        .setPermissionId(permissionId)
        .build();
    transaction = transaction.toBuilder()
        .setRawData(transaction.getRawData().toBuilder().setContract(0, contract))
        .build();
    return  this;
  }

  public Transaction build() {
    return this.transaction;
  }

}