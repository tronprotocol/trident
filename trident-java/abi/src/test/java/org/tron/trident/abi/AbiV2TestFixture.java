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
import java.util.Arrays;
import java.util.Collections;

import org.tron.trident.abi.datatypes.*;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.abi.datatypes.*;

public class AbiV2TestFixture {

    public static final String FUNC_GETBAR = "getBar";

    public static final String FUNC_GETFOO = "getFoo";

    public static final String FUNC_GETFOOBAR = "getFooBar";

    public static final String FUNC_GETFOOUINT = "getFooUint";

    public static final String FUNC_GETFUZZ = "getFuzz";

    public static final String FUNC_GETNAZ = "getNaz";

    public static final String FUNC_SETBAR = "setBar";

    public static final String FUNC_SETBAZ = "setBaz";

    public static final String FUNC_SETBOZ = "setBoz";

    public static final String FUNC_SETFOO = "setFoo";

    public static final String FUNC_SETFUZZ = "setFuzz";

    public static final String FUNC_SETNAZ = "setNaz";

    public static final String FUNC_SETNUU = "setNuu";

    public static final String FUNC_SETWIZ = "setWiz";

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
    }

    public static final Function setFooFunction =
            new Function(
                    FUNC_SETFOO,
                    Arrays.<Type>asList(new Foo("id", "name")),
                    Collections.<TypeReference<?>>emptyList());

    public static final Function getFooFunction =
            new Function(
                    FUNC_GETFOO,
                    Arrays.<Type>asList(),
                    Arrays.<TypeReference<?>>asList(new TypeReference<Foo>() {}));

    public static final Function getFooUintFunction =
            new Function(
                    FUNC_GETFOOUINT,
                    Arrays.<Type>asList(),
                    Arrays.<TypeReference<?>>asList(
                            new TypeReference<Foo>() {}, new TypeReference<Uint256>() {}));

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

    public static final Function setFuzzFunction =
            new Function(
                    FUNC_SETFUZZ,
                    Arrays.<Type>asList(
                            new Fuzz(new Bar(BigInteger.ONE, BigInteger.TEN), BigInteger.ONE)),
                    Collections.<TypeReference<?>>emptyList());

    public static final Function getFuzzFunction =
            new Function(
                    FUNC_GETFUZZ,
                    Arrays.<Type>asList(),
                    Arrays.<TypeReference<?>>asList(new TypeReference<Fuzz>() {}));

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

    public static final Function setNazFunction =
            new Function(
                    FUNC_SETNAZ,
                    Arrays.<Type>asList(
                            new Naz(new Nar(new Nuu(new Foo("id", "name"))), BigInteger.ONE)),
                    Collections.<TypeReference<?>>emptyList());

    public static final Function getNazFunction =
            new Function(
                    FUNC_GETNAZ,
                    Arrays.<Type>asList(),
                    Arrays.<TypeReference<?>>asList(new TypeReference<Naz>() {}));

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
                    Collections.<TypeReference<?>>emptyList());
}
