package dst.ass1.jpa.dao.impl;

import dst.ass1.jpa.dao.IRiderDAO;
import dst.ass1.jpa.model.IRider;
import dst.ass1.jpa.model.impl.Rider;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RiderDAO implements IRiderDAO {

    private final EntityManager em;

    public RiderDAO(EntityManager em) {
        this.em = em;
    }

    @Override
    public IRider findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must be provided");
        }
        return em.find(Rider.class, id);
    }

    @Override
    public List<IRider> findAll() {
        return new ArrayList<>(
                em.createQuery("SELECT e FROM Rider e", Rider.class).getResultList());
    }

    @Override
    public List<IRider> findRidersWithNoTrips(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates must be provided");
        }
        TypedQuery<Rider> riderQuery = em.createNamedQuery("findAllRiders", Rider.class);
        List<Rider> allRiders = riderQuery.getResultList();

        TypedQuery<Rider> tripInfoQuery = em.createNamedQuery("findTripInfoWithinTimeRange", Rider.class)
                .setParameter("startDate", start)
                .setParameter("endDate", end);
        List<Rider> ridersWithTrips = tripInfoQuery.getResultList();

        // Filter riders who have trips within the specified time range
        allRiders.removeIf(rider -> ridersWithTrips.contains(rider));

        return new ArrayList<>(allRiders);
    }

    @Override
    public IRider findByEmail(String email) {
        if(email == null) {
            throw new IllegalArgumentException("Email must be provided");
        }
        return em.createNamedQuery("riderByEmail", Rider.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}
