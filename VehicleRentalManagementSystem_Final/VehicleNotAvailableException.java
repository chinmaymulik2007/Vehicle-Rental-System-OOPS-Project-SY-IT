/**
 * EXCEPTION HANDLING - our own (user defined) exception.
 * Thrown when a vehicle is already booked for the selected dates.
 */
public class VehicleNotAvailableException extends Exception {
    public VehicleNotAvailableException(String message) {
        super(message);
    }
}
