package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.IEmployment;
import dst.ass1.jpa.model.IOrganization;
import dst.ass1.jpa.model.IVehicle;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

import static dst.ass1.jpa.util.Constants.*;

@Entity
public class Organization implements IOrganization {
    @Id
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(name = J_ORGANIZATION_VEHICLE,
            joinColumns = @JoinColumn(name = I_ORGANIZATION),
            inverseJoinColumns = @JoinColumn(name = I_VEHICLE))
    private Collection<Vehicle> vehicles;

    @OneToMany(mappedBy = "id.organization") // Mapped by the related entity (Employment) through part of its composite key (id.organization)
    private Collection<Employment> employments;

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

    @Override
    public Collection<IOrganization> getParts() {
        return new ArrayList<>(parts);
    }

    @Override
    public void setParts(Collection<IOrganization> parts) {
        this.parts.clear();
        for (IOrganization part : parts) {
            this.parts.add((Organization) part);
        }
    }

    @Override
    public void addPart(IOrganization part) {
        this.parts.add((Organization) part);
    }

    @Override
    public Collection<IOrganization> getPartOf() {
        return new ArrayList<>(partOf);
    }

    @Override
    public void setPartOf(Collection<IOrganization> partOf) {
        this.partOf.clear();
        for (IOrganization part : partOf) {
            this.partOf.add((Organization) part);
        }
    }

    @Override
    public void addPartOf(IOrganization partOf) {
        this.partOf.add((Organization) partOf);
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
    public Collection<IVehicle> getVehicles() {
        return new ArrayList<>(vehicles);
    }

    @Override
    public void setVehicles(Collection<IVehicle> vehicles) {
        this.vehicles.clear();
        for (IVehicle vehicle : vehicles) {
            this.vehicles.add((Vehicle) vehicle);
        }
    }

    @Override
    public void addVehicle(IVehicle vehicle) {
        this.vehicles.add((Vehicle) vehicle);
    }
}
