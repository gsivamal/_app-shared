package wjw.shiro.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisCache<K, V> implements Cache<K, V> {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  //The Redis key prefix for the cache
  private String keyPrefix = RedisPoolManager.DEFAULT_ROOTKEY + "cache:";

  //keyPrefix+"all_caches"
  private String all_caches_Key = keyPrefix + "all_caches";

  //RedisCache
  public RedisCache() {
    this.keyPrefix = RedisPoolManager.rootKey + "cache:";
    this.all_caches_Key = this.keyPrefix + "all_caches";
  }

  @Override
  @SuppressWarnings("unchecked")
  public V get(K key) throws CacheException {
    logger.debug("get(K key) from redis:key [" + key + "]");
    try {
      if (key == null) {
        return null;
      } else {
        byte[] rawValue = RedisPoolManager.get(getByteKey(key));
        if (rawValue == null) {
          return null;
        } else {
          V value = (V) SerializeUtils.deserialize(rawValue);
          return value;
        }
      }
    } catch (Throwable t) {
      throw new CacheException(t);
    }

  }

  @Override
  public V put(K key, V value) throws CacheException {
    logger.debug("put(K key, V value) to redis:key [" + key + "]");
    try {
      RedisPoolManager.set(getByteKey(key), SerializeUtils.serialize(value));

      {
        String preKey;
        if (key instanceof String) {
          preKey = (String) key;
        } else {
          preKey = key.getClass().getName() + ":" + (new Md5Hash(SerializeUtils.serialize(key))).toHex();
        }
        RedisPoolManager.sadd(all_caches_Key, preKey);
      }

      return value;
    } catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  @Override
  public V remove(K key) throws CacheException {
    logger.debug("remove(K key) from redis: key [" + key + "]");
    try {
      V previous = this.get(key);

      RedisPoolManager.del(getByteKey(key));

      {
        String preKey;
        if (key instanceof String) {
          preKey = (String) key;
        } else {
          preKey = key.getClass().getName() + ":" + (new Md5Hash(SerializeUtils.serialize(key))).toHex();
        }
        RedisPoolManager.srem(all_caches_Key, preKey);
      }

      return previous;
    } catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  @Override
  public void clear() throws CacheException {
    logger.debug("Cache clear() from redis");
    try {
      java.util.Set<String> cacheKeys = RedisPoolManager.smembers(all_caches_Key);
      for (String key : cacheKeys) {
    	  RedisPoolManager.del((this.keyPrefix + key).getBytes());
      }

      RedisPoolManager.delStr(all_caches_Key);
    } catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  @Override
  public int size() {
    try {
      return RedisPoolManager.scard(all_caches_Key).intValue();
    } catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<K> keys() {
    try {
      java.util.Set<String> keys = RedisPoolManager.smembers(all_caches_Key);

      if (CollectionUtils.isEmpty(keys)) {
        return Collections.emptySet();
      }

      Set<K> newKeys = new HashSet<K>();
      for (String key : keys) {
        if (RedisPoolManager.exists((this.keyPrefix + key).getBytes()) == false) {
        	RedisPoolManager.srem(all_caches_Key, key);
        } else {
          newKeys.add((K) key);
        }
      }
      return newKeys;
    } catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<V> values() {
    try {
      java.util.Set<String> keys = RedisPoolManager.smembers(all_caches_Key);

      if (CollectionUtils.isEmpty(keys)) {
        return Collections.emptyList();
      }

      Collection<V> values = new ArrayList<V>(keys.size());
      for (String key : keys) {
        V value = this.get((K) key);
        if (value == null) {
        	RedisPoolManager.srem(all_caches_Key, key);
        } else {
          values.add(value);
        }
      }
      return values;
    } catch (Throwable t) {
      throw new CacheException(t);
    }
  }

  //byte[] key
  private byte[] getByteKey(K key) {
    if (key instanceof String) {
      return (this.keyPrefix + key).getBytes();
    } else {
      return (this.keyPrefix + key.getClass().getName() + ":" + (new Md5Hash(SerializeUtils.serialize(key))).toHex()).getBytes();
    }
  }

}
