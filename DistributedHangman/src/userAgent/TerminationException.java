package userAgent;

public class TerminationException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public TerminationException(String message){
		super(message);
	}
}