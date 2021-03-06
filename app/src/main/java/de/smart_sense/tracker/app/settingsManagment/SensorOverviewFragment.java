package de.smart_sense.tracker.app.settingsManagment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import de.smart_sense.tracker.app.R;

import de.smart_sense.tracker.app.settingsManagment.sensorSettings.SensorSettings;

public class SensorOverviewFragment extends Fragment implements AbsListView.OnItemClickListener {


    private OnSensorOverviewFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SensorOverviewFragment() {
    }

    public static Fragment newInstance() {
        SensorOverviewFragment mFrgment = new SensorOverviewFragment();
        return mFrgment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new ArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_1, SensorSettings.ITEMS);
        //mAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, SensorSettings.ITEMS);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_sensor_overview, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(R.id.dataSourceListView);

        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter); // TODO check if mAdapter is not null esp. does not implementi interface

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSensorOverviewFragmentInteractionListener) activity;
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onOpenSensorDetail(SensorSettings.ITEMS.get(position).getId());
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
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
    public interface OnSensorOverviewFragmentInteractionListener {
        public void onCloseSensorOverview();
        public void onOpenSensorDetail(String sensor);
    }

}
