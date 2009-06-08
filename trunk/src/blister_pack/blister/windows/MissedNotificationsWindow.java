package blister_pack.blister.windows;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import blister_pack.blister.MissedNotification;
import blister_pack.blister.R;
import blister_pack.blister.database.BlisterDatabase;
import blister_pack.blister.database.tables.Course;
import blister_pack.blister.database.tables.PillNotification;

public class MissedNotificationsWindow extends ListWindow{

	public static final int CONFIRM_REQUEST_CODE=0;
	public static final String TIME_FORMAT="HH:mm dd.MM.yyyy";
	
	public static final int CONFIRM_ALL_DIALOG = 0,
							IGNORE_ALL_DIALOG = 1;
	
	protected static SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
	
	ArrayList<MissedNotification> missedNotifications;
	int notificationID;
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case CONFIRM_ALL_DIALOG:
			return new AlertDialog.Builder(MissedNotificationsWindow.this)
				.setTitle(R.string.confirm_all_dialog_title)
				.setPositiveButton(R.string.ok_text,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								performConfirmAllDialogOkPressed();
							}
					}).setNegativeButton(R.string.cancel_text,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
							}
					}).create();
		case IGNORE_ALL_DIALOG:
			return new AlertDialog.Builder(MissedNotificationsWindow.this)
			.setTitle(R.string.ignore_all_dialog_title)
			.setPositiveButton(R.string.ok_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							performIgnoreAllDialogOkPressed();
						}
				}).setNegativeButton(R.string.cancel_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
				}).create();
		}
		return null;
	}
	
	@Override
	protected void onCreate(Bundle bundle) {
		
		Log.v("eldar", "MissedNotificationsWindow: onCreate");
		
		super.onCreate(bundle);
		if (bundle!=null) {
			missedNotifications = getMissedNotificationsFromBundle(bundle);
		} else if (missedNotifications==null) {
			missedNotifications = getMissedNotificationsFromIntent(getIntent());
		}
		notificationID = getIntent().getIntExtra("notification_id", -1);
		
		((TextView)findViewById(R.id.ListWindowEmptyListText)).setText(R.string.missed_notifications_list_is_empty);
		findViewById(R.id.ListWindowBottomButtonPanel).setVisibility(View.GONE);
		((TextView)findViewById(R.id.ListWindowText)).setText(R.string.missed_notifications_window_title);
		registerForContextMenu(dataList);
		
		((Button)findViewById(R.id.ListWindowEmptyListAddButton1)).setVisibility(View.GONE);
		((Button)findViewById(R.id.ListWindowEmptyListAddButton2)).setVisibility(View.GONE);

		Log.v("eldar", "MissedNotificationsWindow: created");
	}
	
	@Override
	protected void onDestroy() {
		Log.v("eldar", "MainWindow: onDestroy");
		super.onDestroy();
		Log.v("eldar", "MainWindow: destroyed");
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		ArrayList<String> notificationNames = getNotificationNames();
		ArrayList<Date> notificationTimes = getNotificationTimes();
		outState.putStringArrayList("notification_names", notificationNames);
		outState.putSerializable("notification_times", notificationTimes);
	}

	@Override
	protected Cursor getCursor() {
		MatrixCursor cursor = new MatrixCursor(new String[] { "_id", "title", "info" });
		for (int i=0; i<missedNotifications.size();i++) {
			MissedNotification notification = missedNotifications.get(i);
			cursor.addRow(new String[] {""+i, notification.courseName, timeFormat.format(notification.occurTime)});
		}
		return cursor;
	}

	@Override
	int getItemStatID() {
		return R.id.basicListItemStat;
	}

	@Override
	int getItemTitleID() {
		return R.id.basicListItemTitle;
	}

	@Override
	int getListItemID() {
		return R.layout.basic_list_item;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		listMenu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.missed_notifications_window_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.missedConfirmAll:
			showDialog(CONFIRM_ALL_DIALOG);
			return true;
		case R.id.missedIgnoreAll:
			showDialog(IGNORE_ALL_DIALOG);
		default:
			return false;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this,ConfirmActivity.class);
		MissedNotification notification=missedNotifications.get((int) id);
		intent.putExtra("course_name", notification.courseName)
			.putExtra("time", notification.occurTime);
		intent.putExtra("id", (int)id);
		startActivityForResult(intent,CONFIRM_REQUEST_CODE);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Cursor cursor = ((Cursor) dataList.getItemAtPosition(info.position));
		String name = cursor.getString(cursor.getColumnIndex("title"));
		menu.setHeaderTitle(name);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.missed_notifications_window_context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Cursor cursor = ((Cursor) dataList.getItemAtPosition(info.position));
		String name = cursor.getString(cursor.getColumnIndex("title"));
		selectedItemTitle = name;
		selectedItemPosition = (int) dataList
				.getItemIdAtPosition(info.position);
		switch (item.getItemId()) {
		case R.id.missedContextConfirmItem:
			performConfirmItemSelected(selectedItemPosition);
			return true;
		case R.id.missedContextIgnoreItem:
			performIgnoreItemSelected(selectedItemPosition);
			return true;
		default:
			return false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==CONFIRM_REQUEST_CODE) {
			if (resultCode==RESULT_OK) {
				int id = data.getIntExtra("id", -1);
				removeNotification(id);
				refreshListActivity();
			}
		}
	}

	private void performIgnoreItemSelected(int selectedItemPosition) {
		removeNotification(selectedItemPosition);
		refreshListActivity();
	}

	private void performConfirmItemSelected(int selectedItemPosition) {
		MissedNotification notification = missedNotifications.get(selectedItemPosition);
		Course course = getCourse(notification);
		int pillsToTake = getPillsToTake(notification);
		updateNumberOfPillsInDatabase(course.courseName, pillsToTake, course.pillsRemained);
		removeNotification(selectedItemPosition);
		refreshListActivity();
	}

	private void performIgnoreAllDialogOkPressed() {
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		for (MissedNotification notification : missedNotifications) {
			db.getOccuredNotificationTable().delete(notification);
		}
		missedNotifications.clear();
		cancelNotification();
		refreshListActivity();
	}

	private void performConfirmAllDialogOkPressed() {
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		for (MissedNotification notification : missedNotifications) {
			db.getOccuredNotificationTable().delete(notification);
			Course course = getCourse(notification);
			int pillsToTake = getPillsToTake(notification);
			updateNumberOfPillsInDatabase(course.courseName, pillsToTake, course.pillsRemained);
		}
		missedNotifications.clear();
		cancelNotification();
		refreshListActivity();
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<MissedNotification> getMissedNotificationsFromIntent(Intent intent) {
		ArrayList<String> names = intent.getStringArrayListExtra("names");
		ArrayList<Date> times = (ArrayList<Date>)intent.getSerializableExtra("times");
		return getMissedNotificationsFromArrays(names, times);
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<MissedNotification> getMissedNotificationsFromBundle(Bundle bundle) {
		ArrayList<String> names = bundle.getStringArrayList("notification_names");
		ArrayList<Date> times = (ArrayList<Date>)bundle.getSerializable("notification_times");
		return getMissedNotificationsFromArrays(names, times);
	}
	
	private ArrayList<MissedNotification> getMissedNotificationsFromArrays(ArrayList<String> names, ArrayList<Date> times) {
		ArrayList<MissedNotification> result = new ArrayList<MissedNotification>();
		for (int i=0;i<names.size();i++) {
			String name = names.get(i);
			Date time = times.get(i);
			result.add(new MissedNotification(name,time));
		}
		return result;
	}
	
	private ArrayList<String> getNotificationNames() {
		ArrayList<String> result = new ArrayList<String>();
		for (int i=0; i<missedNotifications.size(); i++) {
			MissedNotification notification = missedNotifications.get(i);
			result.add(notification.courseName);
		}
		return result;
	}
	
	private ArrayList<Date> getNotificationTimes() {
		ArrayList<Date> result = new ArrayList<Date>();
		for (MissedNotification notification : missedNotifications) {
			result.add(notification.occurTime);
		}
		return result;
	}
	
	private void removeNotification(int position) {
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		MissedNotification notification = missedNotifications.get(position);
		db.getOccuredNotificationTable().delete(notification);
		missedNotifications.remove(position);
		if (missedNotifications.isEmpty()) {
			cancelNotification();
		}
	}
	
	private void cancelNotification() {
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(notificationID);
	}
	
	private void updateNumberOfPillsInDatabase(String courseName, int pillsToTake, int pillsRemained) {
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		if ((pillsToTake>0) && (pillsRemained>0)) {
			db.getCourseTable().updatePills(courseName, pillsRemained-pillsToTake);
		}	
	}
	
	private Course getCourse(MissedNotification notification) {
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		return db.getCourseTable().select(notification.courseName);
	}
	
	private int getPillsToTake(MissedNotification notification) {
		String courseName = notification.courseName;
		Date time = notification.occurTime;
		Calendar timeCalendar = Calendar.getInstance();
		timeCalendar.setTime(time);
		int dayOfWeek = timeCalendar.get(Calendar.DAY_OF_WEEK);
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		PillNotification pillNotification = db.getPillNotificationTable().selectOne(courseName, dayOfWeek, time);
		if (pillNotification == null) {
			Log.v("adalx", "MissedNotificationWindow: pillNotification was deleted: name=" +courseName+ "; day=" + dayOfWeek 
					+ "; time=" + new SimpleDateFormat("HH:mm").format(time));
			return 0;
		} else {
			return pillNotification.pillsToTake;	
		}
	}
	
}
