import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** INHERITANCE + METHOD OVERRIDING - runs when the window is closed: stops threads and saves data. */
public class ExitHandler extends WindowAdapter {
    private VehicleRentalGUI gui;

    public ExitHandler(VehicleRentalGUI gui) {
        this.gui = gui;
    }

    public void windowClosing(WindowEvent e) {
        gui.saveAndExit();
    }
}
