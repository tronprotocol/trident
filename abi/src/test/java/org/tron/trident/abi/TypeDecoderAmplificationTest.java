/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.tron.trident.abi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.tron.trident.abi.datatypes.DynamicArray;
import org.tron.trident.abi.datatypes.DynamicBytes;
import org.tron.trident.abi.datatypes.DynamicStruct;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.abi.datatypes.Utf8String;
import org.tron.trident.abi.datatypes.generated.StaticArray20;
import org.tron.trident.abi.datatypes.generated.Uint256;

/**
 * Regression tests for decode amplification: a well-formed ABI payload never decodes to more
 * values, or more bytes, than it physically carries, so an array element must not be able to
 * claim input another element already claimed.
 *
 * <p>Each element of a dynamic array carries its own tail pointer, read from attacker-controlled
 * input, and nothing requires those pointers to be distinct. Every pointer may name the same
 * tail — or overlapping tails — and each element then decodes independently while passing a
 * bound measured against the <em>whole</em> remaining input.
 *
 * <p>The shapes below inflate in one of two directions, and each is caught by its own count.
 * A few elements sharing one large payload inflate bytes while the value count stays low;
 * many elements sharing an empty container do the reverse. Both kinds are represented here,
 * alongside legitimate encodings that must keep decoding.
 */
public class TypeDecoderAmplificationTest {

  /** Hex characters per 32-byte ABI word. */
  private static final int WORD = 64;

  private static final String TOO_MANY_VALUES = "more values than it can carry";
  private static final String TOO_MANY_BYTES = "more than it occupies";

