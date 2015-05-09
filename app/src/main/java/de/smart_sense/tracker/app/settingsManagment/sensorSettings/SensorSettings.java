package de.smart_sense.tracker.app.settingsManagment.sensorSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorSettings {

    public static List<SensorSetting> ITEMS = new ArrayList<SensorSetting>();


    static {
        // Add 3 sample items.
        addItem(new AccelerometerSetting("1", "Item 1"));
        addItem(new GyroscopeSetting("2", "Item 2"));
        addItem(new GyroscopeSetting("2", "Item 2"));
        addItem(new GyroscopeSetting("2", "Item 2"));
        addItem(new GyroscopeSetting("2", "Item 2"));
        addItem(new GyroscopeSetting("2", "Item 2"));
        addItem(new GyroscopeSetting("2", "Item 2"));
        addItem(new GyroscopeSetting("2", "Item 2"));
        addItem(new GyroscopeSetting("2", "Item 2"));
        addItem(new GyroscopeSetting("2", "Item 2"));
        addItem(new GyroscopeSetting("2", "Item 2"));
        addItem(new GyroscopeSetting("2", "Item 2"));
        addItem(new GyroscopeSetting("2", "Item 2"));

    }

    private static void addItem(SensorSetting item) {
        ITEMS.add(item);
    }

}
