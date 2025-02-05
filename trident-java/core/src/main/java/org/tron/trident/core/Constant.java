package org.tron.trident.core;

public final class Constant {

  //TronGrid gRPC services, maintained by official team
  public static final String TRONGRID_MAIN_NET = "grpc.trongrid.io:50051";
  public static final String TRONGRID_MAIN_NET_SOLIDITY = "grpc.trongrid.io:50052";

  public static final String TRONGRID_SHASTA = "grpc.shasta.trongrid.io:50051";
  public static final String TRONGRID_SHASTA_SOLIDITY = "grpc.shasta.trongrid.io:50052";

  //Public Fullnode, maintained by official team
  public static final String FULLNODE_NILE = "grpc.nile.trongrid.io:50051";
  public static final String FULLNODE_NILE_SOLIDITY = "grpc.nile.trongrid.io:50061";


  public static final long TRANSACTION_DEFAULT_EXPIRATION_TIME = 60 * 1_000L; //60 seconds

  public static final long GRPC_TIMEOUT = 30 * 1_000L; //30 seconds

  public static final long FEE_LIMIT = 150_000_000L; //150 TRX

  public static final long CONSUME_USER_RESOURCE_PERCENT = 100L;

  public static final long ORIGIN_ENERGY_LIMIT = 100_000_000L;

  public static final String TRX_SYMBOL_BYTES = "_";

}