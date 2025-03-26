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

package org.tron.trident.abi.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AddressTest {

  @Test
  public void testToString() {
//    assertEquals(
//        new Address("4152b08330e05d731e38c856c1043288f7d9744").toString(),
//        ("T9yKC9LCoVvmhaFxKcdK9iL18TUWtyFtjh")); //39
    assertEquals(
        new Address("T9yKC9LCoVvmhaFxKcdK9iL18TUWtyFtjh").toString(),
        ("T9yKC9LCoVvmhaFxKcdK9iL18TUWtyFtjh"));
    assertEquals(
        new Address("0x52b08330e05d731e38c856c1043288f7d9744").toString(),//37, padding 0 head
        ("T9yKC9LCoVvmhaFxKcdK9iL18TUWtyFtjh"));
    assertEquals(
        new Address("0x052b08330e05d731e38c856c1043288f7d9744").toString(),//38
        ("T9yKC9LCoVvmhaFxKcdK9iL18TUWtyFtjh"));
    assertEquals(new Address("0x00052b08330e05d731e38c856c1043288f7d9744").toString(),
        ("T9yKC9LCoVvmhaFxKcdK9iL18TUWtyFtjh"));

    assertEquals(new Address("41A9BC828A3005B9A3B909F2CC5C2A54794DE05F").toString(),
        ("TFxQG1y8MYdj6PURDhG22gJFUyt72TfDfg"));
    assertEquals(new Address("4141A9BC828A3005B9A3B909F2CC5C2A54794DE05F").toString(),//double 41
        ("TFxQG1y8MYdj6PURDhG22gJFUyt72TfDfg"));
  }
}
