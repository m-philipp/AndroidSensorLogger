AndroidSensorLogger
===================

Android Application Logging the Builtin Android Sensors; plus the Sensors on your Android Wear Smartwatch.

## API: Broadcasted intents

Annotation added:

    Intent sendIntent = new Intent(SensorDataSavingService.BROADCAST_ANNOTATION);
    sendIntent.putExtra(SensorDataSavingService.EXTRA_ANNOTATION_NAME, "smoking");
    sendBroadcast(sendIntent);
