package com.common;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import wjw.shiro.redis.RedisPoolManager;
import wjw.shiro.redis.RedisRealm;

//TODO - company-1-settings
//TODO - company-1-trainings

public class Bootstrap {
	
	//make sure it is same in the child projects
	public String shirotk = ":";
	
	public static String MRO = "mro";
	
	//1
	public void createPeopleMatrixCompanyUsers() throws Exception {
	
	  RedisPoolManager.getRedisPoolManager().init();
	
	  RedisRealm redisRealm = new RedisRealm();
	    //redisRealm.setRedisManager(redis);
	    redisRealm.setPermissionsLookupEnabled(true);

	    //clean up ->
	    //redisRealm.removeUser("root");	    
	    //redisRealm.removeRole("admin");
	    //redisRealm.removeRole("guest");	    
	    //redisRealm.removeUserOwnedRoles(String username, String... roles)
	    //redisRealm.removeRolePermission(String role)
	    //<- clean up
	    
	    // Create Nustone Company
	    Map<String,String> companydetails = new HashMap<String,String>();
	    	companydetails.put("name", "PeopleMatrix Software Inc");
	    	companydetails.put("address1", "8700 Lindenwood");
	    	companydetails.put("zip", "75063");
	    	companydetails.put("phone", "214-566-3915");
	    	companydetails.put("email", "contact@tcompliance.com");
	    	companydetails.put("contact", "Sales");
	    	
	    	RedisPoolManager.hmset("c0", companydetails);
	    
		    //1. add User - keyPrefix:users:$username
		    redisRealm.addUser("root@tcompliance.com", "topsecret");
		    redisRealm.addUser("support@tcompliance.com", "secret");
		    redisRealm.addUser("billing@tcompliance.com", "$cash");
		    
		    //salai added method
		    //redisRealm.addUserDetails("drmro@tcompliance.com", "Dr,Dallas","989-334-5808","drdallas@hospital.com");
		    //redisRealm.addUserDetails("julie@tcompliance.com", "Julie,Waldrip","254-744-0808","julie@centexdotsafety.com");
		
		    //2. add role - keyPrefix:all_roles (salai: all roles and permissions created only once, so here)
		    redisRealm.addRole("admin", "support", "billing", "owner", "partner", "DER", "driver", "mechanic", "mro", "dotagent");
		
		    //3. add user owned role - keyPrefix:user_roles:$username
		    redisRealm.addUserOwnedRoles("root@tcompliance.com", "admin", "support", "billing");
		    redisRealm.addUserOwnedRoles("websupport@tcompliance.com", "support");
		    redisRealm.addUserOwnedRoles("billing@tcompliance.com", "billing");
		
		    //4. add role permission - keyPrefix:roles_permissions
		    //Permission format => app:modules:screens:operations
		    redisRealm.addRolePermission("root", "*");
		    redisRealm.addRolePermission("admin", "company:all:full");
		    redisRealm.addRolePermission("support", "company:all:partial"); //cannot delete companies..kind
		    redisRealm.addRolePermission("billing", "company:all:partial");
		    
		    redisRealm.addRolePermission("owner", "company:selected:full");
		    redisRealm.addRolePermission("operator", "company:selected:partial");
		    redisRealm.addRolePermission("driver", "company:selected:full");
		    
		    redisRealm.addRolePermission("mro", "company:assigned:partial");
		    redisRealm.addRolePermission("dotagent", "company:assigned:partial");
		    
		    //5. salai- added add user company
		    redisRealm.addUserOwnedCompany("root@tcompliance.com", "c0");
		    redisRealm.addUserOwnedCompany("support@tcompliance.com", "c0");
		    redisRealm.addUserOwnedCompany("billing@tcompliance.com", "c0");

		// create company users list
		RedisPoolManager.sadd("c0:users", "root@tcompliance.com", "support@tcompliance.com", "billing@tcompliance.com");
	    
	    //creates 13 keys
	    
		RedisPoolManager.destroy();
	}
	
