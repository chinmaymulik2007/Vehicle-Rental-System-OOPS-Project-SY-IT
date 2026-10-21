import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * Keeps all vehicles, customers and rentals and does the rent / return work.
 * MULTITHREADING - the methods are 'synchronized' because the GUI thread, the
 * OverdueMonitor thread and the AutoSaveTask thread all use this object.
 * EXCEPTION HANDLING - methods throw our own exceptions when something is wrong.
 */
public class RentalManager {
    private ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
    private ArrayList<Customer> customers = new ArrayList<Customer>();
    private ArrayList<Rental> rentals = new ArrayList<Rental>();
    private int nextCustomerId = 1;
    private int nextRentalId = 1;
    private boolean dirty = false;          // true = something changed and is not saved yet
    private FileManager files = new FileManager();

    public FileManager getFiles() { return files; }

    // ---------------------------------------------------------------- load / save
    public synchronized void loadAll() throws IOException {
        for (String line : files.readLines(FileManager.VEHICLES_FILE)) {
            try {
                vehicles.add(parseVehicle(line));
            } catch (InvalidDataException e) {
                System.out.println("Skipped damaged line in vehicles file: " + line);
            }
        }
        if (vehicles.isEmpty()) {
            addSampleVehicles();
        }
        for (String line : files.readLines(FileManager.CUSTOMERS_FILE)) {
            try {
                Customer c = parseCustomer(line);
                customers.add(c);
                if (c.getCustomerId() >= nextCustomerId) {
                    nextCustomerId = c.getCustomerId() + 1;
                }
            } catch (InvalidDataException e) {
                System.out.println("Skipped damaged line in customers file: " + line);
            }
        }
        for (String line : files.readLines(FileManager.RENTALS_FILE)) {
            try {
                Rental r = parseRental(line);
                rentals.add(r);
                if (r.getRentalId() >= nextRentalId) {
                    nextRentalId = r.getRentalId() + 1;
                }
            } catch (InvalidDataException e) {
                System.out.println("Skipped damaged line in rentals file: " + line);
            }
        }
    }

    public synchronized void saveAll() throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        for (Vehicle v : vehicles) {
            lines.add(v.toFileString());            // interface Storable
        }
        files.writeLines(FileManager.VEHICLES_FILE, lines);

        lines = new ArrayList<String>();
        for (Customer c : customers) {
            lines.add(c.toFileString());
        }
        files.writeLines(FileManager.CUSTOMERS_FILE, lines);

