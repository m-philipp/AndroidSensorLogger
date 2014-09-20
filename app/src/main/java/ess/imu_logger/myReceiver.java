package ess.imu_logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class myReceiver extends BroadcastReceiver {
    public myReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.



        String action = intent.getAction();
	    if (intent != null) {
		    if(action.equals("ess.imu_logger.smokeAnnotation")){
			    Toast.makeText(context, "smokeAnnotation Broadcast received", Toast.LENGTH_SHORT).show();



		    } else if(action.equals(intent.ACTION_BOOT_COMPLETED)){
			    // TODO magic boot starting
			    /*
			      Intent serviceIntent = new Intent(context, MySystemService.class);
                  context.startService(serviceIntent);
			     */
		    }
	    }
    }
}
