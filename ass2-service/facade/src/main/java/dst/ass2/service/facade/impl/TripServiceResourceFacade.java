package dst.ass2.service.facade.impl;

import com.fasterxml.jackson.core.util.JacksonFeature;
import dst.ass2.service.api.trip.*;
import dst.ass2.service.api.trip.rest.ITripServiceResource;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;
import java.net.URI;

@RequireAuthentication
public class TripServiceResourceFacade implements ITripServiceResource {

    private ITripServiceResource tripServiceResource;

    @Inject
    public TripServiceResourceFacade(URI tripServiceURI) {
        Configuration config = new ResourceConfig()
                .packages("dst.ass2")
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                .register(JacksonFeature.class);

        Client client = ClientBuilder.newClient(config);
        WebTarget webTarget = client.target(tripServiceURI);

        tripServiceResource = WebResourceFactory.newResource(ITripServiceResource.class, webTarget);
    }

    @Override
    public Response createTrip(Long riderId, Long pickupId, Long destinationId) throws EntityNotFoundException {
        return tripServiceResource.createTrip(riderId, pickupId, destinationId);
    }

    @Override
    public Response confirm(Long tripId) throws EntityNotFoundException, InvalidTripException {
        return tripServiceResource.confirm(tripId);
    }

    @Override
    public Response getTrip(Long tripId) throws EntityNotFoundException {
        return tripServiceResource.getTrip(tripId);
    }

    @Override
    public Response deleteTrip(Long tripId) throws EntityNotFoundException {
        return tripServiceResource.deleteTrip(tripId);
    }

    @Override
    public Response addStop(Long tripId, Long locationId) throws EntityNotFoundException {
        return tripServiceResource.addStop(tripId, locationId);
    }

    @Override
    public Response removeStop(Long tripId, Long locationId) throws EntityNotFoundException {
        return tripServiceResource.removeStop(tripId, locationId);
    }

    @Override
    public Response match(Long tripId, MatchDTO matchDTO) throws EntityNotFoundException, DriverNotAvailableException {
        return tripServiceResource.match(tripId, matchDTO);
    }

    @Override
    public Response complete(Long tripId, TripInfoDTO tripInfoDTO) throws EntityNotFoundException {
        return tripServiceResource.complete(tripId, tripInfoDTO);
    }

    @Override
    public Response cancel(Long tripId) throws EntityNotFoundException {
        return tripServiceResource.cancel(tripId);
    }
}
