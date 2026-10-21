import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * One rental of one vehicle by one customer.
 * Has TWO constructors (METHOD OVERLOADING) and two getFine() methods (METHOD OVERLOADING).
 */
public class Rental implements Storable {
    private int rentalId;
    private Customer customer;
    private Vehicle vehicle;
    private int days;
    private LocalDate startDate;    // day the vehicle is given
    private LocalDate dueDate;      // startDate + days
    private double baseAmount;      // rent for the booked days
    private LocalDate returnDate;   // actual return date (null until returned)
    private double fine;            // fine, fixed when the vehicle is returned
    private boolean returned;

    // constructor 1 - a new rental
    public Rental(int rentalId, Customer customer, Vehicle vehicle, int days, LocalDate startDate) {
        this.rentalId = rentalId;
        this.customer = customer;
        this.vehicle = vehicle;
        this.days = days;
        this.startDate = startDate;
        this.dueDate = startDate.plusDays(days);
        this.baseAmount = vehicle.calculateRent(days);   // polymorphism: Car/Bike/Truck version runs
        this.returned = false;
    }

    // constructor 2 - a rental loaded back from the file
    public Rental(int rentalId, Customer customer, Vehicle vehicle, int days, LocalDate startDate,
                  LocalDate returnDate, double fine, boolean returned) {
        this(rentalId, customer, vehicle, days, startDate);
        this.returnDate = returnDate;
        this.fine = fine;
        this.returned = returned;
    }

    public int getRentalId() { return rentalId; }
    public Customer getCustomer() { return customer; }
    public Vehicle getVehicle() { return vehicle; }
    public int getDays() { return days; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getBaseAmount() { return baseAmount; }
    public boolean isReturned() { return returned; }

    // how many days late (if already returned, the real return date is used)
    public int getLateDays(LocalDate asOf) {
        LocalDate endDate = asOf;
        if (returned) {
            endDate = returnDate;
        }
        long late = ChronoUnit.DAYS.between(dueDate, endDate);
        if (late < 0) {
            return 0;
        }
        return (int) late;
    }

    // getFine() - overloaded: version 1 uses today's date
    public double getFine() {
        return getFine(LocalDate.now());
    }

    // getFine(date) - overloaded: version 2 uses the given date
    public double getFine(LocalDate asOf) {
        if (returned) {
            return fine;
        }
        return vehicle.calculateFine(getLateDays(asOf));
    }

    public double getTotalAmount(LocalDate asOf) {
        return baseAmount + getFine(asOf);
    }

    // vehicle is still out although the due date is over
    public boolean isOverdue(LocalDate today) {
        return !returned && today.isAfter(dueDate);
    }

    // does this rental keep the vehicle busy in the period [start, end) ?
    public boolean blocks(LocalDate start, LocalDate end, LocalDate today) {
        if (returned) {
            return false;
        }
        if (isOverdue(today)) {
            return end.isAfter(startDate);      // overdue vehicle is blocked for all later dates
        }
        return start.isBefore(dueDate) && end.isAfter(startDate);
    }

    public void returnVehicle(LocalDate date) throws InvalidDataException {
        if (returned) {
            throw new InvalidDataException("This rental is already returned.");
        }
        if (date.isBefore(startDate)) {
            throw new InvalidDataException("Return date cannot be before the start date (" + startDate + ").");
        }
        returnDate = date;
        returned = true;
        fine = vehicle.calculateFine(getLateDays(date));
    }

    public String getStatus(LocalDate today) {
        if (returned) {
            if (getLateDays(today) > 0) {
                return "Returned (Late)";
            }
            return "Returned";
        }
        if (today.isBefore(startDate)) {
            return "Upcoming";
        }
        if (isOverdue(today)) {
            return "Overdue";
        }
        return "Active";
    }

    public void displayDetails() {
        LocalDate today = LocalDate.now();
        System.out.println("----------------------------------------");
        System.out.println("Rental ID    : " + rentalId);
        System.out.println("Customer     : " + customer.getName());
        System.out.println("Vehicle      : " + vehicle.getBrand() + " " + vehicle.getModel());
        System.out.println("Start Date   : " + startDate);
        System.out.println("Due Date     : " + dueDate);
        System.out.printf("Rent         : Rs. %.2f%n", baseAmount);
        System.out.printf("Fine         : Rs. %.2f%n", getFine());
        System.out.println("Status       : " + getStatus(today));
        System.out.println("----------------------------------------");
    }

    public String toFileString() {
        String ret = "-";
        if (returnDate != null) {
            ret = returnDate.toString();
        }
        return rentalId + "|" + customer.getCustomerId() + "|" + vehicle.getVehicleId() + "|" + days + "|"
                + startDate + "|" + ret + "|" + fine + "|" + returned;
    }

    public String toString() {
        String text = "Rental " + rentalId + " - " + vehicle.getVehicleId() + " - " + customer.getName()
                + " (due " + dueDate + ")";
        if (isOverdue(LocalDate.now())) {
            text = text + " OVERDUE";
        }
        return text;
    }
}
