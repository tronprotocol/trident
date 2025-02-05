package org.tron.trident.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TokenValidatorTest {

  @Test
  public void testValidateTokenId() {
    TokenValidator.validateTokenId("123");
    TokenValidator.validateTokenId("");
    TokenValidator.validateTokenId("#");
    TokenValidator.validateTokenId("  123");

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

  @Test
  public void testValidateTrc10TokenId() {
    TokenValidator.validateTrc10TokenId("1000000");
    try {
      TokenValidator.validateTrc10TokenId("999");
    } catch(Exception e){
      assertEquals("TRC10 token ID must ge 1000000", e.getMessage());
    }

    try {
      TokenValidator.validateTrc10TokenId("abc");
    } catch(Exception e){
      assertEquals("Token ID must be a valid number", e.getMessage());
    }

  }

}
