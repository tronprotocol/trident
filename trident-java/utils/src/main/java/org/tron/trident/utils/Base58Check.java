/*
 * Bitcoin cryptography library
 * Copyright (c) Project Nayuki
 *
 * https://www.nayuki.io/page/bitcoin-cryptography-library
 * https://github.com/nayuki/Bitcoin-Cryptography-Library
 */

package org.tron.trident.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import org.bouncycastle.jcajce.provider.digest.SHA256;

/**
 * Converts between an array of bytes and a Base58Check string. Not instantiable.
 */
public final class Base58Check {
  /*---- Static functions ----*/

  // Adds the checksum and converts to Base58Check. Note that the caller needs to prepend the version byte(s).
  public static String bytesToBase58(byte[] data) {
    return rawBytesToBase58(addCheckHash(data));
  }

  // Directly converts to Base58Check without adding a checksum.
  static String rawBytesToBase58(byte[] data) {
    // Convert to base-58 string
    StringBuilder sb = new StringBuilder();
    BigInteger num = new BigInteger(1, data);
    while (num.signum() != 0) {
      BigInteger[] quotrem = num.divideAndRemainder(ALPHABET_SIZE);
      sb.append(ALPHABET.charAt(quotrem[1].intValue()));
      num = quotrem[0];
    }

    // Add '1' characters for leading 0-value bytes
    for (int i = 0; i < data.length && data[i] == 0; i++) sb.append(ALPHABET.charAt(0));
    return sb.reverse().toString();
  }

  // Returns a new byte array by concatenating the given array with its checksum.
  static byte[] addCheckHash(byte[] data) {
    try {
      SHA256.Digest digest = new SHA256.Digest();
      digest.update(data);
      byte[] hash0 = digest.digest();
      digest.reset();
      digest.update(hash0);
      byte[] hash = Arrays.copyOf(digest.digest(), 4);
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      buf.write(data);
      buf.write(hash);
      return buf.toByteArray();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  // Converts the given Base58Check string to a byte array, verifies the checksum, and removes the checksum to return
  // the payload. The caller is responsible for handling the version byte(s).
  public static byte[] base58ToBytes(String s) {
    byte[] concat = base58ToRawBytes(s);
    byte[] data = Arrays.copyOf(concat, concat.length - 4);
    byte[] hash = Arrays.copyOfRange(concat, concat.length - 4, concat.length);

    SHA256.Digest digest = new SHA256.Digest();
    digest.update(data);
    byte[] hash0 = digest.digest();
    digest.reset();
    digest.update(hash0);

    byte[] rehash = Arrays.copyOf(digest.digest(), 4);
    if (!Arrays.equals(rehash, hash))
      throw new IllegalArgumentException("Checksum mismatch");
    return data;
  }

  // Converts the given Base58Check string to a byte array, without checking or removing the trailing 4-byte checksum.
  static byte[] base58ToRawBytes(String s) {
    // Parse base-58 string
    BigInteger num = BigInteger.ZERO;
    for (int i = 0; i < s.length(); i++) {
      num = num.multiply(ALPHABET_SIZE);
      int digit = ALPHABET.indexOf(s.charAt(i));
      if (digit == -1)
        throw new IllegalArgumentException("Invalid character for Base58Check");
      num = num.add(BigInteger.valueOf(digit));
    }

    // Strip possible leading zero due to mandatory sign bit
    byte[] b = num.toByteArray();
    if (b[0] == 0)
      b = Arrays.copyOfRange(b, 1, b.length);

    try {
      // Convert leading '1' characters to leading 0-value bytes
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      for (int i = 0; i < s.length() && s.charAt(i) == ALPHABET.charAt(0); i++) buf.write(0);
      buf.write(b);
      return buf.toByteArray();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  /*---- Class constants ----*/

  public static final String ALPHABET =
      "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"; // Everything except 0OIl
  private static final BigInteger ALPHABET_SIZE = BigInteger.valueOf(ALPHABET.length());

  /*---- Miscellaneous ----*/

  private Base58Check() {} // Not instantiable
}
