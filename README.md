AndroidSensorLogger
===================

![IMU-Logger](
https://github.com/mzittel/AndroidSensorLogger/blob/master/loggingBackend/src/main/res/drawable-xhdpi/ic_launcher.png?raw=true)

Android Application Logging the Builtin Android Sensors; plus the Sensors on your Android Wear Smartwatch.

## API: Broadcasted intents

Annotation added:

```java
String via = "watch_ui"; // or "smartphone_ui"
Intent sendIntent = new Intent("de.smart_sense.tracker.libs.data_save.annotate");
sendIntent.putExtra("de.smart_sense.tracker.libs.data_save.extra.annotationName", "smoking");
sendIntent.putExtra("de.smart_sense.tracker.libs.data_save.extra.annotationVia", via);
sendBroadcast(sendIntent);
```
