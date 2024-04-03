package org.tron.trident.core.inceptors;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain;

public class TronWalletTest {

    /**
     * Test privateKey (Not important)
     */
    private static final String MAIN_WALLET_PRIVATE_KEY = "18ef152ae3711498556a22db1f820f33f27080aea51b19d39fdb6752a0591e1f";
    /**
     * test address (Not important)
     */
    private static final String MAIN_WALLET_ADDRESS = "TFtGcTF8HER3tHAL9Fzu95D5kfBixGYPfL";
    private static final String USDT_CONTRACT_ADDRESS = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf";

    @Test
    public void createOfflineAccount() {
        KeyPair keyPair = KeyPair.generate();
        String privateKey = keyPair.toPrivateKey();
        String address = keyPair.toBase58CheckAddress();
        System.out.println(privateKey);
        System.out.println(address);
        Assertions.assertNotNull(privateKey);
        Assertions.assertNotNull(address);
    }

    /**
     * By default get tron (trx) balance
     */
    @Test
    public void getWalletBalance() {
        ApiWrapper wrapper = ApiWrapper.ofNile(MAIN_WALLET_PRIVATE_KEY);
        long accountBalance = wrapper.getAccountBalance(MAIN_WALLET_ADDRESS);
        System.out.println(accountBalance);
        Assertions.assertTrue(accountBalance >= 0);
    }

    @Test
    public void getWalletBalanceByContract() {
        ApiWrapper wrapper = ApiWrapper.ofNile(MAIN_WALLET_PRIVATE_KEY);
        Contract contract = wrapper.getContract(USDT_CONTRACT_ADDRESS); //USDT Contract
        Trc20Contract trc20Contract = new Trc20Contract(contract, MAIN_WALLET_ADDRESS, wrapper);
        System.out.println(trc20Contract.symbol());
        System.out.println(trc20Contract.balanceOf(MAIN_WALLET_ADDRESS));
        System.out.println(trc20Contract.decimals());
    }

    /**
     * Activation requires transferring at least 0.1 TRX coins to your new Tron account
     */
    @Test
    public void activateAccount() {
        try {
            /* Generate new Address */
            KeyPair keyPair = KeyPair.generate();
            String targetPrivateKey = "18ef152ae3711498556a22db1f820f33f27080aea51b19d39fdb6752a0591e1f";
            String targetAddress = "TFtGcTF8HER3tHAL9Fzu95D5kfBixGYPfL";
            System.out.println(targetPrivateKey);
            System.out.println(targetAddress);

            /* Transfer Token (TRX)
             * amount = amount * 100000
             * 0.100000 = 1 * 100000 (6 decimal)
             * */
            long amount = 100000;
            ApiWrapper wrapper = ApiWrapper.ofNile(MAIN_WALLET_PRIVATE_KEY);
            Response.TransactionExtention transactionExtention = wrapper.transfer(MAIN_WALLET_ADDRESS, targetAddress, amount);
            Chain.Transaction transaction = wrapper.signTransaction(transactionExtention);
            Response.TransactionReturn transactionReturn = wrapper.blockingStub.broadcastTransaction(transaction);
            System.out.println("> Result : \n" + transactionReturn.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void transferTokenByContract() throws IllegalException {
        /* Generate new Address */
        KeyPair keyPair = KeyPair.generate();
        String targetPrivateKey = keyPair.toPrivateKey();
        String targetAddress = keyPair.toBase58CheckAddress();
        System.out.println(targetPrivateKey);
        System.out.println(targetAddress);

        /* Create Transaction */
        ApiWrapper wrapper = ApiWrapper.ofNile(MAIN_WALLET_PRIVATE_KEY);
        Response.TransactionExtention txnExt = wrapper.transfer(MAIN_WALLET_ADDRESS, targetAddress, USDT_CONTRACT_ADDRESS, 50000000);
        System.out.println("Transaction ID (txid) => " + Hex.toHexString(txnExt.getTxid().toByteArray()));

        /* Sign transaction */
        Chain.Transaction signedTxn = wrapper.signTransaction(txnExt);

        /* broadcast on tron network */
        Response.TransactionReturn ret = wrapper.blockingStub.broadcastTransaction(signedTxn);
        System.out.println("> Result\n" + ret.toString());
    }

    @Test
    public void createSubAccount() throws IllegalException {
        /* Generate new Address */
        KeyPair keyPair = KeyPair.generate();
        String subAccountPrivateKey = keyPair.toPrivateKey();
        String subAccountAddress = keyPair.toBase58CheckAddress();
        System.out.println(subAccountPrivateKey);
        System.out.println(subAccountAddress);

        /* Create SubAccount */
        ApiWrapper wrapper = ApiWrapper.ofNile(MAIN_WALLET_PRIVATE_KEY);
        Response.TransactionExtention txnExt = wrapper.createAccount(MAIN_WALLET_ADDRESS, subAccountAddress);

        /* Sign request */
        Chain.Transaction transaction = wrapper.signTransaction(txnExt);

        /* Broadcast to tron network */
        String result = wrapper.broadcastTransaction(transaction);
        System.out.println("Result : " + result);
    }
}
