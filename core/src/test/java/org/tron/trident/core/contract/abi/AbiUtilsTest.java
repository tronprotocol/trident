package org.tron.trident.core.contract.abi;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.proto.Common.SmartContract;
import org.tron.trident.proto.Common.SmartContract.ABI;
import org.tron.trident.proto.Common.SmartContract.ABI.Entry.EntryType;

class AbiUtilsTest {

  public static String abiJson = "["
      + "{"
      + "    \"anonymous\": false,"
      + "    \"inputs\": ["
      + "        {"
      + "            \"indexed\": true,"
      + "            \"internalType\": \"address\","
      + "            \"name\": \"employee\","
      + "            \"type\": \"address\""
      + "        },"
      + "        {"
      + "            \"indexed\": false,"
      + "            \"internalType\": \"string\","
      + "            \"name\": \"place\","
      + "            \"type\": \"string\""
      + "        },"
      + "        {"
      + "            \"indexed\": false,"
      + "            \"internalType\": \"uint256\","
      + "            \"name\": \"timestamp\","
      + "            \"type\": \"uint256\""
      + "        },"
      + "        {"
      + "            \"indexed\": false,"
      + "            \"internalType\": \"bool\","
      + "            \"name\": \"late\","
      + "            \"type\": \"bool\""
      + "        },"
      + "        {"
      + "            \"indexed\": false,"
      + "            \"internalType\": \"uint256\","
      + "            \"name\": \"fine\","
      + "            \"type\": \"uint256\""
      + "        }"
      + "    ],"
      + "    \"name\": \"EmployeeClockIn\","
      + "    \"type\": \"event\""
      + "},"
      + "{"
      + "    \"anonymous\": false,"
      + "    \"inputs\": ["
      + "        {"
      + "            \"indexed\": false,"
      + "            \"internalType\": \"uint256\","
      + "            \"name\": \"ind\","
      + "            \"type\": \"uint256\""
      + "        }"
      + "    ],"
      + "    \"name\": \"LogFallback\","
      + "    \"type\": \"event\""
      + "},"
      + "{"
      + "    \"stateMutability\": \"payable\","
      + "    \"type\": \"fallback\""
      + "},"
      + "{"
      + "    \"inputs\": ["
      + "        {"
      + "            \"internalType\": \"address\","
      + "            \"name\": \"employee\","
      + "            \"type\": \"address\""
      + "        },"
      + "        {"
      + "            \"internalType\": \"string\","
      + "            \"name\": \"employeePlace\","
      + "            \"type\": \"string\""
      + "        },"
      + "        {"
      + "            \"internalType\": \"uint256\","
      + "            \"name\": \"currentTime\","
      + "            \"type\": \"uint256\""
      + "        },"
      + "        {"
      + "            \"internalType\": \"bool\","
      + "            \"name\": \"isLate\","
      + "            \"type\": \"bool\""
      + "        },"
      + "        {"
      + "            \"internalType\": \"uint256\","
      + "            \"name\": \"fine\","
      + "            \"type\": \"uint256\""
      + "        }"
      + "    ],"
      + "    \"name\": \"clockOut\","
      + "    \"outputs\": ["
      + "        {"
      + "            \"internalType\": \"bool\","
      + "            \"name\": \"\","
      + "            \"type\": \"bool\""
      + "        }"
      + "    ],"
      + "    \"stateMutability\": \"nonpayable\","
      + "    \"type\": \"function\""
      + "}"
      + "]";

  @Test
  void testLoadContract() {
    ABI abi = AbiUtils.jsonStr2ABI(abiJson);
    assertEquals(4, abi.getEntrysCount());

    // Verify event
    ABI.Entry eventEntry = abi.getEntrys(0);
    assertEquals("EmployeeClockIn", eventEntry.getName());
    assertEquals(ABI.Entry.EntryType.Event, eventEntry.getType());
    assertEquals(5, eventEntry.getInputsCount());
    assertTrue(eventEntry.getInputs(0).getIndexed());
    assertEquals("address", eventEntry.getInputs(0).getType());

    // Verify event
    ABI.Entry eventEntry2 = abi.getEntrys(1);
    assertEquals("LogFallback", eventEntry2.getName());
    assertEquals(ABI.Entry.EntryType.Event, eventEntry2.getType());
    assertEquals(1, eventEntry2.getInputsCount());
    assertFalse(eventEntry2.getInputs(0).getIndexed());
    assertEquals("uint256", eventEntry2.getInputs(0).getType());

    //// Verify fallback
    ABI.Entry fallbackEntry = abi.getEntrys(2);
    assertEquals(ABI.Entry.StateMutabilityType.Payable, fallbackEntry.getStateMutability());
    assertEquals(EntryType.Fallback, fallbackEntry.getType());

    // Verify function
    ABI.Entry functionEntry = abi.getEntrys(3);
    assertEquals("clockOut", functionEntry.getName());
    assertEquals(ABI.Entry.EntryType.Function, functionEntry.getType());
    assertEquals(5, functionEntry.getInputsCount());
    assertEquals(1, functionEntry.getOutputsCount());
    assertEquals("bool", functionEntry.getOutputs(0).getType());
  }

