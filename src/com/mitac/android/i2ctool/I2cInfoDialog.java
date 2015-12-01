package com.mitac.android.i2ctool;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.Toast;

public class I2cInfoDialog extends DialogFragment {
    
    private static final String TAG = "i2c_I2cInfoDialog";
    
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 0xf;
    
    private int mI2cBusId = 0;
    private int mI2cAddrHigh = 0;
    private int mI2cAddrLow = 0;
    private int mI2cMode = 0;
    
    private OnI2cInfoChangedListener mOnI2cInfoChangedListener;
    
    public interface OnI2cInfoChangedListener {
        void onI2cInfoChanged(int busId, int address, String mode);            
    }
    
    public void setOnI2cInfoChangedListener(OnI2cInfoChangedListener l) {
        mOnI2cInfoChangedListener = l;
    }
    
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        View view = inflater.inflate(R.layout.dialog_i2c_info, null);
        builder.setView(view)
            .setPositiveButton(android.R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mOnI2cInfoChangedListener != null) {
                        Log.d(TAG, "Address High " + mI2cAddrHigh + ", Low " + mI2cAddrLow);
                        int address = mI2cAddrHigh * 16 + mI2cAddrLow;
                        String mode = I2cInfo.getI2cDumpMode(mI2cMode);
                        mOnI2cInfoChangedListener.onI2cInfoChanged(mI2cBusId, address, mode);
                        I2cInfo.saveI2cInfoToPref(getActivity(), mI2cBusId, address, mI2cMode);
                    }
                }
            })
            .setNegativeButton(android.R.string.cancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    I2cInfoDialog.this.getDialog().cancel();                    
                }
            });
        
        AlertDialog dialog = builder.create();
        
        // dialog.findViewById in DialogFragment onCreateDialog()
        // view.findViewById solve it.
        
        // Bus picker
        NumberPicker i2cBusIdPicker = (NumberPicker) view.findViewById(R.id.dialogBusIdPicker);
        i2cBusIdPicker.setMinValue(MIN_VALUE);
        i2cBusIdPicker.setMaxValue(MAX_VALUE);
        i2cBusIdPicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mI2cBusId = newVal;
            }
        });
        
        // Adress picker
        NumberPicker i2cAddrHightPicker = (NumberPicker) view.findViewById(R.id.dialogAddrHighPicker);
        i2cAddrHightPicker.setMinValue(MIN_VALUE);
        i2cAddrHightPicker.setMaxValue(MAX_VALUE);
        i2cAddrHightPicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mI2cAddrHigh = newVal;
            }
        });        
        // setFormatter return "0x" + Integer.toHexString(value)
        // 停止滚动后，0xfe变成0，0xf2变成2，所有字母都没有显示！
        // workaround
        i2cAddrHightPicker.setDisplayedValues(getAddrDisplayedValues());
        
        NumberPicker i2cAddrLowPicker = (NumberPicker) view.findViewById(R.id.dialogAddrLowPicker);
        i2cAddrLowPicker.setMinValue(MIN_VALUE);
        i2cAddrLowPicker.setMaxValue(MAX_VALUE);
        i2cAddrLowPicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mI2cAddrLow = newVal;
            }
        });        
        i2cAddrLowPicker.setDisplayedValues(getAddrDisplayedValues());
        
        // Mode picker
        NumberPicker i2cModePicker = (NumberPicker) view.findViewById(R.id.dialogModePicker);
        i2cModePicker.setMinValue(0);
        i2cModePicker.setMaxValue(I2cInfo.DUMP_MODE_COUNT-1);
        i2cModePicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mI2cMode = newVal;
                Toast.makeText(getActivity(), I2cInfo.getI2cDumpModeDescription(newVal), Toast.LENGTH_SHORT).show();;
            }
        });
        i2cModePicker.setDisplayedValues(getModeDisplayedValues());
        
        // Init data
        mI2cBusId = I2cInfo.getBusIdFromPref(getActivity());
        int address = I2cInfo.getAddressFromPref(getActivity());
        mI2cAddrHigh = address/16;
        mI2cAddrLow = address%16;
        mI2cMode = I2cInfo.getDumpModeFromFref(getActivity());
        
        i2cBusIdPicker.setValue(mI2cBusId);
        i2cAddrHightPicker.setValue(mI2cAddrHigh);
        i2cAddrLowPicker.setValue(mI2cAddrLow);
        i2cModePicker.setValue(mI2cMode);
        return dialog;
    }       
    
    private String[] getModeDisplayedValues() {
        String[] displayedValues = new String[I2cInfo.DUMP_MODE_COUNT];
        for (int i=0; i<I2cInfo.DUMP_MODE_COUNT; i++) {
            displayedValues[i] = I2cInfo.getI2cDumpMode(i);
        }
        return displayedValues;
    }
    
    private String[] getAddrDisplayedValues() {
        String[] displayedValues = new String[I2cInfo.ADDR_COUNT];
        for (int i=0; i<displayedValues.length; i++) {
            displayedValues[i] = Integer.toHexString(i);
        }
        return displayedValues;
    }
}
