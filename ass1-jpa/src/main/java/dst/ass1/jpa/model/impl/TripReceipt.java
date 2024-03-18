package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.IMoney;
import dst.ass1.jpa.model.ITripReceipt;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TripReceipt implements ITripReceipt {
    @Id
    private Long id;

    @Embedded
    private IMoney total;

    @Embedded
    private IMoney tip;

    private Boolean paid;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public IMoney getTotal() {
        return total;
    }

    public void setTotal(IMoney total) {
        this.total = total;
    }

    @Override
    public IMoney getTip() {
        return tip;
    }

    public void setTip(IMoney tip) {
        this.tip = tip;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }
}
