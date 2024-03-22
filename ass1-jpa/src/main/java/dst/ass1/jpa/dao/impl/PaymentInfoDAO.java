package dst.ass1.jpa.dao.impl;

import dst.ass1.jpa.dao.IPaymentInfoDAO;
import dst.ass1.jpa.model.IPaymentInfo;
import dst.ass1.jpa.model.impl.PaymentInfo;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class PaymentInfoDAO implements IPaymentInfoDAO {

    private final EntityManager em;

    public PaymentInfoDAO(EntityManager em) {
        this.em = em;
    }
}
