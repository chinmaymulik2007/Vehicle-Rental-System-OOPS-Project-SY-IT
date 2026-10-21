/** INHERITANCE - Car is a Vehicle. METHOD OVERRIDING - Car gives its own calculateRent(). */
public class Car extends Vehicle {
    private int seats;

    public Car(String id, String brand, String model, double rent, int seats) {
        super(id, brand, model, rent);
        this.seats = seats;
    }

    public double calculateRent(int days) {
        return getRentPerDay() * days + 200 * days;   // Rs. 200 per day service charge
    }

    public String getType() { return "Car"; }
    public String getExtra() { return String.valueOf(seats); }

    public void displayDetails() {
        super.displayDetails();
        System.out.println("         Type: Car | Seats: " + seats);
    }
}
