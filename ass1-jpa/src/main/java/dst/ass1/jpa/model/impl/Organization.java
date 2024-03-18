package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.IEmployment;
import dst.ass1.jpa.model.IOrganization;
import dst.ass1.jpa.model.IVehicle;

import javax.persistence.*;
import java.util.Collection;

import static dst.ass1.jpa.util.Constants.*;

@Entity
public class Organization implements IOrganization {
    @Id
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(name = J_ORGANIZATION_VEHICLE,
            joinColumns = @JoinColumn(name = "organization_id"),
            inverseJoinColumns = @JoinColumn(name = "vehicle_id"))
    private Collection<Vehicle> vehicles;

    @OneToMany(mappedBy = "driver")
    private Collection<Driver> drivers;

    @ManyToMany(fetch = FetchType.LAZY) // TODO check why lazy load here
    @JoinTable(
            name = J_ORGANIZATION_PARTS,
            joinColumns = @JoinColumn(name = I_ORGANIZATION_PART_OF),
            inverseJoinColumns = @JoinColumn(name = I_ORGANIZATION_PARTS)
    )
    private Collection<Organization> partOf;

    @ManyToMany(mappedBy = "partOf")
    private Collection<Organization> parts;

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
}
