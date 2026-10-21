import javax.swing.SwingUtilities;

/**
 * MULTITHREADING (way 1: extends Thread)
 * Runs in the background and every 5 seconds asks the GUI to update the overdue counter,
 * so a vehicle becomes "Overdue" on its own when the due date passes.
 */
public class OverdueMonitor extends Thread {
    private VehicleRentalGUI gui;
    private volatile boolean running = true;

    public OverdueMonitor(VehicleRentalGUI gui) {
        this.gui = gui;
        setDaemon(true);
    }

    public void stopMonitor() {
        running = false;
        interrupt();
    }

    // this method is the thread's work - it starts when start() is called
    public void run() {
        while (running) {
            // Swing components must be changed from the GUI thread, so we give it a Runnable
            SwingUtilities.invokeLater(new CounterUpdater(gui));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    // small Runnable that runs on the GUI thread
    private static class CounterUpdater implements Runnable {
        private VehicleRentalGUI gui;

        CounterUpdater(VehicleRentalGUI gui) {
            this.gui = gui;
        }

        public void run() {
            gui.refreshCounts();
        }
    }
}
