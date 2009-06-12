package blister_pack.blister.windows;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import blister_pack.blister.R;
import blister_pack.blister.database.BlisterDatabase;
import blister_pack.blister.database.tables.PillNotification;

public class WeekWindow extends ListWindow {

	private static final int CLEAR_ALL_DIALOG = 0;
	private static final int CLEAR_DAY_DIALOG = 1;
	private static final int TEMPORARY_UNAVAILABLE_DIALOG = 2;
	private static final int DELETE_COURSE_DIALOG = 3;

	private static final String TIME_FORMAT = "HH:mm";
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat(
			TIME_FORMAT);

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case CLEAR_ALL_DIALOG:
			return new AlertDialog.Builder(WeekWindow.this)
				.setTitle(R.string.clear_all_dialog_message)
				.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						performClearAllDialogOkPressed();
					}
				}).setNegativeButton(R.string.cancel_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
				}).create();
		case CLEAR_DAY_DIALOG:
			return new AlertDialog.Builder(WeekWindow.this)
				.setTitle(R.string.clear_day_dialog_message)
				.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						performClearDialogOkPressed(selectedItemPosition);
					}
				}).setNegativeButton(R.string.cancel_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
				}).create();
		case DELETE_COURSE_DIALOG:
			return new AlertDialog.Builder(WeekWindow.this)
				.setTitle(R.string.delete_course_dialog_message)
				.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						performDeleteCourseDialogOkPressed();
					}
				}).setNegativeButton(R.string.cancel_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
				}).create();
		case TEMPORARY_UNAVAILABLE_DIALOG:
			return new AlertDialog.Builder(WeekWindow.this)
				.setCancelable(false)
				.setTitle(getString(R.string.temporarily_unavailable_message))
				.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
					}
				}).create();
		}
		return null;
	}

	protected int getListItemID() {
		return R.layout.week_list_item;
	}

	protected int getItemTitleID() {
		return R.id.weekListItemTitle;
	}

	protected int getItemStatID() {
		return R.id.weekListItemStat;
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Intent intent = getIntent();
		pillNameTitle = intent.getStringExtra("pillName");
		if (intent.getBooleanExtra("newScheduleState", false)) {
			((TextView) findViewById(R.id.ListWindowText))
					.setText(pillNameTitle);
		} else {
			findViewById(R.id.ListWindowTopPanel).setVisibility(View.GONE);
		}

		registerForContextMenu(dataList);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		listMenu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.week_window_menu, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.weekAddItem:
			Intent intent = new Intent(WeekWindow.this, AddTimetableWindow.class)
				.putExtra("pillName", pillNameTitle);
			startActivity(intent);
			return true;
		case R.id.weekClearItem:
			showDialog(CLEAR_ALL_DIALOG);
			return true;
		case R.id.weekDeleteItem:
			showDialog(DELETE_COURSE_DIALOG);
			return true;
		default:
			return false;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Cursor cursor = ((Cursor) dataList.getItemAtPosition(position));
		String day = cursor.getString(cursor.getColumnIndex("title"));
		int dayNumber = (int) dataList.getItemIdAtPosition(position);
		Intent intent = new Intent(WeekWindow.this, SetTimeWindow.class)
				.putExtra("day", day).putExtra("dayNumber", dayNumber);
		startActivity(intent);
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
		inflater.inflate(R.menu.week_window_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Cursor cursor = ((Cursor) dataList.getItemAtPosition(info.position));
		String name = cursor.getString(cursor.getColumnIndex("title"));
		int dayNumber = (int) dataList.getItemIdAtPosition(info.position);
		selectedItemTitle = name;
		selectedItemPosition = info.position;
		switch (item.getItemId()) {
		case R.id.weekContextDeleteItem:
			showDialog(CLEAR_DAY_DIALOG);
			return true;
		case R.id.weekContextEditItem:
			Intent intent = new Intent(WeekWindow.this, SetTimeWindow.class)
					.putExtra("day", name).putExtra("dayNumber", dayNumber);
			;
			startActivity(intent);
			return true;
		default:
			return false;
		}
	}

	/*
	 * Returns the Cursor to be used when filling rows in ListView. This Cursor
	 * MUST contain columns "_id", "title", "info". Id should be unique for each
	 * row in Cursor.
	 */
	protected Cursor getCursor() {
		Calendar calendar = Calendar.getInstance();
		int firstDay = calendar.getFirstDayOfWeek();
		int dayOfWeek = firstDay;
		String info;
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		MatrixCursor cursor = new MatrixCursor(new String[] { "_id", "title",
				"info" });
		for (int count = 0; count < 7; count++) {
			ArrayList<PillNotification> pillNotifications = db
					.getPillNotificationTable()
					.select(pillNameTitle, dayOfWeek);
			info = getInfoString(pillNotifications);
			cursor.addRow(new String[] { "" + dayOfWeek,
					getDayOfWeekString(dayOfWeek), info });
			dayOfWeek++;
			if (dayOfWeek > 7) {
				dayOfWeek = 1;
			}
		}
		return cursor;
	}

	/*
	 * If user clicks OkButton in DelDialog we should remove selected items from
	 * database
	 */
	private void performClearAllDialogOkPressed() {
		clearAllItems();
		refreshListActivity();
	}

	private void performClearDialogOkPressed(int position) {
		clearItem(position);
		refreshListActivity();
	}
	
	private void performDeleteCourseDialogOkPressed() {
		deleteCourse(pillNameTitle);
		finish();
	}
	
	private void deleteCourse(String courseName) {
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		db.getCourseTable().delete(courseName);
	}

	/* generates info string using data from database */
	private String getInfoString(ArrayList<PillNotification> pillNotificaitons) {
		String infoString = "";
		Calendar timeCalendar = Calendar.getInstance();
		for (PillNotification pillNotification : pillNotificaitons) {
			if (!(infoString.equals(""))) {
				infoString += ", ";
			}
			timeCalendar.setTime(pillNotification.time); // taking time zone
			// into
			// consideration
			infoString += timeFormat.format(timeCalendar.getTime());
		}
		return infoString;
	}

	private String getDayOfWeekString(int dayOfWeekNumber) {
		switch (dayOfWeekNumber) {
		case Calendar.SUNDAY:
			return getString(R.string.Sunday);
		case Calendar.MONDAY:
			return getString(R.string.Monday);
		case Calendar.TUESDAY:
			return getString(R.string.Tuesday);
		case Calendar.WEDNESDAY:
			return getString(R.string.Wednesday);
		case Calendar.THURSDAY:
			return getString(R.string.Thursday);
		case Calendar.FRIDAY:
			return getString(R.string.Friday);
		case Calendar.SATURDAY:
			return getString(R.string.Saturday);
		default:
			return null;
		}
	}

	private void clearAllItems() {
		for (int i = 0; i < dataList.getCount(); i++) {
			clearItem(i);
		}
	}

	private void clearItem(int position) {
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		int dayOfWeek = (int) dataList.getItemIdAtPosition(position);
		db.getPillNotificationTable().delete(pillNameTitle, dayOfWeek);
	}

}
