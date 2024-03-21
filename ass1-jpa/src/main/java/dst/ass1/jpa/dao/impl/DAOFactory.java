package dst.ass1.jpa.dao.impl;

import dst.ass1.jpa.dao.*;

import javax.persistence.EntityManager;

public class DAOFactory implements IDAOFactory {

    private EntityManager em;

    public DAOFactory(EntityManager em) {
        this.em = em;
    }

    @Override
    public IDriverDAO createDriverDAO() {
        return new DriverDAO(em);
    }

    @Override
    public IEmploymentDAO createEmploymentDAO() {
        return new EmploymentDAO(em);
    }

    @Override
    public ILocationDAO createLocationDAO() {
        // TODO
        return null;
    }

    @Override
    public IMatchDAO createMatchDAO() {
        return new MatchDAO(em);
    }

    @Override
    public IOrganizationDAO createOrganizationDAO() {
        return new OrganizationDAO(em);
    }

    @Override
    public IRiderDAO createRiderDAO() {
        // TODO
        return null;
    }

    @Override
    public ITripDAO createTripDAO() {
        // TODO
        return null;
    }

    @Override
    public ITripInfoDAO createTripInfoDAO() {
        // TODO
        return null;
    }

    @Override
    public IVehicleDAO createVehicleDAO() {
        return new VehicleDAO(em);
    }

    @Override
    public ITripReceiptDAO createTripReceiptDAO() {
        // TODO
        return null;
    }

    @Override
    public IPaymentInfoDAO createPaymentInfoDAO() {
        // TODO
        return null;
    }
}
