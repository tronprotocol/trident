package org.tron.trident.abi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.tron.trident.abi.Utils.convert;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.AbiV2TestFixture.Nazz;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.CustomError;
import org.tron.trident.abi.datatypes.DynamicArray;
import org.tron.trident.abi.datatypes.DynamicStruct;
import org.tron.trident.abi.datatypes.StaticStruct;
import org.tron.trident.abi.datatypes.Utf8String;
import org.tron.trident.abi.datatypes.generated.Uint256;

public class CustomErrorEncoderTest {
  @Test
  public void testCalculateSignatureHash() {
    assertEquals(
        CustomErrorEncoder.calculateSignatureHash("InvalidAccess(address,string,uint256)"),
        ("cb5157bf1b439b9573ea7a95f7c00cc33f832ed728345c2bd29146ce58bbab57"));

    assertEquals(
        CustomErrorEncoder.calculateSignatureHash("RandomError(address[],bytes)"),
        ("bf37b77ddf0fbbf29ee6a3ebda3d177c2d438123b10571806c57958230d9f905"));
  }

  @Test
  public void testEncode() {
    CustomError error =
        new CustomError(
            "InvalidAccess",
            Arrays.<TypeReference<?>>asList(
                new TypeReference<Address>() {},
                new TypeReference<Utf8String>() {},
                new TypeReference<Uint256>() {}));

    assertEquals(
        CustomErrorEncoder.encode(error),
        "cb5157bf1b439b9573ea7a95f7c00cc33f832ed728345c2bd29146ce58bbab57");
  }

  @Test
  public void testBuildErrorSignature() {
    List<TypeReference<?>> parameters =
        Arrays.<TypeReference<?>>asList(
            new TypeReference<Address>() {},
            new TypeReference<Utf8String>() {},
            new TypeReference<Uint256>() {});

    assertEquals(
        "InvalidAccess(address,string,uint256)",
        CustomErrorEncoder.buildErrorSignature("InvalidAccess", convert(parameters)));
  }

  @Test
  void testBuildErrorSignatureWithDynamicStructs() {
    List<TypeReference<?>> parameters =
        Arrays.asList(
            new TypeReference<AbiV2TestFixture.Nazz>() {},
            new TypeReference<AbiV2TestFixture.Foo>() {});

    assertEquals(
        "DynamicStructError((((string,string)[])[],uint256),(string,string))",
        CustomErrorEncoder.buildErrorSignature("DynamicStructError", convert(parameters)));
  }

  @Test
  void testBuildErrorSignatureWithDynamicArrays() {
    List<TypeReference<?>> parameters =
        Arrays.asList(new TypeReference<DynamicArray<Nazz>>() {});

    assertEquals(
        "DynamicArrayError((((string,string)[])[],uint256)[])",
        CustomErrorEncoder.buildErrorSignature("DynamicArrayError", convert(parameters)));
  }

  @Test
  void testBuildErrorSignatureWithInnerTypesTuple() throws ClassNotFoundException {
    List<TypeReference<?>> tupleFields =
        Arrays.asList(
            TypeReference.makeTypeReference("uint256"),
            TypeReference.makeTypeReference("string"));
    List<TypeReference<?>> parameters =
        Arrays.asList(new TypeReference<DynamicStruct>(false, tupleFields) {
        });

    assertEquals(
        "TupleError((uint256,string))",
        CustomErrorEncoder.buildErrorSignature("TupleError", convert(parameters)));
  }

  @Test
  void testBuildErrorSignatureWithInnerTypesStaticTuple() throws ClassNotFoundException {
    List<TypeReference<?>> tupleFields =
        Arrays.asList(
            TypeReference.makeTypeReference("uint256"),
            TypeReference.makeTypeReference("address"));
    List<TypeReference<?>> parameters =
        Arrays.asList(new TypeReference<StaticStruct>(false, tupleFields) {
        });

    assertEquals(
        "StaticTupleError((uint256,address))",
        CustomErrorEncoder.buildErrorSignature("StaticTupleError", convert(parameters)));
  }

}
