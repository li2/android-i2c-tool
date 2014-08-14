package com.weiyi.i2ctesttool;


import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Activity_alc5625 extends Activity {

    //logfile
	private static File logfile;	

    //pe30, smartGPS V2 board i2c chip alc address.
    private String DevAddr = new String("1 0x1e");
    private String DevMode = new String("W");
   
	private EditText EditText_ReadCycleTime;
	
	private Button Button_Read;
	private TextView TextView_Result;
	private TextView TextView_Warn;

	private String[] I2CDumpValHexStr = new String[256];
	
	/*
	 *主线程：执行命令、处理“子线程处理反馈数据后向主线程发送的消息”
	 */
	private Process Process_ExecCmd;
    private boolean boolean_CaptureOutputThreadRunnig = true;
    private Handler Handler_MsgOperate;
    

    /*
     *循环读取总线需要定义的变量 
     */
    //循环时间若为0，则点击按钮只读一次
    private boolean boolean_LoopReadEnable = false;
    //循环读取线程，点击按钮后开始循环读取，再次点击按钮结束循环读取
    private Thread ThreadReadCycle;
    private boolean boolean_LoopReadState = false;
    
  
    
	/************************************************************************** 
	 * Called when the activity is first created. 
	 */  
    @SuppressLint("HandlerLeak")
	public void onCreate(Bundle savedInstanceState) {   
        super.onCreate(savedInstanceState);   
        setContentView(R.layout.layout_alc5625);   
    
        /**********************************************************************
         * log 新建activity-basic logfile
         */
    	logfile = WriteReadSDCard.CreateFile(Activity_TableLayout.I2CLogDirFile,
    			"i2clog-alc-" + PublicFunc.DateGet() + ".txt");
    	
    	alc5625.ItemArrayInit();
    	WriteReadSDCard.writeToSDFile(logfile, alc5625.ItemLogNames());
 
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
    	TextView_Result = (TextView) findViewById(R.id.idtextViewResult); 
    	TextView_Result.setMovementMethod(new ScrollingMovementMethod());	
    	TextView_Warn = (TextView) findViewById(R.id.idtextViewWarn); 
    	TextView_Warn.setMovementMethod(new ScrollingMovementMethod());	
    	 
    	/*
    	 *定义edittext, 获取read/set的操作参数
    	 */
    	EditText_ReadCycleTime = (EditText) findViewById(R.id.ideditTextCycleTime);


    	/**********************************************************************
    	 *read button 循环时间是否为0决定read的2种模式，只读1次、循环读取
    	 *时间为0的标准：如果为空或者全部输入0。
    	 *循环读取时，按一次开始，再按结束。
    	 *所以需要监听用户输入的循环时间，
    	 *由于回车键导致异常退出，需要限制输入类型为数字，限制输入长度5位， xml中完成。
    	 */
    	//edittext change listener
    	EditText_ReadCycleTime.addTextChangedListener(new TextWatcher() {
	        public void afterTextChanged(Editable s) {
	        	String Str_cycletime = EditText_ReadCycleTime.getText().toString();
	        	if ((Str_cycletime.compareTo("") == 0) || (Integer.valueOf(Str_cycletime) == 0)){
	        		//System.out.println("boolean_LoopReadEnable = false");
	        		//bug 如果正在cycle read， boolean_LoopReadState = true，
	        		//此时更改cycletime=0，导致stop无效
	        		//if(==true)存在的理由是，如果没有cycle，先写入非0值，使boolean_LoopReadEnable=true，
	        		//紧接着改为0，preformClick()，会进入else分子执行thread_read_cycle()读取edittext值，
	        		//空的edittext ""转 int 时会导致
	        		//FATAL EXCEPTION: Thread-387
	        		//java.lang.NumberFormatException: Invalid int: ""
	        		//所以，全局标志位容易出问题啊！！！
	        		//TODO 替代全局标志的方法 非常重要
	        		if (boolean_LoopReadState == true){
		        		Button_Read.performClick();	        			
	        		}
	        		boolean_LoopReadEnable = false;
	        	} else {        		
	        		//System.out.println(Integer.valueOf(EditText_ReadCycleTime.getText().toString()));
	        		boolean_LoopReadEnable = true;
	        	}	
	        }
	
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
	
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
    	});//endof EditText_ReadCycleTime.addTextChangedListener()
        
    	/**********************************************************************
    	 *read button listener 按钮事件监听函数 
    	 */
        Button_Read = (Button) findViewById(R.id.idButtonRead);          
        Button_Read.setOnClickListener(new OnClickListener() {  
            public void onClick(View v) {
            	if (boolean_LoopReadEnable == false){
            		//只读一次
                	do_exec(read_cmd_get());
            	} else {
            		//循环读取，点击按钮后开始，再次点击按钮结束
                	if (false == boolean_LoopReadState){
                		boolean_LoopReadState = true;
                		Button_Read.setText("StopRead"); 
                		Button_Read.setTextColor(Color.RED);
                		thread_read_cycle();
                	} else {
                		boolean_LoopReadState = false;
                		ThreadReadCycle.interrupt();
                		Button_Read.setText("read");
                		Button_Read.setTextColor(Color.BLACK);
                	}
            	}
             }//end of onClick()             
        });//end of Button_Read.setOnClickListener()        	
        
 
        /**********************************************************************
    	 *call do_exec() first to init Process_ExecCmd before creating child thread.
    	 */
		do_exec("ls " + logfile.getPath());       
        

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

    } /*end of onCreate()*****************************************************/

    
    private String read_cmd_get(){
        //Usage: i2cdump [-f] [-y] [-r first-last] I2CBUS ADDRESS [MODE [BANK [BANKREG]]]
    	//i2cdump -f -y -r first-last i2cbus address mode 
    	String dev = DevAddr;
		String mode = DevMode;
    	String cmd = Activity_TableLayout.I2CToolDir 
    			+ "i2cdump -f -y" + " " + dev + " " + mode;
    	return cmd;    	
    }
    
    
    private void thread_read_cycle(){
    	ThreadReadCycle = new Thread(new Runnable() {
        public void run() {
           	int readcycletime;
           	//Integer.valueOf("")会导致 
    		//FATAL EXCEPTION: Thread-387
    		//java.lang.NumberFormatException: Invalid int: ""
        	String str_readcycletime = EditText_ReadCycleTime.getText().toString();
        	if (str_readcycletime.compareTo("") == 0){
        		readcycletime = 0;
        	} else {
        		readcycletime = Integer.valueOf(str_readcycletime);
        	}
        	if (readcycletime < 100){
        		readcycletime = 100;            		
        	}

        	String cmd = read_cmd_get();
        	while (boolean_LoopReadState){
				try {
					Thread.sleep(readcycletime);
                	do_exec(cmd);               		
				} catch (InterruptedException e) {
					//调用thread.interrupt()会进入catch (InterruptedException e)
					//执行e.printStackTrace(); 从System.err信息看线程睡眠。
					e.printStackTrace();
				}	
           	}//end of while(loop)        	        	
        }//end of run()
        });//end of new Thread()
    	ThreadReadCycle.start();    	
    }
    
    
    private void do_exec(String cmd) {
//    	WriteReadSDCard.writeToSDFile(logfile, PublicFunc.DateGet() + "\r\n" + cmd + "\r\n");
//    	WriteReadSDCard.writeToSDFile(logfile, "--------------------------------------------\r\n");
    	try {  
    		Process_ExecCmd = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }   
    }/*end of do_exec()*******************************************************/
    

	private void hanle_exec_output_parse_msg(Message msg){
		switch (msg.what) {
	    case 0:
	    	alc5625.ItemValCalc(I2CDumpValHexStr);
			TextView_Result.setText(alc5625.ItemTextviewRes());
    		WriteReadSDCard.writeToSDFile(logfile, alc5625.ItemLogVal());
    		break;
				    	
		default:
			break;
	    }		
	}/*end of hanle_exec_output_parse_msg()***********************************/
    
	
	private void exec_output_parse(Process p){
    	if (p == null){
        	System.out.println("p == null");
        	return;
        }

        //读取stdout, 然后分析
    	String stdoutStr = new String("");
		stdoutStr = ExecOutputParse.StdoutGet(p);
		if (stdoutStr.length() > 1){
	    	//i2cdump output parse
	    	if (true == ExecOutputParse.StdoutMatch_I2CDump(stdoutStr, I2CDumpValHexStr)){
		    	Message msg = new Message();
	            msg.what = 0;    		
	            System.out.println(stdoutStr);
		        Handler_MsgOperate.sendMessage(msg);	 
	    	}
    	}		
    }/*end of exec_output_parse()*********************************************/

	
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

    
/*    
   //TODO activity不处于当前状态时，睡眠！！2013年8月25日 02:21
    //临时通过模拟点击按钮，解决不再当前状态后，readloop仍在执行
    protected void onPause(){
        super.onPause();
		if (boolean_LoopReadState == true){
    		Button_Read.performClick();	        			
		}
    }
*/    

}