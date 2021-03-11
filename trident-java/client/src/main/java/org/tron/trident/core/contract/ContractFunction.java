package org.tron.trident.core.contract;

/**
 * The class {@code ContractFunction} provides a easier way to access smart contract
 * functions.
 *
 * <p>With a {@code ContractFunction} object, it's easy for users to see the function
 * declaration and easy to call by construct a {@link
 * org.tron.trident.abi.datatypes.Function}</p>
 *
 * @since jdk 13.0.2+8
 * @see org.tron.trident.abi.datatypes.Function
 * @see org.tron.trident.proto.Common.SmartContract
 */

import com.google.protobuf.ByteString;
import org.tron.trident.proto.Common.SmartContract.ABI.Entry;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;

public class ContractFunction {
    private String name;
    private Entry abi;
    private Contract cntr;
    private ByteString ownerAddr;
    private List<String> inputParams = new ArrayList();
    private List<String> inputTypes = new ArrayList();
    private String output = "";
    private String outputType;
    private long callValue = 0;
    private long callTokenValue = 0;
    private int callTokenId = 0;

  public ContractFunction(Builder builder) {
    this.name = builder.name;
    this.abi = builder.abi;
    this.cntr = builder.cntr;
    this.ownerAddr = builder.ownerAddr;
    this.inputParams = builder.inputParams;
    this.inputTypes = builder.inputTypes;
    this.output = builder.output;
    this.outputType = builder.outputType;
    this.callValue = builder.callValue;
    this.callTokenValue = builder.callTokenValue;
    this.callTokenId = builder.callTokenId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Entry getAbi() {
    return abi;
  }

  public void setAbi(Entry abi) {
    this.abi = abi;
  }

  public Contract getCntr() {
    return cntr;
  }

  public void setCntr(Contract cntr) {
    this.cntr = cntr;
  }

  public ByteString getOwnerAddr() {
    return ownerAddr;
  }

  public void setOwnerAddr(ByteString ownerAddr) {
    this.ownerAddr = ownerAddr;
  }

  public List<String> getInputParams() {
    return inputParams;
  }

  public void setInputParams(List<String> inputParams) {
    this.inputParams = inputParams;
  }

  public List<String> getInputTypes() {
    return inputTypes;
  }

  public void setInputTypes(List<String> inputTypes) {
    this.inputTypes = inputTypes;
  }

  public String getOutput() {
    return output;
  }

  public void setOutput(String outputs) {
    this.output = output;
  }

  public String getOutputType() {
    return outputType;
  }

  public void setOutputType(String outputType) {
    this.outputType = outputType;
  }

  public long getCallValue() {
    return callValue;
  }

  public void setCallValue(long callValue) {
    this.callValue = callValue;
  }

  public long getCallTokenValue() {
    return callTokenValue;
  }

  public void setCallTokenValue(long callTokenValue) {
    this.callTokenValue = callTokenValue;
  }

  public int getCallTokenId() {
    return callTokenId;
  }

  public void setCallTokenId(int callTokenId) {
    this.callTokenId = callTokenId;
  }

  public static class Builder {
    private String name;
    private Entry abi;
    private Contract cntr;
    private ByteString ownerAddr;
    private List<String> inputParams = new ArrayList();
    private List<String> inputTypes = new ArrayList();
    private String output = "";
    private String outputType;
    private long callValue = 0;
    private long callTokenValue = 0;
    private int callTokenId = 0;

    public Builder setName(String name) {
        this.name = name;
        return this;
    }

    public Builder setAbi(Entry abi) {
        this.abi = abi;
        return this;
    }

    public Builder setCntr(Contract cntr) {
        this.cntr = cntr;
        return this;
    }

    public Builder setOwnerAddr(ByteString ownerAddr) {
        this.ownerAddr = ownerAddr;
        return this;
    }

    public Builder setInputParams(List inputParams) {
        this.inputParams = inputParams;
        return this;
    }

    public Builder setInputTypes(List inputTypes) {
        this.inputTypes = inputTypes;
        return this;
    }

    public Builder setOutput(String output) {
        this.output = output;
        return this;
    }

    public Builder setOutputType(String outputType) {
        this.outputType = outputType;
        return this;
    }

    public Builder setCallValue(long callValue) {
        this.callValue = callValue;
        return this;
    }

    public Builder setCallTokenValue(long callTokenValue) {
        this.callTokenValue = callTokenValue;
        return this;
    }

    public Builder setCallTokenId(int callTokenId) {
        this.callTokenId = callTokenId;
        return this;
    }

    public ContractFunction build() {
        return new ContractFunction(this);
    }
  }

  public String toString() {
    StringBuilder ret = new StringBuilder("# function ");
    ret.append(name);
    ret.append("(");
    if (inputParams.size() != 0) {
      for (int i = 0; i < inputParams.size(); i++) {
        ret.append(inputParams.get(i) + " " + inputTypes.get(i) + ", ");
      }
      ret.delete(ret.length() - 2, ret.length() - 1);
    }
    ret.append(")");
    if (abi.getStateMutabilityValue() == 2) {
      ret.append(" view");
    }
    ret.append(" returns (");
    ret.append(outputType);
    ret.append(" " + output);
    ret.append(")");
    
    return ret.toString();
  }

}