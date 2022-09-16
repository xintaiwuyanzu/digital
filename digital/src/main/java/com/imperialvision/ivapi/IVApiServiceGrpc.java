package com.imperialvision.ivapi;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 *
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 0.15.0)",
        comments = "Source: ivapi.proto")
public class IVApiServiceGrpc {

    private IVApiServiceGrpc() {
    }

    public static final String SERVICE_NAME = "com.imperialvision.ivapi.IVApiService";

    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    public static final io.grpc.MethodDescriptor<IVApiRequest,
            IVApiResponse> METHOD_GET_IVAPI_SERVICE =
            io.grpc.MethodDescriptor.create(
                    io.grpc.MethodDescriptor.MethodType.UNARY,
                    generateFullMethodName(
                            "com.imperialvision.ivapi.IVApiService", "getIVApiService"),
                    io.grpc.protobuf.ProtoUtils.marshaller(IVApiRequest.getDefaultInstance()),
                    io.grpc.protobuf.ProtoUtils.marshaller(IVApiResponse.getDefaultInstance()));

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static IVApiServiceStub newStub(io.grpc.Channel channel) {
        return new IVApiServiceStub(channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static IVApiServiceBlockingStub newBlockingStub(
            io.grpc.Channel channel) {
        return new IVApiServiceBlockingStub(channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
     */
    public static IVApiServiceFutureStub newFutureStub(
            io.grpc.Channel channel) {
        return new IVApiServiceFutureStub(channel);
    }

    /**
     *
     */
    @Deprecated
    public static interface IVApiService {

        /**
         *
         */
        public void getIVApiService(IVApiRequest request,
                                    io.grpc.stub.StreamObserver<IVApiResponse> responseObserver);
    }

    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1469")
    public static abstract class IVApiServiceImplBase implements IVApiService, io.grpc.BindableService {

        @Override
        public void getIVApiService(IVApiRequest request,
                                    io.grpc.stub.StreamObserver<IVApiResponse> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_GET_IVAPI_SERVICE, responseObserver);
        }

        @Override
        public io.grpc.ServerServiceDefinition bindService() {
            return IVApiServiceGrpc.bindService(this);
        }
    }

    /**
     *
     */
    @Deprecated
    public static interface IVApiServiceBlockingClient {

        /**
         *
         */
        public IVApiResponse getIVApiService(IVApiRequest request);
    }

    /**
     *
     */
    @Deprecated
    public static interface IVApiServiceFutureClient {

        /**
         *
         */
        public com.google.common.util.concurrent.ListenableFuture<IVApiResponse> getIVApiService(
                IVApiRequest request);
    }

    public static class IVApiServiceStub extends io.grpc.stub.AbstractStub<IVApiServiceStub>
            implements IVApiService {
        private IVApiServiceStub(io.grpc.Channel channel) {
            super(channel);
        }

        private IVApiServiceStub(io.grpc.Channel channel,
                                 io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected IVApiServiceStub build(io.grpc.Channel channel,
                                         io.grpc.CallOptions callOptions) {
            return new IVApiServiceStub(channel, callOptions);
        }

        @Override
        public void getIVApiService(IVApiRequest request,
                                    io.grpc.stub.StreamObserver<IVApiResponse> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(METHOD_GET_IVAPI_SERVICE, getCallOptions()), request, responseObserver);
        }
    }

    public static class IVApiServiceBlockingStub extends io.grpc.stub.AbstractStub<IVApiServiceBlockingStub>
            implements IVApiServiceBlockingClient {
        private IVApiServiceBlockingStub(io.grpc.Channel channel) {
            super(channel);
        }

        private IVApiServiceBlockingStub(io.grpc.Channel channel,
                                         io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected IVApiServiceBlockingStub build(io.grpc.Channel channel,
                                                 io.grpc.CallOptions callOptions) {
            return new IVApiServiceBlockingStub(channel, callOptions);
        }

        @Override
        public IVApiResponse getIVApiService(IVApiRequest request) {
            return blockingUnaryCall(
                    getChannel(), METHOD_GET_IVAPI_SERVICE, getCallOptions(), request);
        }
    }

    public static class IVApiServiceFutureStub extends io.grpc.stub.AbstractStub<IVApiServiceFutureStub>
            implements IVApiServiceFutureClient {
        private IVApiServiceFutureStub(io.grpc.Channel channel) {
            super(channel);
        }

        private IVApiServiceFutureStub(io.grpc.Channel channel,
                                       io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected IVApiServiceFutureStub build(io.grpc.Channel channel,
                                               io.grpc.CallOptions callOptions) {
            return new IVApiServiceFutureStub(channel, callOptions);
        }

        @Override
        public com.google.common.util.concurrent.ListenableFuture<IVApiResponse> getIVApiService(
                IVApiRequest request) {
            return futureUnaryCall(
                    getChannel().newCall(METHOD_GET_IVAPI_SERVICE, getCallOptions()), request);
        }
    }

    @Deprecated
    public static abstract class AbstractIVApiService extends IVApiServiceImplBase {
    }

    private static final int METHODID_GET_IVAPI_SERVICE = 0;

    private static class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final IVApiService serviceImpl;
        private final int methodId;

        public MethodHandlers(IVApiService serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_GET_IVAPI_SERVICE:
                    serviceImpl.getIVApiService((IVApiRequest) request,
                            (io.grpc.stub.StreamObserver<IVApiResponse>) responseObserver);
                    break;
                default:
                    throw new AssertionError();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(
                io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                default:
                    throw new AssertionError();
            }
        }
    }

    public static io.grpc.ServiceDescriptor getServiceDescriptor() {
        return new io.grpc.ServiceDescriptor(SERVICE_NAME,
                METHOD_GET_IVAPI_SERVICE);
    }

    @Deprecated
    public static io.grpc.ServerServiceDefinition bindService(
            final IVApiService serviceImpl) {
        return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                .addMethod(
                        METHOD_GET_IVAPI_SERVICE,
                        asyncUnaryCall(
                                new MethodHandlers<
                                        IVApiRequest,
                                        IVApiResponse>(
                                        serviceImpl, METHODID_GET_IVAPI_SERVICE)))
                .build();
    }
}
