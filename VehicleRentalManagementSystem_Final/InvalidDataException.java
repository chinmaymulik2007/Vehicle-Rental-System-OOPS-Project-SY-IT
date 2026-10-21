/**
 * EXCEPTION HANDLING - our own (user defined) exception.
 * Thrown when the user enters wrong data (bad date, wrong days, duplicate id ...).
 */
public class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
}
