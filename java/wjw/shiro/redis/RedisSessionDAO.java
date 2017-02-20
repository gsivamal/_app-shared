package wjw.shiro.redis;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisSessionDAO extends CachingSessionDAO {
	
	  private static Logger logger = LoggerFactory.getLogger(RedisSessionDAO.class);
	
	  //private RedisPoolManager redisManager;
	
	  // The Redis key prefix for the sessions
	  private String keyPrefix = RedisPoolManager.DEFAULT_ROOTKEY + "session:";
	
	  // all_sessions key prefix
	  private String all_sessions_Key = keyPrefix + "all_sessions";	
	
	  
	  @Override
	  protected void doDelete(Session session) {
	    if (session == null || session.getId() == null) {
	      logger.error("session or session id is null");
	      return;
	    }
	    
	    //salai: there will be no sessionid key, so..nothing would happen here
	    RedisPoolManager.del(this.getByteKey(session.getId()));
	
	    RedisPoolManager.srem(all_sessions_Key, session.getId().toString());
	  }
	
	  @Override
	  protected void doUpdate(Session session) {
	    this.saveSession(session);
	  }
	
	  @Override
	  public Collection<Session> getActiveSessions() {
	    super.getActiveSessions();
	
	    Collection<Session> sessions = new HashSet<Session>();
	    Set<String> keys = RedisPoolManager.smembers(all_sessions_Key);
	    if (keys != null && keys.size() > 0) {
	      for (String key : keys) {
	        byte[] rawValue = RedisPoolManager.get((this.keyPrefix + key).getBytes());
	        if (rawValue == null) {
	        	RedisPoolManager.srem(all_sessions_Key, key);
	        } else {
	          Session s = (Session) SerializeUtils.deserialize(rawValue);
	          sessions.add(s);
	        }
	      }
	    }
	
	    return sessions;
	  }
	
	  @Override
	  protected Serializable doCreate(Session session) {
	    Serializable sessionId = this.generateSessionId(session);
	    this.assignSessionId(session, sessionId);
	
	    this.saveSession(session);
	
	    return sessionId;
	  }
	
	  @Override
	  protected Session doReadSession(Serializable sessionId) {
	    if (sessionId == null) {
	      logger.error("session id is null");
	      return null;
	    }
	
	    Session s = (Session) SerializeUtils.deserialize(RedisPoolManager.get(this.getByteKey(sessionId)));
	    return s;
	  }
	
	  // save session
	  private void saveSession(Session session) throws UnknownSessionException {
	    
		if (session == null || session.getId() == null) {
	      logger.error("session or session id is null");
	      return;
	    }
	
	    byte[] key = getByteKey(session.getId());
	    byte[] value = SerializeUtils.serialize(session);
	    
	    session.setTimeout(RedisPoolManager.expire * 1000);
	    
	    //salai commented: not required session key and ttl at this point, pollutes the db
	    RedisPoolManager.set(key, value, RedisPoolManager.expire);
	    
	    RedisPoolManager.sadd(all_sessions_Key, session.getId().toString());
	  }
	
	  // get byte by key
	  private byte[] getByteKey(Serializable sessionId) {
	    return (this.keyPrefix + sessionId).getBytes();
	  }
	


}