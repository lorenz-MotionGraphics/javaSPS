import java.util.*;
import java.io.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

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

        UI.printHeader("SOLAR SYSTEM CALCULATION RESULTS");
        
        System.out.println(UI.CYAN + "\n┌─────────────────────────────────────┬──────────────────────┐" + UI.RESET);
        System.out.println(UI.CYAN + "│" + UI.BRIGHT_WHITE + " ENERGY REQUIREMENTS                 " + UI.CYAN + "│" + UI.BRIGHT_WHITE + "                      " + UI.CYAN + "│" + UI.RESET);
        System.out.println(UI.CYAN + "├─────────────────────────────────────┼──────────────────────┤" + UI.RESET);
        printTableRow("Total Daily Load", String.format("%.2f Wh", totalDailyEnergy), UI.YELLOW);
        printTableRow("Required Solar Power", String.format("%.2f W", totalSolarPower), UI.YELLOW);
        
        System.out.println(UI.CYAN + "├─────────────────────────────────────┼──────────────────────┤" + UI.RESET);
        System.out.println(UI.CYAN + "│" + UI.BRIGHT_WHITE + " SOLAR PANELS                        " + UI.CYAN + "│" + UI.BRIGHT_WHITE + "                      " + UI.CYAN + "│" + UI.RESET);
        System.out.println(UI.CYAN + "├─────────────────────────────────────┼──────────────────────┤" + UI.RESET);
        printTableRow("Number of Panels", String.format("%d panels", numberOfPanels), UI.GREEN);
        printTableRow("Panel Rating", String.format("%.0f W each", panelWatt), UI.GREEN);
        printTableRow("Total Array Power", String.format("%.0f W", panelWatt * numberOfPanels), UI.GREEN);
        
        System.out.println(UI.CYAN + "├─────────────────────────────────────┼──────────────────────┤" + UI.RESET);
        System.out.println(UI.CYAN + "│" + UI.BRIGHT_WHITE + " BATTERY SYSTEM                      " + UI.CYAN + "│" + UI.BRIGHT_WHITE + "                      " + UI.CYAN + "│" + UI.RESET);
        System.out.println(UI.CYAN + "├─────────────────────────────────────┼──────────────────────┤" + UI.RESET);
        printTableRow("Battery Capacity", String.format("%.2f Ah", batteryCapacityAh), UI.MAGENTA);
        printTableRow("System Voltage", String.format("%.0f V", systemVoltage), UI.MAGENTA);
        printTableRow("Total Energy Storage", String.format("%.2f Wh", batteryCapacityWh), UI.MAGENTA);
        printTableRow("Days of Autonomy", String.format("%.1f days", daysOfAutonomy), UI.MAGENTA);
        
        System.out.println(UI.CYAN + "├─────────────────────────────────────┼──────────────────────┤" + UI.RESET);
        System.out.println(UI.CYAN + "│" + UI.BRIGHT_WHITE + " POWER ELECTRONICS                   " + UI.CYAN + "│" + UI.BRIGHT_WHITE + "                      " + UI.CYAN + "│" + UI.RESET);
        System.out.println(UI.CYAN + "├─────────────────────────────────────┼──────────────────────┤" + UI.RESET);
        printTableRow("Inverter Size", String.format("%.2f W", inverterSize), UI.BLUE);
        printTableRow("Charge Controller", String.format("%.2f A", chargeControllerCurrent), UI.BLUE);
        
        System.out.println(UI.CYAN + "└─────────────────────────────────────┴──────────────────────┘" + UI.RESET);
        
        UI.printWarning("\n Add 10-20% safety margin to all values for real installations");
        UI.printInfo("Consult a certified solar installer for final system design");
    }
    
    private void printTableRow(String label, String value, String color) {
        System.out.printf(UI.CYAN + "│ " + UI.RESET + "%-35s " + UI.CYAN + "│ " + color + "%20s " + UI.CYAN + "│" + UI.RESET + "\n", 
            label, value);
    }
}

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
            UI.printSuccess("✓ Saved " + appliances.size() + " appliances to " + FILE_NAME);
        } catch (IOException e) {
            UI.printError("✗ Error saving appliances: " + e.getMessage());
        }
    }
}

