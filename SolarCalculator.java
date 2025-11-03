import java.util.*;
import java.io.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

class SolarCalculator {
    List<Appliance> appliances;
    double sunHours, systemVoltage, panelWatt, daysOfAutonomy, dod, inverterEfficiency;

    public SolarCalculator(List<Appliance> appliances) { this.appliances = appliances; }
    public double getTotalDailyEnergy() { return appliances.stream().mapToDouble(Appliance::getDailyConsumption).sum(); }
    public void computeSystem(double sunHours, double systemVoltage, double panelWatt, double daysOfAutonomy, double dod, double inverterEfficiency) {
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
