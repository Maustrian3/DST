package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.*;
import dst.ass1.jpa.model.impl.Location;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import static dst.ass1.jpa.util.Constants.*;

public class Trip implements ITrip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date created;
    private Date update;

    private TripState state;

    private TripInfo tripInfo;

    private Match match;
    private Rider rider;

    private Location pickup;
    private Collection<Location> stops = new ArrayList<>();
    private Location destination;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public Date getUpdated() {
        return update;
    }

    @Override
    public void setUpdated(Date updated) {
        this.update = updated;
    }

    @Override
    public TripState getState() {
        return state;
    }

    @Override
    public void setState(TripState state) {
        this.state = state;
    }

    @Override
    public ILocation getPickup() {
        return pickup;
    }

    @Override
    public void setPickup(ILocation pickup) {
        this.pickup = (Location) pickup;
    }

    @Override
    public ILocation getDestination() {
        return destination;
    }

    @Override
    public void setDestination(ILocation destination) {
        this.destination = (Location) destination;
    }

    @Override
    public Collection<ILocation> getStops() {
        return new ArrayList<>(stops);
    }

    @Override
    public void setStops(Collection<ILocation> stops) {
        this.stops.clear();
        for (ILocation stop : stops) {
            this.stops.add((Location) stop);
        }
    }

    @Override
    public void addStop(ILocation stop) {
        this.stops.add((Location) stop);
    }

    @Override
    public ITripInfo getTripInfo() {
        return tripInfo;
    }

    @Override
    public void setTripInfo(ITripInfo tripInfo) {
        this.tripInfo = (TripInfo) tripInfo;
    }

    @Override
    public IMatch getMatch() {
        return match;
    }

    @Override
    public void setMatch(IMatch match) {
        this.match = (Match) match;
    }

    @Override
    public IRider getRider() {
        return rider;
    }

    @Override
    public void setRider(IRider rider) {
        this.rider = (Rider) rider;
    }
}
