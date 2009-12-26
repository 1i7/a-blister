package blister_pack.blister;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import blister_pack.blister.database.BlisterDatabase;
import blister_pack.blister.database.tables.Course;
import blister_pack.blister.database.tables.OccuredNotification;
import blister_pack.blister.database.tables.PillNotification;
import blister_pack.blister.windows.ConfirmActivity;
import blister_pack.blister.windows.MissedNotificationsWindow;

public class CopyOfNotificationService extends Service {
	static int notificationID = 0;

	NotificationManager manager;
	Notification notification;
	Timer timer;
	
	// is used in scheduling notifications (see below)
	private class NotificationTask extends TimerTask {
		String courseName;
		Date time;


		public NotificationTask(String courseName, Date time) {
			this.courseName = courseName;
			this.time = time;
		}

		public void run() {
			showNotification(courseName, time);
		}
	}
	
	private class MissedNotificationsTask extends TimerTask {
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
	}

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
		scheduleNotificationsOrStop();
	}

	// schedules notifications or stops self if no further notifications needed
	private void scheduleNotificationsOrStop() {
		if (scheduleNotifications() == 0) {
			stopSelf();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		/*if (manager != null)
			manager.cancelAll();*/
		if (timer != null)
			timer.cancel();
	}

	/**
	 * Schedules all notifications according to the information in database
	 * Schedules only notifications for current day and one task for the next 
	 * day to schedule further notifications if necessary
	 */
	private int scheduleNotifications() {
		BlisterDatabase database = BlisterDatabase.openDatabase(CopyOfNotificationService.this);
		ArrayList<OccuredNotification> occuredNotifications = database.getOccuredNotificationTable().selectAll();
		int count = 0;
		if (occuredNotifications.size()>0) {
			timer.schedule(new MissedNotificationsTask(occuredNotifications), new Date());
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

						timer.schedule(new NotificationTask(pillNotification.courseName, pillNotificationDateTime),
								pillNotificationDateTime);
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
			timer.schedule(new TimerTask() {
				public void run() {
					scheduleNotificationsOrStop();
				}
			}, furtherNotificationCalendar.getTime());
			count++;
		}
		return count;
	}

	// shows notification
	private void showNotification(String courseName, Date time) {
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

		notification = new Notification(R.drawable.pills_man, notificationMessage, time.getTime());
		//notification.defaults |= Notification.DEFAULT_ALL;
		Intent confirmIntent = new Intent(CopyOfNotificationService.this, ConfirmActivity.class);
		confirmIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		confirmIntent.putExtra("notification_id", notificationID);
		confirmIntent.putExtra("course_name", courseName).putExtra("time", time);
		PendingIntent contentIntent = PendingIntent.getActivity(CopyOfNotificationService.this, notificationID, 
				confirmIntent, 0);
		notification.setLatestEventInfo(CopyOfNotificationService.this, notificationTitle, 
				courseName + ": " + notificationMessage, contentIntent);
		manager.notify(notificationID, notification);
		
		notificationID++;
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
		PendingIntent contentIntent = PendingIntent.getActivity(CopyOfNotificationService.this, notificationID, confirmIntent, 0);

		notification.setLatestEventInfo(CopyOfNotificationService.this, notificationTitle, notificationMessage, contentIntent);
		manager.notify(notificationID, notification);
		
		notificationID++;
	}

}
