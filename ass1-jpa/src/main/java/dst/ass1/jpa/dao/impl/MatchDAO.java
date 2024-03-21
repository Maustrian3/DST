package dst.ass1.jpa.dao.impl;

import dst.ass1.jpa.dao.IMatchDAO;
import dst.ass1.jpa.model.IEmployment;
import dst.ass1.jpa.model.IMatch;
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
        return em.find(IMatch.class, id);
    }

    @Override
    public List<IMatch> findAll() {
        return new ArrayList<>(
                em.createQuery("SELECT m FROM Match m", Match.class).getResultList());
    }
}
