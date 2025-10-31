import java.util.*;
import java.io.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

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
