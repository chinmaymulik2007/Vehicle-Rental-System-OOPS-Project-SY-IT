/** INHERITANCE - Bike is a Vehicle. METHOD OVERRIDING - Bike gives its own calculateRent(). */
public class Bike extends Vehicle {
    private int engineCC;

    public Bike(String id, String brand, String model, double rent, int engineCC) {
        super(id, brand, model, rent);
        this.engineCC = engineCC;
    }

    public double calculateRent(int days) {
        return getRentPerDay() * days + 50 * days;    // Rs. 50 per day service charge
    }

    public String getType() { return "Bike"; }
    public String getExtra() { return String.valueOf(engineCC); }

    public void displayDetails() {
        super.displayDetails();
        System.out.println("         Type: Bike | Engine: " + engineCC + " CC");
    }
}
