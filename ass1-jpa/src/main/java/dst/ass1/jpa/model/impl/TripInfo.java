package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.ITrip;
import dst.ass1.jpa.model.ITripInfo;
import dst.ass1.jpa.model.ITripReceipt;

import javax.persistence.*;
import java.util.Date;

import static dst.ass1.jpa.util.Constants.I_TRIP;
import static dst.ass1.jpa.util.Constants.I_TRIP_RECEIPT;

@Entity
public class TripInfo implements ITripInfo {
    @Id
    private Long id;

    private Date completed;

    private Double distance;

    private Integer driverRating;

    private Integer riderRating;

    @OneToOne(mappedBy = "tripreceipt", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private TripReceipt tripReceipt;

    @ManyToOne(cascade =  CascadeType.ALL )
    @JoinColumn(name = I_TRIP)
    //@NotFound(action=NotFoundAction.IGNORE) // TODO Check is this necessary here?
    private Trip trip;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Date getCompleted() {
        return completed;
    }

    @Override
    public void setCompleted(Date completed) {
        this.completed = completed;
    }

    @Override
    public Double getDistance() {
        return distance;
    }

    @Override
    public void setDistance(Double distance) {
        this.distance = distance;
    }

    @Override
    public Integer getDriverRating() {
        return driverRating;
    }

    public void setDriverRating(Integer driverRating) {
        this.driverRating = driverRating;
    }

    @Override
    public Integer getRiderRating() {
        return riderRating;
    }

    public void setRiderRating(Integer riderRating) {
        this.riderRating = riderRating;
    }

    @Override
    public ITrip getTrip() {
        return trip;
    }

    @Override
    public void setTrip(ITrip trip) {
        this.trip = (Trip) trip;
    }

    @Override
    public ITripReceipt getReceipt() {
        return tripReceipt;
    }

    @Override
    public void setReceipt(ITripReceipt receipt) {
        this.tripReceipt = (TripReceipt) receipt;
    }
}
