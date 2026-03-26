package org.tron.trident.abi;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.tron.trident.abi.datatypes.CustomError;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.crypto.Hash;
import org.tron.trident.utils.Numeric;

/**
 * Ethereum custom error encoding. Further limited details are available <a
 * href="https://docs.soliditylang.org/en/develop/abi-spec.html#errors">here</a>.
 */
public class CustomErrorEncoder {

  private CustomErrorEncoder() {
  }

  public static String encode(CustomError error) {
    return calculateSignatureHash(
        buildErrorSignature(error.getName(), error.getParameters()));
  }

  static <T extends Type> String buildErrorSignature(
      String errorName, List<TypeReference<T>> parameters) {

    StringBuilder result = new StringBuilder();
    result.append(errorName);
    result.append("(");
    String params =
        parameters.stream().map(Utils::getTypeName).collect(Collectors.joining(","));
    result.append(params);
    result.append(")");
    return result.toString();
  }

  public static String calculateSignatureHash(String errorSignature) {
    byte[] input = errorSignature.getBytes(StandardCharsets.UTF_8);
    byte[] hash = Hash.sha3(input);
    return Numeric.toHexString(hash).substring(2);
  }
}
