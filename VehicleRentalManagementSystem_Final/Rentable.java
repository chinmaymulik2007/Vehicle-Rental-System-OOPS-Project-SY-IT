/**
 * INTERFACE 1 - every rentable thing must be able to calculate rent and fine.
 */
public interface Rentable {
    double calculateRent(int days);
    double calculateFine(int lateDays);
}
