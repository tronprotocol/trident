package org.tron.trident.core;


import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.FunctionReturnDecoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.transaction.TransactionBuilder;
import org.tron.trident.core.utils.ByteArray;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Common.SmartContract;
import org.tron.trident.proto.Response.EstimateEnergyMessage;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionInfo.code;

@Disabled("add private key to enable this case")
class ContractTest extends BaseTest {

  @Test
  void testTransferTrc10() throws InterruptedException, IllegalException {
    TransactionExtention transactionExtention = client.transferTrc10(testAddress,
        "TAB1TVw5N8g1FLcKxPD17h2A3eEpSXvMQd", 1000587, 100);
    Transaction transaction = client.signTransaction(transactionExtention);
    String txId = client.broadcastTransaction(transaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());
  }

  @Test
  void testDeployByContract() throws Exception {
    String abiStr = "{\"entrys\":[{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"initTotal\",\"type\":\"uint256\"}],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"balanceOf\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"i\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\":\"j\",\"type\":\"uint256\"}],\"name\":\"set\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]}";

    // 2. bytecode
    String bytecode = "608060405234801561000f575f80fd5b50d3801561001b575f80fd5b50d28015610027575f80fd5b5060405161034e38038061034e8339818101604052810190610049919061008d565b80600181905550506100b8565b5f80fd5b5f819050919050565b61006c8161005a565b8114610076575f80fd5b50565b5f8151905061008781610063565b92915050565b5f602082840312156100a2576100a1610056565b5b5f6100af84828501610079565b91505092915050565b610289806100c55f395ff3fe608060405234801561000f575f80fd5b50d3801561001b575f80fd5b50d28015610027575f80fd5b506004361061004c575f3560e01c80631ab06ee5146100505780639cc7f7081461006c575b5f80fd5b61006a6004803603810190610065919061012f565b61009c565b005b6100866004803603810190610081919061016d565b6100e4565b60405161009391906101a7565b60405180910390f35b805f808481526020019081526020015f20546001546100bb91906101ed565b6100c59190610220565b600181905550805f808481526020019081526020015f20819055505050565b5f602052805f5260405f205f915090505481565b5f80fd5b5f819050919050565b61010e816100fc565b8114610118575f80fd5b50565b5f8135905061012981610105565b92915050565b5f8060408385031215610145576101446100f8565b5b5f6101528582860161011b565b92505060206101638582860161011b565b9150509250929050565b5f60208284031215610182576101816100f8565b5b5f61018f8482850161011b565b91505092915050565b6101a1816100fc565b82525050565b5f6020820190506101ba5f830184610198565b92915050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b5f6101f7826100fc565b9150610202836100fc565b925082820390508181111561021a576102196101c0565b5b92915050565b5f61022a826100fc565b9150610235836100fc565b925082820190508082111561024d5761024c6101c0565b5b9291505056fea26474726f6e58221220b6b9c602900efe0e4231e740f9a350b7b7c76b6ab51fdd7438eacdefe644dc1f64736f6c63430008160033";

    // 3. constructorParams
    List<Type<?>> constructorParams = Collections.singletonList(
        new Uint256(BigInteger.valueOf(100))  // initTotal 100
    );

    // 4. Contract.Builder
    Contract.Builder builder = new Contract.Builder();
    SmartContract.ABI.Builder abiBuilder = SmartContract.ABI.newBuilder();
    Contract.loadAbiFromJson(abiStr, abiBuilder);
    SmartContract.ABI abi = abiBuilder.build();

    builder.setOwnerAddr(ApiWrapper.parseAddress(client.keyPair.toBase58CheckAddress()))
        .setOriginAddr(ApiWrapper.parseAddress(client.keyPair.toBase58CheckAddress()))
        .setAbi(abi)
        .setBytecode(ApiWrapper.parseHex(bytecode))
        .setCallValue(0L)
        .setConsumeUserResourcePercent(100)
        .setOriginEnergyLimit(10_000_000L);

    // 5. Contract
    Contract contract = builder.build();
    contract.setWrapper(client);
    // 6. deploy
    TransactionBuilder txBuilder = contract.deploy(constructorParams);
    txBuilder.setFeeLimit(1000_000_000L);

    // 7. sign & broadcast
    Transaction transaction = client.signTransaction(txBuilder.getTransaction());
    String txId = client.broadcastTransaction(transaction);
    System.out.println("Deploy contract transaction id: " + txId);
  }

