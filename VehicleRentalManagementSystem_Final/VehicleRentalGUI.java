import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Main window (Java Swing).
 * INTERFACE - the class implements ActionListener, so one method (actionPerformed)
 * handles every button click.
 */
public class VehicleRentalGUI extends JFrame implements ActionListener {

    // colours
    private static final Color BG = new Color(245, 247, 251);
    private static final Color NAVY = new Color(25, 34, 56);
    private static final Color BLUE = new Color(49, 102, 246);
    private static final Color GREEN = new Color(32, 158, 91);
    private static final Color ORANGE = new Color(235, 145, 45);
    private static final Color RED = new Color(214, 72, 72);
    private static final Color TEXT = new Color(35, 42, 55);
    private static final Color MUTED = new Color(108, 117, 133);
    private static final Color LIGHT_RED = new Color(255, 228, 228);
    private static final Color LIGHT_GREEN = new Color(222, 246, 233);

    // object that holds all data + the background threads
    private RentalManager manager = new RentalManager();
    private OverdueMonitor monitor;
    private AutoSaveTask autoSave;

    // layout
    private CardLayout cardLayout = new CardLayout();
    private JPanel cards = new JPanel(cardLayout);
    private JLabel pageTitle = new JLabel("Dashboard");
    private JLabel banner = new JLabel("");

    // tables
    private ReadOnlyTableModel vehicleModel = new ReadOnlyTableModel(new String[]{"ID", "Type", "Brand", "Model", "Rent / Day", "Status"});
    private ReadOnlyTableModel customerModel = new ReadOnlyTableModel(new String[]{"ID", "Name", "Phone", "License"});
    private ReadOnlyTableModel rentalModel = new ReadOnlyTableModel(new String[]{"Rental ID", "Customer", "Vehicle", "Start", "Due", "Returned On", "Days", "Rent", "Fine", "Total", "Status"});
    private ReadOnlyTableModel neverModel = new ReadOnlyTableModel(new String[]{"ID", "Type", "Brand", "Model", "Rent / Day"});
    private ReadOnlyTableModel freqModel = new ReadOnlyTableModel(new String[]{"No", "ID", "Vehicle", "Times Rented", "Total Days", "Income", "Category"});
    private JTable vehicleTable = makeTable(vehicleModel);
    private JTable customerTable = makeTable(customerModel);
    private JTable rentalTable = makeTable(rentalModel);
    private JTable neverTable = makeTable(neverModel);
    private JTable freqTable = makeTable(freqModel);

    // dashboard numbers
    private JLabel lblVehicles = new JLabel("0");
    private JLabel lblAvailable = new JLabel("0");
    private JLabel lblCustomers = new JLabel("0");
    private JLabel lblRentals = new JLabel("0");
    private JLabel lblOverdue = new JLabel("0");

    // navigation buttons
    private JButton navDashboard = makeButton("Dashboard", NAVY);
    private JButton navVehicles = makeButton("Vehicles", NAVY);
    private JButton navCustomers = makeButton("Customers", NAVY);
    private JButton navRent = makeButton("Rent / Return", NAVY);
    private JButton navRecords = makeButton("Rental Records", NAVY);
    private JButton navReports = makeButton("Reports", NAVY);
    private JButton navLog = makeButton("Rent Log File", NAVY);

    // other buttons
    private JButton addVehicleBtn = makeButton("+ Add Vehicle", BLUE);
    private JButton addCustomerBtn = makeButton("+ Add Customer", BLUE);
    private JButton refreshRecordsBtn = makeButton("Refresh", BLUE);
    private JButton checkBtn = makeButton("Show Available Vehicles", new Color(100, 110, 128));
    private JButton rentBtn = makeButton("Rent Vehicle", BLUE);
    private JButton previewBtn = makeButton("Calculate Fine", new Color(100, 110, 128));
    private JButton returnBtn = makeButton("Return Vehicle", BLUE);
    private JButton exportBtn = makeButton("Export Report to File", BLUE);
    private JButton refreshLogBtn = makeButton("Refresh", BLUE);

