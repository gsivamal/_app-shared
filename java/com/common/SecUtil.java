package com.common;

import static spark.Spark.halt;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;

import wjw.shiro.redis.SimpleAuthenticationInfoExt;

public class SecUtil {
	
	private static final String SECRET;
	
	static {
        SECRET = "my company secret";
    }
	
	private static JWTSigner signer = new JWTSigner(SECRET);
	private static JWTVerifier verifier = new JWTVerifier(SECRET);

	//SHIRO
	
	//JWT
	public static String _create_jwt(String email, Serializable shirosession) {
		 
			//Token format:[header.payload.signature]
			Map<String, Object> claims = new HashMap<String, Object>();
				claims.put("email", email);
				claims.put("session", shirosession);
			String token = signer.sign(claims, new JWTSigner.Options().setIssuedAt(true));
			
			return token;
	    }
		
	 public static boolean _verify_jwt(String token) {
		 
			boolean verification = false; 
			try{
				Map<String, Object> decoded = verifier.verify(token);
				//long iat = ((Number) decoded.get("iat")).longValue();

		        // 3 possible invalidations of token
		        //if (!(decoded.exp >= new Date())) { verification = undefined; }
		        if (decoded.get("email") != null) { verification = true; }
		        if (decoded.get("role") != null) { verification = true; }
				
				System.out.println("Token Verified:"+decoded);
			}catch(Exception e){						
				e.printStackTrace();
				halt(401, "Invalid Token!!!");
			} finally {
		        return verification;
			}
			
	    } 
	 
	// Clean way to get the subject
	 private static Subject getSubject()
	 {
	     Subject currentUser = ThreadContext.getSubject();// SecurityUtils.getSubject();

	     if (currentUser == null)
	     {
	         currentUser = SecurityUtils.getSubject();
	     }

	     return currentUser;
	 }
	 
	 // Logout the user fully before continuing.
	 private static void ensureUserIsLoggedOut()
	 {
	     try
	     {
	         // Get the user if one is logged in.
	         Subject currentUser = getSubject();
	         if (currentUser == null)
	             return;

	         // Log the user out and kill their session if possible.
	         currentUser.logout();
	         Session session = currentUser.getSession(false);
	         if (session == null)
	             return;

	         session.stop();
	     }
	     catch (Exception e)
	     {
	         // Ignore all errors, as we're trying to silently 
	         // log the user out.
	     }
	 }	 
	 
	 public static SimpleAuthenticationInfoExt _authenticate(String username, String password){
			
			SimpleAuthenticationInfoExt authInfo = null;
			try {
				//Subject cu = SecurityUtils.getSubject();
				//org.apache.shiro.session.Session cs = cu.getSession();
				//cu.login(new UsernamePasswordToken(username, password));
				//authenticated = cu.isAuthenticated();
				//cu.logout(); //salai what is for, this is an existing code

			    // login the current subject
			    Subject subject = getSubject();				

			    // use a user name/pass token
			    UsernamePasswordToken token = new UsernamePasswordToken(username, password);
			    token.setRememberMe(true);
			    //subject.login(token);  
			    
			    ensureUserIsLoggedOut();
			    
				authInfo = (SimpleAuthenticationInfoExt)SecurityUtils.getSecurityManager().authenticate(token);
				
				if(subject.isAuthenticated()){
					System.out.println("Authenticated TRUE");
					//Serializable shirosession = subject.getSession().getId();
					// set the token expire
					//System.out.println("shirosession:"+ shirosession);
				}
				
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				return authInfo;
			}
		}
	 
	 
	 public static void _logout(){
			
			try {
				Subject cu = SecurityUtils.getSubject();
					cu.logout();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

}
