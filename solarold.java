import java.util.*;
import java.io.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.fusesource.jansi.AnsiConsole;

class Appliance {
    String name;
    double wattage;
    double hoursPerDay;

    public Appliance(String name, double wattage, double hoursPerDay) {
        this.name = name;
        this.wattage = wattage;
        this.hoursPerDay = hoursPerDay;
    }

    public double getDailyConsumption() {
        return wattage * hoursPerDay;
    }
}

class SolarCalculator {
    List<Appliance> appliances;
    double sunHours, systemVoltage, panelWatt, daysOfAutonomy, dod, inverterEfficiency;

    public SolarCalculator(List<Appliance> appliances) {
        this.appliances = appliances;
    }

    public double getTotalDailyEnergy() {
        return appliances.stream().mapToDouble(Appliance::getDailyConsumption).sum();
    }

    public void computeSystem(double sunHours, double systemVoltage, double panelWatt,
                              double daysOfAutonomy, double dod, double inverterEfficiency) {
        this.sunHours = sunHours;
        this.systemVoltage = systemVoltage;
        this.panelWatt = panelWatt;
        this.daysOfAutonomy = daysOfAutonomy;
        this.dod = dod;
        this.inverterEfficiency = inverterEfficiency;
    }

    public void displayReport() {
        double totalDailyEnergy = getTotalDailyEnergy();
        double totalSolarPower = totalDailyEnergy / sunHours;
        int numberOfPanels = (int) Math.ceil(totalSolarPower / panelWatt);
        double batteryCapacityWh = (totalDailyEnergy * daysOfAutonomy) / (dod * inverterEfficiency);
        double batteryCapacityAh = batteryCapacityWh / systemVoltage;
        double inverterSize = totalDailyEnergy / inverterEfficiency;
        double chargeControllerCurrent = (panelWatt * numberOfPanels) / systemVoltage * 1.25;

        final String GREEN = "\u001B[32m";
        final String CYAN = "\u001B[36m";
        final String RESET = "\u001B[0m";

        System.out.println(CYAN + "\n+======================================================+");
        System.out.println("|               SOLAR POWER SYSTEM REPORT              |");
        System.out.println("+======================================================+" + RESET);
        System.out.printf("| %-30s | %15.2f %-4s |\n", "Total Daily Load", totalDailyEnergy, "Wh");
        System.out.printf("| %-30s | %15.2f %-4s |\n", "Total Solar Power Needed", totalSolarPower, "W");
        System.out.printf("| %-30s | %15d %-4s |\n", "Number of Panels Required", numberOfPanels, "pcs");
        System.out.printf("| %-30s | %15.2f %-4s |\n", "Battery Capacity Required", batteryCapacityAh, "Ah");
        System.out.printf("| %-30s | %15.2f %-4s |\n", "System Voltage", systemVoltage, "V");
        System.out.printf("| %-30s | %15.2f %-4s |\n", "Inverter Size Recommended", inverterSize, "W");
        System.out.printf("| %-30s | %15.2f %-4s |\n", "Charge Controller", chargeControllerCurrent, "A");
        System.out.println("+======================================================+");
        System.out.println(GREEN + "NOTE: Values are approximate; consider 10–20%% safety margin." + RESET);
    }
}

class JsonStorage {
    private static final String FILE_NAME = "appliances.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static List<Appliance> loadAppliances() {
        try (FileReader reader = new FileReader(FILE_NAME)) {
            return gson.fromJson(reader, new TypeToken<List<Appliance>>() {}.getType());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static void saveAppliances(List<Appliance> appliances) {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            gson.toJson(appliances, writer);
            System.out.println("\u001B[32m✔ Appliances saved successfully to " + FILE_NAME + "\u001B[0m");
        } catch (IOException e) {
            System.out.println("⚠ Error saving appliances: " + e.getMessage());
        }
    }
}

public class solar {

    // ✅ Jansi-safe wrappers must be inside the class
    public static void enableJansiSafe() {
        try {
            Class<?> ansiClass = Class.forName("org.fusesource.jansi.AnsiConsole");
            ansiClass.getMethod("systemInstall").invoke(null);
            System.out.println("✔ Jansi color support enabled.");
        } catch (Exception e) {
            System.out.println("⚠ Jansi not found, running without color support.");
        }
    }

