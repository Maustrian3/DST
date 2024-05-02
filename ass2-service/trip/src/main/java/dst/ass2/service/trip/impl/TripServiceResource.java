package dst.ass2.service.trip.impl;

import dst.ass2.service.api.trip.*;
import dst.ass2.service.api.trip.rest.ITripServiceResource;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/trips")
public class TripServiceResource implements ITripServiceResource {

    @Inject
    private ITripService tripService;

    @Override
    @POST
    public Response createTrip(@FormParam("riderId") Long riderId,
                               @FormParam("pickupId") Long pickupId,
                               @FormParam("destinationId") Long destinationId)
            throws EntityNotFoundException {
        TripDTO newTripDTO = tripService.create(riderId, pickupId, destinationId);
        return Response.ok(newTripDTO.getId()).build();
    }

    @Override
    @PATCH
    @Path("{id}/confirm")
    public Response confirm(@PathParam("id") Long tripId)
            throws EntityNotFoundException, InvalidTripException {
        tripService.confirm(tripId);
        return Response.ok().build();
    }

    @Override
    @GET
    @Path("{id}")
    @Produces("application/json")
    public Response getTrip(@PathParam("id") Long tripId)
            throws EntityNotFoundException {
        TripDTO tripDTO = tripService.find(tripId);
        if (tripDTO == null) {
            throw new EntityNotFoundException("Trip not found");
        }
        return Response.ok(tripDTO).build();
    }

    @Override
    @DELETE
    @Path("{id}")
    public Response deleteTrip(@PathParam("id") Long tripId)
            throws EntityNotFoundException {
        tripService.delete(tripId);
        return Response.ok().build();
    }

    @Override
    @POST
    @Path("{id}/stops")
    @Produces("application/json")
    public Response addStop(@PathParam("id") Long tripId,
                            @FormParam("locationId") Long locationId)
            throws EntityNotFoundException {
        TripDTO tripDTO = tripService.find(tripId);
        if (tripDTO == null) {
            throw new EntityNotFoundException("Trip not found");
        }
        boolean wasAdded = tripService.addStop(tripDTO, locationId);
        if (!wasAdded) {
            throw new IllegalStateException("Stop could not be added");
        }
        return Response.ok(tripDTO.getFare()).build();
    }

    @Override
    @DELETE
    @Path("{id}/stops/{locationId}")
    public Response removeStop(@PathParam("id") Long tripId,
                               @PathParam("locationId") Long locationId)
            throws EntityNotFoundException {
        TripDTO tripDTO = tripService.find(tripId);
        if (tripDTO == null) {
            throw new EntityNotFoundException("Trip not found");
        }
        boolean wasRemoved = tripService.removeStop(tripDTO, locationId);
        if (!wasRemoved) {
            throw new IllegalStateException("Stop could not be removed");
        }
        return Response.ok().build();
    }

    @Override
    @POST
    @Path("{id}/match")
    @Consumes("application/json")
    public Response match(@PathParam("id") Long tripId,
                          MatchDTO matchDTO)
            throws EntityNotFoundException, DriverNotAvailableException {
        tripService.match(tripId, matchDTO);
        return Response
                .status(Response.Status.CREATED)
                .build();
    }

    @Override
    @POST
    @Path("{id}/complete")
    @Consumes("application/json")
    public Response complete(@PathParam("id") Long tripId,
                             TripInfoDTO tripInfoDTO)
            throws EntityNotFoundException {
        tripService.complete(tripId, tripInfoDTO);
        return Response
                .status(Response.Status.CREATED)
                .build();
    }

    @Override
    @PATCH
    @Path("{id}/cancel")
    public Response cancel(@PathParam("id") Long tripId)
            throws EntityNotFoundException {
        tripService.cancel(tripId);
        return Response.ok().build();
    }
}
