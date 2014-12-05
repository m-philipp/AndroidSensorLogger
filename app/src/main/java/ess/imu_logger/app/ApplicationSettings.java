package ess.imu_logger.app;


import android.app.Dialog;
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

import ess.imu_logger.libs.Util;

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
	final static String ACTION_PREFS_GENERAL = "ess.imu_logger.action.prefs_general";
	final static String ACTION_PREFS_DATA_SYNC = "ess.imu_logger.action.prefs_data_sync";
	final static String ACTION_PREFS_SENSOR = "ess.imu_logger.action.prefs_sensor";
*/
    private static final String TAG = "ess.imu_logger.app.ApplicationSettings";

	private static HashMap<String, Boolean> activeSensors = new HashMap<String, Boolean>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		Sensor s = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (s == null)
			activeSensors.put("accelerometer", false);
		else
			activeSensors.put("accelerometer", true);

		s = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		if (s == null)
			activeSensors.put("gyroscope", false);
		else
			activeSensors.put("gyroscope", true);

		s = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		if (s == null)
			activeSensors.put("magneticField", false);
		else
			activeSensors.put("magneticField", true);

		s = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		if (s == null)
			activeSensors.put("ambientLight", false);
		else
			activeSensors.put("ambientLight", true);

		s = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (s == null)
			activeSensors.put("proximity", false);
		else
			activeSensors.put("proximity", true);

		s = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		if (s == null)
			activeSensors.put("temperature", false);
		else
			activeSensors.put("temperature", true);

		s = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
		if (s == null)
			activeSensors.put("humidity", false);
		else
			activeSensors.put("humidity", true);

		s = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		if (s == null)
			activeSensors.put("pressure", false);
		else
			activeSensors.put("pressure", true);

		s = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		if (s == null)
			activeSensors.put("rotation", false);
		else
			activeSensors.put("rotation", true);

		s = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		if (s == null)
			activeSensors.put("gravity", false);
		else
			activeSensors.put("gravity", true);

		s = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		if (s == null)
			activeSensors.put("linearAccelerometer", false);
		else
			activeSensors.put("linearAccelerometer", true);

		s = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
		if (s == null)
			activeSensors.put("steps", false);
		else
			activeSensors.put("steps", true);


		/*
		SensorManager mgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensors = mgr.getSensorList(Sensor.TYPE_ALL);
*/


		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment())
				.commit();
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



			if (!activeSensors.get("accelerometer")) {
				CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("accelerometer");
				csp.setEnabled(false);
				csp.setChecked(false);
			}
			if (!activeSensors.get("gyroscope")) {
				CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("gyroscope");
				csp.setEnabled(false);
				csp.setChecked(false);
			}

			if (!activeSensors.get("magneticField")) {
				CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("magneticField");
				csp.setEnabled(false);
				csp.setChecked(false);
			}

			if (!activeSensors.get("ambientLight")) {
				CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("ambientLight");
				csp.setEnabled(false);
				csp.setChecked(false);
			}

			if (!activeSensors.get("proximity")) {
				CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("proximity");
				csp.setEnabled(false);
				csp.setChecked(false);
			}

			if (!activeSensors.get("temperature")) {
				CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("temperature");
				csp.setEnabled(false);
				csp.setChecked(false);
			}

			if (!activeSensors.get("humidity")) {
				CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("humidity");
				csp.setEnabled(false);
				csp.setChecked(false);
			}

			if (!activeSensors.get("pressure")) {
				CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("pressure");
				csp.setEnabled(false);
				csp.setChecked(false);
			}

			if (!activeSensors.get("rotation")) {
				CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("rotation");
				csp.setEnabled(false);
				csp.setChecked(false);
			}

			if (!activeSensors.get("gravity")) {
				CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("gravity");
				csp.setEnabled(false);
				csp.setChecked(false);
			}

			if (!activeSensors.get("linearAccelerometer")) {
				CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("linearAccelerometer");
				csp.setEnabled(false);
				csp.setChecked(false);
			}

			if (!activeSensors.get("steps")) {
				CustomSwitchPreference csp = (CustomSwitchPreference) findPreference("steps");
				csp.setEnabled(false);
				csp.setChecked(false);
			}


			//findPreference("accelerometer").setEnabled(false);//Disabling



            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_SERVER_PORT));
            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_SERVER_URL));
            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_SAMPLING_RATE));
            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_NAME));
            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_LAST_UPLOAD));
            bindPreferenceSummaryToValue(findPreference(Util.PREFERENCES_ANNOTATION_NAME));



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

			} else if(preference instanceof EditTextPreference &&
                    preference.getKey().equals("last_upload")) {

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
