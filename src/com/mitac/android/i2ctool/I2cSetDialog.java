package com.mitac.android.i2ctool;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.Toast;

public class I2cSetDialog extends DialogFragment {
    
    private static final String TAG = "i2c_I2cSetDialog";
    
    private int mDataAddr = 0;
    private int mDataValue = 0;
    private int mI2cSetMode = 0;
    
    EditText mDataAddrEt;
    EditText mDataValueEt;
    
    private OnSetClickListener mOnSetClickListener;
    
    public interface OnSetClickListener {
        void onSetClick(int dataAddress, int value, String mode);            
    }
    
    public void setOnSetClickListener(OnSetClickListener l) {
        mOnSetClickListener = l;
    }
    
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        View view = inflater.inflate(R.layout.dialog_i2c_set, null);
        builder.setView(view)
            .setPositiveButton(R.string.i2c_set, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mOnSetClickListener != null) {
                        String mode = I2cInfo.getI2cSetMode(mI2cSetMode);
                        mDataAddr = Integer.valueOf(mDataAddrEt.getText().toString(), 16);
                        mDataValue = Integer.valueOf(mDataValueEt.getText().toString(), 16);
                        mOnSetClickListener.onSetClick(mDataAddr, mDataValue, mode);
                        I2cInfo.saveI2cSetToPref(getActivity(), mDataAddr, mDataValue, mI2cSetMode);
                    }
                }
            })
            .setNegativeButton(android.R.string.cancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    I2cSetDialog.this.getDialog().cancel();                    
                }
            });
        
        mDataAddrEt = (EditText) view.findViewById(R.id.dialogDataAddrEt);
        mDataValueEt = (EditText) view.findViewById(R.id.dialogDataValueEt);
        
        // Mode picker
        NumberPicker i2cModePicker = (NumberPicker) view.findViewById(R.id.dialogModePicker);
        i2cModePicker.setMinValue(0);
        i2cModePicker.setMaxValue(I2cInfo.SET_MODE_COUNT-1);
        i2cModePicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mI2cSetMode = newVal;
                Toast.makeText(getActivity(), I2cInfo.getI2cSetModeDescription(newVal), Toast.LENGTH_SHORT).show();;
            }
        });
        i2cModePicker.setDisplayedValues(getModeDisplayedValues());
        
        // Init data
        mDataAddr = I2cInfo.getDataAddrFromPref(getActivity());
        mDataValue = I2cInfo.getDataValueFromPref(getActivity());
        mI2cSetMode = I2cInfo.getSetModeFromPref(getActivity());
        
        mDataAddrEt.setText(Integer.toHexString(mDataAddr));
        mDataValueEt.setText(Integer.toHexString(mDataValue));
        i2cModePicker.setValue(mI2cSetMode);
        
        return builder.create();
    }       
    
    private String[] getModeDisplayedValues() {
        String[] displayedValues = new String[I2cInfo.SET_MODE_COUNT];
        for (int i=0; i<I2cInfo.SET_MODE_COUNT; i++) {
            displayedValues[i] = I2cInfo.getI2cSetMode(i);
        }
        return displayedValues;
    }
}
