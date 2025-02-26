package org.tron.trident.core.interceptor;

import io.grpc.*;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MonitoringInterceptor implements ClientInterceptor {
  private final AtomicLong requestCount = new AtomicLong(0);

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
      MethodDescriptor<ReqT, RespT> method,
      CallOptions callOptions,
      Channel next) {

    long startTime = System.currentTimeMillis();
    String methodName = method.getFullMethodName();

    logCallOptions(callOptions, methodName);

    ClientCall<ReqT, RespT> delegate = next.newCall(method, callOptions);

    return new SimpleForwardingClientCall<ReqT, RespT>(delegate) {
      private boolean headersSent = false;

      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        System.out.println("Starting call to " + methodName);
        System.out.println("Request headers: " + headers);

        Listener<RespT> monitoringListener = new SimpleForwardingClientCallListener<RespT>(responseListener) {
          @Override
          public void onMessage(RespT message) {
            System.out.println("Received response message for " + methodName);
            super.onMessage(message);
          }

          @Override
          public void onHeaders(Metadata headers) {
            System.out.println("Received response headers: " + headers);
            super.onHeaders(headers);
          }

          @Override
          public void onClose(Status status, Metadata trailers) {
            long duration = System.currentTimeMillis() - startTime;
            System.out.printf(
                "Call to %s completed with status %s in %dms%n",
                methodName, status.getCode(), duration
            );
            super.onClose(status, trailers);
          }
        };

        super.start(monitoringListener, headers);
        headersSent = true;
      }

      @Override
      public void sendMessage(ReqT message) {
        long requestNum = requestCount.incrementAndGet();
        System.out.printf(
            "Sending request #%d to %s: %s%n",
            requestNum, methodName, message
        );
        super.sendMessage(message);
      }
    };
  }

  private void logCallOptions(CallOptions callOptions, String methodName) {
    StringBuilder sb = new StringBuilder();
    sb.append("\nCall Options for ").append(methodName).append(":\n");

    Deadline deadline = callOptions.getDeadline();
    if (deadline != null) {
      long timeoutMillis = deadline.timeRemaining(TimeUnit.MILLISECONDS);
      sb.append("- Timeout: ").append(timeoutMillis).append("ms\n");
    } else {
      sb.append("- No timeout set\n");
    }

    String compressorName = callOptions.getCompressor();
    if (compressorName != null) {
      sb.append("- Compressor: ").append(compressorName).append("\n");
    }

    CallCredentials credentials = callOptions.getCredentials();
    if (credentials != null) {
      sb.append("- Using call credentials\n");
    }

    System.out.println(sb.toString());
  }
}
