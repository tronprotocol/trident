# Trident - TRON Java SDK

## Overview

Trident is a lightweight Java SDK for interacting with the TRON blockchain. It provides a simple and efficient way to integrate TRON functionality into your Java applications.

## Features

- Complete implementation of TRON's gRPC interfaces
- Smart contract deployment and interaction
- Wallet key management and address utilities
- Transaction building and signing
- TRC10/TRC20/TRC721 token support

## Adding Trident to your build

Since version 0.11.0, Trident can be built with JDK 1.8 or JDK17 on Gradle 8.5.

The latest version (built with JDK 1.8) can be found on [Maven Central](https://mvnrepository.com/artifact/io.github.tronprotocol/trident).

### Gradle

```groovy
implementation("io.github.tronprotocol:trident:0.11.0")
```

### Maven

Add repo setting:

```xml
<dependency>
  <groupId>io.github.tronprotocol</groupId>
  <artifactId>trident</artifactId>
  <version>0.11.0</version>
</dependency>
```


### Using local build

You can use locally built packages by the following steps:

1. Copy the compiled jar file to your project's `libs` directory
2. Add the following to your project's `build.gradle`:
```groovy
dependencies {
    implementation files('libs/trident-0.11.0.jar')
    implementation "com.google.guava:guava:33.0.0-jre"
    implementation "io.grpc:grpc-netty-shaded:1.75.0"
    implementation "io.grpc:grpc-netty:1.75.0"
    implementation "io.grpc:grpc-okhttp:1.75.0"
    implementation "io.grpc:grpc-protobuf:1.75.0"
    implementation "io.grpc:grpc-stub:1.75.0"
    implementation "com.google.protobuf:protobuf-java-util:3.25.8"
    implementation "org.bouncycastle:bcprov-jdk18on:1.78.1"
    implementation "io.vertx:vertx-core:4.5.21"
    implementation "io.netty:netty-all:4.1.125.Final"
    implementation "com.alibaba.fastjson2:fastjson2:2.0.55"
}
```

## Quick Start

**Initialize client**
```java
// Initialize with TronGrid mainnet 
ApiWrapper client = ApiWrapper.ofMainnet("private_key", "api_key"); //api_key from TronGrid

//Or Shasta test net 
ApiWrapper client = ApiWrapper.ofShasta("private key");

// Or nile testnet
ApiWrapper client = ApiWrapper.ofNile("private_key");

//Initialize with special grpc endpoint
ApiWrapper client = new ApiWrapper("grpc endpoint", "solidity grpc endpoint", "private_key");

// Send TRX
TransactionExtention transactionExtention = client.transfer("fromAddress", "toAddress", 100_000_000L); //100TRX
// Sign
Transaction signedTxn = client.signTransaction(transactionExtention);
// Broadcast
String txId = client.broadcastTransaction(signedTxn);
System.out.println("txId is " + txId);
```

## Documentation

- [Official Documentation](https://tronprotocol.github.io/trident/)


## Build instructions
Trident includes integration tests for running on the Nile testnet. If you want to run test cases involving write operations on the blockchain, such as transfer or deploy contract and so on, please follow the steps:

1. Uncomment the Disabled function in the unit test cases.
```
   //@Disabled("add private key to enable this case")
```
2. Set the tron.private-key and tron.tokenId in the test configuration file in the core directory [here](core/src/test/resources/application-test.properties).


``` 
tron.private-key=xxx
tron.tokenId=1000587
```

**Note:** The account should have at least 1000 TRX, 100 USDT, and 1000 TRC10 token on the Nile testnet. you can get testCoin from [nileex.io](https://nileex.io/join/getJoinPage).

## Contribution

We're very glad and appreciate to have contributions from the community.

Refer to our [contributing guide ](CONTRIBUTING.md)for more information.

## Integrity Check

Starting from version 0.9.2, releases are published to Maven repository and signed with the gpg key:

```
pub: 3149 FCA5 6377 2D11 2624 9C36 CC3F 8CEA 7B0C 74D6
uid: buildtrident@tron.network
```