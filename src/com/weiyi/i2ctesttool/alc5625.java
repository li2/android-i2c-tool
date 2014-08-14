package com.weiyi.i2ctesttool;



public class alc5625
{
	private static String ItemLogFormat = new String("%-10s");

	private static int ItemTotal = 20;
	
 	public static ItemClass[] ItemArray = new ItemClass[ItemTotal];	
 	
 	public static void ItemArrayInit(){
 		int index;
 		for (index=0; index<ItemTotal; index++){
 			ItemArray[index] = new ItemClass();
 		}
 		
 		index = 0;
 		ItemArray[index].itemName = "REG02";
 		ItemArray[index].index = 2;
 		index++;
 		ItemArray[index].itemName = "REG04";
 		ItemArray[index].index = 4;
 		index++;
 		ItemArray[index].itemName = "REG06";
 		ItemArray[index].index = 6;
 		index++;
 		ItemArray[index].itemName = "REG0A";
 		ItemArray[index].index = 0xa;
 		index++;
 		ItemArray[index].itemName = "REG0C";
 		ItemArray[index].index = 0xc;
 		index++;
 		ItemArray[index].itemName = "REG0E";
 		ItemArray[index].index = 0xe;
 		index++;
 		ItemArray[index].itemName = "REG10";
 		ItemArray[index].index = 0x10;
 		index++;
 		ItemArray[index].itemName = "REG12";
 		ItemArray[index].index = 0x12;
 		index++;
 		ItemArray[index].itemName = "REG14";
 		ItemArray[index].index = 0x14;
 		index++;
 		ItemArray[index].itemName = "REG18";
 		ItemArray[index].index = 0x18;
 		index++;
 		ItemArray[index].itemName = "REG1A";
 		ItemArray[index].index = 0x1a;
 		index++;
 		ItemArray[index].itemName = "REG1C";
 		ItemArray[index].index = 0x1c;
 		index++;
 		ItemArray[index].itemName = "REG1E";
 		ItemArray[index].index = 0x1e;
 		index++;
 		ItemArray[index].itemName = "REG22";
 		ItemArray[index].index = 0x22;
 		index++;
 		ItemArray[index].itemName = "REG2E";
 		ItemArray[index].index = 0x2e;
 		index++;
 		ItemArray[index].itemName = "REG34";
 		ItemArray[index].index = 0x34;
 		index++;
 		ItemArray[index].itemName = "REG36";
 		ItemArray[index].index = 0x36;
 		index++;
 		ItemArray[index].itemName = "REG3A";
 		ItemArray[index].index = 0x3a;
 		index++;
 		ItemArray[index].itemName = "REG3C";
 		ItemArray[index].index = 0x3c;
 		index++;
 		ItemArray[index].itemName = "REG3E";
 		ItemArray[index].index = 0x3e;
 	}
 	
	//计算状态寄存器的值,
	public static void ItemValCalc(String[] hexStrArray){
		for (int i=0; i<ItemArray.length; i++){
			ItemArray[i].valCalc(hexStrArray, ItemArray[i].index);
		}		
	}

	//向logfile中写入状态寄存器Item的名字，按照特殊的格式打包成字符串
	//名字仅在初始化时写入一次
	public static String ItemLogNames(){	
		String namelog = new String("");
		namelog += String.format(ItemLogFormat, "Time");
		for (int i=0; i<ItemArray.length; i++){
			namelog += String.format(ItemLogFormat, ItemArray[i].itemName);
		}
		namelog += "\r\n";		
		return namelog;
	}
	
	//向logfile中写入状态寄存器的值，按照特殊格式打包成字符串
	public static String ItemLogVal(){	
		String vallog = new String("");
		vallog += String.format(ItemLogFormat, PublicFunc.TimeGet());
		for (int i=0; i<ItemArray.length; i++){
			vallog += String.format(ItemLogFormat, ItemArray[i].itemStrVal);
		}
		vallog += "\r\n";		
		return vallog;
	}
	
	//向textviewResult中写入状态寄存器的结果，按照另一种特殊格式打包成字符串
	public static String ItemTextviewRes(){	
		String textviewRes = new String("");
		for (int i=0; i<ItemArray.length; i++){
			textviewRes += String.format("%-10s%s\r\n", 
					ItemArray[i].itemName, ItemArray[i].itemStrVal);
		}
		textviewRes += "\r\n";		
		return textviewRes;
	}

}



/******************************************************************************
 * 以下是状态寄存器相关class定义
 * 模仿c中的结构体，以便主程序可以通过for循环处理，
 * 而且将细节（处理函数、名称等）隐藏在class中，
 * 参考think in java #9.1, 9.2
 */
class ItemClass {
	String itemName;	//名称
	int index;			//在字节流中的序号
	String itemStrVal;	//十六进制字符串值
	String valCalc(String[] hexStrArray, int index){
		itemStrVal = hexStrArray[index] + hexStrArray[index+1];
		return itemStrVal;
	}
}
