package Registry;

public class UserInfo {
	private boolean loggedIn;
	private String encryptedPassword, host;
	
	public UserInfo(String encryptedPassword, String host){
		this.loggedIn = false;
		this.host = host;
		this.encryptedPassword = encryptedPassword;
	}
	
	public void login(){
		this.loggedIn = true;
	}
	
	public void logout(){
		this.loggedIn = false;
	}
	
	public boolean isLoggedIn(){
		return this.loggedIn;
	}
	
	public String getEncryptedPassword(){
		return this.encryptedPassword;
	}
	
	public void setHost(String newHost){
		this.host = newHost;
	}
	
	public String getHost(){
		return this.host;
	}
}
