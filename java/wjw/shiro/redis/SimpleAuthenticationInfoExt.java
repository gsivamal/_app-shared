package wjw.shiro.redis;

import java.util.Set;

import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;


//salai added - to get Company after success login
public class SimpleAuthenticationInfoExt extends SimpleAuthenticationInfo {
	
    /**
     * The subjects company
     */
    protected Object company;
    protected Object language;
    protected Set<String> roles;

	/**
	 * 
	 */
	private static final long serialVersionUID = 15647549456756L;
	
	
	public SimpleAuthenticationInfoExt(Object company, Object lang, Object principal, Object hashedCredentials, Set<String> rs, ByteSource credentialsSalt, String realmName) {
        this.principals = new SimplePrincipalCollection(principal, realmName);
        this.credentials = hashedCredentials;
        this.credentialsSalt = credentialsSalt;
        
        this.company = company;
        this.language = lang;
        this.roles = rs;
    }
	
	public String getCompanyId(){
		
		if(this.company != null)
			return this.company.toString();
		else
			return "";		
	}
	
	public String getLanguage(){
		
		if(this.language != null)
			return this.language.toString();
		else
			return "english"; //default English set		
	}
	
	public Set<String> getRoles(){
			return roles;		
	}


}
