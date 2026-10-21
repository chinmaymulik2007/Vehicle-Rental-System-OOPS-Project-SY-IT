import java.io.IOException;

/**
 * MULTITHREADING (way 2: implements Runnable)
 * Second background thread: every 10 seconds it saves the data files if something changed.
 */
public class AutoSaveTask implements Runnable {
    private RentalManager manager;
    private volatile boolean running = true;

    public AutoSaveTask(RentalManager manager) {
        this.manager = manager;
    }

    public void stopTask() {
        running = false;
    }

    public void run() {
        while (running) {
            try {
                Thread.sleep(10000);
                if (manager.isDirty()) {
                    manager.saveAll();
                    manager.getFiles().log("AUTOSAVE", "Data files saved by background thread");
                }
            } catch (InterruptedException e) {
                running = false;
            } catch (IOException e) {
                System.out.println("Auto save failed: " + e.getMessage());
            }
        }
    }
}
