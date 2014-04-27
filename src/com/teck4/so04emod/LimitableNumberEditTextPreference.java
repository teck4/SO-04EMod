package com.teck4.so04emod;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LimitableNumberEditTextPreference extends EditTextPreference {

	private Integer mMaxValue;
	private Integer mMinValue;
	private String mOverMessage;
	private String mUnderMessage;
	
	public LimitableNumberEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(context, attrs);
	}
	
	public LimitableNumberEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(context, attrs);
	}
	
	private void setup(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LimitableNumberEditTextPreference, 0, 0);
		mMaxValue = a.getInt(R.styleable.LimitableNumberEditTextPreference_maxValue, Integer.MAX_VALUE);
		mMinValue = a.getInt(R.styleable.LimitableNumberEditTextPreference_minValue, Integer.MIN_VALUE);
		mOverMessage = a.getString(R.styleable.LimitableNumberEditTextPreference_overMessage);
		mUnderMessage = a.getString(R.styleable.LimitableNumberEditTextPreference_underMessage);
		getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

		a.recycle();
	}

	private void updateOKButton(String str) {

		Dialog dialog = getDialog();
		if (dialog != null) {
			Button okButton = (Button)dialog.findViewById(android.R.id.button1);
			
			if(str.length() == 0){
				okButton.setEnabled(true);
				return;					
			}
			
			Integer value;
			try {
				value = Integer.parseInt(str);
			} catch (Exception e) {
				okButton.setEnabled(false);
				return;
			}

			if (value > mMaxValue){
				okButton.setEnabled(false);
				if (mOverMessage != null) {
					getEditText().setError(mOverMessage);
				}
			} else if (value < mMinValue) {
				okButton.setEnabled(false);
				if (mUnderMessage != null) {
					getEditText().setError(mUnderMessage);
				}
			} else {
				okButton.setEnabled(true);
			}
		}
	}

	private TextWatcher textChangeListener = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			updateOKButton(s.toString());
		}
	 
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	    }

		@Override
		public void afterTextChanged(Editable s) {
		}
		
	};


	@Override
	protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
		super.onAddEditTextToDialogView(dialogView, editText);
		editText.addTextChangedListener(textChangeListener);
	}
	
	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);
		updateOKButton(getText());
	}
	
}
