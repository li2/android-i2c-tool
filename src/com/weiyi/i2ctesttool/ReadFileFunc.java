package com.weiyi.i2ctesttool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;



public class ReadFileFunc
{
	/**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public static void readFileByLines(String fileName, String[] s) {
        File file = new File(fileName);
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            // 一次读入一行，直到读入null为文件结束
            while ((line = reader.readLine()) != null) {
            	s[0] += line + "\r\n";
            }
            //System.out.println(s);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
    } // end of class ReadFileFunc{}
        
}