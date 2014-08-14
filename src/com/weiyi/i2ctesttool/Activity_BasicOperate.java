package com.weiyi.i2ctesttool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class Activity_BasicOperate extends Activity {

    private File File_basic;	

    /*
	 *创建下拉列表，存储板载i2c设备信息
	 */
    private Spinner Spinner_I2CDevOnBoard;   
    private ArrayAdapter<String> Adapter_I2CDevOnBoard;   
	private List<String> ListStr_I2CBusOnBoard = new ArrayList<String>();
	//板载i2c设备列表，设置为public，供其它activity调用
	public static List<String> ListStr_I2CDevOnBoard = new ArrayList<String>();
    //pe30 chip address.
    private String DevAddr = new String("");


	/*
	 *创建下拉列表，存储i2c操作模式
	 */
    private Spinner Spinner_I2CModes;   
    private ArrayAdapter<String> Adapter_I2CModes;   
    private List<String> ListStr_I2CModes = new ArrayList<String>();   
    
	private EditText EditText_ByteFirst;
	private EditText EditText_ByteLast;
	private EditText EditText_ReadCycleTime;
	private EditText EditText_ByteNo; 
	private EditText EditText_ByteVal; 
	
	private EditText EditText_Bus; 
	private EditText EditText_Addr; 

	
	private Button Button_Read;
	private TextView TextView_Result;

	private String[] I2CDumpValHexStr = new String[256];
	
	/*
	 *主线程：执行命令、处理“子线程处理反馈数据后向主线程发送的消息”
	 */
	private Process Process_ExecCmd;
    private boolean boolean_CaptureOutputThreadRunnig = true;
    private Handler Handler_MsgOperate;
    
    /*
     *i2cdetect 探测板载i2c总线及设备需要定义的变量 
     */
    //i2c bus number detect now 当前探测的总线号
	private String Str_I2CBusDetecting = new String("");  
    //if current i2c device detect finish 当前探测是否结束
    private boolean boolean_I2CDetectCurFinish = false;
    //if all i2c device detect finish 所有探测是否结束
    private boolean boolean_I2CDetectAllFinish = false;

    /*
     *循环读取总线需要定义的变量 
     *		[TODO bug20130828注释]
			bug 如果正在cycle read， boolean_LoopReadState = true，
			此时更改cycletime=0，导致stop无效
			if(==true)存在的理由是，如果没有cycle，先写入非0值，使boolean_LoopReadEnable=true，
			紧接着改为0，preformClick()，会进入else分子执行thread_read_cycle()读取edittext值，
			空的edittext ""转 int 时会导致
			FATAL EXCEPTION: Thread-387
			java.lang.NumberFormatException: Invalid int: ""
			所以，全局标志位容易出问题啊！！！
     */
    //循环时间若为0，则点击按钮只读一次
    private boolean boolean_LoopReadEnable = false;
    //循环读取线程，点击按钮后开始循环读取，再次点击按钮结束循环读取
    private Thread ThreadReadCycle;
    private boolean boolean_LoopReadState = false;
    
    private Thread tempButtonClick;
    
	/************************************************************************** 
	 * Called when the activity is first created. 
	 */  
    public void onCreate(Bundle savedInstanceState) {   
        super.onCreate(savedInstanceState);   
        setContentView(R.layout.layout_basic);   
    
        /**********************************************************************
         * log 新建activity-basic logfile
         */
//    	File_basic = WriteReadSDCard.CreateFile(Activity_TableLayout.I2CLogDirFile,
//    			"i2clog-basic-" + PublicFunc.DateGet() + ".txt");

    	File_basic = WriteReadSDCard.CreateFile(Activity_TableLayout.I2CLogDirFile,
    			"i2clog-basic.txt");

    	
    	//初始化i2cdump output存储数组，格式为16进制字符串
    	for (int i=0; i<I2CDumpValHexStr.length; i++){
    		I2CDumpValHexStr[i] = "00";
    	}
    	
    	
    	/**********************************************************************
    	 * 初始化activity-basic UI
    	 */
    	/*
    	 *创建spinner，存储板载i2c设备信息，存储格式 i2cbus address，比如 0 0x34 
    	 *定义下拉列表的适配器；选择下拉列表适配器的样式；添加适配器到下拉列表；定义下拉列表被选中时的响应
    	 */
        Spinner_I2CDevOnBoard = (Spinner)findViewById(R.id.idSpinnerI2CDevOnBoard);   
        Adapter_I2CDevOnBoard = new ArrayAdapter<String>(this,
        					android.R.layout.simple_spinner_item, ListStr_I2CDevOnBoard);   
        Adapter_I2CDevOnBoard.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);   
        Spinner_I2CDevOnBoard.setAdapter(Adapter_I2CDevOnBoard);     
        Spinner_I2CDevOnBoard.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){   
            public void onItemSelected(AdapterView parent, View view, int pos, long id) {   
                parent.setVisibility(View.VISIBLE); 
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLUE);
                ((TextView) parent.getChildAt(0)).setTextSize(24);
                DevAddr = Spinner_I2CDevOnBoard.getSelectedItem().toString();
            }   
			public void onNothingSelected(AdapterView parent) {   
                parent.setVisibility(View.VISIBLE);   
            }   
        });  
        
    	/*
    	 *创建spinner，存储i2c操作模式，存储格式 i2cbus address，比如 0 0x34 
    	 *定义下拉列表的适配器；选择下拉列表适配器的样式；添加适配器到下拉列表；定义下拉列表被选中时的响应
    	 */
		ListStr_I2CModes.add("b (byte, default)");
		//ListStr_I2CModes.add("w (word)");
		ListStr_I2CModes.add("W (word on even register addresses)");
		ListStr_I2CModes.add("s (SMBus block)");
		ListStr_I2CModes.add("i (I2C block)");
		ListStr_I2CModes.add("c (consecutive byte)");		
        Spinner_I2CModes = (Spinner)findViewById(R.id.idSpinnerI2CModes);   
        Adapter_I2CModes = new ArrayAdapter<String>(this,
        					android.R.layout.simple_spinner_item, ListStr_I2CModes);   
        Adapter_I2CModes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);   
        Spinner_I2CModes.setAdapter(Adapter_I2CModes);   
        Spinner_I2CModes.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){   
            public void onItemSelected(AdapterView parent, View view, int pos, long id) {   
                parent.setVisibility(View.VISIBLE); 
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLUE);
                ((TextView) parent.getChildAt(0)).setTextSize(24);
            }   
			public void onNothingSelected(AdapterView parent) {   
                parent.setVisibility(View.VISIBLE);   
            }   
        });   
                
        /*
         *定义textview, 显示操作结果
         */
    	TextView_Result = (TextView) findViewById(R.id.idtextViewResult); 
    	TextView_Result.setMovementMethod(new ScrollingMovementMethod());	
    	
    	/*
    	 *定义edittext, 获取read/set的操作参数
    	 */
    	EditText_ByteFirst = (EditText) findViewById(R.id.ideditTextByteFirst);
    	EditText_ByteLast = (EditText) findViewById(R.id.ideditTextByteLast);
    	EditText_ReadCycleTime = (EditText) findViewById(R.id.ideditTextCycleTime);
    	EditText_ByteNo = (EditText) findViewById(R.id.idEditTextByteNo);
    	EditText_ByteVal = (EditText) findViewById(R.id.idEditTextByteVal);   

    	EditText_Bus = (EditText) findViewById(R.id.idEditTextBus);   
    	EditText_Addr = (EditText) findViewById(R.id.idEditTextAddr);   

    	
    	//bus & addr
    	EditText_Bus.addTextChangedListener(new TextWatcher() {
	        public void afterTextChanged(Editable s) {
	        	DevAddr = EditText_Bus.getText().toString() + " 0x" + EditText_Addr.getText().toString();
	        }
	
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
	
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
    	});//endof EditText_Bus.addTextChangedListener()

    	EditText_Addr.addTextChangedListener(new TextWatcher() {
	        public void afterTextChanged(Editable s) {
	        	DevAddr = EditText_Bus.getText().toString() + " 0x" + EditText_Addr.getText().toString();
	        }
	
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
	
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
    	});//endof EditText_Bus.addTextChangedListener()
    	
    	
    	/*
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
	        		//TODO 替代全局标志的方法 非常重要 [bug20130828注释]
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
        
    	/*
    	 *read button listener 按钮事件监听函数 
    	 */
        Button_Read = (Button) findViewById(R.id.idButtonRead);          
        Button_Read.setOnClickListener(new OnClickListener() {  
            public void onClick(View v) {
            	if (boolean_LoopReadEnable == false){
            		//只读一次
                	String cmd = new String("/system/xbin/i2cdump -f -y 0 0x34");
                	do_exec(cmd);
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
        
    	/*
    	 *set button listener 按钮事件监听函数 
    	 */
        Button ButtonSet_ls = (Button) findViewById(R.id.idbuttonSet);          
        ButtonSet_ls.setOnClickListener(new OnClickListener() {  
            public void onClick(View v) {
            	//String dev = Spinner_I2CDevOnBoard.getSelectedItem().toString();
            	String dev = DevAddr;
            	String mode = String.valueOf((Spinner_I2CModes.getSelectedItem().toString()).charAt(0)).toLowerCase();
            	String cmd = Activity_TableLayout.I2CToolDir + "i2cset -f -y " + dev + " "
            			+ "0x" + EditText_ByteNo.getText().toString() + " "           			
            			+ "0x" + EditText_ByteVal.getText().toString() + " "
            			+ mode;
            	do_exec(cmd);
            	//设置完成后清屏。
    			TextView_Result.setText("");            	
            }             
        });//end of Button_Set.setOnClickListener()           	        
 		
        
        /**********************************************************************
         *activity-basic 打开阶段探测板载i2c设备 
         *call do_exec() first to init Process_ExecCmd before creating child thread.
         */
        do_exec(Activity_TableLayout.I2CToolDir + "i2cdetect -l");      

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

        //test
        String hexstr = new String("ff");
        int byteint = Integer.valueOf(hexstr, 16);        
        System.out.println(String.format("%s 0x%2x %d", "String(ff) to int", byteint, byteint));
        
    } /*end of onCreate()*****************************************************/

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
    
    private String read_cmd_get(){
        //Usage: i2cdump [-f] [-y] [-r first-last] I2CBUS ADDRESS [MODE [BANK [BANKREG]]]
    	//i2cdump -f -y -r first-last i2cbus address mode 
    	//first和last未填写时默认 00 和 ff
    	String fisrtbyte = new String("");
    	fisrtbyte = EditText_ByteFirst.getText().toString();
    	if (fisrtbyte.compareTo("") == 0){
    		fisrtbyte = "00";        		
    	}       	
    	
    	String lastbyte = new String("");
    	lastbyte = EditText_ByteLast.getText().toString();
    	if (lastbyte.compareTo("") == 0){
    		lastbyte = "FF";        		
    	}      	
    	
    	//String dev = Spinner_I2CDevOnBoard.getSelectedItem().toString();
    	String dev = DevAddr;
    	
    	String mode = String.valueOf((Spinner_I2CModes.getSelectedItem().toString()).charAt(0));

    	//Range parameter not compatible with i&s mode
    	String range = new String("");
    	if ((mode.compareTo("i") != 0) && (mode.compareTo("s") != 0)){
    		range = " -r" + " " + "0x" + fisrtbyte + "-" + "0x" + lastbyte;
    	}
    	
    	String cmd = Activity_TableLayout.I2CToolDir + "i2cdump -f -y" + range + " " + dev + " " + mode;
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
//    	WriteReadSDCard.writeToSDFile(File_basic, "\r\n" + PublicFunc.DateGet() + "\r\n" + cmd + "\r\n");
    	try {  
    		Process_ExecCmd = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {  
            e.printStackTrace();  
        }   
    }/*end of do_exec()*******************************************************/
    

	private void hanle_exec_output_parse_msg(Message msg){
		switch (msg.what) {
	    case 0:
	    	//display stderr & stdout message.
			TextView_Result.setText((String) msg.obj);
			break;
	    	
	    case 1:
	    	if (true == i2c_dev_detect()){
	        	//探测完所有总线后（如果上面没有错误中退出），更新下拉列表spinner;	
	    		Adapter_I2CDevOnBoard.notifyDataSetChanged();
	    		
//	    		WriteReadSDCard.writeToSDFile(File_basic, "i2cdev list:\r\n");
	        	String i2cdevall = new String("");		
	    		for (int i=0; i<ListStr_I2CDevOnBoard.size(); i++){
//	    			WriteReadSDCard.writeToSDFile(File_basic, ListStr_I2CDevOnBoard.get(i) + "\r\n");
	    			i2cdevall += ListStr_I2CDevOnBoard.get(i) + "\r\n";
	    		}
				TextView_Result.setText(i2cdevall);
				//如果探测成功，则禁止bus/addr eiittext的输入
				EditText_Bus.setFocusable(false);
				EditText_Addr.setFocusable(false);
	    	} else {
	    		//探测失败，下拉列表选择设备无法使用，只能通过edittext输入
				TextView_Result.setText("detect failed!");
				//
				Spinner_I2CDevOnBoard.setFocusable(false);
				Spinner_I2CDevOnBoard.setFocusableInTouchMode(false);
	    	}
	    	
			//该标志位用于分析stdout数据时，排除i2c detect命令返回数据类型的分析，以加快分析速度。
			boolean_I2CDetectAllFinish = true;	

	        Button_Read.performClick();	//2013-11-07 14:36		

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
        
        /* 读取stderr, 然后分析 */
    	String stderrStr = new String("");		
    	stderrStr = ExecOutputParse.StderrGet(p);		
		if (stderrStr.length() > 1){
//			WriteReadSDCard.writeToSDFile(File_basic, stderrStr + "\r\n");			
			if (true == ExecOutputParse.StderrMatch(stderrStr)){
				//send message to inform main thread change UI.
				Message msg = new Message();
				msg.obj = stderrStr;
		        msg.what = 0;
		        Handler_MsgOperate.sendMessage(msg);				
			}
    	}/*end of stderr parse*/	

        /* 读取stdout, 然后分析 */
    	String stdoutStr = new String("");
		stdoutStr = ExecOutputParse.StdoutGet(p);
		if (stdoutStr.length() > 1){
//			WriteReadSDCard.writeToSDFile(File_basic, stdoutStr + "\r\n");
	    	Message msg = new Message();
	    	// i2c设备探测必须在tool打开的初始阶段完成，所以此后在分析output数据时，
	        // 排除i2c detect bus/dev 命令返回数据类型的分析，以加快分析速度。
	        if (boolean_I2CDetectAllFinish == false){
		    	// i2cdetect to identify id-0, id-1, id-2, ...;
	           	if (true == ExecOutputParse.StdoutMatch_I2CBusDetect(stdoutStr, ListStr_I2CBusOnBoard)){ 
	           		//send message to inform main thread change UI.
	        		msg.obj = stdoutStr;
	        		msg.what = 1;
	        		Handler_MsgOperate.sendMessage(msg);
	        	// i2cdetect to identify i2c devices address.
	        	} else if (true == ExecOutputParse.StdoutMatch_I2CDevDetect(stdoutStr, 
	        								ListStr_I2CDevOnBoard, Str_I2CBusDetecting)){
	           		boolean_I2CDetectCurFinish = true;
	        		//msg.obj = s;
	        		//msg.what = 3;   
	        	}  
	        } else {
				WriteReadSDCard.writeToSDFile(File_basic, stdoutStr + "\r\n");
				//todo
				
		    	//i2cdump output parse
		    	if (true == ExecOutputParse.StdoutMatch_I2CDump(stdoutStr, I2CDumpValHexStr)){
		        	msg.obj = stdoutStr;
		            msg.what = 0;    		
			        Handler_MsgOperate.sendMessage(msg);	 
		    	}
		    	//other stdout message to display.		        	
	        } 	
    	}/*end of stdout parse*/		
    }/*end of exec_output_parse()*********************************************/

    
    private boolean i2c_dev_detect(){
    	//we have detected several i2c bus, then execute i2c device detect command,
    	if (ListStr_I2CBusOnBoard.size() < 0){
    		return false;
    	}
    	
    	for (int i=0; i<ListStr_I2CBusOnBoard.size(); i++){
    		//200ms is short for i2cdetect -y bus
			Str_I2CBusDetecting = ListStr_I2CBusOnBoard.get(i);
			do_exec(Activity_TableLayout.I2CToolDir + "i2cdetect -y " + Str_I2CBusDetecting);
			//为了保证命令与返回数据对应，设置标志位false用于延迟下次命令的执行，
			//上次命令的返回数据正确解析后，置标志位true使得下次命令得以执行。
			boolean_I2CDetectCurFinish = false;
			int timeoutCtr = 0;
    		while (boolean_I2CDetectCurFinish == false){
            	//todo 如果标志位未被置true，程序陷在while中，超时退出打印错误信息.
				try {
                    Thread.currentThread();
					Thread.sleep(100);
					timeoutCtr++;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (timeoutCtr > 100){
					return false;
				}
        	}
    	}
    	
        return true;	
    }/*end of i2c_dev_detect()************************************************/
    	

	
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