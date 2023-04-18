package org.tron.trident.core.utils;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.trident.core.transaction.BlockId;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;
import org.tron.trident.proto.Response;

public class Utils {

  public static BlockId getBlockId(Response.BlockExtention blockExtention) {
    BlockId blockId = new BlockId(Sha256Hash.ZERO_HASH, 0);
    if (blockId.equals(Sha256Hash.ZERO_HASH)) {
      blockId =
          new BlockId(Sha256Hash.of(true,
              blockExtention.getBlockHeader().getRawData().toByteArray()), blockExtention.getBlockHeader().getRawData().getNumber());
    }
    return blockId;
  }

  public static Contract.CreateSmartContract getSmartContractFromTransaction(Chain.Transaction trx) {
    try {
      Any any = trx.getRawData().getContract(0).getParameter();
      Contract.CreateSmartContract createSmartContract = any.unpack(Contract.CreateSmartContract.class);
      return createSmartContract;
    } catch (InvalidProtocolBufferException e) {
      return null;
    }
  }

}
