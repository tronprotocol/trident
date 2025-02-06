# Trident - TRON Java SDK

## Overview

Trident is a lightweight Java SDK for interacting with the TRON blockchain. It provides a simple and efficient way to integrate TRON functionality into your Java applications.

## Features

- Complete implementation of TRON's gRPC interfaces
- Smart contract deployment and interaction
- Wallet key management and address utilities
- Transaction building and signing
- TRC10/TRC20/TRC721 token support

## Adding Guava to your build

Trident-java is compiled with java version 1.8 and gradle 7.6.

### Gradle

```groovy
    implementation("io.github.tronprotocol:trident:0.9.2")
```

### Maven

Add repo setting:

```xml
<dependency>
  <groupId>io.github.tronprotocol</groupId>
  <artifactId>trident</artifactId>
  <version>0.9.2</version>
</dependency>
```

## Quick Start

**Initialize client**
```java
// Initialize with mainnet
ApiWrapper client = ApiWrapper.ofMainnet("private_key", "api_key"); //api_key from TronGrid

//Or Shasta test net 
ApiWrapper wrapper = ApiWrapper.ofShasta("private key");

// Or nile testnet
ApiWrapper client = ApiWrapper.ofNile("private_key");

// Send TRX
TransactionExtention transactionExtention = client.transfer("fromAddress", "toAddress", 100_000_000L); //100TRX
// Sign
Transaction signedTxn = client.signTransaction(transactionExtention);
// Broadcast
String txId = client.broadcastTransaction(signedTxn);
System.out.println("txId is " + signedTxn.toString());
```

## Documentation

- [Official Documentation](https://developers.tron.network/docs/trident-java)


## Build instructions
Trident includes integration tests for running on the Nile testnet. If you want to run test cases involving write operations on the blockchain, such as transfers or deploy contract and so on, please follow the steps:

1. Uncomment the Disabled function in the unit test cases.
```
   //@Disabled("add private key to enable this case")
```
2. Set the tron.private-key in the test configuration file in the core directory [here](trident-java/core/src/test/resources/application-test.properties).


` tron.private-key=xxx`

**Note:** The account should have at least 1000 TRX, 100 USDT, and 1000 TRC10 token (token ID: 1000587) on the Nile testnet.

## Contribution

We're very glad and appreciate to have contributions from the community.

Refer to our [contributing guide ](CONTRIBUTING.md)for more information.

## Licence

Trident is distributed under a MIT licence.