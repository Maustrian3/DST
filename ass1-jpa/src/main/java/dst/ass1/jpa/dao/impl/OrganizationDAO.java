package dst.ass1.jpa.dao.impl;

import dst.ass1.jpa.dao.IOrganizationDAO;
import dst.ass1.jpa.model.IEmployment;
import dst.ass1.jpa.model.IOrganization;
import dst.ass1.jpa.model.impl.Employment;
import dst.ass1.jpa.model.impl.Organization;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class OrganizationDAO implements IOrganizationDAO {

    private final EntityManager em;

    public OrganizationDAO(EntityManager em) {
        this.em = em;
    }

    @Override
    public IOrganization findById(Long id) {
        return em.find(IOrganization.class, id);
    }

    @Override
    public List<IOrganization> findAll() {
        return new ArrayList<>(
                em.createQuery("SELECT o FROM Organization o", Organization.class).getResultList());
    }
}
