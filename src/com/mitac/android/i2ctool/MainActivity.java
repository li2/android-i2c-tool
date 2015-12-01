package com.mitac.android.i2ctool;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mitac.android.i2ctool.I2cInfoDialog.OnI2cInfoChangedListener;
import com.mitac.android.i2ctool.I2cSetDialog.OnSetClickListener;
import com.mitac.android.i2ctool.I2cToolHelper.OnStreamParsedListener;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "i2c_MainActivity";
    
    private static final String LOG_RAW_FILE_NAME = "i2c_log_raw.txt";
    private static final String LOG_PARSED_FILE_NAME = "i2c_log_parsed.txt";
    
    private int mI2cBus = 0;
    private int mI2cAddress = 0x0;
    private String mI2cMode = null;
    private int mLoopInterval = 500; // 500ms
    private int mConsoleMaxLines = 512;
    
    private Handler mHandler = new Handler();
    private I2cToolHelper mI2cToolHelper;
    private File mLogRawFile;
    private File mLogParsedFile;
    
    private ScrollView mConsoleScroll;
    private TextView mConsoleTv;
    private Button mInfoBtn;
    private Button mCleanBtn;
    private Button mDetectBtn;
    private Button mDumpBtn;
    private Button mSetBtn;
    private Button mLoopIntervalBtn;
    private CheckBox mLoopCheckBox;
    private AlertDialog mLoopChoicesDialog;
    private I2cInfoDialog mInfoDialog;
    private I2cSetDialog mSetDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        
        mI2cToolHelper = new I2cToolHelper(mHandler);
        mI2cToolHelper.setOnStreamParsedListener(mOnStreamParsedListener);
        mLogRawFile = LogFileHelper.get().createFile(LOG_RAW_FILE_NAME);
        mLogParsedFile = LogFileHelper.get().createFile(LOG_PARSED_FILE_NAME);
        mConsoleMaxLines = getResources().getInteger(R.integer.console_max_lines);
        
        mI2cBus = I2cInfo.getBusIdFromPref(this);
        mI2cAddress = I2cInfo.getAddressFromPref(this);
        mI2cMode = I2cInfo.getI2cDumpMode(I2cInfo.getDumpModeFromFref(this));
        
        updateI2cInfoView();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // clean dump schedule
        stopScheduleDump();
        mExecutorService.shutdown();
    }
    
    private void initView() {
        mConsoleScroll = (ScrollView) findViewById(R.id.i2cConsoleScroll);
        mConsoleTv = (TextView) findViewById(R.id.i2cConsoleTv);
        
        mCleanBtn = (Button) findViewById(R.id.i2cClearBtn);
        mCleanBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mConsoleTv.setText("");
            }
        });
        
        mDetectBtn = (Button) findViewById(R.id.i2cDetectBtn);
        mDetectBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                detect();
            }
        });
        
        mDumpBtn = (Button) findViewById(R.id.i2cDumpBtn);
        mDumpBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoopCheckBox.isChecked()) {
                    scheduleDump();
                } else {
                    dump();
                }
            }
        });
        
        // android AlertDialog integer-array NullPointerException, so use string-array instead
        // android.widget.ArrayAdapter.createViewFromResource(ArrayAdapter.java:394)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(R.array.loop_interval_choices, 0, new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView lv = ((AlertDialog)dialog).getListView();
                String checkedItem = (String)lv.getAdapter().getItem(which);
                mLoopChoicesDialog.dismiss();
                mLoopInterval = (int)(Float.parseFloat(checkedItem) * 1000);
                mLoopIntervalBtn.setText(checkedItem + " s");
            }
        });
        mLoopChoicesDialog = builder.create();
        
        mLoopIntervalBtn = (Button) findViewById(R.id.i2cLoopIntervalBtn);
        mLoopIntervalBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoopChoicesDialog.show();
            }
        });
        String[] dumpChoiceItems = getResources().getStringArray(R.array.loop_interval_choices);
        mLoopIntervalBtn.setText(dumpChoiceItems[0] + " s");
        
        mLoopCheckBox = (CheckBox) findViewById(R.id.i2cLoopCheckbox);
        mLoopCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkbox = (CheckBox) v;
                mLoopIntervalBtn.setEnabled(!checkbox.isChecked());
                if (!checkbox.isChecked()) {
                    stopScheduleDump();
                }
            }
        });
        
        mInfoDialog = new I2cInfoDialog();
        mInfoDialog.setOnI2cInfoChangedListener(new OnI2cInfoChangedListener() {
            @Override
            public void onI2cInfoChanged(int busId, int address, String mode) {
                Log.d(TAG, "Address " + address);
                mI2cBus = busId;
                mI2cAddress = address;
                mI2cMode = mode;
                updateI2cInfoView();
            }
        });
        
        mInfoBtn = (Button) findViewById(R.id.i2cInfoBtn);
        mInfoBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mInfoDialog.show(getSupportFragmentManager(), TAG);
            }
        });
        
        mSetDialog = new I2cSetDialog();
        mSetDialog.setOnSetClickListener(new OnSetClickListener() {
            @Override
            public void onSetClick(int dataAddress, int value, String mode) {
                set(dataAddress, value, mode);
            }
        });
        
        mSetBtn = (Button) findViewById(R.id.i2cSetBtn);
        mSetBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSetDialog.show(getSupportFragmentManager(), TAG);
            }
        });
    }
    
    private void updateI2cInfoView() {
        mInfoBtn.setText("" + mI2cBus + ", 0x" + Integer.toHexString(mI2cAddress) + ", " + mI2cMode);
    }
    
    private void updateConsoleView(ArrayList<String> stream) {
        // write log raw file
        String newResult = "";
        for (String str : stream) {
            newResult += str + "\r\n";
        }
        LogFileHelper.get().writeFile(mLogRawFile, newResult);
        
        // write log parsed file
        ArrayList<String> parsedStream = I2cParserHelper.parseI2cDumpMode_W(stream);
        if (parsedStream != null) {
            LogFileHelper.get().writeFile(mLogParsedFile,
                    LogFileHelper.get().timestamp(LogFileHelper.TIME_FORMATTER_HMS)
                    + " - "
                    + TextUtils.join(" ", parsedStream));
        }
        
        // update console view
        if (mConsoleTv.getLineCount() + stream.size() > mConsoleMaxLines) {
            mConsoleTv.setText("");
        }
        String result = mConsoleTv.getText().toString() + "\r\n" + newResult;
        mConsoleTv.setText(result);
        mConsoleScroll.postDelayed(new Runnable() { // delay to scrolling down completely
            @Override
            public void run() {
                mConsoleScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 100);
    }
    
    private void detect() {
        mI2cToolHelper.doDetect();
    }
    
    private void set(int dataAddress, int value, String mode) {
        mI2cToolHelper.doSet(mI2cBus, mI2cAddress, dataAddress, value, mode);
    }
    
    private void dump() {
        mI2cToolHelper.doDump(mI2cBus, mI2cAddress, mI2cMode);
    }
    
    private ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduledFuture; 
    
    private void scheduleDump() {
        stopScheduleDump();
        if (!mExecutorService.isShutdown()) {
            mScheduledFuture = mExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    mHandler.post(mDumpTask);
                }
            }, 0, mLoopInterval, TimeUnit.MILLISECONDS);
        }
    }
    
    private void stopScheduleDump() {
        if (mScheduledFuture != null) {
            mScheduledFuture.cancel(false);
        }
    }
    
    private final Runnable mDumpTask = new Runnable() {
        @Override
        public void run() {
            dump();
        }
    };
    
    private OnStreamParsedListener mOnStreamParsedListener = new OnStreamParsedListener() {
        @Override
        public void onStreamParsed(int streamType, ArrayList<String> stream) {
            // stream is not null
            updateConsoleView(stream);
        }
    };
}