    public static void disableJansiSafe() {
        try {
            Class<?> ansiClass = Class.forName("org.fusesource.jansi.AnsiConsole");
            ansiClass.getMethod("systemUninstall").invoke(null);
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        enableJansiSafe();

        Scanner sc = new Scanner(System.in);
        List<Appliance> appliances = new ArrayList<>();

        final String YELLOW = "\u001B[33m";
        final String GREEN = "\u001B[32m";
        final String CYAN = "\u001B[36m";
        final String RESET = "\u001B[0m";

        System.out.println(CYAN + "=== Solar Power System Calculator (Enhanced JSON + Safe Input) ===" + RESET);
        System.out.println("[1] New Calculation");
        System.out.println("[2] Load Appliances from JSON");
        System.out.print("Choose option: ");
        int option = sc.nextInt();
        sc.nextLine();

        if (option == 2) {
            appliances = JsonStorage.loadAppliances();
            if (appliances.isEmpty()) {
                System.out.println(YELLOW + "⚠ No saved data found, starting fresh.\n" + RESET);
            } else {
                System.out.println(GREEN + "✔ Loaded existing appliance data.\n" + RESET);
            }
        }

        System.out.println(YELLOW + "Enter appliances (type 'done' to finish or 'exit' to quit):" + RESET);
        while (true) {
            System.out.print("Appliance name: ");
            String name = sc.nextLine().trim();
            if (name.equalsIgnoreCase("done")) break;
            if (name.equalsIgnoreCase("exit")) {
                System.out.println(YELLOW + "Exiting safely... Goodbye!" + RESET);
                sc.close();
                return;
            }

            double watt = inputPositiveDouble(sc, "Wattage (W): ");
            double hrs = inputPositiveDouble(sc, "Hours per day (0–24): ");
            appliances.add(new Appliance(name, watt, hrs));
        }

        System.out.print("\nSave and merge with existing appliances? (y/n): ");
        if (sc.nextLine().equalsIgnoreCase("y")) {
            List<Appliance> existing = JsonStorage.loadAppliances();
            existing.addAll(appliances);
            JsonStorage.saveAppliances(existing);
            appliances = existing;
        }

        System.out.println(CYAN + "\n+---------------------------------------------------------------------------------+");
        System.out.println("|                    APPLIANCE LIST                   |");
        System.out.println("+----------------------------------------------------------------------------+" + RESET);
        for (Appliance a : appliances) {
            System.out.printf("| %-20s | %6.1f W | %5.1f hrs/day | %8.1f Wh/day |\n",
                    a.name, a.wattage, a.hoursPerDay, a.getDailyConsumption());
        }
        System.out.println("+------------------------------------------------------+");

        System.out.println("\nEnter your system parameters below:");
        double sunHours = inputPositiveDouble(sc, "Average sun hours/day: ");
        double systemVoltage = inputPositiveDouble(sc, "System voltage (12/24/48): ");
        double panelWatt = inputPositiveDouble(sc, "Panel wattage (W): ");
        double days = inputPositiveDouble(sc, "Days of autonomy: ");
        double dod = inputPositiveDouble(sc, "Depth of discharge (0.1–1.0): ");
        double invEff = inputPositiveDouble(sc, "Inverter efficiency (0.7–1.0): ");

        SolarCalculator calc = new SolarCalculator(appliances);
        calc.computeSystem(sunHours, systemVoltage, panelWatt, days, dod, invEff);
        calc.displayReport();

        System.out.println(GREEN + "\nProgram finished successfully. Press Enter to exit..." + RESET);
        sc.nextLine();
        disableJansiSafe();
        sc.close();
    }

    private static double inputPositiveDouble(Scanner sc, String prompt) {
        double value;
        while (true) {
            System.out.print(prompt);
            try {
                value = Double.parseDouble(sc.nextLine());
                if (value > 0) return value;
                System.out.println("⚠ Please enter a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("⚠ Invalid number, try again.");
            }
        }
    }
}