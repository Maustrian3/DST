package dst.ass1.jpa.dao.impl;

import dst.ass1.jpa.dao.ITripInfoDAO;
import dst.ass1.jpa.model.ITripInfo;
import dst.ass1.jpa.model.impl.Rider;
import dst.ass1.jpa.model.impl.TripInfo;
import dst.ass1.jpa.util.TupleResult;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

public class TripInfoDAO implements ITripInfoDAO {

    private final EntityManager em;

    public TripInfoDAO(EntityManager em) {
        this.em = em;
    }

    @Override
    public ITripInfo findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must be provided");
        }
        return em.find(TripInfo.class, id);
    }

    @Override
    public List<ITripInfo> findAll() {
        return new ArrayList<>(
                em.createQuery("SELECT e FROM TripInfo e", TripInfo.class).getResultList());
    }

    @Override
    public List<TupleResult<Long, Double>> findRidersAverageRating() {
        TypedQuery<Object[]> query = em.createNamedQuery("averageRatingByRider", Object[].class);
        List<Object[]> resultList = query.getResultList();

        List<TupleResult<Long, Double>> tupleResults = new ArrayList<>();
        for (Object[] result : resultList) {
            Long riderId = (Long) result[0];
            Double averageRating = (Double) result[1];
            tupleResults.add(new TupleResult<>(riderId, averageRating));
        }
        return tupleResults;
    }
}
