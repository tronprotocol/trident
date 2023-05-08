package org.tron.trident.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.api.GrpcAPI;
import org.tron.trident.api.GrpcAPI.EmptyMessage;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Contract.TriggerSmartContract;
import org.tron.trident.proto.Response;
import org.tron.trident.proto.Response.BlockExtention;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionReturn;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

public class ApiWrapperTest {
    @Test
    public void testQuery() throws IllegalException {
        ApiWrapper client = ApiWrapper.ofMainnet("3333333333333333333333333333333333333333333333333333333333333333");

        GrpcAPI.TransactionIdList transactionIdList = client.getTransactionListFromPending();
        System.out.println(transactionIdList.getTxIdCount());

        long pendingSize  = client.getPendingSize();
        System.out.println(pendingSize);

        //String txId ="22d57a6a6e607e5bb8c7fbec3eacc590bc90b791701d917b313c8d3ac76a8d88";
        //Transaction transaction  = client.getTransactionFromPending(txId);
        //System.out.println(transaction);

        String blockId ="000000000309c3c40be03c04615856fc6672b08af6d2cdbbf500a7cf9920fbdb";
        Chain.Block block  = client.getBlockById(blockId);
        System.out.println(block);

        long timestamp  = client.getNextMaintenanceTime();
        System.out.println(timestamp);

        long burnTRX  = client.getBurnTRX();
        System.out.println(burnTRX);

        String blockId2 ="000000000309c3c40be03c04615856fc6672b08af6d2cdbbf500a7cf9920fbdb";
        long blockNum =50971588L;
        Response.BlockBalanceTrace blockBalanceTrace = client.getBlockBalance(blockId2,blockNum);
        System.out.println(blockBalanceTrace);

    }

    @Test
    public void testWithdrawBalance() throws Exception {
        ApiWrapper client = ApiWrapper.ofNile("bcbe4094385c836b779b9c59894d3aa0bbfcd8f2b7a3e3610f0a34d221b2c667");

        //--------------------withdrawBalance---------------
        //TUMpXGYUs99uQwVV6e9ZaLxGc4KrvbQsFZ
        TransactionExtention txnExt = client.withdrawBalance("41C9B9110BD5F6697075659AB3AAF6107840A9D96D");
        System.out.println("txn id => " + Hex.toHexString(txnExt.getTxid().toByteArray()));
        Transaction signedTxn = client.signTransaction(txnExt);
        System.out.println(signedTxn.toString());
        TransactionReturn ret = client.blockingStub.broadcastTransaction(signedTxn);
        System.out.println("======== Result ========\n" + ret.toString());

    }

    @Test
    public void testProposalCreate() throws Exception {
        ApiWrapper client = ApiWrapper.ofNile("bcbe4094385c836b779b9c59894d3aa0bbfcd8f2b7a3e3610f0a34d221b2c667");

        //--------------------ProposalCreate---------------
        //TUMpXGYUs99uQwVV6e9ZaLxGc4KrvbQsFZ
        HashMap<Long, Long> parameters = new HashMap<Long, Long>();
        parameters.put(1L, 2L);
        parameters.put(0L, 100000L);

        TransactionExtention txnExt = client.proposalCreate("41C9B9110BD5F6697075659AB3AAF6107840A9D96D",parameters);
        System.out.println("txn id => " + Hex.toHexString(txnExt.getTxid().toByteArray()));
        Transaction signedTxn = client.signTransaction(txnExt);
        System.out.println(signedTxn.toString());
        TransactionReturn ret = client.blockingStub.broadcastTransaction(signedTxn);
        System.out.println("======== Result ========\n" + ret.toString());

    }

    @Test
    public void testProposalApprove() throws Exception {
        ApiWrapper client = ApiWrapper.ofNile("bcbe4094385c836b779b9c59894d3aa0bbfcd8f2b7a3e3610f0a34d221b2c667");

        //--------------------ProposalApprove---------------
        //TUMpXGYUs99uQwVV6e9ZaLxGc4KrvbQsFZ
        TransactionExtention txnExt = client.approveProposal("41C9B9110BD5F6697075659AB3AAF6107840A9D96D",1,true);
        System.out.println("txn id => " + Hex.toHexString(txnExt.getTxid().toByteArray()));
        Transaction signedTxn = client.signTransaction(txnExt);
        System.out.println(signedTxn.toString());
        TransactionReturn ret = client.blockingStub.broadcastTransaction(signedTxn);
        System.out.println("======== Result ========\n" + ret.toString());

    }

    @Test
    public void testProposalDelete() throws Exception {
        ApiWrapper client = ApiWrapper.ofNile("bcbe4094385c836b779b9c59894d3aa0bbfcd8f2b7a3e3610f0a34d221b2c667");

        //--------------------ProposalApprove---------------
        //TUMpXGYUs99uQwVV6e9ZaLxGc4KrvbQsFZ
        TransactionExtention txnExt = client.deleteProposal("41C9B9110BD5F6697075659AB3AAF6107840A9D96D",1);
        System.out.println("txn id => " + Hex.toHexString(txnExt.getTxid().toByteArray()));
        Transaction signedTxn = client.signTransaction(txnExt);
        System.out.println(signedTxn.toString());
        TransactionReturn ret = client.blockingStub.broadcastTransaction(signedTxn);
        System.out.println("======== Result ========\n" + ret.toString());

    }


    @Test
    public void testSendTrc20Transaction() {
        ApiWrapper client = ApiWrapper.ofNile("3333333333333333333333333333333333333333333333333333333333333333");

        // transfer(address,uint256) returns (bool)
        Function trc20Transfer = new Function("transfer",
            Arrays.asList(new Address("TVjsyZ7fYF3qLF6BQgPmTEZy1xrNNyVAAA"),
                new Uint256(BigInteger.valueOf(10).multiply(BigInteger.valueOf(10).pow(18)))),
            Arrays.asList(new TypeReference<Bool>() {}));

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
}
