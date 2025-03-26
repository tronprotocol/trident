package org.tron.trident.core;


import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
import org.tron.trident.core.transaction.BlockId;
import org.tron.trident.core.transaction.TransactionBuilder;
import org.tron.trident.core.utils.ByteArray;
import org.tron.trident.core.utils.Sha256Hash;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Response.EstimateEnergyMessage;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionInfo.code;
import org.tron.trident.utils.Base58Check;

@Disabled("add private key to enable this case")
class ContractTest extends BaseTest {

  @Test
  void testTransferTrc10() throws InterruptedException, IllegalException {
    TransactionExtention transactionExtention = client.transferTrc10(testAddress,
        "TAB1TVw5N8g1FLcKxPD17h2A3eEpSXvMQd", Integer.parseInt(tokenId), 100);
    Transaction transaction = client.signTransaction(transactionExtention);
    String txId = client.broadcastTransaction(transaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());
  }

  @Test
  void testTransfer() throws InterruptedException, IllegalException {
    TransactionExtention transactionExtention = client.transfer(testAddress,
        "TAB1TVw5N8g1FLcKxPD17h2A3eEpSXvMQd", 1_000_000L);
    Transaction transaction = client.signTransaction(transactionExtention);
    String txId = client.broadcastTransaction(transaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());
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

    TransactionExtention transactionExtention = client.deployContract("testDeployContract", abiStr,
        bytecode, null, 150_000_000L, 0, 1_000_000L, 0, null, 0);

    Transaction transaction = client.signTransaction(transactionExtention.getTransaction());
    String txId = client.broadcastTransaction(transaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());
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

    TransactionExtention transactionExtention = client.deployContract("testDConstructorParams",
        abiStr, bytecode,
        constructorParams, 1000_000_000L, 100,
        10_000_000L, 0L, null, 0L);
    //System.out.println("Transaction ID: " + txId);

    Transaction transaction = client.signTransaction(transactionExtention.getTransaction());
    String txId = client.broadcastTransaction(transaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());

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
    TransactionExtention transactionExtention = client.deployContract("testDConstructorParams",
        abiStr, bytecode,
        constructorParams, 100_000_000L, 100,
        10_000_000L, 1_000_000L, null, 0L);

    Transaction transaction = client.signTransaction(transactionExtention.getTransaction());
    String txId = client.broadcastTransaction(transaction);

    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());
  }

  @Test
  void testDeployContractWithTRC10() throws Exception {

    String abiStr = "{\"entrys\":[{\"inputs\":[],\"stateMutability\":\"payable\",\"type\":\"constructor\"},{\"inputs\":[{\"internalType\":\"address payable\",\"name\":\"toAddress\",\"type\":\"address\"},{\"internalType\":\"trcToken\",\"name\":\"id\",\"type\":\"trcToken\"},{\"internalType\":\"uint256\",\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"TransferTokenTo\",\"outputs\":[],\"stateMutability\":\"payable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"getResultInCon\",\"outputs\":[{\"internalType\":\"trcToken\",\"name\":\"\",\"type\":\"trcToken\"},{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"payable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"msgTokenValueAndTokenIdTest\",\"outputs\":[{\"internalType\":\"trcToken\",\"name\":\"\",\"type\":\"trcToken\"},{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"payable\",\"type\":\"function\"}]}";
    String bytecode = "60806040526000805560006001556000600255d3600081905550d260018190555034600281905550610332806100366000396000f3fe6080604052600436106100345760003560e01c806305c24200146100395780633be9ece71461005957806371dc08ce14610075575b600080fd5b610041610095565b60405161005093929190610214565b60405180910390f35b610073600480360381019061006e91906101a3565b6100ad565b005b61007d610135565b60405161008c93929190610214565b60405180910390f35b60008060008054600154600254925092509250909192565b8273ffffffffffffffffffffffffffffffffffffffff166108fc82908115029084801580156100db57600080fd5b5080678000000000000000111580156100f357600080fd5b5080620f42401015801561010657600080fd5b5060405160006040518083038185878a8ad094505050505015801561012f573d6000803e3d6000fd5b50505050565b600080600080d390506000d290506000349050828282955095509550505050909192565b600081359050610168816102b7565b6101718161024b565b905092915050565b600081359050610188816102ce565b92915050565b60008135905061019d816102e5565b92915050565b6000806000606084860312156101bc576101bb6102b2565b5b60006101ca86828701610159565b93505060206101db86828701610179565b92505060406101ec8682870161018e565b9150509250925092565b6101ff8161025d565b82525050565b61020e816102a8565b82525050565b600060608201905061022960008301866101f6565b6102366020830185610205565b6102436040830184610205565b949350505050565b600061025682610267565b9050919050565b6000819050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b600074ffffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b600080fd5b6102c081610287565b81146102cb57600080fd5b50565b6102d78161025d565b81146102e257600080fd5b50565b6102ee816102a8565b81146102f957600080fd5b5056fea26474726f6e58221220fdf0f3a4587a08bebc9e8382b06c5f235f5836675d1cefa1cd29157296a52b8264736f6c63430008060033";

    //callValue 1TRX
    //tokenId
    //tokenValue 10
    TransactionExtention transactionExtention = client.deployContract("testDeployContractWithTRC10",
        abiStr, bytecode,
        null, 100_000_000L, 100,
        10_000_000L, 1_000_000L, tokenId, 10L);
    //System.out.println("Transaction ID: " + txId);

    Transaction transaction = client.signTransaction(transactionExtention.getTransaction());
    String txId = client.broadcastTransaction(transaction);
    sleep(10_000L);

    TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    assertEquals(code.SUCESS, transactionInfo.getResult());
  }


  @Test
  void testTriggerContract() throws Exception {
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
    TransactionExtention transactionExtention = client.triggerContract(fromAddr, usdtAddr,
        encodedHex, 0, 0, null, 150_000_000L);

    Transaction signedTxn = client.signTransaction(transactionExtention);

    //System.out.println(signedTxn.toString());
    String ret = client.broadcastTransaction(signedTxn);
    //System.out.println("======== Result ========\n" + ret);
    sleep(10_000L);
    TransactionInfo transactionInfo = client.getTransactionInfoById(ret);
    assertEquals(0, transactionInfo.getResult().getNumber());

  }

  @Test
  void testTriggerContractWithFeeLimit() {
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
    try {
      client.triggerContract(fromAddr, usdtAddr,
          encodedHex, 0L, 0L, null, 0L);
      assert false;
    } catch (Exception e) {
      assertTrue(e instanceof IllegalException);
    }
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
    //System.out.println(estimateEnergyMessage.getEnergyRequired());
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
    EstimateEnergyMessage estimateEnergyMessage = client.estimateEnergy(fromAddr, strx,
        encodedHex, 1_000_000L, 0, "");
    //System.out.println(estimateEnergyMessage.getEnergyRequired());
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
    //System.out.println(hexResult);
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
    //System.out.println(ret);
    sleep(10_000L);
    TransactionInfo transactionInfo = client.getTransactionInfoById(ret);
    assertEquals(code.SUCESS, transactionInfo.getResult());

  }

  @Test
  void testGetContract() {
    //this is a CreatedByContract
    Contract contract = client.getContract("TAhMH9fxh5mLRki46qFkLckChxTykTvsVY"); //nile

    String originContractAddress = Base58Check.bytesToBase58(
        contract.getOriginAddr().toByteArray());
    Contract originContract = client.getContract(originContractAddress);
    assertFalse(originContract.getBytecode().isEmpty());

    assertTrue(contract.getTrxHash().toByteArray().length > 0);
    assertTrue(contract.getCodeHash().toByteArray().length > 0);
    assertEquals(0, contract.getVersion());
  }

  @Test
  void testTriggerConstantContract() throws InterruptedException, IllegalException {
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
    try {
      client.triggerConstantContract(fromAddr, usdtAddr,
          encodedHex, -1L, 0L, null);
      assert false;
    } catch (Exception e) {
      assert e instanceof IllegalArgumentException;
    }
    try {
      client.triggerConstantContract(fromAddr, usdtAddr,
          encodedHex, 0L, 0L, "999999");
      assert false;
    } catch (Exception e) {
      assert e instanceof IllegalArgumentException;
    }
    TransactionExtention transactionExtention = client.triggerConstantContract(fromAddr, usdtAddr,
        encodedHex, 0L, 0L, null);
    long energy = transactionExtention.getEnergyUsed();
    assertTrue(energy > 0);
  }

  @Test
  void testRefer() throws IllegalException {
    byte[] rawHashBytes = getBlockId();
    BlockId blockId = new BlockId(Sha256Hash.wrap(rawHashBytes));
    byte[] refBlockNumBytes = ByteArray.fromLong(blockId.getNum());
    long timestamp = System.currentTimeMillis();

    //enable but invalid blockId
    client.enableLocalCreate(null, 1);
    try {
      client.transferTrc10(testAddress,
          "TAB1TVw5N8g1FLcKxPD17h2A3eEpSXvMQd", Integer.parseInt(tokenId), 100);
      assert false;
    } catch (Exception e) {
      assert true;
    }

    //enable but valid blockId and timestamp
    client.enableLocalCreate(blockId, timestamp);
    TransactionExtention transactionExtention = client.transferTrc10(testAddress,
        "TAB1TVw5N8g1FLcKxPD17h2A3eEpSXvMQd", Integer.parseInt(tokenId), 100);
    assertArrayEquals(ByteArray.subArray(rawHashBytes, 8, 16),
        transactionExtention.getTransaction().getRawData().getRefBlockHash().toByteArray());
    assertArrayEquals(ByteArray.subArray(refBlockNumBytes, 6, 8),
        transactionExtention.getTransaction().getRawData().getRefBlockBytes().toByteArray());
    assertEquals(timestamp, transactionExtention.getTransaction().getRawData().getExpiration());

    //enable but invalid blockId
    client.setReferHeadBlockId(null);
    try {
      client.transferTrc10(testAddress,
          "TAB1TVw5N8g1FLcKxPD17h2A3eEpSXvMQd", Integer.parseInt(tokenId), 100);
      assert false;
    } catch (Exception e) {
      assert true;
    }

    //disable but set timestamp
    client.disableLocalCreate();
    try {
      client.setExpireTimeStamp(100L);
      assert false;
    } catch (Exception e) {
      assert true;
    }

    //disable
    transactionExtention = client.transferTrc10(testAddress,
        "TAB1TVw5N8g1FLcKxPD17h2A3eEpSXvMQd", Integer.parseInt(tokenId), 100);
    assertNotEquals(
        ByteArray.toHexString(ByteArray.subArray(rawHashBytes, 8, 16)),
        ByteArray.toHexString(
            transactionExtention.getTransaction().getRawData().getRefBlockHash().toByteArray()));
    assertNotEquals(ByteArray.toHexString(ByteArray.subArray(refBlockNumBytes, 6, 8)),
        ByteArray.toHexString(
            transactionExtention.getTransaction().getRawData().getRefBlockBytes().toByteArray()));
    assertNotEquals(timestamp, transactionExtention.getTransaction().getRawData().getExpiration());
  }

  private byte[] getBlockId() {
    byte[] id = new byte[32];
    new Random().nextBytes(id);
    return id;
  }
}