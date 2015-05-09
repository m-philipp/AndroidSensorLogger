package de.smart_sense.tracker.app;


import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.CustomSwitchPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.HashMap;

import de.smart_sense.tracker.libs.Util;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class ApplicationSettings extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    /*
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
	final static String ACTION_PREFS_GENERAL = "de.smart_sense.tracker.action.prefs_general";
	final static String ACTION_PREFS_DATA_SYNC = "de.smart_sense.tracker.action.prefs_data_sync";
	final static String ACTION_PREFS_SENSOR = "de.smart_sense.tracker.action.prefs_sensor";
    */

    private static final String TAG = ApplicationSettings.class.getName();

    private static HashMap<String, Boolean> activeSensors = new HashMap<String, Boolean>();
    private static HashMap<String, Float> sensorsEnergyConsumption = new HashMap<String, Float>();

    public static String versionName = "0.0";
    public static int versionNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        activeSensors = getSensors(mSensorManager);

		/*
		SensorManager mgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensors = mgr.getSensorList(Sensor.TYPE_ALL);
        */
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionNumber = pinfo.versionCode;
            versionName = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package Info not found");
        }

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    private HashMap<String, Boolean> getSensors(SensorManager mSensorManager) {

        HashMap<String, Boolean> foundSensors = new HashMap<String, Boolean>();

        Sensor s = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (s == null)
            foundSensors.put("accelerometer", false);
        else {
            foundSensors.put("accelerometer", true);
            sensorsEnergyConsumption.put("accelerometer", s.getPower());
        }

        s = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (s == null)
            foundSensors.put("gyroscope", false);
        else {
            foundSensors.put("gyroscope", true);
            sensorsEnergyConsumption.put("gyroscope", s.getPower());
        }

        s = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (s == null)
            foundSensors.put("magneticField", false);
        else {
            foundSensors.put("magneticField", true);
            sensorsEnergyConsumption.put("magneticField", s.getPower());
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (s == null)
            foundSensors.put("ambientLight", false);
        else{
            foundSensors.put("ambientLight", true);
            sensorsEnergyConsumption.put("ambientLight", s.getPower());
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (s == null)
            foundSensors.put("proximity", false);
        else{
            foundSensors.put("proximity", true);
            sensorsEnergyConsumption.put("proximity", s.getPower());
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (s == null)
            foundSensors.put("temperature", false);
        else{
            foundSensors.put("temperature", true);
            sensorsEnergyConsumption.put("temperature", s.getPower());
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        if (s == null)
            foundSensors.put("humidity", false);
        else{
            foundSensors.put("humidity", true);
            sensorsEnergyConsumption.put("humidity", s.getPower());
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (s == null)
            foundSensors.put("pressure", false);
        else{
            foundSensors.put("pressure", true);
            sensorsEnergyConsumption.put("pressure", s.getPower());
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (s == null)
            foundSensors.put("rotation", false);
        else{
            foundSensors.put("rotation", true);
            sensorsEnergyConsumption.put("rotation", s.getPower());
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (s == null)
            foundSensors.put("gravity", false);
        else{
            foundSensors.put("gravity", true);
            sensorsEnergyConsumption.put("gravity", s.getPower());
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (s == null)
            foundSensors.put("linearAccelerometer", false);
        else{
            foundSensors.put("linearAccelerometer", true);
            sensorsEnergyConsumption.put("linearAccelerometer", s.getPower());
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (s == null)
            foundSensors.put("steps", false);
        else
        {
            foundSensors.put("steps", true);
            sensorsEnergyConsumption.put("steps", s.getPower());
        }

        return foundSensors;
    }


    protected boolean isValidFragment(String fragmentName) {
        return SettingsFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class SettingsFragment extends PreferenceFragment {


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);


            EditTextPreference etp = (EditTextPreference) findPreference("version");
            etp.setText(versionName);

            if (!activeSensors.get("accelerometer")) {
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("accelerometer");
                csp.setSummary("nicht vorhanden");
                //csp.setEnabled(false);
                //csp.setChecked(false);
            }
            else{
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("accelerometer");
                csp.setSummary(String.format("%.2f", sensorsEnergyConsumption.get("accelerometer")) + "mA");
            }


            if (!activeSensors.get("gyroscope")) {
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("gyroscope");
                csp.setSummary("nicht vorhanden");
                //csp.setEnabled(false);
                //csp.setChecked(false);
            }
            else{
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("gyroscope");
                csp.setSummary(String.format("%.2f", sensorsEnergyConsumption.get("gyroscope")) + "mA");
            }



            if (!activeSensors.get("magneticField")) {
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("magneticField");
                csp.setSummary("nicht vorhanden");
                //csp.setEnabled(false);
                //csp.setChecked(false);
            }
            else{
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("magneticField");
                csp.setSummary(String.format("%.2f", sensorsEnergyConsumption.get("magneticField")) + "mA");
            }



            if (!activeSensors.get("ambientLight")) {
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("ambientLight");
                csp.setSummary("nicht vorhanden");
                //csp.setEnabled(false);
                //csp.setChecked(false);
            }
            else{
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("ambientLight");
                csp.setSummary(String.format("%.2f", sensorsEnergyConsumption.get("ambientLight")) + "mA");
            }



            if (!activeSensors.get("proximity")) {
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("proximity");
                csp.setSummary("nicht vorhanden");
                //csp.setEnabled(false);
                //csp.setChecked(false);
            }
            else{
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("proximity");
                csp.setSummary(String.format("%.2f", sensorsEnergyConsumption.get("proximity")) + "mA");
            }



            if (!activeSensors.get("temperature")) {
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("temperature");
                csp.setSummary("nicht vorhanden");
                //csp.setEnabled(false);
                //csp.setChecked(false);
            }
            else{
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("temperature");
                csp.setSummary(String.format("%.2f", sensorsEnergyConsumption.get("temperature")) + "mA");
            }



            if (!activeSensors.get("humidity")) {
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("humidity");
                csp.setSummary("nicht vorhanden");
                //csp.setEnabled(false);
                //csp.setChecked(false);
            }
            else{
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("humidity");
                csp.setSummary(String.format("%.2f", sensorsEnergyConsumption.get("humidity")) + "mA");
            }



            if (!activeSensors.get("pressure")) {
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("pressure");
                csp.setSummary("nicht vorhanden");
                //csp.setEnabled(false);
                //csp.setChecked(false);
            }
            else{
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("pressure");
                csp.setSummary(String.format("%.2f", sensorsEnergyConsumption.get("pressure")) + "mA");
            }



            if (!activeSensors.get("rotation")) {
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("rotation");
                csp.setSummary("nicht vorhanden");
                //csp.setEnabled(false);
                //csp.setChecked(false);
            }
            else{
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("rotation");
                csp.setSummary(String.format("%.2f", sensorsEnergyConsumption.get("rotation")) + "mA");
            }



            if (!activeSensors.get("gravity")) {
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("gravity");
                csp.setSummary("nicht vorhanden");
                //csp.setEnabled(false);
                //csp.setChecked(false);
            }
            else{
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("gravity");
                csp.setSummary(String.format("%.2f", sensorsEnergyConsumption.get("gravity")) + "mA");
            }



            if (!activeSensors.get("linearAccelerometer")) {
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("linearAccelerometer");
                csp.setSummary("nicht vorhanden");
                //csp.setEnabled(false);
                //csp.setChecked(false);
            }
            else{
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("linearAccelerometer");
                csp.setSummary(String.format("%.2f", sensorsEnergyConsumption.get("linearAccelerometer")) + "mA");
            }



            if (!activeSensors.get("steps")) {
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("steps");
                csp.setSummary("nicht vorhanden");
                //csp.setEnabled(false);
                //csp.setChecked(false);
            }
            else{
                CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("steps");
                csp.setSummary(String.format("%.2f", sensorsEnergyConsumption.get("steps")) + "mA");
            }




            //findPreference("accelerometer").setEnabled(false);//Disabling


            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_SERVER_PORT));
            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_SERVER_URL));
            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_SAMPLING_RATE));
            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_NAME));
            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_LAST_UPLOAD));
            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_ANNOTATION_NAME));
            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_WEAR_TEMP_LOGGING_DURATION));
            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_LAST_ANNOTATION));
            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_VERSION));


        }

    }


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null
                );

            } else if (preference instanceof EditTextPreference &&
                    (preference.getKey().equals(Util.PREFERENCES_LAST_UPLOAD) || preference.getKey().equals(Util.PREFERENCES_LAST_ANNOTATION))) {

                preference.setSummary(Util.getFriendlyTime(Long.parseLong(stringValue), System.currentTimeMillis()));

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference
                                                             preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "")
        );
    }

}