  @Test
  void testLoadComplexContract() {
    String abiJson2 = "["
        + "{"
        + "    \"anonymous\": false,"
        + "    \"inputs\": ["
        + "        {"
        + "            \"indexed\": true,"
        + "            \"name\": \"from\","
        + "            \"type\": \"address\""
        + "        },"
        + "        {"
        + "            \"indexed\": true,"
        + "            \"name\": \"to\","
        + "            \"type\": \"address\""
        + "        },"
        + "        {"
        + "            \"indexed\": false,"
        + "            \"name\": \"value\","
        + "            \"type\": \"uint256\""
        + "        }"
        + "    ],"
        + "    \"name\": \"Transfer\","
        + "    \"type\": \"event\""
        + "},"
        + "{"
        + "    \"inputs\": ["
        + "        {"
        + "            \"name\": \"initialSupply\","
        + "            \"type\": \"uint256\""
        + "        }"
        + "    ],"
        + "    \"stateMutability\": \"nonpayable\","
        + "    \"type\": \"constructor\""
        + "},"
        + "{"
        + "    \"inputs\": ["
        + "        {"
        + "            \"name\": \"to\","
        + "            \"type\": \"address\""
        + "        },"
        + "        {"
        + "            \"name\": \"value\","
        + "            \"type\": \"uint256\""
        + "        }"
        + "    ],"
        + "    \"name\": \"transfer\","
        + "    \"outputs\": ["
        + "        {"
        + "            \"name\": \"\","
        + "            \"type\": \"bool\""
        + "        }"
        + "    ],"
        + "    \"stateMutability\": \"nonpayable\","
        + "    \"type\": \"function\""
        + "}"
        + "]";

    ABI.Builder builder = ABI.newBuilder();
    AbiUtils.loadAbiFromJson(abiJson2, builder);
    
    ABI abi = builder.build();
    assertEquals(3, abi.getEntrysCount());

    // Verify event
    ABI.Entry eventEntry = abi.getEntrys(0);
    assertEquals("Transfer", eventEntry.getName());
    assertEquals(ABI.Entry.EntryType.Event, eventEntry.getType());
    assertEquals(3, eventEntry.getInputsCount());
    assertTrue(eventEntry.getInputs(0).getIndexed());
    assertEquals("address", eventEntry.getInputs(0).getType());

    // Verify constructor
    ABI.Entry constructorEntry = abi.getEntrys(1);
    assertEquals(ABI.Entry.EntryType.Constructor, constructorEntry.getType());
    assertEquals(ABI.Entry.StateMutabilityType.Nonpayable, constructorEntry.getStateMutability());

    // Verify function
    ABI.Entry functionEntry = abi.getEntrys(2);
    assertEquals("transfer", functionEntry.getName());
    assertEquals(ABI.Entry.EntryType.Function, functionEntry.getType());
    assertEquals(2, functionEntry.getInputsCount());
    assertEquals(1, functionEntry.getOutputsCount());
    assertEquals("bool", functionEntry.getOutputs(0).getType());
  }

  @Test
  void testLoadEmptyOrInvalidAbi() {
    ABI.Builder builder = ABI.newBuilder();

    AbiUtils.loadAbiFromJson("", builder);
    assertEquals(0, builder.getEntrysCount());

    AbiUtils.loadAbiFromJson("[]", builder);
    assertEquals(0, builder.getEntrysCount());

    AbiUtils.loadAbiFromJson(null, builder);
    assertEquals(0, builder.getEntrysCount());

    String invalidJson = "[{\"type\":\"invalid\"}]";
    try {
      AbiUtils.loadAbiFromJson(invalidJson, builder);
      fail("invalid json");
    } catch (Exception e) {
      assertTrue(e.getMessage().getBytes().length > 0);
    }
  }

