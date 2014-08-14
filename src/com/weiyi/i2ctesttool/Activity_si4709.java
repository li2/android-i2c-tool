package com.weiyi.i2ctesttool;


import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Activity_si4709 extends Activity {

    //logfile
    private static File File_si4709;	

    //pe30, smartGPS V2 board i2c chip axp address.
    private String DevAddr = new String("1 0x10");
	
	/*
	 *主线程：执行命令、处理“子线程处理反馈数据后向主线程发送的消息”
	 */
	private Process Process_ExecCmd;
    private boolean boolean_CaptureOutputThreadRunnig = true;
    private Handler Handler_MsgOperate;
    
	private TextView TextView_DeviceID;
	private TextView TextView_ChipID;
	private TextView TextView_Enable;	

	private String[] I2CDumpValHexStr = new String[256];

    
    public void onCreate(Bundle savedInstanceState) {   
        super.onCreate(savedInstanceState);   
        setContentView(R.layout.layout_si4709); 
        
        /**********************************************************************
         * log 新建activity-basic logfile
         */
        File_si4709 = WriteReadSDCard.CreateFile(Activity_TableLayout.I2CLogDirFile,
        		"i2clog-si4709-" + PublicFunc.DateGet() + ".txt");
        
    	//初始化i2cdump output存储数组，格式为16进制字符串
    	for (int i=0; i<I2CDumpValHexStr.length; i++){
    		I2CDumpValHexStr[i] = "00";
    	}
        
    	/**********************************************************************
    	 * 初始化activity-basic UI
    	 */
        /*
         *定义textview, 显示操作结果
         */
        TextView_DeviceID = (TextView) findViewById(R.id.textViewDeviceID);    
        TextView_ChipID = (TextView) findViewById(R.id.textViewChipID);    
        TextView_Enable = (TextView) findViewById(R.id.textViewEnable);   
        
    	
        /**********************************************************************
    	 *call do_exec() first to init Process_ExecCmd before creating child thread.
    	 */
        do_exec("ls " + File_si4709.getPath());       
    	

        /**********************************************************************
         *创建子线程，用于从stdout&stderr中读取文本数据，解析，然后根据解析结果分类通知activity
         */
        new Thread(new Runnable() {
	        public void run() {
            while (boolean_CaptureOutputThreadRunnig) {
                try {
                	//polling space 200ms
                    Thread.currentThread();
					Thread.sleep(200);
					//
					exec_output_parse(Process_ExecCmd);	
                    //Log.d(TAG, "lost  time " + timer);
                } catch (InterruptedException e) {
					e.printStackTrace();
				}
            }}
	    }).start(); //启动线程        
        
        /**********************************************************************
         *创建Hander，
         *用于 “activity-basic” 和 “处理反馈数据的子线程” 之间的通信。
         *因为需要根据反馈数据更新ui，而ui是在activity中建立，必须由activity更新, 所以需要通信。
         *FATAL EXCEPTION: Thread-374
         *android.view.ViewRootImpl$CalledFromWrongThreadException:
         *Only the original thread that created a view hierarchy can touch its views.
         */
        Handler_MsgOperate = new Handler() {
	        public void handleMessage(Message msg) {
	        	hanle_exec_output_parse_msg(msg);
	        }
        };


        //powerdown button click listener
        Button ButtonPowerDown_ls = (Button) findViewById(R.id.buttonPowerDown);          
        ButtonPowerDown_ls.setOnClickListener(new OnClickListener() {  
            public void onClick(View v) {
            	WriteReadSDCard.writeToSDFile(File_si4709, "to powerdown si4709:\r\n");
            	String cmd = "";
            	//To power down the device, write 1 to the ENABLE and DISABLE bits. 
            	//After being written to 1, both bits will get cleared as part of the internal device powerdown sequence.
            	//i2cset -f -y 1 0x10 0x11 0x41 i
            	cmd = Activity_TableLayout.I2CToolDir + "i2cset -f -y " + 
            			DevAddr + " 0x11 0x41 i";
            	do_exec(cmd);
            	
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            	
            	//read
            	cmd = Activity_TableLayout.I2CToolDir + "i2cdump -f -y " + 
            			DevAddr + " i";
            	do_exec(cmd);
             }             
        });        	

        //powerup button click listener        
        Button ButtonPowerUp_ls = (Button) findViewById(R.id.buttonPowerUp);          
        ButtonPowerUp_ls.setOnClickListener(new OnClickListener() {  
            public void onClick(View v) {
            	WriteReadSDCard.writeToSDFile(File_si4709, "to powerup si4709:\r\n");
            	
            	String cmd = "";            	
            	//To power up the device, set ENABLE = 1 and DISABLE = 0 
            	cmd = Activity_TableLayout.I2CToolDir + "i2cset -f -y " + 
            			DevAddr + " 0x11 0x01 i";
            	do_exec(cmd);
            	
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            	
            	//read
            	cmd = Activity_TableLayout.I2CToolDir + "i2cdump -f -y " + 
            			DevAddr + " i";
            	do_exec(cmd);
             }             
        });       	        
       
    } /*end of onCreate()*****************************************************/

    
    private void do_exec(String cmd) {
    	WriteReadSDCard.writeToSDFile(File_si4709, PublicFunc.DateGet() + "\r\n" + cmd + "\r\n");
    	try {  
    		Process_ExecCmd = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }   
    }/*end of do_exec()*******************************************************/

    private void exec_output_parse(Process p){
        if (p == null){
        	System.out.println("p == null");
        	return;
        }
        
        /* 读取stdout, 然后分析 */
    	String stdoutStr = new String("");
		stdoutStr = ExecOutputParse.StdoutGet(p);
		if (stdoutStr.length() > 1){
			WriteReadSDCard.writeToSDFile(File_si4709, stdoutStr + "\r\n");
	    	//i2cdump output parse
	    	if (true == ExecOutputParse.StdoutMatch_I2CDump(stdoutStr, I2CDumpValHexStr)){
	            //send message to inform main thread change UI.
		    	Message msg = new Message();
	            msg.what = 1;    		
		        Handler_MsgOperate.sendMessage(msg);	 
	    	}
	    	//other stdout message to display.		        } 	
    	}/*end of stdout parse*/		
    }/*end of exec_output_parse()*********************************************/

    
	private void hanle_exec_output_parse_msg(Message msg){
		switch (msg.what) {
	    case 1:
	    	//update register value.
	    	update_si4709_regval(I2CDumpValHexStr);
			break;
	    	
		default:
			break;
	    }		
	}/*end of hanle_exec_output_parse_msg()***********************************/
  
    
	private void update_si4709_regval(String[] val){
		String devid = "0x" + val[0xc] + val[0xd];
    	String chipid = "0x" + val[0xe] + val[0xf];
		//enable_bit register02h[bit0], i2cdump返回数据的第0x11个字节，
    	String enable_bit = PublicFunc.HexStrToBinStr(val[0x11], 0, 0);
    	
    	TextView_DeviceID.setText(devid);
    	TextView_ChipID.setText(chipid);
    	TextView_Enable.setText(enable_bit);
	}
	
    
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