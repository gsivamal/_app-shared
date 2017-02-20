package com.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.domain.JSONObjectBase;

import wjw.shiro.redis.RedisPoolManager;

public class DBService {
	
	
	public static boolean save(String rediskey, String seqno, JSONObjectBase data){		
		Map<String, String> map = new HashMap<String, String>();
		map.put(seqno, data.toString());		
		return RedisPoolManager.hmset(rediskey, map).equals("OK");		
	}

	public static boolean save(String rediskey, JSONObjectBase data){		
		return RedisPoolManager.hmset(rediskey, data.map()).equals("OK");		
	}

	public static boolean save(String rediskey, Map<String, String> data){		
		return RedisPoolManager.hmset(rediskey, data).equals("OK");		
	}
	
	public static Map<String, String> get(String rediskey){		
		return RedisPoolManager.hgetAll(rediskey);	
	}

	public static Set<String> getSet(String rediskey){		
		return RedisPoolManager.smembers(rediskey);	
	}
	
	public static boolean saveSet(String rediskey, String entry){		
		return (RedisPoolManager.sadd(rediskey, entry).longValue() == 1);	
	}
	
	public static boolean saveString(String rediskey, String entry){		
		return new String(RedisPoolManager.set(rediskey.getBytes(), entry.getBytes())).equals("OK");
	}
	
	//TODO: not used, implement later
	public static JSONObjectBase getJson(String rediskey, Class<? extends JSONObjectBase> resulttype) throws Exception {		
		JSONObjectBase newjson = null;		
		try {			
			newjson = (JSONObjectBase)resulttype.newInstance();
		
		}catch(InstantiationException ie){
			throw new Exception(ie.getLocalizedMessage());
		}catch(IllegalAccessException ile){
			throw new Exception(ile.getLocalizedMessage());
		}
		
		return null;	
	}

	public static void del(String rediskey){		
		RedisPoolManager.delStr(rediskey);	
	}
}
