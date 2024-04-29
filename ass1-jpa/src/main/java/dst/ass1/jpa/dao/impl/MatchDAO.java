package dst.ass1.jpa.dao.impl;

import dst.ass1.jpa.dao.IMatchDAO;
import dst.ass1.jpa.model.IDriver;
import dst.ass1.jpa.model.IEmployment;
import dst.ass1.jpa.model.IMatch;
import dst.ass1.jpa.model.ITrip;
import dst.ass1.jpa.model.impl.Employment;
import dst.ass1.jpa.model.impl.Match;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class MatchDAO implements IMatchDAO {

    private final EntityManager em;

    public MatchDAO(EntityManager em) {
        this.em = em;
    }

    @Override
    public IMatch findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must be provided");
        }
        return em.find(Match.class, id);
    }

    @Override
    public List<IMatch> findAll() {
        return new ArrayList<>(
                em.createQuery("SELECT m FROM Match m", Match.class).getResultList());
    }

    public List<IMatch> findByDriverAndStates(Long driverId, List<String> states) {
        if (driverId == null) {
            throw new IllegalArgumentException("Driver must be provided");
        }
        if (states == null) {
            throw new IllegalArgumentException("States must be provided");
        }
        return new ArrayList<>(
                em.createNamedQuery("findMatchByDriverAndStates", Match.class)
                        .setParameter("driver", driverId)
                        .setParameter("states", states)
                        .getResultList());
    }
}
