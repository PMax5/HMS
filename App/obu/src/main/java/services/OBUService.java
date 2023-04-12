package services;

import hmsProto.GatewaysGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class OBUService implements AutoCloseable {
    private final GatewaysGrpc.GatewaysBlockingStub stub;
    private final ManagedChannel channel;

    public OBUService(String host, int port) {
        this.channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
        this.stub = GatewaysGrpc.newBlockingStub(this.channel);
    }

    @Override
    public void close() throws Exception {
        this.channel.shutdown();
    }
}
