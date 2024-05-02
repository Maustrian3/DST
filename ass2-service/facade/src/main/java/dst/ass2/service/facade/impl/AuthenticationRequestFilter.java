package dst.ass2.service.facade.impl;

import dst.ass2.service.auth.client.IAuthenticationClient;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
@RequireAuthentication // Set filter for annotation
public class AuthenticationRequestFilter implements ContainerRequestFilter {

    @Inject
    private IAuthenticationClient client;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Extract token from Authorization header
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Missing or invalid token")
                    .build());
            return;
        }

        String token = authorizationHeader.substring("Bearer".length()).trim();

        // Validate token
        if (!client.isTokenValid(token)) {
            requestContext.abortWith(
                    Response.status(
                            Response.Status.UNAUTHORIZED)
                            .entity("Invalid token")
                            .build());
        }
    }
}
