import java.time.LocalDate;
import java.util.ArrayList;

/** Makes the analysis: which vehicles were never rented and which are rented most often. */
public class ReportGenerator {

    // small class to keep the numbers of one vehicle
    public static class VehicleStat {
        public Vehicle vehicle;
        public int timesRented;
        public int totalDays;
        public double income;
        public String category;

        public VehicleStat(Vehicle vehicle) {
            this.vehicle = vehicle;
        }
    }

    // all vehicles with their numbers, most rented first
    public static ArrayList<VehicleStat> frequency(ArrayList<Vehicle> vehicles, ArrayList<Rental> rentals) {
        LocalDate today = LocalDate.now();
        ArrayList<VehicleStat> list = new ArrayList<VehicleStat>();
        for (Vehicle v : vehicles) {
            VehicleStat s = new VehicleStat(v);
            for (Rental r : rentals) {
                if (r.getVehicle() == v) {
                    s.timesRented++;
                    s.totalDays += r.getDays();
                    s.income += r.getTotalAmount(today);
                }
            }
            list.add(s);
        }

        // bubble sort - highest timesRented first
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - 1 - i; j++) {
                VehicleStat a = list.get(j);
                VehicleStat b = list.get(j + 1);
                boolean swap = false;
                if (b.timesRented > a.timesRented) {
                    swap = true;
                } else if (b.timesRented == a.timesRented && b.totalDays > a.totalDays) {
                    swap = true;
                }
                if (swap) {
                    list.set(j, b);
                    list.set(j + 1, a);
                }
            }
        }

        // category: High = rented at least 2 times and at least 60% of the top vehicle's count
        int max = 0;
        if (!list.isEmpty()) {
            max = list.get(0).timesRented;
        }
        for (VehicleStat s : list) {
            if (s.timesRented == 0) {
                s.category = "Never rented";
            } else if (s.timesRented >= 2 && s.timesRented >= 0.6 * max) {
                s.category = "High";
            } else {
                s.category = "Low";
            }
        }
        return list;
    }

    public static ArrayList<Vehicle> neverRented(ArrayList<Vehicle> vehicles, ArrayList<Rental> rentals) {
        ArrayList<Vehicle> list = new ArrayList<Vehicle>();
        for (Vehicle v : vehicles) {
            boolean used = false;
            for (Rental r : rentals) {
                if (r.getVehicle() == v) {
                    used = true;
                }
            }
            if (!used) {
                list.add(v);
            }
        }
        return list;
    }

    // the same analysis as text (saved in report_analysis.txt)
    public static String buildTextReport(ArrayList<Vehicle> vehicles, ArrayList<Rental> rentals) {
        LocalDate today = LocalDate.now();
        ArrayList<VehicleStat> stats = frequency(vehicles, rentals);
        ArrayList<Vehicle> never = neverRented(vehicles, rentals);
        double rent = 0;
        double fines = 0;
        int overdue = 0;
        for (Rental r : rentals) {
            rent += r.getBaseAmount();
            fines += r.getFine();
            if (r.isOverdue(today)) {
                overdue++;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("VEHICLE RENTAL - ANALYSIS REPORT\n");
        sb.append("Generated on: ").append(today).append("\n");
        sb.append("==============================================================\n\n");
        sb.append("SUMMARY\n");
        sb.append(String.format("  Total vehicles        : %d%n", vehicles.size()));
        sb.append(String.format("  Rented at least once  : %d%n", vehicles.size() - never.size()));
        sb.append(String.format("  Never rented          : %d%n", never.size()));
        sb.append(String.format("  Total rentals         : %d%n", rentals.size()));
        sb.append(String.format("  Currently overdue     : %d%n", overdue));
        sb.append(String.format("  Rent amount           : Rs. %.2f%n", rent));
        sb.append(String.format("  Late fines            : Rs. %.2f%n", fines));
        sb.append(String.format("  Total income          : Rs. %.2f%n%n", rent + fines));

        sb.append("VEHICLES NEVER RENTED\n");
        if (never.isEmpty()) {
            sb.append("  (none - every vehicle has been rented)\n");
        }
        for (Vehicle v : never) {
            sb.append(String.format("  %-8s %-6s %-14s %-14s Rs. %.2f/day%n", v.getVehicleId(), v.getType(),
                    v.getBrand(), v.getModel(), v.getRentPerDay()));
        }

        sb.append("\nRENTAL FREQUENCY (HIGH TO LOW)\n");
        sb.append(String.format("  %-4s %-8s %-26s %-7s %-6s %-14s %s%n", "No", "ID", "Vehicle", "Times", "Days", "Income", "Category"));
        int rank = 1;
        for (VehicleStat s : stats) {
            sb.append(String.format("  %-4d %-8s %-26s %-7d %-6d Rs. %-10.2f %s%n", rank, s.vehicle.getVehicleId(),
                    s.vehicle.getBrand() + " " + s.vehicle.getModel(), s.timesRented, s.totalDays, s.income, s.category));
            rank++;
        }

        sb.append("\nHIGH FREQUENCY VEHICLES\n");
        boolean any = false;
        for (VehicleStat s : stats) {
            if (s.category.equals("High")) {
                any = true;
                sb.append("  " + s.vehicle + " - rented " + s.timesRented + " times\n");
            }
        }
        if (!any) {
            sb.append("  (none yet - a vehicle needs at least 2 rentals)\n");
        }
        return sb.toString();
    }
}
