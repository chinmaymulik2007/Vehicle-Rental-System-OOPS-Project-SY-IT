VEHICLE RENTAL MANAGEMENT SYSTEM  (Java Swing, college project)

HOW TO RUN (inside this folder, so the data files are created here)
    javac *.java
    java VehicleRentalGUI

FEATURES
1. Vehicles are given according to DATE: choose start date + days; only vehicles
   free for the whole period are shown. A vehicle cannot be booked twice.
   If the vehicle is not returned by the due date it shows as OVERDUE and a FINE
   is added: fine = rent per day x 1.5 for every late day.
2. Every rent / return / add event is written in  rental_log.txt  (file handling).
   Data is also saved in vehicles.txt, customers.txt, rentals.txt and is loaded
   again when the program starts.
3. Reports page: vehicles never rented + rental frequency (High / Low).
   "Export Report to File" creates report_analysis.txt.

CONCEPTS USED AND WHERE
Class and Object ........ Vehicle, Customer, Rental, RentalManager, FileManager ... (objects are
                          created with 'new', e.g. new Car(...), new Rental(...))
Inheritance ............. Car, Bike, Truck extend Vehicle;  ReadOnlyTableModel, StatusRenderer,
                          ExitHandler, OverdueMonitor extend library classes
Abstract class .......... Vehicle (abstract methods: calculateRent, getType, getExtra)
Interface ............... Rentable, Storable (our own);  ActionListener, Runnable (library)
Method overriding ....... calculateRent() / getType() / getExtra() / displayDetails() in Car, Bike,
                          Truck;  toString() in Vehicle, Customer, Rental;  actionPerformed(), run()
Method overloading ...... Rental constructors (new rental / loaded from file), Rental.getFine() and
                          getFine(date), FileManager.log(event) and log(type, details)
Exception handling ...... InvalidDataException, VehicleNotAvailableException (our own),
                          IOException, NumberFormatException, DateTimeParseException, InterruptedException
                          (try / catch / finally)
Multithreading .......... OverdueMonitor (extends Thread) updates the overdue counter every 5 s;
                          AutoSaveTask (implements Runnable) saves data every 10 s;
                          RentalManager methods are 'synchronized'
File handling ........... FileManager (BufferedReader, BufferedWriter, FileReader, FileWriter, File)

FILES IN THIS FOLDER
Rentable, Storable ............. interfaces
Vehicle, Car, Bike, Truck ...... vehicle classes
Customer, Rental ............... data classes
InvalidDataException, VehicleNotAvailableException ... exceptions
RentalManager .................. all rent / return logic
FileManager .................... file reading and writing
ReportGenerator ................ analysis report
OverdueMonitor, AutoSaveTask ... threads
VehicleRentalGUI ............... main window (run this one)
ReadOnlyTableModel, StatusRenderer, ExitHandler ... small GUI helper classes
