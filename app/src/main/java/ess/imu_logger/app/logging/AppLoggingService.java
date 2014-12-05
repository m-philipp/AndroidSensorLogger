package ess.imu_logger.app.logging;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import ess.imu_logger.app.StartScreen;
import ess.imu_logger.libs.Util;
import ess.imu_logger.libs.logging.LoggingService;

public class AppLoggingService extends LoggingService {

    @Override
    protected Notification getNotificationIntent(){

        Intent openIntent = new Intent(this, StartScreen.class);
        PendingIntent pendingOpenIntent = PendingIntent.getActivity(this,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent annotateIntent = new Intent(this, StartScreen.class);
        annotateIntent.setAction(Util.ACTION_ANNOTATE);
        PendingIntent pendingAnnotateIntent = PendingIntent.getActivity(this,
                0,
                annotateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(StartScreen.class);
        stackBuilder.addNextIntent(annotateIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(ess.imu_logger.R.drawable.ic_action_core_refresh_hd)
                        .setLocalOnly(true)
                        .setContentTitle("Android Sensor Logger") // Title
                        .setContentText("Aufzeichnung läuft.") // Sub-Title
                        .setContentIntent(pendingOpenIntent)
                        .addAction(ess.imu_logger.R.drawable.ic_cigarette_white,
                                "Annotiere",
                                resultPendingIntent);

        return mBuilder.build();
    }

}
