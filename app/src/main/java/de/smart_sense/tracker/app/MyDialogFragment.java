package de.smart_sense.tracker.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by martin on 05.12.2014.
 */
public class MyDialogFragment extends DialogFragment {

    private String mMessage, mConfirm, mCancel;
    private int mId = 0;

    public static final String ARG_MESSAGE = "message"; // R.string.dialog_sure_annotate
    public static final String ARG_CONFIRM = "confirm"; // R.string.confirm_annotate
    public static final String ARG_CANCEL = "cancel"; // R.string.cancel
    public static final String ARG_ID = "id"; // R.string.cancel

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        mMessage = args.getString(ARG_MESSAGE, "Want some Donuts?");
        mConfirm = args.getString(ARG_CONFIRM, "Sure!");
        mCancel = args.getString(ARG_CANCEL, "Nah.");
        mId = args.getInt(ARG_ID, 0);
    }


    public interface NoticeDialogListener {
        public void onDialogPositiveClick(int id);
    }
    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(mMessage)
                .setPositiveButton(mConfirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                        mListener.onDialogPositiveClick(mId);
                    }
                })
                .setNegativeButton(mCancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
