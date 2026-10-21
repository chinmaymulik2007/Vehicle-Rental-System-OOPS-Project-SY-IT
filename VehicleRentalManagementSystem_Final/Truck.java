/** INHERITANCE - Truck is a Vehicle. METHOD OVERRIDING - Truck gives its own calculateRent(). */
public class Truck extends Vehicle {
    private double loadCapacity;

    public Truck(String id, String brand, String model, double rent, double loadCapacity) {
        super(id, brand, model, rent);
        this.loadCapacity = loadCapacity;
    }

    public double calculateRent(int days) {
        return getRentPerDay() * days + 500 * days;   // Rs. 500 per day service charge
    }

    public String getType() { return "Truck"; }
    public String getExtra() { return String.valueOf(loadCapacity); }

    public void displayDetails() {
        super.displayDetails();
        System.out.println("         Type: Truck | Load Capacity: " + loadCapacity + " tons");
    }
}
