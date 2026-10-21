import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * FILE HANDLING - reads and writes all text files. The files are created in the
 * folder from which the program is run.
 *   rental_log.txt   : log of every rent / return / add event (new lines are added at the end)
 *   vehicles.txt, customers.txt, rentals.txt : saved data, loaded again when the program starts
 *   report_analysis.txt : exported report
 * METHOD OVERLOADING - there are two log() methods.
 */
public class FileManager {
    public static final String LOG_FILE = "rental_log.txt";
    public static final String VEHICLES_FILE = "vehicles.txt";
    public static final String CUSTOMERS_FILE = "customers.txt";
    public static final String RENTALS_FILE = "rentals.txt";
    public static final String REPORT_FILE = "report_analysis.txt";

    public String getFolderPath() {
        return System.getProperty("user.dir");
    }

    // log(event) - overloaded version 1
    public synchronized void log(String event) throws IOException {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = LocalDateTime.now().format(format);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(LOG_FILE, true));   // true = add at the end
            writer.write("[" + time + "] " + event);
            writer.newLine();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    // log(type, details) - overloaded version 2
    public synchronized void log(String type, String details) throws IOException {
        log(type + " | " + details);
    }

    // write many lines to a file (old content is replaced)
    public synchronized void writeLines(String fileName, ArrayList<String> lines) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    // read all lines of a file (empty list if the file does not exist yet)
    public synchronized ArrayList<String> readLines(String fileName) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        File file = new File(fileName);
        if (!file.exists()) {
            return lines;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
                line = reader.readLine();
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return lines;
    }

    // whole log file as one text (for the "Rent Log File" page)
    public String readLogText() {
        try {
            ArrayList<String> lines = readLines(LOG_FILE);
            if (lines.isEmpty()) {
                return "(Log is empty - it is filled when the first rent / return happens.)";
            }
            StringBuilder text = new StringBuilder();
            for (String line : lines) {
                text.append(line).append("\n");
            }
            return text.toString();
        } catch (IOException e) {
            return "Could not read log file: " + e.getMessage();
        }
    }
}