  @Test
  void testDeployContract() throws Exception {
    String abiStr = "{\"entrys\":[{\"constant\":false,\"inputs\":[{\"name\":\"i\",\"type\":\"uint256\"}],\"name\":\"findArgsByIndexTest\",\"outputs\":[{\"name\":\"z\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]}";
    String bytecode =
        "608060405234801561001057600080fd5b50610134806100206000396000f3006080604052600436106100405763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663329000b5"
            + "8114610045575b600080fd5b34801561005157600080fd5b5061005d60043561006f565b60408051918252519081900360200190f35b60408051600380825260808201909252"
            + "6000916060919060208201838038833901905050905060018160008151811015156100a657fe5b602090810290910101528051600290829060019081106100c257fe5b602090"
            + "810290910101528051600390829060029081106100de57fe5b6020908102909101015280518190849081106100f657fe5b906020019060200201519150509190505600a16562"
            + "7a7a72305820b24fc247fdaf3644b3c4c94fcee380aa610ed83415061ff9e65d7fa94a5a50a00029";

    String txId = client.deployContract("testDeployContract", abiStr, bytecode);
    System.out.println("Transaction ID: " + txId);
  }

  @Test
  void testDeployContractWithConstructorParams() throws Exception {
    String abiStr = "{\"entrys\":[{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"initTotal\",\"type\":\"uint256\"}],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"balanceOf\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"i\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\":\"j\",\"type\":\"uint256\"}],\"name\":\"set\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]}";

    // 2. bytecode
    String bytecode = "608060405234801561000f575f80fd5b50d3801561001b575f80fd5b50d28015610027575f80fd5b5060405161034e38038061034e8339818101604052810190610049919061008d565b80600181905550506100b8565b5f80fd5b5f819050919050565b61006c8161005a565b8114610076575f80fd5b50565b5f8151905061008781610063565b92915050565b5f602082840312156100a2576100a1610056565b5b5f6100af84828501610079565b91505092915050565b610289806100c55f395ff3fe608060405234801561000f575f80fd5b50d3801561001b575f80fd5b50d28015610027575f80fd5b506004361061004c575f3560e01c80631ab06ee5146100505780639cc7f7081461006c575b5f80fd5b61006a6004803603810190610065919061012f565b61009c565b005b6100866004803603810190610081919061016d565b6100e4565b60405161009391906101a7565b60405180910390f35b805f808481526020019081526020015f20546001546100bb91906101ed565b6100c59190610220565b600181905550805f808481526020019081526020015f20819055505050565b5f602052805f5260405f205f915090505481565b5f80fd5b5f819050919050565b61010e816100fc565b8114610118575f80fd5b50565b5f8135905061012981610105565b92915050565b5f8060408385031215610145576101446100f8565b5b5f6101528582860161011b565b92505060206101638582860161011b565b9150509250929050565b5f60208284031215610182576101816100f8565b5b5f61018f8482850161011b565b91505092915050565b6101a1816100fc565b82525050565b5f6020820190506101ba5f830184610198565b92915050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b5f6101f7826100fc565b9150610202836100fc565b925082820390508181111561021a576102196101c0565b5b92915050565b5f61022a826100fc565b9150610235836100fc565b925082820190508082111561024d5761024c6101c0565b5b9291505056fea26474726f6e58221220b6b9c602900efe0e4231e740f9a350b7b7c76b6ab51fdd7438eacdefe644dc1f64736f6c63430008160033";

    // 3. constructorParams
    List<Type<?>> constructorParams = Collections.singletonList(
        new Uint256(BigInteger.valueOf(100))  // initTotal 100
    );

    String txId = client.deployContract("testDConstructorParams", abiStr, bytecode,
        constructorParams, 1000_000_000L, 100,
        10_000_000L, 0L);
    System.out.println("Transaction ID: " + txId);
  }

