package com.mitac.android.i2ctool;

import java.util.ArrayList;
import java.util.List;

public class I2cParserHelper {

    private static final int I2C_DUMP_MODE_W_LINES = 16; // data lines in W mode(word on even register addresses)

    private static final String I2C_DUMP_MODE_W_PATTERN = "(.*)([\\s\\S]*)0123456789abcdef([\\s\\S]*)";
    
    // if @s contains the special @pattern, then return true.
    private static boolean matchPattern(String s, String pattern){
        return s.matches(pattern);
    }
    
    /*标准错误输出匹配*/
    public static boolean StderrMatch(String s){
        //std err 本来包含 询问是否继续操作"I will probe file /dev/i2c-1, address 0x10, mode i2c block Continue? [Y/n]"
        //然后需要向标准输入中写入y，
        //但由于命令有选项 -y, 可以忽略上述交互操作，所以stderr目前可以不分析而直接显示。
        return true;
    }
    

    //先检查i2cdetect/i2cdump/i2cset是否存在
    // i2cdump: No such file or directory
    // 废弃，不使用这种方法，2013-09-04
    private static boolean StderrMatch_ToolNotExit(String s){
        String pattern_stderr_i2cdetectnotexit = "(.*)([\\s\\S]*)i2cdetect:(.*)No(.*)such(.*)file(.*)([\\s\\S]*)";
        String pattern_stderr_i2cdumpnotexit = "(.*)([\\s\\S]*)i2cdump:(.*)No(.*)such(.*)file(.*)([\\s\\S]*)";
        String pattern_stderr_i2csetnotexit = "(.*)([\\s\\S]*)i2cset:(.*)No(.*)such(.*)file(.*)([\\s\\S]*)";

        if (true == matchPattern(s, pattern_stderr_i2cdetectnotexit)){
            System.out.println("pattern_stderr_i2cdetectnotexit mach success.");
            System.out.print(s);            
            return true;
        } else if (true == matchPattern(s, pattern_stderr_i2cdumpnotexit)){
            System.out.println("pattern_stderr_i2cdumpnotexit mach success.");
            System.out.print(s);            
            return true;
        } else if (true == matchPattern(s, pattern_stderr_i2csetnotexit)){
            System.out.println("pattern_stderr_i2csetnotexit mach success.");
            System.out.print(s);            
            return true;
        } else {
            //i2cdetect, i2cdump, i2cset 3个文件全部存在
            return false;           
        }
    }    
    
    
    
    /*
     * i2c-detect -l
     * output example is:
        i2c-0   i2c             sun5i-i2c.0                             I2C adapter
        i2c-1   i2c             sun5i-i2c.1                             I2C adapter
        i2c-2   i2c             sun5i-i2c.2                             I2C adapter 
     *
     * split by "i2c-", then you will get
     * 0i2c...
     * 1i2c...
     * 2i2c...
     * 
     * i2c bus num is the first char.
     */
    public static boolean StdoutMatch_I2CBusDetect(String s, List<String> buslist){
        String pattern_stdout_i2cbus = "(.*)([\\s\\S]*)i2c-(.*)i2c(.*)I2C(.*)adapter(.*)([\\s\\S]*)";

        // i2cdetect to identify id-0, id-1, id-2, ...;
        if (true == matchPattern(s, pattern_stdout_i2cbus)){
            String s_split[] = s.split("i2c-");
            for (int i=0; i<s_split.length; i++){
                //System.out.println(s_split[i]);
                //s_split[] length is 4, but only 3 is useful, so jude > 0
                if (s_split[i].length() > 0){
                    buslist.add(String.valueOf(s_split[i].charAt(0)));
                }
            } 
            return true;
        }
        return false;
    }
    

