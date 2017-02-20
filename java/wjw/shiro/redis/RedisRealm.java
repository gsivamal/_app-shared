package wjw.shiro.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.common.NuUtil;

public class RedisRealm extends AuthorizingRealm {
	  private static Logger logger = LoggerFactory.getLogger(RedisRealm.class);
	
	  private static final int hashIterations = 100000; //number of iterations used in the hash.  not used when validating the password, so don't change it.
	  private static final String F_PASSWORD = "password";
	  private static final String F_SALT = "salt";
	  private static final String F_NAME = "name";
	  private static final String F_ALGORITHM = "algorithm";
	  private static final String F_HASHITERATIONS = "hashIterations";
	
	  //private RedisPoolManager redisManager;
	
	  //The Redis key prefix for the Realm
	  //salai-made all constants as static
	  private static String keyPrefix = RedisPoolManager.DEFAULT_ROOTKEY + "realm:";
	
	  //users
	  private static String users_KeyPrefix = keyPrefix + "user:";
	
	  //all roles
	  private static String all_roles_Key = keyPrefix + "all_roles";
	
	  //user roles (salai made public)
	  public static String user_roles_KeyPrefix = keyPrefix + "user_roles:";
	  
	  //role users
	  private static String role_users_KeyPrefix = keyPrefix + "role_users:";
	
	  //role permissions
	  private static String roles_permissions_Key = keyPrefix + "roles_permissions";
	
	  protected boolean permissionsLookupEnabled = false;
	
	  private HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(Sha256Hash.ALGORITHM_NAME);
	  private RandomNumberGenerator rng = new SecureRandomNumberGenerator();
	
	  public RedisRealm() {
	    matcher.setHashIterations(hashIterations);
	    matcher.setStoredCredentialsHexEncoded(false);
	    super.setCredentialsMatcher(matcher);
	  }	  
	
	
	  // Enables lookup of permissions during authorization. The default is "false" meaning that only roles are associated with a user. 
	  // Set this to true in order to lookup roles <b>and</b> permissions.
	  //    ->    true  if permissions should be looked up during authorization, or
	  //    ->    false if only roles should be looked up.
	  public void setPermissionsLookupEnabled(boolean permissionsLookupEnabled) {
	    this.permissionsLookupEnabled = permissionsLookupEnabled;
	  }
	
	  @Override
	  public boolean supports(AuthenticationToken token) {
	    return token instanceof UsernamePasswordToken;
	  }
	
