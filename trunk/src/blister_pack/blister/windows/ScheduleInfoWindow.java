package blister_pack.blister.windows;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import blister_pack.blister.R;
import blister_pack.blister.database.BlisterDatabase;
import blister_pack.blister.database.tables.Course;

public class ScheduleInfoWindow extends Activity {
	private static final int PERFORM_ACTION_SUCCESSFUL = 0;
	private static final int PERFORM_ACTION_ERROR = -1;

	private static final int NAME_NOT_TYPED_DIALOG = 1;
	private static final int DATA_NOT_TYPED_DIALOG = 2;
	private static final int INCORRECT_DATA_DIALOG = 3;
	private static final int NAME_ALREADY_EXISTS_DIALOG = 4;
	private static final int SET_VALUE_DIALOG = 5;

	private static final int PARSE_DATA_OK = 0;

	private boolean newScheduleState;
	
	private boolean setValueDialogShowing;
	
	private String pillNameTitle;
	
	int numberOfPills;
	int durationOfCourse;

	EditText nameEditText;

	Button okButton;
	LinearLayout setValueLayout;
	LinearLayout setValueDialogLayout;
	Picker picker;
	TextView valueText;
	Spinner extraParamSpinner;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case NAME_NOT_TYPED_DIALOG:
			return new AlertDialog.Builder(ScheduleInfoWindow.this)
				.setCancelable(false)
				.setTitle(R.string.name_not_typed_dialog_message)
				.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
					}
				}).create();
		case DATA_NOT_TYPED_DIALOG:
			return new AlertDialog.Builder(ScheduleInfoWindow.this)
				.setCancelable(false)
				.setTitle(R.string.data_not_typed_dialog_message)
				.setPositiveButton(R.string.ok_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
				}).create();
		case INCORRECT_DATA_DIALOG:
			return new AlertDialog.Builder(ScheduleInfoWindow.this)
			.setCancelable(false)	
			.setTitle(R.string.incorrect_data_dialog_message)
				.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
					}
				}).create();
		case NAME_ALREADY_EXISTS_DIALOG:
			return new AlertDialog.Builder(ScheduleInfoWindow.this)
				.setCancelable(false)
				.setTitle(R.string.name_already_exists_dialog_message)
				.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
					}
				}).create();
		case SET_VALUE_DIALOG:
			return new AlertDialog.Builder(ScheduleInfoWindow.this)
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
		}
		return null;
	}

	@Override
	protected void onCreate(Bundle bundle) {
		
		Log.v("eldar", "ScheduleInfoWindow: onCreate");
		Intent intent = getIntent();
		newScheduleState = intent.getBooleanExtra("newScheduleState", false);

		super.onCreate(bundle);
		int orientation=this.getResources().getConfiguration().orientation;
		if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			
			setContentView(R.layout.edit_schedule_window);
		} else {
			setContentView(R.layout.edit_schedule_window_vertical);
		}

		((TextView)findViewById(R.id.editScheduleText)).setText(R.string.edit_schedule_window_title);
		nameEditText = (EditText) findViewById(R.id.editScheduleEditName);
		okButton = (Button) findViewById(R.id.editScheduleOKButton);
		setValueLayout = (LinearLayout)findViewById(R.id.editScheduleValueLayout);
		valueText = (TextView)findViewById(R.id.editScheduleValueText);
		extraParamSpinner = (Spinner) findViewById(R.id.editScheduleSpinner);
		LayoutInflater factory = LayoutInflater.from(this);
		setValueDialogLayout= (LinearLayout)factory.inflate(R.layout.set_value_layout, null);
		picker = (Picker)setValueDialogLayout.findViewById(R.id.setValuePicker);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.extra_params,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		extraParamSpinner.setAdapter(adapter);
		
		

		if (newScheduleState) {
			okButton.setText(R.string.ok_text);
		} else {
			findViewById(R.id.editScheduleTopPanel).setVisibility(View.GONE);
			okButton.setText(R.string.save_text);
		}		

		setButtonListeners();
		
		if (bundle!=null) {
			initValuesFromBundle(bundle);
			if (setValueDialogShowing) {
				showDialog(SET_VALUE_DIALOG);
			}
		}

		Log.v("eldar", "ScheduleInfoWindow: created");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (pillNameTitle == null) {
			if (!newScheduleState) {
				pillNameTitle = getIntent().getStringExtra("pillName");
				numberOfPills = getInitialNumberOfPills(pillNameTitle);
				durationOfCourse = getInitialDurationOfCourse(pillNameTitle);
				if (durationOfCourse > 0) {
					extraParamSpinner.setSelection(1);
					valueText.setText(parseIntToString(durationOfCourse));
				} else if (numberOfPills > 0) {
					extraParamSpinner.setSelection(0);
					valueText.setText(parseIntToString(numberOfPills));
				} else
					valueText.setText(R.string.number_preset);
			} else {
				valueText.setText(R.string.number_preset);
			}
			nameEditText.setText(pillNameTitle);
		}
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.v("eldar","ScheduleInfoWindow: onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putString("pill_name",pillNameTitle);
		outState.putInt("number_of_pills", numberOfPills);
		outState.putInt("duration_of_course", durationOfCourse);
		outState.putInt("spinner_selection", extraParamSpinner.getSelectedItemPosition());
		outState.putString("value_text", valueText.getText().toString());
		outState.putBoolean("set_value_dialog_showing", setValueDialogShowing);
		outState.putInt("picker_value", picker.getValue());
		Log.v("eldar","ScheduleInfoWindow: instance state saved");
	}

	protected void initValuesFromBundle(Bundle bundle) {
		pillNameTitle=bundle.getString("pill_name");
		numberOfPills=bundle.getInt("number_of_pills");
		durationOfCourse=bundle.getInt("duration_of_course");
		extraParamSpinner.setSelection(bundle.getInt("spinner_selection"));
		valueText.setText(bundle.getString("value_text"));
		setValueDialogShowing=bundle.getBoolean("set_value_dialog_showing");
		picker.setValue(bundle.getInt("picker_value"));
	}

	@Override
	protected void onDestroy() {
		Log.v("eldar", "ScheduleInfoWindow: onDestroy");
		super.onDestroy();
		Log.v("eldar", "ScheduleInfoWindow: destroyed");
	}

	private void setButtonListeners() {
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				performOkButtonClick();
			}
		});

		setValueLayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				performSetValueButtonClick();
			}
		});
	}

	/*
	 * Runs when user clicks on the OkButton we should store the information
	 * typed by user use methods getNumberOfPills, getDurationOfCourse,
	 * getPillName
	 */
	private void performOkButtonClick() {
		Log.v("eldar", "ScheduleInfoWindow: performOkButtonClick");
		int dialogId = parseData();
		if (dialogId == PARSE_DATA_OK) {
			if (insertOrUpdate() == PERFORM_ACTION_SUCCESSFUL) {
				if (newScheduleState) {
					Intent intent = new Intent(ScheduleInfoWindow.this,
							WeekWindow.class)
						.putExtra("newScheduleState", newScheduleState)
						.putExtra("pillName", getPillName());
					startActivity(intent);
					finish();
				} else {
					pillNameTitle = getPillName();
					Intent intent = new Intent(ScheduleInfoWindow.this,
							EditScheduleWindow.class)
						.putExtra("pillName", pillNameTitle)
						.putExtra("rebuildState", true);
					startActivity(intent);
					finish();
				}
			} else {
				showDialog(NAME_ALREADY_EXISTS_DIALOG);
			}
		} else {
			showDialog(dialogId);
		}
	}

	protected void performSetValueButtonClick() {
		setValueDialogShowing=true;
		int value;
		try {
			value = Integer.parseInt(valueText.getText().toString());
		} catch (NumberFormatException e) {
			value = 0;
		}
		picker.setValue(value);
		showDialog(SET_VALUE_DIALOG);
	}

	protected void performSetValueDialogOkPressed(int value) {
		setValueDialogShowing=false;
		if (value > 0)
			valueText.setText(parseIntToString(value));
		else
			valueText.setText(R.string.number_preset);
	}
	
	protected void performSetValueDialogCancelPressed() {
		setValueDialogShowing=false;
	}

	/* Returns String typed by user */
	private String getPillName() {
		return nameEditText.getText().toString();
	}

	/*
	 * Returns integer typed by user, NO_DATA_TYPED if user typed nothing,
	 * NUMBER_FORMAT_ERROR if user typed not a number
	 */
	private int getNumberOfPills() {
		if (extraParamSpinner.getSelectedItem().equals(
				getString(R.string.new_schedule_pills_text))) {
			try {
				int result = Integer.parseInt(valueText.getText()
						.toString());
				return result;
			} catch (NumberFormatException e) {
				return Picker.NO_DATA_TYPED;
			}
		}
		return Picker.NO_DATA_TYPED;
	}

	/*
	 * Returns integer typed by user, NO_DATA_TYPED if user typed nothing,
	 * NUMBER_FORMAT_ERROR if user typed not a number
	 */
	private int getDurationOfCourse() {
		if (extraParamSpinner.getSelectedItem().equals(
				getString(R.string.new_schedule_duration_text))) {
			try {
				int result = Integer.parseInt(valueText.getText()
						.toString());
				return result;
			} catch (NumberFormatException e) {
				return Picker.NO_DATA_TYPED;
			}
		}
		return Picker.NO_DATA_TYPED;
	}

	private int getInitialNumberOfPills(String pillName) {
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		Course course = db.getCourseTable().select(pillName);
		return course.pillsRemained;
	}

	private int getInitialDurationOfCourse(String pillName) {
		BlisterDatabase db = BlisterDatabase.openDatabase(this);
		Course course = db.getCourseTable().select(pillName);
		Calendar currentDate = Calendar.getInstance();
		Calendar startDate = Calendar.getInstance();
		startDate.setTime(course.startDate);
		final int millisecondsInDay = 24 * 3600 * 1000;
		int diff = (int) (currentDate.getTimeInMillis() - startDate
				.getTimeInMillis())
				/ millisecondsInDay;
		int duration = course.duration - diff;
		return duration;
	}

	private int insertOrUpdate() {
		BlisterDatabase db = BlisterDatabase
				.openDatabase(ScheduleInfoWindow.this);
		if (newScheduleState) {
			if (db.getCourseTable().insert(getPillName(), new Date(),
					getDurationOfCourse(), getNumberOfPills()) == -1) {
				return PERFORM_ACTION_ERROR;
			}
		} else {
			if (db.getCourseTable().update(pillNameTitle, getPillName(),
					new Date(), getDurationOfCourse(), getNumberOfPills()) == 0) {
				return PERFORM_ACTION_ERROR;
			}
		}
		return PERFORM_ACTION_SUCCESSFUL;
	}
	
	private String parseIntToString(int number)
	{
		if (number<10) {
			return ("0"+Integer.toString(number));
		} else {
			return(Integer.toString(number));
		}
	}

	private int parseData() {
		String name = getPillName();
		int numberOfPills = getNumberOfPills();
		int durationOfCourse = getDurationOfCourse();
		if (name.length() == 0) {
			return NAME_NOT_TYPED_DIALOG;
		} else if ((numberOfPills == Picker.NO_DATA_TYPED)
				&& (durationOfCourse == Picker.NO_DATA_TYPED)) {
			return DATA_NOT_TYPED_DIALOG;
		} else if (numberOfPills == Picker.NUMBER_FORMAT_ERROR
				|| (durationOfCourse == Picker.NUMBER_FORMAT_ERROR)) {
			return INCORRECT_DATA_DIALOG;
		} else
			return PARSE_DATA_OK;
	}

}
