package com.weiyi.i2ctesttool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;


public class WriteReadSDCard
{		    
	/** Method to check whether external media available and writable. This is adapted from
	   http://developer.android.com/guide/topics/data/data-storage.html#filesExternal */
	//截止2013-08-28，这个函数没有用到
	 public static void checkExternalMedia(){
	      boolean mExternalStorageAvailable = false;
	    boolean mExternalStorageWriteable = false;
	    String state = Environment.getExternalStorageState();
	
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        // Can read and write the media
	        mExternalStorageAvailable = mExternalStorageWriteable = true;
	    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        // Can only read the media
	        mExternalStorageAvailable = true;
	        mExternalStorageWriteable = false;
	    } else {
	        // Can't read or write
	        mExternalStorageAvailable = mExternalStorageWriteable = false;
	    }   
	    System.out.println("\n\nExternal Media: readable="
	            +mExternalStorageAvailable+" writable="+mExternalStorageWriteable);
	}

	 //新建文件，测试 10s 钟建立了 1000个文件
	 //并向文件中写入文件名称和建立时间
	 public static File CreateFile(File dir, String filename){
		    File file = new File(dir, filename);
		    //writeToSDFile(file, filename + "\r\n");
		    //writeToSDFile(file, PublicFunc.TimeGet() + "\r\n\r\n");
		    return file;
	 }
	 
	 /** Method to write ascii text characters to file on SD card. Note that you must add a 
	   WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
	   a FileNotFound Exception because you won't have write permission. */
	 public static  void writeToSDFile(File file, String content){
		 //写入的函数不做检查，所以写入之前需要确保路径和文件已经存在
		 
		 //调试时也打印信息到logcat
		 if (false){
			 System.out.println(content);
		 }
		
		try {
			//在文件末尾增加内容，而不是全部覆盖，需要在参数中增加true, 
		    FileWriter out = new FileWriter(file, true);
		    out.write(content);
		    //out.write("\r\n");
		    out.flush();
		    out.close();
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		    //TODO 文件检查，创建失败时提示
		    System.out.println("******* File not found. Did you" +
		            " add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
		} catch (IOException e) {
		    e.printStackTrace();
		}   
	}

	
}