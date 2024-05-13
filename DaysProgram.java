import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DaysProgram {
    private static final String CSV_FILE = "events.csv";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java DaysProgram <command> [options]");
            return;
        }

        String command = args[0];
        switch (command) {
            case "list":
                if (args.length >= 3 && args[1].equals("--before-date")) {
                    listEventsBeforeDate(args[2]);
                } else if (args.length >= 3 && args[1].equals("--after-date")) {
                    listEventsAfterDate(args[2]);
                } else {
                    listEvents();
                }
                break;
            case "add":
                addEvent(args);
                break;
            case "delete":
                deleteEvent(args);
                break;
            default:
                System.out.println("Unknown command.");
        }
    }

    private static void listEvents() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            // Print each event
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
    }

    private static void listEventsBeforeDate(String date) {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            // Print each event before the specified date
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0 && isBeforeDate(parts[0], date)) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
    }

    private static void listEventsAfterDate(String date) {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            // Print each event after the specified date
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0 && isAfterDate(parts[0], date)) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
    }

    private static void addEvent(String[] args) {
        String date = null;
        String category = null;
        String description = null;

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--date":
                    if (i < args.length - 1) {
                        date = args[i + 1];
                    }
                    break;
                case "--category":
                    if (i < args.length - 1) {
                        category = args[i + 1];
                    }
                    break;
                case "--description":
                    if (i < args.length - 1) {
                        description = args[i + 1];
                    }
                    break;
            }
        }

        if (date != null && category != null && description != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE, true))) {
                writer.write(date + "," + category + "," + description + "\n");
                System.out.println("Event added successfully.");
            } catch (IOException e) {
                System.err.println("Error writing to CSV file: " + e.getMessage());
            }
        } else {
            System.out.println("Error: Missing arguments.");
        }
    }

    private static void deleteEvent(String[] args) {
        if (args.length == 1) {
            System.out.println("Error: No deletion criteria specified.");
            return;
        }
    
        boolean deleteAll = false;
        for (String arg : args) {
            if (arg.equals("--all")) {
                deleteAll = true;
                break;
            }
        }
    
        if (deleteAll) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE))) {
                writer.write("");
                System.out.println("Deletion successful.");
            } catch (IOException e) {
                System.err.println("Error clearing CSV file: " + e.getMessage());
            }
            return;
        }
    
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE));
             BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE + ".tmp"))) {
    
            String line;
            while ((line = reader.readLine()) != null) {
                boolean delete = false;
                for (int i = 1; i < args.length; i++) {
                    switch (args[i]) {
                        case "--date":
                            if (i < args.length - 1 && line.startsWith(args[i + 1] + ",")) {
                                delete = true;
                            }
                            break;
                        case "--category":
                            if (i < args.length - 1 && line.contains("," + args[i + 1] + ",")) {
                                delete = true;
                            }
                            break;
                        case "--description":
                            if (i < args.length - 1 && line.endsWith("," + args[i + 1])) {
                                delete = true;
                            }
                            break;
                    }
                }
                if (!delete) {
                    writer.write(line + "\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing CSV file: " + e.getMessage());
            return;
        }
    
        try {
            java.nio.file.Files.move(java.nio.file.Paths.get(CSV_FILE + ".tmp"),
                    java.nio.file.Paths.get(CSV_FILE), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Deletion successful.");
        } catch (IOException e) {
            System.err.println("Error replacing CSV file: " + e.getMessage());
        }
    }

    private static boolean isBeforeDate(String date1, String date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/dd/MM");
        try {
            Date d1 = sdf.parse(date1);
            Date d2 = sdf.parse(date2);
            return d1.before(d2);
        } catch (ParseException e) {
            return false;
        }
    }

    private static boolean isAfterDate(String date1, String date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/dd/MM");
        try {
            Date d1 = sdf.parse(date1);
            Date d2 = sdf.parse(date2);
            return d1.after(d2);
        } catch (ParseException e) {
            return false;
        }
    }
}