class UI {
    // Color codes
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";
    
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printBanner() {
        System.out.println(BRIGHT_YELLOW + "\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                              ║");
        System.out.println("║     " + BRIGHT_WHITE + "   SOLAR POWER SYSTEM CALCULATOR   " + BRIGHT_YELLOW + "                 ║");
        System.out.println("║                                                              ║");
        System.out.println("║     " + BRIGHT_CYAN + "Design Your Off-Grid Energy Solution" + BRIGHT_YELLOW + "                 ║");
        System.out.println("║                                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝" + RESET);
    }

    public static void printHeader(String title) {
        int totalWidth = 62;
        int padding = (totalWidth - title.length() - 2) / 2;
        String pad = "═".repeat(padding);
        System.out.println("\n" + BRIGHT_CYAN + "╔" + pad + " " + title + " " + pad + "╗" + RESET);
    }

    public static void printMenu(String title, String[] options) {
        System.out.println("\n" + CYAN + "┌─ " + BOLD + title + RESET + CYAN + " ─┐" + RESET);
        for (int i = 0; i < options.length; i++) {
            System.out.println(CYAN + "│ " + BRIGHT_WHITE + "[" + (i + 1) + "]" + RESET + " " + options[i]);
        }
        System.out.println(CYAN + "└" + "─".repeat(40) + "┘" + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(BRIGHT_GREEN + "✓ " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(BRIGHT_RED + "✗ " + message + RESET);
    }

    public static void printWarning(String message) {
        System.out.println(BRIGHT_YELLOW + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(BRIGHT_BLUE + message + RESET);
    }

    public static void printDivider() {
        System.out.println(BRIGHT_BLACK + "─".repeat(64) + RESET);
    }
}

public class solar {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Appliance> appliances = new ArrayList<>();

        UI.printBanner();
        
        String[] menuOptions = {
            "Start New Calculation",
            "Load Saved Appliances",
            "Exit Program"
        };
        
        UI.printMenu("MAIN MENU", menuOptions);
        System.out.print(UI.CYAN + "→ " + UI.RESET + "Select option: ");
        
        int option = getIntInput(sc, 1, 3);

        if (option == 3) {
            UI.printInfo("\n Thank you for using Solar Calculator. Goodbye!");
            sc.close();
            return;
        }

        if (option == 2) {
            appliances = JsonStorage.loadAppliances();
            if (appliances.isEmpty()) {
                UI.printWarning("\n No saved data found. Starting fresh...");
            } else {
                UI.printSuccess("\n✓ Loaded " + appliances.size() + " appliances from file");
            }
        }

        UI.printHeader("APPLIANCE CONFIGURATION");
        UI.printInfo("\nEnter your appliances one by one (type 'done' when finished)\n");

        int count = 1;
        while (true) {
            System.out.println(UI.BRIGHT_BLACK + "─── Appliance #" + count + " ───" + UI.RESET);
            System.out.print(UI.CYAN + "→ " + UI.RESET + "Name (or 'done' to finish): ");
            String name = sc.nextLine().trim();
            
            if (name.equalsIgnoreCase("done")) break;
            if (name.isEmpty()) {
                UI.printWarning(" Name cannot be empty");
                continue;
            }

            double watt = inputPositiveDouble(sc, "→ Wattage (W): ");
            double hrs = inputRangeDouble(sc, "→ Hours per day: ", 0, 24);
            
            appliances.add(new Appliance(name, watt, hrs));
            UI.printSuccess("✓ Added: " + name + " (" + watt + "W × " + hrs + "h = " + (watt * hrs) + "Wh/day)");
            System.out.println();
            count++;
        }

        if (!appliances.isEmpty()) {
            System.out.print("\n" + UI.YELLOW + "💾 Save appliances to file? (y/n): " + UI.RESET);
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
        
        double sunHours = inputRangeDouble(sc, "→ Average sun hours per day (3-8): ", 1, 12);
        double systemVoltage = inputChoice(sc, "→ System voltage: ", new double[]{12, 24, 48});
        double panelWatt = inputPositiveDouble(sc, "→ Solar panel wattage (W): ");
        double days = inputRangeDouble(sc, "→ Days of autonomy (1-5): ", 1, 10);
        double dod = inputRangeDouble(sc, "→ Depth of discharge (0.5-0.8 recommended): ", 0.1, 1.0);
        double invEff = inputRangeDouble(sc, "→ Inverter efficiency (0.85-0.95 typical): ", 0.5, 1.0);

        SolarCalculator calc = new SolarCalculator(appliances);
        calc.computeSystem(sunHours, systemVoltage, panelWatt, days, dod, invEff);
        calc.displayReport();

        System.out.println("\n" + UI.BRIGHT_GREEN + "═".repeat(64));
        System.out.println("  Calculation complete! Press Enter to exit..." + UI.RESET);
        sc.nextLine();
        sc.close();
    }

    private static void displayApplianceTable(List<Appliance> appliances) {
        if (appliances.isEmpty()) {
            UI.printWarning("\n No appliances configured");
            return;
        }

        UI.printHeader("APPLIANCE SUMMARY");
        System.out.println(UI.CYAN + "\n┌────┬─────────────────────────┬──────────┬───────────┬─────────────┐");
        System.out.println("│ #  │ Appliance               │ Wattage  │ Hours/Day │ Daily (Wh)  │");
        System.out.println("├────┼─────────────────────────┼──────────┼───────────┼─────────────┤" + UI.RESET);
        
        int idx = 1;
        double totalDaily = 0;
        for (Appliance a : appliances) {
            double daily = a.getDailyConsumption();
            totalDaily += daily;
            System.out.printf(UI.CYAN + "│" + UI.RESET + " %-2d " + UI.CYAN + "│" + UI.RESET + 
                " %-23s " + UI.CYAN + "│" + UI.YELLOW + " %6.0f W " + UI.CYAN + "│" + UI.BLUE + 
                " %7.1f h " + UI.CYAN + "│" + UI.GREEN + " %9.1f Wh" + UI.CYAN + " │" + UI.RESET + "\n",
                idx++, truncate(a.name, 23), a.wattage, a.hoursPerDay, daily);
        }
        
        System.out.println(UI.CYAN + "├────┴─────────────────────────┴──────────┴───────────┼─────────────┤" + UI.RESET);
        System.out.printf(UI.CYAN + "│" + UI.BRIGHT_WHITE + " %-46s " + UI.CYAN + "│" + UI.BRIGHT_GREEN + 
            " %9.1f Wh" + UI.CYAN + " │" + UI.RESET + "\n", "TOTAL DAILY ENERGY CONSUMPTION", totalDaily);
        System.out.println(UI.CYAN + "└────────────────────────────────────────────────────┴─────────────┘" + UI.RESET);
    }

    private static String truncate(String str, int maxLen) {
        return str.length() > maxLen ? str.substring(0, maxLen - 3) + "..." : str;
    }

    private static int getIntInput(Scanner sc, int min, int max) {
        while (true) {
            try {
                int value = Integer.parseInt(sc.nextLine().trim());
                if (value >= min && value <= max) return value;
                UI.printWarning(" Please enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                UI.printWarning(" Invalid input. Please enter a number.");
            }
        }
    }

    private static double inputPositiveDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(UI.CYAN + prompt + UI.RESET);
            try {
                double value = Double.parseDouble(sc.nextLine().trim());
                if (value > 0) return value;
                UI.printWarning(" Please enter a positive number");
            } catch (NumberFormatException e) {
                UI.printWarning(" Invalid number format");
            }
        }
    }

    private static double inputRangeDouble(Scanner sc, String prompt, double min, double max) {
        while (true) {
            System.out.print(UI.CYAN + prompt + UI.RESET);
            try {
                double value = Double.parseDouble(sc.nextLine().trim());
                if (value >= min && value <= max) return value;
                UI.printWarning(" Please enter a value between " + min + " and " + max);
            } catch (NumberFormatException e) {
                UI.printWarning(" Invalid number format");
            }
        }
    }

    private static double inputChoice(Scanner sc, String prompt, double[] choices) {
        System.out.print(UI.CYAN + prompt + UI.RESET);
        for (int i = 0; i < choices.length; i++) {
            System.out.print(UI.BRIGHT_WHITE + "[" + (i + 1) + "] " + choices[i] + "V" + UI.RESET);
            if (i < choices.length - 1) System.out.print("  ");
        }
        System.out.println();
        System.out.print(UI.CYAN + "→ Select: " + UI.RESET);
        
        int choice = getIntInput(sc, 1, choices.length);
        return choices[choice - 1];
    }
}