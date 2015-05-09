package de.smart_sense.tracker.app.settingsManagment.sensorSettings;

/**
 * Created by martin on 12.04.2015.
 */
public class AccelerometerSetting implements SensorSetting {

    String id;

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getId() {
        return id;
    }

    String content;

    public AccelerometerSetting(String id, String content){
        this.id = id;
        this.content = content;
    }

    public String toString(){
        return "Accelerometer Setting!";
    }


}
