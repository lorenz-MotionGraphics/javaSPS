import java.util.*;
import java.io.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

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
