package org.tron.trident.core.transaction;

/**
 * The {@code TransactionBuilder} class provides mutator methods
 * for common used attributes.
 *
 * <p>The {@code TransactionBuilder} object are mostly used before signing a
 * transaction, for setting attributes values like {@link #setFeeLimit}, {@link
 * #setMemo}, Etc.</p>
 *
 * @since java version 1.8.0_231
 * @see org.tron.trident.proto.Chain.Transaction;
 */

import com.google.protobuf.ByteString;
import org.tron.trident.proto.Chain.Transaction;

public class TransactionBuilder {
    private Transaction transaction;

    public TransactionBuilder(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
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

    public Transaction build() {
        return this.transaction;
    }
}