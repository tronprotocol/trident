package org.tron.trident.core.utils;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.abi.TypeEncoder;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.core.exceptions.ContractCreateException;
import org.tron.trident.core.transaction.BlockId;
import org.tron.trident.crypto.Hash;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Strings;

public class Utils {

  public static byte ADD_PRE_FIX_BYTE_MAINNET = (byte) 0x41;   //41 + address
  public static byte ADD_PRE_FIX_BYTE_TESTNET = (byte) 0xa0;   //a0 + address
  public static int ADDRESS_SIZE = 21;

  private static byte addressPreFixByte = ADD_PRE_FIX_BYTE_MAINNET;

  public static BlockId getBlockId(Response.BlockExtention blockExtention) {
    BlockId blockId = new BlockId(Sha256Hash.ZERO_HASH, 0);
    if (blockId.equals(Sha256Hash.ZERO_HASH)) {
      blockId =
          new BlockId(Sha256Hash.of(true,
              blockExtention.getBlockHeader().getRawData().toByteArray()),
              blockExtention.getBlockHeader().getRawData().getNumber());
    }
    return blockId;
  }

  public static Contract.CreateSmartContract getSmartContractFromTransaction(
      Chain.Transaction trx) {
    try {
      Any any = trx.getRawData().getContract(0).getParameter();
      return any.unpack(Contract.CreateSmartContract.class);
    } catch (InvalidProtocolBufferException e) {
      return null;
    }
  }

  public static byte getAddressPreFixByte() {
    return ADD_PRE_FIX_BYTE_MAINNET;
  }


  public static boolean addressValid(byte[] address) {
    if (address == null || address.length == 0) {
      return false;
    }
    if (address.length != ADDRESS_SIZE) {
      return false;
    }
    byte preFixByte = address[0];
    return preFixByte == getAddressPreFixByte();
    // Other rule;
  }

  public static String encode58Check(byte[] input) {
    byte[] hash0 = Sha256Hash.hash(true, input);
    byte[] hash1 = Sha256Hash.hash(true, hash0);
    byte[] inputCheck = new byte[input.length + 4];
    System.arraycopy(input, 0, inputCheck, 0, input.length);
    System.arraycopy(hash1, 0, inputCheck, input.length, 4);
    return Base58.encode(inputCheck);
  }

  private static byte[] decode58Check(String input) {
    if (Strings.isEmpty(input)) {
      return null;
    }
    try {
      byte[] decodeCheck = Base58.decode(input);
      if (decodeCheck.length <= 4) {
        return null;
      }
      byte[] decodeData = new byte[decodeCheck.length - 4];
      System.arraycopy(decodeCheck, 0, decodeData, 0, decodeData.length);
      byte[] hash0 = Sha256Hash.hash(true, decodeData);
      byte[] hash1 = Sha256Hash.hash(true, hash0);
      if (hash1[0] == decodeCheck[decodeData.length]
          && hash1[1] == decodeCheck[decodeData.length + 1]
          && hash1[2] == decodeCheck[decodeData.length + 2]
          && hash1[3] == decodeCheck[decodeData.length + 3]) {
        return decodeData;
      }
      return null;
    } catch (Exception e) {
      return null;
    }

  }

  public static byte[] decodeFromBase58Check(String addressBase58) {
    if (Strings.isEmpty(addressBase58)) {
      //System.out.println("Warning: Address is empty !!");
      return null;
    }
    byte[] address = decode58Check(addressBase58);
    if (!addressValid(address)) {
      return null;
    }
    return address;
  }

  public static ByteString encodeParameter(List<Type<?>> params) throws ContractCreateException {
    StringBuilder builder = new StringBuilder();
    for (Type<?> p : params) {
      builder.append(TypeEncoder.encode(p));
    }
    return ByteString.copyFrom(Hex.decode(builder.toString()));
  }

  public static byte[] replaceLibraryAddress(String code, String libraryAddressPair,
      String compilerVersion) {
    if (Strings.isEmpty(code)) {
      throw new IllegalArgumentException("code is empty");
    }
    if (Strings.isEmpty(libraryAddressPair)) {
      throw new IllegalArgumentException("libraryAddressPair is empty");
    }

    String[] libraryAddressList = libraryAddressPair.split("[,]");

    for (String cur : libraryAddressList) {
      int lastPosition = cur.lastIndexOf(":");
      if (-1 == lastPosition) {
        throw new IllegalArgumentException("libraryAddress delimit by ':'");
      }
      String libraryName = cur.substring(0, lastPosition);
      String address = cur.substring(lastPosition + 1);
      byte[] addressBytes = decodeFromBase58Check(address);
      if (addressBytes == null) {
        throw new IllegalArgumentException("invalid address format");
      }
      String libraryAddressHex = ByteArray.toHexString(addressBytes).substring(2);

      String beReplaced;
      if (compilerVersion == null) {
        // old version
        String repeated = new String(
            new char[40 - libraryName.length() - 2])
            .replace("\0", "_");
        beReplaced = "__" + libraryName + repeated;
      } else if (compilerVersion.equalsIgnoreCase("v5")) {
        // 0.5.4 version
        String libraryNameKeccak256 =
            ByteArray.toHexString(
                    Hash.sha3(libraryName.getBytes()))
                .substring(0, 34);
        beReplaced = "__\\$" + libraryNameKeccak256 + "\\$__";
      } else {
        throw new IllegalArgumentException("unknown compiler version.");
      }

      Matcher m = Pattern.compile(beReplaced).matcher(code);
      code = m.replaceAll(libraryAddressHex);
    }

    return ByteArray.fromHexString(code);
  }
}
