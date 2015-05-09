package de.smart_sense.tracker.app.settingsManagment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.smart_sense.tracker.app.R;
import de.smart_sense.tracker.app.settingsManagment.sensorSettings.SensorSetting;
import de.smart_sense.tracker.app.settingsManagment.sensorSettings.SensorSettings;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SensorFragment.OnSensorFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SensorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SensorFragment extends Fragment implements SensorDetailFragment.OnSensorDetailFragmentInteractionListener, SensorOverviewFragment.OnSensorOverviewFragmentInteractionListener {

    private OnSensorFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SensorFragment.
     */
    public static SensorFragment newInstance() {
        SensorFragment fragment = new SensorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SensorFragment() {
        // Required empty public constructor
    }

    private void setSensorOverviewFragment() {
        Fragment fg = SensorOverviewFragment.newInstance();
        getChildFragmentManager().beginTransaction().add(R.id.settingsSensorFragmentContainer, fg).commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_sensor, container, false);

        setSensorOverviewFragment();

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSensorFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCloseSensorDetails() {
        setSensorOverviewFragment();
    }

    @Override
    public void onCloseSensorOverview() {
        mListener.onExitSensorFragment();
    }

    @Override
    public void onOpenSensorDetail(String sensor) {
        setSensorDetailFragment();
    }

    private void setSensorDetailFragment() {
        Fragment fg = SensorDetailFragment.newInstance();
        getChildFragmentManager().beginTransaction().add(R.id.settingsSensorFragmentContainer, fg).commit();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSensorFragmentInteractionListener {
        public void onExitSensorFragment();
    }

}
