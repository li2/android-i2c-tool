package com.mitac.android.i2ctool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.os.Handler;
import android.util.Log;

public class I2cToolHelper {
    
    private static final String TAG = "i2c_I2cToolHelper";
    
    private static final String I2C_TOOL_PATH = "/system/xbin";
    private static final String I2C_TOOL_DETECT = "i2cdetect";
    private static final String I2C_TOOL_DUMP = "i2cdump";
    private static final String I2C_TOOL_GET = "i2cget";
    private static final String I2C_TOOL_SET = "i2cset";
    
    public static final int PROCESS_STREAM_STDOUT = 1;
    public static final int PROCESS_STREAM_STDERR = 2;
    
    private static final int DELAY_AFTER_CMD_EXECUTED = 50; // 50ms
    
    private Process mNativeProcess;
    private Handler mHandler = new Handler();
    private Handler mResponseHandler;
    private OnStreamParsedListener mOnStreamParsedListener;       
    
    public interface OnStreamParsedListener {
        void onStreamParsed(int streamType, ArrayList<String> stream);
    }
    
    public void setOnStreamParsedListener(OnStreamParsedListener l) {
        mOnStreamParsedListener = l;
    }
    
    public I2cToolHelper(Handler responseHandler) {
        super();
        mResponseHandler = responseHandler;
    }   
    
    // android runtime exec Could not open file `/dev/i2c-2'Permission denied, Run as root?
    // default is 600
    // chmod 666 /dev/i2c-2
    // crw------- root     root      89,   2 2015-10-27 14:51 i2c-2
    // crw-rw-rw- root     root      89,   2 2015-10-27 14:51 i2c-2        
    private Process doRuntimeExec(String program) {
        Process process;
        try {
            Log.i(TAG, "Execute " + program);
            process = Runtime.getRuntime().exec(program);
            mHandler.postDelayed(mStreamParserRunnable, DELAY_AFTER_CMD_EXECUTED);
            
        } catch (IOException ioe) {
            process = null;
            Log.e(TAG, "Requested program can not be executed: " + program + ", " + ioe);
        }
        
        return process;
    }
    
    public void doDump(int bus, int address, String mode) {
        String program = I2C_TOOL_PATH + "/" + I2C_TOOL_DUMP + " -f -y " + bus + " " + address;
        if (mode != null) {
            program += " " + mode;
        }
        mNativeProcess = doRuntimeExec(program);
    }
    
    public void doSet(int bus, int address, int dataAddress, int value, String mode) {
        String program = I2C_TOOL_PATH + "/" + I2C_TOOL_SET + " -f -y " +
                bus + " " + address + " " + dataAddress + " " + value;
        if (mode != null) {
            program += " " + mode;
        }
        mNativeProcess = doRuntimeExec(program);
    }
    
    public void doDetect() {
        String program = I2C_TOOL_PATH + "/" + I2C_TOOL_DETECT + " -l";
        // use this command instead
        program = "ls /sys/bus/i2c/devices ; ls /sys/bus/i2c/drivers";
        // program = "ls /sys/bus/i2c/devices ; cat /sys/bus/i2c/devices/*/name";
        mNativeProcess = doRuntimeExec(program);
    }
    
    private Runnable mStreamParserRunnable = new Runnable() {
        @Override
        public void run() {
            if (mNativeProcess == null) {
                return;
            }
            ArrayList<String> result;
            result = getStandOut();
            if (result == null || result.size() == 0) {
                result = getStandError();
                if (result != null) {
                    Log.e(TAG, "Stand Err: " + result);
                    handleParseResult(PROCESS_STREAM_STDERR, result);
                }
            } else {
                // Log.d(TAG, "Stand Out: " + result);
                handleParseResult(PROCESS_STREAM_STDOUT, result);
            }
        }
    };
    
    private void handleParseResult(final int streamType, final ArrayList<String> stream) {
        if (mResponseHandler == null) {
            Log.e(TAG, "Response handler is null");
            return;
        }
        
        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnStreamParsedListener != null) {
                    mOnStreamParsedListener.onStreamParsed(streamType, stream);
                }
            }
        });
    }
    
    private ArrayList<String> getStandOut() {
        if (mNativeProcess == null) {
            return null;
        }
        return getProcessStream(mNativeProcess, PROCESS_STREAM_STDOUT);
    }
    
    private ArrayList<String> getStandError() {
        if (mNativeProcess == null) {
            return null;
        }
        return getProcessStream(mNativeProcess, PROCESS_STREAM_STDERR);
    }
    
    private ArrayList<String> getProcessStream(Process process, int streamType) {
        try {
            ArrayList<String> result = new ArrayList<String>();
            String line = "";
            BufferedReader bufReader;
            if (streamType == PROCESS_STREAM_STDERR) {
                bufReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            } else {
                bufReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            }
            
            while ((line = bufReader.readLine()) != null) {
                if (line.trim().equals("")) {
                    continue;
                }
                result.add(line);
            }            
            return result;
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }
}
