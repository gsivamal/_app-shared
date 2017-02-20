package com.common;

import static spark.Spark.*;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;

import org.json.JSONObject;

import com.common.SecUtil;
import com.domain.StatusMessage;

import spark.template.freemarker.FreeMarkerEngine;
import wjw.shiro.redis.RedisCacheManager;
import wjw.shiro.redis.RedisPoolManager;
import wjw.shiro.redis.RedisRealm;
import wjw.shiro.redis.RedisSessionDAO;


public abstract class SparkServerBase {
	
	//protected RedisPoolManager redisManager;
	
	protected FreeMarkerEngine templateEngine = new FreeMarkerEngine();

	protected void bootstarp(){
  
		 //1. shiro init
	 	//Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro-realm.ini");
	 
	    //SecurityManager securityManager = factory.getInstance();
	    
	    // for this simple example quickstart, make the SecurityManager accessible as a JVM singleton.  Most applications wouldn't do this
	    // and instead rely on their container configuration or web.xml for webapps.  That is outside the scope of this simple quickstart, so
	    // we'll just do the bare minimum so you can continue to get a feel for things.
	    //SecurityUtils.setSecurityManager(securityManager);
	    
	    
	    //-->
		RedisPoolManager.getRedisPoolManager().init();

		//CacheManager
		RedisCacheManager cacheManager = new RedisCacheManager();
		
		//Realm
		RedisRealm redisRealm = new RedisRealm();
				redisRealm.setCacheManager(cacheManager);
				redisRealm.setPermissionsLookupEnabled(true);
		
		DefaultSecurityManager securityManager = new DefaultSecurityManager(redisRealm);
			securityManager.setRealm(redisRealm);
			securityManager.setCacheManager(cacheManager);	    

		//SessionDAO
		RedisSessionDAO	redisSessionDAO = new RedisSessionDAO();

		//SessionManager
		DefaultSessionManager sessionManager = (DefaultSessionManager)securityManager.getSessionManager();
			sessionManager.setSessionDAO(redisSessionDAO);
			sessionManager.setSessionValidationInterval(600000);
		
		securityManager.setSessionManager(sessionManager);		
		
		SecurityUtils.setSecurityManager(securityManager);			
	    
	    //<--
	    
	    //2. Datasource initialize
 	}
	    
 	protected void initAuthFilters(){
 		
 		//before("/gateway",(req, resp) -> {
			
		//	System.out.println("May be from Quest, let it go...:");
			
			//resp.status(200);  
			
 		//});  		
 		
  
 		before("/api/*",(req, resp) -> {
			String authjson = req.headers("Authorization");
			System.out.println("Token in Before:"+authjson);
			
			if(authjson==null){
				halt(401,"Not logged in");
			} else {
				authjson = authjson.replaceFirst("Bearer ", "");
				System.out.println("Token to verify:"+authjson);
				
				JSONObject hdr = new JSONObject(authjson);
				
				//to verify
				if(SecUtil._verify_jwt(hdr.get("token").toString())){
					resp.status(200);  
				} else {
					halt(401,"Invalid token");
				}
			}
		});  	  
  
  
		after("/api/*",(req, resp) -> {
			resp.type("application/json");   
		}); 

		//after("/pdf/*",(req, resp) -> {
		//	resp.type("application/vnd.fdf");   
		//}); 
		
		
		exception(Exception.class, (exception, req, resp) -> {
		    
			exception.printStackTrace();
			
			System.out.println("Exception while running..."+exception.getLocalizedMessage() );
			
			//trigger an Email to check the error, smart :O)
			
		});
 	}	
 	
 	
  
 
    public static void main(String[] args) {
     
     //SparkServerBase hw = new SparkServerBase();
     // hw.bootstarp();     
     
    }
    
}