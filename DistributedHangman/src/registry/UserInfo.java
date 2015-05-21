package registry;

public class UserInfo {
	private boolean loggedIn;
	private String encryptedPassword, host;
	private UserNotificationIF callback = null;
	
	public UserInfo(String encryptedPassword){
		this.loggedIn = false;
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
	
	public void setCallback(UserNotificationIF callback){
		this.callback = callback;
	}
	
	public UserNotificationIF getCallback(){
		return this.callback;
	}
}
