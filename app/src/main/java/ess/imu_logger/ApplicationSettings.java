package ess.imu_logger;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;

import java.util.List;

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


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String action = getIntent().getAction();
		if (action != null && action.equals(ACTION_PREFS_GENERAL)) {
			addPreferencesFromResource(R.xml.pref_general);
		} else if (action != null && action.equals(ACTION_PREFS_DATA_SYNC)) {
			addPreferencesFromResource(R.xml.pref_data_sync);
		} else if (action != null && action.equals(ACTION_PREFS_SENSOR)) {
			addPreferencesFromResource(R.xml.pref_sensor);
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			// Load the legacy preferences headers
			addPreferencesFromResource(R.xml.pref_headers_legacy);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.pref_headers, target);
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
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class SettingsFragment extends PreferenceFragment {



		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);


			String settings = getArguments().getString("settings");

			if (ACTION_PREFS_GENERAL.equals(settings)) {

				addPreferencesFromResource(R.xml.pref_general);
				bindPreferenceSummaryToValue(findPreference("name"));

			} else if (ACTION_PREFS_SENSOR.equals(settings)) {

				addPreferencesFromResource(R.xml.pref_sensor);
				bindPreferenceSummaryToValue(findPreference("sampling_rate"));

			} else if (ACTION_PREFS_DATA_SYNC.equals(settings)) {

				addPreferencesFromResource(R.xml.pref_data_sync);
				bindPreferenceSummaryToValue(findPreference("server_url"));
				bindPreferenceSummaryToValue(findPreference("server_port"));
				bindPreferenceSummaryToValue(findPreference("upload_frequency"));

			} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				// Load the legacy preferences headers
				addPreferencesFromResource(R.xml.pref_headers_legacy);
			}

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
