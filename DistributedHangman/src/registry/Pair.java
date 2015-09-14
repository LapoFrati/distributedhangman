package registry;

import java.io.Serializable;

public class Pair implements Serializable {
	
	private static final long serialVersionUID = -8478244958358729925L;
	private String str1, str2;
	
	public Pair(String userName, String encryptedPassword){
		this.str1 = userName;
		this.str2 = encryptedPassword;
	}
	
	/**
	 * Getter for the userName field
	 * @return the userName
	 */
	public String getUserName(){
		return this.str1;
	}
	
	/**
	 * Getter for the encryptedPassword field
	 * @return the encryptedPassword
	 */
	public String getEncryptedPassword(){
		return this.str2;
	}
}
