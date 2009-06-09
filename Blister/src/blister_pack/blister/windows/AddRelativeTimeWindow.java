package blister_pack.blister.windows;

import blister_pack.blister.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class AddRelativeTimeWindow extends Activity {
	
	public static final int ADD_DIRECT_TIME_DIALOG = 0,
							INCORRECT_DATA_DIALOG = 1;
	
	public static final int SET_EVERY_TIME = 0, 
							SET_FROM_TIME = 1, 
							SET_TO_TIME = 2;

	TextView everyTextView, fromTextView, toTextView;
	
	int everyHour, everyMinute, fromHour, fromMinute, toHour, toMinute;
	
	Button okButton, cancelButton;
	
	TimePicker timePicker;
	LinearLayout timePickerLayout;
	
	AlertDialog addDirectTimeDialog;
	
	LayoutInflater factory;
	
	private int setRelativeTimeIndex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v("eldar", "AddRelativeTimeWindow: onCreate");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_rel_time_window);
		
		everyTextView = (TextView)findViewById(R.id.AddRelTimeEverySetPanel);
		fromTextView = (TextView)findViewById(R.id.AddRelTimeFromSetPanel);
		toTextView = (TextView)findViewById(R.id.AddRelTimeToSetPanel);
		
		okButton = (Button)findViewById(R.id.AddRelTimeOkButton);
		cancelButton = (Button)findViewById(R.id.AddRelTimeCancelButton);
		
		factory = LayoutInflater.from(this);
		
		timePickerLayout = (LinearLayout) factory.inflate(R.layout.add_time_layout, null);
		timePicker = (TimePicker) timePickerLayout.findViewById(R.id.AddDirectTimeTimePicker);
		timePicker.setIs24HourView(true);
		
		setOnClickListeners();
		
		if (savedInstanceState!=null) {
			everyHour = savedInstanceState.getInt("every_hour");
			everyMinute = savedInstanceState.getInt("evety_minute");
			fromHour = savedInstanceState.getInt("from_hour");
			fromMinute = savedInstanceState.getInt("from_minute");
			toHour = savedInstanceState.getInt("to_hour");
			toMinute = savedInstanceState.getInt("to_minute");
			String everyText = savedInstanceState.getString("every_text");
			String fromText = savedInstanceState.getString("from_text");
			String toText = savedInstanceState.getString("to_text");
			everyTextView.setText(everyText);
			fromTextView.setText(fromText);
			toTextView.setText(toText);
			setRelativeTimeIndex = savedInstanceState.getInt("set_relative_time_index");
		} else {
			everyTextView.setText(R.string.time_preset);
			fromTextView.setText(R.string.time_preset);
			toTextView.setText(R.string.time_preset);
		}
		
		Log.v("eldar", "AddRelativeTimeWindow: created");
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case ADD_DIRECT_TIME_DIALOG:
			addDirectTimeDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.add_time_text).setView(timePickerLayout)
				.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						int hour = timePicker.getCurrentHour();
						int minute = timePicker.getCurrentMinute();
						performAddDirectTimeDialogOkPressed(hour, minute);
					}
				})
				.setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						performAddDirectTimeDialogCancelPressed();
					}
				})
				.create();
			addDirectTimeDialog.setTitle(getDialogTitle());
			return addDirectTimeDialog;
		case INCORRECT_DATA_DIALOG:
			return new AlertDialog.Builder(this)
				.setTitle(R.string.incorrect_data_dialog_message)
				.setCancelable(false)
				.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).create();
		default:
			return null;
		}
	}

	@Override
	protected void onDestroy() {
		Log.v("eldar", "AddRelativeTimeWindow: onDestroy");
		super.onDestroy();
		Log.v("eldar", "AddRelativeTimeWindow: destroyed");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.v("eldar","AddRelativeTimeWindow: onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putInt("every_hour", everyHour);
		outState.putInt("evety_minute", everyMinute);
		outState.putInt("from_hour", fromHour);
		outState.putInt("from_minute", fromMinute);
		outState.putInt("to_hour", toHour);
		outState.putInt("to_minute", toMinute);
		outState.putString("every_text", everyTextView.getText().toString());
		outState.putString("from_text", fromTextView.getText().toString());
		outState.putString("to_text", toTextView.getText().toString());
		outState.putInt("set_relative_time_index", setRelativeTimeIndex);
		Log.v("eldar","AddRelativeTimeWindow: instance state saved");
	}
	
	private void setOnClickListeners() {
		everyTextView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setRelativeTimeIndex = SET_EVERY_TIME;
				timePicker.setCurrentHour(everyHour);
				timePicker.setCurrentMinute(everyMinute);
				if (addDirectTimeDialog!=null)
					addDirectTimeDialog.setTitle(getDialogTitle());
				showDialog(ADD_DIRECT_TIME_DIALOG);
			}
		});
		fromTextView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setRelativeTimeIndex = SET_FROM_TIME;
				timePicker.setCurrentHour(fromHour);
				timePicker.setCurrentMinute(fromMinute);
				if (addDirectTimeDialog!=null)
					addDirectTimeDialog.setTitle(getDialogTitle());
				showDialog(ADD_DIRECT_TIME_DIALOG);
			}
		});
		toTextView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setRelativeTimeIndex = SET_TO_TIME;
				timePicker.setCurrentHour(toHour);
				timePicker.setCurrentMinute(toMinute);
				if (addDirectTimeDialog!=null)
					addDirectTimeDialog.setTitle(getDialogTitle());
				showDialog(ADD_DIRECT_TIME_DIALOG);
			}
		});
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
	}
	
	private void performOkButtonClick() {
		if (isCorrectData()) {
			Intent intent = getIntent();
			intent.putExtra("every_hour", everyHour)
				.putExtra("evety_minute", everyMinute)
				.putExtra("from_hour", fromHour)
				.putExtra("from_minute", fromMinute)
				.putExtra("to_hour", toHour)
				.putExtra("to_minute", toMinute);
			setResult(RESULT_OK, intent);
			finish();
		} else {
			showDialog(INCORRECT_DATA_DIALOG);
		}
		
	}
	
	private void performCancelButtonClick() {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	private void performAddDirectTimeDialogOkPressed(int hour, int minute) {
		switch (setRelativeTimeIndex) {
		case SET_EVERY_TIME:
			everyHour = hour;
			everyMinute = minute;
			everyTextView.setText(convertIntToString(everyHour)+":"+convertIntToString(everyMinute));
			break;
		case SET_FROM_TIME:
			fromHour = hour;
			fromMinute = minute;
			fromTextView.setText(convertIntToString(fromHour)+":"+convertIntToString(fromMinute));
			break;
		case SET_TO_TIME:
			toHour = hour;
			toMinute = minute;
			toTextView.setText(convertIntToString(toHour)+":"+convertIntToString(toMinute));
		}
		timePicker.clearFocus();
	}
	
	private void performAddDirectTimeDialogCancelPressed() {
		timePicker.clearFocus();
	}
	
	private String getDialogTitle() {
		switch (setRelativeTimeIndex) {
		case SET_EVERY_TIME:
			return getString(R.string.time_interval_text);
		case SET_FROM_TIME:
			return getString(R.string.start_time_text);
		case SET_TO_TIME:
			return getString(R.string.end_time_text);
		default:
			return getString(R.string.add_time_text);
		}
	}
	
	private String convertIntToString(int number)
	{
		if (number<10) {
			return ("0"+Integer.toString(number));
		} else {
			return(Integer.toString(number));
		}
	}
	
	private boolean isCorrectData()
	{
		String every = everyTextView.getText().toString();
		String from = fromTextView.getText().toString();
		String to = toTextView.getText().toString();
		if (isTimeTextPreset(every)||
			isTimeTextPreset(from)||isTimeTextPreset(to)) {
			return false;
		} else return true;
	}
	
	private boolean isTimeTextPreset(String string)
	{
		if (string==getString(R.string.time_preset)) {
			return true;
		} else return false;
	}
	
}
