package dst.ass2.service.auth.impl;

import dst.ass2.service.api.auth.AuthenticationException;
import dst.ass2.service.api.auth.IAuthenticationService;
import dst.ass2.service.api.auth.NoSuchUserException;
import dst.ass2.service.api.auth.proto.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

@ManagedBean
public class AuthService extends AuthServiceGrpc.AuthServiceImplBase {

    @Inject
    private IAuthenticationService authenticationService;

    @Override
    public void authenticate(AuthenticationRequest request, StreamObserver<AuthenticationResponse> responseObserver) {
        AuthenticationResponse.Builder responseBuilder = AuthenticationResponse.newBuilder();

        try {
            String authToken = authenticationService.authenticate(request.getEmail(), request.getPassword());
            responseBuilder.setAuthToken(authToken);
            responseBuilder.setIsAuthenticated(true);

        } catch (NoSuchUserException e) {
            responseObserver.onError(Status.NOT_FOUND.asException());
            return;
        } catch (AuthenticationException e) {
            responseObserver.onError(Status.PERMISSION_DENIED.asException());
            return;
        }
        responseObserver.onNext(responseBuilder.build()); // Send the response
        responseObserver.onCompleted(); // Finished the RPC call
    }

    @Override
    public void validateToken(TokenValidationRequest request, StreamObserver<TokenValidationResponse> responseObserver) {
        super.validateToken(request, responseObserver);
    }
}
