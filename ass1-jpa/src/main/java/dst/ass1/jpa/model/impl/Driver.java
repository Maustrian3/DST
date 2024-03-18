package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.IDriver;
import dst.ass1.jpa.model.IEmployment;
import dst.ass1.jpa.model.IVehicle;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;

import static dst.ass1.jpa.util.Constants.I_VEHICLE;

@Entity
public class Driver extends PlatformUser implements IDriver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String tel;

    private Double avgRating;
    @ManyToOne
    @JoinColumn(name = I_VEHICLE, nullable = false)
    private Vehicle vehicle;

    @OneToMany(mappedBy = "organization")
    private Collection<Employment> employments;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTel() {
        return tel;
    }

    @Override
    public void setTel(String tel) {
        this.tel = tel;
    }

    @Override
    public Double getAvgRating() {
        return avgRating;
    }

    @Override
    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }
}
