package de.smart_sense.tracker.app.settingsManagment.sensorSettings;

/**
 * Created by martin on 14.04.2015.
 */
public class GyroscopeSetting implements SensorSetting {

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

        public GyroscopeSetting(String id, String content){
            this.id = id;
            this.content = content;
        }

        public String toString(){
            return "Gyroscope Setting!";
        }


    }