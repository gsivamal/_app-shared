package wjw.shiro.redis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

// Salai: made it Single-ton
public class RedisPoolManager {
	
	  static RedisPoolManager thispool = new RedisPoolManager();
	
	  private final static Logger log = LoggerFactory.getLogger(RedisPoolManager.class);
	
	  static final String TOMCAT_SESSION_PREFIX = "TS:";
	  
	  static ShardedJedisPool _shardedPool = null;
	  static JedisPool _pool = null;
	
	  static String serverlist = "127.0.0.1:6379";
	  //static String serverlist = "192.168.1.251:6379";
	  
	  static int maxConn = 100;
	  static int minConn = 5;
	  static int socketTO = 6000;
	  
	  static int expire = 1800; //0 - never expire
	
	  public static final String DEFAULT_ROOTKEY = "shiro:";
	  static String rootKey = DEFAULT_ROOTKEY;
	  
	  //constructor
	  private RedisPoolManager() {
	    super();
	  }
	  
	  public static RedisPoolManager getRedisPoolManager() {
		  
		  if(thispool == null){
			  thispool = new RedisPoolManager();
		  }
		  
		    return thispool;
	  }
	
	  // init
	  public static void init() {
	    synchronized (RedisPoolManager.class) {
	      try {
	        if (_shardedPool == null && _pool == null) {
	          try {
	            JedisPoolConfig poolConfig = new JedisPoolConfig();
		            poolConfig.setMaxTotal(maxConn);
		            poolConfig.setMinIdle(minConn);
		            int maxIdle = poolConfig.getMinIdle() + 5;
		            if (maxIdle > poolConfig.getMaxTotal()) {  //maxActive
		            	maxIdle = poolConfig.getMaxTotal();
		            }
		            poolConfig.setMaxIdle(maxIdle);
		            poolConfig.setMaxWaitMillis(1000L);
		            //salai: need to set what happen when pool exhausted
		            //poolConfig.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
		            poolConfig.setTestOnBorrow(false);
		            poolConfig.setTestOnReturn(false);
		            poolConfig.setTestWhileIdle(true);
		            poolConfig.setMinEvictableIdleTimeMillis(1000L * 60L * 10L); 
		            poolConfig.setTimeBetweenEvictionRunsMillis(1000L * 30L);
		            poolConfig.setNumTestsPerEvictionRun(-1);
		
		            String[] servers = serverlist.split(",");
		            java.util.List<JedisShardInfo> shards = new java.util.ArrayList<JedisShardInfo>(servers.length);
		            for (int i = 0; i < servers.length; i++) {
		              String[] hostAndPort = servers[i].split(":");
		              JedisShardInfo shardInfo = new JedisShardInfo(hostAndPort[0], Integer.parseInt(hostAndPort[1]), socketTO);
		              if (hostAndPort.length == 3) {
		                shardInfo.setPassword(hostAndPort[2]);
		              }
		              shards.add(shardInfo);
		            }
		
		            if (shards.size() == 1) {
		              _pool = new JedisPool(poolConfig, shards.get(0).getHost(), shards.get(0).getPort(), shards.get(0).getConnectionTimeout(), shards.get(0).getPassword());
		              log.info("JedisPool");
		            } else {
		              _shardedPool = new ShardedJedisPool(poolConfig, shards);
		              log.info("ShardedJedisPool");
		            }
		
		            log.info("RedisShards:" + shards.toString());
		            //log.info("RedisManager:" + toString());
		          } catch (Exception ex) {
		            log.error("error:", ex);
		          }
	        }
	
	      } catch (Exception ex) {
	        log.error("error:", ex);
	      }
	    }
	  }
	
	
	  // get value from redis by byte
	  //used for Session id storage and retrieval
	  public static byte[] get(byte[] key) {
		  
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        return jedis.get(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        return jedis.get(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  public static String getStr(String key) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        return jedis.get(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        return jedis.get(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  // set the key and value
	  public static byte[] set(byte[] key, byte[] value) {
	    return set(key, value, expire);
	  }
	
	  // set key value expire
	  public static byte[] set(byte[] key, byte[] value, int expire) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        if (expire == 0) {
	          jedis.set(key, value);
	        } else {
	          jedis.setex(key, expire, value);
	        }
	        return value;
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        if (expire == 0) {
	          jedis.set(key, value);
	        } else {
	          jedis.setex(key, expire, value);
	        }
	        return value;
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  public String setStr(String key, String value) {
	    return this.setStr(key, value, expire);
	  }
	
	  public String setStr(String key, String value, int expire) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        if (this.expire == 0) {
	          jedis.set(key, value);
	        } else {
	          jedis.setex(key, expire, value);
	        }
	
	        return value;
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        if (this.expire == 0) {
	          jedis.set(key, value);
	        } else {
	          jedis.setex(key, expire, value);
	        }
	
	        return value;
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  // del key
	  public static void del(byte[] key) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        jedis.del(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        Jedis jedisA = jedis.getShard(key);
	        jedisA.del(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  // db size
	  public long dbSize() {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        return jedis.dbSize();
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      long dbSize = 0;
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        Collection<Jedis> jedisList = jedis.getAllShards();
	        for (Jedis item : jedisList) {
	          dbSize = dbSize + item.dbSize();
	        }
	
	        return dbSize;
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  /**
	   * keys
	   * 
	   * @param regex
	   * @return
	   */
	  //  public Set<byte[]> keys(String pattern) {
	  //    if (_pool != null) {
	  //      Jedis jedis = null;
	  //      try {
	  //        jedis = _pool.getResource();
	  //        return jedis.keys(pattern.getBytes());
	  //      } finally {
	  //        if (jedis != null) {
	  //          try {
	  //            _pool.returnResourceObject(jedis);
	  //          } catch (Throwable thex) {
	  //          }
	  //        }
	  //      }
	  //    } else {
	  //      Set<byte[]> keys = new HashSet<byte[]>();
	  //      ShardedJedis jedis = null;
	  //      try {
	  //        jedis = _shardedPool.getResource();
	  //        Collection<Jedis> jedisList =
	  //            jedis.getAllShards();
	  //        for (Jedis item : jedisList) {
	  //          keys.addAll(item.keys(pattern.getBytes()));
	  //        }
	  //
	  //        return keys;
	  //      } finally {
	  //        if (jedis != null) {
	  //          try {
	  //            _shardedPool.returnResourceObject(jedis);
	  //          } catch (Throwable thex) {
	  //          }
	  //        }
	  //      }
	  //    }
	  //  }
	
	  public static Map<String, String> hgetAll(String key) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        return jedis.hgetAll(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        return jedis.hgetAll(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  public static List<java.lang.String> hmget(String key, String... fields) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        return jedis.hmget(key, fields);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        return jedis.hmget(key, fields);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  //Return OK or Exception if hash is empty
	  public static String hmset(String key, java.util.Map<String, String> hash) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        return jedis.hmset(key, hash);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        return jedis.hmset(key, hash);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  public String hget(String key, String field) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        return jedis.hget(key, field);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        return jedis.hget(key, field);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  //If the field already exists, and the HSET just produced an update of the value, 0 is returned, otherwise if a new field is created 1 is returned.
	  public static Long hset(String key, String field, String value) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        return jedis.hset(key, field, value);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        return jedis.hset(key, field, value);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  public static Long hdel(String hkey, String field) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        return jedis.hdel(hkey, field);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        return jedis.hdel(hkey, field);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  public static Long sadd(String key, String... members) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        return jedis.sadd(key, members);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        return jedis.sadd(key, members);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  public static Long srem(String key, String... members) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        return jedis.srem(key, members);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        return jedis.srem(key, members);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  public static Long scard(String key) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        return jedis.scard(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        return jedis.scard(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  public static Set<String> smembers(String key) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        return jedis.smembers(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        return jedis.smembers(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  /**
	   * del
	   * 
	   * @param key
	   */
	  public static void delStr(String key) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        jedis.del(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        Jedis jedisA = jedis.getShard(key);
	        jedisA.del(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	
	  public static boolean exists(byte[] key) {
	    if (_pool != null) {
	      Jedis jedis = null;
	      try {
	        jedis = _pool.getResource();
	        return jedis.exists(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _pool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    } else {
	      ShardedJedis jedis = null;
	      try {
	        jedis = _shardedPool.getResource();
	        return jedis.exists(key);
	      } finally {
	        if (jedis != null) {
	          try {
	            _shardedPool.returnResourceObject(jedis);
	          } catch (Throwable thex) {
	          }
	        }
	      }
	    }
	  }
	  
	  public static Long append(String key, String field) {
		    if (_pool != null) {
		      Jedis jedis = null;
		      try {
		        jedis = _pool.getResource();
		        return jedis.append(key, field);
		      } finally {
		        if (jedis != null) {
		          try {
		            _pool.returnResourceObject(jedis);
		          } catch (Throwable thex) {
		          }
		        }
		      }
		    } else {
		      ShardedJedis jedis = null;
		      try {
		        jedis = _shardedPool.getResource();
		        return jedis.append(key, field);
		      } finally {
		        if (jedis != null) {
		          try {
		            _shardedPool.returnResourceObject(jedis);
		          } catch (Throwable thex) {
		          }
		        }
		      }
		    }
		  }
	  
	  public static Long incr(String key) {
		    if (_pool != null) {
		      Jedis jedis = null;
		      try {
		        jedis = _pool.getResource();
		        return jedis.incr(key);
		      } finally {
		        if (jedis != null) {
		          try {
		            _pool.returnResourceObject(jedis);
		          } catch (Throwable thex) {
		          }
		        }
		      }
		    } else {
		      ShardedJedis jedis = null;
		      try {
		        jedis = _shardedPool.getResource();
		        return jedis.incr(key);
		      } finally {
		        if (jedis != null) {
		          try {
		            _shardedPool.returnResourceObject(jedis);
		          } catch (Throwable thex) {
		          }
		        }
		      }
		    }
		  }
	  
	  //Note: invoked through reflection from RedisCacheManager
	  public static void destroy() {
		    try {
		      synchronized (RedisPoolManager.class) {
		        if (_shardedPool != null) {
		          ShardedJedisPool myPool = _shardedPool;
		          _shardedPool = null;
		          try {
		            myPool.destroy();
		            //log.info("RedisManager:" + this.toString());
		          } catch (Exception ex) {
		            //log.error("error:", ex);
		          }	
		        }
		
		        if (_pool != null) {
		          JedisPool myPool = _pool;
		          _pool = null;
		          try {
		            myPool.destroy();
		          } catch (Exception ex) {
		            //log.error("error:", ex);
		          }
		
		        }
		      }
		    } finally {
		      //
		    }
		  }
	  
	    //salai added for pub/sub
	  	public static Long publish(String channel, String message) {
		    if (_pool != null) {
		      Jedis jedis = null;
		      try {
		        jedis = _pool.getResource();
		        return jedis.publish(channel, message);
		      } finally {
		        if (jedis != null) {
		          try {
		            _pool.returnResourceObject(jedis);
		          } catch (Throwable thex) {
		          }
		        }
		      }
		    } else {
		      ShardedJedis jedis = null;
		      try {
		        jedis = _shardedPool.getResource();
		        //return jedis.publish(channel, message); //salai, what do we do for shrded pool ? currently subscribe used for GPS notofication only
		        return 0l;
		      } finally {
		        if (jedis != null) {
		          try {
		            _shardedPool.returnResourceObject(jedis);
		          } catch (Throwable thex) {
		          }
		        }
		      }
		    }
		  }	
	  
	  	public static void subscribe(JedisPubSub subr, String channel) {
		    if (_pool != null) {
		      Jedis jedis = null;
		      try {
		        jedis = _pool.getResource();
		        jedis.subscribe(subr, channel);
		      } finally {
		        if (jedis != null) {
		          try {
		            _pool.returnResourceObject(jedis);
		          } catch (Throwable thex) {
		          }
		        }
		      }
		    } else {
		      ShardedJedis jedis = null;
		      try {
		        jedis = _shardedPool.getResource();
		        //jedis.subscribe(subr, channel);  //salai, what do we do for shrded pool ? currently subscribe used for GPS notofication only
		      } finally {
		        if (jedis != null) {
		          try {
		            _shardedPool.returnResourceObject(jedis);
		          } catch (Throwable thex) {
		          }
		        }
		      }
		    }
		  }	  
	  	
	  	public static void psubscribe(JedisPubSub subr, String... channels) {
		    if (_pool != null) {
		      Jedis jedis = null;
		      try {
		        jedis = _pool.getResource();
		        jedis.psubscribe(subr, channels);
		      } finally {
		        if (jedis != null) {
		          try {
		            _pool.returnResourceObject(jedis);
		          } catch (Throwable thex) {
		          }
		        }
		      }
		    } else {
		      ShardedJedis jedis = null;
		      try {
		        jedis = _shardedPool.getResource();
		        //jedis.psubscribe(subr, channel);  //salai, what do we do for shrded pool ? currently subscribe used for GPS notofication only
		      } finally {
		        if (jedis != null) {
		          try {
		            _shardedPool.returnResourceObject(jedis);
		          } catch (Throwable thex) {
		          }
		        }
		      }
		    }
		  }	  
		  
		
		  @Override
		  public String toString() {
		    return "RedisManager{" + "rootKey=" + rootKey + ",expire=" + expire + ",serverlist=" + serverlist + ",minConn=" + minConn + ",maxConn=" + maxConn + ",socketTO=" + socketTO + '}';
		  }

}