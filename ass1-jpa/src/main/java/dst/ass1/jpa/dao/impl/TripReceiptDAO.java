package dst.ass1.jpa.dao.impl;

import dst.ass1.jpa.dao.ITripReceiptDAO;
import dst.ass1.jpa.model.ITripReceipt;
import dst.ass1.jpa.model.PaymentMethod;
import dst.ass1.jpa.model.impl.TripReceipt;
import dst.ass1.jpa.util.TupleResult;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TripReceiptDAO implements ITripReceiptDAO {

    private final EntityManager em;

    public TripReceiptDAO(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<TupleResult<PaymentMethod, Double>> calculateAverageTipPerPaymentMethod(Date start, Date end) {
        // TODO
        return null;
    }
}