        lines = new ArrayList<String>();
        for (Rental r : rentals) {
            lines.add(r.toFileString());
        }
        files.writeLines(FileManager.RENTALS_FILE, lines);
        dirty = false;
    }

    public synchronized boolean isDirty() { return dirty; }

    private Vehicle parseVehicle(String line) throws InvalidDataException {
        try {
            String[] f = line.split("\\|", -1);
            double rent = Double.parseDouble(f[4]);
            if (f[0].equals("Car")) {
                return new Car(f[1], f[2], f[3], rent, Integer.parseInt(f[5]));
            } else if (f[0].equals("Bike")) {
                return new Bike(f[1], f[2], f[3], rent, Integer.parseInt(f[5]));
            } else if (f[0].equals("Truck")) {
                return new Truck(f[1], f[2], f[3], rent, Double.parseDouble(f[5]));
            }
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Bad number in vehicle line");
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new InvalidDataException("Vehicle line is incomplete");
        }
        throw new InvalidDataException("Unknown vehicle type");
    }

    private Customer parseCustomer(String line) throws InvalidDataException {
        try {
            String[] f = line.split("\\|", -1);
            return new Customer(Integer.parseInt(f[0]), f[1], f[2], f[3]);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Bad number in customer line");
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new InvalidDataException("Customer line is incomplete");
        }
    }

    private Rental parseRental(String line) throws InvalidDataException {
        try {
            String[] f = line.split("\\|", -1);
            Customer c = findCustomer(Integer.parseInt(f[1]));
            Vehicle v = findVehicle(f[2]);
            if (c == null || v == null) {
                throw new InvalidDataException("Rental refers to a missing customer or vehicle");
            }
            LocalDate returnDate = null;
            if (!f[5].equals("-")) {
                returnDate = LocalDate.parse(f[5]);
            }
            return new Rental(Integer.parseInt(f[0]), c, v, Integer.parseInt(f[3]), LocalDate.parse(f[4]),
                    returnDate, Double.parseDouble(f[6]), Boolean.parseBoolean(f[7]));
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Bad number in rental line");
        } catch (DateTimeParseException e) {
            throw new InvalidDataException("Bad date in rental line");
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new InvalidDataException("Rental line is incomplete");
        }
    }

    private void addSampleVehicles() {
        vehicles.add(new Car("C101", "Toyota", "Innova", 1800, 7));
        vehicles.add(new Car("C102", "Hyundai", "Creta", 1500, 5));
        vehicles.add(new Bike("B101", "Honda", "Shine", 600, 125));
        vehicles.add(new Bike("B102", "Royal Enfield", "Classic 350", 900, 350));
        vehicles.add(new Truck("T101", "Tata", "407", 3000, 4));
    }

    // ---------------------------------------------------------------- find
    public synchronized Vehicle findVehicle(String id) {
        for (Vehicle v : vehicles) {
            if (v.getVehicleId().equalsIgnoreCase(id)) {
                return v;
            }
        }
        return null;
    }

    public synchronized Customer findCustomer(int id) {
        for (Customer c : customers) {
            if (c.getCustomerId() == id) {
                return c;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- get copies of the lists
    public synchronized ArrayList<Vehicle> getVehicles() { return new ArrayList<Vehicle>(vehicles); }
    public synchronized ArrayList<Customer> getCustomers() { return new ArrayList<Customer>(customers); }
    public synchronized ArrayList<Rental> getRentals() { return new ArrayList<Rental>(rentals); }

    // ---------------------------------------------------------------- add vehicle / customer
    public synchronized void addVehicle(Vehicle v) throws InvalidDataException, IOException {
        if (findVehicle(v.getVehicleId()) != null) {
            throw new InvalidDataException("Vehicle ID already exists.");
        }
        vehicles.add(v);
        dirty = true;
        files.log("VEHICLE", "Added " + v.getType() + " " + v + " @ Rs. " + v.getRentPerDay() + "/day");
    }

    public synchronized Customer addCustomer(String name, String phone, String license)
            throws InvalidDataException, IOException {
        if (name.isEmpty() || phone.isEmpty() || license.isEmpty()) {
            throw new InvalidDataException("All fields are required.");
        }
        Customer c = new Customer(nextCustomerId, name, phone, license);
        nextCustomerId++;
        customers.add(c);
        dirty = true;
        files.log("CUSTOMER", "Added customer " + c);
        return c;
    }

    // ---------------------------------------------------------------- dates
    public static LocalDate parseDate(String text) throws InvalidDataException {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new InvalidDataException("Date must be written as yyyy-MM-dd  (example 2026-10-25).");
        }
    }

    // is the vehicle free for the whole period [start, end) ?
    public synchronized boolean isFree(Vehicle v, LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();
        for (Rental r : rentals) {
            if (r.getVehicle() == v && r.blocks(start, end, today)) {
                return false;
            }
        }
        return true;
    }

    public synchronized ArrayList<Vehicle> getFreeVehicles(LocalDate start, int days) {
        ArrayList<Vehicle> list = new ArrayList<Vehicle>();
        for (Vehicle v : vehicles) {
            if (isFree(v, start, start.plusDays(days))) {
                list.add(v);
            }
        }
        return list;
    }

    // Available / Booked (future) / Rented (now) / Overdue
    public synchronized String getVehicleStatus(Vehicle v) {
        LocalDate today = LocalDate.now();
        boolean booked = false;
        for (Rental r : rentals) {
            if (r.getVehicle() != v || r.isReturned()) {
                continue;
            }
            if (r.isOverdue(today)) {
                return "Overdue";
            }
            if (!today.isBefore(r.getStartDate())) {
                return "Rented";
            }
            booked = true;
        }
        if (booked) {
            return "Booked";
        }
        return "Available";
    }

    // ---------------------------------------------------------------- rent
    public synchronized Rental rentVehicle(Customer c, Vehicle v, int days, LocalDate start)
            throws VehicleNotAvailableException, InvalidDataException, IOException {
        if (days < 1) {
            throw new InvalidDataException("Rental days must be at least 1.");
        }
        if (start.isBefore(LocalDate.now())) {
            throw new InvalidDataException("Start date cannot be in the past.");
        }
        if (!isFree(v, start, start.plusDays(days))) {
            throw new VehicleNotAvailableException(v + " is not available for " + start + " to " + start.plusDays(days) + ".");
        }
        Rental r = new Rental(nextRentalId, c, v, days, start);
        nextRentalId++;
        rentals.add(r);
        dirty = true;
        files.log("RENT", "Rental " + r.getRentalId() + " | Customer " + c + " | Vehicle " + v
                + " | Start " + r.getStartDate() + " | Due " + r.getDueDate() + " | Days " + days
                + " | Rent Rs. " + r.getBaseAmount());
        return r;
    }

    // ---------------------------------------------------------------- return
    public synchronized void returnVehicle(Rental r, LocalDate date) throws InvalidDataException, IOException {
        r.returnVehicle(date);          // may throw InvalidDataException
        dirty = true;
        files.log("RETURN", "Rental " + r.getRentalId() + " | Customer " + r.getCustomer() + " | Vehicle " + r.getVehicle()
                + " | Returned " + date + " | Due " + r.getDueDate() + " | Late days " + r.getLateDays(date)
                + " | Rent Rs. " + r.getBaseAmount() + " | Fine Rs. " + r.getFine(date)
                + " | Total Rs. " + r.getTotalAmount(date));
    }

    // ---------------------------------------------------------------- counts
    public synchronized int countOverdue() {
        int count = 0;
        LocalDate today = LocalDate.now();
        for (Rental r : rentals) {
            if (r.isOverdue(today)) {
                count++;
            }
        }
        return count;
    }

    public synchronized int countAvailable() {
        int count = 0;
        for (Vehicle v : vehicles) {
            String s = getVehicleStatus(v);
            if (s.equals("Available") || s.equals("Booked")) {
                count++;
            }
        }
        return count;
    }
}
