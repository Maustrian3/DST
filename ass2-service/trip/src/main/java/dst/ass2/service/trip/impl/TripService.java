package dst.ass2.service.trip.impl;

import dst.ass1.jpa.dao.*;
import dst.ass1.jpa.model.*;
import dst.ass1.jpa.model.impl.Employment;
import dst.ass2.service.api.match.IMatchingService;
import dst.ass2.service.api.trip.*;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Singleton
@ManagedBean
public class TripService implements ITripService {

    @PersistenceContext(name = "dst")
    private EntityManager entityManager;

    @Inject
    IDAOFactory daoFactory;

    @Inject
    IModelFactory modelFactory;

    @Inject
    IMatchingService matchingService;

    private TripDTO tripModelToDTO(ITrip trip) {
        if (trip == null) {
            return null;
        }

        TripDTO tripDTO = new TripDTO();
        tripDTO.setId(trip.getId());
        tripDTO.setRiderId(trip.getRider().getId());
        tripDTO.setPickupId(trip.getPickup().getId());
        tripDTO.setDestinationId(trip.getDestination().getId());

        List<Long> stopIds = new ArrayList<>();
        for (ILocation location : trip.getStops()) {
            stopIds.add(location.getId());
        }
        tripDTO.setStops(stopIds);

        recalculateFare(tripDTO);

        return tripDTO;
    }

    private void recalculateFare(TripDTO tripDTO) {
        try {
            MoneyDTO fare = matchingService.calculateFare(tripDTO);
            tripDTO.setFare(fare);
        } catch (InvalidTripException e) {
            tripDTO.setFare(null);
        }
    }

    @Override
    @Transactional
    public TripDTO create(Long riderId, Long pickupId, Long destinationId) throws EntityNotFoundException {
        ITrip tripModel = modelFactory.createTrip();

        IRiderDAO riderDAO = daoFactory.createRiderDAO();
        IRider rider = riderDAO.findById(riderId);
        if (rider == null) {
            throw new EntityNotFoundException("Rider not found");
        }

        ILocationDAO locationDAO = daoFactory.createLocationDAO();
        ILocation pickup = locationDAO.findById(pickupId);
        if (pickup == null) {
            throw new EntityNotFoundException("Pickup location not found");
        }
        ILocation destination = locationDAO.findById(destinationId);
        if (destination == null) {
            throw new EntityNotFoundException("Destination location not found");
        }

        tripModel.setState(TripState.CREATED);
        tripModel.setRider(rider);
        tripModel.setPickup(pickup);
        tripModel.setDestination(destination);

        entityManager.persist(tripModel);

        return tripModelToDTO(tripModel);
    }

    @Override
    @Transactional
    public void confirm(Long tripId) throws EntityNotFoundException, IllegalStateException, InvalidTripException {
        ITripDAO tripDAO = daoFactory.createTripDAO();
        ITrip tripModel = tripDAO.findById(tripId);
        if (tripModel == null) {
            throw new EntityNotFoundException("Trip not found");
        }

        TripDTO tripDTO = tripModelToDTO(tripModel);
        if (tripDTO.getFare() == null) {
            throw new InvalidTripException("Fare not calculated");
        }

        if (tripModel.getState() != TripState.CREATED) {
            throw new IllegalStateException("Trip is not in CREATED state");
        }

        tripModel.setState(TripState.QUEUED);

        // Put trip into queue for driver matching
        matchingService.queueTripForMatching(tripId);
    }

    @Override
    @Transactional
    public void match(Long tripId, MatchDTO match) throws EntityNotFoundException, DriverNotAvailableException, IllegalStateException {
        // TODO Ensure write locks


        IMatchDAO matchDAO = daoFactory.createMatchDAO();
        IDriverDAO driverDAO = daoFactory.createDriverDAO();
        IVehicleDAO vehicleDAO = daoFactory.createVehicleDAO();
        ITripDAO tripDAO = daoFactory.createTripDAO();
        IDriver driverModel;
        IVehicle vehicleModel;
        ITrip tripModel;
        try {
            // Validate availability of the driver
            if (match.getDriverId() == null) {
                throw new IllegalStateException("Driver ID is required");
            }
            driverModel = driverDAO.findById(match.getDriverId());
            if (driverModel == null) {
                throw new EntityNotFoundException("Driver not found");
            }
            if (driverModel.getEmployments().isEmpty()) {
                throw new DriverNotAvailableException("Driver is not employed");
            }
            if (!matchDAO.findByDriverAndStates(driverModel.getId(), List.of("MATCHED", "APPROACHING", "IN_PROGRESS")).isEmpty()) {
                throw new DriverNotAvailableException("Driver is already assigned to another trip");
            }

            // Validate availability of the vehicle
            vehicleModel = vehicleDAO.findById(match.getVehicleId());
            if (vehicleModel == null) {
                throw new EntityNotFoundException("Vehicle not found");
            }

            // Validate availability of the trip
            tripModel = tripDAO.findById(tripId);
            if (tripModel == null) {
                throw new EntityNotFoundException("Trip not found");
            }

            if (tripModel.getState() != TripState.QUEUED) {
                throw new IllegalStateException("Trip is not in QUEUED state");
            }
        } catch (Exception e) {
            // Requeue trip
            matchingService.queueTripForMatching(tripId); // TODO Add mechanism to prevent requeueing invalid trips?

            // TODO Release write locks

            throw e;
        }

        // If everything is valid
        // Update trip state to MATCHED
        tripModel.setState(TripState.MATCHED);

        // Create Match entity
        IMoney fareModel = modelFactory.createMoney();
        fareModel.setCurrency(match.getFare().getCurrency());
        fareModel.setCurrencyValue(match.getFare().getValue());
        entityManager.persist(fareModel);

        IMatch matchModel = modelFactory.createMatch();
        matchModel.setDate(new Date());
        matchModel.setFare(fareModel);
        matchModel.setDriver(driverModel);
        matchModel.setVehicle(vehicleModel);
        entityManager.persist(matchModel);

        // TODO Release write locks
    }

