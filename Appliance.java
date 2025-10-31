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