    // rent form
    private JComboBox<Customer> customerBox = new JComboBox<Customer>();
    private JComboBox<Vehicle> vehicleBox = new JComboBox<Vehicle>();
    private JTextField startField = new JTextField(LocalDate.now().toString());
    private JTextField daysField = new JTextField("1");
    private JLabel dueLabel = makeValueLabel("-");

    // return form
    private JComboBox<Rental> rentalBox = new JComboBox<Rental>();
    private JTextField returnField = new JTextField(LocalDate.now().toString());
    private JLabel retDueLabel = makeValueLabel("-");
    private JLabel retLateLabel = makeValueLabel("-");
    private JLabel retFineLabel = makeValueLabel("-");
    private JLabel retTotalLabel = makeValueLabel("-");

    // reports + log
    private JLabel reportSummary = new JLabel("");
    private JTextArea logArea = new JTextArea();

    // ------------------------------------------------------------------ start
    public VehicleRentalGUI() {
        setTitle("Vehicle Rental Management System");
        setSize(1240, 740);
        setMinimumSize(new Dimension(1020, 640));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new ExitHandler(this));
        setLocationRelativeTo(null);

        try {
            manager.loadAll();                        // FILE HANDLING - read saved data
        } catch (IOException e) {
            showError("Could not read the data files: " + e.getMessage());
        }

        buildUI();
        refreshAll();
        installRenderers();

