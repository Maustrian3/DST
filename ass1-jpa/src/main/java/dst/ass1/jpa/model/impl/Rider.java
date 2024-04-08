package dst.ass1.jpa.model.impl;

import dst.ass1.jpa.model.IPaymentInfo;
import dst.ass1.jpa.model.IRider;
import dst.ass1.jpa.model.ITrip;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import static dst.ass1.jpa.util.Constants.I_PAYMENT_INFO;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name","email"})})
@NamedQuery(
        name = "riderByEmail",
        query = "SELECT r FROM Rider r WHERE r.email = :email"
)
public class Rider extends PlatformUser implements IRider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(length = 20)
    private byte[] password;

    @OneToMany
    private Collection<PaymentInfo> paymentInfos = new ArrayList<>();

    @OneToMany(mappedBy = "rider")
    private Collection<Trip> trips = new ArrayList<>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public byte[] getPassword() {
        return password;
    }

    @Override
    public void setPassword(byte[] password) {
        this.password = password;
    }

    @Override
    public Collection<ITrip> getTrips() {
        return new ArrayList<>(this.trips);
    }

    @Override
    public void setTrips(Collection<ITrip> trips) {
        this.trips.clear();
        for(ITrip trip : trips) {
            this.trips.add((Trip) trip);
        }
    }

    @Override
    public void addTrip(ITrip trip) {
        this.trips.add((Trip) trip);
    }

    @Override
    public Collection<IPaymentInfo> getPaymentInfos() {
        return new ArrayList<>(this.paymentInfos);
    }

    @Override
    public void setPaymentInfos(Collection<IPaymentInfo> paymentInfos) {
        this.paymentInfos.clear();
        for(IPaymentInfo paymentInfo : paymentInfos) {
            this.paymentInfos.add((PaymentInfo) paymentInfo);
        }
    }
}