	  @Override
	  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authToken) throws AuthenticationException {
	    if (!(authToken instanceof UsernamePasswordToken)) {
	      throw new AuthenticationException("This realm only supports UsernamePasswordTokens");
	    }
	    UsernamePasswordToken token = (UsernamePasswordToken) authToken;	
	    if (token.getUsername() == null) {
	      throw new AuthenticationException("Cannot log in null user");
	    }
	
	    String username = token.getUsername();	  
	    // Does the actual mechanics of creating the Authentication info object from the database.
	    Map<String, String> user = RedisPoolManager.hgetAll(users_KeyPrefix + username);	
	    if (user == null || user.size() == 0) {
	      throw new UnknownAccountException("Unkown user " + username);
	    }
	
	    String password = user.get(F_PASSWORD);
	    String salt = user.get(F_SALT);
	    
	    //salai added - company
	    String company = user.get("company");
	    String lang = user.get("language");
	    
	    System.out.println("Company:"+company);
	    
	    //salai added - roles
	    Set<String> roles = getUserOwnedRoles(username);
	
	    //salai commented
	    //return new SimpleAuthenticationInfo(username, password, Sha256Hash.fromBase64String(salt), getName());
	    
	    return new SimpleAuthenticationInfoExt(company, lang, username, password, roles, Sha256Hash.fromBase64String(salt), getName());
	  }  
	
	  @SuppressWarnings("unchecked")
	  @Override
	  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
	    //null usernames are invalid
	    if (principals == null) {
	      throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
	    }
	    String username = (String) getAvailablePrincipal(principals);
	
	    java.util.Set<String> roles = RedisPoolManager.smembers(user_roles_KeyPrefix + username);
	
	    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roles);
	
	    if (permissionsLookupEnabled) {
	      java.util.List<java.lang.String> permissionsList = RedisPoolManager.hmget(roles_permissions_Key, roles.toArray(new String[0]));
	      Set<String> permissionsSet = new HashSet<String>(permissionsList.size());
	      permissionsSet.addAll(permissionsList);
	      info.setStringPermissions(permissionsSet);
	    }
	
	    return info;
	  }
	
	  // add user key
	  public boolean addUser(String username, String plainTextPassword) {
	    ByteSource salt = rng.nextBytes();
	
	    Map<String, String> user = new HashMap<String, String>(5);
		    user.put(F_NAME, username);
		    user.put(F_PASSWORD, new Sha256Hash(plainTextPassword, salt, hashIterations).toBase64());
		    user.put(F_SALT, salt.toBase64());
		    user.put(F_ALGORITHM, Sha256Hash.ALGORITHM_NAME);
		    user.put(F_HASHITERATIONS, String.valueOf(hashIterations));
	
	    if (RedisPoolManager.hmset(users_KeyPrefix + username, user).equalsIgnoreCase("OK")) {
	      return true;
	    } else {
	      return false;
	    }
	  }
	  
	  // get user
	  public Map<String,String> getUser(String username) {
		  return RedisPoolManager.hgetAll(users_KeyPrefix + username);
	  }

	  // update password
	  public boolean updateUserPassword(String username, String plainTextPassword) {
	    return addUser(username, plainTextPassword);
	  }
	  
	
	  // remove user
	  public boolean removeUser(String username) {
	    //1. remove user key
	    java.util.Set<String> rolesSet = RedisPoolManager.smembers(user_roles_KeyPrefix + username);
	    for (String role : rolesSet) {
	      RedisPoolManager.srem(role_users_KeyPrefix + role, username);
	    }
	
	    //2. delete str
	    RedisPoolManager.delStr(users_KeyPrefix + username);
	
	    //3. delete role prefix str
	    RedisPoolManager.delStr(user_roles_KeyPrefix + username);
	
	    return true;
	  }
	
	  // add Roles
	  public boolean addRole(String... roles) {
	    RedisPoolManager.sadd(all_roles_Key, roles);
	    return true;
	  }
	
	  // remove rolw
	  public void removeRole(String role) {
	    //1. remove role key
	    java.util.Set<String> usersSet = RedisPoolManager.smembers(role_users_KeyPrefix + role);
	    for (String user : usersSet) {
	      RedisPoolManager.srem(user_roles_KeyPrefix + user, role);
	    }
	    //2. delete str
	    RedisPoolManager.delStr(role_users_KeyPrefix + role);
	
	    //3. remove rolws
	    RedisPoolManager.srem(all_roles_Key, role);
	
	    //4. remove the permissions
	    this.removeRolePermission(role);
	  }
	
	  // add user owned roles
	  public boolean addUserOwnedRoles(String username, String... roles) {
	    RedisPoolManager.sadd(user_roles_KeyPrefix + username, roles);
	    for (String role : roles) {
	      RedisPoolManager.sadd(role_users_KeyPrefix + role, username);
	    }
	    return true;
	  }
	  
	  //salai-added
	  // get user owned roles
	  public Set<String> getUserOwnedRoles(String username) {
		  Set<String> roles = RedisPoolManager.smembers(user_roles_KeyPrefix + username);
		  return roles;
	  }
	  
	  //salai-added
	  public boolean addUserOwnedCompany(String username, String company) {
		  RedisPoolManager.hset(users_KeyPrefix + username, "company", company);
		  return true;
	  }
	  //salai - get user company
	  public String getUserOwnedCompany(String username) {
		  String company = RedisPoolManager.hgetAll(users_KeyPrefix + username).get("company");
		  return company;
	  }

	  //salai added method:add user details
	  /*public boolean addUserDetails(String username, String fullname, String phone, String email) {
		    Map<String, String> user = new HashMap<String, String>(5);
			    user.put("fullname", fullname);
			    user.put("phone", phone);
			    user.put("email", email);
		
		    if (RedisPoolManager.hmset(users_KeyPrefix + username, user).equalsIgnoreCase("OK")) {
		      return true;
		    } else {
		      return false;
		    }
		  }*/
		
	
	  // update the user owned roles
	  public void updateUserOwnedRoles(String username, String... roles) {
	    this.removeUserOwnedRoles(username, roles);
	    this.addUserOwnedRoles(username, roles);
	  }
	
	  // remove the user owned roles
	  public void removeUserOwnedRoles(String username, String... roles) {
	    RedisPoolManager.srem(user_roles_KeyPrefix + username, roles);
	    for (String role : roles) {
	      RedisPoolManager.srem(role_users_KeyPrefix + role, username);
	    }
	  }
	
	  // add role permissions
	  public boolean addRolePermission(String role, String permission) {
	    RedisPoolManager.hset(roles_permissions_Key, role, permission);
	    return true;
	  }
	
	  // update role permissions
	  public boolean updateRolePermission(String role, String permission) {
	    RedisPoolManager.hset(roles_permissions_Key, role, permission);
	    return true;
	  }
	
	  // remove role permision
	  public void removeRolePermission(String role) {
	    RedisPoolManager.hdel(roles_permissions_Key, role);
	  }
  
	  // add user owned roles
	  public boolean addUserActivationCode(String username, String code) {
		  RedisPoolManager.hset(users_KeyPrefix + username, "activationcode", code); //set TTL for activation code later
		  
		  //this means, user is not active...right..so set the status
		  RedisPoolManager.hset(users_KeyPrefix + username, "status", NuUtil.UserStatus.NotActivated.value);
		  RedisPoolManager.hset(users_KeyPrefix + username, "createddate", NuUtil.getTodayDate());
		  return true;
	  }
	  
	  // add user owned roles
	  public String getUserActivationCode(String username, String code) {
		  String activationcode = RedisPoolManager.hgetAll(users_KeyPrefix + username).get("activationcode");
		  return activationcode;
	  }


	public void clearUserActivationCode(String username) {
		  RedisPoolManager.hdel(users_KeyPrefix + username, "activationcode");

		  //this means, user is active...right..so set the status
		  RedisPoolManager.hset(users_KeyPrefix + username, "status", NuUtil.UserStatus.Active.value);
		  RedisPoolManager.hset(users_KeyPrefix + username, "createddate", NuUtil.getTodayDate());
	}
}