  @Test
  void testAllEntryTypes() {
    String abiJson2 = "["
        + "{"
        + "    \"type\": \"function\","
        + "    \"name\": \"myFunction\","
        + "    \"inputs\": ["
        + "        {"
        + "            \"name\": \"param1\","
        + "            \"type\": \"uint256\""
        + "        }"
        + "    ],"
        + "    \"outputs\": ["
        + "        {"
        + "            \"name\": \"result\","
        + "            \"type\": \"bool\""
        + "        }"
        + "    ],"
        + "    \"stateMutability\": \"pure\""
        + "},"
        + "{"
        + "    \"type\": \"event\","
        + "    \"name\": \"MyEvent\","
        + "    \"inputs\": ["
        + "        {"
        + "            \"indexed\": true,"
        + "            \"name\": \"sender\","
        + "            \"type\": \"address\""
        + "        }"
        + "    ],"
        + "    \"anonymous\": false"
        + "},"
        + "{"
        + "    \"type\": \"fallback\","
        + "    \"stateMutability\": \"payable\""
        + "},"
        + "{"
        + "    \"type\": \"receive\","
        + "    \"stateMutability\": \"payable\""
        + "},"
        + "{"
        + "    \"type\": \"error\","
        + "    \"name\": \"MyError\","
        + "    \"inputs\": ["
        + "        {"
        + "            \"name\": \"reason\","
        + "            \"type\": \"string\""
        + "        }"
        + "    ]"
        + "}"
        + "]";

    ABI.Builder builder = ABI.newBuilder();
    AbiUtils.loadAbiFromJson(abiJson2, builder);

    ABI abi = builder.build();
    assertEquals(5, abi.getEntrysCount());
    
    // Verify all entry types
    assertEquals(ABI.Entry.EntryType.Function, abi.getEntrys(0).getType());
    assertEquals(ABI.Entry.EntryType.Event, abi.getEntrys(1).getType());
    assertEquals(ABI.Entry.EntryType.Fallback, abi.getEntrys(2).getType());
    assertEquals(ABI.Entry.EntryType.Receive, abi.getEntrys(3).getType());
    assertEquals(ABI.Entry.EntryType.Error, abi.getEntrys(4).getType());

    // Verify specific properties
    assertEquals(ABI.Entry.StateMutabilityType.Pure, abi.getEntrys(0).getStateMutability());
    assertTrue(abi.getEntrys(1).getInputs(0).getIndexed());
    assertEquals(ABI.Entry.StateMutabilityType.Payable, abi.getEntrys(2).getStateMutability());
    assertEquals(ABI.Entry.StateMutabilityType.Payable, abi.getEntrys(3).getStateMutability());
    assertEquals("string", abi.getEntrys(4).getInputs(0).getType());
  }

  @Test
  void testLoadAbiWithEnums() {
    String abiJson2 = "{"
        + "\"entrys\": [{"
        + "    \"type\": \"function\","
        + "    \"name\": \"transfer\","
        + "    \"stateMutability\": \"payable\","
        + "    \"inputs\": [],"
        + "    \"constant\": false,"
        + "    \"payable\": true"
        + "}]}";

    ABI.Builder builder = ABI.newBuilder();
    AbiUtils.loadAbiFromJson(abiJson2, builder);

    ABI abi = builder.build();
    assertEquals(1, abi.getEntrysCount());

    ABI.Entry entry = abi.getEntrys(0);
    assertEquals(ABI.Entry.EntryType.Function, entry.getType());
    assertEquals(ABI.Entry.StateMutabilityType.Payable, entry.getStateMutability());
    assertFalse(entry.getConstant());
    assertTrue(entry.getPayable());
  }

  @Test
  void testLoadEntry() {

    ABI.Builder builder = ABI.newBuilder();
    AbiUtils.loadAbiFromJson(abiJson, builder);

    String abiStringWithEntrys = "{\"entrys\":" + abiJson + "}";
    ABI.Builder builder2 = ABI.newBuilder();
    AbiUtils.loadAbiFromJson(abiStringWithEntrys, builder2);

    assertEquals(builder.toString(), builder2.toString());
  }

  @Test
  void testAnonymousEvent() {
    String abiJson2 = "["
        + "{"
        + "    \"anonymous\": true,"
        + "    \"inputs\": ["
        + "        {"
        + "            \"indexed\": true,"
        + "            \"name\": \"from\","
        + "            \"type\": \"address\""
        + "        }"
        + "    ],"
        + "    \"name\": \"AnonymousEvent\","
        + "    \"type\": \"event\""
        + "}"
        + "]";

    ABI.Builder builder = ABI.newBuilder();
    AbiUtils.loadAbiFromJson(abiJson2, builder);
    
    ABI abi = builder.build();
    assertEquals(1, abi.getEntrysCount());
    
    ABI.Entry eventEntry = abi.getEntrys(0);
    assertEquals(ABI.Entry.EntryType.Event, eventEntry.getType());
    assertTrue(eventEntry.getAnonymous());
  }

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
      Assertions.assertTrue(true);
      assert true;
    } catch (Exception e) {
      Assertions.fail();
    }
  }
}
