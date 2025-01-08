package org.tron.trident.api;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * NOTE: All other solidified APIs are useless.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.1)",
    comments = "Source: api/api.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class WalletSolidityGrpc {

  private WalletSolidityGrpc() {}

  public static final java.lang.String SERVICE_NAME = "protocol.WalletSolidity";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.AccountAddressMessage,
      org.tron.trident.proto.Response.Account> getGetAccountMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAccount",
      requestType = org.tron.trident.api.GrpcAPI.AccountAddressMessage.class,
      responseType = org.tron.trident.proto.Response.Account.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.AccountAddressMessage,
      org.tron.trident.proto.Response.Account> getGetAccountMethod() {
    io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.AccountAddressMessage, org.tron.trident.proto.Response.Account> getGetAccountMethod;
    if ((getGetAccountMethod = WalletSolidityGrpc.getGetAccountMethod) == null) {
      synchronized (WalletSolidityGrpc.class) {
        if ((getGetAccountMethod = WalletSolidityGrpc.getGetAccountMethod) == null) {
          WalletSolidityGrpc.getGetAccountMethod = getGetAccountMethod =
              io.grpc.MethodDescriptor.<org.tron.trident.api.GrpcAPI.AccountAddressMessage, org.tron.trident.proto.Response.Account>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAccount"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.api.GrpcAPI.AccountAddressMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.proto.Response.Account.getDefaultInstance()))
              .setSchemaDescriptor(new WalletSolidityMethodDescriptorSupplier("GetAccount"))
              .build();
        }
      }
    }
    return getGetAccountMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.EmptyMessage,
      org.tron.trident.proto.Response.BlockExtention> getGetNowBlock2Method;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetNowBlock2",
      requestType = org.tron.trident.api.GrpcAPI.EmptyMessage.class,
      responseType = org.tron.trident.proto.Response.BlockExtention.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.EmptyMessage,
      org.tron.trident.proto.Response.BlockExtention> getGetNowBlock2Method() {
    io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.EmptyMessage, org.tron.trident.proto.Response.BlockExtention> getGetNowBlock2Method;
    if ((getGetNowBlock2Method = WalletSolidityGrpc.getGetNowBlock2Method) == null) {
      synchronized (WalletSolidityGrpc.class) {
        if ((getGetNowBlock2Method = WalletSolidityGrpc.getGetNowBlock2Method) == null) {
          WalletSolidityGrpc.getGetNowBlock2Method = getGetNowBlock2Method =
              io.grpc.MethodDescriptor.<org.tron.trident.api.GrpcAPI.EmptyMessage, org.tron.trident.proto.Response.BlockExtention>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetNowBlock2"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.api.GrpcAPI.EmptyMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.proto.Response.BlockExtention.getDefaultInstance()))
              .setSchemaDescriptor(new WalletSolidityMethodDescriptorSupplier("GetNowBlock2"))
              .build();
        }
      }
    }
    return getGetNowBlock2Method;
  }

  private static volatile io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.BytesMessage,
      org.tron.trident.proto.Chain.Transaction> getGetTransactionByIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTransactionById",
      requestType = org.tron.trident.api.GrpcAPI.BytesMessage.class,
      responseType = org.tron.trident.proto.Chain.Transaction.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.BytesMessage,
      org.tron.trident.proto.Chain.Transaction> getGetTransactionByIdMethod() {
    io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.BytesMessage, org.tron.trident.proto.Chain.Transaction> getGetTransactionByIdMethod;
    if ((getGetTransactionByIdMethod = WalletSolidityGrpc.getGetTransactionByIdMethod) == null) {
      synchronized (WalletSolidityGrpc.class) {
        if ((getGetTransactionByIdMethod = WalletSolidityGrpc.getGetTransactionByIdMethod) == null) {
          WalletSolidityGrpc.getGetTransactionByIdMethod = getGetTransactionByIdMethod =
              io.grpc.MethodDescriptor.<org.tron.trident.api.GrpcAPI.BytesMessage, org.tron.trident.proto.Chain.Transaction>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTransactionById"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.api.GrpcAPI.BytesMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.proto.Chain.Transaction.getDefaultInstance()))
              .setSchemaDescriptor(new WalletSolidityMethodDescriptorSupplier("GetTransactionById"))
              .build();
        }
      }
    }
    return getGetTransactionByIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.BytesMessage,
      org.tron.trident.api.GrpcAPI.NumberMessage> getGetRewardInfoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetRewardInfo",
      requestType = org.tron.trident.api.GrpcAPI.BytesMessage.class,
      responseType = org.tron.trident.api.GrpcAPI.NumberMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.BytesMessage,
      org.tron.trident.api.GrpcAPI.NumberMessage> getGetRewardInfoMethod() {
    io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.BytesMessage, org.tron.trident.api.GrpcAPI.NumberMessage> getGetRewardInfoMethod;
    if ((getGetRewardInfoMethod = WalletSolidityGrpc.getGetRewardInfoMethod) == null) {
      synchronized (WalletSolidityGrpc.class) {
        if ((getGetRewardInfoMethod = WalletSolidityGrpc.getGetRewardInfoMethod) == null) {
          WalletSolidityGrpc.getGetRewardInfoMethod = getGetRewardInfoMethod =
              io.grpc.MethodDescriptor.<org.tron.trident.api.GrpcAPI.BytesMessage, org.tron.trident.api.GrpcAPI.NumberMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetRewardInfo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.api.GrpcAPI.BytesMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.api.GrpcAPI.NumberMessage.getDefaultInstance()))
              .setSchemaDescriptor(new WalletSolidityMethodDescriptorSupplier("GetRewardInfo"))
              .build();
        }
      }
    }
    return getGetRewardInfoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.BytesMessage,
      org.tron.trident.proto.Chain.Transaction> getGetTransactionFromPendingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTransactionFromPending",
      requestType = org.tron.trident.api.GrpcAPI.BytesMessage.class,
      responseType = org.tron.trident.proto.Chain.Transaction.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.BytesMessage,
      org.tron.trident.proto.Chain.Transaction> getGetTransactionFromPendingMethod() {
    io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.BytesMessage, org.tron.trident.proto.Chain.Transaction> getGetTransactionFromPendingMethod;
    if ((getGetTransactionFromPendingMethod = WalletSolidityGrpc.getGetTransactionFromPendingMethod) == null) {
      synchronized (WalletSolidityGrpc.class) {
        if ((getGetTransactionFromPendingMethod = WalletSolidityGrpc.getGetTransactionFromPendingMethod) == null) {
          WalletSolidityGrpc.getGetTransactionFromPendingMethod = getGetTransactionFromPendingMethod =
              io.grpc.MethodDescriptor.<org.tron.trident.api.GrpcAPI.BytesMessage, org.tron.trident.proto.Chain.Transaction>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTransactionFromPending"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.api.GrpcAPI.BytesMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.proto.Chain.Transaction.getDefaultInstance()))
              .setSchemaDescriptor(new WalletSolidityMethodDescriptorSupplier("GetTransactionFromPending"))
              .build();
        }
      }
    }
    return getGetTransactionFromPendingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.EmptyMessage,
      org.tron.trident.proto.Response.PricesResponseMessage> getGetBandwidthPricesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetBandwidthPrices",
      requestType = org.tron.trident.api.GrpcAPI.EmptyMessage.class,
      responseType = org.tron.trident.proto.Response.PricesResponseMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.EmptyMessage,
      org.tron.trident.proto.Response.PricesResponseMessage> getGetBandwidthPricesMethod() {
    io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.EmptyMessage, org.tron.trident.proto.Response.PricesResponseMessage> getGetBandwidthPricesMethod;
    if ((getGetBandwidthPricesMethod = WalletSolidityGrpc.getGetBandwidthPricesMethod) == null) {
      synchronized (WalletSolidityGrpc.class) {
        if ((getGetBandwidthPricesMethod = WalletSolidityGrpc.getGetBandwidthPricesMethod) == null) {
          WalletSolidityGrpc.getGetBandwidthPricesMethod = getGetBandwidthPricesMethod =
              io.grpc.MethodDescriptor.<org.tron.trident.api.GrpcAPI.EmptyMessage, org.tron.trident.proto.Response.PricesResponseMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetBandwidthPrices"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.api.GrpcAPI.EmptyMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.proto.Response.PricesResponseMessage.getDefaultInstance()))
              .setSchemaDescriptor(new WalletSolidityMethodDescriptorSupplier("GetBandwidthPrices"))
              .build();
        }
      }
    }
    return getGetBandwidthPricesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.EmptyMessage,
      org.tron.trident.proto.Response.PricesResponseMessage> getGetEnergyPricesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetEnergyPrices",
      requestType = org.tron.trident.api.GrpcAPI.EmptyMessage.class,
      responseType = org.tron.trident.proto.Response.PricesResponseMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.EmptyMessage,
      org.tron.trident.proto.Response.PricesResponseMessage> getGetEnergyPricesMethod() {
    io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.EmptyMessage, org.tron.trident.proto.Response.PricesResponseMessage> getGetEnergyPricesMethod;
    if ((getGetEnergyPricesMethod = WalletSolidityGrpc.getGetEnergyPricesMethod) == null) {
      synchronized (WalletSolidityGrpc.class) {
        if ((getGetEnergyPricesMethod = WalletSolidityGrpc.getGetEnergyPricesMethod) == null) {
          WalletSolidityGrpc.getGetEnergyPricesMethod = getGetEnergyPricesMethod =
              io.grpc.MethodDescriptor.<org.tron.trident.api.GrpcAPI.EmptyMessage, org.tron.trident.proto.Response.PricesResponseMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetEnergyPrices"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.api.GrpcAPI.EmptyMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.proto.Response.PricesResponseMessage.getDefaultInstance()))
              .setSchemaDescriptor(new WalletSolidityMethodDescriptorSupplier("GetEnergyPrices"))
              .build();
        }
      }
    }
    return getGetEnergyPricesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.BlockReq,
      org.tron.trident.proto.Response.BlockExtention> getGetBlockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetBlock",
      requestType = org.tron.trident.api.GrpcAPI.BlockReq.class,
      responseType = org.tron.trident.proto.Response.BlockExtention.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.BlockReq,
      org.tron.trident.proto.Response.BlockExtention> getGetBlockMethod() {
    io.grpc.MethodDescriptor<org.tron.trident.api.GrpcAPI.BlockReq, org.tron.trident.proto.Response.BlockExtention> getGetBlockMethod;
    if ((getGetBlockMethod = WalletSolidityGrpc.getGetBlockMethod) == null) {
      synchronized (WalletSolidityGrpc.class) {
        if ((getGetBlockMethod = WalletSolidityGrpc.getGetBlockMethod) == null) {
          WalletSolidityGrpc.getGetBlockMethod = getGetBlockMethod =
              io.grpc.MethodDescriptor.<org.tron.trident.api.GrpcAPI.BlockReq, org.tron.trident.proto.Response.BlockExtention>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetBlock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.api.GrpcAPI.BlockReq.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.tron.trident.proto.Response.BlockExtention.getDefaultInstance()))
              .setSchemaDescriptor(new WalletSolidityMethodDescriptorSupplier("GetBlock"))
              .build();
        }
      }
    }
    return getGetBlockMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static WalletSolidityStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<WalletSolidityStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<WalletSolidityStub>() {
        @java.lang.Override
        public WalletSolidityStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new WalletSolidityStub(channel, callOptions);
        }
      };
    return WalletSolidityStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static WalletSolidityBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<WalletSolidityBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<WalletSolidityBlockingStub>() {
        @java.lang.Override
        public WalletSolidityBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new WalletSolidityBlockingStub(channel, callOptions);
        }
      };
    return WalletSolidityBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static WalletSolidityFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<WalletSolidityFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<WalletSolidityFutureStub>() {
        @java.lang.Override
        public WalletSolidityFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new WalletSolidityFutureStub(channel, callOptions);
        }
      };
    return WalletSolidityFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * NOTE: All other solidified APIs are useless.
   * </pre>
   */
  public interface AsyncService {

    /**
     */
    default void getAccount(org.tron.trident.api.GrpcAPI.AccountAddressMessage request,
        io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.Account> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetAccountMethod(), responseObserver);
    }

    /**
     */
    default void getNowBlock2(org.tron.trident.api.GrpcAPI.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.BlockExtention> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetNowBlock2Method(), responseObserver);
    }

    /**
     * <pre>
     * rpc GetBlockByLatestNum2(NumberMessage) returns (BlockListExtention) {}
     * </pre>
     */
    default void getTransactionById(org.tron.trident.api.GrpcAPI.BytesMessage request,
        io.grpc.stub.StreamObserver<org.tron.trident.proto.Chain.Transaction> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTransactionByIdMethod(), responseObserver);
    }

    /**
     */
    default void getRewardInfo(org.tron.trident.api.GrpcAPI.BytesMessage request,
        io.grpc.stub.StreamObserver<org.tron.trident.api.GrpcAPI.NumberMessage> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetRewardInfoMethod(), responseObserver);
    }

    /**
     */
    default void getTransactionFromPending(org.tron.trident.api.GrpcAPI.BytesMessage request,
        io.grpc.stub.StreamObserver<org.tron.trident.proto.Chain.Transaction> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTransactionFromPendingMethod(), responseObserver);
    }

    /**
     * <pre>
     *query resource price
     * </pre>
     */
    default void getBandwidthPrices(org.tron.trident.api.GrpcAPI.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.PricesResponseMessage> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetBandwidthPricesMethod(), responseObserver);
    }

    /**
     */
    default void getEnergyPrices(org.tron.trident.api.GrpcAPI.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.PricesResponseMessage> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetEnergyPricesMethod(), responseObserver);
    }

    /**
     */
    default void getBlock(org.tron.trident.api.GrpcAPI.BlockReq request,
        io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.BlockExtention> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetBlockMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service WalletSolidity.
   * <pre>
   * NOTE: All other solidified APIs are useless.
   * </pre>
   */
  public static abstract class WalletSolidityImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return WalletSolidityGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service WalletSolidity.
   * <pre>
   * NOTE: All other solidified APIs are useless.
   * </pre>
   */
  public static final class WalletSolidityStub
      extends io.grpc.stub.AbstractAsyncStub<WalletSolidityStub> {
    private WalletSolidityStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WalletSolidityStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new WalletSolidityStub(channel, callOptions);
    }

    /**
     */
    public void getAccount(org.tron.trident.api.GrpcAPI.AccountAddressMessage request,
        io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.Account> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetAccountMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getNowBlock2(org.tron.trident.api.GrpcAPI.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.BlockExtention> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetNowBlock2Method(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * rpc GetBlockByLatestNum2(NumberMessage) returns (BlockListExtention) {}
     * </pre>
     */
    public void getTransactionById(org.tron.trident.api.GrpcAPI.BytesMessage request,
        io.grpc.stub.StreamObserver<org.tron.trident.proto.Chain.Transaction> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTransactionByIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getRewardInfo(org.tron.trident.api.GrpcAPI.BytesMessage request,
        io.grpc.stub.StreamObserver<org.tron.trident.api.GrpcAPI.NumberMessage> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetRewardInfoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTransactionFromPending(org.tron.trident.api.GrpcAPI.BytesMessage request,
        io.grpc.stub.StreamObserver<org.tron.trident.proto.Chain.Transaction> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTransactionFromPendingMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *query resource price
     * </pre>
     */
    public void getBandwidthPrices(org.tron.trident.api.GrpcAPI.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.PricesResponseMessage> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetBandwidthPricesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getEnergyPrices(org.tron.trident.api.GrpcAPI.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.PricesResponseMessage> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetEnergyPricesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getBlock(org.tron.trident.api.GrpcAPI.BlockReq request,
        io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.BlockExtention> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetBlockMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service WalletSolidity.
   * <pre>
   * NOTE: All other solidified APIs are useless.
   * </pre>
   */
  public static final class WalletSolidityBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<WalletSolidityBlockingStub> {
    private WalletSolidityBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WalletSolidityBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new WalletSolidityBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.tron.trident.proto.Response.Account getAccount(org.tron.trident.api.GrpcAPI.AccountAddressMessage request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetAccountMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.tron.trident.proto.Response.BlockExtention getNowBlock2(org.tron.trident.api.GrpcAPI.EmptyMessage request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetNowBlock2Method(), getCallOptions(), request);
    }

    /**
     * <pre>
     * rpc GetBlockByLatestNum2(NumberMessage) returns (BlockListExtention) {}
     * </pre>
     */
    public org.tron.trident.proto.Chain.Transaction getTransactionById(org.tron.trident.api.GrpcAPI.BytesMessage request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTransactionByIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.tron.trident.api.GrpcAPI.NumberMessage getRewardInfo(org.tron.trident.api.GrpcAPI.BytesMessage request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetRewardInfoMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.tron.trident.proto.Chain.Transaction getTransactionFromPending(org.tron.trident.api.GrpcAPI.BytesMessage request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTransactionFromPendingMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *query resource price
     * </pre>
     */
    public org.tron.trident.proto.Response.PricesResponseMessage getBandwidthPrices(org.tron.trident.api.GrpcAPI.EmptyMessage request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetBandwidthPricesMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.tron.trident.proto.Response.PricesResponseMessage getEnergyPrices(org.tron.trident.api.GrpcAPI.EmptyMessage request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetEnergyPricesMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.tron.trident.proto.Response.BlockExtention getBlock(org.tron.trident.api.GrpcAPI.BlockReq request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetBlockMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service WalletSolidity.
   * <pre>
   * NOTE: All other solidified APIs are useless.
   * </pre>
   */
  public static final class WalletSolidityFutureStub
      extends io.grpc.stub.AbstractFutureStub<WalletSolidityFutureStub> {
    private WalletSolidityFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WalletSolidityFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new WalletSolidityFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.tron.trident.proto.Response.Account> getAccount(
        org.tron.trident.api.GrpcAPI.AccountAddressMessage request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetAccountMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.tron.trident.proto.Response.BlockExtention> getNowBlock2(
        org.tron.trident.api.GrpcAPI.EmptyMessage request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetNowBlock2Method(), getCallOptions()), request);
    }

    /**
     * <pre>
     * rpc GetBlockByLatestNum2(NumberMessage) returns (BlockListExtention) {}
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.tron.trident.proto.Chain.Transaction> getTransactionById(
        org.tron.trident.api.GrpcAPI.BytesMessage request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTransactionByIdMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.tron.trident.api.GrpcAPI.NumberMessage> getRewardInfo(
        org.tron.trident.api.GrpcAPI.BytesMessage request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetRewardInfoMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.tron.trident.proto.Chain.Transaction> getTransactionFromPending(
        org.tron.trident.api.GrpcAPI.BytesMessage request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTransactionFromPendingMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     *query resource price
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.tron.trident.proto.Response.PricesResponseMessage> getBandwidthPrices(
        org.tron.trident.api.GrpcAPI.EmptyMessage request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetBandwidthPricesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.tron.trident.proto.Response.PricesResponseMessage> getEnergyPrices(
        org.tron.trident.api.GrpcAPI.EmptyMessage request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetEnergyPricesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.tron.trident.proto.Response.BlockExtention> getBlock(
        org.tron.trident.api.GrpcAPI.BlockReq request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetBlockMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_ACCOUNT = 0;
  private static final int METHODID_GET_NOW_BLOCK2 = 1;
  private static final int METHODID_GET_TRANSACTION_BY_ID = 2;
  private static final int METHODID_GET_REWARD_INFO = 3;
  private static final int METHODID_GET_TRANSACTION_FROM_PENDING = 4;
  private static final int METHODID_GET_BANDWIDTH_PRICES = 5;
  private static final int METHODID_GET_ENERGY_PRICES = 6;
  private static final int METHODID_GET_BLOCK = 7;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_ACCOUNT:
          serviceImpl.getAccount((org.tron.trident.api.GrpcAPI.AccountAddressMessage) request,
              (io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.Account>) responseObserver);
          break;
        case METHODID_GET_NOW_BLOCK2:
          serviceImpl.getNowBlock2((org.tron.trident.api.GrpcAPI.EmptyMessage) request,
              (io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.BlockExtention>) responseObserver);
          break;
        case METHODID_GET_TRANSACTION_BY_ID:
          serviceImpl.getTransactionById((org.tron.trident.api.GrpcAPI.BytesMessage) request,
              (io.grpc.stub.StreamObserver<org.tron.trident.proto.Chain.Transaction>) responseObserver);
          break;
        case METHODID_GET_REWARD_INFO:
          serviceImpl.getRewardInfo((org.tron.trident.api.GrpcAPI.BytesMessage) request,
              (io.grpc.stub.StreamObserver<org.tron.trident.api.GrpcAPI.NumberMessage>) responseObserver);
          break;
        case METHODID_GET_TRANSACTION_FROM_PENDING:
          serviceImpl.getTransactionFromPending((org.tron.trident.api.GrpcAPI.BytesMessage) request,
              (io.grpc.stub.StreamObserver<org.tron.trident.proto.Chain.Transaction>) responseObserver);
          break;
        case METHODID_GET_BANDWIDTH_PRICES:
          serviceImpl.getBandwidthPrices((org.tron.trident.api.GrpcAPI.EmptyMessage) request,
              (io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.PricesResponseMessage>) responseObserver);
          break;
        case METHODID_GET_ENERGY_PRICES:
          serviceImpl.getEnergyPrices((org.tron.trident.api.GrpcAPI.EmptyMessage) request,
              (io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.PricesResponseMessage>) responseObserver);
          break;
        case METHODID_GET_BLOCK:
          serviceImpl.getBlock((org.tron.trident.api.GrpcAPI.BlockReq) request,
              (io.grpc.stub.StreamObserver<org.tron.trident.proto.Response.BlockExtention>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetAccountMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.tron.trident.api.GrpcAPI.AccountAddressMessage,
              org.tron.trident.proto.Response.Account>(
                service, METHODID_GET_ACCOUNT)))
        .addMethod(
          getGetNowBlock2Method(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.tron.trident.api.GrpcAPI.EmptyMessage,
              org.tron.trident.proto.Response.BlockExtention>(
                service, METHODID_GET_NOW_BLOCK2)))
        .addMethod(
          getGetTransactionByIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.tron.trident.api.GrpcAPI.BytesMessage,
              org.tron.trident.proto.Chain.Transaction>(
                service, METHODID_GET_TRANSACTION_BY_ID)))
        .addMethod(
          getGetRewardInfoMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.tron.trident.api.GrpcAPI.BytesMessage,
              org.tron.trident.api.GrpcAPI.NumberMessage>(
                service, METHODID_GET_REWARD_INFO)))
        .addMethod(
          getGetTransactionFromPendingMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.tron.trident.api.GrpcAPI.BytesMessage,
              org.tron.trident.proto.Chain.Transaction>(
                service, METHODID_GET_TRANSACTION_FROM_PENDING)))
        .addMethod(
          getGetBandwidthPricesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.tron.trident.api.GrpcAPI.EmptyMessage,
              org.tron.trident.proto.Response.PricesResponseMessage>(
                service, METHODID_GET_BANDWIDTH_PRICES)))
        .addMethod(
          getGetEnergyPricesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.tron.trident.api.GrpcAPI.EmptyMessage,
              org.tron.trident.proto.Response.PricesResponseMessage>(
                service, METHODID_GET_ENERGY_PRICES)))
        .addMethod(
          getGetBlockMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.tron.trident.api.GrpcAPI.BlockReq,
              org.tron.trident.proto.Response.BlockExtention>(
                service, METHODID_GET_BLOCK)))
        .build();
  }

  private static abstract class WalletSolidityBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    WalletSolidityBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.tron.trident.api.GrpcAPI.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("WalletSolidity");
    }
  }

  private static final class WalletSolidityFileDescriptorSupplier
      extends WalletSolidityBaseDescriptorSupplier {
    WalletSolidityFileDescriptorSupplier() {}
  }

  private static final class WalletSolidityMethodDescriptorSupplier
      extends WalletSolidityBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    WalletSolidityMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (WalletSolidityGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new WalletSolidityFileDescriptorSupplier())
              .addMethod(getGetAccountMethod())
              .addMethod(getGetNowBlock2Method())
              .addMethod(getGetTransactionByIdMethod())
              .addMethod(getGetRewardInfoMethod())
              .addMethod(getGetTransactionFromPendingMethod())
              .addMethod(getGetBandwidthPricesMethod())
              .addMethod(getGetEnergyPricesMethod())
              .addMethod(getGetBlockMethod())
              .build();
        }
      }
    }
    return result;
  }
}
