package dst.ass2.service.auth.client.impl;

import dst.ass2.service.api.auth.AuthenticationException;
import dst.ass2.service.api.auth.NoSuchUserException;
import dst.ass2.service.api.auth.proto.AuthServiceGrpc;
import dst.ass2.service.api.auth.proto.AuthenticationRequest;
import dst.ass2.service.api.auth.proto.TokenValidationRequest;
import dst.ass2.service.auth.client.AuthenticationClientProperties;
import dst.ass2.service.auth.client.IAuthenticationClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class GrpcAuthenticationClient implements IAuthenticationClient {

    private ManagedChannel channel;
    private AuthServiceGrpc.AuthServiceBlockingStub blockingStub;

    public GrpcAuthenticationClient(AuthenticationClientProperties properties) {
        channel = ManagedChannelBuilder.forAddress(properties.getHost(), properties.getPort())
                .usePlaintext()
                .build();
        blockingStub = AuthServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public String authenticate(String email, String password) throws NoSuchUserException, AuthenticationException {
        AuthenticationRequest request = AuthenticationRequest.newBuilder()
                .setEmail(email)
                .setPassword(password)
                .build();
        try {
            return blockingStub.authenticate(request).getAuthToken();
        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            if (status == Status.NOT_FOUND) {
                throw new NoSuchUserException();
            } else if (status == Status.PERMISSION_DENIED) {
                throw new AuthenticationException();
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean isTokenValid(String token) {
        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setAuthToken(token)
                .build();
        return blockingStub.validateToken(request).getIsValid();
    }

    @Override
    public void close() {
        channel.shutdown();
    }
}
