package blister_pack.blister.windows;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.AdapterView.AdapterContextMenuInfo;
import blister_pack.blister.R;
import blister_pack.blister.database.BlisterDatabase;
import blister_pack.blister.database.tables.PillNotification;

public class SetTimeWindow extends ListWindow {
	
	private static final int ADD_DIRECT_ITEM_DIALOG = 0,
							 DELETE_ALL_DIALOG = 1,
							 DELETE_ITEM_DIALOG = 2,
							 SET_VALUE_DIALOG = 3;
	
	private static final int ADD_REL_TIME_REQUEST_CODE = 0;

	private ArrayList<UserOperation> userOperations = new ArrayList<UserOperation>();
	private ArrayList<PillsTakeTime> dataArrayList;
	private static final String TIME_FORMAT = "HH:mm";
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
	
	LayoutInflater factory;
	Picker picker;
	LinearLayout setValueLayout;
	TimePicker timePicker;
	LinearLayout timePickerLayout;
	
	Button addButton1;
	Button addButton2;
	
	private class UserOperation {
		
		public String operationName;
		public Date time;
		public int pills;
		
		public UserOperation(String operationName, Date time, int pills) {
			this.operationName = operationName;
			this.time = time;
			this.pills = pills;
		}
	}
	
	private class PillsTakeTime implements Comparable<PillsTakeTime> {
		public Date time;
		public int pills;
		
		PillsTakeTime(Date time, int pills) {
			this.time=time;
			this.pills=pills;
		}

		@Override
		public boolean equals(Object object) {
			if (object instanceof PillsTakeTime) {
				PillsTakeTime o = (PillsTakeTime)object;
				return this.time.getTime()==o.time.getTime();
			} else if (object instanceof Date) {
				Date time = (Date)object;
				return (this.time.getTime()==time.getTime());
			}
			return super.equals(object);
		}

		@Override
		public int compareTo(PillsTakeTime another) {
			return (int)(this.time.getTime()-another.time.getTime());
		}
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

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v("eldar", "SetDirectTimeWindow: onCreate");
		super.onCreate(savedInstanceState);

		findViewById(R.id.ListWindowBottomButtonPanel).setVisibility(View.VISIBLE);
		((TextView)findViewById(R.id.ListWindowEmptyListText)).setText(R.string.times_list_is_empty);
		factory = LayoutInflater.from(this);
		
		timePickerLayout = (LinearLayout) factory.inflate(R.layout.add_time_layout, null);
		timePicker = (TimePicker) timePickerLayout.findViewById(R.id.AddDirectTimeTimePicker);
		timePicker.setIs24HourView(true);
		
		setValueLayout = (LinearLayout) factory.inflate(R.layout.set_value_layout, null);
		picker = (Picker)setValueLayout.findViewById(R.id.setValuePicker);
		picker.setValueDiapason(1, Picker.DEFAULT_MAX_VALUE);
		
		okButton.setText(R.string.ok_text);
		TextView tag = (TextView) findViewById(R.id.ListWindowText);
		tag.setText(getIntent().getStringExtra("day"));
		
		addButton1 = (Button)findViewById(R.id.ListWindowEmptyListAddButton1);
		addButton2 = (Button)findViewById(R.id.ListWindowEmptyListAddButton2);
		addButton1.setText(R.string.add_time_text);
		addButton2.setText(R.string.add_time_series_text);

		setButtonListeners();
		
		registerForContextMenu(dataList);
		
		if (savedInstanceState!=null) {
			ArrayList<Date> times = (ArrayList<Date>)savedInstanceState.getSerializable("times");
			ArrayList<Integer> pillNumbers = savedInstanceState.getIntegerArrayList("pills");
			dataArrayList = getDataArrayListFromArrays(times,pillNumbers);
			ArrayList<String> userOperationNames = savedInstanceState.getStringArrayList("user_operation_names");
			ArrayList<Date> userOperationTimes = (ArrayList<Date>)savedInstanceState.getSerializable("user_operation_times");
			ArrayList<Integer> userOperationPills = savedInstanceState.getIntegerArrayList("user_operation_pills");
			userOperations = getUserOperationsFromArrays(userOperationNames,userOperationTimes,userOperationPills);
			int pickerValue = savedInstanceState.getInt("picker_value");
			picker.setValue(pickerValue);
		}

