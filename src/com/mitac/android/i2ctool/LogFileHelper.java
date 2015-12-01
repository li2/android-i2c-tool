package com.mitac.android.i2ctool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;

public class LogFileHelper {

    private static final String TAG = "i2c_LogFileHelper";

    public static final String TIME_FORMATTER_YMDHMS = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMATTER_HMS = "HH:mm:ss";
    
    private static final String LOG_FILE_SUBDIR = "/i2clog";
    
    private static LogFileHelper sLogFileHelper;
    private File mLogDirectory;

    private LogFileHelper() {
        super();
        mLogDirectory = new File(getInnerSDCardPath() + LOG_FILE_SUBDIR);
        mLogDirectory.mkdirs();
    }

    public static LogFileHelper get() {
        if (sLogFileHelper == null) {
            sLogFileHelper = new LogFileHelper();
        }
        return sLogFileHelper;
    }

    public File createFile(String filename) {
        File file = new File(mLogDirectory, filename);
        writeFile(file, "\r\n" + timestamp(TIME_FORMATTER_YMDHMS) + "\r\n\r\n" );
        return file;
    }

    /**
     * Method to write ascii text characters to file on SD card. Note that you
     * must add a WRITE_EXTERNAL_STORAGE permission to the manifest file or this
     * method will throw a FileNotFound Exception because you won't have write
     * permission.
     */
    public void writeFile(File file, String content) {
        if (file == null) {
            Log.e(TAG, "File not created.");
        }
        
        try {
            FileWriter out = new FileWriter(file, true);
            out.write("\r\n" + content);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "File not found. Did you add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //Get inner SDCard path
    public String getInnerSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }
    
    public String timestamp(String formatter) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatter, Locale.US);
        Date date = new Date();        
        return sdf.format(date);
    }
}
