package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.IMoney;
import dst.ass1.jpa.model.IPaymentInfo;
import dst.ass1.jpa.model.ITripInfo;
import dst.ass1.jpa.model.ITripReceipt;

import javax.persistence.*;

import static dst.ass1.jpa.util.Constants.*;

@Entity
public class TripReceipt implements ITripReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private IMoney total;

    @Embedded
    private IMoney tip;

    private Boolean paid;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = I_TRIP_RECEIPT)
    private TripInfo tripInfo;

    @ManyToOne(optional = false)
    private PaymentInfo paymentInfo;

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

    @Override
    public void setTotal(IMoney total) {
        this.total = total;
    }

    @Override
    public IMoney getTip() {
        return tip;
    }

    @Override
    public void setTip(IMoney tip) {
        this.tip = tip;
    }

    @Override
    public boolean isPaid() {
        return paid;
    }

    @Override
    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    @Override
    public IPaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    @Override
    public void setPaymentInfo(IPaymentInfo paymentInfo) {
        this.paymentInfo = (PaymentInfo) paymentInfo;
    }

    @Override
    public ITripInfo getTripInfo() {
        return tripInfo;
    }

    @Override
    public void setTripInfo(ITripInfo tripInfo) {
        this.tripInfo = (TripInfo) tripInfo;
    }
}
