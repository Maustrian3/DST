package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.IDriver;
import dst.ass1.jpa.model.IEmploymentKey;
import dst.ass1.jpa.model.IOrganization;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
public class EmploymentKey implements IEmploymentKey, Serializable {
    @ManyToOne
    private Driver driver;

    @ManyToOne
    private Organization organization;

    @Override
    public Driver getDriver() {
        return driver;
    }

    public void setDriver(IDriver driver) {
        this.driver = (Driver) driver;
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(IOrganization organization) {
        this.organization = (Organization) organization;
    }
}
