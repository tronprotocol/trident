package org.tron.trident.core.contract;

import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.abi.AbiUtils;
import org.tron.trident.core.exceptions.ContractCreateException;
import org.tron.trident.core.transaction.TransactionBuilder;
import org.tron.trident.proto.Common.SmartContract;
import org.tron.trident.proto.Common.SmartContract.ABI;
import org.tron.trident.proto.Common.SmartContract.ABI.Entry;
import org.tron.trident.proto.Common.SmartContract.ABI.Entry.Param;
import org.tron.trident.proto.Contract.CreateSmartContract;

/**
 * The {@code Contract} class is a wrapper for the {@code SmartContract}
 * class defined in the .proto file. An object of {@code Contract} contains
 * same attributes with the {@code SmartContract} class.
 *
 * <p>This class provides mutator and accessor methods for all
 * attributes. Besides, functions of smart contracts are wrapped
 * and can be easily viewed through {@link
 * org.tron.trident.core.contract.ContractFunction#toString()}
 *
 * @see org.tron.trident.core.contract.ContractFunction
 * @see org.tron.trident.proto.Common.SmartContract
 * @since jdk 1.8.0_231
 */

public class Contract {

  @Getter
  @Setter
  protected ApiWrapper wrapper;
  @Setter
  @Getter
  protected ByteString originAddr = ByteString.EMPTY;//who create it? normal or contract address
  @Setter
  @Getter
  protected ByteString cntrAddr = ByteString.EMPTY;
  @Getter
  protected ABI abi;
  @Setter
  @Getter
  protected ByteString bytecode;
  //the amount of deposit TRX(unit sun), default is 0
  @Setter
  @Getter
  protected long callValue = 0;
  //the energy percent user consumes, default is 100%
  @Setter
  @Getter
  protected long consumeUserResourcePercent = 100;
  @Setter
  @Getter
  protected String name = "";
  @Setter
  @Getter
  protected long originEnergyLimit = 1;
  @Setter
  @Getter
  protected ByteString codeHash = ByteString.EMPTY;
  @Setter
  @Getter
  protected ByteString trxHash = ByteString.EMPTY;
  @Setter
  @Getter
  protected int version = 0;

  //Current transaction owner's address, to call or trigger contract"
  @Setter
  @Getter
  protected ByteString ownerAddr = ByteString.EMPTY;
  @Setter
  @Getter
  protected List<ContractFunction> functions = new ArrayList<>();
  //the constructor is loaded automatically from the abi, if has
  @Getter
  protected ContractConstructor constructor = null;

  public Contract(Contract cntr, String ownerAddr, ApiWrapper wrapper) {
    this.originAddr = cntr.getOriginAddr();
    this.cntrAddr = cntr.getCntrAddr();
    this.abi = cntr.getAbi();
    this.bytecode = cntr.getBytecode();
    this.callValue = cntr.getCallValue();
    this.consumeUserResourcePercent = cntr.getConsumeUserResourcePercent();
    this.name = cntr.getName();
    this.originEnergyLimit = cntr.getOriginEnergyLimit();
    this.codeHash = cntr.getCodeHash();
    this.trxHash = cntr.getTrxHash();
    this.ownerAddr = ApiWrapper.parseAddress(ownerAddr);
    this.wrapper = wrapper;
  }

  public Contract(ByteString cntrAddr, ABI abi, ByteString bytecode,
      long consumeUserResourcePercent, String name, long originEnergyLimit) {
    this.cntrAddr = cntrAddr;
    this.abi = abi;
    this.bytecode = bytecode;
    this.consumeUserResourcePercent = consumeUserResourcePercent;
    this.name = name;
    this.originEnergyLimit = originEnergyLimit;
  }

