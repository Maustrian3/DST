package dst.ass2.service.auth.impl;

import dst.ass1.jpa.dao.IDAOFactory;
import dst.ass1.jpa.dao.IRiderDAO;
import dst.ass1.jpa.model.IRider;
import dst.ass2.service.api.auth.AuthenticationException;
import dst.ass2.service.api.auth.NoSuchUserException;
import dst.ass2.service.auth.ICachingAuthenticationService;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@ManagedBean
public class CachingAuthenticationService implements ICachingAuthenticationService {

    @PersistenceContext(name = "dst")
    private EntityManager entityManager;

    @Inject
    IDAOFactory daoFactory;

    // Key: email, Value: password
    ConcurrentHashMap<String, byte[]> passwordMap = new ConcurrentHashMap<>();

    // Key: token, Value: email
    ConcurrentHashMap<String, String> emailMap = new ConcurrentHashMap<>();

    private byte[] hashPassword(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            return messageDigest.digest(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void changePassword(String email, String newPassword) throws NoSuchUserException {
        IRider rider = daoFactory.createRiderDAO().findByEmail(email);
        if (rider == null) {
            throw new NoSuchUserException();
        }

        byte[] passwordHash = hashPassword(newPassword);

        // Update Database
        rider.setPassword(passwordHash);
        // Update Cache
        passwordMap.put(email, passwordHash);
    }

    @Override
    public String getUser(String token) {
        String usermail = emailMap.get(token);
        if (usermail == null) {
            return null;
        }

        return usermail;
    }

    @Override
    public boolean isValid(String token) {
        if (token == null) {
            return false;
        }
        return emailMap.containsKey(token);
    }

    @Override
    public boolean invalidate(String token) {
        if (token == null) {
            return false;
        }
        return emailMap.remove(token) != null;
    }

    @Override
    @Transactional
    public String authenticate(String email, String password) throws NoSuchUserException, AuthenticationException {
        byte[] passwordHash = passwordMap.get(email);
        if (passwordHash == null) {
            IRider rider = daoFactory.createRiderDAO().findByEmail(email);
            if (rider == null) {
                throw new NoSuchUserException();
            }
            passwordHash = rider.getPassword();
            passwordMap.put(email, passwordHash);
        }

        byte[] inputHash = hashPassword(password);
        if (!MessageDigest.isEqual(passwordHash, inputHash)) {
            throw new AuthenticationException();
        }

        // Generate new token
        String token = java.util.UUID.randomUUID().toString();
        emailMap.put(token, email);
        return token;
    }

    @Override
    @PostConstruct
    public void loadData() {
        IRiderDAO riderDAO = daoFactory.createRiderDAO();
        for (IRider rider : riderDAO.findAll()) {
            passwordMap.put(rider.getEmail(), rider.getPassword());
        }
    }

    @Override
    public void clearCache() {
        passwordMap.clear();
    }
}
