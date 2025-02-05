package org.tron.trident.core.utils;

import org.tron.trident.utils.Numeric;

public class TokenValidator {
  /**
   * Validates the general format and value of a token ID
   *
   * @param tokenId The token ID to validate
   * @throws IllegalArgumentException if the token ID is invalid
   */
  public static void validateTokenId(String tokenId) {
    // Return if it's a special marker
    if (tokenId == null || tokenId.isEmpty() || "#".equals(tokenId)) {
      return;
    }

    // Remove whitespace
    String trimmedTokenId = tokenId.trim();

    // Check if it's a valid numeric string
    if (!Numeric.isNumericString(trimmedTokenId)) {
      throw new IllegalArgumentException("Token ID must be a valid number");
    }

  }

  /**
   * check TRC10 ID
   */
  public static void validateTrc10TokenId(String tokenId) {
    validateTokenId(tokenId);
    try {
      long tokenValue = Long.parseLong(tokenId.trim());
      if (tokenValue < 1000000L) {
        throw new IllegalArgumentException("TRC10 token ID must ge 1000000");
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid TRC10 token ID", e);
    }
  }

}
