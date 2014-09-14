package ess.imu_logger.app;


import android.app.Dialog;
import android.hardware.Sensor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.List;

import ess.imu_logger.R;

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
	private static final boolean ALWAYS_SIMPLE_PREFS = false;
	final static String ACTION_PREFS_GENERAL = "ess.imu_logger.action.prefs_general";
	final static String ACTION_PREFS_DATA_SYNC = "ess.imu_logger.action.prefs_data_sync";
	final static String ACTION_PREFS_SENSOR = "ess.imu_logger.action.prefs_sensor";

	private static List<Sensor> sensors;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		SensorManager mgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensors = mgr.getSensorList(Sensor.TYPE_ALL);
		*/


		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment())
				.commit();
	}


	protected boolean isValidFragment (String fragmentName)
	{
		if(SettingsFragment.class.getName().equals(fragmentName))
			return true;
		return false;

	}

	/**
	 * This fragment shows data and sync preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	public static class SettingsFragment extends PreferenceFragment {

		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
			super.onPreferenceTreeClick(preferenceScreen, preference);

			// If the user has clicked on a preference screen, set up the action bar
			if (preference instanceof PreferenceScreen) {
				initializeActionBar((PreferenceScreen) preference);
			}

			return false;
		}



		/** Sets up the action bar for an {@link PreferenceScreen} */
		public static void initializeActionBar(PreferenceScreen preferenceScreen) {
			final Dialog dialog = preferenceScreen.getDialog();

			if (dialog != null) {
				// Inialize the action bar
				dialog.getActionBar().setDisplayHomeAsUpEnabled(true);

				// Apply custom home button area click listener to close the PreferenceScreen because PreferenceScreens are dialogs which swallow
				// events instead of passing to the activity
				// Related Issue: https://code.google.com/p/android/issues/detail?id=4611
				View homeBtn = dialog.findViewById(android.R.id.home);

				if (homeBtn != null) {
					View.OnClickListener dismissDialogClickListener = new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					};

					// Prepare yourselves for some hacky programming
					ViewParent homeBtnContainer = homeBtn.getParent();

					// The home button is an ImageView inside a FrameLayout
					if (homeBtnContainer instanceof FrameLayout) {
						ViewGroup containerParent = (ViewGroup) homeBtnContainer.getParent();

						if (containerParent instanceof LinearLayout) {
							// This view also contains the title text, set the whole view as clickable
							((LinearLayout) containerParent).setOnClickListener(dismissDialogClickListener);
						} else {
							// Just set it on the home button
							((FrameLayout) homeBtnContainer).setOnClickListener(dismissDialogClickListener);
						}
					} else {
						// The 'If all else fails' default case
						homeBtn.setOnClickListener(dismissDialogClickListener);
					}
				}
			}
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.preferences);

			/*
			for (Sensor sensor : sensors) {
				Log.d("Sensors", "" + sensor.getName());
			}
			*/

			//findPreference("accelerometer").setEnabled(false);//Disabling

			bindPreferenceSummaryToValue(findPreference("name"));
			bindPreferenceSummaryToValue(findPreference("sampling_rate"));
			bindPreferenceSummaryToValue(findPreference("server_url"));
			bindPreferenceSummaryToValue(findPreference("server_port"));
			bindPreferenceSummaryToValue(findPreference("upload_frequency"));

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
	private static void bindPreferenceSummaryToValue(Preference preference) {
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
