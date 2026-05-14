package org.tron.trident.core;

import org.junit.jupiter.api.*;
import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.FunctionReturnDecoder;
import org.tron.trident.abi.TypeEncoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.Utils;
import org.tron.trident.abi.datatypes.*;
import org.tron.trident.abi.datatypes.generated.StaticArray2;
import org.tron.trident.abi.datatypes.generated.StaticArray3;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.abi.datatypes.reflection.Parameterized;
import org.tron.trident.core.utils.ByteArray;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Response;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.utils.Base58Check;
import org.tron.trident.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * End-to-end test for ABIv2 and encodePacked features.
 * This test deploys the AbiV2TestContract and interacts with it to verify
 * complex type encoding/decoding and packed encoding.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AbiV2ContractTest {
  // 1. Compile AbiV2TestContract.sol to get its ABI and Bytecode.
  // 2. Provide a private key for an account on the Nile testnet with some TRX.
  private static final String CONTRACT_ABI = "[\n"
      + "\t{\n"
      + "\t\t\"inputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"uint256\",\n"
      + "\t\t\t\t\t\t\"name\": \"topLevelId\",\n"
      + "\t\t\t\t\t\t\"type\": \"uint256\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\t\t\"internalType\": \"uint256\",\n"
      + "\t\t\t\t\t\t\t\t\"name\": \"staticId\",\n"
      + "\t\t\t\t\t\t\t\t\"type\": \"uint256\"\n"
      + "\t\t\t\t\t\t\t},\n"
      + "\t\t\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\t\t\"internalType\": \"address\",\n"
      + "\t\t\t\t\t\t\t\t\"name\": \"staticAddress\",\n"
      + "\t\t\t\t\t\t\t\t\"type\": \"address\"\n"
      + "\t\t\t\t\t\t\t}\n"
      + "\t\t\t\t\t\t],\n"
      + "\t\t\t\t\t\t\"internalType\": \"struct AbiV2TestContract.StaticInfo\",\n"
      + "\t\t\t\t\t\t\"name\": \"staticPart\",\n"
      + "\t\t\t\t\t\t\"type\": \"tuple\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\t\t\"internalType\": \"string\",\n"
      + "\t\t\t\t\t\t\t\t\"name\": \"name\",\n"
      + "\t\t\t\t\t\t\t\t\"type\": \"string\"\n"
      + "\t\t\t\t\t\t\t},\n"
      + "\t\t\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\t\t\"internalType\": \"bytes\",\n"
      + "\t\t\t\t\t\t\t\t\"name\": \"data\",\n"
      + "\t\t\t\t\t\t\t\t\"type\": \"bytes\"\n"
      + "\t\t\t\t\t\t\t},\n"
      + "\t\t\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\t\t\"internalType\": \"uint256[]\",\n"
      + "\t\t\t\t\t\t\t\t\"name\": \"scores\",\n"
      + "\t\t\t\t\t\t\t\t\"type\": \"uint256[]\"\n"
      + "\t\t\t\t\t\t\t}\n"
      + "\t\t\t\t\t\t],\n"
      + "\t\t\t\t\t\t\"internalType\": \"struct AbiV2TestContract.DynamicInfo\",\n"
      + "\t\t\t\t\t\t\"name\": \"dynamicPart\",\n"
      + "\t\t\t\t\t\t\"type\": \"tuple\"\n"
      + "\t\t\t\t\t}\n"
      + "\t\t\t\t],\n"
      + "\t\t\t\t\"internalType\": \"struct AbiV2TestContract.NestedInfo\",\n"
      + "\t\t\t\t\"name\": \"_info\",\n"
      + "\t\t\t\t\"type\": \"tuple\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"name\": \"setAndGetNestedStruct\",\n"
      + "\t\t\"outputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"uint256\",\n"
      + "\t\t\t\t\t\t\"name\": \"topLevelId\",\n"
      + "\t\t\t\t\t\t\"type\": \"uint256\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\t\t\"internalType\": \"uint256\",\n"
      + "\t\t\t\t\t\t\t\t\"name\": \"staticId\",\n"
      + "\t\t\t\t\t\t\t\t\"type\": \"uint256\"\n"
      + "\t\t\t\t\t\t\t},\n"
      + "\t\t\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\t\t\"internalType\": \"address\",\n"
      + "\t\t\t\t\t\t\t\t\"name\": \"staticAddress\",\n"
      + "\t\t\t\t\t\t\t\t\"type\": \"address\"\n"
      + "\t\t\t\t\t\t\t}\n"
      + "\t\t\t\t\t\t],\n"
      + "\t\t\t\t\t\t\"internalType\": \"struct AbiV2TestContract.StaticInfo\",\n"
      + "\t\t\t\t\t\t\"name\": \"staticPart\",\n"
      + "\t\t\t\t\t\t\"type\": \"tuple\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\t\t\"internalType\": \"string\",\n"
      + "\t\t\t\t\t\t\t\t\"name\": \"name\",\n"
      + "\t\t\t\t\t\t\t\t\"type\": \"string\"\n"
      + "\t\t\t\t\t\t\t},\n"
      + "\t\t\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\t\t\"internalType\": \"bytes\",\n"
      + "\t\t\t\t\t\t\t\t\"name\": \"data\",\n"
      + "\t\t\t\t\t\t\t\t\"type\": \"bytes\"\n"
      + "\t\t\t\t\t\t\t},\n"
      + "\t\t\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\t\t\"internalType\": \"uint256[]\",\n"
      + "\t\t\t\t\t\t\t\t\"name\": \"scores\",\n"
      + "\t\t\t\t\t\t\t\t\"type\": \"uint256[]\"\n"
      + "\t\t\t\t\t\t\t}\n"
      + "\t\t\t\t\t\t],\n"
      + "\t\t\t\t\t\t\"internalType\": \"struct AbiV2TestContract.DynamicInfo\",\n"
      + "\t\t\t\t\t\t\"name\": \"dynamicPart\",\n"
      + "\t\t\t\t\t\t\"type\": \"tuple\"\n"
      + "\t\t\t\t\t}\n"
      + "\t\t\t\t],\n"
      + "\t\t\t\t\"internalType\": \"struct AbiV2TestContract.NestedInfo\",\n"
      + "\t\t\t\t\"name\": \"\",\n"
      + "\t\t\t\t\"type\": \"tuple\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"stateMutability\": \"nonpayable\",\n"
      + "\t\t\"type\": \"function\"\n"
      + "\t},\n"
      + "\t{\n"
      + "\t\t\"inputs\": [],\n"
      + "\t\t\"name\": \"lastNestedInfo\",\n"
      + "\t\t\"outputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"internalType\": \"uint256\",\n"
      + "\t\t\t\t\"name\": \"topLevelId\",\n"
      + "\t\t\t\t\"type\": \"uint256\"\n"
      + "\t\t\t},\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"uint256\",\n"
      + "\t\t\t\t\t\t\"name\": \"staticId\",\n"
      + "\t\t\t\t\t\t\"type\": \"uint256\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"address\",\n"
      + "\t\t\t\t\t\t\"name\": \"staticAddress\",\n"
      + "\t\t\t\t\t\t\"type\": \"address\"\n"
      + "\t\t\t\t\t}\n"
      + "\t\t\t\t],\n"
      + "\t\t\t\t\"internalType\": \"struct AbiV2TestContract.StaticInfo\",\n"
      + "\t\t\t\t\"name\": \"staticPart\",\n"
      + "\t\t\t\t\"type\": \"tuple\"\n"
      + "\t\t\t},\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"string\",\n"
      + "\t\t\t\t\t\t\"name\": \"name\",\n"
      + "\t\t\t\t\t\t\"type\": \"string\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"bytes\",\n"
      + "\t\t\t\t\t\t\"name\": \"data\",\n"
      + "\t\t\t\t\t\t\"type\": \"bytes\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"uint256[]\",\n"
      + "\t\t\t\t\t\t\"name\": \"scores\",\n"
      + "\t\t\t\t\t\t\"type\": \"uint256[]\"\n"
      + "\t\t\t\t\t}\n"
      + "\t\t\t\t],\n"
      + "\t\t\t\t\"internalType\": \"struct AbiV2TestContract.DynamicInfo\",\n"
      + "\t\t\t\t\"name\": \"dynamicPart\",\n"
      + "\t\t\t\t\"type\": \"tuple\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"stateMutability\": \"view\",\n"
      + "\t\t\"type\": \"function\"\n"
      + "\t},\n"
      + "\t{\n"
      + "\t\t\"inputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"string\",\n"
      + "\t\t\t\t\t\t\"name\": \"name\",\n"
      + "\t\t\t\t\t\t\"type\": \"string\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"bytes\",\n"
      + "\t\t\t\t\t\t\"name\": \"data\",\n"
      + "\t\t\t\t\t\t\"type\": \"bytes\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"uint256[]\",\n"
      + "\t\t\t\t\t\t\"name\": \"scores\",\n"
      + "\t\t\t\t\t\t\"type\": \"uint256[]\"\n"
      + "\t\t\t\t\t}\n"
      + "\t\t\t\t],\n"
      + "\t\t\t\t\"internalType\": \"struct AbiV2TestContract.DynamicInfo[]\",\n"
      + "\t\t\t\t\"name\": \"_infos\",\n"
      + "\t\t\t\t\"type\": \"tuple[]\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"name\": \"setAndGetDynamicArrayOfStructs\",\n"
      + "\t\t\"outputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"string\",\n"
      + "\t\t\t\t\t\t\"name\": \"name\",\n"
      + "\t\t\t\t\t\t\"type\": \"string\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"bytes\",\n"
      + "\t\t\t\t\t\t\"name\": \"data\",\n"
      + "\t\t\t\t\t\t\"type\": \"bytes\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"uint256[]\",\n"
      + "\t\t\t\t\t\t\"name\": \"scores\",\n"
      + "\t\t\t\t\t\t\"type\": \"uint256[]\"\n"
      + "\t\t\t\t\t}\n"
      + "\t\t\t\t],\n"
      + "\t\t\t\t\"internalType\": \"struct AbiV2TestContract.DynamicInfo[]\",\n"
      + "\t\t\t\t\"name\": \"\",\n"
      + "\t\t\t\t\"type\": \"tuple[]\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"stateMutability\": \"pure\",\n"
      + "\t\t\"type\": \"function\"\n"
      + "\t},\n"
      + "\t{\n"
      + "\t\t\"inputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"string\",\n"
      + "\t\t\t\t\t\t\"name\": \"name\",\n"
      + "\t\t\t\t\t\t\"type\": \"string\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"bytes\",\n"
      + "\t\t\t\t\t\t\"name\": \"data\",\n"
      + "\t\t\t\t\t\t\"type\": \"bytes\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"uint256[]\",\n"
      + "\t\t\t\t\t\t\"name\": \"scores\",\n"
      + "\t\t\t\t\t\t\"type\": \"uint256[]\"\n"
      + "\t\t\t\t\t}\n"
      + "\t\t\t\t],\n"
      + "\t\t\t\t\"internalType\": \"struct AbiV2TestContract.DynamicInfo\",\n"
      + "\t\t\t\t\"name\": \"_info\",\n"
      + "\t\t\t\t\"type\": \"tuple\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"name\": \"setAndGetDynamicStruct\",\n"
      + "\t\t\"outputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"string\",\n"
      + "\t\t\t\t\t\t\"name\": \"name\",\n"
      + "\t\t\t\t\t\t\"type\": \"string\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"bytes\",\n"
      + "\t\t\t\t\t\t\"name\": \"data\",\n"
      + "\t\t\t\t\t\t\"type\": \"bytes\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"uint256[]\",\n"
      + "\t\t\t\t\t\t\"name\": \"scores\",\n"
      + "\t\t\t\t\t\t\"type\": \"uint256[]\"\n"
      + "\t\t\t\t\t}\n"
      + "\t\t\t\t],\n"
      + "\t\t\t\t\"internalType\": \"struct AbiV2TestContract.DynamicInfo\",\n"
      + "\t\t\t\t\"name\": \"\",\n"
      + "\t\t\t\t\"type\": \"tuple\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"stateMutability\": \"pure\",\n"
      + "\t\t\"type\": \"function\"\n"
      + "\t},\n"
      + "\t{\n"
      + "\t\t\"inputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"internalType\": \"uint256[2][3]\",\n"
      + "\t\t\t\t\"name\": \"_matrix\",\n"
      + "\t\t\t\t\"type\": \"uint256[2][3]\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"name\": \"setAndGetMultiDimArray\",\n"
      + "\t\t\"outputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"internalType\": \"uint256[2][3]\",\n"
      + "\t\t\t\t\"name\": \"\",\n"
      + "\t\t\t\t\"type\": \"uint256[2][3]\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"stateMutability\": \"pure\",\n"
      + "\t\t\"type\": \"function\"\n"
      + "\t},\n"
      + "\t{\n"
      + "\t\t\"inputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"uint256\",\n"
      + "\t\t\t\t\t\t\"name\": \"staticId\",\n"
      + "\t\t\t\t\t\t\"type\": \"uint256\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"address\",\n"
      + "\t\t\t\t\t\t\"name\": \"staticAddress\",\n"
      + "\t\t\t\t\t\t\"type\": \"address\"\n"
      + "\t\t\t\t\t}\n"
      + "\t\t\t\t],\n"
      + "\t\t\t\t\"internalType\": \"struct AbiV2TestContract.StaticInfo[2]\",\n"
      + "\t\t\t\t\"name\": \"_infos\",\n"
      + "\t\t\t\t\"type\": \"tuple[2]\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"name\": \"setAndGetStaticArrayOfStructs\",\n"
      + "\t\t\"outputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"uint256\",\n"
      + "\t\t\t\t\t\t\"name\": \"staticId\",\n"
      + "\t\t\t\t\t\t\"type\": \"uint256\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"address\",\n"
      + "\t\t\t\t\t\t\"name\": \"staticAddress\",\n"
      + "\t\t\t\t\t\t\"type\": \"address\"\n"
      + "\t\t\t\t\t}\n"
      + "\t\t\t\t],\n"
      + "\t\t\t\t\"internalType\": \"struct AbiV2TestContract.StaticInfo[2]\",\n"
      + "\t\t\t\t\"name\": \"\",\n"
      + "\t\t\t\t\"type\": \"tuple[2]\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"stateMutability\": \"pure\",\n"
      + "\t\t\"type\": \"function\"\n"
      + "\t},\n"
      + "\t{\n"
      + "\t\t\"inputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"uint256\",\n"
      + "\t\t\t\t\t\t\"name\": \"staticId\",\n"
      + "\t\t\t\t\t\t\"type\": \"uint256\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"address\",\n"
      + "\t\t\t\t\t\t\"name\": \"staticAddress\",\n"
      + "\t\t\t\t\t\t\"type\": \"address\"\n"
      + "\t\t\t\t\t}\n"
      + "\t\t\t\t],\n"
      + "\t\t\t\t\"internalType\": \"struct AbiV2TestContract.StaticInfo\",\n"
      + "\t\t\t\t\"name\": \"_info\",\n"
      + "\t\t\t\t\"type\": \"tuple\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"name\": \"setAndGetStaticStruct\",\n"
      + "\t\t\"outputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"components\": [\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"uint256\",\n"
      + "\t\t\t\t\t\t\"name\": \"staticId\",\n"
      + "\t\t\t\t\t\t\"type\": \"uint256\"\n"
      + "\t\t\t\t\t},\n"
      + "\t\t\t\t\t{\n"
      + "\t\t\t\t\t\t\"internalType\": \"address\",\n"
      + "\t\t\t\t\t\t\"name\": \"staticAddress\",\n"
      + "\t\t\t\t\t\t\"type\": \"address\"\n"
      + "\t\t\t\t\t}\n"
      + "\t\t\t\t],\n"
      + "\t\t\t\t\"internalType\": \"struct AbiV2TestContract.StaticInfo\",\n"
      + "\t\t\t\t\"name\": \"\",\n"
      + "\t\t\t\t\"type\": \"tuple\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"stateMutability\": \"pure\",\n"
      + "\t\t\"type\": \"function\"\n"
      + "\t},\n"
      + "\t{\n"
      + "\t\t\"inputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"internalType\": \"bytes\",\n"
      + "\t\t\t\t\"name\": \"javaPacked\",\n"
      + "\t\t\t\t\"type\": \"bytes\"\n"
      + "\t\t\t},\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"internalType\": \"uint256\",\n"
      + "\t\t\t\t\"name\": \"id\",\n"
      + "\t\t\t\t\"type\": \"uint256\"\n"
      + "\t\t\t},\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"internalType\": \"address\",\n"
      + "\t\t\t\t\"name\": \"addr\",\n"
      + "\t\t\t\t\"type\": \"address\"\n"
      + "\t\t\t},\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"internalType\": \"string\",\n"
      + "\t\t\t\t\"name\": \"name\",\n"
      + "\t\t\t\t\"type\": \"string\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"name\": \"verifyPackedEncoding\",\n"
      + "\t\t\"outputs\": [\n"
      + "\t\t\t{\n"
      + "\t\t\t\t\"internalType\": \"bool\",\n"
      + "\t\t\t\t\"name\": \"\",\n"
      + "\t\t\t\t\"type\": \"bool\"\n"
      + "\t\t\t}\n"
      + "\t\t],\n"
      + "\t\t\"stateMutability\": \"pure\",\n"
      + "\t\t\"type\": \"function\"\n"
      + "\t}\n"
      + "]";
  private static final String CONTRACT_BYTECODE = "6080604052348015600e575f5ffd5b50611cbc8061001c5f395ff3fe608060405234801561000f575f5ffd5b5060043610610086575f3560e01c806373b8a93d1161005957806373b8a93d1461013a5780639007edcf1461016a578063b73b16011461019a578063f683b31e146101ca57610086565b806328c60ba01461008a57806330a4842d146100ba5780634fb4bedd146100ea5780635f2f84921461010a575b5f5ffd5b6100a4600480360381019061009f91906109dc565b6101fa565b6040516100b19190610be7565b60405180910390f35b6100d460048036038101906100cf9190610d5c565b61020a565b6040516100e19190610e59565b60405180910390f35b6100f261021a565b60405161010193929190610eae565b60405180910390f35b610124600480360381019061011f9190610eea565b610417565b6040516101319190610f15565b60405180910390f35b610154600480360381019061014f919061108a565b610427565b60405161016191906111ca565b60405180910390f35b610184600480360381019061017f9190611260565b610437565b604051610191919061134f565b60405180910390f35b6101b460048036038101906101af919061144d565b6104f8565b6040516101c1919061154f565b60405180910390f35b6101e460048036038101906101df919061156f565b610502565b6040516101f19190611625565b60405180910390f35b610202610546565b819050919050565b610212610567565b819050919050565b5f805f015490806001016040518060400160405290815f8201548152602001600182015f9054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152505090806003016040518060600160405290815f820180546102ac9061166b565b80601f01602080910402602001604051908101604052809291908181526020018280546102d89061166b565b80156103235780601f106102fa57610100808354040283529160200191610323565b820191905f5260205f20905b81548152906001019060200180831161030657829003601f168201915b5050505050815260200160018201805461033c9061166b565b80601f01602080910402602001604051908101604052809291908181526020018280546103689061166b565b80156103b35780601f1061038a576101008083540402835291602001916103b3565b820191905f5260205f20905b81548152906001019060200180831161039657829003601f168201915b505050505081526020016002820180548060200260200160405190810160405280929190818152602001828054801561040957602002820191905f5260205f20905b8154815260200190600101908083116103f5575b505050505081525050905083565b61041f610594565b819050919050565b61042f6105c2565b819050919050565b61043f6105ef565b815f5f820151815f01556020820151816001015f820151815f01556020820151816001015f6101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050506040820151816003015f820151815f0190816104be919061184c565b5060208201518160010190816104d49190611980565b5060408201518160020190816104ea9190611b2b565b505050905050819050919050565b6060819050919050565b5f5f84848460405160200161051993929190611c4e565b60405160208183030381529060405290508080519060200120868051906020012014915050949350505050565b60405180606001604052806060815260200160608152602001606081525090565b60405180604001604052806002905b61057e610594565b8152602001906001900390816105765790505090565b60405180604001604052805f81526020015f73ffffffffffffffffffffffffffffffffffffffff1681525090565b60405180606001604052806003905b6105d961061b565b8152602001906001900390816105d15790505090565b60405180606001604052805f8152602001610608610594565b8152602001610615610546565b81525090565b6040518060400160405280600290602082028036833780820191505090505090565b5f604051905090565b5f5ffd5b5f5ffd5b5f5ffd5b5f601f19601f8301169050919050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52604160045260245ffd5b61069882610652565b810181811067ffffffffffffffff821117156106b7576106b6610662565b5b80604052505050565b5f6106c961063d565b90506106d5828261068f565b919050565b5f5ffd5b5f5ffd5b5f5ffd5b5f67ffffffffffffffff821115610700576106ff610662565b5b61070982610652565b9050602081019050919050565b828183375f83830152505050565b5f610736610731846106e6565b6106c0565b905082815260208101848484011115610752576107516106e2565b5b61075d848285610716565b509392505050565b5f82601f830112610779576107786106de565b5b8135610789848260208601610724565b91505092915050565b5f67ffffffffffffffff8211156107ac576107ab610662565b5b6107b582610652565b9050602081019050919050565b5f6107d46107cf84610792565b6106c0565b9050828152602081018484840111156107f0576107ef6106e2565b5b6107fb848285610716565b509392505050565b5f82601f830112610817576108166106de565b5b81356108278482602086016107c2565b91505092915050565b5f67ffffffffffffffff82111561084a57610849610662565b5b602082029050602081019050919050565b5f5ffd5b5f819050919050565b6108718161085f565b811461087b575f5ffd5b50565b5f8135905061088c81610868565b92915050565b5f6108a461089f84610830565b6106c0565b905080838252602082019050602084028301858111156108c7576108c661085b565b5b835b818110156108f057806108dc888261087e565b8452602084019350506020810190506108c9565b5050509392505050565b5f82601f83011261090e5761090d6106de565b5b813561091e848260208601610892565b91505092915050565b5f6060828403121561093c5761093b61064e565b5b61094660606106c0565b90505f82013567ffffffffffffffff811115610965576109646106da565b5b61097184828501610765565b5f83015250602082013567ffffffffffffffff811115610994576109936106da565b5b6109a084828501610803565b602083015250604082013567ffffffffffffffff8111156109c4576109c36106da565b5b6109d0848285016108fa565b60408301525092915050565b5f602082840312156109f1576109f0610646565b5b5f82013567ffffffffffffffff811115610a0e57610a0d61064a565b5b610a1a84828501610927565b91505092915050565b5f81519050919050565b5f82825260208201905092915050565b8281835e5f83830152505050565b5f610a5582610a23565b610a5f8185610a2d565b9350610a6f818560208601610a3d565b610a7881610652565b840191505092915050565b5f81519050919050565b5f82825260208201905092915050565b5f610aa782610a83565b610ab18185610a8d565b9350610ac1818560208601610a3d565b610aca81610652565b840191505092915050565b5f81519050919050565b5f82825260208201905092915050565b5f819050602082019050919050565b610b078161085f565b82525050565b5f610b188383610afe565b60208301905092915050565b5f602082019050919050565b5f610b3a82610ad5565b610b448185610adf565b9350610b4f83610aef565b805f5b83811015610b7f578151610b668882610b0d565b9750610b7183610b24565b925050600181019050610b52565b5085935050505092915050565b5f606083015f8301518482035f860152610ba68282610a4b565b91505060208301518482036020860152610bc08282610a9d565b91505060408301518482036040860152610bda8282610b30565b9150508091505092915050565b5f6020820190508181035f830152610bff8184610b8c565b905092915050565b5f67ffffffffffffffff821115610c2157610c20610662565b5b602082029050919050565b5f73ffffffffffffffffffffffffffffffffffffffff82169050919050565b5f610c5582610c2c565b9050919050565b610c6581610c4b565b8114610c6f575f5ffd5b50565b5f81359050610c8081610c5c565b92915050565b5f60408284031215610c9b57610c9a61064e565b5b610ca560406106c0565b90505f610cb48482850161087e565b5f830152506020610cc784828501610c72565b60208301525092915050565b5f610ce5610ce084610c07565b6106c0565b90508060408402830185811115610cff57610cfe61085b565b5b835b81811015610d285780610d148882610c86565b845260208401935050604081019050610d01565b5050509392505050565b5f82601f830112610d4657610d456106de565b5b6002610d53848285610cd3565b91505092915050565b5f60808284031215610d7157610d70610646565b5b5f610d7e84828501610d32565b91505092915050565b5f60029050919050565b5f81905092915050565b5f819050919050565b610dad81610c4b565b82525050565b604082015f820151610dc75f850182610afe565b506020820151610dda6020850182610da4565b50505050565b5f610deb8383610db3565b60408301905092915050565b5f602082019050919050565b610e0c81610d87565b610e168184610d91565b9250610e2182610d9b565b805f5b83811015610e51578151610e388782610de0565b9650610e4383610df7565b925050600181019050610e24565b505050505050565b5f608082019050610e6c5f830184610e03565b92915050565b610e7b8161085f565b82525050565b604082015f820151610e955f850182610afe565b506020820151610ea86020850182610da4565b50505050565b5f608082019050610ec15f830186610e72565b610ece6020830185610e81565b8181036060830152610ee08184610b8c565b9050949350505050565b5f60408284031215610eff57610efe610646565b5b5f610f0c84828501610c86565b91505092915050565b5f604082019050610f285f830184610e81565b92915050565b5f67ffffffffffffffff821115610f4857610f47610662565b5b602082029050919050565b5f67ffffffffffffffff821115610f6d57610f6c610662565b5b602082029050919050565b5f610f8a610f8584610f53565b6106c0565b90508060208402830185811115610fa457610fa361085b565b5b835b81811015610fcd5780610fb9888261087e565b845260208401935050602081019050610fa6565b5050509392505050565b5f82601f830112610feb57610fea6106de565b5b6002610ff8848285610f78565b91505092915050565b5f61101361100e84610f2e565b6106c0565b9050806040840283018581111561102d5761102c61085b565b5b835b8181101561105657806110428882610fd7565b84526020840193505060408101905061102f565b5050509392505050565b5f82601f830112611074576110736106de565b5b6003611081848285611001565b91505092915050565b5f60c0828403121561109f5761109e610646565b5b5f6110ac84828501611060565b91505092915050565b5f60039050919050565b5f81905092915050565b5f819050919050565b5f60029050919050565b5f81905092915050565b5f819050919050565b5f602082019050919050565b611104816110d2565b61110e81846110dc565b9250611119826110e6565b805f5b838110156111495781516111308782610b0d565b965061113b836110ef565b92505060018101905061111c565b505050505050565b5f61115c83836110fb565b60408301905092915050565b5f602082019050919050565b61117d816110b5565b61118781846110bf565b9250611192826110c9565b805f5b838110156111c25781516111a98782611151565b96506111b483611168565b925050600181019050611195565b505050505050565b5f60c0820190506111dd5f830184611174565b92915050565b5f608082840312156111f8576111f761064e565b5b61120260606106c0565b90505f6112118482850161087e565b5f83015250602061122484828501610c86565b602083015250606082013567ffffffffffffffff811115611248576112476106da565b5b61125484828501610927565b60408301525092915050565b5f6020828403121561127557611274610646565b5b5f82013567ffffffffffffffff8111156112925761129161064a565b5b61129e848285016111e3565b91505092915050565b5f606083015f8301518482035f8601526112c18282610a4b565b915050602083015184820360208601526112db8282610a9d565b915050604083015184820360408601526112f58282610b30565b9150508091505092915050565b5f608083015f8301516113175f860182610afe565b50602083015161132a6020860182610db3565b506040830151848203606086015261134282826112a7565b9150508091505092915050565b5f6020820190508181035f8301526113678184611302565b905092915050565b5f67ffffffffffffffff82111561138957611388610662565b5b602082029050602081019050919050565b5f6113ac6113a78461136f565b6106c0565b905080838252602082019050602084028301858111156113cf576113ce61085b565b5b835b8181101561141657803567ffffffffffffffff8111156113f4576113f36106de565b5b8086016114018982610927565b855260208501945050506020810190506113d1565b5050509392505050565b5f82601f830112611434576114336106de565b5b813561144484826020860161139a565b91505092915050565b5f6020828403121561146257611461610646565b5b5f82013567ffffffffffffffff81111561147f5761147e61064a565b5b61148b84828501611420565b91505092915050565b5f81519050919050565b5f82825260208201905092915050565b5f819050602082019050919050565b5f6114c883836112a7565b905092915050565b5f602082019050919050565b5f6114e682611494565b6114f0818561149e565b935083602082028501611502856114ae565b805f5b8581101561153d578484038952815161151e85826114bd565b9450611529836114d0565b925060208a01995050600181019050611505565b50829750879550505050505092915050565b5f6020820190508181035f83015261156781846114dc565b905092915050565b5f5f5f5f6080858703121561158757611586610646565b5b5f85013567ffffffffffffffff8111156115a4576115a361064a565b5b6115b087828801610803565b94505060206115c18782880161087e565b93505060406115d287828801610c72565b925050606085013567ffffffffffffffff8111156115f3576115f261064a565b5b6115ff87828801610765565b91505092959194509250565b5f8115159050919050565b61161f8161160b565b82525050565b5f6020820190506116385f830184611616565b92915050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52602260045260245ffd5b5f600282049050600182168061168257607f821691505b6020821081036116955761169461163e565b5b50919050565b5f819050815f5260205f209050919050565b5f6020601f8301049050919050565b5f82821b905092915050565b5f600883026116f77fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff826116bc565b61170186836116bc565b95508019841693508086168417925050509392505050565b5f819050919050565b5f61173c6117376117328461085f565b611719565b61085f565b9050919050565b5f819050919050565b61175583611722565b61176961176182611743565b8484546116c8565b825550505050565b5f5f905090565b611780611771565b61178b81848461174c565b505050565b5f5b828110156117b1576117a65f828401611778565b600181019050611792565b505050565b601f8211156118045782821115611803576117d08161169b565b6117d9836116ad565b6117e2856116ad565b60208610156117ef575f90505b8083016117fe82840382611790565b505050505b5b505050565b5f82821c905092915050565b5f6118245f1984600802611809565b1980831691505092915050565b5f61183c8383611815565b9150826002028217905092915050565b61185582610a23565b67ffffffffffffffff81111561186e5761186d610662565b5b611878825461166b565b6118838282856117b6565b5f60209050601f8311600181146118b4575f84156118a2578287015190505b6118ac8582611831565b865550611913565b601f1984166118c28661169b565b5f5b828110156118e9578489015182556001820191506020850194506020810190506118c4565b868310156119065784890151611902601f891682611815565b8355505b6001600288020188555050505b505050505050565b5f819050815f5260205f209050919050565b601f82111561197b578282111561197a576119478161191b565b611950836116ad565b611959856116ad565b6020861015611966575f90505b80830161197582840382611790565b505050505b5b505050565b61198982610a83565b67ffffffffffffffff8111156119a2576119a1610662565b5b6119ac825461166b565b6119b782828561192d565b5f60209050601f8311600181146119e8575f84156119d6578287015190505b6119e08582611831565b865550611a47565b601f1984166119f68661191b565b5f5b82811015611a1d578489015182556001820191506020850194506020810190506119f8565b86831015611a3a5784890151611a36601f891682611815565b8355505b6001600288020188555050505b505050505050565b5f81549050919050565b5f8190506001806001038301049050919050565b5f819050815f5260205f209050919050565b5f5b82811015611aa057611a955f828401611778565b600181019050611a81565b505050565b81831015611adc57611ab682611a59565b611abf84611a59565b611ac883611a6d565b818101611ad783850382611a7f565b505050505b505050565b68010000000000000000821115611afb57611afa610662565b5b611b0481611a4f565b828255611b12838284611aa5565b505050565b5f611b22825161085f565b80915050919050565b611b3482610ad5565b67ffffffffffffffff811115611b4d57611b4c610662565b5b611b578183611ae1565b611b6083610aef565b611b6983611a6d565b600183045f5b81811015611ba6575f611b8185611b17565b611b8a81611743565b8092506020870196505050808285015550600181019050611b6f565b50505050505050565b5f819050919050565b611bc9611bc48261085f565b611baf565b82525050565b5f8160601b9050919050565b5f611be582611bcf565b9050919050565b5f611bf682611bdb565b9050919050565b611c0e611c0982610c4b565b611bec565b82525050565b5f81905092915050565b5f611c2882610a23565b611c328185611c14565b9350611c42818560208601610a3d565b80840191505092915050565b5f611c598286611bb8565b602082019150611c698285611bfd565b601482019150611c798284611c1e565b915081905094935050505056fea2646970667358221220d4662cb1ae03e587216abbcef7d1fd771291a1d23ffdb75a265b7fedc2c78daf64736f6c63430008210033";
  //also deployed https://sepolia.arbiscan.io/address/0x51f2799edab0fb17c51bb1878f67d325220c858b#readContract
  private String contractAddress = "TVvQP8CM7dkFtjtiomxRVMyrcr4kzsLcsj";

  // ================= STRUCT DEFINITIONS (mirroring Solidity) =================

  public static class StaticInfo extends StaticStruct {
    public Uint256 staticId;
    public Address staticAddress;

    public StaticInfo(Uint256 staticId, Address staticAddress) {
      super(staticId, staticAddress);
      this.staticId = staticId;
      this.staticAddress = staticAddress;
    }
    public StaticInfo(List<Type> values) {
      super(values);
      this.staticId = (Uint256) values.get(0);
      this.staticAddress = (Address) values.get(1);
    }
    public StaticInfo(Type staticId, Type staticAddress) {
      this(
          (Uint256) staticId,
          (Address) staticAddress
      );
    }
  }

  public static class DynamicInfo extends DynamicStruct {
    public Utf8String name;
    public DynamicBytes data;
    public DynamicArray<Uint256> scores;

    public DynamicInfo(Utf8String name, DynamicBytes data,
        @Parameterized(type = Uint256.class) DynamicArray<Uint256> scores) {
      super(name, data, scores);
      this.name = name;
      this.data = data;
      this.scores = scores;
    }
    public DynamicInfo(List<Type> values) {
      super(values);
      this.name = (Utf8String) values.get(0);
      this.data = (DynamicBytes) values.get(1);
      this.scores = (DynamicArray<Uint256>) values.get(2);
    }
    public DynamicInfo(Type name, Type data, Type scores) {
      this(
          (Utf8String) name,
          (DynamicBytes) data,
          (DynamicArray<Uint256>) scores
      );
    }
  }

  public static class NestedInfo extends DynamicStruct{
    public Uint256 topLevelId;
    public StaticInfo staticPart;
    public DynamicInfo dynamicPart;

    public NestedInfo(Uint256 topLevelId, StaticInfo staticPart,@Parameterized(type = DynamicInfo.class) DynamicInfo dynamicPart) {
      super(topLevelId, staticPart, dynamicPart);
      this.topLevelId = topLevelId;
      this.staticPart = staticPart;
      this.dynamicPart = dynamicPart;
    }
    public NestedInfo(List<Type> values) {
      super(values);
      this.topLevelId = (Uint256) values.get(0);
      this.staticPart = (StaticInfo) values.get(1);
      this.dynamicPart = (DynamicInfo) values.get(2);
    }
    public NestedInfo(Type topLevelId, Type staticPart, Type dynamicPart) {
      this(
          (Uint256) topLevelId,
          (StaticInfo) staticPart,
          (DynamicInfo) dynamicPart
      );
    }
  }

  static ApiWrapper client;
  static String testAddress;

  @BeforeAll
  static void setUp() {
    client = ApiWrapper.ofNile(ApiWrapper.generateAddress().toPrivateKey());
    testAddress = client.keyPair.toBase58CheckAddress();
  }

  @AfterAll
  static void tearDown() {
    if (client != null) {
      client.close();
    }
  }

  @Test
  @Order(0)
  @Disabled("has deployed")
  public void deployContract() throws Exception {

    // Deploy the contract
    Response.TransactionExtention transactionExtention =
          client.deployContract("AbiV2TestContract", CONTRACT_ABI, CONTRACT_BYTECODE, null, 1000_000_000L, 100, 10_000_000L, 0, null, 0);

    Transaction signTransaction = client.signTransaction(transactionExtention.getTransaction());
    String txId = client.broadcastTransaction(signTransaction);
//    System.out.println("Deploy transaction ID: " + txId);

    // Wait for deployment to be confirmed (adjust sleep time if needed for the network)

    Thread.sleep(5000);

    Response.TransactionInfo transactionInfo = client.getTransactionInfoById(txId);
    String contractAddress = Base58Check.bytesToBase58(transactionInfo.getContractAddress().toByteArray());

    assertNotNull(contractAddress, "Contract deployment failed or was not confirmed in time.");
//    System.out.println("Contract deployed to address: " + contractAddress);
    }


  @Test
  @Order(1)
  @DisplayName("Test encoding and decoding of a static struct")
  void testStaticStruct() {
    StaticInfo inputStruct = new StaticInfo(new Uint256(101), new Address(testAddress));

    Function function = new Function("setAndGetStaticStruct",
        Collections.singletonList(inputStruct),
        Collections.singletonList(new TypeReference<StaticInfo>() {})
    );

    Response.TransactionExtention transactionExtention =  client.triggerConstantContract(client.keyPair.toBase58CheckAddress(), contractAddress, function);

    assertNotNull(transactionExtention.getConstantResultList());
    assertFalse(transactionExtention.getConstantResultList().isEmpty());
    List<Type> result = FunctionReturnDecoder.decode(
        ByteArray.toHexString(transactionExtention.getConstantResult(0).toByteArray()),
        function.getOutputParameters());

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    StaticInfo outputStruct = (StaticInfo) result.get(0);
    assertEquals(inputStruct.staticId.getValue(), outputStruct.staticId.getValue());
    assertEquals(inputStruct.staticAddress.getValue(), outputStruct.staticAddress.getValue());
    assertEquals(new BigInteger("101"), outputStruct.staticId.getValue());
    assertEquals(testAddress, outputStruct.staticAddress.getValue());
  }

  @Test
  @Order(2)
  @DisplayName("Test encoding and decoding of a dynamic struct")
  void testDynamicStruct() {
    DynamicInfo inputStruct = new DynamicInfo(
        new Utf8String("Trident"),
        new DynamicBytes("TestData".getBytes()),
        new DynamicArray<>(Uint256.class, new Uint256(98), new Uint256(99), new Uint256(100))
    );

    Function function = new Function("setAndGetDynamicStruct",
        Collections.singletonList(inputStruct),
        Collections.singletonList(new TypeReference<DynamicInfo>() {})
    );

    Response.TransactionExtention result = client.triggerConstantContract(client.keyPair.toBase58CheckAddress(), contractAddress, function);

    assertEquals(1, result.getConstantResultCount());
    System.out.println(FunctionEncoder.encode(function));
    System.out.println(ByteArray.toHexString(result.getConstantResult(0).toByteArray()));

    List<Type> outputStructs
        = FunctionReturnDecoder.decode(ByteArray.toHexString(result.getConstantResult(0).toByteArray()), function.getOutputParameters());

    DynamicInfo outputStruct = (DynamicInfo)outputStructs.get(0);

    assertEquals(inputStruct.name.getValue(), outputStruct.name.getValue());
    assertArrayEquals(inputStruct.data.getValue(), outputStruct.data.getValue());
    assertEquals(inputStruct.scores.getValue().size(), outputStruct.scores.getValue().size());
    assertEquals(inputStruct.scores.getValue().get(0).getValue(), outputStruct.scores.getValue().get(0).getValue());
    assertEquals(inputStruct.scores.getValue().get(1).getValue(), outputStruct.scores.getValue().get(1).getValue());
    assertEquals(inputStruct.scores.getValue().get(2).getValue(), outputStruct.scores.getValue().get(2).getValue());
  }

  @Test
  @Order(3)
  @DisplayName("Test encoding and decoding of a nested struct")
  @Disabled("add private key to enable this case")
  void testNestedStruct() throws Exception {
    StaticInfo staticPart = new StaticInfo(new Uint256(202), new Address(testAddress));
    DynamicInfo dynamicPart = new DynamicInfo(
        new Utf8String("Nested"),
        new DynamicBytes(new byte[]{0x1, 0x2, 0x3}),
        new DynamicArray<>(Uint256.class, new Uint256(1))
    );
    NestedInfo inputStruct = new NestedInfo(new Uint256(303), staticPart, dynamicPart);

    Function function = new Function("setAndGetNestedStruct",
        Collections.singletonList(inputStruct),
        Collections.singletonList(new TypeReference<NestedInfo>() {})
    );

    TransactionExtention transactionExtention
        = client.triggerContract(client.keyPair.toBase58CheckAddress(),
        contractAddress,
        FunctionEncoder.encode(function),
        0,
        0,
        null,
        300_000_000L);

    Transaction signTransaction = client.signTransaction(transactionExtention.getTransaction());

    String txId = client.broadcastTransaction(signTransaction);
    System.out.println("setAndGetNestedStruct transaction ID: " + txId);
    assertNotNull(txId);


    Function function2 = new Function("lastNestedInfo",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<NestedInfo>() {
        })
    );
    Response.TransactionExtention result
        = client.triggerConstantContract(client.keyPair.toBase58CheckAddress(), contractAddress, function2);
    assertEquals(1, result.getConstantResultCount());

    String inputRaw = "0000000000000000000000000000000000000000000000000000000000000020"
        + ByteArray.toHexString(result.getConstantResult(0).toByteArray());

    List<Type> outputStructs
        = FunctionReturnDecoder.decode(inputRaw, Utils.convert(Collections.singletonList(new TypeReference<NestedInfo>() {})));

    NestedInfo outputStruct = (NestedInfo)outputStructs.get(0);

    assertEquals(inputStruct.topLevelId.getValue(), outputStruct.topLevelId.getValue());
    assertEquals(staticPart.staticId.getValue(), outputStruct.staticPart.staticId.getValue());
    assertEquals(staticPart.staticAddress.getValue(), outputStruct.staticPart.staticAddress.getValue());
    assertEquals(dynamicPart.name.getValue(), outputStruct.dynamicPart.name.getValue());
    assertArrayEquals(dynamicPart.data.getValue(), outputStruct.dynamicPart.data.getValue());
    assertEquals(dynamicPart.scores.getValue().size(), outputStruct.dynamicPart.scores.getValue().size());
    assertEquals(dynamicPart.scores.getValue().get(0).getValue(), outputStruct.dynamicPart.scores.getValue().get(0).getValue());
  }


  @Test
  @Order(4)
  @DisplayName("Test static array of static structs")
  void testStaticArrayOfStructs() {
    StaticArray<StaticInfo> inputArray = new StaticArray2<>(
        new StaticInfo(new Uint256(1), new Address(testAddress)),
        new StaticInfo(new Uint256(2), new Address(testAddress))
    );

    Function function = new Function("setAndGetStaticArrayOfStructs",
        Collections.singletonList(inputArray),
        Collections.singletonList(new TypeReference<StaticArray2<StaticInfo>>() {})
    );

    Response.TransactionExtention transactionExtention = client.triggerConstantContract(client.keyPair.toBase58CheckAddress(), contractAddress, function);

    List<Type> outPutStructs
        = FunctionReturnDecoder.decode(ByteArray.toHexString(transactionExtention.getConstantResult(0).toByteArray()), function.getOutputParameters());

    StaticArray2 outputStaticArrayOfStructs = (StaticArray2)outPutStructs.get(0);

    assertEquals(2, outputStaticArrayOfStructs.getValue().size());
    assertEquals(BigInteger.valueOf(1), ((StaticInfo)outputStaticArrayOfStructs.getValue().get(0)).staticId.getValue());
    assertEquals(testAddress, ((StaticInfo)outputStaticArrayOfStructs.getValue().get(0)).staticAddress.getValue());
    assertEquals(BigInteger.valueOf(2), ((StaticInfo)outputStaticArrayOfStructs.getValue().get(1)).staticId.getValue());
    assertEquals(testAddress, ((StaticInfo)outputStaticArrayOfStructs.getValue().get(1)).staticAddress.getValue());
  }

  @Test
  @Order(5)
  @DisplayName("Test multi-dimensional static array")
  @SuppressWarnings("unchecked")
  void testMultiDimArray() {
    // Represents uint256[2][3]
    StaticArray3<StaticArray2<Uint256>> inputArray = new StaticArray3<>(
        (Class)StaticArray2.class,
        new StaticArray2<>(Uint256.class, new Uint256(1), new Uint256(2)),
        new StaticArray2<>(Uint256.class, new Uint256(3), new Uint256(4)),
        new StaticArray2<>(Uint256.class, new Uint256(5), new Uint256(6))
    );

    Function function = new Function("setAndGetMultiDimArray",
        Collections.singletonList(inputArray),
        Collections.singletonList(new TypeReference<StaticArray3<StaticArray2<Uint256>>>() {})
    );

    Response.TransactionExtention transactionExtention = client.triggerConstantContract(testAddress, contractAddress, function);
    assertNotNull(transactionExtention.getConstantResultList());
    assertFalse(transactionExtention.getConstantResultList().isEmpty());
    assertFalse(transactionExtention.getConstantResultList().get(0).isEmpty());

    List<Type> outPutStructs
        = FunctionReturnDecoder.decode(ByteArray.toHexString(transactionExtention.getConstantResult(0).toByteArray()), function.getOutputParameters());

    StaticArray3<StaticArray2<Uint256>> outputStaticArrayOfStructs = (StaticArray3<StaticArray2<Uint256>>)outPutStructs.get(0);

    assertEquals(3, outputStaticArrayOfStructs.getValue().size());

    StaticArray2<Uint256> outputArray1 = (StaticArray2<Uint256>) outputStaticArrayOfStructs.getValue().get(0);
    StaticArray2<Uint256> outputArray2 = (StaticArray2<Uint256>) outputStaticArrayOfStructs.getValue().get(1);
    StaticArray2<Uint256> outputArray3 = (StaticArray2<Uint256>) outputStaticArrayOfStructs.getValue().get(2);

    assertEquals(2, outputArray1.getValue().size());
    assertEquals(2, outputArray2.getValue().size());
    assertEquals(2, outputArray3.getValue().size());
    assertEquals(1, outputArray1.getValue().get(0).getValue().intValue());
    assertEquals(2, outputArray1.getValue().get(1).getValue().intValue());
    assertEquals(3, outputArray2.getValue().get(0).getValue().intValue());
    assertEquals(4, outputArray2.getValue().get(1).getValue().intValue());
    assertEquals(5, outputArray3.getValue().get(0).getValue().intValue());
    assertEquals(6, outputArray3.getValue().get(1).getValue().intValue());
  }

  @Test
  @Order(6)
  @DisplayName("Test abi.encodePacked verification")
  void testEncodePacked() {
    // 1. Define inputs
    Uint256 id = new Uint256(12345);
    Address addr = new Address(client.keyPair.toBase58CheckAddress());
    Utf8String name = new Utf8String("hello packed");

    // 2. Generate packed bytes in Java
    String javaPackedHex = TypeEncoder.encodePacked(id) + TypeEncoder.encodePacked(addr) + TypeEncoder.encodePacked(name);
    byte[] javaPackedBytes = Numeric.hexStringToByteArray(javaPackedHex);

    // 3. Prepare function call to Solidity for verification
    Function function = new Function("verifyPackedEncoding",
        Arrays.asList(new DynamicBytes(javaPackedBytes), id, addr, name),
        Collections.singletonList(new TypeReference<Bool>() {})
    );

    // 4. Call the contract and get the boolean result
    TransactionExtention transactionExtention = client.triggerConstantContract(client.keyPair.toBase58CheckAddress(),
        contractAddress, function);

    List<Type> result
        = FunctionReturnDecoder.decode(ByteArray.toHexString(transactionExtention.getConstantResult(0).toByteArray()), function.getOutputParameters());

    Bool isMatch = (Bool) result.get(0);

    assertTrue(isMatch.getValue(), "Java's abi.encodePacked result did NOT match Solidity's.");
  }
}
