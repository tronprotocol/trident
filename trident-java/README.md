# trident

Trident-Java is a lightweight SDK that includes libraries for working with TRON system contracts and smart contracts.

Trident-Java makes it easy to build TRON applications with java.

Trident-Java document: https://developers.tron.network/docs/trident-java

## How to use

### Gradle Setting

Add repo setting:

```groovy
repositories {
    maven {
        url  "https://dl.bintray.com/tridentjava/trident/"
    }
}
```

Then add required packages as dependencies.

```groovy
dependencies {
    // protobuf & grpc
    implementation 'com.google.protobuf:protobuf-java:3.11.0'

    implementation 'org.tron.trident:abi:0.1.0'
    implementation 'org.tron.trident:core:0.1.0'
    implementation 'org.tron.trident:utils:0.1.0'

    implementation 'com.google.guava:guava:28.0-jre'
}
```

### Maven Settings

```xml
<dependency>
  <groupId>org.tron.trident</groupId>
  <artifactId>abi</artifactId>
  <version>0.1.0</version>
  <type>pom</type>
</dependency>
<dependency>
  <groupId>org.tron.trident</groupId>
  <artifactId>utils</artifactId>
  <version>0.1.0</version>
  <type>pom</type>
</dependency>
<dependency>
  <groupId>org.tron.trident</groupId>
  <artifactId>core</artifactId>
  <version>0.1.0</version>
  <type>pom</type>
</dependency>
```

## Signature Verification

Package signature files are uploaded together with .jar files. Packages are signed with GnuPG.

### Install GPG

```Shell
brew install gnupg
```

### Get the Public Key

```Shell
gpg --keyserver keyserver.ubuntu.com --recv-key AD0876A4
```

Alternatively, you may import the public manually from below:

```Text
-----BEGIN PGP PUBLIC KEY BLOCK-----

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
uVDYCpNygD8kz5k23id4GQ==
=bbTk
-----END PGP PUBLIC KEY BLOCK-----
```

### Verify Packages

Download package and the corresponding .asc file, take `abi` as an example:

```Shell
gpg --verify abi-0.1.0.jar.asc abi-0.1.0.jar

gpg: Signature made Mon Dec 28 11:51:48 2020 CST
gpg:                using RSA key 76C182C1E1269F494C2315FEA825D8D6E8B49016
gpg: Good signature from "tridentjava <tridentjava@gmail.com>" [ultimate]
```