  /**
   * Asserts the decode is rejected, and rejected by the accounting rather than incidentally.
   * Bounds checks raise the same exception type, so a payload with a construction mistake could
   * otherwise pass the test while never exercising the amplification it is meant to cover.
   */
  private static void assertRejected(String expectedReason, Executable decode) {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, decode);
    assertTrue(
        thrown.getMessage().contains(expectedReason),
        "rejected for the wrong reason: " + thrown.getMessage());
  }

  /** Left-pads {@code value} into a single 32-byte ABI word. Locale-independent. */
  private static String word(long value) {
    String hex = Long.toHexString(value);
    StringBuilder sb = new StringBuilder(WORD);
    for (int i = hex.length(); i < WORD; i++) {
      sb.append('0');
    }
    return sb.append(hex).toString();
  }

  private static String zeros(int hexChars) {
    StringBuilder sb = new StringBuilder(hexChars);
    for (int i = 0; i < hexChars; i++) {
      sb.append('0');
    }
    return sb.toString();
  }

  /**
   * Byte offset that a head slot must carry for its element data to land at
   * {@code targetHex}. Element pointers are relative to the start of the head region, which
   * begins one word after the array-length prefix.
   */
  private static long pointerTo(int targetHex) {
    return (targetHex - WORD) / 2;
  }

  // ---------------------------------------------------------------------------------------
  // Aliased tails: every element points at one shared payload.
  // ---------------------------------------------------------------------------------------

  /**
   * {@code string[]} whose every head slot names one shared tail: ~4 KB of input that decodes
   * into 64 independent 2 KB strings, a shape that scales quadratically.
   */
  private static String aliasedStringTailPayload() {
    final int elements = 64;
    final int payloadBytes = 2048;
    final int tailHex = WORD + elements * WORD;

    StringBuilder input = new StringBuilder();
    input.append(word(elements));
    for (int i = 0; i < elements; i++) {
      input.append(word(pointerTo(tailHex)));
    }
    input.append(word(payloadBytes));
    input.append(zeros(payloadBytes * 2));
    return input.toString();
  }

  @Test
  public void decodeDynamicArray_rejectsAliasedStringTails() {
    String malicious = aliasedStringTailPayload();
    assertRejected(
        TOO_MANY_BYTES,
        () -> TypeDecoder.decodeDynamicArray(
            malicious, 0, new TypeReference<DynamicArray<Utf8String>>() {}));
  }

  @Test
  public void decodeDynamicArray_rejectsAliasedBytesTails() {
    // The same shape down the DynamicBytes arm of the value dispatch: bytes[] is named in the
    // report alongside string[], and the two are separate branches in decode().
    String malicious = aliasedStringTailPayload();
    assertRejected(
        TOO_MANY_BYTES,
        () -> TypeDecoder.decodeDynamicArray(
            malicious, 0, new TypeReference<DynamicArray<DynamicBytes>>() {}));
  }

  @Test
  public void decodeDynamicArray_rejectsAliasedNestedArrayTails() {
    final int outerElements = 32;
    final int innerElements = 64;
    final int tailHex = WORD + outerElements * WORD;

    StringBuilder input = new StringBuilder();
    input.append(word(outerElements));
    for (int i = 0; i < outerElements; i++) {
      input.append(word(pointerTo(tailHex)));
    }
    input.append(word(innerElements));
    for (int i = 0; i < innerElements; i++) {
      input.append(word(i));
    }

    // Object-count amplification rather than byte amplification: 32 x 64 = 2048
    // Uint256 instances materialise from ~3 KB of input.
    String malicious = input.toString();
    assertRejected(
        TOO_MANY_VALUES,
        () -> TypeDecoder.decodeDynamicArray(
            malicious, 0, new TypeReference<DynamicArray<DynamicArray<Uint256>>>() {}));
  }

  // ---------------------------------------------------------------------------------------
  // Overlapping tails: pointers strictly increase, so a monotonicity-only check accepts
  // them, but each element's payload still runs over its neighbours'.
  // ---------------------------------------------------------------------------------------

  @Test
  public void decodeDynamicArray_rejectsOverlappingStringTails() {
    final int elements = 64;
    final int fillerHex = 4096;
    final int tailHex = WORD + elements * WORD;
    final int totalHex = tailHex + elements * WORD + fillerHex;

    StringBuilder head = new StringBuilder();
    head.append(word(elements));
    StringBuilder tail = new StringBuilder();
    for (int i = 0; i < elements; i++) {
      int lengthWordHex = tailHex + i * WORD;
      head.append(word(pointerTo(lengthWordHex)));
      // Claim every byte left after this element's own length word: the maximum the
      // existing whole-input bound allows, which makes all payloads overlap.
      tail.append(word((totalHex - (lengthWordHex + WORD)) / 2));
    }

    String malicious = head.append(tail).append(zeros(fillerHex)).toString();
    assertEquals(totalHex, malicious.length());
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicArray(
            malicious, 0, new TypeReference<DynamicArray<Utf8String>>() {}));
  }

  @Test
  public void decodeDynamicArray_rejectsOverlappingNestedArrayTails() {
    final int outerElements = 32;
    final int fillerHex = 4096;
    final int tailHex = WORD + outerElements * WORD;
    final int totalHex = tailHex + outerElements * WORD + fillerHex;

    StringBuilder head = new StringBuilder();
    head.append(word(outerElements));
    StringBuilder tail = new StringBuilder();
    for (int i = 0; i < outerElements; i++) {
      int lengthWordHex = tailHex + i * WORD;
      head.append(word(pointerTo(lengthWordHex)));
      // Each inner array declares as many elements as the whole remaining input can
      // hold, so the inner element runs read straight through their neighbours.
      tail.append(word((totalHex - (lengthWordHex + WORD)) / WORD));
    }

    // This is the case that survives if only the payload-length bound is narrowed:
    // the amplification is carried by the array-length bound instead.
    String malicious = head.append(tail).append(zeros(fillerHex)).toString();
    assertEquals(totalHex, malicious.length());
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicArray(
            malicious, 0, new TypeReference<DynamicArray<DynamicArray<Uint256>>>() {}));
  }

  // ---------------------------------------------------------------------------------------
  // Aliased dynamic structs: the amplification rides on the struct's static fields, which
  // cost the attacker one word each but are decoded afresh for every aliased element.
  // ---------------------------------------------------------------------------------------

  @Test
  public void decodeDynamicArray_rejectsAliasedDynamicStructTails() throws Exception {
    final int elements = 100;
    final int staticFields = 100;
    final int tailHex = WORD + elements * WORD;

    List<TypeReference<?>> innerTypes = new ArrayList<TypeReference<?>>();
    for (int i = 0; i < staticFields; i++) {
      innerTypes.add(TypeReference.makeTypeReference("uint256"));
    }
    // One cheap dynamic field: it makes the struct ABI-dynamic (so the array is head/tail
    // addressed and can be aliased) while costing a single zero-length word.
    innerTypes.add(TypeReference.makeTypeReference("uint256[]"));
    final TypeReference<DynamicStruct> structRef =
        new TypeReference<DynamicStruct>(false, innerTypes) {};

    StringBuilder input = new StringBuilder();
    input.append(word(elements));
    for (int i = 0; i < elements; i++) {
      input.append(word(pointerTo(tailHex)));
    }
    for (int i = 0; i < staticFields; i++) {
      input.append(word(i));
    }
    // Offset of the struct's dynamic field, relative to the start of the struct.
    input.append(word((staticFields + 1) * (WORD / 2)));
    input.append(word(0));

    // 203 words in, 100 x 101 = 10 100 objects out if the struct body is not charged.
    String malicious = input.toString();
    assertEquals((1 + elements + staticFields + 2) * WORD, malicious.length());
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicArray(
            malicious,
            0,
            new TypeReference<DynamicArray<DynamicStruct>>() {
              @Override
              public TypeReference getSubTypeReference() {
                return structRef;
              }
            }));
  }

  @Test
  public void decodeDynamicArray_rejectsAliasedEmptyNestedArrays() {
    final int outer = 100;
    final int middle = 100;

    StringBuilder input = new StringBuilder();
    input.append(word(outer));
    for (int i = 0; i < outer; i++) {
      input.append(word(outer * (WORD / 2)));
    }
    input.append(word(middle));
    for (int i = 0; i < middle; i++) {
      input.append(word(middle * (WORD / 2)));
    }
    input.append(word(0));

    // Nothing chargeable lives at the leaves — the innermost array is empty — so the whole
    // amplification is carried by the element containers themselves: 203 words in, 10 100
    // arrays out unless each tail dereference is paid for.
    String malicious = input.toString();
    assertEquals((1 + outer + 1 + middle + 1) * WORD, malicious.length());
    assertRejected(
        TOO_MANY_VALUES,
        () -> TypeDecoder.decodeDynamicArray(
            malicious,
            0,
            new TypeReference<DynamicArray<DynamicArray<DynamicArray<Uint256>>>>() {}));
  }

  // ---------------------------------------------------------------------------------------
  // Partitioning the tails is not enough on its own: an element's inline reads have to respect
  // the region it was given, or it simply reads on into the next element's slots.
  // ---------------------------------------------------------------------------------------

  @Test
  public void decodeDynamicArray_rejectsStructHeadsReadingSharedSlots() throws Exception {
    final int elements = 100;
    final int staticFields = 100;
    final int headEnd = WORD + elements * WORD;

    List<TypeReference<?>> innerTypes = new ArrayList<TypeReference<?>>();
    for (int i = 0; i < staticFields; i++) {
      innerTypes.add(TypeReference.makeTypeReference("uint256"));
    }
    innerTypes.add(TypeReference.makeTypeReference("uint256[]"));
    final TypeReference<DynamicStruct> structRef =
        new TypeReference<DynamicStruct>(false, innerTypes) {};

    StringBuilder input = new StringBuilder();
    input.append(word(elements));
    for (int i = 0; i < elements; i++) {
      // Tails are distinct and one word apart, so they survive any check on the pointers
      // themselves — the amplification is in each struct's head reading the next struct's
      // slots, so one run of static words is decoded once per element.
      input.append(word(pointerTo(headEnd + i * WORD)));
    }
    // An all-zero tail region: every struct's trailing dynamic field then carries offset 0,
    // pointing back at its own first word, which reads as a zero-length array. Nothing here
    // is out of bounds; only the static head words are re-read by element after element.
    for (int i = 0; i < (elements - 1) + (staticFields + 1); i++) {
      input.append(word(0));
    }

    // 301 words in, 100 x 101 = 10 100 values out if each struct head is not paid for.
    String malicious = input.toString();
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicArray(
            malicious,
            0,
            new TypeReference<DynamicArray<DynamicStruct>>() {
              @Override
              public TypeReference getSubTypeReference() {
                return structRef;
              }
            }));
  }

  @Test
  public void decodeDynamicArray_rejectsWideStaticElementsReadingSharedSlots() {
    final int outer = 20;
    final int inner = 20;
    final int elementWords = 20;
    final int headEnd = WORD + outer * WORD;
    // Inner arrays sit one element-count apart, so each has room for its length word plus one
    // word per element — while every element is in fact 20 words wide and reads on into its
    // neighbours.
    final int span = (1 + inner) * WORD;

    StringBuilder input = new StringBuilder();
    input.append(word(outer));
    for (int i = 0; i < outer; i++) {
      input.append(word(pointerTo(headEnd + i * span)));
    }
    for (int i = 0; i < outer; i++) {
      input.append(word(inner));
      for (int j = 0; j < inner; j++) {
        input.append(word(j));
      }
    }
    for (int i = 0; i < inner * elementWords; i++) {
      input.append(word(1));
    }

    // 20 x 20 x 20 = 8 000 numeric values from 841 words.
    String malicious = input.toString();
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicArray(
            malicious,
            0,
            new TypeReference<DynamicArray<DynamicArray<StaticArray20<Uint256>>>>() {}));
  }

  // ---------------------------------------------------------------------------------------
  // One response, many output parameters: the count has to span the whole function result, or
  // each output starts fresh and they can all claim the same tail.
  // ---------------------------------------------------------------------------------------

  @Test
  public void functionReturnDecoder_rejectsTailsAliasedAcrossOutputParameters() {
    final int outputs = 40;
    final int elements = 50;

    List<TypeReference<Type>> outputParameters = new ArrayList<TypeReference<Type>>();
    StringBuilder input = new StringBuilder();
    for (int i = 0; i < outputs; i++) {
      outputParameters.add(
          (TypeReference<Type>) (TypeReference<?>) new TypeReference<DynamicArray<Uint256>>() {});
      // Every output head names the same, perfectly well-formed, uint256[] tail.
      input.append(word(outputs * (WORD / 2)));
    }
    input.append(word(elements));
    for (int i = 0; i < elements; i++) {
      input.append(word(i));
    }

    // 91 words in, 40 x 50 = 2 000 Uint256 out while every individual output looks legitimate.
    String malicious = input.toString();
    assertEquals((outputs + 1 + elements) * WORD, malicious.length());
    assertThrows(
        IllegalArgumentException.class,
        () -> FunctionReturnDecoder.decode(malicious, outputParameters));
  }

  // ---------------------------------------------------------------------------------------
  // Legitimate encodings must keep decoding — these guard against over-tightening.
  // ---------------------------------------------------------------------------------------

  @Test
  public void decodeDynamicArray_acceptsLegitimateStringArray() {
    List<Utf8String> values =
        Arrays.asList(
            new Utf8String("alpha"),
            new Utf8String(""),
            new Utf8String(
                "a string comfortably longer than a single 32-byte word, spanning several"));
    String encoded =
        TypeEncoder.encode(new DynamicArray<Utf8String>(Utf8String.class, values));

    DynamicArray<Utf8String> decoded =
        TypeDecoder.decodeDynamicArray(
            encoded, 0, new TypeReference<DynamicArray<Utf8String>>() {});

    assertEquals(3, decoded.getValue().size());
    assertEquals("alpha", decoded.getValue().get(0).getValue());
    assertEquals("", decoded.getValue().get(1).getValue());
    assertEquals(values.get(2).getValue(), decoded.getValue().get(2).getValue());
  }

  @Test
  public void decodeDynamicArray_acceptsLegitimateArrayWithRepeatedValues() {
    // Equal values still get their own tails; nothing here may look like aliasing.
    List<Utf8String> values =
        Arrays.asList(new Utf8String("same"), new Utf8String("same"), new Utf8String("same"));
    String encoded =
        TypeEncoder.encode(new DynamicArray<Utf8String>(Utf8String.class, values));

    DynamicArray<Utf8String> decoded =
        TypeDecoder.decodeDynamicArray(
            encoded, 0, new TypeReference<DynamicArray<Utf8String>>() {});

    assertEquals(3, decoded.getValue().size());
    for (int i = 0; i < 3; i++) {
      assertEquals("same", decoded.getValue().get(i).getValue());
    }
  }

  @Test
  public void decodeDynamicArray_acceptsLegitimateEmptyStringElements() {
    // Empty payloads make consecutive tails only one word apart — the tightest
    // legitimate spacing there is.
    List<Utf8String> values =
        Arrays.asList(new Utf8String(""), new Utf8String(""), new Utf8String(""));
    String encoded =
        TypeEncoder.encode(new DynamicArray<Utf8String>(Utf8String.class, values));

    DynamicArray<Utf8String> decoded =
        TypeDecoder.decodeDynamicArray(
            encoded, 0, new TypeReference<DynamicArray<Utf8String>>() {});

    assertEquals(3, decoded.getValue().size());
    assertEquals("", decoded.getValue().get(2).getValue());
  }

  @Test
  public void decodeDynamicArray_acceptsLegitimateEmptyStringArray() {
    // The tightest legitimate shape there is. Every element costs one head slot and one
    // zero-length word, so the count lands closer to its limit here than anywhere else — this
    // is what would break first if anything were ever counted twice.
    List<Utf8String> values = new ArrayList<Utf8String>();
    for (int i = 0; i < 64; i++) {
      values.add(new Utf8String(""));
    }
    String encoded =
        TypeEncoder.encode(new DynamicArray<Utf8String>(Utf8String.class, values));

    DynamicArray<Utf8String> decoded =
        TypeDecoder.decodeDynamicArray(
            encoded, 0, new TypeReference<DynamicArray<Utf8String>>() {});

    assertEquals(64, decoded.getValue().size());
    assertEquals("", decoded.getValue().get(63).getValue());
  }

  @Test
  public void decodeDynamicArray_acceptsLegitimateDynamicStructArray() throws Exception {
    // The malicious struct shapes above are the ones most at risk of over-tightening, so the
    // ordinary case needs to be pinned too.
    DynamicStruct first =
        new DynamicStruct(new Uint256(BigInteger.ONE), new Utf8String("alpha"));
    DynamicStruct second =
        new DynamicStruct(new Uint256(BigInteger.TEN), new Utf8String("beta"));
    String encoded =
        TypeEncoder.encode(new DynamicArray(DynamicStruct.class, Arrays.asList(first, second)));

    final TypeReference<DynamicStruct> structRef =
        new TypeReference<DynamicStruct>(
            false,
            Arrays.<TypeReference<?>>asList(
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("string"))) {};
    DynamicArray<DynamicStruct> decoded =
        TypeDecoder.decodeDynamicArray(
            encoded,
            0,
            new TypeReference<DynamicArray<DynamicStruct>>() {
              @Override
              public TypeReference getSubTypeReference() {
                return structRef;
              }
            });

    assertEquals(2, decoded.getValue().size());
    assertEquals(
        BigInteger.ONE, decoded.getValue().get(0).getValue().get(0).getValue());
    assertEquals("beta", decoded.getValue().get(1).getValue().get(1).getValue());
  }

  @Test
  public void decodeDynamicArray_acceptsLegitimateLargeStringArray() {
    List<Utf8String> values = new ArrayList<Utf8String>();
    for (int i = 0; i < 64; i++) {
      values.add(new Utf8String("element-" + i));
    }
    String encoded =
        TypeEncoder.encode(new DynamicArray<Utf8String>(Utf8String.class, values));

    DynamicArray<Utf8String> decoded =
        TypeDecoder.decodeDynamicArray(
            encoded, 0, new TypeReference<DynamicArray<Utf8String>>() {});

    assertEquals(64, decoded.getValue().size());
    assertEquals("element-0", decoded.getValue().get(0).getValue());
    assertEquals("element-63", decoded.getValue().get(63).getValue());
  }

  @Test
  public void decodeDynamicArray_acceptsLegitimateNestedUintArray() {
    List<DynamicArray<Uint256>> outer = new ArrayList<DynamicArray<Uint256>>();
    outer.add(
        new DynamicArray<Uint256>(
            Uint256.class,
            Arrays.asList(new Uint256(BigInteger.ONE), new Uint256(BigInteger.TEN))));
    outer.add(
        new DynamicArray<Uint256>(
            Uint256.class, Arrays.asList(new Uint256(BigInteger.valueOf(7)))));

    String encoded =
        TypeEncoder.encode(
            new DynamicArray(DynamicArray.class, outer));

    DynamicArray<DynamicArray<Uint256>> decoded =
        TypeDecoder.decodeDynamicArray(
            encoded, 0, new TypeReference<DynamicArray<DynamicArray<Uint256>>>() {});

    assertEquals(2, decoded.getValue().size());
    assertEquals(2, decoded.getValue().get(0).getValue().size());
    assertEquals(1, decoded.getValue().get(1).getValue().size());
    assertEquals(BigInteger.TEN, decoded.getValue().get(0).getValue().get(1).getValue());
  }

  // ---------------------------------------------------------------------------------------
  // Accounting is per decode, never per thread: SDK callers run on pooled threads, so a count
  // left behind by one request would silently reject the next one to reuse that thread.
  // These pin the "always cleared, even when the decode aborts" invariant.
  // ---------------------------------------------------------------------------------------

  /** A legitimate 64-element {@code string[]} that spends most of what its input carries. */
  private static void decodeLegitimateArray() {
    List<Utf8String> values = new ArrayList<Utf8String>();
    for (int i = 0; i < 64; i++) {
      values.add(new Utf8String("element-" + i));
    }
    String encoded =
        TypeEncoder.encode(new DynamicArray<Utf8String>(Utf8String.class, values));
    DynamicArray<Utf8String> decoded =
        TypeDecoder.decodeDynamicArray(
            encoded, 0, new TypeReference<DynamicArray<Utf8String>>() {});
    assertEquals(64, decoded.getValue().size());
  }

  @Test
  public void repeatedDecodesOnOneThreadDoNotAccumulate() {
    // Anything carried over from the previous decode would make this one look over-spent.
    for (int i = 0; i < 5; i++) {
      decodeLegitimateArray();
    }
  }

  @Test
  public void rejectedDecodeLeavesNothingBehindOnTheThread() {
    // The dangerous ordering: a decode aborts part-way, then the same thread serves the
    // next request. Interleaved so a leak shows up on the very next decode.
    String malicious = aliasedStringTailPayload();
    for (int i = 0; i < 3; i++) {
      assertThrows(
          IllegalArgumentException.class,
          () -> TypeDecoder.decodeDynamicArray(
              malicious, 0, new TypeReference<DynamicArray<Utf8String>>() {}));
      decodeLegitimateArray();
    }
  }

  @Test
  public void pooledThreadIsNotPoisonedByAnEarlierRejection() throws Exception {
    // Two "requests" forced onto one pooled thread, the first one rejected.
    ExecutorService pool = Executors.newSingleThreadExecutor();
    try {
      final String malicious = aliasedStringTailPayload();
      pool.submit(
          () -> {
            assertThrows(
                IllegalArgumentException.class,
                () -> TypeDecoder.decodeDynamicArray(
                    malicious, 0, new TypeReference<DynamicArray<Utf8String>>() {}));
            return null;
          })
          .get();
      pool.submit(
          () -> {
            decodeLegitimateArray();
            return null;
          })
          .get();
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  public void concurrentDecodesDoNotInterfere() throws Exception {
    final int threads = 8;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    try {
      List<Future<?>> futures = new ArrayList<Future<?>>();
      for (int i = 0; i < threads * 8; i++) {
        futures.add(
            pool.submit(
                () -> {
                  decodeLegitimateArray();
                  return null;
                }));
      }
      for (Future<?> future : futures) {
        // Rethrows whatever the worker threw, so shared state surfaces as a failure here.
        future.get();
      }
    } finally {
      pool.shutdownNow();
    }
  }
}
