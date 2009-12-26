package blister_pack.blister;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String courseName = intent.getStringExtra("course_name");
		Date time = (Date)intent.getSerializableExtra("time");
		int notificationID = intent.getIntExtra("notification_id", 0);
		Intent notificationIntent = new Intent(context, ShowNotificationService.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notificationIntent.putExtra("notification_id", notificationID);
		notificationIntent.putExtra("course_name", courseName).putExtra("time", time);
		AlarmAlertWakeLock.acquireCpuWakeLock(context);
		context.startService(notificationIntent);
	}

}
