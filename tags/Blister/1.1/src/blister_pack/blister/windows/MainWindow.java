package blister_pack.blister.windows;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import blister_pack.blister.NotificationService;
import blister_pack.blister.R;
import blister_pack.blister.database.BlisterDatabase;
import blister_pack.blister.database.tables.Course;

public class MainWindow extends ListWindow {

	private static final int CLEAR_ALL_DIALOG = 0;
	private static final int DELETE_ITEM_DIALOG = 1;
	
	Button addButton1;
	Button addButton2;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case CLEAR_ALL_DIALOG:
			return new AlertDialog.Builder(MainWindow.this).setTitle(
					R.string.clear_all_dialog_message).setPositiveButton(
					R.string.ok_text, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							performClearDialogOkPressed();
						}
					}).setNegativeButton(R.string.cancel_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					}).create();
		case DELETE_ITEM_DIALOG:
			return new AlertDialog.Builder(MainWindow.this).setTitle(
					R.string.delete_item_dialog_message).setPositiveButton(
					R.string.ok_text, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							performDeleteDialogOkPressed(selectedItemPosition);
						}
					}).setNegativeButton(R.string.cancel_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					}).create();
		}
		return null;
	}

	protected int getListItemID() {
		return R.layout.basic_list_item;
	}

	protected int getItemTitleID() {
		return R.id.basicListItemTitle;
	}

	protected int getItemStatID() {
		return R.id.basicListItemStat;
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		stopService(new Intent(this, NotificationService.class));
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancelAll();
		
		addButton1 = (Button)findViewById(R.id.ListWindowEmptyListAddButton1);
		addButton2 = (Button)findViewById(R.id.ListWindowEmptyListAddButton2);
		addButton1.setText(R.string.add_text);
		addButton2.setVisibility(View.GONE);
		
		((TextView)findViewById(R.id.ListWindowText)).setText(R.string.main_window_title);
		((TextView)findViewById(R.id.ListWindowEmptyListText)).setText(R.string.schedules_list_is_empty);
		registerForContextMenu(dataList);
		
		setButtonListeners();

	}

	@Override
	protected void onDestroy() {
		startService(new Intent(this, NotificationService.class));
		super.onDestroy();
	}
	
	private void setButtonListeners() {
		addButton1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				performAddAction();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		listMenu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_window_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mainAddItem:
			performAddAction();
			return true;
		case R.id.mainClearItem:
			showDialog(CLEAR_ALL_DIALOG);
			return true;
		case R.id.mainAboutItem:
			// TODO
			return true;
		default:
			return false;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Cursor cursor = ((Cursor) dataList.getItemAtPosition(position));
		String name = cursor.getString(cursor.getColumnIndex("title"));
		pillNameTitle = name;
		Intent intent = new Intent(MainWindow.this, EditScheduleWindow.class)
				.putExtra("pillName", name);
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
		inflater.inflate(R.menu.main_window_context_menu, menu);
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
		case R.id.mainContextDeleteItem:
			showDialog(DELETE_ITEM_DIALOG);
			return true;
		case R.id.mainContextEditItem:
			Intent intent = new Intent(MainWindow.this,
					EditScheduleWindow.class).putExtra("pillName", name);
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
		BlisterDatabase database = BlisterDatabase.openDatabase(this);
		ArrayList<Course> allCourses = database.getCourseTable().selectAll();
		MatrixCursor cursor = new MatrixCursor(new String[] { "_id", "title", "info" });
		for (int i = 0; i < allCourses.size(); i++) {
			Course course = allCourses.get(i);
			cursor.addRow(new String[] { "" + i, course.courseName,
					getInfoString(course) });
		}
		return cursor;
	}

	/*
	 * If user clicks OK button in ClearDialog we should remove all items from
	 * database
	 */
	private void performClearDialogOkPressed() {
		deleteAllItems();
		refreshListActivity();
	}

	/*
	 * If user clicks OK button in DeleteDialog we should remove selected item
	 * from database
	 */
	private void performDeleteDialogOkPressed(int position) {
		deleteItem(position);
		refreshListActivity();
	}
	
	private void performAddAction() {
		Intent intent = new Intent(MainWindow.this,
				ScheduleInfoWindow.class)
				.putExtra("newScheduleState", true);
		startActivity(intent);
	}

	private void deleteAllItems() {
		for (int i = 0; i < dataList.getCount(); i++) {
			deleteItem(i);
		}
	}

	private void deleteItem(int position) {
		Cursor cursor = (Cursor) dataList.getItemAtPosition(position);
		String name = cursor.getString(cursor.getColumnIndex("title"));
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		db.getCourseTable().delete(name);
	}

	private String getInfoString(Course course) {
		String info = "";
		Calendar currentDate = Calendar.getInstance();
		Calendar startDate = Calendar.getInstance();
		startDate.setTime(course.startDate);
		final int millisecondsInDay = 24 * 3600 * 1000;
		int diff = (int) (currentDate.getTimeInMillis() - startDate
				.getTimeInMillis())
				/ millisecondsInDay;
		int daysRemained = course.duration - diff;
		if (course.pillsRemained > 0)
			info += getString(R.string.pills_remained_text) + ": "
					+ course.pillsRemained;
		if (daysRemained > 0) {
			if (course.pillsRemained > 0)
				info += "\n";
			info += getString(R.string.days_remained_text) + ": "
					+ daysRemained;
		}
		if (info == "")
			return "<" + getString(R.string.course_ended_text) + ">";
		return info;
	}
}
