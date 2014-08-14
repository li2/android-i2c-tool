//http://stackoverflow.com/questions/7358072/android-tabs-without-icons?rq=1
//http://joshclemm.com/blog/?p=136
//https://code.google.com/p/android-custom-tabs/

package com.weiyi.i2ctesttool;

import java.io.File;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;



public class Activity_TableLayout extends TabActivity {

	private TabHost mTabHost;

	private void setupTabHost() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
	}
		
	public static String I2CToolDir = new String("/system/xbin/");
//	public static String I2CTestToolDir = new String("/mnt/external_sd/i2ctools/");

	public static String SDCardPath = new String("");
	public static File I2CLogDirFile;

	
	private String releaseNote = "update note (2013-10-22):\r\n"
							+ "svn84-117\r\n"
							+ "A 增加alc5625 3个寄存器\r\n"
							+ "A 屏幕亮度调节\r\n"
							+ "A 绘制温度曲线图\r\n"
							+ "A 2min采样绘制电量曲线图（进入axp标签页后可用）\r\n"
							+ "A 获取系统电量，并写入log;\r\n"
							+ "A 增加alc5625\r\n"
							+ "A 当axp状态寄存器超出范围时，显示并写入log;\r\n"
							+ "A 通过查表显示NTC温度;\r\n"
							+ "A 增加app图标;\r\n"
							+ "M 删除si4709/axp地址选择列表;\r\n"
							+ "M 修改axp logfile中的寄存器名称;\r\n"
							+ "A 睡眠后app仍然工作;\r\n"
							+ "A 增加右上角菜单About;\r\n"
							+ "M 检查/system/xbin/i2cdetect,i2cdump,i2cset不存在时退出app."
	;
	
	
	
	/** Called when the activity is first created. */		  
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// construct the tabhost
		setContentView(R.layout.layout_table);

		//检查是否存在，若不存在，直接退出。
		if (false == checki2ctoolExist()){
			i2ctoolNoExist();
		}
		
		//初始化app log文件存储路径;
		SDCardPath = PublicFunc.getExternalSDCardPath();
		I2CLogDirFile = new File(SDCardPath + "/" +"i2clog");
		I2CLogDirFile.mkdirs();				
		
		setupTabHost();
		//mTabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);

		setupTab(new TextView(this), "basic", Activity_BasicOperate.class);
		setupTab(new TextView(this), "si4709", Activity_si4709.class);
		setupTab(new TextView(this), "axp", Activity_axpmfd.class);
		setupTab(new TextView(this), "alc5625", Activity_alc5625.class);
		setupTab(new TextView(this), "Display", Activity_ScreenCtrl.class);
	}

    
	private void setupTab(final View view, final String tag, Class cls) {
		View tabview = createTabView(mTabHost.getContext(), tag);

		Intent intent = new Intent(this, cls);

		TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(intent);
		mTabHost.addTab(setContent);
	}

	private static View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}

	
	/**************************************************************************
	 * 检查i2ctool是否存在，
	 * 有一个不存在，或者权限不对，弹出对话框提示用户，用户确定后，退出程序。
	 * 权限检查呢？ TODO
	 */
	private static boolean checki2ctoolExist(){
		boolean i2cdetect = false;
		boolean i2cdump = false;
		boolean i2cset = false;
		
		String name = new String("");		
		File[] i2ctools = new File("/system/xbin/").listFiles();

		for (int i=0; i<i2ctools.length; i++){
			name = i2ctools[i].getName();
			if (name.compareTo("i2cdetect") == 0){
				i2cdetect = true;
			} else if (name.compareTo("i2cdump") == 0){
				i2cdump = true;
			} else if (name.compareTo("i2cset") == 0){
				i2cset = true;
			}
		}
		
		return (i2cdetect && i2cdump && i2cset);
	}
	
    private void i2ctoolNoExist(){
        new AlertDialog.Builder(this)
		.setTitle("Error: APP Will Closed!")
		.setMessage("Please check files exist or not:\r\n" +
				"	/system/xbin/i2cdetect\r\n" +
				"	/system/xbin/i2cdump\r\n" + 
				"	/system/xbin/i2cset")
		.setPositiveButton("Yes, I know.", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) { 
	        finish();
	        System.exit(0);    	
		}
		})
		.show();     	
    }
	
	
	/**************************************************************************
	 * 创建右上角的菜单
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.clear();
//暂时屏蔽log文件路径选择、log删除，在程序中固定为/mnt/external_sd/i2clog/
//		menu.add(0, MenuItemIDType.SelLogDir.getNumericType(), 0, "Select Logfile Directory");
//		menu.add(0, MenuItemIDType.DelLog.getNumericType(), 0, "Delete Logfile");
		menu.add(0, MenuItemIDType.About.getNumericType(), 0, "About");
		menu.add(0, MenuItemIDType.Close.getNumericType(), 0, "Close");
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	int itemid = item.getItemId();
    	System.out.println(itemid);
    	
    	if (itemid == MenuItemIDType.DelLog.getNumericType()){			//删除logfile节省空间
        	MenuItem_DelLog();           	    		
			return true;
    	} else if (itemid == MenuItemIDType.SelLogDir.getNumericType()){//选择logfile存储位置
    		MenuItem_SelLogDir();
			return true;
    	} else if (itemid == MenuItemIDType.Close.getNumericType()){	//关闭app
    		MenuItem_CloseApp();
			return true;
    	} else if (itemid == MenuItemIDType.About.getNumericType()){	//关于，显示存储路径版本号
    		MenuItem_About();
    		return true;
    	} else {
            return super.onOptionsItemSelected(item);	    		
    	}
    		
    }
    
    private enum MenuItemIDType
    {
        SelLogDir(1),
        DelLog(2),
        Close(3),
        About(4);

        MenuItemIDType (int i) { this.type = i; }

        private int type;
        public int getNumericType() { return type; }
    }
    
    private void MenuItem_SelLogDir(){
		CharSequence[] items = new CharSequence[2];
		items[0] = "internal SD, /mnt/sdcard/i2clog/";
		items[1] = "external SD, /mnt/external_sd/i2clog/";
    	
    	//弹出一个列表供用户选择    	
		File[] mnt = new File("/mnt/").listFiles();
		for (int i=0; i<mnt.length; i++){
			if (mnt[i].getName().compareTo("sdcard") == 0){
				items[0] = "internal SD, /mnt/sdcard/i2clog/";
			} else if (mnt[i].getName().compareTo("external_sd") == 0){
				items[1] = "external SD, /mnt/external_sd/i2clog/";
			}
		}

		new AlertDialog.Builder(this)
		.setTitle("Select logfile directory")
		.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// The 'which' argument contains the index position
				// of the selected item
				System.out.println(which);
			}
		})
		.show();
    }
    
    private void MenuItem_DelLog(){
        new AlertDialog.Builder(this)
		.setTitle("Warn!")
		.setMessage("Are you sure want to delete logfile?")
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) { 
			// continue with delete
			if (I2CLogDirFile.isDirectory()) {
		        String[] children = I2CLogDirFile.list();
		        for (int i = 0; i < children.length; i++) {
		            new File(I2CLogDirFile, children[i]).delete();
		        }
		    }
		}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) { 
			// do nothing
			}
		})
		.show();     	
    }
    
    private void MenuItem_CloseApp(){
		//直接关闭不予提示
        finish();
        System.exit(0);    	
    }
    
    private void MenuItem_About(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("About");
		builder.setMessage("logfile path:" + I2CLogDirFile.getPath() + "\r\n\r\n" + releaseNote);
		AlertDialog alertDialog = builder.create();
		alertDialog.show();     	
		alertDialog.getWindow().setLayout(600, 400);
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
