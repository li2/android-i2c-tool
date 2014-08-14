package com.weiyi.i2ctesttool;


import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class Activity_ScreenCtrl extends Activity {

	private Button Button_ScreenCtrl;	
	private boolean Flag_ScreenIsOn = true;
    
    public void onCreate(Bundle savedInstanceState) {   
        super.onCreate(savedInstanceState);   
        setContentView(R.layout.layout_screen_ctrl); 
       
    	/**********************************************************************
    	 *read button listener 按钮事件监听函数 
    	 */
        Button_ScreenCtrl = (Button) findViewById(R.id.buttonScreenCtrl);          
        Button_ScreenCtrl.setOnClickListener(new OnClickListener() {  
            public void onClick(View v) {
            	if (Flag_ScreenIsOn == true){
            		Flag_ScreenIsOn = false;
            		screen_brightness_adjust(0.004f);
            		Button_ScreenCtrl.setText("ON");    	    		
            	} else {
            		Flag_ScreenIsOn = true;
            		screen_brightness_adjust(-1);
            		Button_ScreenCtrl.setText("OFF");    	    		
            	}
             }          
        });   	
        
 
        /**********************************************************************
         *创建子线程，倒计时
         */
/*
        new Thread(new Runnable() {
	        public void run() {
            while (true) {
                try {
                    Thread.currentThread();
					Thread.sleep(200);
                } catch (InterruptedException e) {
					e.printStackTrace();
				}
            }}
	    }).start(); //启动线程        
*/       
    } /*end of onCreate()*****************************************************/

    
    //http://developer.android.com/reference/android/view/WindowManager.LayoutParams.html#screenBrightness
    //http://www.apkbus.com/android-89276-1-1.html
    //当我们遇到把Activity做为子Activity潜入到TabActivity 或者 ViewGroup 类容器时，通常上面的方法设置无法取得成功
    //需要通过getParent（）方法获取器Parent，然后设置
    //screenBrightness 设为 -1恢复到原先的亮度（即系统设置）
    //将屏幕设置到最低亮度值是0.004（精度0.001），这时屏幕基本全黑，但仍能控制。低于0.004（精度0.001）时，屏幕便失去控制
    private void screen_brightness_adjust(float val){
        WindowManager.LayoutParams params = getParent().getWindow().getAttributes();    
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.screenBrightness = val;
        getParent().getWindow().setAttributes(params); 
    }

    
    private void do_exec(String cmd) {
    	try {  
    		Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {  
            e.printStackTrace();  
        }   
    }/*end of do_exec()*******************************************************/

    
    //catch the Home button click in android, then close app.
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HOME){
	        finish();
	        System.exit(0);    	
		}
		if (keyCode==KeyEvent.KEYCODE_BACK){
	        finish();
	        System.exit(0);    	
		}
		return false;
	};
    
}