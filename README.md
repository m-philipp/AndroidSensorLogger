AndroidSensorLogger
===================

Android Application Logging the Builtin Android Sensors; plus the Sensors on your Android Wear Smartwatch.

## API: Broadcasted intents

Annotation added:

    Intent sendIntent = new Intent("ess.imu_logger.libs.data_save.annotate");
    sendIntent.putExtra("ess.imu_logger.libs.data_save.extra.annotationName", "smoking");
    sendBroadcast(sendIntent);
