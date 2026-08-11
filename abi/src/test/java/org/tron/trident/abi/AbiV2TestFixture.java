/*
 * Copyright 2020 Web3 Labs Ltd.
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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.DynamicArray;
import org.tron.trident.abi.datatypes.DynamicBytes;
import org.tron.trident.abi.datatypes.DynamicStruct;
import org.tron.trident.abi.datatypes.Event;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.StaticStruct;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.abi.datatypes.Utf8String;
import org.tron.trident.abi.datatypes.generated.StaticArray1;
import org.tron.trident.abi.datatypes.generated.StaticArray2;
import org.tron.trident.abi.datatypes.generated.StaticArray3;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.abi.datatypes.generated.Uint32;
import org.tron.trident.abi.datatypes.reflection.Parameterized;

public class AbiV2TestFixture {

  public static final String FUNC_GETBAR = "getBar";

  public static final String FUNC_GETFOO = "getFoo";

  public static final String FUNC_GETFOOBAR = "getFooBar";

  public static final String FUNC_GETFOOBARBAR = "getFooBarBar";

  public static final String FUNC_GETFOOFOOBARBAR = "getFooFooBarBar";

  public static final String FUNC_GETNARBARBARFUZZFOONARFUZZNUUFOOFUZZFUNCTION =
      "getNarBarBarFuzzFooNarFuzzNuuFooFuzz";

  public static final String FUNC_GETFOOUINT = "getFooUint";

  public static final String FUNC_GETFOOSTATICARRAY1 = "getFooStaticArray1";

  public static final String FUNC_GETFOOSTATICARRAY2 = "getFooStaticArray2";

  public static final String FUNC_GETFOOSTATICARRAY3 = "getFooStaticArray3";

  public static final String FUNC_GETFOODYNAMICARRAY = "getFooDynamicArray";

  public static final String FUNC_GETNARBARFOONARFOODYNAMICARRAY = "getNarBarFooNarFooDynamicArrays";

  public static final String FUNC_IDNARBARFOONARFOODYNAMICARRAY = "idNarBarFooNarFooDynamicArrays";

  public static final String FUNC_GETBARDYNAMICARRAY = "getBarDynamicArray";

  public static final String FUNC_GETBARSTATICARRAY = "getBarStaticArray";

  public static final String FUNC_SETBARSTATICARRAY = "setBarStaticArray";

  public static final String FUNC_SETBARDYNAMICARRAY = "setBarDynamicArray";

  public static final String FUNC_GETNARDYNAMICARRAY = "getNarDynamicArray";

  public static final String FUNC_GETNARSTATICARRAY = "getNarStaticArray";

  public static final String FUNC_SETFOODYNAMICARRAY = "setFooDynamicArray";

  public static final String FUNC_GETFOOMULTIPLESTATICARRAY = "getFooMultipleStaticArray";

  public static final String FUNC_GETFOOMULTIPLEDYNAMICARRAY = "getFooMultipleDynamicArray";

  public static final String FUNC_IDNARBARFOONARFOOARRAYS = "idNarBarFooNarFooArrays";

  public static final String FUNC_IDBARNARFOONARFOOARRAYS = "idBarNarFooNarFooArrays";

  public static final String FUNC_GETFOOMULTIPLEDYNAMICSTATICARRAY =
      "getFooMultipleDynamicStaticArray";

  public static final String FUNC_GETFUZZ = "getFuzz";

  public static final String FUNC_GETFUZZFUZZ = "getFuzzFuzz";

  public static final String FUNC_GETNAZ = "getNaz";

  public static final String FUNC_GETNAR = "getNar";

  public static final String FUNC_SETBAR = "setBar";

  public static final String FUNC_SETBAZ = "setBaz";

  public static final String FUNC_SETBOZ = "setBoz";

  public static final String FUNC_SETFOO = "setFoo";

  public static final String FUNC_SETFUZZ = "setFuzz";

  public static final String FUNC_SETNAZ = "setNaz";

  public static final String FUNC_SETNUU = "setNuu";

  public static final String FUNC_SETWIZ = "setWiz";

  public static final String FUNC_SETQUX = "setQux";

  public static final String FUNC_GETQUX = "getQux";

  public static final String FUNC_addDynamicBytesArray = "addDynamicBytesArray";

  public static final String FUNC_setArrayOfStructWithArraysFunction =
      "setArrayOfStructWithArraysFunction";

  public static final String FUNC_SETGETMULTIDIMSTATICARRAY = "setGetMultiDimStaticArray";
  
  public static final String FUNC_SETGETMULTIDIMDYNAMICARRAY = "setGetMultiDimDynamicArray";

  public static final String FUNC_setMultiDimStaticArrayWithUtf8StringSubType
      = "setMultiDimStaticArrayWithUtf8StringSubType";

  public static class Foo extends DynamicStruct {
    public String id;

    public String name;

    public Foo(String id, String name) {
      super(
          new Utf8String(id),
          new Utf8String(name));
      this.id = id;
      this.name = name;
    }

    public Foo(Utf8String id, Utf8String name) {
      super(id, name);
      this.id = id.getValue();
      this.name = name.getValue();
    }

    @Override
    public String toString() {
      return "Foo{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
    }
  }

  public static final Function setFooFunction =
      new Function(
          FUNC_SETFOO,
          Arrays.<Type>asList(new Foo("id", "name")),
          Collections.emptyList());

  public static final Function getFooFunction =
      new Function(
          FUNC_GETFOO, Arrays.<Type>asList(), Arrays.asList(new TypeReference<Foo>() {}));

  public static final Function getFooUintFunction =
      new Function(
          FUNC_GETFOOUINT,
          Arrays.<Type>asList(),
          Arrays.asList(new TypeReference<Foo>() {}, new TypeReference<Uint256>() {}));

  public static final Function getFooStaticArray1Function =
      new Function(
          FUNC_GETFOOSTATICARRAY1,
          Arrays.<Type>asList(),
          Arrays.asList(new TypeReference<StaticArray1<Foo>>() {}));

  public static final Function getFooStaticArray2Function =
      new Function(
          FUNC_GETFOOSTATICARRAY2,
          Arrays.<Type>asList(),
          Arrays.asList(new TypeReference<StaticArray2<Foo>>() {}));

  public static final Function getFooStaticArray3Function =
      new Function(
          FUNC_GETFOOSTATICARRAY3,
          Arrays.<Type>asList(),
          Arrays.asList(new TypeReference<StaticArray3<Foo>>() {}));

  public static final Function getFooDynamicArrayFunction =
      new Function(
          FUNC_GETFOODYNAMICARRAY,
          Arrays.<Type>asList(),
          Arrays.asList(new TypeReference<DynamicArray<Foo>>() {}));

  public static final Function getNarBarFooNarFooDynamicArrayFunction =
      new Function(
          FUNC_GETNARBARFOONARFOODYNAMICARRAY,
          Arrays.<Type>asList(),
          Arrays.asList(
              new TypeReference<StaticArray3<Nar>>() {},
              new TypeReference<StaticArray3<Bar>>() {},
              new TypeReference<DynamicArray<Foo>>() {},
              new TypeReference<DynamicArray<Nar>>() {},
              new TypeReference<StaticArray3<Foo>>() {}));

  public static final Function idNarBarFooNarFooDynamicArrayFunction =
      new Function(
          FUNC_IDNARBARFOONARFOODYNAMICARRAY,
          Arrays.asList(
              new StaticArray3<>(
                  AbiV2TestFixture.Nar.class,
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("", ""))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo")))),
              new StaticArray3<>(
                  AbiV2TestFixture.Bar.class,
                  new AbiV2TestFixture.Bar(BigInteger.ZERO, BigInteger.ZERO),
                  new AbiV2TestFixture.Bar(
                      BigInteger.valueOf(123), BigInteger.valueOf(123)),
                  new AbiV2TestFixture.Bar(BigInteger.ZERO, BigInteger.ZERO)),
              new DynamicArray<>(
                  AbiV2TestFixture.Foo.class,
                  new AbiV2TestFixture.Foo("id", "name")),
              new DynamicArray<>(
                  AbiV2TestFixture.Nar.class,
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("", "")))),
              new StaticArray3<>(
                  AbiV2TestFixture.Foo.class,
                  new AbiV2TestFixture.Foo("id", "name"),
                  new AbiV2TestFixture.Foo("id", "name"),
                  new AbiV2TestFixture.Foo("id", "name"))),
          Arrays.asList(
              new TypeReference<StaticArray3<Nar>>() {},
              new TypeReference<StaticArray3<Bar>>() {},
              new TypeReference<DynamicArray<Foo>>() {},
              new TypeReference<DynamicArray<Nar>>() {},
              new TypeReference<StaticArray3<Foo>>() {}));

  public static final Function getBarDynamicArrayFunction =
      new Function(
          FUNC_GETBARDYNAMICARRAY,
          Arrays.<Type>asList(),
          Arrays.asList(new TypeReference<DynamicArray<Bar>>() {}));

  public static final Function getBarStaticArrayFunction =
      new Function(
          FUNC_GETBARSTATICARRAY,
          Arrays.<Type>asList(),
          Arrays.asList(new TypeReference<StaticArray3<Bar>>() {}));

  @SuppressWarnings("unchecked")
  public static final Function setBarStaticArrayFunction =
      new Function(
          FUNC_SETBARSTATICARRAY,
          Arrays.<Type>asList(
              new StaticArray3(
                  AbiV2TestFixture.Bar.class,
                  new AbiV2TestFixture.Bar(
                      BigInteger.valueOf(0), BigInteger.valueOf(0)),
                  new AbiV2TestFixture.Bar(
                      BigInteger.valueOf(123), BigInteger.valueOf(123)),
                  new AbiV2TestFixture.Bar(
                      BigInteger.valueOf(0), BigInteger.valueOf(0)))),
          Arrays.asList());

  @SuppressWarnings("unchecked")
  public static final Function setBarDynamicArrayFunction =
      new Function(
          FUNC_SETBARDYNAMICARRAY,
          Arrays.<Type>asList(
              new DynamicArray(
                  AbiV2TestFixture.Bar.class,
                  new AbiV2TestFixture.Bar(
                      BigInteger.valueOf(0), BigInteger.valueOf(0)),
                  new AbiV2TestFixture.Bar(
                      BigInteger.valueOf(123), BigInteger.valueOf(123)),
                  new AbiV2TestFixture.Bar(
                      BigInteger.valueOf(0), BigInteger.valueOf(0)))),
          Arrays.asList());

  public static final Function getNarDynamicArrayFunction =
      new Function(
          FUNC_GETNARDYNAMICARRAY,
          Arrays.<Type>asList(),
          Arrays.asList(new TypeReference<DynamicArray<Nar>>() {}));

  public static final Function getNarStaticArrayFunction =
      new Function(
          FUNC_GETNARSTATICARRAY,
          Arrays.<Type>asList(),
          Arrays.asList(new TypeReference<StaticArray3<Nar>>() {}));

  public static final Function getFooMultipleStaticArrayFunction =
      new Function(
          FUNC_GETFOOMULTIPLESTATICARRAY,
          Arrays.<Type>asList(),
          Arrays.asList(
              new TypeReference<StaticArray3<Foo>>() {},
              new TypeReference<StaticArray2<Foo>>() {}));

  public static final Function getFooMultipleDynamicArrayFunction =
      new Function(
          FUNC_GETFOOMULTIPLEDYNAMICARRAY,
          Arrays.<Type>asList(),
          Arrays.asList(
              new TypeReference<DynamicArray<Foo>>() {},
              new TypeReference<DynamicArray<Foo>>() {}));

  public static final Function idNarBarFooNarFooArraysFunction =
      new Function(
          FUNC_IDNARBARFOONARFOOARRAYS,
          Arrays.<Type>asList(
              new DynamicArray<>(
                  AbiV2TestFixture.Nar.class,
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("", "")))),
              new StaticArray3<>(
                  AbiV2TestFixture.Bar.class,
                  new AbiV2TestFixture.Bar(BigInteger.ZERO, BigInteger.ZERO),
                  new AbiV2TestFixture.Bar(
                      BigInteger.valueOf(123), BigInteger.valueOf(123)),
                  new AbiV2TestFixture.Bar(BigInteger.ZERO, BigInteger.ZERO)),
              new DynamicArray<>(
                  AbiV2TestFixture.Foo.class,
                  new AbiV2TestFixture.Foo("id", "name")),
              new DynamicArray<>(
                  AbiV2TestFixture.Nar.class,
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("", "")))),
              new DynamicArray<>(
                  AbiV2TestFixture.Foo.class,
                  new AbiV2TestFixture.Foo("id", "name"))),
          Arrays.<TypeReference<?>>asList(
              new TypeReference<DynamicArray<Nar>>() {},
              new TypeReference<StaticArray3<Bar>>() {},
              new TypeReference<DynamicArray<Foo>>() {},
              new TypeReference<DynamicArray<Nar>>() {},
              new TypeReference<DynamicArray<Foo>>() {}));

  public static final Function idNarBarFooNarFooArraysFunction2 =
      new Function(
          FUNC_IDNARBARFOONARFOOARRAYS,
          Arrays.<Type>asList(
              new StaticArray3<>(
                  AbiV2TestFixture.Nar.class,
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("", ""))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo")))),
              new DynamicArray<>(
                  AbiV2TestFixture.Bar.class,
                  new AbiV2TestFixture.Bar(
                      BigInteger.valueOf(123), BigInteger.valueOf(123)),
                  new AbiV2TestFixture.Bar(
                      BigInteger.valueOf(12), BigInteger.valueOf(33)),
                  new AbiV2TestFixture.Bar(BigInteger.ZERO, BigInteger.ZERO)),
              new DynamicArray<>(
                  AbiV2TestFixture.Foo.class,
                  new AbiV2TestFixture.Foo("id", "name")),
              new DynamicArray<>(
                  AbiV2TestFixture.Nar.class,
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("", "")))),
              new StaticArray3<>(
                  AbiV2TestFixture.Foo.class,
                  new AbiV2TestFixture.Foo("id", "name"),
                  new AbiV2TestFixture.Foo("id", "name"),
                  new AbiV2TestFixture.Foo("id", "name"))),
          Arrays.<TypeReference<?>>asList(
              new TypeReference<StaticArray3<Nar>>() {},
              new TypeReference<DynamicArray<Bar>>() {},
              new TypeReference<DynamicArray<Foo>>() {},
              new TypeReference<DynamicArray<Nar>>() {},
              new TypeReference<StaticArray3<Foo>>() {}));

  public static final Function idBarNarFooNarFooArraysFunction =
      new Function(
          FUNC_IDBARNARFOONARFOOARRAYS,
          Arrays.<Type>asList(
              new StaticArray3<>(
                  AbiV2TestFixture.Bar.class,
                  new AbiV2TestFixture.Bar(BigInteger.ZERO, BigInteger.ZERO),
                  new AbiV2TestFixture.Bar(
                      BigInteger.valueOf(123), BigInteger.valueOf(123)),
                  new AbiV2TestFixture.Bar(BigInteger.ZERO, BigInteger.ZERO)),
              new StaticArray3<>(
                  AbiV2TestFixture.Nar.class,
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("", ""))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo")))),
              new DynamicArray<>(
                  AbiV2TestFixture.Foo.class,
                  new AbiV2TestFixture.Foo("id", "name")),
              new DynamicArray<>(
                  AbiV2TestFixture.Nar.class,
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("4", "nestedFoo"))),
                  new AbiV2TestFixture.Nar(
                      new AbiV2TestFixture.Nuu(
                          new AbiV2TestFixture.Foo("", "")))),
              new StaticArray3<>(
                  AbiV2TestFixture.Foo.class,
                  new AbiV2TestFixture.Foo("id", "name"),
                  new AbiV2TestFixture.Foo("id", "name"),
                  new AbiV2TestFixture.Foo("id", "name"))),
          Arrays.<TypeReference<?>>asList(
              new TypeReference<StaticArray3<Bar>>() {},
              new TypeReference<StaticArray3<Nar>>() {},
              new TypeReference<DynamicArray<Foo>>() {},
              new TypeReference<DynamicArray<Nar>>() {},
              new TypeReference<StaticArray3<Foo>>() {}));

  public static final Function getFooMultipleDynamicStaticArrayFunction =
      new Function(
          FUNC_GETFOOMULTIPLEDYNAMICSTATICARRAY,
          Arrays.<Type>asList(),
          Arrays.<TypeReference<?>>asList(
              new TypeReference<StaticArray3<Foo>>() {},
              new TypeReference<DynamicArray<Foo>>() {}));

  public static class Bar extends StaticStruct {
    public BigInteger id;

    public BigInteger data;

    public Bar(BigInteger id, BigInteger data) {
      super(
          new Uint256(id),
          new Uint256(data));
      this.id = id;
      this.data = data;
    }

    public Bar(Uint256 id, Uint256 data) {
      super(id, data);
      this.id = id.getValue();
      this.data = data.getValue();
    }

    @Override
    public String toString() {
      return "Bar{" + "id='" + id + '\'' + ", data='" + data + '\'' + '}';
    }
  }

  public static final Function setBarFunction =
      new Function(
          FUNC_SETBAR,
          Arrays.<Type>asList(new Bar(BigInteger.ONE, BigInteger.TEN)),
          Collections.<TypeReference<?>>emptyList());

  public static final Function getBarFunction =
      new Function(
          FUNC_GETBAR,
          Arrays.<Type>asList(),
          Arrays.<TypeReference<?>>asList(new TypeReference<Bar>() {}));

  public static final Function getFooBarFunction =
      new Function(
          FUNC_GETFOOBAR,
          Arrays.<Type>asList(),
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Foo>() {}, new TypeReference<Bar>() {}));

  public static final Function getFooBarBarFunction =
      new Function(
          FUNC_GETFOOBARBAR,
          Arrays.<Type>asList(),
          Arrays.asList(
              new TypeReference<Foo>() {},
              new TypeReference<Bar>() {},
              new TypeReference<Bar>() {}));

  public static final Function getFooFooBarBarFunction =
      new Function(
          FUNC_GETFOOFOOBARBAR,
          Arrays.<Type>asList(),
          Arrays.asList(
              new TypeReference<Foo>() {},
              new TypeReference<Foo>() {},
              new TypeReference<Bar>() {},
              new TypeReference<Bar>() {}));

  public static final Function
      getNarBarBarFuzzFooNarFuzzNuuFooFuzzFunction =
      new Function(
          FUNC_GETNARBARBARFUZZFOONARFUZZNUUFOOFUZZFUNCTION,
          Arrays.<Type>asList(),
          Arrays.asList(
              new TypeReference<Nar>() {},
              new TypeReference<Bar>() {},
              new TypeReference<Bar>() {},
              new TypeReference<Fuzz>() {},
              new TypeReference<Foo>() {},
              new TypeReference<Nar>() {},
              new TypeReference<Fuzz>() {},
              new TypeReference<Nuu>() {},
              new TypeReference<Foo>() {},
              new TypeReference<Fuzz>() {}));

  public static class Baz extends DynamicStruct {
    public String id;

    public BigInteger data;

    public Baz(String id, BigInteger data) {
      super(
          new Utf8String(id),
          new Uint256(data));
      this.id = id;
      this.data = data;
    }

    public Baz(Utf8String id, Uint256 data) {
      super(id, data);
      this.id = id.getValue();
      this.data = data.getValue();
    }
  }

  public static final Function setBazFunction =
      new Function(
          FUNC_SETBAZ,
          Arrays.<Type>asList(new Baz("id", BigInteger.ONE)),
          Collections.<TypeReference<?>>emptyList());

  public static class Boz extends DynamicStruct {
    public BigInteger data;

    public String id;

    public Boz(BigInteger data, String id) {
      super(
          new Uint256(data),
          new Utf8String(id));
      this.data = data;
      this.id = id;
    }

    public Boz(Uint256 data, Utf8String id) {
      super(data, id);
      this.data = data.getValue();
      this.id = id.getValue();
    }
  }

  public static final Function setBozFunction =
      new Function(
          FUNC_SETBOZ,
          Arrays.<Type>asList(new Boz(BigInteger.ONE, "id")),
          Collections.<TypeReference<?>>emptyList());

  public static final Function getBozFunction =
      new Function(
          FUNC_SETBOZ,
          Collections.<Type>emptyList(),
          Arrays.<TypeReference<?>>asList(new TypeReference<Boz>() {}));

  public static class Fuzz extends StaticStruct {
    public Bar bar;

    public BigInteger data;

    public Fuzz(Bar bar, BigInteger data) {
      super(bar, new Uint256(data));
      this.bar = bar;
      this.data = data;
    }

    public Fuzz(Bar bar, Uint256 data) {
      super(bar, data);
      this.bar = bar;
      this.data = data.getValue();
    }
  }

  public static final Function setFooDynamicArrayFunction =
      new Function(
          FUNC_SETFOODYNAMICARRAY,
          Collections.singletonList(
              new DynamicArray<>(
                  Foo.class,
                  new Foo("", ""),
                  new Foo("id", "name"),
                  new Foo("", ""))),
          Collections.emptyList());

  public static final Function setDoubleFooStaticArrayFunction =
      new Function(
          FUNC_SETFOODYNAMICARRAY,
          Arrays.<Type>asList(new Foo("", ""), new Foo("id", "name"), new Foo("", "")),
          Collections.emptyList());

  public static final Function setFuzzFunction =
      new Function(
          FUNC_SETFUZZ,
          Arrays.<Type>asList(
              new Fuzz(new Bar(BigInteger.ONE, BigInteger.TEN), BigInteger.ONE)),
          Collections.emptyList());

  public static final Function getFuzzFunction =
      new Function(
          FUNC_GETFUZZ,
          Arrays.<Type>asList(),
          Arrays.asList(new TypeReference<Fuzz>() {}));

  public static final Function getFuzzFuzzFunction =
      new Function(
          FUNC_GETFUZZFUZZ,
          Arrays.<Type>asList(),
          Arrays.asList(new TypeReference<Fuzz>() {}, new TypeReference<Fuzz>() {}));

  public static class Nuu extends DynamicStruct {
    public Foo foo;

    public Nuu(Foo foo) {
      super(foo);
      this.foo = foo;
    }
  }

  public static final Function setNuuFunction =
      new Function(
          FUNC_SETNUU,
          Arrays.<Type>asList(new Nuu(new Foo("id", "name"))),
          Collections.<TypeReference<?>>emptyList());

  public static final Function getNuuFunction =
      new Function(
          FUNC_SETNUU,
          Collections.<Type>emptyList(),
          Arrays.<TypeReference<?>>asList(new TypeReference<Nuu>() {}));

  public static class Nar extends DynamicStruct {
    public Nuu nuu;

    public Nar(Nuu nuu) {
      super(nuu);
      this.nuu = nuu;
    }
  }

  public static class Naz extends DynamicStruct {
    public Nar nar;

    public BigInteger data;

    public Naz(Nar nar, BigInteger data) {
      super(nar, new Uint256(data));
      this.nar = nar;
      this.data = data;
    }

    public Naz(Nar nar, Uint256 data) {
      super(nar, data);
      this.nar = nar;
      this.data = data.getValue();
    }
  }

  public static class Nazzy extends DynamicStruct {
    public List<Foo> foo;

    public Nazzy(List<Foo> foo) {
      super(new DynamicArray<>(Foo.class, foo));
      this.foo = foo;
    }

    public Nazzy(@Parameterized(type = Foo.class) DynamicArray<Foo> foo) {
      super(foo);
      this.foo = foo.getValue();
    }

    @Override
    public String toString() {
      return "Nazzy{" + "foo=" + foo + '}';
    }
  }

  public static class Barr extends DynamicStruct {
    public List<Bar> bars;

    public BigInteger data;

    public Barr(List<Bar> bars, BigInteger data) {
      super(
          new DynamicArray<>(Bar.class, bars),
          new Uint256(data));
      this.bars = bars;
      this.data = data;
    }

    public Barr(@Parameterized(type = Bar.class) DynamicArray<Bar> bars, Uint256 data) {
      super(bars, data);
      this.bars = bars.getValue();
      this.data = data.getValue();
    }
  }

  public static class Nazz extends DynamicStruct {
    public List<Nazzy> nazzy;

    public BigInteger data;

    public Nazz(List<Nazzy> nazzy, BigInteger data) {
      super(
          new DynamicArray<>(Nazzy.class, nazzy),
          new Uint256(data));
      this.nazzy = nazzy;
      this.data = data;
    }

    public Nazz(@Parameterized(type = Nazzy.class) DynamicArray<Nazzy> nazzy, Uint256 data) {
      super(nazzy, data);
      this.nazzy = nazzy.getValue();
      this.data = data.getValue();
    }

    @Override
    public String toString() {
      return "Nazz{" + "nazzy=" + nazzy + ", data=" + data + '}';
    }
  }

  public static final Function setNazFunction =
      new Function(
          FUNC_SETNAZ,
          Arrays.<Type>asList(
              new Naz(new Nar(new Nuu(new Foo("id", "name"))), BigInteger.ONE)),
          Collections.emptyList());

  public static final Function getNazzFunction =
      new Function(
          FUNC_GETNAZ,
          Arrays.<Type>asList(),
          Arrays.asList(new TypeReference<Nazz>() {}));

  public static final Event nazzEvent =
      new Event(
          "nazzEvent",
          Arrays.asList(new TypeReference<Nazz>() {}, new TypeReference<Foo>() {}));

  public static final Event nazzEvent2 =
      new Event("nazzEvent2", Arrays.asList(new TypeReference<DynamicArray<Nazz>>() {}));

  public static final Function getNarFunction =
      new Function(
          FUNC_GETNAR, Arrays.<Type>asList(), Arrays.asList(new TypeReference<Nar>() {}));

  public static class Wiz extends DynamicStruct {
    public Foo foo;

    public String data;

    public Wiz(Foo foo, String data) {
      super(foo, new Utf8String(data));
      this.foo = foo;
      this.data = data;
    }

    public Wiz(Foo foo, Utf8String data) {
      super(foo, data);
      this.foo = foo;
      this.data = data.getValue();
    }
  }

  public static final Function setWizFunction =
      new Function(
          FUNC_SETWIZ,
          Arrays.<Type>asList(new Wiz(new Foo("id", "name"), "data")),
          Collections.emptyList());

  public static class Qux extends DynamicStruct {
    public Bar bar;

    public String data;

    public Qux(Bar bar, String data) {
      super(bar, new Utf8String(data));
      this.bar = bar;
      this.data = data;
    }

    public Qux(Bar bar, Utf8String data) {
      super(bar, data);
      this.bar = bar;
      this.data = data.getValue();
    }
  }

  public static final Function setQuxFunction =
      new Function(
          FUNC_SETQUX,
          Arrays.<Type>asList(new Qux(new Bar(BigInteger.ONE, BigInteger.TEN), "data")),
          Collections.emptyList());

  public static final Function getQuxFunction =
      new Function(
          FUNC_GETQUX, Arrays.<Type>asList(), Arrays.asList(new TypeReference<Qux>() {}));

  public static class BytesStruct extends DynamicStruct {
    public byte[] pubkey;

    public BigInteger something;

    public byte[] metadata;

    public BytesStruct(byte[] pubkey, BigInteger something, byte[] metadata) {
      super(
          new DynamicBytes(pubkey),
          new Uint32(something),
          new DynamicBytes(metadata));
      this.pubkey = pubkey;
      this.something = something;
      this.metadata = metadata;
    }

    public BytesStruct(DynamicBytes pubkey, Uint32 something, DynamicBytes metadata) {
      super(pubkey, something, metadata);
      this.pubkey = pubkey.getValue();
      this.something = something.getValue();
      this.metadata = metadata.getValue();
    }
  }

  public static final Function addDynamicBytesArrayFunction =
      new Function(
          FUNC_addDynamicBytesArray,
          Arrays.<Type>asList(
              new BytesStruct(
                  "dynamic".getBytes(StandardCharsets.UTF_8),
                  BigInteger.ZERO,
                  "Bytes".getBytes(StandardCharsets.UTF_8))),
          Collections.<TypeReference<?>>emptyList());

  public static class ArrayStruct extends DynamicStruct {
    public BigInteger id;

    public List<String> addresses;

    public ArrayStruct(BigInteger id, List<String> addresses) {
      super(
          new Uint256(id),
          new DynamicArray<Address>(
              Address.class,
              Utils.typeMap(
                  addresses, Address.class)));
      this.id = id;
      this.addresses = addresses;
    }

    public ArrayStruct(Uint256 id, DynamicArray<Address> addresses) {
      super(id, addresses);
      this.id = id.getValue();
      this.addresses =
          addresses.getValue().stream()
              .map(v -> v.getValue())
              .collect(Collectors.toList());
    }
  }

  public static final Function setArrayOfStructWithArraysFunction =
      new Function(
          FUNC_setArrayOfStructWithArraysFunction,
          Arrays.<Type>asList(
              new DynamicArray<ArrayStruct>(
                  ArrayStruct.class,
                  Arrays.asList(
                      new ArrayStruct(
                          BigInteger.ONE,
                          Arrays.asList(
                              "0x0000000000000000000000000000000000000000",
                              "0x1111111111111111111111111111111111111111")),
                      new ArrayStruct(
                          BigInteger.TEN,
                          Arrays.asList(
                              "0x2222222222222222222222222222222222222222",
                              "0x3333333333333333333333333333333333333333"))))),
          Collections.<TypeReference<?>>emptyList());

  public static final Function setGetMultiDimStaticArrayFunction =
      new Function(FUNC_SETGETMULTIDIMSTATICARRAY,
          Collections.singletonList(new StaticArray3<>(
              (Class)StaticArray2.class,
              new StaticArray2<>(Uint256.class, new Uint256(1), new Uint256(2)),
              new StaticArray2<>(Uint256.class, new Uint256(3), new Uint256(4)),
              new StaticArray2<>(Uint256.class, new Uint256(5), new Uint256(6)))),
          Collections.singletonList(new TypeReference<StaticArray3<StaticArray2<Uint256>>>() {})
      );

  public static final Function setGetMultiDimDynamicArrayFunction =
      new Function(FUNC_SETGETMULTIDIMDYNAMICARRAY,
          Collections.singletonList(new DynamicArray<>(
              (Class)DynamicArray.class,
              new DynamicArray<>(
                  (Class)DynamicArray.class,
                  new DynamicArray<>(
                  Uint256.class, 
                      new Uint256(1))),
              new DynamicArray<>(
                  (Class)DynamicArray.class,
                  new DynamicArray<>(
                    Uint256.class, 
                      new Uint256(2), new Uint256(3))),
              new DynamicArray<>(
                  (Class)DynamicArray.class,
                  new DynamicArray<>(
                      Uint256.class, 
                      new Uint256(4), new Uint256(5)),
                  new DynamicArray<>(
                      Uint256.class, 
                      new Uint256(6), new Uint256(7), new Uint256(8))))),
          Collections.singletonList(new TypeReference<DynamicArray<DynamicArray<DynamicArray<Uint256>>>>() {})
      );

  public static Function setMultiDimStaticArrayWithUtf8StringSubTypeFunction() {
    //string[2][2]
    StaticArray2<Utf8String> inner1 = new StaticArray2<>(
        Utf8String.class,
        new Utf8String("1234567890123456789012345678901234567890"
            + "1234567890123456789012345678901234567890"),
        new Utf8String("short1")
    );

    StaticArray2<Utf8String> inner2 = new StaticArray2<>(
        Utf8String.class,
        new Utf8String("short2"),
        new Utf8String("short3")
    );

    @SuppressWarnings("unchecked")
    StaticArray2<StaticArray2<Utf8String>> outer = new StaticArray2<>(
        (Class<StaticArray2<Utf8String>>) (Class<?>) StaticArray2.class,
        inner1,
        inner2
    );

    return new Function(
        FUNC_setMultiDimStaticArrayWithUtf8StringSubType,
        Collections.singletonList(outer),
        Collections.emptyList()
    );
  }

}