  public Contract(Builder builder) {
    this.originAddr = builder.originAddr;
    this.cntrAddr = builder.cntrAddr;
    this.abi = builder.abi;
    this.bytecode = builder.bytecode;
    this.callValue = builder.callValue;
    this.consumeUserResourcePercent = builder.consumeUserResourcePercent;
    this.name = builder.name;
    this.originEnergyLimit = builder.originEnergyLimit;
    this.codeHash = builder.codeHash;
    this.trxHash = builder.trxHash;
    this.version = builder.version;

    this.ownerAddr = builder.ownerAddr;
    abiToFunctions();
  }

  public void setAbi(String abiString) throws Exception {
    ABI.Builder builder = ABI.newBuilder();
    loadAbiFromJson(abiString, builder);
    this.abi = builder.build();
  }

  /**
   * create a CreateSmartContract object to get ready for deployment.
   * please note if any deposit is made during deployment(@see callValue),
   * the constructor of this contract must be payable
   *
   * @return TransactionBuilder object for signing and broadcasting
   * @throws Exception if deployment duplicating / owner and origin address don't match
   * @throws ContractCreateException if passes parameters but no constructor exists
   */
  public CreateSmartContract createSmartContract(List<Type<?>> buildParams) throws Exception {
    //throws if deployed
    if (!this.cntrAddr.isEmpty()) {
      throw new ContractCreateException("This contract has already been deployed.");
    }
    //throws if origin address does not match owner address
    if (!this.originAddr.equals(this.ownerAddr)) {
      throw new ContractCreateException("Origin address and owner address mismatch.");
    }
    loadConstructor();
    //throws if the contract does not have a constructor
    if (null == this.constructor && !buildParams.isEmpty()) {
      throw new ContractCreateException("The contract does not have a constructor.");
    }
    if (!buildParams.isEmpty()) {
      this.constructor.encodeParameter(buildParams);
      setBytecode(getBytecode().concat(this.constructor.getBytecode()));
    }

    //create
    CreateSmartContract.Builder createSmartContractBuilder = CreateSmartContract.newBuilder();
    createSmartContractBuilder.setOwnerAddress(ownerAddr);
    createSmartContractBuilder.setNewContract(toProto());

    return createSmartContractBuilder.build();
  }

  /**
   * Convert abi entries to ContractFunction objects
   *
   * @see org.tron.trident.proto.Common.SmartContract.ABI.Entry
   * @see ContractFunction ;
   */
  protected void abiToFunctions() {
    int funcNum = abi.getEntrysCount();
    for (int i = 0; i < funcNum; i++) {
      Entry funcAbi = abi.getEntrysList().get(i);
      if (funcAbi.getTypeValue() == 2) {
        ContractFunction.Builder builder = new ContractFunction.Builder();
        builder.setName(funcAbi.getName());
        builder.setAbi(funcAbi);
        builder.setCntr(this);
        builder.setOwnerAddr(this.ownerAddr);
        //if has input
        if (0 != funcAbi.getInputsCount()) {
          List<Param> params = funcAbi.getInputsList();
          builder.setInputParams(collectParams(params, 'p'));
          builder.setInputTypes(collectParams(params, 't'));
        }
        //if has output
        if (0 != funcAbi.getOutputsCount()) {
          List<Param> params = funcAbi.getOutputsList();
          if (null != params.get(0).getName()) {
            builder.setOutput((String) collectParams(params, 'p').get(0));
          }
          builder.setOutputType((String) collectParams(params, 't').get(0));
        }

        switch (funcAbi.getStateMutabilityValue()) {
          case 0:
            builder.setStateMutability("unknownmutabilitytype");
            break;
          case 1:
            builder.setStateMutability("pure");
            break;
          case 2:
            builder.setStateMutability("view");
            break;
          case 3:
            builder.setStateMutability("nonpayable");
            break;
          case 4:
            builder.setStateMutability("payable");
            break;
          default:
        }

        functions.add(builder.build());
      }
    }
  }

  protected List<String> collectParams(List<Param> params, char flag) {
    List<String> ret = new ArrayList<>();
    switch (flag) {
      //p = param, t = type
      case 'p':
        for (Param p : params) {
          ret.add(p.getName());
        }
        break;
      case 't':
        for (Param p : params) {
          ret.add(p.getType());
        }
        break;
      default:
    }
    return ret;
  }

