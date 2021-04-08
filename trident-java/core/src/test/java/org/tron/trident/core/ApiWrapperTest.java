package org.tron.trident.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.api.GrpcAPI.EmptyMessage;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Contract.TriggerSmartContract;
import org.tron.trident.proto.Response.BlockExtention;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionReturn;

import java.math.BigInteger;
import java.util.Arrays;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

public class ApiWrapperTest {
    @Test
    public void testGetNowBlockQuery() {
        ApiWrapper client = ApiWrapper.ofShasta("3333333333333333333333333333333333333333333333333333333333333333");
        BlockExtention block = client.blockingStub.getNowBlock2(EmptyMessage.newBuilder().build());

        System.out.println(block.getBlockHeader());
        assertTrue(block.getBlockHeader().getRawDataOrBuilder().getNumber() > 0);
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
