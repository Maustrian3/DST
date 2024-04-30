package dst.ass2.service.auth.grpc.impl;

import dst.ass2.service.auth.grpc.GrpcServerProperties;
import dst.ass2.service.auth.grpc.IGrpcServerRunner;
import dst.ass2.service.auth.impl.AuthService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.io.IOException;

@ManagedBean
public class GrpcServerRunner implements IGrpcServerRunner {

    @Inject
    private GrpcServerProperties properties;

    @Inject
    private AuthService authService;

    @Override
    public void run() throws IOException {
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(properties.getPort());
        Server server =
                serverBuilder.addService(authService)
                        .build();
        server.start();
    }
}
