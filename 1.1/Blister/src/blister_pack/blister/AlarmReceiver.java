package blister_pack.blister;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmAlertWakeLock.acquireCpuWakeLock(context);
		String courseName = intent.getStringExtra("course_name");
		Date time = (Date)intent.getSerializableExtra("time");
		int notificationID = intent.getIntExtra("notification_id", 0);
		Log.v("AlarmReceiver","Received alarm: "+courseName+": "+time);
		Intent notificationIntent = new Intent(context, ShowNotificationService.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notificationIntent.putExtra("notification_id", notificationID);
		notificationIntent.putExtra("course_name", courseName).putExtra("time", time);
		context.startService(notificationIntent);
	}

}
