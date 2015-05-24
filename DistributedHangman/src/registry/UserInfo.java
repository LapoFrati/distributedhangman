package registry;

public class UserInfo {
	private boolean loggedIn;
	private String encryptedPassword;
	private UserNotificationIF callback = null;
	private WaitingRoom waitingRoom;
	
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
	
	public void setCallback(UserNotificationIF callback){
		this.callback = callback;
	}
	
	public UserNotificationIF getCallback(){
		return this.callback;
	}
	
	public void setWaitingRoom( WaitingRoom newRoom ){
		this.waitingRoom = newRoom;
	}
	
	public WaitingRoom getWaitingRoom(){
		return this.waitingRoom;
	}
}
