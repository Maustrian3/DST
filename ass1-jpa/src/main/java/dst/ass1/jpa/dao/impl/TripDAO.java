package dst.ass1.jpa.dao.impl;

import dst.ass1.jpa.dao.ITripDAO;
import dst.ass1.jpa.model.ITrip;
import dst.ass1.jpa.model.TripState;
import dst.ass1.jpa.model.impl.Rider;
import dst.ass1.jpa.model.impl.Trip;

import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TripDAO implements ITripDAO {

    private final EntityManager em;

    public TripDAO(EntityManager em) {
        this.em = em;
    }

    @Override
    public ITrip findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must be provided");
        }
        return em.find(Trip.class, id);
    }

    @Override
    public List<ITrip> findAll() {
        return new ArrayList<>(
                em.createQuery("SELECT t FROM Trip t", Trip.class).getResultList());
    }

    @Override
    public List<ITrip> findByStatus(TripState state) {
        if (state == null) {
            throw new IllegalArgumentException("State must be provided");
        }
        return new ArrayList<>(
                em.createNamedQuery("tripsByStatus", Trip.class)
                .setParameter("state", state)
                .getResultList());
    }
}
