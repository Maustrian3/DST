package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.IMatch;
import dst.ass1.jpa.model.IMoney;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

import static dst.ass1.jpa.util.Constants.I_DRIVER;
import static dst.ass1.jpa.util.Constants.I_VEHICLE;

@Entity
public class Match implements IMatch {
    @Id
    private Long id;

    private Date date;

    @Embedded
    private IMoney fare;

    @ManyToOne
    @JoinColumn(name = I_VEHICLE, nullable = false)
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = I_DRIVER, nullable = false)
    private Driver driver;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public IMoney getFare() {
        return fare;
    }

    public void setFare(IMoney fare) {
        this.fare = fare;
    }
}
