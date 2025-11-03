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
        for (int i = 0; i < options.length; i++) { System.out.println("| [" + (i + 1) + "] " + options[i]); }
        System.out.println("+----------------------------------------+");
    }

    public static void printSuccess(String message) { System.out.println("[SUCCESS] " + message); }
    public static void printError(String message) { System.out.println("[ERROR] " + message); }
    public static void printWarning(String message) { System.out.println(message); }
    public static void printInfo(String message) { System.out.println(message); }
    public static void printDivider() { System.out.println("----------------------------------------------------------------"); }
}
