package query;

@SuppressWarnings("serial")
public class WrongQuerySyntaxException extends Exception {
	
	public WrongQuerySyntaxException(String message) {
        super(message);
    }

}
