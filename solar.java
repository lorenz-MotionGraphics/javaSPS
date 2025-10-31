import java.util.*;
import java.io.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

// =============================================================
// == CLASS 1: Appliance.java
// =============================================================
class Appliance {
    String name;
    double wattage;
    int quantity;
    double hoursPerDay;

    public Appliance(String name, double wattage, int quantity, double hoursPerDay) {
        this.name = name;
        this.wattage = wattage;
        this.quantity = quantity;
        this.hoursPerDay = hoursPerDay;
    }

    public double getDailyConsumption() {
        return wattage * quantity * hoursPerDay;
    }
}

// =============================================================
// == CLASS 2: SolarCalculator.java
// =============================================================
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

        UI.printHeader("SOLAR SYSTEM CALCULATION RESULTS");

        System.out.println("\n+-------------------------------------+----------------------+");
        System.out.println("| ENERGY REQUIREMENTS                 |                      |");
        System.out.println("+-------------------------------------+----------------------+");
        printTableRow("Total Daily Load", String.format("%.2f Wh", totalDailyEnergy));
        printTableRow("Required Solar Power", String.format("%.2f W", totalSolarPower));

        System.out.println("+-------------------------------------+----------------------+");
        System.out.println("| SOLAR PANELS                        |                      |");
        System.out.println("+-------------------------------------+----------------------+");
        printTableRow("Number of Panels", String.format("%d panels", numberOfPanels));
        printTableRow("Panel Rating", String.format("%.0f W each", panelWatt));
        printTableRow("Total Array Power", String.format("%.0f W", panelWatt * numberOfPanels));

        System.out.println("+-------------------------------------+----------------------+");
        System.out.println("| BATTERY SYSTEM                      |                      |");
        System.out.println("+-------------------------------------+----------------------+");
        printTableRow("Battery Capacity", String.format("%.2f Ah", batteryCapacityAh));
        printTableRow("System Voltage", String.format("%.0f V", systemVoltage));
        printTableRow("Total Energy Storage", String.format("%.2f Wh", batteryCapacityWh));
        printTableRow("Days of Autonomy", String.format("%.1f days", daysOfAutonomy));

        System.out.println("+-------------------------------------+----------------------+");
        System.out.println("| POWER ELECTRONICS                   |                      |");
        System.out.println("+-------------------------------------+----------------------+");
        printTableRow("Inverter Size", String.format("%.2f W", inverterSize));
        printTableRow("Charge Controller", String.format("%.2f A", chargeControllerCurrent));
        System.out.println("+-------------------------------------+----------------------+");

        UI.printWarning("\nWARNING: Add 10-20% safety margin to all values for real installations");
        UI.printInfo("INFO: Consult a certified solar installer for final system design");
    }

    private void printTableRow(String label, String value) {
        System.out.printf("| %-35s | %20s |\n", label, value);
    }
}

// =============================================================
// == CLASS 3: JsonStorage.java
// =============================================================
class JsonStorage {
    private static final String FILE_NAME = "appliances.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static List<Appliance> loadAppliances() {
        try (FileReader reader = new FileReader(FILE_NAME)) {
            List<Appliance> loaded = gson.fromJson(reader, new TypeToken<List<Appliance>>() {}.getType());
            return loaded != null ? loaded : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static void saveAppliances(List<Appliance> appliances) {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            gson.toJson(appliances, writer);
            UI.printSuccess("Saved " + appliances.size() + " appliances to " + FILE_NAME);
        } catch (IOException e) {
            UI.printError("Error saving appliances: " + e.getMessage());
        }
    }
}

// =============================================================
// == CLASS 4: UI.java
// =============================================================
class UI {
    public static void printBanner() {
        System.out.println("\n+==============================================================+");
        System.out.println("|                                                              |");
        System.out.println("|        SOLAR POWER SYSTEM CALCULATOR                         |");
        System.out.println("|                                                              |");
        System.out.println("|        Design Your Off-Grid Energy Solution                  |");
        System.out.println("|                                                              |");
        System.out.println("+==============================================================+");
    }

    public static void printHeader(String title) {
        int totalWidth = 60;
        int padding = (totalWidth - title.length() - 2) / 2;
        String pad = "=".repeat(padding);
        System.out.println("\n" + "+" + pad + " " + title + " " + pad + "+");
    }

    public static void printMenu(String title, String[] options) {
        System.out.println("\n+--- " + title + " ---+");
        for (int i = 0; i < options.length; i++) {
            System.out.println("| [" + (i + 1) + "] " + options[i]);
        }
        System.out.println("+----------------------------------------+");
    }

    public static void printSuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    public static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    public static void printWarning(String message) {
        System.out.println(message);
    }

    public static void printInfo(String message) {
        System.out.println(message);
    }

    public static void printDivider() {
        System.out.println("----------------------------------------------------------------");
    }
}

// =============================================================
// == CLASS 5: solar (main)
// =============================================================
public class solar {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Appliance> appliances = new ArrayList<>();

