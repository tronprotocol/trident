package org.tron.trident.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TokenValidatorTest {

  @Test
  public void testValidateTokenId() {
    TokenValidator.validateTokenId("1000058");
    TokenValidator.validateTokenId("");
    TokenValidator.validateTokenId("_");
    TokenValidator.validateTokenId("  1000000");
    TokenValidator.validateTokenId(null);

    try {
      TokenValidator.validateTokenId(" ");

    } catch(Exception e){
      assertEquals("Token ID must be a valid number", e.getMessage());
    }

    try {
      TokenValidator.validateTokenId("abc");

    } catch(Exception e){
      assertEquals("Token ID must be a valid number", e.getMessage());
    }

    try {
      TokenValidator.validateTokenId("-123");

    } catch(Exception e){
      assertEquals("Token ID must be a valid number", e.getMessage());
    }

  }

}
