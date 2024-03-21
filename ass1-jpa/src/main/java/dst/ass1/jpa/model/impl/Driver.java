package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.IDriver;
import dst.ass1.jpa.model.IEmployment;
import dst.ass1.jpa.model.IVehicle;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static dst.ass1.jpa.util.Constants.I_DRIVER;
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

    @OneToMany(mappedBy = "id.driver") // Mapped by the related entity (Employment) through part of its composite key (id.driver)
    private Collection<Employment> employments = new ArrayList<>();

//    @ManyToMany(mappedBy = "trip") // TODO Check is this no needed here? No methods for it in interface
//    private Collection<Trip> trips;

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

    @Override
    public Collection<IEmployment> getEmployments() {
        return new ArrayList<>(employments);
    }

    @Override
    public void setEmployments(Collection<IEmployment> employments) {
        this.employments.clear();
        for (IEmployment employment : employments) {
            this.employments.add((Employment) employment);
        }
    }

    @Override
    public void addEmployment(IEmployment employment) {
        this.employments.add((Employment) employment);
    }

    @Override
    public IVehicle getVehicle() {
        return vehicle;
    }

    @Override
    public void setVehicle(IVehicle vehicle) {
        this.vehicle = (Vehicle) vehicle;
    }
}
