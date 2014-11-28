# Broadcast API

If there was an Annotation added by the User. It is created like this:

```java
String via = "watch_ui"; // or "smartphone_ui"
Intent sendIntent = new Intent("ess.imu_logger.libs.data_save.annotate");
sendIntent.putExtra("ess.imu_logger.libs.data_save.extra.annotationName", "smoking");
sendIntent.putExtra("ess.imu_logger.libs.data_save.extra.annotationVia", via);
sendBroadcast(sendIntent);
```