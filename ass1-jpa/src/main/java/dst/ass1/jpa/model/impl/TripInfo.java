package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.ITrip;
import dst.ass1.jpa.model.ITripInfo;
import dst.ass1.jpa.model.ITripReceipt;

import javax.persistence.*;
import java.util.Date;

import static dst.ass1.jpa.util.Constants.*;

@Entity
@NamedQuery(
        name = "averageRatingByRider",
        query = "SELECT r.id, AVG(t.riderRating) " +
                "FROM TripInfo t " +
                "JOIN t.trip trip " +
                "JOIN trip.rider r " +
                "GROUP BY r " +
                "ORDER BY AVG(t.riderRating) DESC"
)
@NamedQuery(
        name = "findTripInfoWithinTimeRange",
        query = "SELECT t.rider " +
                "FROM TripInfo ti " +
                "JOIN ti.trip t " +
                "WHERE ti.completed BETWEEN :startDate AND :endDate"
)
public class TripInfo implements ITripInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date completed;

    private Double distance;

    private Integer driverRating;

    private Integer riderRating;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private TripReceipt tripReceipt;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
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
