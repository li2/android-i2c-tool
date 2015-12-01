package com.mitac.android.i2ctool;

import android.content.Context;
import android.preference.PreferenceManager;

public class I2cInfo {

    public static final int ADDR_COUNT = 256;
    public static final int DUMP_MODE_COUNT = 5;
    public static final int SET_MODE_COUNT = 2;
    
    public static final String PREF_I2C_BUS = "i2cBusId";
    public static final String PREF_I2C_ADDR = "i2cAddress";
    public static final String PREF_I2C_DUMP_MODE = "i2cDumpMode";
    public static final String PREF_I2C_SET_MODE = "i2cSetMode";
    public static final String PREF_I2C_DATA_ADDR = "i2cDataAddress";
    public static final String PREF_I2C_DATA_VALUE = "i2cDataValue";
    
    public enum I2cDumpMode {
        Byte("b - byte, default", 0),
//        Word("w - word", 1),
        WordOnEvenAddr("W - word on even register addresses", 2),
        SMBusBlock("s - SMBus block", 3),
        I2CBlock("i - I2C block", 4),
        ConsecutiveByte("c- consecutive byte", 5);
        
        private String mStringValue;
        @SuppressWarnings("unused")
        private int mIntValue;
        private I2cDumpMode(String toString, int value) {
            mStringValue = toString;
            mIntValue = value;
        }
        
        @Override
        public String toString() {
            return mStringValue;
        }
        
        // get Enum value from integer
        private static I2cDumpMode fromInteger(int i) {
            I2cDumpMode[] modeValues = I2cDumpMode.values();
            return modeValues[i];
        }
    }
    
    public enum I2cSetMode {
        Byte("b - byte, default", 0),
        Word("w - word", 1);
        
        private String mStringValue;
        @SuppressWarnings("unused")
        private int mIntValue;
        private I2cSetMode(String toString, int value) {
            mStringValue = toString;
            mIntValue = value;
        }
        
        @Override
        public String toString() {
            return mStringValue;
        }
        
        // get Enum value from integer
        private static I2cSetMode fromInteger(int i) {
            I2cSetMode[] modeValues = I2cSetMode.values();
            return modeValues[i];
        }
    }    
    
    // get the first letter
    public static String getI2cDumpMode(int value) {
        return I2cDumpMode.fromInteger(value).toString().substring(0, 1);
    }
    
    public static String getI2cDumpModeDescription(int value) {
        return I2cDumpMode.fromInteger(value).toString();
    }
    
    public static String getI2cSetMode(int value) {
        return I2cSetMode.fromInteger(value).toString().substring(0, 1);
    }
    
    // get total string
    public static String getI2cSetModeDescription(int value) {
        return I2cSetMode.fromInteger(value).toString();
    }
    
    // Shared Preference
    public static int getBusIdFromPref(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_I2C_BUS, 0);
    }
    
    public static int getAddressFromPref(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_I2C_ADDR, 0);
    }
    
    public static int getDumpModeFromFref(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_I2C_DUMP_MODE, 0);
    }
    
    public static int getSetModeFromPref(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_I2C_SET_MODE, 0);
    }
    
    public static int getDataAddrFromPref(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_I2C_DATA_ADDR, 0);
    }
    
    public static int getDataValueFromPref(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_I2C_DATA_VALUE, 0);
    }
    
    public static void saveI2cInfoToPref(Context context, int busId, int address, int mode) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putInt(PREF_I2C_BUS, busId)
            .putInt(PREF_I2C_ADDR, address)
            .putInt(PREF_I2C_DUMP_MODE, mode)
            .commit();
    }
    
    public static void saveI2cSetToPref(Context context, int dataAddr, int dataValue, int mode) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putInt(PREF_I2C_DATA_ADDR, dataAddr)
            .putInt(PREF_I2C_DATA_VALUE, dataValue)
            .putInt(PREF_I2C_SET_MODE, mode)
            .commit();
    }
}
