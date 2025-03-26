package org.tron.trident.core.inceptors;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.grpc.ClientInterceptor;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.Constant;
import org.tron.trident.core.interceptor.TimeoutInterceptor;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Response.BlockExtention;

public class TimeoutInterceptorTest {

  @Test
  void testTimeoutInterceptor() {

    List<ClientInterceptor> clientInterceptorList = new ArrayList<>();

    clientInterceptorList.add(new TimeoutInterceptor(1));  // 1ms timeout


    ApiWrapper client = new ApiWrapper(
        Constant.FULLNODE_NILE,
        Constant.FULLNODE_NILE_SOLIDITY,
        KeyPair.generate().toPrivateKey(),
        clientInterceptorList
    );

    try {
      client.getBlock(false);
      fail("Except DEADLINE_EXCEEDED Exception");

    } catch (Exception e) {
      System.out.println(e.getMessage());
      assertTrue(e.getMessage().contains("DEADLINE_EXCEEDED"));
      assertTrue(e instanceof io.grpc.StatusRuntimeException);
    } finally {
      client.close();
    }
  }

  @Test
  void testClientInterceptorWithShortTimeOut() {

    List<ClientInterceptor> clientInterceptorList = new ArrayList<>();


    ApiWrapper client = new ApiWrapper(
        Constant.FULLNODE_NILE,
        Constant.FULLNODE_NILE_SOLIDITY,
        KeyPair.generate().toPrivateKey(),
        clientInterceptorList,
        1
    );

    try {
      BlockExtention blockExtention = client.getBlock(false);
      System.out.println(blockExtention.getBlockHeader().getRawData().getNumber());
      fail("Except DEADLINE_EXCEEDED Exception");

    } catch (Exception e) {
      System.out.println(e.getMessage());
      assertTrue(e.getMessage().contains("DEADLINE_EXCEEDED"));
      assertTrue(e instanceof io.grpc.StatusRuntimeException);
    } finally {
      client.close();
    }
  }

  @Test
  void testClientInterceptorWithLongTimeOut() {

    ApiWrapper clientDefault = new ApiWrapper(
        Constant.FULLNODE_NILE,
        Constant.FULLNODE_NILE_SOLIDITY,
        KeyPair.generate().toPrivateKey(),
        10_000 //10s
    );

    for (int i = 0; i < 2; i++) {
      assertDoesNotThrow(() -> {
        clientDefault.getBlock(false);
        sleep(10_000L);
      });
    }
  }
}
