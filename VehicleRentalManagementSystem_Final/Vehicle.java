/**
 * ABSTRACT CLASS - a general "Vehicle" cannot be rented by itself,
 * only Car, Bike and Truck can. It implements both interfaces.
 */
public abstract class Vehicle implements Rentable, Storable {

    // late fine per day = rent per day x 1.5
    public static final double FINE_MULTIPLIER = 1.5;

    private String vehicleId;
    private String brand;
    private String model;
    private double rentPerDay;

    public Vehicle(String vehicleId, String brand, String model, double rentPerDay) {
        this.vehicleId = vehicleId;
        this.brand = brand;
        this.model = model;
        this.rentPerDay = rentPerDay;
    }

    // abstract methods - every child class MUST write its own version
    public abstract double calculateRent(int days);
    public abstract String getType();
    public abstract String getExtra();

    // normal method - common for all vehicles
    public double calculateFine(int lateDays) {
        if (lateDays <= 0) {
            return 0;
        }
        return lateDays * rentPerDay * FINE_MULTIPLIER;
    }

    public String getVehicleId() { return vehicleId; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public double getRentPerDay() { return rentPerDay; }

    public void displayDetails() {
        System.out.printf("%-8s %-12s %-15s %-10.2f%n", vehicleId, brand, model, rentPerDay);
    }

    // from Storable interface
    public String toFileString() {
        return getType() + "|" + vehicleId + "|" + brand + "|" + model + "|" + rentPerDay + "|" + getExtra();
    }

    public String toString() {
        return vehicleId + " - " + brand + " " + model;
    }
}
