package org.tron.trident.core.inceptors;

import io.grpc.*;

public class GrpcClientResponseInterceptor implements ClientInterceptor {

  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
      final MethodDescriptor<ReqT, RespT> methodDescriptor,
      final CallOptions callOptions,
      final Channel channel) {

    return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
        channel.newCall(methodDescriptor, callOptions)) {

      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        super.start(
            new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                responseListener) {

              @Override
              public void onMessage(RespT message) {
                /**
                 * write your interceptor logic here
                 */
                System.out.println("do something ...");
                super.onMessage(message);
              }
            },
            headers);
      }
    };
  }
}
