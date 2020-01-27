package dataImporter;

@SuppressWarnings("serial")
public class TwoRowsHaveSameValueException extends Exception {
 
    public TwoRowsHaveSameValueException(String message) {
        super(message);
    }
}