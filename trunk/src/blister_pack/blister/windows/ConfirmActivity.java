package blister_pack.blister.windows;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import blister_pack.blister.R;
import blister_pack.blister.database.BlisterDatabase;
import blister_pack.blister.database.tables.Course;
import blister_pack.blister.database.tables.OccuredNotificationTable;
import blister_pack.blister.database.tables.PillNotification;

public class ConfirmActivity extends Activity
{	
	Button ignoreButton;
	Button confirmButton;
	
	public static final String TIME_FORMAT_STRING="HH:mm";
	
	String courseName;
	Date time;
	int pillsRemained;
	int daysRemained;
	int pillsToTake;
	int notificationID;
	static SimpleDateFormat fullTimeFormat = new SimpleDateFormat(OccuredNotificationTable.TIME_FORMAT_STRING);
	static SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_STRING);

	@Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);

		readExtras();
		
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.confirm_activity);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.shield);
		
		confirmButton = (Button)findViewById(R.id.confirmActivityConfirmButton);
		ignoreButton = (Button)findViewById(R.id.confirmActivityIgnoreButton);
		
		((TextView)findViewById(R.id.confirmActivityNameText)).setText(getName());
		((TextView)findViewById(R.id.confirmActivityStatText)).setText(getStatistics());
		
		setButtonListeners();
	}
	
	private void setButtonListeners() {
		confirmButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				performConfirmButtonClick();
			}
		});
		ignoreButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				performIgnoreButtonClick();
			}
		});
	}
	
	private void performConfirmButtonClick() {
		cancelNotification();
		deleteNotificationFromDatabase(courseName, time); 
		updateNumberOfPillsInDatabase();
		finish();
	}
	
	private void performIgnoreButtonClick() {
		cancelNotification();
		deleteNotificationFromDatabase(courseName, time);
		finish();
	}
	
	private String getName() {
		return courseName;
	}
	
	private String getStatistics() {
		String statistics="";
		statistics += getString(R.string.time_text) + ": " + fullTimeFormat.format(time) + "\n";
		if (pillsToTake>0)
			statistics += getString(R.string.pills_to_take_text) + ": " + pillsToTake + "\n";
		if (pillsRemained>0) {
			statistics += getString(R.string.pills_remained_text) + ": " + pillsRemained;
		} else if (daysRemained>0) {
			statistics += getString(R.string.days_remained_text) + ": " + daysRemained;
		} else {
			statistics += "<" + getString(R.string.course_ended_text) + ">";
		}
		return statistics;
	}
	
	private void readExtras() {
		Intent intent=getIntent();
		notificationID = intent.getIntExtra("notification_id", -1);
		courseName = intent.getStringExtra("course_name");
		time = (Date)intent.getSerializableExtra("time");
		BlisterDatabase database = BlisterDatabase.openDatabase(this);
		Course course = database.getCourseTable().select(courseName);

		if (course == null) {
			Log.v("adalx", "ConfirmActivity: course not found \""+courseName+"\"");
			deleteNotificationFromDatabase(courseName,time);
			cancelNotification();
			finish();
			return;
		} else {
			pillsRemained = course.pillsRemained;
			daysRemained = course.duration;
		}
		Calendar timeCalendar = Calendar.getInstance();
		timeCalendar.setTime(time);
		int dayOfWeek = timeCalendar.get(Calendar.DAY_OF_WEEK);
		PillNotification pillNotification = database.getPillNotificationTable().selectOne(courseName, dayOfWeek, time);
		if (pillNotification == null) {
			Log.v("adalx", "ConfirmActivity: pillNotification was deleted: name=" +courseName+ "; day=" + dayOfWeek 
					+ "; time=" + timeFormat.format(time));
			pillsToTake = 0;
		} else {
			pillsToTake = pillNotification.pillsToTake;	
		}
	}
	
	private void cancelNotification() {
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(notificationID);
	}
	
	private void deleteNotificationFromDatabase(String name, Date occurTime) {
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		db.getOccuredNotificationTable().delete(name, occurTime);
		this.setResult(RESULT_OK, getIntent());
	}
	
	private void updateNumberOfPillsInDatabase() {
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		if ((pillsToTake>0) && (pillsRemained>0)) {
			db.getCourseTable().updatePills(courseName, pillsRemained-pillsToTake);
		}	
	}
}
