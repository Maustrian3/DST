package dst.ass1.jpa.dao.impl;

import dst.ass1.jpa.dao.IRiderDAO;
import dst.ass1.jpa.model.IRider;
import dst.ass1.jpa.model.impl.Rider;

import javax.persistence.EntityManager;
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
        return em.find(IRider.class, id);
    }

    @Override
    public List<IRider> findAll() {
        return new ArrayList<>(
                em.createQuery("SELECT e FROM Rider e", Rider.class).getResultList());
    }

    @Override
    public List<IRider> findRidersWithNoTrips(Date start, Date end) {
        // TODO check if the query is correct
        return new ArrayList<>(
                em.createQuery("SELECT r FROM Rider r WHERE r.trips IS EMPTY", Rider.class).getResultList());
    }

    @Override
    public IRider findByEmail(String email) {
        return em.find(IRider.class, email);
    }
}