  /**
   * build a SmartContract object
   *
   * @return SmartContract object
   */
  public SmartContract toProto() {
    return SmartContract.newBuilder()
        .setOriginAddress(originAddr)
        .setContractAddress(cntrAddr)
        .setAbi(abi)
        .setBytecode(bytecode)
        .setCallValue(callValue)
        .setConsumeUserResourcePercent(consumeUserResourcePercent)
        .setName(name)
        .setOriginEnergyLimit(originEnergyLimit)
        .setCodeHash(codeHash)
        .setTrxHash(trxHash)
        .setVersion(version)
        .build();
  }

  public TransactionBuilder deploy() throws Exception {
    //No deposit when creating contract
    return deploy(Collections.emptyList());
  }

  public TransactionBuilder deploy(List<Type<?>> buildParams) throws Exception {
    CreateSmartContract createSmartContract = createSmartContract(buildParams);
    return new TransactionBuilder(
        wrapper.blockingStub.deployContract(createSmartContract).getTransaction());
  }

  public static class Builder {

    protected ByteString originAddr = ByteString.EMPTY;
    protected ByteString cntrAddr = ByteString.EMPTY;
    protected ABI abi;
    protected ByteString bytecode;
    protected long callValue = 0;
    protected long consumeUserResourcePercent = 100;
    protected String name = "";
    protected long originEnergyLimit = 1;
    protected ByteString codeHash = ByteString.EMPTY;
    protected ByteString trxHash = ByteString.EMPTY;
    protected int version = 0;

    protected ByteString ownerAddr = ByteString.EMPTY;

    public Builder setOriginAddr(ByteString originAddr) {
      this.originAddr = originAddr;
      return this;
    }

    public Builder setCntrAddr(ByteString cntrAddr) {
      this.cntrAddr = cntrAddr;
      return this;
    }

    public Builder setAbi(ABI abi) {
      this.abi = abi;
      return this;
    }

    public Builder setAbi(String abiString) throws Exception {
      ABI.Builder builder = ABI.newBuilder();
      loadAbiFromJson(abiString, builder);
      this.abi = builder.build();
      return this;
    }

    public Builder setBytecode(ByteString bytecode) {
      this.bytecode = bytecode;
      return this;
    }

    public Builder setCallValue(long callValue) {
      this.callValue = callValue;
      return this;
    }

    public Builder setConsumeUserResourcePercent(long consumeUserResourcePercent) {
      this.consumeUserResourcePercent = consumeUserResourcePercent;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setOriginEnergyLimit(long originEnergyLimit) {
      this.originEnergyLimit = originEnergyLimit;
      return this;
    }

    public Builder setCodeHash(ByteString codeHash) {
      this.codeHash = codeHash;
      return this;
    }

    public Builder setTrxHash(ByteString trxHash) {
      this.trxHash = trxHash;
      return this;
    }

    public Builder setVersion(int version) {
      this.version = version;
      return this;
    }

    public Builder setOwnerAddr(ByteString ownerAddr) {
      this.ownerAddr = ownerAddr;
      return this;
    }

    public Contract build() {
      return new Contract(this);
    }
  }

  /**
   * load the constructor from ABI
   */
  protected void loadConstructor() {
    for (Entry e : abi.getEntrysList()) {
      if (e.getName().equals("")) {
        this.constructor = new ContractConstructor(e);
      }
    }
  }

  /**
   * load abi from json format string
   *
   * @param abiString abi string in json format
   * @throws Exception if the input is not valid JSON format or there are unknown fields in the input
   */
  public static void loadAbiFromJson(String abiString, ABI.Builder builder) throws Exception {
    if (abiString == null || abiString.isEmpty()) {
      return;
    }
    AbiUtils.loadAbiFromJson(abiString, builder);
  }

}