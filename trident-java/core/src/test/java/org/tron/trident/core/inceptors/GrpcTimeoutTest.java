package org.tron.trident.core.inceptors;

import static org.junit.jupiter.api.Assertions.*;

import com.google.protobuf.ByteString;
import io.grpc.ClientInterceptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tron.trident.api.GrpcAPI.EmptyMessage;
import org.tron.trident.api.WalletGrpc;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.interceptor.TimeoutInterceptor;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain.Block;
import org.tron.trident.proto.Chain.BlockHeader;

class GrpcTimeoutTest {
  private Server server;
  private String serverAddress;
  private ApiWrapper client;
  private long mockResponseDelay = 300;

  @BeforeEach
  void setUp() throws IOException {
    WalletGrpc.WalletImplBase serviceImpl =
        new WalletGrpc.WalletImplBase() {
          @Override
          public void getNowBlock(EmptyMessage request, StreamObserver<Block> responseObserver) {
            try {
              Thread.sleep(mockResponseDelay);
              BlockHeader.raw.Builder rawBuilder =
                  BlockHeader.raw
                      .newBuilder()
                      .setNumber(1L)
                      .setTimestamp(System.currentTimeMillis())
                      .setVersion(0)
                      .setWitnessAddress(ByteString.copyFrom(new byte[21]))
                      .setParentHash(ByteString.copyFrom(new byte[32]))
                      .setTxTrieRoot(ByteString.copyFrom(new byte[32]));

              Block block =
                  Block.newBuilder()
                      .setBlockHeader(
                          BlockHeader.newBuilder().setRawData(rawBuilder.build()).build())
                      .build();
              responseObserver.onNext(block);
              responseObserver.onCompleted();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        };

    // start server
    server = ServerBuilder.forPort(0).addService(serviceImpl).build().start();

    serverAddress = "localhost:" + server.getPort();
  }

  @AfterEach
  void tearDown() {
    if (client != null) {
      client.close();
    }
    if (server != null) {
      server.shutdown();
    }
  }

  @Test
  void testTimeoutInterceptor() {

    List<ClientInterceptor> interceptors = new ArrayList<>();

    client =
        new ApiWrapper(
            serverAddress,
            serverAddress,
            KeyPair.generate().toPrivateKey(),
            interceptors,
            100 // 100ms timeout
            );

    try {
      client.getNowBlock();
      fail("Expected timeout exception");
    } catch (Exception e) {
      assertTrue(e instanceof io.grpc.StatusRuntimeException);
      assertTrue(e.getMessage().contains("DEADLINE_EXCEEDED"));
    }
  }

  @Test
  void testIdleTimeout() {

    List<ClientInterceptor> interceptors = new ArrayList<>();

    client =
        new ApiWrapper(
            serverAddress,
            serverAddress,
            KeyPair.generate().toPrivateKey(),
            true,
            100
        );

    try {
      Block block = client.getNowBlock();
      assertNotNull(block);
      assertTrue(block.getBlockHeader().getRawData().getNumber() > 0);
      System.out.println("Request completed successfully");
    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    }
  }


  @Test
  void testTimeoutNotTriggered() {

    List<ClientInterceptor> interceptors = new ArrayList<>();
    interceptors.add(new TimeoutInterceptor(1000));

    client =
        new ApiWrapper(
            serverAddress,
            serverAddress,
            KeyPair.generate().toPrivateKey(),
            interceptors
        );

    try {
      Block block = client.getNowBlock();
      assertNotNull(block);
      assertTrue(block.getBlockHeader().getRawData().getNumber() > 0);
      System.out.println("Request completed successfully");
    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    }
  }


}
