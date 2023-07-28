# What's Trident-java

Trident-java is a lightweight SDK that includes libraries for working with TRON system contracts and smart contracts.Trident-java makes it easy to build TRON applications with java.

Functions include:

- Offline address generation

- Offline transaction construct

- Offline transaction signature 

- java-tron full node API support

# Building the source
Building trident-java requires `git` and 64-bit version of `Oracle JDK 1.8` to be installed, other JDK versions are not supported yet. 

Clone the repo and switch to the `master` branch

  ```bash
  $ git clone https://github.com/tronprotocol/trident.git
  $ cd trident
  $ cd trident-java
  $ git checkout -t origin/master
  ```
then run the following command to build trident-java, the `trident-0.7.0.jar` file can be found in `build/libs/` after build successful.
```bash
$ ./gradlew clean build -x test
```

# Install trident-java
trident-java has set compatibility for jdk8 and can be used as a dependency. You can also add compatibility settings before compiling, located in trident-java/build.gradle, 'subject' item:
```
java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        // Add any compatibility 
    }
```
### 1.Repository settings
Add Bintray to Gradle repositories:
```
repositories {
    mavenCentral()
}
```
### 2.Choose required packages
trident-java includes three packages:

* abi---This contains datatypes and ABI encoders/decoders.
* core---This contains the wrapping functions for easily interacting with TRON system and smart contracts.
* utils---This contains tools including encryption, conversion, Etc.

### 3.Gradle dependencies
trident-java interacts with the TRON network through GRPC, Protobuf & GRPC related packages are required.
```
dependencies {
    // protobuf & grpc
    implementation 'com.google.protobuf:protobuf-java:3.11.0'
  
    implementation 'io.grpc:grpc-netty-shaded:1.31.0'
    implementation 'io.grpc:grpc-netty:1.31.0'
    implementation 'io.grpc:grpc-protobuf:1.31.0'
    implementation 'io.grpc:grpc-stub:1.31.0'
  
    implementation "org.bouncycastle:bcprov-jdk15on:1.68"

    implementation fileTree(dir:'../core')
    implementation fileTree(dir:'../utils')
    implementation fileTree(dir:'../abi')
    //if you are using the *.jar files, ues the following line
    implementation fileTree(dir:'your path', include: '*.jar')

    implementation 'com.google.guava:guava:28.0-jre'
}
```

## Contribution

We're very glad and appreciate to have contributions from the community. 

Refer to our [contributing guide](./CONTRIBUTING.md) for more information.

[Join our Telegram group](https://t.me/TronOfficialDevelopersGroupEn)




For more informations refer to : [trident-java document](https://developers.tron.network/docs/trident-java)