        try {
            UI.printBanner();

            String[] menuOptions = {
                "Start New Calculation",
                "Load Saved Appliances",
                "Exit Program"
            };

            UI.printMenu("MAIN MENU", menuOptions);
            System.out.print("> Select option: ");

            int option = getIntInput(sc, 1, 3);

            if (option == 3) {
                UI.printInfo("\nThank you for using Solar Calculator. Goodbye!");
                return;
            }

            if (option == 2) {
                appliances = JsonStorage.loadAppliances();
                if (appliances.isEmpty()) {
                    UI.printWarning("\n[WARNING] No saved data found. Starting fresh...");
                } else {
                    UI.printSuccess("\nLoaded " + appliances.size() + " appliances from file");
                }
            }

            UI.printHeader("APPLIANCE CONFIGURATION");
            UI.printInfo("\nEnter your appliances one by one (type 'done' when finished)\n");

            int count = 1;
            while (true) {
                System.out.println("--- Appliance #" + count + " ---");
                System.out.print("> Name (or 'done' to finish): ");
                String name = sc.nextLine().trim();

                if (name.equalsIgnoreCase("done")) break;
                if (name.isEmpty()) {
                    UI.printWarning("[WARNING] Name cannot be empty");
                    continue;
                }

                double watt = inputPositiveDouble(sc, "> Wattage (W): ");
                int qty = inputPositiveInt(sc, "> Quantity: ");
                double hrs = inputRangeDouble(sc, "> Hours per day: ", 0, 24);

                appliances.add(new Appliance(name, watt, qty, hrs));
                UI.printSuccess("Added: " + qty + " x " + name + " (" + watt + "W x " + hrs + "h = " + (watt * qty * hrs) + "Wh/day)");
                System.out.println();
                count++;
            }

            if (!appliances.isEmpty()) {
                System.out.print("\nSave appliances to file? (y/n): ");
                if (sc.nextLine().trim().equalsIgnoreCase("y")) {
                    List<Appliance> existing = JsonStorage.loadAppliances();
                    existing.addAll(appliances);
                    JsonStorage.saveAppliances(existing);
                    appliances = existing;
                }
            }

            displayApplianceTable(appliances);

            UI.printHeader("SYSTEM PARAMETERS");
            System.out.println();

            double sunHours = inputRangeDouble(sc, "> Average sun hours per day (3-8): ", 1, 12);
            double systemVoltage = inputChoice(sc, "> System voltage: ", new double[]{12, 24, 48});
            double panelWatt = inputPositiveDouble(sc, "> Solar panel wattage (W): ");
            double days = inputRangeDouble(sc, "> Days of autonomy (1-5): ", 1, 10);
            double dod = inputRangeDouble(sc, "> Depth of discharge (0.5-0.8 recommended): ", 0.1, 1.0);
            double invEff = inputRangeDouble(sc, "> Inverter efficiency (0.85-0.95 typical): ", 0.5, 1.0);

            SolarCalculator calc = new SolarCalculator(appliances);
            calc.computeSystem(sunHours, systemVoltage, panelWatt, days, dod, invEff);
            calc.displayReport();

            System.out.println("\n================================================================");
            System.out.println("  Calculation complete! Press Enter to exit...");
            sc.nextLine();

        } finally {
            sc.close();
        }
    }

    private static void displayApplianceTable(List<Appliance> appliances) {
        if (appliances.isEmpty()) {
            UI.printWarning("\n[WARNING] No appliances configured");
            return;
        }

        UI.printHeader("APPLIANCE SUMMARY");
        System.out.println("\n+----+-------------------------+----------+----------+-----------+-------------+");
        System.out.println("|  # | Appliance               | Wattage  | Quantity | Hours/Day | Daily (Wh)  |");
        System.out.println("+----+-------------------------+----------+----------+-----------+-------------+");

        int idx = 1;
        double totalDaily = 0;
        for (Appliance a : appliances) {
            double daily = a.getDailyConsumption();
            totalDaily += daily;
            System.out.printf("| %2d | %-23s | %6.0f W | %8d | %7.1f h | %9.1f Wh |\n",
                    idx++, truncate(a.name, 23), a.wattage, a.quantity, a.hoursPerDay, daily);
        }

        System.out.println("+----+-------------------------+----------+----------+-----------+-------------+");
        System.out.printf("| %-58s | %9.1f Wh |\n", "TOTAL DAILY ENERGY CONSUMPTION", totalDaily);
        System.out.println("+------------------------------------------------------------+-------------+");
    }

    private static String truncate(String str, int maxLen) {
        return str.length() > maxLen ? str.substring(0, maxLen - 3) + "..." : str;
    }

    private static int getIntInput(Scanner sc, int min, int max) {
        while (true) {
            try {
                int value = Integer.parseInt(sc.nextLine().trim());
                if (value >= min && value <= max) return value;
                UI.printWarning("[WARNING] Please enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                UI.printWarning("[WARNING] Invalid input. Please enter a number.");
            }
        }
    }

    private static int inputPositiveInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(sc.nextLine().trim());
                if (value > 0) return value;
                UI.printWarning("[WARNING] Please enter a positive number");
            } catch (NumberFormatException e) {
                UI.printWarning("[WARNING] Invalid number format");
            }
        }
    }

    private static double inputPositiveDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(sc.nextLine().trim());
                if (value > 0) return value;
                UI.printWarning("[WARNING] Please enter a positive number");
            } catch (NumberFormatException e) {
                UI.printWarning("[WARNING] Invalid number format");
            }
        }
    }

    private static double inputRangeDouble(Scanner sc, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(sc.nextLine().trim());
                if (value >= min && value <= max) return value;
                UI.printWarning("[WARNING] Please enter a value between " + min + " and " + max);
            } catch (NumberFormatException e) {
                UI.printWarning("[WARNING] Invalid number format");
            }
        }
    }

    private static double inputChoice(Scanner sc, String prompt, double[] choices) {
        System.out.print(prompt);
        for (int i = 0; i < choices.length; i++) {
            System.out.print("[" + (i + 1) + "] " + choices[i] + "V");
            if (i < choices.length - 1) System.out.print("  ");
        }
        System.out.println();
        System.out.print("> Select: ");

        int choice = getIntInput(sc, 1, choices.length);
        return choices[choice - 1];
    }
}
