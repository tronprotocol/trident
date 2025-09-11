package org.tron.trident.core.exceptions;

public class IllegalException extends Exception {

  public static String TRANSACTION_FAIL_REASON = "Transaction not found. Possible reasons: \n"
          + " 1) Parma is incorrect or invalid; \n"
          + " 2) Transaction is still pending and not yet confirmed in a block; \n"
          + " 3) Other reasons such as transaction may have been dropped from the network...";

  public static String BLOCK_FAIL_REASON = "Block not found. Possible reasons: \n"
          + " 1) Param is incorrect or invalid; \n"
          + " 2) Block is not yet confirmed; ";

  public static String EXCHANGE_FAIL_REASON = "Exchange not found. Possible reasons: \n"
          + " 1) Parma is incorrect or invalid; \n"
          + " 2) Exchange is not yet confirmed; ";

  public IllegalException() {
    super("Query failed. Please check the parameters.");
  }

  public IllegalException(String message) {
    super(message);
  }
}