		Log.v("eldar", "SetDirectTimeWindow: created");
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		
		switch (id) {
		case ADD_DIRECT_ITEM_DIALOG:
			Dialog dialog = new AlertDialog.Builder(SetTimeWindow.this)
			.setTitle(R.string.add_time).setView(timePickerLayout)
			.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					int hour = timePicker.getCurrentHour();
					int minute = timePicker.getCurrentMinute();
					performAddDirectTimeDialogOkPressed(hour, minute);
					timePicker.clearFocus();
				}
			})
			.setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					timePicker.clearFocus();
				}
			})
			.create();
			return dialog;
		case DELETE_ITEM_DIALOG:
			return new AlertDialog.Builder(SetTimeWindow.this)
				.setTitle(R.string.delete_item_dialog_title)
				.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						performDeleteDialogOkPressed(selectedItemPosition);
					}
				}).setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
					}
				}).create();
		case DELETE_ALL_DIALOG:
			return new AlertDialog.Builder(SetTimeWindow.this)
				.setTitle(R.string.clear_all_dialog_title)
				.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						performClearDialogOkPressed();
					}
				}).setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
					}
				}).create();
		case SET_VALUE_DIALOG:
			return new AlertDialog.Builder(SetTimeWindow.this)
				.setTitle(R.string.set_value_dialog_title).setView(setValueLayout)
				.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						int value = picker.getValue();
						picker.refresh();
						performSetValueDialogOkPressed(value);
					}
				}).setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						picker.refresh();
					}
				}).create();
				
		default:
			return new AlertDialog.Builder(SetTimeWindow.this)
				.setCancelable(false)
				.setTitle("Sorry, this feature is temporary unavailable")
				.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).create();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.v("eldar","SetTimeWindow: onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putSerializable("times", getTimes());
		outState.putIntegerArrayList("pills", getPills());
		outState.putStringArrayList("user_operation_names", getUserOperationNames());
		outState.putSerializable("user_operation_times", getUserOperationTimes());
		outState.putIntegerArrayList("user_operation_pills", getUserOperationPills());
		outState.putInt("picker_value", picker.getValue());
		Log.v("eldar","SetTimeWindow: instance state saved");
	}

	@Override
	protected void onDestroy() {
		Log.v("eldar", "SetDirectTimeTab: onDestroy");
		super.onDestroy();
		Log.v("eldar", "SetDirectTimeTab: destroyed");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		listMenu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.set_time_window_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.setTimeAddDirectItem:
			performAddTimeAction();
			return true;
		case R.id.setTimeAddRelativeItem:
			performAddTimeSeriesAction();
			return true;
		case R.id.setTimeDeleteAllItem:
			showDialog(DELETE_ALL_DIALOG);
			return true;
		default:
			return false;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == ADD_REL_TIME_REQUEST_CODE) && (resultCode == RESULT_OK)) {
			int everyHour = data.getIntExtra("every_hour", -1);
			int everyMinute = data.getIntExtra("evety_minute", -1);
			int fromHour = data.getIntExtra("from_hour", -1);
			int fromMinute = data.getIntExtra("from_minute", -1);
			int toHour = data.getIntExtra("to_hour", -1);
			int toMinute = data.getIntExtra("to_minute", -1);
			addRelativeItems(everyHour, everyMinute, fromHour, fromMinute, toHour, toMinute);
			refreshListActivity();
		}
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
		inflater.inflate(R.menu.set_time_window_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Cursor cursor = ((Cursor) dataList.getItemAtPosition(info.position));
		String name = cursor.getString(cursor.getColumnIndex("title"));
		selectedItemPosition = info.position;
		selectedItemTitle = name;
		switch (item.getItemId()) {
		case R.id.setTimeContextDeleteItem:
			showDialog(DELETE_ITEM_DIALOG);
			return true;
		case R.id.setTimeContextEditItem:
			showDialog(SET_VALUE_DIALOG);
			return true;
		default:
			return false;
		}
	}
	
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		selectedItemPosition = position;
		Cursor cursor = (Cursor)dataList.getItemAtPosition(position);
		selectedItemTitle = cursor.getString(cursor.getColumnIndex("title"));
		picker.setValue(dataArrayList.get(position).pills);
		showDialog(SET_VALUE_DIALOG);
	}

	public static int getAddDirectTimeDialogId()
	{
		return ADD_DIRECT_ITEM_DIALOG;
	}
	
	private void setButtonListeners() {
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				performOkButtonClick();
			}
		});
		
		addButton1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				performAddTimeAction();
			}
		});
		
		addButton2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				performAddTimeSeriesAction();
			}
		});
	}

	/*
	 * Returns Cursor to be used when filling the ListView. Cursor MUST contain
	 * columns "_id", "title", "info". Info may be simply an empty String (if we
	 * have nothing to describe the information bounded with current time).
	 * Function returns different Cursors depending on the boolean editState.
	 * editState = true means that this Activity was called by clicking
	 * EditButton in WeekWindow. In this case we should get the already set
	 * schedule for current day from database. editState = false means that this
	 * Activity was called by clicking AddButton in WeekWindow. In this case we
	 * should fill Cursor with data that was typed by user in this Activity. See
	 * performAddDialogOkPressed method
	 */
	protected Cursor getCursor() {
		if (dataArrayList == null) {
			dataArrayList = getArrayFromDatabase();
		}
		Collections.sort(dataArrayList);
		MatrixCursor cursor = new MatrixCursor(new String[] { "_id", "title", "info" });
		int i = 0;
		for (PillsTakeTime pillsTakeTime : dataArrayList) {
			cursor.addRow(new String[] { "" + i,
					timeFormat.format(pillsTakeTime.time), getString(R.string.pills_to_take_text)+": "+pillsTakeTime.pills });
			i++;
		}
		return cursor;
	}

	/*
	 * When User clicks OkButton we should store all changes made to the
	 * schedule of current pill. It is stored in String pillNameTitle (variable
	 * of ListWindow class) This method performs before jumping back to
	 * WeekWindow
	 */
	protected void performOkButtonClick() {
		Log.v("eldar", "SetDirectTimeTab: performOkButtonClick");
		saveOperationsToDatabase();
		finish();
		Log.v("eldar", "SetDirectTimeTab: finish");
	}
	
	protected void performAddTimeAction() {
		timePicker.setCurrentHour(0);
		timePicker.setCurrentMinute(0);
		showDialog(ADD_DIRECT_ITEM_DIALOG);
	}
	
	protected void performAddTimeSeriesAction() {
		startActivityForResult(new Intent(this,AddRelativeTimeWindow.class),ADD_REL_TIME_REQUEST_CODE);
	}

	/*
	 * When user clicks OkButton in AddDialog we should add typed time to Cursor
	 * used to view data in ListView. Here hour and minute represent data typed
	 * by user
	 */
	private void performAddDirectTimeDialogOkPressed(int hour, int minute) {
		Log.v("eldar", "SetDirectTime AddDialogOkButtonClick hour=" + hour
				+ " minute=" + minute);
		addNewItem(hour, minute);
		refreshListActivity();
	}

	/*
	 * When user clicks DelButton in DelDialog we should delete all items from
	 * database
	 */
	private void performClearDialogOkPressed() {
		Log.v("eldar", "SetDirectTime DelDialogOkButtonClick");
		deleteAllItems();
		refreshListActivity();
	}

	/*
	 * When user clicks DelButton in DelDialog we should delete selected item
	 * from database
	 */
	private void performDeleteDialogOkPressed(int position) {
		deleteItem(position);
		refreshListActivity();
	}
	
	private void performSetValueDialogOkPressed(int value) {
		updateItem(value);
		refreshListActivity();
	}

	/* loads times from database */
	private ArrayList<PillsTakeTime> getArrayFromDatabase() {

		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		int dayOfWeek = getIntent().getIntExtra("dayNumber", -1);
		ArrayList<PillNotification> pillNotifications = db.getPillNotificationTable().select(pillNameTitle, dayOfWeek);
		ArrayList<PillsTakeTime> result = new ArrayList<PillsTakeTime>();
		for (PillNotification pillNotification : pillNotifications) {
			result.add(new PillsTakeTime(pillNotification.time,pillNotification.pillsToTake));
		}

		return result;
	}

	/* saves all user operations to database */
	private void saveOperationsToDatabase() {
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		int dayOfWeek = getIntent().getIntExtra("dayNumber", -1);
		for (UserOperation userOperation : userOperations) {
			if (userOperation.operationName == "add") {
				db.getPillNotificationTable().insert(pillNameTitle, dayOfWeek, userOperation.time, userOperation.pills);
			} else if (userOperation.operationName == "del") {
				db.getPillNotificationTable().delete(pillNameTitle, dayOfWeek, userOperation.time);
			} else if (userOperation.operationName == "edit") {
				db.getPillNotificationTable().updatePills(pillNameTitle, dayOfWeek, userOperation.time, userOperation.pills);
			}
		}
	}

	/* adds new item (and user operation) */
	private void addNewItem(int hour, int minute) {
		Calendar timeCalendar = Calendar.getInstance();
		long offset = timeCalendar.getTimeZone().getRawOffset(); // time zone offset
		timeCalendar.setTimeInMillis(1000 * 60 * (minute + hour * 60) - offset);

		// checking whether time to add isn't already added
		if (dataArrayList.contains(new PillsTakeTime(timeCalendar.getTime(),1)))
			return;
		userOperations.add(new UserOperation("add", timeCalendar.getTime(),1));
		dataArrayList.add(new PillsTakeTime(timeCalendar.getTime(),1));
	}
	
	private void updateItem(int value) {
			Date time = dataArrayList.get(selectedItemPosition).time;
			PillsTakeTime pillsTakeTime = new PillsTakeTime(time,value);
			dataArrayList.remove(pillsTakeTime);
			dataArrayList.add(pillsTakeTime);
			userOperations.add(new UserOperation("edit",time,value));
	}

	/* adds items (and user operations) */
	private void addRelativeItems(int everyHour, int everyMinute, int fromHour,
			int fromMinute, int toHour, int toMinute) {
		Calendar timeCalendar = Calendar.getInstance();
		long offset = timeCalendar.getTimeZone().getRawOffset(); // time zone offset
		long from = 1000 * 60 * (fromMinute + fromHour * 60) - offset;
		long increment = 1000 * 60 * (everyMinute + everyHour * 60);
		long to = 1000 * 60 * (toMinute + toHour * 60) - offset;
		for (long time = from; time <= to; time += increment) {
			timeCalendar.setTimeInMillis(time);
			PillsTakeTime pillsTakeTime = new PillsTakeTime(timeCalendar.getTime(),1);
			// checking whether time to add isn't already added
			if (!dataArrayList.contains(pillsTakeTime)) {
				
				userOperations.add(new UserOperation("add", timeCalendar
						.getTime(),1));
				dataArrayList.add(pillsTakeTime);
			}
			if (increment==0)
				break;
		}
	}

	/* deletes all items */
	private void deleteAllItems() {
		for (int i = 0; i < dataList.getCount(); i++) {
			deleteItem(i);
		}
	}

	// deletes item specified by <position>
	private void deleteItem(int position) {
		Cursor cursor = (MatrixCursor) dataList.getItemAtPosition(position);
		int timeIndex = cursor.getColumnIndex("title");
		try {
			Date time = timeFormat.parse(cursor.getString(timeIndex));
			userOperations.add(new UserOperation("del", time,0));
			PillsTakeTime pillsTakeTime = new PillsTakeTime(time,1);
			dataArrayList.remove(pillsTakeTime);
		} catch (ParseException e) {
			Log.e("adalx", "SetTime: Invalid time format");
		}
	}
	
	private ArrayList<Date> getTimes() {
		ArrayList<Date> result = new ArrayList<Date>();
		for (PillsTakeTime operation : dataArrayList) {
			result.add(operation.time);
		}
		return result;
	}
	
	private ArrayList<Integer> getPills() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (PillsTakeTime operation : dataArrayList) {
			result.add(operation.pills);
		}
		return result;
	}
	
	private ArrayList<PillsTakeTime> getDataArrayListFromArrays(ArrayList<Date> times, ArrayList<Integer> pillNumbers) {
		ArrayList<PillsTakeTime> result = new ArrayList<PillsTakeTime>();
		for (int i=0; i<times.size(); i++) {
			Date time = times.get(i);
			int pills = pillNumbers.get(i);
			result.add(new PillsTakeTime(time, pills));
		}
		return result;
	}
	
	private ArrayList<String> getUserOperationNames() {
		ArrayList<String> result = new ArrayList<String>();
		for (UserOperation operation : userOperations) {
			result.add(operation.operationName);
		}
		return result;
	}
	
	private ArrayList<Date> getUserOperationTimes() {
		ArrayList<Date> result = new ArrayList<Date>();
		for (UserOperation operation : userOperations) {
			result.add(operation.time);
		}
		return result;
	}
	
	private ArrayList<Integer> getUserOperationPills() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (UserOperation operation : userOperations) {
			result.add(operation.pills);
		}
		return result;
	}
	
	private ArrayList<UserOperation> getUserOperationsFromArrays(ArrayList<String> userOperationNames,
			ArrayList<Date> userOperationArgs, ArrayList<Integer> userOperationPills) {
		ArrayList<UserOperation> result = new ArrayList<UserOperation>();
		for (int i=0; i<userOperationNames.size(); i++) {
			String operationName = userOperationNames.get(i);
			Date operationTime = userOperationArgs.get(i);
			int operationPills = userOperationPills.get(i);
			result.add(new UserOperation(operationName, operationTime, operationPills));
		}
		return result;
	}
}