    @Override
    @Transactional
    public void complete(Long tripId, TripInfoDTO tripInfoDTO) throws EntityNotFoundException {
        ITripDAO tripDAO = daoFactory.createTripDAO();
        ITrip tripModel = tripDAO.findById(tripId);
        if (tripModel == null) {
            throw new EntityNotFoundException("Trip not found");
        }
        tripModel.setState(TripState.COMPLETED);

        IMoney totalModel = modelFactory.createMoney();
        totalModel.setCurrency(tripInfoDTO.getFare().getCurrency());
        totalModel.setCurrencyValue(tripInfoDTO.getFare().getValue());

        IPaymentInfo paymentInfoModel = modelFactory.createPaymentInfo();
        entityManager.persist(paymentInfoModel); // FIXME Empty payment info?

        ITripReceipt receiptModel = modelFactory.createTripReceipt();
        ITripInfo tripInfoModel = modelFactory.createTripInfo();

        receiptModel.setTotal(totalModel);
        receiptModel.setTripInfo(tripInfoModel);
        receiptModel.setPaymentInfo(paymentInfoModel);
        receiptModel.setPaid(false);

        tripInfoModel.setDistance(tripInfoDTO.getDistance());
        tripInfoModel.setCompleted(tripInfoDTO.getCompleted());
        tripInfoModel.setDistance(tripInfoDTO.getDistance());
        tripInfoModel.setReceipt(receiptModel);
        tripInfoModel.setTrip(tripModel);

        entityManager.persist(receiptModel);
        entityManager.persist(tripInfoModel);
    }

    @Override
    @Transactional
    public void cancel(Long tripId) throws EntityNotFoundException {
        ITripDAO tripDAO = daoFactory.createTripDAO();
        ITrip tripModel = tripDAO.findById(tripId);
        if (tripModel == null) {
            throw new EntityNotFoundException("Trip not found");
        }
        tripModel.setState(TripState.CANCELLED);
    }

    @Override
    @Transactional
    public boolean addStop(TripDTO trip, Long locationId) throws EntityNotFoundException, IllegalStateException {
        ITripDAO tripDAO = daoFactory.createTripDAO();
        ITrip tripModel = tripDAO.findById(trip.getId());
        if (tripModel == null) {
            throw new EntityNotFoundException("Trip not found");
        }
        if (tripModel.getState() != TripState.CREATED) {
            throw new IllegalStateException("Trip is not in CREATED state");
        }

        ILocationDAO locationDAO = daoFactory.createLocationDAO();
        ILocation location = locationDAO.findById(locationId);
        if (location == null) {
            throw new EntityNotFoundException("Location not found");
        }

        if (trip.getStops().contains(locationId)) {
            return false;
        }

        // Update the entity
        tripModel.addStop(location);

        // Update the DTO
        trip.getStops().add(locationId);

        recalculateFare(trip);

        return true;
    }

    @Override
    @Transactional
    public boolean removeStop(TripDTO trip, Long locationId) throws EntityNotFoundException, IllegalStateException {
        ITripDAO tripDAO = daoFactory.createTripDAO();
        ITrip tripModel = tripDAO.findById(trip.getId());
        if (tripModel == null) {
            throw new EntityNotFoundException("Trip not found");
        }
        if (tripModel.getState() != TripState.CREATED) {
            throw new IllegalStateException("Trip is not in CREATED state");
        }

        ILocationDAO locationDAO = daoFactory.createLocationDAO();
        ILocation location = locationDAO.findById(locationId);
        if (location == null) {
            throw new EntityNotFoundException("Location not found");
        }

        if (!trip.getStops().contains(locationId)) {
            return false;
        }

        // Update the entity
        Collection<ILocation> modelStops = tripModel.getStops();
        modelStops.removeIf(l -> l.getId().equals(locationId));
        tripModel.setStops(modelStops);

        // Update the DTO
        // RemoveIf to remove all occurrences of locationId
        trip.getStops().removeIf(l -> l.equals(locationId));
        recalculateFare(trip);

        return true;
    }

    @Override
    @Transactional
    public void delete(Long tripId) throws EntityNotFoundException {
        ITripDAO tripDAO = daoFactory.createTripDAO();

        ITrip tripModel = tripDAO.findById(tripId);
        if (tripModel == null) {
            throw new EntityNotFoundException("Trip not found");
        }

        entityManager.remove(tripModel);
    }

    @Override
    public TripDTO find(Long tripId) {
        ITripDAO tripDAO = daoFactory.createTripDAO();
        ITrip tripModel = tripDAO.findById(tripId);
        return tripModelToDTO(tripModel);
    }
}
