package com.weiyi.i2ctesttool;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.os.Environment;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

public class PublicFunc
{
    //Limit edittext length, and restrict the chars to 0-9, a-f, A-F only
	public static void  setEditTextFilters(EditText eidttext){
	    InputFilter[] FilterArray = new InputFilter[2];

	    FilterArray[0] = new InputFilter.LengthFilter(2);
	    FilterArray[1] = new InputFilter() { 
	        public CharSequence filter(CharSequence source, int start, int end, 
	        							Spanned dest, int dstart, int dend) { 
				char input_c;
				for (int i = start; i < end; i++) { 
					input_c = source.charAt(i);
					//不是字母，或者，不是数字，返回空，相当于没有输入
					if (!Character.isLetterOrDigit(input_c)) { 
				        return ""; 
					}
					//如果是在 'f' < c <= 'z' 范围内的字符，也返回空
					if (input_c > 'f' && input_c <= 'z'){
				    	return "";
				    }
				} 
				return null; 
	        } 
	    };
	    
	    eidttext.setFilters(FilterArray);
	}//end of setEditTextFilters();

	/**************************************************************************
	 *获取时间并转换成字符串 
	 */
    public static String DateGet() {
        String DATE_FORMAT_NOW = "yyyyMMdd-HHmmss";
    	Calendar cal = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    	return sdf.format(cal.getTime());
    }
 
	/**************************************************************************
	 *获取时间并转换成字符串 
	 */
    public static String TimeGet() {
        String DATE_FORMAT_NOW = "HH:mm:ss";
    	Calendar cal = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    	return sdf.format(cal.getTime());
    }
 
    
	/**************************************************************************
	 *16进制字符串 转化为 2进制字符串
	 *比如 "31" to "00110001"
	 *然后返回截取的字符串，比如("31", 6, 2) 返回 "01100"
	 */
	public static String HexStrToBinStr(String str, int bitH, int bitL) {
		//仅用于解析一个字节
		if ((str.length() > 2) || (bitH < bitL) || (bitL < 0) || (bitH > 8))
			return "";
		
		//convert hexadecimal string to int
		int temp = Integer.parseInt(str, 16);

		//convert int to a fixed-length 8-bit binary string
		String bin = Integer.toBinaryString(0x100 | temp).substring(1);
		
		//字符串的charAt(7) 是  二进制的bit0
		return bin.subSequence(7-bitH, (7-bitL)+1).toString();
	}
    

	/**************************************************************************
     * 获取外置SD卡路径
     * http://my.eoe.cn/1028320/archive/4718.html
     * @return 存在时返回外置卡，不存在时返回内置卡
     */
    public static String getExternalSDCardPath() {
        String cmd = "cat /proc/mounts";
        Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
        try {
            Process p = run.exec(cmd);// 启动另一个进程来执行命令
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));

            String lineStr;
            while ((lineStr = inBr.readLine()) != null) {
                // 获得命令执行后在控制台的输出信息
                //System.out.println("CommonUtil:getSDCardPath"+lineStr);
                if (lineStr.contains("external_sd") && lineStr.contains("/mnt/")) {
                    String[] strArray = lineStr.split(" ");
                    if (strArray != null && strArray.length >= 5) {
                        String result = strArray[1].replace("/.android_secure",
                                "");
                        //System.out.println("getSDCardPath.restult return");
                        return result;
                    }
                }
                // 检查命令是否执行失败。
                if (p.waitFor() != 0 && p.exitValue() == 1) {
                    // p.exitValue()==0表示正常结束，1：非正常结束
                	System.out.println("CommonUtil:getSDCardPath"+ "命令执行失败!");
                }
            }
            inBr.close();
            in.close();
        } catch (Exception e) {
        	System.out.println("CommonUtil:getSDCardPath"+ e.toString());
            return Environment.getExternalStorageDirectory().getPath();
        }
        
        return Environment.getExternalStorageDirectory().getPath();
    }
	
}//end of PublicFunc.java