        // start the two background threads
        monitor = new OverdueMonitor(this);
        monitor.start();
        autoSave = new AutoSaveTask(manager);
        Thread saveThread = new Thread(autoSave);
        saveThread.setDaemon(true);
        saveThread.start();
    }

    public void saveAndExit() {
        monitor.stopMonitor();
        autoSave.stopTask();
        try {
            manager.saveAll();
        } catch (IOException e) {
            showError("Could not save data: " + e.getMessage());
        }
        System.exit(0);
    }

    // warns about vehicles that are not returned after the due date
    public void showOverdueAlert() {
        LocalDate today = LocalDate.now();
        String text = "";
        for (Rental r : manager.getRentals()) {
            if (r.isOverdue(today)) {
                text = text + "Rental " + r.getRentalId() + " - " + r.getVehicle() + " - " + r.getCustomer().getName()
                        + " - due " + r.getDueDate() + " - " + r.getLateDays(today) + " day(s) late - fine Rs. "
                        + String.format("%.2f", r.getFine()) + "\n";
            }
        }
        if (!text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "These vehicles have NOT been returned on time:\n\n" + text,
                    "Overdue vehicles", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ------------------------------------------------------------------ build screen
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        // sidebar
        JPanel side = new JPanel(new GridLayout(0, 1, 0, 4));
        side.setBackground(NAVY);
        JPanel sideWrap = new JPanel(new BorderLayout());
        sideWrap.setBackground(NAVY);
        sideWrap.setPreferredSize(new Dimension(220, 0));
        sideWrap.setBorder(new EmptyBorder(22, 14, 18, 14));
        JLabel brand = new JLabel("<html><b style='font-size:15px'>Vehicle Rental</b><br>Management System</html>");
        brand.setForeground(Color.WHITE);
        brand.setBorder(new EmptyBorder(0, 6, 26, 0));
        sideWrap.add(brand, BorderLayout.NORTH);
        side.add(navDashboard);
        side.add(navVehicles);
        side.add(navCustomers);
        side.add(navRent);
        side.add(navRecords);
        side.add(navReports);
        side.add(navLog);
        JPanel sideTop = new JPanel(new BorderLayout());
        sideTop.setBackground(NAVY);
        sideTop.add(side, BorderLayout.NORTH);
        sideWrap.add(sideTop, BorderLayout.CENTER);
        JLabel footer = new JLabel("OOP | Threads | Files", SwingConstants.CENTER);
        footer.setForeground(new Color(145, 155, 178));
        sideWrap.add(footer, BorderLayout.SOUTH);
        root.add(sideWrap, BorderLayout.WEST);

        // header
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(22, 26, 22, 26));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        pageTitle.setForeground(TEXT);
        header.add(pageTitle, BorderLayout.WEST);
        banner.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.add(banner, BorderLayout.EAST);
        main.add(header, BorderLayout.NORTH);

        // pages
        cards.setOpaque(false);
        cards.add(buildDashboardPage(), "Dashboard");
        cards.add(buildTablePage("Vehicle Fleet", vehicleTable, addVehicleBtn), "Vehicles");
        cards.add(buildTablePage("Customer Directory", customerTable, addCustomerBtn), "Customers");
        cards.add(buildRentReturnPage(), "Rent / Return");
        cards.add(buildTablePage("Rental History (fine for vehicles not yet returned is counted up to today)", rentalTable, refreshRecordsBtn), "Rental Records");
        cards.add(buildReportsPage(), "Reports");
        cards.add(buildLogPage(), "Rent Log File");
        main.add(cards, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
    }

    private JPanel buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setOpaque(false);

        JPanel stats = new JPanel(new GridLayout(1, 5, 14, 0));
        stats.setOpaque(false);
        stats.add(makeStatCard("TOTAL VEHICLES", lblVehicles, BLUE));
        stats.add(makeStatCard("AVAILABLE", lblAvailable, GREEN));
        stats.add(makeStatCard("CUSTOMERS", lblCustomers, ORANGE));
        stats.add(makeStatCard("RENTALS", lblRentals, NAVY));
        stats.add(makeStatCard("OVERDUE", lblOverdue, RED));
        page.add(stats, BorderLayout.NORTH);

        String info = "<html><body style='font-size:12px'>"
                + "<b style='color:#3166f6'>CLASS / OBJECT</b> - Vehicle, Customer, Rental, RentalManager ...<br><br>"
                + "<b style='color:#3166f6'>INHERITANCE</b> - Car, Bike, Truck extend Vehicle<br><br>"
                + "<b style='color:#3166f6'>ABSTRACT CLASS</b> - Vehicle (abstract methods calculateRent, getType, getExtra)<br><br>"
                + "<b style='color:#3166f6'>INTERFACE</b> - Rentable, Storable, ActionListener<br><br>"
                + "<b style='color:#3166f6'>METHOD OVERRIDING</b> - calculateRent() in Car / Bike / Truck, toString() ...<br><br>"
                + "<b style='color:#3166f6'>METHOD OVERLOADING</b> - Rental constructors, getFine(), FileManager.log()<br><br>"
                + "<b style='color:#3166f6'>EXCEPTION HANDLING</b> - InvalidDataException, VehicleNotAvailableException, IOException<br><br>"
                + "<b style='color:#3166f6'>MULTITHREADING</b> - OverdueMonitor (Thread) and AutoSaveTask (Runnable)<br><br>"
                + "<b style='color:#3166f6'>FILE HANDLING</b> - rental_log.txt, vehicles.txt, customers.txt, rentals.txt, report_analysis.txt"
                + "</body></html>";
        JLabel infoLabel = new JLabel(info);
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        infoLabel.setForeground(TEXT);
        JPanel card = makeCard("Concepts Used In This Project", infoLabel);
        page.add(card, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildTablePage(String title, JTable table, JButton button) {
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(button, BorderLayout.EAST);
        body.add(top, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(224, 228, 236)));
        body.add(scroll, BorderLayout.CENTER);
        return makeCard(title, body);
    }

    private JPanel buildRentReturnPage() {
        JPanel page = new JPanel(new GridLayout(1, 2, 16, 0));
        page.setOpaque(false);

        // ---- rent form
        JPanel rentForm = new JPanel(new GridLayout(0, 2, 10, 12));
        rentForm.setOpaque(false);
        rentForm.add(makeLabel("Customer"));
        rentForm.add(customerBox);
        rentForm.add(makeLabel("Start Date (yyyy-MM-dd)"));
        rentForm.add(startField);
        rentForm.add(makeLabel("Rental Days"));
        rentForm.add(daysField);
        rentForm.add(makeLabel("Due / Return By"));
        rentForm.add(dueLabel);
        rentForm.add(makeLabel("Vehicle (free for these dates)"));
        rentForm.add(vehicleBox);
        rentForm.add(checkBtn);
        rentForm.add(rentBtn);
        JLabel note1 = new JLabel("<html>Step 1: enter start date and days, click 'Show Available Vehicles'.<br>"
                + "Late return fine = rent per day x " + Vehicle.FINE_MULTIPLIER + " for every extra day.</html>");
        note1.setForeground(MUTED);
        JPanel rentBody = new JPanel(new BorderLayout(0, 14));
        rentBody.setOpaque(false);
        rentBody.add(rentForm, BorderLayout.NORTH);
        rentBody.add(note1, BorderLayout.CENTER);
        page.add(makeCard("Rent a Vehicle", rentBody));

        // ---- return form
        JPanel retForm = new JPanel(new GridLayout(0, 2, 10, 12));
        retForm.setOpaque(false);
        retForm.add(makeLabel("Active Rental"));
        retForm.add(rentalBox);
        retForm.add(makeLabel("Return Date (yyyy-MM-dd)"));
        retForm.add(returnField);
        retForm.add(makeLabel("Due Date"));
        retForm.add(retDueLabel);
        retForm.add(makeLabel("Days Late"));
        retForm.add(retLateLabel);
        retForm.add(makeLabel("Fine"));
        retForm.add(retFineLabel);
        retForm.add(makeLabel("Total Payable"));
        retForm.add(retTotalLabel);
        retForm.add(previewBtn);
        retForm.add(returnBtn);
        JLabel note2 = new JLabel("<html>Return date is today by default. Change it to check or try a late return.</html>");
        note2.setForeground(MUTED);
        JPanel retBody = new JPanel(new BorderLayout(0, 14));
        retBody.setOpaque(false);
        retBody.add(retForm, BorderLayout.NORTH);
        retBody.add(note2, BorderLayout.CENTER);
        page.add(makeCard("Return a Vehicle", retBody));
        return page;
    }

    private JPanel buildReportsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);
        reportSummary.setForeground(TEXT);
        top.add(reportSummary, BorderLayout.CENTER);
        top.add(exportBtn, BorderLayout.EAST);
        page.add(top, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gb = new GridBagConstraints();
        gb.fill = GridBagConstraints.BOTH;
        gb.weighty = 1;
        gb.gridy = 0;
        gb.gridx = 0;
        gb.weightx = 0.4;
        gb.insets = new Insets(0, 0, 0, 7);
        grid.add(makeCard("Vehicles Never Rented", new JScrollPane(neverTable)), gb);
        gb.gridx = 1;
        gb.weightx = 0.6;
        gb.insets = new Insets(0, 7, 0, 0);
        grid.add(makeCard("Rental Frequency (High to Low)", new JScrollPane(freqTable)), gb);
        page.add(grid, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildLogPage() {
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel where = new JLabel("File: " + manager.getFiles().getFolderPath() + "/" + FileManager.LOG_FILE);
        where.setForeground(MUTED);
        top.add(where, BorderLayout.CENTER);
        top.add(refreshLogBtn, BorderLayout.EAST);
        body.add(top, BorderLayout.NORTH);
        body.add(new JScrollPane(logArea), BorderLayout.CENTER);
        return makeCard("Rent Log (saved in " + FileManager.LOG_FILE + ")", body);
    }

    // ------------------------------------------------------------------ small helper methods
    private JButton makeButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(11, 16, 11, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(this);          // 'this' is an ActionListener
        return b;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        return l;
    }

    private JLabel makeValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        return l;
    }

    private JPanel makeCard(String title, JComponent body) {
        JPanel p = new JPanel(new BorderLayout(10, 12));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(225, 228, 235)), new EmptyBorder(15, 16, 16, 16)));
        JLabel l = new JLabel(title);
        l.setFont(new Font("SansSerif", Font.BOLD, 16));
        l.setForeground(TEXT);
        p.add(l, BorderLayout.NORTH);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JPanel makeStatCard(String title, JLabel value, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(225, 228, 235)), new EmptyBorder(15, 17, 14, 17)));
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 10));
        t.setForeground(MUTED);
        value.setForeground(color);
        value.setFont(new Font("SansSerif", Font.BOLD, 28));
        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private JTable makeTable(ReadOnlyTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(30);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        return t;
    }

    private void installRenderers() {
        String[] lateWords = {"Overdue", "Returned (Late)"};
        rentalTable.setDefaultRenderer(Object.class, new StatusRenderer(10, lateWords, LIGHT_RED));
        String[] overdueWord = {"Overdue"};
        vehicleTable.setDefaultRenderer(Object.class, new StatusRenderer(5, overdueWord, LIGHT_RED));
        String[] highWord = {"High"};
        freqTable.setDefaultRenderer(Object.class, new StatusRenderer(6, highWord, LIGHT_GREEN));
        int[] rw = {70, 90, 170, 85, 85, 90, 45, 85, 80, 95, 120};
        for (int i = 0; i < rw.length; i++) {
            rentalTable.getColumnModel().getColumn(i).setPreferredWidth(rw[i]);
        }
        int[] w = {45, 55, 150, 95, 85, 95, 100};
        for (int i = 0; i < w.length; i++) {
            freqTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // remove the '|' sign from user text because '|' separates the fields in our files
    private String clean(String text) {
        return text.trim().replace('|', '/');
    }

    // ------------------------------------------------------------------ show a page
    private void showPage(String name) {
        pageTitle.setText(name);
        if (name.equals("Reports")) {
            fillReports();
        } else if (name.equals("Rent / Return")) {
            fillRentReturnBoxes();
        } else if (name.equals("Rent Log File")) {
            logArea.setText(manager.getFiles().readLogText());
            logArea.setCaretPosition(logArea.getDocument().getLength());
        } else {
            refreshAll();
        }
        cardLayout.show(cards, name);
    }

    // ------------------------------------------------------------------ the ONE method that handles all clicks
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == navDashboard) {
            showPage("Dashboard");
        } else if (source == navVehicles) {
            showPage("Vehicles");
        } else if (source == navCustomers) {
            showPage("Customers");
        } else if (source == navRent) {
            showPage("Rent / Return");
        } else if (source == navRecords) {
            showPage("Rental Records");
        } else if (source == navReports) {
            showPage("Reports");
        } else if (source == navLog) {
            showPage("Rent Log File");
        } else if (source == addVehicleBtn) {
            addVehicle();
        } else if (source == addCustomerBtn) {
            addCustomer();
        } else if (source == refreshRecordsBtn) {
            refreshAll();
        } else if (source == checkBtn) {
            showAvailableVehicles();
        } else if (source == rentBtn) {
            rentVehicle();
        } else if (source == previewBtn) {
            previewFine();
        } else if (source == returnBtn) {
            returnVehicle();
        } else if (source == exportBtn) {
            exportReport();
        } else if (source == refreshLogBtn) {
            logArea.setText(manager.getFiles().readLogText());
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }

    // ------------------------------------------------------------------ add customer / vehicle
    private void addCustomer() {
        JTextField name = new JTextField();
        JTextField phone = new JTextField();
        JTextField license = new JTextField();
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Name"));
        form.add(name);
        form.add(new JLabel("Phone"));
        form.add(phone);
        form.add(new JLabel("License"));
        form.add(license);
        int choice = JOptionPane.showConfirmDialog(this, form, "Add Customer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            manager.addCustomer(clean(name.getText()), clean(phone.getText()), clean(license.getText()));
            refreshAll();
            showInfo("Customer added successfully.");
        } catch (InvalidDataException ex) {
            showError(ex.getMessage());
        } catch (IOException ex) {
            showError("File problem: " + ex.getMessage());
        }
    }

    private void addVehicle() {
        JComboBox<String> type = new JComboBox<String>(new String[]{"Car", "Bike", "Truck"});
        JTextField id = new JTextField();
        JTextField brand = new JTextField();
        JTextField model = new JTextField();
        JTextField rent = new JTextField();
        JTextField extra = new JTextField();
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Type"));
        form.add(type);
        form.add(new JLabel("Vehicle ID"));
        form.add(id);
        form.add(new JLabel("Brand"));
        form.add(brand);
        form.add(new JLabel("Model"));
        form.add(model);
        form.add(new JLabel("Rent / Day"));
        form.add(rent);
        form.add(new JLabel("Seats / CC / Tons"));
        form.add(extra);
        int choice = JOptionPane.showConfirmDialog(this, form, "Add Vehicle", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            String vid = clean(id.getText());
            String b = clean(brand.getText());
            String m = clean(model.getText());
            if (vid.isEmpty() || b.isEmpty() || m.isEmpty()) {
                throw new InvalidDataException("All fields are required.");
            }
            double r = Double.parseDouble(rent.getText().trim());
            String t = (String) type.getSelectedItem();
            Vehicle v;
            if (t.equals("Car")) {
                v = new Car(vid, b, m, r, Integer.parseInt(extra.getText().trim()));
            } else if (t.equals("Bike")) {
                v = new Bike(vid, b, m, r, Integer.parseInt(extra.getText().trim()));
            } else {
                v = new Truck(vid, b, m, r, Double.parseDouble(extra.getText().trim()));
            }
            manager.addVehicle(v);
            refreshAll();
            showInfo("Vehicle added successfully.");
        } catch (NumberFormatException ex) {
            showError("Rent and Seats / CC / Tons must be numbers.");
        } catch (InvalidDataException ex) {
            showError(ex.getMessage());
        } catch (IOException ex) {
            showError("File problem: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ rent
    private void fillRentReturnBoxes() {
        customerBox.removeAllItems();
        for (Customer c : manager.getCustomers()) {
            customerBox.addItem(c);
        }
        vehicleBox.removeAllItems();
        rentalBox.removeAllItems();
        for (Rental r : manager.getRentals()) {
            if (!r.isReturned()) {
                rentalBox.addItem(r);
            }
        }
        dueLabel.setText("-");
        retDueLabel.setText("-");
        retLateLabel.setText("-");
        retFineLabel.setText("-");
        retTotalLabel.setText("-");
        showAvailableVehicles();
    }

    private void showAvailableVehicles() {
        try {
            LocalDate start = RentalManager.parseDate(startField.getText());
            int days = Integer.parseInt(daysField.getText().trim());
            if (days < 1) {
                throw new InvalidDataException("Rental days must be at least 1.");
            }
            dueLabel.setText(start.plusDays(days).toString());
            vehicleBox.removeAllItems();
            for (Vehicle v : manager.getFreeVehicles(start, days)) {
                vehicleBox.addItem(v);
            }
        } catch (NumberFormatException ex) {
            showError("Rental days must be a whole number.");
        } catch (InvalidDataException ex) {
            showError(ex.getMessage());
        }
    }

    private void rentVehicle() {
        Customer c = (Customer) customerBox.getSelectedItem();
        Vehicle v = (Vehicle) vehicleBox.getSelectedItem();
        if (c == null || v == null) {
            showError("Select a customer and an available vehicle.\n(Add a customer first if the list is empty.)");
            return;
        }
        try {
            LocalDate start = RentalManager.parseDate(startField.getText());
            int days = Integer.parseInt(daysField.getText().trim());
            Rental r = manager.rentVehicle(c, v, days, start);
            refreshAll();
            fillRentReturnBoxes();
            showInfo("Vehicle rented successfully.\nRental ID: " + r.getRentalId()
                    + "\nFrom: " + r.getStartDate() + "   To: " + r.getDueDate()
                    + "\nRent: Rs. " + String.format("%.2f", r.getBaseAmount())
                    + "\n\nIf returned after " + r.getDueDate() + " a fine will be added.");
        } catch (NumberFormatException ex) {
            showError("Rental days must be a whole number.");
        } catch (VehicleNotAvailableException ex) {
            showError(ex.getMessage());
            showAvailableVehicles();
        } catch (InvalidDataException ex) {
            showError(ex.getMessage());
        } catch (IOException ex) {
            showError("File problem: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ return
    private void previewFine() {
        Rental r = (Rental) rentalBox.getSelectedItem();
        if (r == null) {
            showError("No active rental selected.");
            return;
        }
        try {
            LocalDate date = RentalManager.parseDate(returnField.getText());
            int late = r.getLateDays(date);
            double fine = r.getFine(date);
            retDueLabel.setText(r.getDueDate().toString());
            retLateLabel.setText(late + " day(s)");
            retFineLabel.setText(String.format("Rs. %.2f", fine));
            retTotalLabel.setText(String.format("Rs. %.2f", r.getTotalAmount(date)));
            if (late > 0) {
                retFineLabel.setForeground(RED);
            } else {
                retFineLabel.setForeground(TEXT);
            }
        } catch (InvalidDataException ex) {
            showError(ex.getMessage());
        }
    }

    private void returnVehicle() {
        Rental r = (Rental) rentalBox.getSelectedItem();
        if (r == null) {
            showError("No active rental selected.");
            return;
        }
        try {
            LocalDate date = RentalManager.parseDate(returnField.getText());
            manager.returnVehicle(r, date);
            int late = r.getLateDays(date);
            refreshAll();
            fillRentReturnBoxes();
            if (late > 0) {
                showInfo("Vehicle returned LATE by " + late + " day(s).\nRent: Rs. " + String.format("%.2f", r.getBaseAmount())
                        + "\nFine: Rs. " + String.format("%.2f", r.getFine(date))
                        + "\nTotal payable: Rs. " + String.format("%.2f", r.getTotalAmount(date)));
            } else {
                showInfo("Vehicle returned on time.\nTotal: Rs. " + String.format("%.2f", r.getTotalAmount(date)));
            }
        } catch (InvalidDataException ex) {
            showError(ex.getMessage());
        } catch (IOException ex) {
            showError("File problem: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ reports
    private void fillReports() {
        ArrayList<Vehicle> vehicles = manager.getVehicles();
        ArrayList<Rental> rentals = manager.getRentals();
        ArrayList<Vehicle> never = ReportGenerator.neverRented(vehicles, rentals);
        ArrayList<ReportGenerator.VehicleStat> stats = ReportGenerator.frequency(vehicles, rentals);

        neverModel.setRowCount(0);
        for (Vehicle v : never) {
            neverModel.addRow(new Object[]{v.getVehicleId(), v.getType(), v.getBrand(), v.getModel(),
                    String.format("Rs. %.2f", v.getRentPerDay())});
        }
        freqModel.setRowCount(0);
        int rank = 1;
        for (ReportGenerator.VehicleStat s : stats) {
            freqModel.addRow(new Object[]{"" + rank, s.vehicle.getVehicleId(), s.vehicle.getBrand() + " " + s.vehicle.getModel(),
                    "" + s.timesRented, "" + s.totalDays, String.format("Rs. %.2f", s.income), s.category});
            rank++;
        }

        double rent = 0;
        double fines = 0;
        for (Rental r : rentals) {
            rent += r.getBaseAmount();
            fines += r.getFine();
        }
        String top = "-";
        if (!stats.isEmpty() && stats.get(0).timesRented > 0) {
            top = stats.get(0).vehicle.getBrand() + " " + stats.get(0).vehicle.getModel() + " (" + stats.get(0).timesRented + " times)";
        }
        reportSummary.setText("<html><b>Never rented:</b> " + never.size() + " of " + vehicles.size()
                + " &nbsp;|&nbsp; <b>Most rented:</b> " + top
                + " &nbsp;|&nbsp; <b>Rent:</b> Rs. " + String.format("%.2f", rent)
                + " &nbsp;|&nbsp; <b>Fines:</b> Rs. " + String.format("%.2f", fines) + "</html>");
    }

    private void exportReport() {
        try {
            String text = ReportGenerator.buildTextReport(manager.getVehicles(), manager.getRentals());
            ArrayList<String> lines = new ArrayList<String>();
            lines.add(text);
            manager.getFiles().writeLines(FileManager.REPORT_FILE, lines);
            manager.getFiles().log("REPORT", "Analysis report saved in " + FileManager.REPORT_FILE);
            showInfo("Report saved in:\n" + manager.getFiles().getFolderPath() + "/" + FileManager.REPORT_FILE);
        } catch (IOException ex) {
            showError("Could not save report: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ refresh
    // called by the OverdueMonitor thread (through the GUI thread) and by refreshAll()
    public void refreshCounts() {
        lblVehicles.setText("" + manager.getVehicles().size());
        lblAvailable.setText("" + manager.countAvailable());
        lblCustomers.setText("" + manager.getCustomers().size());
        lblRentals.setText("" + manager.getRentals().size());
        int overdue = manager.countOverdue();
        lblOverdue.setText("" + overdue);
        if (overdue > 0) {
            banner.setText("!  " + overdue + " vehicle(s) overdue");
            banner.setForeground(RED);
        } else {
            banner.setText("No overdue vehicles");
            banner.setForeground(GREEN);
        }
    }

    private void refreshAll() {
        LocalDate today = LocalDate.now();

        vehicleModel.setRowCount(0);
        for (Vehicle v : manager.getVehicles()) {
            vehicleModel.addRow(new Object[]{v.getVehicleId(), v.getType(), v.getBrand(), v.getModel(),
                    String.format("Rs. %.2f", v.getRentPerDay()), manager.getVehicleStatus(v)});
        }
        customerModel.setRowCount(0);
        for (Customer c : manager.getCustomers()) {
            customerModel.addRow(new Object[]{"" + c.getCustomerId(), c.getName(), c.getPhone(), c.getLicenseNumber()});
        }
        rentalModel.setRowCount(0);
        for (Rental r : manager.getRentals()) {
            String returned = "-";
            if (r.isReturned()) {
                returned = r.getReturnDate().toString();
            }
            rentalModel.addRow(new Object[]{"" + r.getRentalId(), r.getCustomer().getName(), r.getVehicle().toString(),
                    r.getStartDate().toString(), r.getDueDate().toString(), returned, "" + r.getDays(),
                    String.format("Rs. %.2f", r.getBaseAmount()), String.format("Rs. %.2f", r.getFine()),
                    String.format("Rs. %.2f", r.getTotalAmount(today)), r.getStatus(today)});
        }
        refreshCounts();
    }

    // ------------------------------------------------------------------ main
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // if it fails, the default look is used
        }
        VehicleRentalGUI gui = new VehicleRentalGUI();
        gui.setVisible(true);
        gui.showOverdueAlert();
    }
}
