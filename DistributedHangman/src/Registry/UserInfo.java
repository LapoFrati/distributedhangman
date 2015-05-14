package Registry;

public class UserInfo {
	private boolean loggedIn;
	private String encryptedPassword;
	
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
}
