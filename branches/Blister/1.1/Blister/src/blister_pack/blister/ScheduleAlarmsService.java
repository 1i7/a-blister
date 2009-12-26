package blister_pack.blister;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import blister_pack.blister.database.BlisterDatabase;
import blister_pack.blister.database.tables.Course;
import blister_pack.blister.database.tables.OccuredNotification;
import blister_pack.blister.database.tables.PillNotification;
import blister_pack.blister.windows.ConfirmActivity;
import blister_pack.blister.windows.MissedNotificationsWindow;

public class ScheduleAlarmsService extends Service {
	
	private static String TAG = "ScheduleAlarmsService";
	
	static int notificationID = 0;

	NotificationManager manager;
	AlarmManager alarmManager;
	Notification notification;
	Timer timer;
	
	// is used in scheduling notifications (see below)
	/*private class NotificationTask extends TimerTask {
		String courseName;
		Date time;


		public NotificationTask(String courseName, Date time) {
			this.courseName = courseName;
			this.time = time;
		}

		public void run() {
			showNotification(courseName, time);
		}
	}*/
	
	/*private class MissedNotificationsTask extends TimerTask {
		ArrayList<OccuredNotification> occuredNotifications;


		public MissedNotificationsTask(ArrayList<OccuredNotification> occuredNotifications) {
			this.occuredNotifications = occuredNotifications;
		}

		public void run() {
			ArrayList<MissedNotification> missedNotifications = new ArrayList<MissedNotification>();
			for (OccuredNotification occuredNotification : occuredNotifications)
				missedNotifications.add(new MissedNotification(occuredNotification));
			showMissedNotification(missedNotifications);
		}
	}*/

	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int i) {
		super.onStart(intent, i);
		if (manager == null)
			manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancelAll();
		if (timer!=null)
			timer.cancel();
		timer = new Timer();
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		scheduleNotifications();
	}

	// schedules notifications or stops self if no further notifications needed
	/*private void scheduleNotificationsOrStop() {
		if (scheduleNotifications() == 0) {
			stopSelf();
		}
	}*/
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		/*if (manager != null)
			manager.cancelAll();*/
		if (timer != null)
			timer.cancel();
	}

	private int scheduleNotifications() {
		BlisterDatabase database = BlisterDatabase.openDatabase(ScheduleAlarmsService.this);
		ArrayList<OccuredNotification> occuredNotifications = database.getOccuredNotificationTable().selectAll();
		int count = 0;
		if (occuredNotifications.size()>0) {
			showMissedNotification(getMissedNotifications(occuredNotifications));
			//timer.schedule(new MissedNotificationsTask(occuredNotifications), new Date());
			count++;
		}
		ArrayList<PillNotification> pillNotifications = database
				.getPillNotificationTable().selectAll();
		boolean furtherNotificationsNeeded = false;

		for (PillNotification pillNotification : pillNotifications) {
			Course course = database.getCourseTable().select(
					pillNotification.courseName);
			Calendar endDateCalendar = Calendar.getInstance();
			Calendar currentTimeCalendar = Calendar.getInstance();
			endDateCalendar.setTime(course.startDate);
			endDateCalendar.add(Calendar.DAY_OF_MONTH, course.duration);
			if (!endDateCalendar.before(currentTimeCalendar) || (course.pillsRemained > 0)) {
				furtherNotificationsNeeded = true;
				if (pillNotification.dayOfWeek == Calendar.getInstance().get(
						Calendar.DAY_OF_WEEK)) {
					// appending current year, month and day of month to
					// notification time
					Calendar pillNotificationCalendar = Calendar.getInstance();

					pillNotificationCalendar.setTime(pillNotification.time);
					pillNotificationCalendar.set(Calendar.YEAR,
							currentTimeCalendar.get(Calendar.YEAR));
					pillNotificationCalendar.set(Calendar.MONTH,
							currentTimeCalendar.get(Calendar.MONTH));
					pillNotificationCalendar.set(Calendar.DAY_OF_MONTH,
							currentTimeCalendar.get(Calendar.DAY_OF_MONTH));

					if (!pillNotificationCalendar.before(currentTimeCalendar)) {
						Date pillNotificationDateTime = pillNotificationCalendar.getTime();
						Log.v(TAG,"Scheduling notification: "+pillNotification.courseName+": "+pillNotificationDateTime);
						/*timer.schedule(new NotificationTask(pillNotification.courseName, pillNotificationDateTime),
								pillNotificationDateTime);*/
						alarmManager.set(AlarmManager.RTC_WAKEUP, pillNotificationDateTime.getTime(), 
								getPendingIntent(pillNotification.courseName, pillNotificationDateTime));
						count++;
					}
				}
			}
		}
		// scheduling notification on the next day
		if (furtherNotificationsNeeded) {
			// making time to notify the next day
			Calendar furtherNotificationCalendar = Calendar.getInstance();
			furtherNotificationCalendar.add(Calendar.DAY_OF_MONTH, 1);
			furtherNotificationCalendar.set(Calendar.HOUR_OF_DAY, 0);
			furtherNotificationCalendar.set(Calendar.MINUTE, 0);
			furtherNotificationCalendar.set(Calendar.SECOND, 0);
			/*timer.schedule(new TimerTask() {
				public void run() {
					scheduleNotificationsOrStop();
				}
			}, furtherNotificationCalendar.getTime());*/
			alarmManager.set(AlarmManager.RTC_WAKEUP, furtherNotificationCalendar.getTimeInMillis(), 
					getSchedulingPendingIntent());
			count++;
		}
		stopSelf();
		return count;
	}
	
	PendingIntent getPendingIntent(String courseName, Date time) {
		/*Intent notificationIntent = new Intent(ScheduleAlarmsService.this, ShowNotificationService.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notificationIntent.putExtra("notification_id", notificationID);
		notificationIntent.putExtra("course_name", courseName).putExtra("time", time);
		PendingIntent result = PendingIntent.getService(ScheduleAlarmsService.this, notificationID, 
				notificationIntent, 0);*/
		Intent notificationIntent = new Intent(ScheduleAlarmsService.this,AlarmReceiver.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notificationIntent.putExtra("notification_id", notificationID);
		notificationIntent.putExtra("course_name", courseName).putExtra("time", time);
		PendingIntent result = PendingIntent.getBroadcast(this, 0, notificationIntent, 0);
		notificationID++;
		return result;
	}
	
	PendingIntent getSchedulingPendingIntent() {
		Intent schedulingIntent = new Intent(ScheduleAlarmsService.this, ScheduleAlarmsService.class);
		PendingIntent result = PendingIntent.getService(ScheduleAlarmsService.this, notificationID,	
				schedulingIntent, 0);
		return result;
	}

	ArrayList<MissedNotification> getMissedNotifications(ArrayList<OccuredNotification> occuredNotifications) {
		ArrayList<MissedNotification> missedNotifications = new ArrayList<MissedNotification>();
		for (OccuredNotification occuredNotification : occuredNotifications)
			missedNotifications.add(new MissedNotification(occuredNotification));
		return missedNotifications;
	}
	
	private void showMissedNotification(ArrayList<MissedNotification> missedNotifications) {
		final String notificationTitle = getString(R.string.missed_notification_title);
		final String notificationMessage = getString(R.string.missed_notification_message);

		notification = new Notification(R.drawable.pills_man, notificationMessage, Calendar.getInstance().getTimeInMillis());
		Intent confirmIntent = new Intent(this, MissedNotificationsWindow.class);
		confirmIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//confirmIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		confirmIntent.putExtra("notification_id", notificationID);
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Date> times = new ArrayList<Date>();
		for (int i=0;i<missedNotifications.size();i++) {
			MissedNotification missedNotification = missedNotifications.get(i);
			names.add(missedNotification.courseName);
			times.add(missedNotification.occurTime);
		}
		confirmIntent.putExtra("times", times);
		confirmIntent.putExtra("names", names);
		PendingIntent contentIntent = PendingIntent.getActivity(ScheduleAlarmsService.this, notificationID, confirmIntent, 0);

		notification.setLatestEventInfo(ScheduleAlarmsService.this, notificationTitle, notificationMessage, contentIntent);
		manager.notify(notificationID, notification);
		
		notificationID++;
	}

}
