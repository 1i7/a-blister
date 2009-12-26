package blister_pack.blister;

import java.util.Date;

import blister_pack.blister.database.BlisterDatabase;
import blister_pack.blister.database.tables.Course;
import blister_pack.blister.windows.ConfirmActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ShowNotificationService extends Service{
	
	private static  boolean enabled=true;

	private int notificationID;
	private static String TAG = "ShowNotificationService";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	synchronized public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (isEnabled()) {
			String courseName = intent.getStringExtra("course_name");
			Date time = (Date)intent.getSerializableExtra("time");
			notificationID = intent.getIntExtra("notification_id", 0);
			Log.v(TAG,"Showing notification: "+courseName+": "+time+" id="+notificationID);
			showNotification(courseName, time);
			AlarmAlertWakeLock.releaseCpuLock();
			stopSelf();
		}
	}
	
	synchronized static public boolean isEnabled() {
		return enabled;
	}
	
	synchronized public static void enable() {
		enabled = true;
	}
	
	synchronized public static void disable() {
		enabled = false;
	}
	
	// shows notification
	synchronized private void showNotification(String courseName, Date time) {
		final String notificationTitle = getString(R.string.notification_title);
		final String notificationMessage = getString(R.string.notification_message);
		
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		// checking whether course has been already finished or not
		Course course = db.getCourseTable().select(courseName);
		if ((course.pillsRemained <= 0) && (course.duration <= 0)) {
			return;
		}
		// inserting into OccuredNotification table
		db.getOccuredNotificationTable().insert(courseName, time);

		Notification notification = new Notification(R.drawable.pills_man, notificationMessage, time.getTime());
		notification.defaults |= Notification.DEFAULT_ALL;
		Intent confirmIntent = new Intent(ShowNotificationService.this, ConfirmActivity.class);
		confirmIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		confirmIntent.putExtra("notification_id", notificationID);
		confirmIntent.putExtra("course_name", courseName).putExtra("time", time);
		PendingIntent contentIntent = PendingIntent.getActivity(ShowNotificationService.this, notificationID, 
				confirmIntent, 0);
		notification.setLatestEventInfo(ShowNotificationService.this, notificationTitle, 
				courseName + ": " + notificationMessage, contentIntent);
		NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		manager.notify(notificationID, notification);
	}
}
