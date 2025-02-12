package org.tron.trident.core;


import org.junit.jupiter.api.Test;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.proto.Common.SmartContract;

public class Contract3Test {

  @Test
  void testLoadAbiFromJson() {
    String abi = "{\"entrys\":[{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"internalType\""
        + ":\"address\",\"name\":\"employee\",\"type\":\"address\"},{\"indexed\":false,"
        + "\"internalType\":\"string\",\"name\":\"place\",\"type\":\"string\"},{\"indexed\":false,"
        + "\"internalType\":\"uint256\",\"name\":\"timestamp\",\"type\":\"uint256\"},{\"indexed\":"
        + "false,\"internalType\":\"bool\",\"name\":\"late\",\"type\":\"bool\"},{\"indexed\":false,"
        + "\"internalType\":\"uint256\",\"name\":\"fine\",\"type\":\"uint256\"}],\"name\":"
        + "\"EmployeeClockIn\",\"type\":\"event\"},{\"inputs\":[{\"internalType\":\"address\","
        + "\"name\":\"employee\",\"type\":\"address\"},{\"internalType\":\"string\",\"name\":"
        + "\"employeePlace\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":"
        + "\"currentTime\",\"type\":\"uint256\"},{\"internalType\":\"bool\",\"name\":\"isLate\","
        + "\"type\":\"bool\"}],\"name\":\"clockIn\",\"outputs\":[{\"internalType\":\"bool\",\"name"
        + "\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"payable\",\"type\":\"function\"},"
        + "{\"inputs\":[{\"internalType\":\"address\",\"name\":\"employee\",\"type\":\"address\"},"
        + "{\"internalType\":\"string\",\"name\":\"employeePlace\",\"type\":\"string\"},"
        + "{\"internalType\":\"uint256\",\"name\":\"currentTime\",\"type\":\"uint256\"},"
        + "{\"internalType\":\"bool\",\"name\":\"isLate\",\"type\":\"bool\"},{\"internalType\":"
        + "\"uint256\",\"name\":\"fine\",\"type\":\"uint256\"}],\"name\":\"clockOut\",\"outputs\":"
        + "[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":"
        + "\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"getBlockChainId\","
        + "\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],"
        + "\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":"
        + "\"uint256\",\"name\":\"x\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\""
        + ":\"y\",\"type\":\"uint256\"}],\"name\":\"getMax\",\"outputs\":[{\"internalType\":"
        + "\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"pure\",\"type\":"
        + "\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"seed\",\"type\":"
        + "\"uint256\"}],\"name\":\"getRandom\",\"outputs\":[{\"internalType\":\"uint256\","
        + "\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"}"
        + ",{\"inputs\":[],\"name\":\"payMeTRX\",\"outputs\":[{\"internalType\":\"uint256\","
        + "\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"payable\",\"type\":"
        + "\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"n\",\"type\":"
        + "\"uint256\"}],\"name\":\"writeNumber\",\"outputs\":[{\"internalType\":\"uint256\","
        + "\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"nonpayable\",\"type\":"
        + "\"function\"}]}";
    SmartContract.ABI.Builder abiBuilder = SmartContract.ABI.newBuilder();
    try {
      Contract.loadAbiFromJson(abi, abiBuilder);
    } catch (Exception e) {
      assert false;
    }
  }
}
