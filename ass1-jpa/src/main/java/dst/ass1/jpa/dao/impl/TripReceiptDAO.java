package dst.ass1.jpa.dao.impl;

import dst.ass1.jpa.dao.ITripReceiptDAO;
import dst.ass1.jpa.model.ITripReceipt;
import dst.ass1.jpa.model.PaymentMethod;
import dst.ass1.jpa.model.impl.PaymentInfo;
import dst.ass1.jpa.model.impl.TripInfo;
import dst.ass1.jpa.model.impl.TripReceipt;
import dst.ass1.jpa.util.TupleResult;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TripReceiptDAO implements ITripReceiptDAO {

    private final EntityManager em;

    public TripReceiptDAO(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<TupleResult<PaymentMethod, Double>> calculateAverageTipPerPaymentMethod(Date start, Date end) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        // Define root entity and join necessary entities
        Root<TripReceipt> tripReceiptRoot = query.from(TripReceipt.class);
        Join<TripReceipt, TripInfo> tripInfoJoin = tripReceiptRoot.join("tripInfo");
        Join<TripReceipt, PaymentInfo> paymentInfoJoin = tripReceiptRoot.join("paymentInfo");

        // Define selection for grouping by PaymentMethod and calculating average tip percentage
        Expression<PaymentMethod> paymentMethodExpr = paymentInfoJoin.get("method");

        Expression<BigDecimal> totalAmount = tripReceiptRoot.get("total").get("currencyValue");
        Expression<BigDecimal> tipAmount = tripReceiptRoot.get("tip").get("currencyValue");

        // ((totalAmount - tipAmount) * 100) / totalAmount
        Expression<BigDecimal> difference = cb.diff(totalAmount, tipAmount);
        Expression<Number> tipPercentage = cb.quot(cb.prod(difference, 100), totalAmount);
        Expression<Double> avgTipPercentageExpr = cb.avg(tipPercentage);

        query.multiselect(paymentMethodExpr, avgTipPercentageExpr);
        query.groupBy(paymentMethodExpr);

        // Apply optional criteria for time range
        List<Predicate> predicates = new ArrayList<>();
        if (start != null) {
            predicates.add(cb.greaterThanOrEqualTo(tripInfoJoin.get("completed"), start));
        }
        if (end != null) {
            predicates.add(cb.lessThanOrEqualTo(tripInfoJoin.get("completed"), end));
        }
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }

        // Define ordering by average tip percentage in descending order
        query.orderBy(cb.desc(avgTipPercentageExpr));

        // Execute query
        List<Tuple> resultList = em.createQuery(query).getResultList();

        // Cast Tuple objects to TupleResult objects
        List<TupleResult<PaymentMethod, Double>> tupleResultList = new ArrayList<>();
        for (int i = resultList.size() - 1; i >= 0; i--) {
            Tuple tuple = resultList.get(i);
            PaymentMethod paymentMethod = tuple.get(paymentMethodExpr);
            Double avgTipPercentage = 100.0 - tuple.get(avgTipPercentageExpr);
            tupleResultList.add(new TupleResult<>(paymentMethod, avgTipPercentage));
        }

        return tupleResultList;
    }
}