  @Test
  void testDeployContractWithPayableConstructorParams() throws Exception {
    //payable constructor
    String abiStr = "{\"entrys\":[{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"initTotal\",\"type\":\"uint256\"}],\"stateMutability\":\"payable\",\"type\":\"constructor\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"balanceOf\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"i\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\":\"j\",\"type\":\"uint256\"}],\"name\":\"set\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]}";
    //bytecode
    String bytecode = "608060405260405161032a38038061032a83398181016040528101906100259190610069565b8060018190555050610094565b5f80fd5b5f819050919050565b61004881610036565b8114610052575f80fd5b50565b5f815190506100638161003f565b92915050565b5f6020828403121561007e5761007d610032565b5b5f61008b84828501610055565b91505092915050565b610289806100a15f395ff3fe608060405234801561000f575f80fd5b50d3801561001b575f80fd5b50d28015610027575f80fd5b506004361061004c575f3560e01c80631ab06ee5146100505780639cc7f7081461006c575b5f80fd5b61006a6004803603810190610065919061012f565b61009c565b005b6100866004803603810190610081919061016d565b6100e4565b60405161009391906101a7565b60405180910390f35b805f808481526020019081526020015f20546001546100bb91906101ed565b6100c59190610220565b600181905550805f808481526020019081526020015f20819055505050565b5f602052805f5260405f205f915090505481565b5f80fd5b5f819050919050565b61010e816100fc565b8114610118575f80fd5b50565b5f8135905061012981610105565b92915050565b5f8060408385031215610145576101446100f8565b5b5f6101528582860161011b565b92505060206101638582860161011b565b9150509250929050565b5f60208284031215610182576101816100f8565b5b5f61018f8482850161011b565b91505092915050565b6101a1816100fc565b82525050565b5f6020820190506101ba5f830184610198565b92915050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b5f6101f7826100fc565b9150610202836100fc565b925082820390508181111561021a576102196101c0565b5b92915050565b5f61022a826100fc565b9150610235836100fc565b925082820190508082111561024d5761024c6101c0565b5b9291505056fea26474726f6e58221220c1c1c36ad3fb4f0e2ffaffca14de80c05c6d478081e6eab843f189e2a5f7dfc964736f6c63430008160033";

    //constructorParams
    List<Type<?>> constructorParams = Collections.singletonList(
        new Uint256(BigInteger.valueOf(100))  // initTotal 100
    );

    //callValue 1TRX
    String txId = client.deployContract("testDConstructorParams", abiStr, bytecode,
        constructorParams, 100_000_000L, 100,
        10_000_000L, 1_000_000L);
    System.out.println("Transaction ID: " + txId);
  }


  @Test
  void testTriggerContract() throws InterruptedException, IllegalException {
    // transfer(address,uint256) returns (bool)
    String usdtAddr = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf"; //nile
    String fromAddr = client.keyPair.toBase58CheckAddress();
    String toAddress = "TVjsyZ7fYF3qLF6BQgPmTEZy1xrNNyVAAA";
    Function trc20Transfer = new Function("transfer",
        Arrays.asList(new Address(toAddress),
            new Uint256(BigInteger.valueOf(10).multiply(BigInteger.valueOf(10).pow(6)))),
        Collections.singletonList(new TypeReference<Bool>() {
        }));
    TransactionExtention transactionExtention = client.triggerContract(fromAddr, usdtAddr,
        trc20Transfer);

    Transaction signedTxn = client.signTransaction(transactionExtention);

    System.out.println(signedTxn.toString());
    String ret = client.broadcastTransaction(signedTxn);
    System.out.println("======== Result ========\n" + ret);
    sleep(10_000L);
    TransactionInfo transactionInfo = client.getTransactionInfoById(ret);
    assertEquals(0, transactionInfo.getResult().getNumber());

  }

