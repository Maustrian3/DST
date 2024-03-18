package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.IEmployment;
import dst.ass1.jpa.model.IEmploymentKey;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Employment implements IEmployment {
    @EmbeddedId
    private EmploymentKey id;
    private Date since;
    private Boolean active;

    @Override
    public IEmploymentKey getId() {
        return this.id;
    }

    @Override
    public void setId(IEmploymentKey employmentKey) {
        this.id = (EmploymentKey) employmentKey;
    }

    @Override
    public Date getSince() {
        return this.since;
    }

    @Override
    public void setSince(Date since) {
        this.since = since;
    }

    @Override
    public Boolean isActive() {
        return this.active;
    }

    @Override
    public void setActive(Boolean active) {
        this.active = active;
    }
}
