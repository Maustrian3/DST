package dst.ass1.jpa.dao.impl;

import dst.ass1.jpa.dao.ITripDAO;
import dst.ass1.jpa.model.ITrip;
import dst.ass1.jpa.model.TripState;
import dst.ass1.jpa.model.impl.Trip;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class TripDAO implements ITripDAO {

    private final EntityManager em;

    public TripDAO(EntityManager em) {
        this.em = em;
    }

    @Override
    public ITrip findById(Long id) {
        return em.find(ITrip.class, id);
    }

    @Override
    public List<ITrip> findAll() {
        return new ArrayList<>(
                em.createQuery("SELECT t FROM Trip t", Trip.class).getResultList());
    }

    @Override
    public List<ITrip> findByStatus(TripState state) {
        return new ArrayList<>(
                em.createQuery("SELECT t FROM Trip t WHERE t.state = :state", Trip.class)
                        .setParameter("state", state)
                        .getResultList());
    }
}