	//2
	public void createTruckCompanyUsers() throws Exception {
		
		  RedisPoolManager.getRedisPoolManager().init();
		
		  RedisRealm redisRealm = new RedisRealm();
		    //redisRealm.setRedisManager(redis);
		    redisRealm.setPermissionsLookupEnabled(true);
		    
		    // Create Nustone Company
		    Map<String,String> companydetails = new HashMap<String,String>();
		    	companydetails.put("name", "Paramount WasteWater Solutions LLC");
		    	companydetails.put("dot", "1959622");
		    	companydetails.put("address1", "401 A COTTINGHAM DRIVE");
		    	companydetails.put("city", "Temple");
		    	companydetails.put("state", "TX");
		    	companydetails.put("zip", "76504");
		    	companydetails.put("phone", "254-791-0303");
		    	companydetails.put("fax", "254-742-2755");
		    	companydetails.put("website", "paramountwastewater");
		    	companydetails.put("email", "patrick@paramountwastewater.com");
		    	
		    	companydetails.put("contact1", "PATRICK KERN");
		    	companydetails.put("contact2", "KRISTA KERN");
		    	
		    	RedisPoolManager.hmset("c1", companydetails);
		    
			    //1. add Owner User
			    redisRealm.addUser("patrick@paramountwastewater.com", "secret");
			
			    //2. add user owned role
			    redisRealm.addUserOwnedRoles("patrick@paramountwastewater.com", "owner");
			    redisRealm.addUserOwnedRoles("krista@paramountwastewater.com", "partner");
			    
			    //3. salai - to add user company
			    redisRealm.addUserOwnedCompany("patrick@paramountwastewater.com", "c1");	
			    redisRealm.addUserOwnedCompany("krista@paramountwastewater.com", "c1");	
			    
			    //1. add Driver User - Driver (Remember every driver should have valid email, use that for login)
			    redisRealm.addUser("bob@paramountwastewater.com", "#bob123");
			
			    //2. add user owned role
			    redisRealm.addUserOwnedRoles("bob@paramountwastewater.com", "driver");
			    
			    //3. salai- add user company
			    redisRealm.addUserOwnedCompany("bob@paramountwastewater.com", "c1");
			    
			    //1. add Driver User - Driver (Remember every driver should have valid email, use that for login)
			    redisRealm.addUser("john@paramountwastewater.com", "#john123");
			
			    //2. add user owned role
			    redisRealm.addUserOwnedRoles("john@paramountwastewater.com", "driver","mechanic");
			    
			    //3. salai- added add user company
			    redisRealm.addUserOwnedCompany("john@paramountwastewater.com", "c1");			    

			// create company users list
			RedisPoolManager.sadd("c1:users", "patrick@paramountwastewater.com", "krista@paramountwastewater.com", "bob@paramountwastewater.com", "john@paramountwastewater.com");
			
			//create location list
			Map<String, String> locations = new HashMap<String,String>();
								locations.put("1", "Temple, TX");
								locations.put("2", "Waco, TX");
			RedisPoolManager.hmset("c1:locations", locations); //make sure the separator matches

			//add to company-list
			RedisPoolManager.sadd("companies", "c1"); //for PeopleMatrix access
		    
			RedisPoolManager.destroy();
		}	
		
		//3.
		public void createMroCompanyUsers() throws Exception {
		
		  RedisPoolManager.getRedisPoolManager().init();
		
		  RedisRealm redisRealm = new RedisRealm();
		    //redisRealm.setRedisManager(redis);
		    redisRealm.setPermissionsLookupEnabled(true);
		    
		    // Create Nustone Company
		    Map<String,String> companydetails = new HashMap<String,String>();
		    	companydetails.put("name", "American Medical Review");
		    	companydetails.put("address1", "4237 Salisbury Road #321");
		    	companydetails.put("state", "FL");
		    	companydetails.put("zip", "32216");
		    	companydetails.put("phone", "904-332-0472");
		    	companydetails.put("email", "Barbara@americanmedicalreviewofficer.com");
		    	companydetails.put("contact", "Barbara");
		    	
		    	RedisPoolManager.hmset("mro1", companydetails);
		    
			    //1. add User
			    redisRealm.addUser("Barbara@americanmedicalreviewofficer.com", "secret1");
			    
			    //2. add user owned role
			    redisRealm.addUserOwnedRoles("Barbara@americanmedicalreviewofficer.com", "mro");

			    //3. salai- added add user company
			    redisRealm.addUserOwnedCompany("Barbara@americanmedicalreviewofficer.com", "mro1");

			// create company users list
			RedisPoolManager.sadd("mro1:users", "Barbara@americanmedicalreviewofficer.com");
		    
			RedisPoolManager.destroy();
		}	
		
		//4.
		public void createDotAgentCompanyUsers() throws Exception {
			
			  RedisPoolManager.getRedisPoolManager().init();
			
			  RedisRealm redisRealm = new RedisRealm();
			    //redisRealm.setRedisManager(redis);
			    redisRealm.setPermissionsLookupEnabled(true);
			    
			    // Create Nustone Company
			    Map<String,String> companydetails = new HashMap<String,String>();
			    	companydetails.put("name", "Central Texas DOT Safety Consulting");
			    	companydetails.put("address1", "1303 Chapel Ridge Road");
			    	companydetails.put("city", "Waco");
			    	companydetails.put("state", "TX");
			    	companydetails.put("zip", "76712");
			    	companydetails.put("phone", "254-744-0808");
			    	companydetails.put("email", "julie@centexdotsafety.com");
			    	companydetails.put("website", "www.centexdotsafety.com");
			    	companydetails.put("contact", "Julie, Waldrip");
			    	
			    	RedisPoolManager.hmset("dotagency1", companydetails);
			    
				    //1. add User
				    redisRealm.addUser("julie@centexdotsafety.com", "audit#123");
				    
				    //2. add user owned role
				    redisRealm.addUserOwnedRoles("julie@centexdotsafety.com", "dotagent");

				    //3. salai- added add user company
				    redisRealm.addUserOwnedCompany("julie@centexdotsafety.com", "dotagency1");

				// create company users list
				RedisPoolManager.sadd("dotagency1:users", "julie@centexdotsafety.com");
				
				// attach truck company to dot agency
				RedisPoolManager.sadd("dotagency1:clients", "c1");				
			    
				RedisPoolManager.destroy();
			}
	
}
