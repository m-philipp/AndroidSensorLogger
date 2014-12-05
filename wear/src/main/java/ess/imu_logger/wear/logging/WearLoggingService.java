package ess.imu_logger.wear.logging;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import ess.imu_logger.libs.Util;
import ess.imu_logger.libs.logging.LoggingService;
import ess.imu_logger.wear.Annotate;
import ess.imu_logger.wear.R;
import ess.imu_logger.wear.WearNotificationStartScreen;

/**
 * Created by martin on 04.12.2014.
 */

public class WearLoggingService extends LoggingService {

    @Override
    protected Notification getNotificationIntent(){
        Intent notificationIntent = new Intent(this, WearNotificationStartScreen.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent secondNotificationIntent = new Intent(this, Annotate.class);
        PendingIntent secondNotificationPendingIntent = PendingIntent.getActivity(
                this,
                0,
                secondNotificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification secondPageNotification =
                new Notification.Builder(this)
                        .extend(new Notification.WearableExtender()
                                .setDisplayIntent(secondNotificationPendingIntent))
                        .build();


        Notification.Builder notificationBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Android Sensor Logger")
                        .setOngoing(true)
                        .setLocalOnly(true)
                        .extend(new Notification.WearableExtender()
                                        .setDisplayIntent(notificationPendingIntent)
                                        .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.wear_bg))
                                        .addAction(new Notification.Action(R.drawable.ic_cigarette_white, getString(R.string.annotate), secondNotificationPendingIntent))

                        );

        return notificationBuilder.build();
    }

}
