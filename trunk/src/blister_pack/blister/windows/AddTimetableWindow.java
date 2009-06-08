package blister_pack.blister.windows;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import blister_pack.blister.R;

public class AddTimetableWindow extends ListActivity {
	
	private static final int 	SET_NUMBER_DIALOG = 0,
								SET_TIME_DIALOG = 1,
								NO_DATA_TYPED = 2;
	
	ListView dataList;
	Cursor dataCursor;
	String pillNameTitle;
	
	LinearLayout setValueDialogLayout;
	Picker picker;
	TimePicker timePicker;
	LinearLayout timePickerLayout;
	
	Button setTimeButton;
	Button setNumberButton;
	Button okButton;
	Button cancelButton;
	
	TextView numberValueText;
	TextView timeValueText;
	
	int hour;
	int minute;
	
	private boolean setValueDialogShowing;
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SET_NUMBER_DIALOG:
			return new AlertDialog.Builder(AddTimetableWindow.this)
				.setTitle(R.string.set_value_dialog_title).setView(setValueDialogLayout)
				.setPositiveButton(R.string.ok_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							int value = picker.getValue();
							picker.refresh();
							performSetValueDialogOkPressed(value);
						}
				}).setNegativeButton(R.string.cancel_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							picker.refresh();
							performSetValueDialogCancelPressed();
						}
				}).create();
		case SET_TIME_DIALOG:
			return new AlertDialog.Builder(AddTimetableWindow.this)
			.setTitle(R.string.add_time).setView(timePickerLayout)
			.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					hour = timePicker.getCurrentHour();
					minute = timePicker.getCurrentMinute();
					performSetTimeDialogOkPressed(hour, minute);
					timePicker.clearFocus();
				}
			})
			.setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					timePicker.clearFocus();
				}
			})
			.create();
		case NO_DATA_TYPED:
			return new AlertDialog.Builder(AddTimetableWindow.this)
			.setCancelable(false)
			.setTitle(R.string.add_timetable_no_data_message)
			.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			})
			.create();
		}
		return null;
		}
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		Log.v("eldar",""+this.getClass().getName()+" onCreate");
		
		Intent intent = getIntent();
		pillNameTitle = intent.getStringExtra("pillName");
		
		setContentView(R.layout.add_timetable_window);
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		LayoutInflater factory = LayoutInflater.from(this);
		setValueDialogLayout= (LinearLayout)factory.inflate(R.layout.set_value_layout, null);
		picker = (Picker)setValueDialogLayout.findViewById(R.id.setValuePicker);
		timePickerLayout = (LinearLayout) factory.inflate(R.layout.add_time_layout, null);
		timePicker = (TimePicker) timePickerLayout.findViewById(R.id.AddDirectTimeTimePicker);
		timePicker.setIs24HourView(true);
		
		numberValueText = (TextView)findViewById(R.id.addTimetableNumberValueText);
		timeValueText = (TextView)findViewById(R.id.addTimetableTimeValueText);
		
		setNumberButton = (Button)findViewById(R.id.addTimetableNumberSetButton);
		setTimeButton = (Button)findViewById(R.id.addTimetableTimeSetButton);
		okButton = (Button)findViewById(R.id.addTimetableOkButton);
		cancelButton = (Button)findViewById(R.id.addTimetableCancelButton);
		
		setButtonListeners();
		
		dataCursor = getCursor();
		ListAdapter adapter = new DataCursorAdapter(this,
				R.layout.add_timetable_list_item, dataCursor,
                        new String[] {"title"}, 
                        new int[] {R.id.addTimetableListItemText});
        setListAdapter(adapter);
        
        Log.v("eldar",""+this.getClass().getName()+" created");
	}
	
	protected Cursor getCursor() {
		Calendar calendar = Calendar.getInstance();
		int firstDay = calendar.getFirstDayOfWeek();
		int dayOfWeek = firstDay;
		MatrixCursor cursor = new MatrixCursor(new String[] { "_id", "title"});
		for (int count = 0; count < 7; count++) {
			cursor.addRow(new String[] { "" + dayOfWeek,
					getDayOfWeekString(dayOfWeek)});
			dayOfWeek++;
			if (dayOfWeek > 7) {
				dayOfWeek = 1;
			}
		}
		return cursor;
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
	
	private void setButtonListeners() {
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				performOkButtonClick();
			}
		});
		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				performCancelButtonClick();
			}
		});
		setTimeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				performSetTimeButtonClick();
			}
		});
		setNumberButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				performSetNumberButtonClick();
			}
		});
	}
	
	private void performOkButtonClick() {
		if (isCorrectData()) {
		//TODO
		} else showDialog(NO_DATA_TYPED);

	}
	
	private void performCancelButtonClick() {
		finish();
	}
	
	private void performSetTimeButtonClick() {
		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(minute);
		showDialog(SET_TIME_DIALOG);
	}
	
	private void performSetNumberButtonClick() {
		setValueDialogShowing=true;
		int value;
		try {
			value = Integer.parseInt(numberValueText.getText().toString());
		} catch (NumberFormatException e) {
			value = 0;
		}
		picker.setValue(value);
		showDialog(SET_NUMBER_DIALOG);
	}
	
	private void performSetValueDialogOkPressed(int value) {
		setValueDialogShowing=false;
		if (value > 0)
			numberValueText.setText(parseIntToString(value));
		else
			numberValueText.setText(R.string.number_preset);
	}
	
	private void performSetValueDialogCancelPressed() {
		setValueDialogShowing=false;
	}
	
	private void performSetTimeDialogOkPressed(int hour, int minute) {
		timeValueText.setText(parseIntToString(hour)+":"+parseIntToString(minute));
	}
	
	private String parseIntToString(int number)
	{
		if (number<10) {
			return ("0"+Integer.toString(number));
		} else {
			return(Integer.toString(number));
		}
	}
	
	private boolean isCorrectData()
	{
		String time = timeValueText.getText().toString();
		String number = numberValueText.getText().toString();
		
		if ((time==getString(R.string.time_preset))||
				(number==getString(R.string.number_preset))) {
			return false;
		} else return true;
	}

}
