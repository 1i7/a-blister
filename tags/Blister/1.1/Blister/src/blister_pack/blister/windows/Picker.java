package blister_pack.blister.windows;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import blister_pack.blister.R;

public class Picker extends FrameLayout {
	public static final int DEFAULT_MIN_VALUE = 0;
	public static final int DEFAULT_MAX_VALUE = 999;
	public static final int NO_DATA_TYPED = -1;
	public static final int NUMBER_FORMAT_ERROR = -2;

	private LinearLayout pickerLayout;

	private Button plusButton;
	private Button minusButton;
	private EditText editText;
	
	private int minValue=DEFAULT_MIN_VALUE;
	private int maxValue=DEFAULT_MAX_VALUE;

	public Picker(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		LayoutInflater factory = LayoutInflater.from(context);
		pickerLayout = (LinearLayout) factory.inflate(R.layout.picker_layout, null);
		this.addView(pickerLayout);
		plusButton = (Button) pickerLayout.findViewById(R.id.PickerPlusButton);
		editText = (EditText) pickerLayout.findViewById(R.id.PickerEditText);
		minusButton = (Button) pickerLayout.findViewById(R.id.PickerMinusButton);

		editText.setText(convertIntToString(minValue));
		setListeners();
	}

	private void setListeners() {
		plusButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				editText.setFocusable(false);
				int value = getValue();
				setValue(value+1);
			}
		});
		minusButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				editText.setFocusable(false);
				int value = getValue();
				setValue(value-1);
			}
		});
		editText.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				editText.setFocusable(true);
				editText.setFocusableInTouchMode(true);
				editText.requestFocus();
			}
		});
	}

	private String convertIntToString(int value) {
		int correctValue=getCorrectValue(value);
		if (correctValue < 10) {
			return ("0" + Integer.toString(correctValue));
		} else {
			return Integer.toString(correctValue);
		}
	}

	public int getValue() {
		try {
			String text = editText.getText().toString();
			if (text.length() == 0) {
				return NUMBER_FORMAT_ERROR;
			} else {
				return getCorrectValue(Integer.parseInt(editText.getText().toString()));
			}
		} catch (NumberFormatException e) {
			return NUMBER_FORMAT_ERROR;
		}
	}

	public void setValue(int value) {
		editText.setText(convertIntToString(value));
	}
	
	public void setValueDiapason(int minValue, int maxValue) {
		if (minValue>maxValue) 
			return;
		this.minValue=minValue;
		this.maxValue=maxValue;
		refresh();
	}
	
	private int getCorrectValue(int value) {
		if (value>maxValue)
			return maxValue;
		else if (value<minValue)
			return minValue;
		else
			return value;		
	}
	
	public void refresh() {
		editText.setFocusable(false);
		editText.setText(convertIntToString(getValue()));
	}

}
