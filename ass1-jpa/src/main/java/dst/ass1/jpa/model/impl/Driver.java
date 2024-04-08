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

    @ManyToOne(optional = false)
    private Vehicle vehicle;

    // Mapped by the related entity (Employment) through part of its composite key (id.driver)
    @OneToMany(mappedBy = "id.driver")
    private Collection<Employment> employments = new ArrayList<>();

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
