package ess.imu_logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class testReceiver extends BroadcastReceiver {
    public testReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
	    Toast.makeText(context, "foobar Broadcast received", Toast.LENGTH_SHORT).show();



        String action = intent.getAction();
	    if (intent != null) {
		    if (action.equals("foobar")) {
			    System.out.println("assssssssssssssssssssssssssssssssssss");
		    }
	    }
    }
}
