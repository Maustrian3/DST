package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.IPaymentInfo;
import dst.ass1.jpa.model.PaymentMethod;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PaymentInfo implements IPaymentInfo {
    @Id
    private Long id;

    private String name;

    private PaymentMethod method;

    private Boolean preferred;

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

    public PaymentMethod getPaymentMethod() {
        return method;
    }

    public void setPaymentMethod(PaymentMethod method) {
        this.method = method;
    }

    public Boolean isPreferred() {
        return preferred;
    }

    @Override
    public void setPreferred(Boolean preferred) {
        this.preferred = preferred;
    }
}
