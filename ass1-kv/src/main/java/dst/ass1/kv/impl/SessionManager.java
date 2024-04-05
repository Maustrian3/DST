package dst.ass1.kv.impl;

import dst.ass1.kv.ISessionManager;
import dst.ass1.kv.SessionCreationFailedException;
import dst.ass1.kv.SessionNotFoundException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SessionManager implements ISessionManager {

    private final JedisPool pool;

    public SessionManager(JedisPool jedisPool) {
        this.pool = jedisPool;
    }

    @Override
    public String createSession(Long userId, int timeToLive) throws SessionCreationFailedException {
        try (Jedis jedis = pool.getResource()) {

            // Set up a transaction
            // Watch the user id to ensure that no other session is created for the same user
            jedis.watch(userId.toString());
            // Create a transaction
            Transaction transaction = jedis.multi();

            String sessionToken = UUID.randomUUID().toString();
            Map<String, String> value = Map.of(
                    "userId", userId.toString(),
                    "timeToLive", ((Integer) timeToLive).toString()
            );

            // Set session metadata and user id and their expiration time
            transaction.hmset(sessionToken, value);
            transaction.expire(sessionToken, timeToLive);
            transaction.set(userId.toString(), sessionToken);
            transaction.expire(userId.toString(), timeToLive);

            List<Object> responses = transaction.exec();
            if (responses == null) throw new SessionCreationFailedException();

            return sessionToken;
        } catch (JedisException e) {
            throw new SessionCreationFailedException("Failed to create session", e);
        }
    }

    @Override
    public void setSessionVariable(String sessionId, String key, String value) throws SessionNotFoundException {
        try (var jedis = pool.getResource()) {
            if (!jedis.exists(sessionId)) throw new SessionNotFoundException();
            jedis.hset(sessionId, key, value);
        } catch (JedisException e) {
            throw new SessionNotFoundException("Failed to set session variable", e);
        }
    }

    @Override
    public String getSessionVariable(String sessionId, String key) throws SessionNotFoundException {
        try (var jedis = pool.getResource()) {
            if (!jedis.exists(sessionId)) throw new SessionNotFoundException();
            return jedis.hget(sessionId, key);
        } catch (JedisException e) {
            throw new SessionNotFoundException("Failed to get session variable", e);
        }
    }

    @Override
    public Long getUserId(String sessionId) throws SessionNotFoundException {
        return Long.parseLong(getSessionVariable(sessionId, "userId"));
    }

    @Override
    public int getTimeToLive(String sessionId) throws SessionNotFoundException {
        return Integer.parseInt(getSessionVariable(sessionId, "timeToLive"));
    }

    @Override
    public String requireSession(Long userId, int timeToLive) throws SessionCreationFailedException {
        try (var jedis = pool.getResource()) {

            // If a session already exists, return the session token
            String sessionToken = jedis.get(userId.toString());
            if (sessionToken != null) {
                return sessionToken;
            }

            // Otherwise, create a new session
            return createSession(userId, timeToLive);

        } catch (JedisException e) {
            throw new SessionCreationFailedException("Failed to delete existing session", e);
        }
    }

    @Override
    public void close() {

    }
}