    /*
     * i2cdetect -y 2
     * output example is:
             0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
        00:          -- -- -- -- -- -- -- -- -- -- -- -- --
        10: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
        20: -- -- -- -- -- -- -- -- -- 29 -- -- -- -- -- --
        30: -- -- -- -- -- -- -- -- UU -- -- -- -- -- -- --
        40: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
        50: -- -- UU -- -- -- -- -- -- -- -- -- -- -- -- --
        60: -- -- -- -- -- -- -- -- -- 69 -- -- -- -- -- --
        70: -- -- -- -- -- -- -- --
     * "--". The address was probed but no chip answered.
     * "UU". Probing was skipped, because this address is currently in use by a driver. 
     *      This strongly suggests that there is a chip at this address.
     * An address number in hexadecimal, e.g. "2d" or "4e". 
     *      A chip was found at this address.
     */
    public static boolean StdoutMatch_I2CDevDetect(String s, List<String> devlist, String busnum){
        String pattern_stdout_i2cdev = 
                "(.*)([\\s\\S]*)(.*)00:(.*)--([\\s\\S]*)";

        // i2cdetect to identify device attach to bus
        if (true == matchPattern(s, pattern_stdout_i2cdev)){
            //
            List<String> i2c_dev_list = new ArrayList<String>();
            //
            char[] tempchar = new char[2];

            //split by "\r\n", then parse every line. 
            //Because line length is difference, is difficult to parse.
            String s_split[] = s.split("\r\n");
            //for (int i=0; i<s_split.length; i++){
            //  System.out.println(s_split[i]);
            //} 
            //
            int char_index = 0, dev_index = 0;
            for (int y=1; y<9; y++){
                for (int x=0; x<0x10 && dev_index<0x78; x++ ){
                    char_index = 4 + x*3;
                    dev_index = x + (y-1)*16;
                    s_split[y].getChars(char_index, char_index+2, tempchar, 0);
                    i2c_dev_list.add(String.valueOf(tempchar));
                }
            }
            //
            for (int i=3; i<i2c_dev_list.size() && i<0x78; i++){
                if (i2c_dev_list.get(i).compareTo("--") != 0){
                    //把"总线号" " 0x" "地址"组合成字符串, 注意总线号和0x之间有空格. 
                    //TODO 增加"设备名称"
                    devlist.add(busnum + " 0x" + Integer.toHexString(i));
                }
            }
            return true;
        }   
        
        return false;
    }

    
    /*
     * i2cdump -y -f 1 0x10 i or W
     * output example is:
             0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f    0123456789abcdef
        00: 40 00 00 1b 00 ff 00 00 00 00 00 00 12 42 12 93    @..?........?B??
        10: 00 00 00 1b 00 00 00 00 00 00 01 00 00 03 00 00    ...?......?..?..
        20: 40 00 00 1b 00 ff 00 00 00 00 00 00 12 42 12 93    @..?........?B??
        30: 20 00 00 1b 00 00 00 00 00 00 01 00 00 03 00 00     ..?......?..?..
        40: 40 00 00 1b 00 ff 00 00 00 00 00 00 12 42 12 93    @..?........?B??
        50: 40 00 00 1b 00 00 00 00 00 00 01 00 00 03 00 00    @..?......?..?..
        60: 40 00 00 1b 00 ff 00 00 00 00 00 00 12 42 12 93    @..?........?B??
        70: 60 00 00 1b 00 00 00 00 00 00 01 00 00 03 00 00    `..?......?..?..
        80: 40 00 00 1b 00 ff 00 00 00 00 00 00 12 42 12 93    @..?........?B??
        90: 80 00 00 1b 00 00 00 00 00 00 01 00 00 03 00 00    ?..?......?..?..
        a0: 40 00 00 1b 00 ff 00 00 00 00 00 00 12 42 12 93    @..?........?B??
        b0: a0 00 00 1b 00 00 00 00 00 00 01 00 00 03 00 00    ?..?......?..?..
        c0: 40 00 00 1b 00 ff 00 00 00 00 00 00 12 42 12 93    @..?........?B??
        d0: c0 00 00 1b 00 00 00 00 00 00 01 00 00 03 00 00    ?..?......?..?..
        e0: 40 00 00 1b 00 ff 00 00 00 00 00 00 12 42 12 93    @..?........?B??
        f0: e0 00 00 1b 00 00 00 00 00 00 01 00 00 03 00 00    ?..?......?..?..

        TODO -r first-last
        i2cdump -f -y -r 0-17 0 0x34
             0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f    0123456789abcdef
        00: fa 30 00 41 c8 e4 00 00 2c 00 00 00 00 00 00 0e    ?0.A??..,......?
        10: 01 00                       
     */
    public static ArrayList<String> parseI2cDumpMode_W(ArrayList<String>list){
        if (list == null || list.size() == 0) {
            return null;
        }        
        if (!matchPattern(list.get(0), I2C_DUMP_MODE_W_PATTERN)){
            return null;
        }
        
        ArrayList<String> result = new ArrayList<String>();        
        //parse every line. 
        //Because line length is difference, is difficult to parse.            
        char[] tempchar = new char[2];  //从特定位置连续取2个字符
        int char_index;                 //字符在文本行中的下标；
        int x;                          //i2cdump stdout列序号, 0~15总计16列；
        int y;                          //i2cdump stdout行序号, 0~16 总计17行，数据是1~16行
        int dumpindex;                  //字节在数组中的下标
        
        int y_first = Integer.valueOf(String.valueOf(list.get(1).charAt(0)), 16);
        String tempstr = new String("");
        
        dumpindex = y_first*16;
        //遍历行
        for (y=1; y<list.size() && y <= I2C_DUMP_MODE_W_LINES; y++){
            //遍历列
            for (x=0; x<16 && dumpindex<256; x++, dumpindex++){
                char_index = 4 + x*3;
                list.get(y).getChars(char_index, char_index+2, tempchar, 0); //如果+1的话，显示乱码
                tempstr = String.valueOf(tempchar);
                if (tempstr.compareTo("  ") == 0){
                    result.add(dumpindex, "00");
                } else {
                    result.add(dumpindex, tempstr);
                }
            }
        }
        return result;
    }       
}