  @Test
  void testTriggerContractWithBroadcast() throws InterruptedException, IllegalException {
    //  function deposit() external payable returns (uint256 strxAmount);
    String strx = "TZ8du1HkatTWDbS6FLZei4dQfjfpSm9mxp"; //nile
    String fromAddr = client.keyPair.toBase58CheckAddress();
    Function depositFunction = new Function("deposit",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));
    String ret = client.triggerContractWithBroadcast(fromAddr, strx, depositFunction, 100,
        500_000_000);
    System.out.println(ret);
    sleep(10_000L);
    TransactionInfo transactionInfo = client.getTransactionInfoById(ret);
    assertEquals(0, transactionInfo.getResult().getNumber());
  }


  @Test
  void testEstimateEnergyV2() {
    // transfer(address,uint256) returns (bool)
    String usdtAddr = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf"; //nile
    String fromAddr = client.keyPair.toBase58CheckAddress();
    String toAddress = "TVjsyZ7fYF3qLF6BQgPmTEZy1xrNNyVAAA";
    Function trc20Transfer = new Function("transfer",
        Arrays.asList(new Address(toAddress),
            new Uint256(BigInteger.valueOf(10).multiply(BigInteger.valueOf(10).pow(6)))),
        Collections.singletonList(new TypeReference<Bool>() {
        }));
    String encodedHex = FunctionEncoder.encode(trc20Transfer);
    EstimateEnergyMessage estimateEnergyMessage = client.estimateEnergyV2(fromAddr, usdtAddr,
        encodedHex);
    System.out.println(estimateEnergyMessage.getEnergyRequired());
    assertTrue(estimateEnergyMessage.getEnergyRequired() > 0);
    assertTrue(estimateEnergyMessage.getResult().getResult());
  }

  @Test
  void testEstimateEnergyV2WithCallValue() {
    //  function deposit() external payable returns (uint256 strxAmount);
    String strx = "TZ8du1HkatTWDbS6FLZei4dQfjfpSm9mxp"; //nile
    String fromAddr = client.keyPair.toBase58CheckAddress();
    Function depositFunction = new Function("deposit",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));
    String encodedHex = FunctionEncoder.encode(depositFunction);
    EstimateEnergyMessage estimateEnergyMessage = client.estimateEnergyV2(fromAddr, strx,
        encodedHex, 1_000_000L, 0, "");
    System.out.println(estimateEnergyMessage.getEnergyRequired());
    assertTrue(estimateEnergyMessage.getEnergyRequired() > 0);
    assertTrue(estimateEnergyMessage.getResult().getResult());
  }

  @Test
  void testConstantCallV2() {
    // balanceOf(address) returns (uint256)
    String usdtAddr = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf"; //nile
    String ownerAddr = client.keyPair.toBase58CheckAddress();
    String toAddress = "TVjsyZ7fYF3qLF6BQgPmTEZy1xrNNyVAAA";
    Function balanceOfFunction = new Function("balanceOf",
        Collections.singletonList(new Address(ownerAddr)),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));
    String encodedHex = FunctionEncoder.encode(balanceOfFunction);
    TransactionExtention transactionExtention = client.constantCallV2(ownerAddr, usdtAddr,
        encodedHex);
    String hexResult = ByteArray.toHexString(
        transactionExtention.getConstantResult(0).toByteArray());
    System.out.println(hexResult);
    List<Type> decoded = FunctionReturnDecoder.decode(hexResult,
        balanceOfFunction.getOutputParameters());
    assertFalse(decoded.isEmpty());
    BigInteger balance = ((Uint256) decoded.get(0)).getValue();
    assertTrue(balance.longValue() > 0);
  }

  @Test
  void testTriggerCallV2() throws InterruptedException, IllegalException {
    // transfer(address,uint256) returns (bool)
    String usdtAddr = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf"; //nile
    String fromAddr = client.keyPair.toBase58CheckAddress();
    String toAddress = "TVjsyZ7fYF3qLF6BQgPmTEZy1xrNNyVAAA";
    Function trc20Transfer = new Function("transfer",
        Arrays.asList(new Address(toAddress),
            new Uint256(BigInteger.valueOf(1).multiply(BigInteger.valueOf(10).pow(6)))),
        Collections.singletonList(new TypeReference<Bool>() {
        }));
    String encodedHex = FunctionEncoder.encode(trc20Transfer);
    TransactionBuilder transactionBuilder = client.triggerCallV2(fromAddr, usdtAddr, encodedHex);
    Transaction signedTxn = client.signTransaction(transactionBuilder.getTransaction());
    String ret = client.broadcastTransaction(signedTxn);
    System.out.println(ret);
    sleep(10_000L);
    TransactionInfo transactionInfo = client.getTransactionInfoById(ret);
    assertEquals(code.SUCESS, transactionInfo.getResult());

  }

}
