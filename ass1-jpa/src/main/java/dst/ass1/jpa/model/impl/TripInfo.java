package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.ITripInfo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class TripInfo implements ITripInfo {
    @Id
    private Long id;

    private Date completed;

    private Double distance;

    private Integer driverRating;

    private Integer riderRating;

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
}
