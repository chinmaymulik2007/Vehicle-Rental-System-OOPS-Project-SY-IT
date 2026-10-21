public class Customer implements Storable {
    private int customerId;
    private String name;
    private String phone;
    private String licenseNumber;

    public Customer(int customerId, String name, String phone, String licenseNumber) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.licenseNumber = licenseNumber;
    }

    public int getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getLicenseNumber() { return licenseNumber; }

    public void displayDetails() {
        System.out.println("Customer ID : " + customerId);
        System.out.println("Name        : " + name);
        System.out.println("Phone       : " + phone);
        System.out.println("License No. : " + licenseNumber);
    }

    public String toFileString() {
        return customerId + "|" + name + "|" + phone + "|" + licenseNumber;
    }

    public String toString() {
        return customerId + " - " + name;
    }
}
