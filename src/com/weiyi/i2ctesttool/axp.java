package com.weiyi.i2ctesttool;




public class axp
{
	private static String NameSpaceVal = new String(" ");
	private static String NewLine = new String("\r\n");
	private static String ReservePartName = new String("保留不可改");

	private static String reg00h(String hexval){	
		String res = new String("");
		String regNameStr = new String("");		
		int PARTTOTAL = 8;
		String[] PartNameStr = new String[PARTTOTAL];
		String[] PartValStr = new String[PARTTOTAL];
		
		regNameStr = "REG00H 输入电源状态";
		PartNameStr[7] = "bit7 ACIN存在(是/否)";
		PartNameStr[6] = "bit6 ACIN可用(是/否)";
		PartNameStr[5] = "bit5 VBUS存在(是/否)";
		PartNameStr[4] = "bit4 VBUS可用(是/否)";
		PartNameStr[3] = "bit3 VBUS接入前大于Vhold(是/否)";
		PartNameStr[2] = "bit2 电池充电 or 电池放电";
		PartNameStr[1] = "bit1 ACIN和VBUS在PCB上短接(是/否)";
		PartNameStr[0] = "bit0 启动电源是ACIN/VBUS(是/否)";
		
		for (int bitindex=0; bitindex<PARTTOTAL; bitindex++){
			PartValStr[bitindex] = PublicFunc.HexStrToBinStr(hexval, bitindex, bitindex);
		}
		
		res = regNameStr + NameSpaceVal + "0x" + hexval + NewLine;
		for (int i=PARTTOTAL-1; i>=0; i--){
			res += "    " + PartNameStr[i] + NameSpaceVal + "[" + PartValStr[i] + "]" + NewLine;
		}
		
		return res;
	}
	

	private static String reg01h(String hexval){	
		String res = new String("");
		String regNameStr = new String("");		
		int PARTTOTAL = 8;
		String[] PartNameStr = new String[PARTTOTAL];
		String[] PartValStr = new String[PARTTOTAL];
		
		regNameStr = "REG01H 电源工作模式以及充电状态指示";
		PartNameStr[7] = "bit7 AXP209过温(是/否)";
		PartNameStr[6] = "bit6 正在充电 or 未充电或充电已完成";
		PartNameStr[5] = "bit5 电池已连接到AXP209(是/否)";
		PartNameStr[4] = "bit4 " + ReservePartName;
		PartNameStr[3] = "bit3 电池已进入激活模式(是/否)";
		PartNameStr[2] = "bit2 充电电流小于期望电流(是/否)";
		PartNameStr[1] = "bit1 " + ReservePartName;
		PartNameStr[0] = "bit0 " + ReservePartName;
		
		for (int bitindex=0; bitindex<PARTTOTAL; bitindex++){
			PartValStr[bitindex] = PublicFunc.HexStrToBinStr(hexval, bitindex, bitindex);
		}

		res = regNameStr + NameSpaceVal + "0x" + hexval + NewLine;
		for (int i=PARTTOTAL-1; i>=0; i--){
			res += "    " + PartNameStr[i] + NameSpaceVal + "[" + PartValStr[i] + "]" + NewLine;
		}
		
		return res;
	}
	
	
	private static String reg02h(String hexval){	
		String res = new String("");
		String regNameStr = new String("");		
		int PARTTOTAL = 8;
		String[] PartNameStr = new String[PARTTOTAL];
		String[] PartValStr = new String[PARTTOTAL];
		
		regNameStr = "REG02H USB OTG VBUS 状态指示";
		PartNameStr[7] = "bit7 " + ReservePartName;
		PartNameStr[6] = "bit6 " + ReservePartName;
		PartNameStr[5] = "bit5 " + ReservePartName;
		PartNameStr[4] = "bit4 " + ReservePartName;
		PartNameStr[3] = "bit3 " + ReservePartName;
		PartNameStr[2] = "bit2 VBUS有效(是/否)";
		PartNameStr[1] = "bit1 VBUS Session A/B有效(是/否)";
		PartNameStr[0] = "bit1 Session End状态有效(是/否)";
		
		for (int bitindex=0; bitindex<PARTTOTAL; bitindex++){
			PartValStr[bitindex] = PublicFunc.HexStrToBinStr(hexval, bitindex, bitindex);
		}

		res = regNameStr + NameSpaceVal + "0x" + hexval + NewLine;
		for (int i=PARTTOTAL-1; i>=0; i--){
			res += "    " + PartNameStr[i] + NameSpaceVal + "[" + PartValStr[i] + "]" + NewLine;
		}
		
		return res;
	}
	

	private static String reg30h(String hexval){	
		String res = new String("");
		String regNameStr = new String("");		
		int PARTTOTAL = 5;
		String[] PartNameStr = new String[PARTTOTAL];
		String[] PartValStr = new String[PARTTOTAL];
		
		regNameStr = "REG30H VBUS-IPSOUT通路管理";
		PartNameStr[4] = "bit7 VBUS可用时VBUS-IPSOUT通路选择控制信号";
		PartNameStr[3] = "bit6 VBUS Vhold限压控制(是/否)";
		PartNameStr[2] = "bit5:3 Vhold/V=4.0+bit*0.1";
		PartNameStr[1] = "bit2 " + ReservePartName;
		PartNameStr[0] = "bit1:0 VBUS限流控制打开时";
		
		PartValStr[4] = PublicFunc.HexStrToBinStr(hexval, 7, 7);
		PartValStr[3] = PublicFunc.HexStrToBinStr(hexval, 6, 6);
		PartValStr[2] = PublicFunc.HexStrToBinStr(hexval, 5, 3);
		PartValStr[2] = PartValStr[2] + " " + String.valueOf(Integer.valueOf(PartValStr[2], 2) * 0.1 + 4);
		PartValStr[1] = PublicFunc.HexStrToBinStr(hexval, 2, 2);
		PartValStr[0] = PublicFunc.HexStrToBinStr(hexval, 1, 0);

		if (Integer.valueOf(PartValStr[4], 2) == 1){
			PartNameStr[4] = "bit7 VBUS-IPSOUT可以被选择打开，不管N_VBUSEN pin的状态 ";
		} else if (Integer.valueOf(PartValStr[4], 2) == 0){
			PartNameStr[4] = "bit7 VBUS-IPSOUT由N_VBUSEN pin打开 ";			
		}
		
		if (Integer.valueOf(PartValStr[0], 2) == 0){
			PartNameStr[0] += " 900mA";
		} else if (Integer.valueOf(PartValStr[0], 2) == 1){
			PartNameStr[0] += " 500mA";
		} else if (Integer.valueOf(PartValStr[0], 2) == 2){
			PartNameStr[0] += " 100mA";			
		} else if (Integer.valueOf(PartValStr[0], 2) == 3){
			PartNameStr[0] += " not limit";
		}
		
		res = regNameStr + NameSpaceVal + "0x" + hexval + NewLine;
		for (int i=PARTTOTAL-1; i>=0; i--){
			res += "    " + PartNameStr[i] + NameSpaceVal + "[" + PartValStr[i] + "]" + NewLine;
		}
		
		return res;
	}
	

	private static String reg31h(String hexval){	
		String res = new String("");
		String regNameStr = new String("");		
		int PARTTOTAL = 6;
		String[] PartNameStr = new String[PARTTOTAL];
		String[] PartValStr = new String[PARTTOTAL];
		
		regNameStr = "REG31H Voff关机电压设置 ";
		PartNameStr[5] = "bit7 " + ReservePartName;
		PartNameStr[4] = "bit6 " + ReservePartName;
		PartNameStr[3] = "bit5 " + ReservePartName;
		PartNameStr[2] = "bit4 " + ReservePartName;
		PartNameStr[1] = "bit3 Sleep下PEK或GPIO唤醒使能(是/否)";
		PartNameStr[0] = "bit2:0 Voff/V=2.6+bit*0.1";
		
		PartValStr[5] = PublicFunc.HexStrToBinStr(hexval, 7, 7);
		PartValStr[4] = PublicFunc.HexStrToBinStr(hexval, 6, 6);
		PartValStr[3] = PublicFunc.HexStrToBinStr(hexval, 5, 5);
		PartValStr[2] = PublicFunc.HexStrToBinStr(hexval, 4, 4);		
		PartValStr[1] = PublicFunc.HexStrToBinStr(hexval, 3, 3);
		PartValStr[0] = PublicFunc.HexStrToBinStr(hexval, 2, 0);
		PartValStr[0] = PartValStr[0] + " "+ String.valueOf(Integer.valueOf(PartValStr[0], 2) * 0.1 + 2.6);
		
		res = regNameStr + NameSpaceVal + "0x" + hexval + NewLine;
		for (int i=PARTTOTAL-1; i>=0; i--){
			res += "    " + PartNameStr[i] + NameSpaceVal + "[" + PartValStr[i] + "]" + NewLine;
		}
		
		return res;
	}
	
	
	private static String reg32h(String hexval){	
		String res = new String("");
		String regNameStr = new String("");		
		int PARTTOTAL = 6;
		String[] PartNameStr = new String[PARTTOTAL];
		String[] PartValStr = new String[PARTTOTAL];
		
		regNameStr = "REG32H关机设置、电池检测以及CHGLED管脚控制 ";
		PartNameStr[5] = "bit7 关闭AXP209的输出(是/否)";
		PartNameStr[4] = "bit6 使能电池监测功能(是/否)";
		PartNameStr[3] = "bit5:4 CHGLED管脚功能设置";
		PartNameStr[2] = "bit3 CHGLED管脚控制设置";
		PartNameStr[1] = "bit2 输出关闭时序控制";
		PartNameStr[0] = "bit1:0 N_OE低变高后AXP209关机延迟时间";
		
		PartValStr[5] = PublicFunc.HexStrToBinStr(hexval, 7, 7);
		PartValStr[4] = PublicFunc.HexStrToBinStr(hexval, 6, 6);
		PartValStr[3] = PublicFunc.HexStrToBinStr(hexval, 5, 4);
		PartValStr[2] = PublicFunc.HexStrToBinStr(hexval, 3, 3);
		PartValStr[1] = PublicFunc.HexStrToBinStr(hexval, 2, 2);
		PartValStr[0] = PublicFunc.HexStrToBinStr(hexval, 1, 0);

		int index = 3;
		if (Integer.valueOf(PartValStr[index], 2) == 0){
			PartNameStr[index] += " 高阻";
		} else if (Integer.valueOf(PartValStr[index], 2) == 1){
			PartNameStr[index] += " 25% 1Hz闪烁";
		} else if (Integer.valueOf(PartValStr[index], 2) == 2){
			PartNameStr[index] += " 25% 4Hz闪烁";			
		} else if (Integer.valueOf(PartValStr[index], 2) == 3){
			PartNameStr[index] += " 输出低电平";
		}

		index = 2;
		if (Integer.valueOf(PartValStr[index], 2) == 1){
			PartNameStr[index] += " 由寄存器 REG32H[5:4] 控制";
		} else if (Integer.valueOf(PartValStr[index], 2) == 0){
			PartNameStr[index] += " 由充电功能控制";			
		}
		
		index = 1;
		if (Integer.valueOf(PartValStr[index], 2) == 1){
			PartNameStr[index] += " 与启动时序相反";
		} else if (Integer.valueOf(PartValStr[index], 2) == 0){
			PartNameStr[index] += " 同时关闭";			
		}
		
		index = 0;
		if (Integer.valueOf(PartValStr[index], 2) == 0){
			PartNameStr[index] += " 128ms";
		} else if (Integer.valueOf(PartValStr[index], 2) == 1){
			PartNameStr[index] += " 1s";
		} else if (Integer.valueOf(PartValStr[index], 2) == 2){
			PartNameStr[index] += " 2s";			
		} else if (Integer.valueOf(PartValStr[index], 2) == 3){
			PartNameStr[index] += " 3s";
		}
		
		res = regNameStr + NameSpaceVal + "0x" + hexval + NewLine;
		for (int i=PARTTOTAL-1; i>=0; i--){
			res += "    " + PartNameStr[i] + NameSpaceVal + "[" + PartValStr[i] + "]" + NewLine;
		}
		
		return res;
	}
	
	/**************************************************************************
	 * 控制类寄存器解析
	 */
	public static String CtrlReg(String[] valhexstr){
		String res = new String("");
		res += reg00h(valhexstr[0x00]) + "\r\n"
				+ reg01h(valhexstr[0x01]) + "\r\n"
				+ reg02h(valhexstr[0x02]) + "\r\n"
				+ reg30h(valhexstr[0x30]) + "\r\n"
				+ reg31h(valhexstr[0x31]) + "\r\n"
				+ reg32h(valhexstr[0x32]);
		return res;
	}
	
	
	

	//由一个字节和另一个字节的低4位 组合成12位的二进制字符串，
	//比如 56[7:0],57[3:0]，index是56
	//58[7:0],59[3:0]
	//7A[7:0],7B[3:0]
	public static String ByteH70_ByteL30(String[] valhexstr, int index){
		String res = new String("");
		String Hbyte = valhexstr[index];
		String Lbyte = valhexstr[index+1];
	
		//java.lang.NumberFormatException: Invalid int: "  "
		//if (Hbyte.compareTo("  ") == 0)
		//	Hbyte = "00";
		//if (Lbyte.compareTo("  ") == 0)
		//	Lbyte = "00";
		
		res = PublicFunc.HexStrToBinStr(Hbyte, 7, 0) +
				PublicFunc.HexStrToBinStr(Lbyte, 3, 0);
		return res;
	}
	
	/**************************************************************************
	 * 状态类寄存器解析
	 */
	private static String statusItemLogFormat = new String("%-14s");

 	private static StatusItemClass[] statusItemArray = {
		new StatusItemBatt(),
		
		new StatusItemAcinU(),
		new StatusItemAcinI(),
		
		new StatusItemVbusU(),
		new StatusItemVbusI(),
		
		new StatusItemAxp209T(),
		
		new StatusItemTsU(),
		new StatusItemNtcR(),
		new StatusItemBattT(),
		
		new StatusItemBattU(),
		new StatusItemBattCha(),
		new StatusItemBattDisCha(),
		
		new StatusItemIpsoutU(),
	};	
 	
 	//定义StatusBattPercentClass是为了外部引用电池电量，温度
 	public static StatusItemClass StatusBattPercentClass = statusItemArray[0];
 	public static StatusItemClass StatusItemBattTClass = statusItemArray[8];

 	
 	
	//计算状态寄存器的值,
	public static void statusItemValCalc(String[] hexStrArray){
		for (int i=0; i<statusItemArray.length; i++){
			statusItemArray[i].valCalc(hexStrArray);
		}		
	}

	//向logfile中写入状态寄存器statusItem的名字，按照特殊的格式打包成字符串
	//名字仅在初始化时写入一次
	public static String statusItemLogNames(){	
		String namelog = new String("");
		namelog += String.format(statusItemLogFormat, "Time");
		namelog += String.format(statusItemLogFormat, "SysBatt%");
		for (int i=0; i<statusItemArray.length; i++){
			namelog += String.format(statusItemLogFormat, statusItemArray[i].itemName);
		}
		namelog += "\r\n";		
		return namelog;
	}
	
	//向logfile中写入状态寄存器的值，按照特殊格式打包成字符串
	public static String statusItemLogVal(){	
		String vallog = new String("");
		vallog += String.format(statusItemLogFormat, PublicFunc.TimeGet());
		vallog += String.format(statusItemLogFormat, Activity_axpmfd.String_battPer);
		for (int i=0; i<statusItemArray.length; i++){
			vallog += String.format(statusItemLogFormat, statusItemArray[i].itemStrVal);
		}
		vallog += "\r\n";		
		return vallog;
	}
	
/*
	//向textviewResult中写入状态寄存器的结果，按照另一种特殊格式打包成字符串
	public static String statusItemTextviewRes(){	
		String textviewRes = new String("");
		for (int i=0; i<statusItemArray.length; i++){
			textviewRes += String.format("%-16s%s\r\n", 
					statusItemArray[i].itemName, statusItemArray[i].itemStrVal);
		}
		textviewRes += "\r\n";		
		return textviewRes;
	} 
 */	
	
	//向textviewResult中写入状态寄存器的结果，按照另一种特殊格式打包成字符串
	public static String statusItemTextviewRes(){	
		String textviewRes = new String("");

		textviewRes += String.format("%-14s%s\r\n",
				"SysBatt%", Activity_axpmfd.String_battPer);
		
		int i=0;
		textviewRes += String.format("%-14s%s\r\n", 
				statusItemArray[i].itemName, statusItemArray[i].itemStrVal);
		i++;
	
		textviewRes += String.format("%-14s%-8s%-14s%s\r\n", 
				statusItemArray[i].itemName, statusItemArray[i].itemStrVal,
				statusItemArray[i+1].itemName, statusItemArray[i+1].itemStrVal);
		i += 2;
		
		textviewRes += String.format("%-14s%-8s%-14s%s\r\n", 
				statusItemArray[i].itemName, statusItemArray[i].itemStrVal,
				statusItemArray[i+1].itemName, statusItemArray[i+1].itemStrVal);
		i += 2;

		textviewRes += String.format("%-14s%s\r\n", 
				statusItemArray[i].itemName, statusItemArray[i].itemStrVal);
		i++;

		textviewRes += String.format("%-14s%s\r\n", 
				statusItemArray[i].itemName, statusItemArray[i].itemStrVal);
		i++;

		textviewRes += String.format("%-14s%s\r\n", 
				statusItemArray[i].itemName, statusItemArray[i].itemStrVal);
		i++;
		
		textviewRes += String.format("%-14s%-8s%-14s%s\r\n", 
				statusItemArray[i].itemName, statusItemArray[i].itemStrVal,
				statusItemArray[i+1].itemName, statusItemArray[i+1].itemStrVal);
		i += 2;

		textviewRes += String.format("%-14s%-8s%-14s%s\r\n", 
				statusItemArray[i].itemName, statusItemArray[i].itemStrVal,
				statusItemArray[i+1].itemName, statusItemArray[i+1].itemStrVal);
		i += 2;

		textviewRes += String.format("%-14s%s\r\n", 
				statusItemArray[i].itemName, statusItemArray[i].itemStrVal);
		i++;
		
//		textviewRes += "\r\n";		
		return textviewRes;
	}

	
	//向textviewWarn中写入状态寄存器的超出范围的结果，按照另一种特殊格式打包成字符串
	public static String statusItemTextviewWarn(){	
		String textviewWarn = new String("");
		textviewWarn += PublicFunc.TimeGet() + "\r\n";
		for (int i=0; i<statusItemArray.length; i++){
			if ((statusItemArray[i].itemDoubleVal < statusItemArray[i].itemLowerBand) ||
				(statusItemArray[i].itemDoubleVal > statusItemArray[i].itemUpperBand)){
				textviewWarn += String.format("%-16s%s\r\n", 
						statusItemArray[i].itemName, statusItemArray[i].itemStrVal);				
				//textviewWarn += String.format("    [%s~%s]\r\n", 
				//		statusItemArray[i].itemLowerBand, statusItemArray[i].itemUpperBand);				

			}
		}
		textviewWarn += "\r\n";		
		return textviewWarn;
	}
}



/******************************************************************************
 * 以下是状态寄存器相关class定义
 * 模仿c中的结构体，以便主程序可以通过for循环处理，
 * 而且将细节（处理函数、名称等）隐藏在class中，
 * 参考think in java #9.1, 9.2
 */
abstract class StatusItemClass {
	//条目的名称，值的上下限(这些都是固定值，初始化后就不要更改)
	String itemName;
	double itemLowerBand;
	double itemUpperBand;
	
	//经过计算后、需要显示的十进制真实值
	String itemStrVal;
	//该条目用于判断是否超出范围
	double itemDoubleVal;
	
	//从i2c总线数据中获取该条目的字节数据流
	//某些条目仅一个字节, 某些条目是字节的拼接, 
	//所以先拼接成二进制字符串存储较合适,
	abstract double valCalc(String[] hexStrArray);	
}


//name				range		address				val
//Batt % 			0~100 		B9					原始值
class StatusItemBatt extends StatusItemClass{
	StatusItemBatt(){
		itemName = "Batt %";
		itemLowerBand = 0;
		itemUpperBand = 100;
	}
	double valCalc(String[] hexStrArray){
		int valInt = Integer.valueOf(hexStrArray[0xb9], 16);
		itemStrVal = String.valueOf(String.format("%d", valInt));
		itemDoubleVal = (double)valInt;
		return itemDoubleVal;		
	}
}

//ACIN U/V			4.4~5.25	56[7:0],57[3:0]		*1.7/1000			
class StatusItemAcinU extends StatusItemClass{
	StatusItemAcinU(){
		itemName = "ACIN U/V";
		itemLowerBand = 4.4;
		itemUpperBand = 5.25;
	}
	double valCalc(String[] hexStrArray){
		String valBinStr = axp.ByteH70_ByteL30(hexStrArray, 0x56);
		itemDoubleVal = Integer.valueOf(valBinStr, 2) * 1.7 / 1000;
		itemStrVal = String.valueOf(String.format("%.3f", itemDoubleVal));
		return itemDoubleVal;		
	}
}

//ACIN I/mA		0~800		58[7:0],59[3:0]		*0.625
class StatusItemAcinI extends StatusItemClass{
	StatusItemAcinI(){
		itemName = "ACIN I/mA";
		itemLowerBand = 0;
		itemUpperBand = 800;
	}
	double valCalc(String[] hexStrArray){
		String valBinStr = axp.ByteH70_ByteL30(hexStrArray, 0x58);
		itemDoubleVal = Integer.valueOf(valBinStr, 2) * 0.625;
		itemStrVal = String.valueOf(String.format("%.3f", itemDoubleVal));
		return itemDoubleVal;		
	}
}

//VBUS U/V			4.4~5.25	5A[7:0],5B[3:0]		*1.7/1000
class StatusItemVbusU extends StatusItemClass{
	StatusItemVbusU(){
		itemName = "VBUS U/V";
		itemLowerBand = 4.4;
		itemUpperBand = 5.25;
	}
	double valCalc(String[] hexStrArray){
		String valBinStr = axp.ByteH70_ByteL30(hexStrArray, 0x5a);
		itemDoubleVal = Integer.valueOf(valBinStr, 2) * 1.7 / 1000;
		itemStrVal = String.valueOf(String.format("%.3f", itemDoubleVal));
		return itemDoubleVal;		
	}
}

//VBUS I/mA		0~500		5C[7:0],5D[3:0]		*0.375
class StatusItemVbusI extends StatusItemClass{
	StatusItemVbusI(){
		itemName = "VBUS I/mA";
		itemLowerBand = 0;
		itemUpperBand = 500;
	}
	double valCalc(String[] hexStrArray){
		String valBinStr = axp.ByteH70_ByteL30(hexStrArray, 0x5c);
		itemDoubleVal = Integer.valueOf(valBinStr, 2) * 0.375;
		itemStrVal = String.valueOf(String.format("%.3f", itemDoubleVal));
		return itemDoubleVal;		
	}
}

//AXP209 T/℃		-40~130		5E[7:0],5F[3:0]		*0.1-144.7	
class StatusItemAxp209T extends StatusItemClass{
	StatusItemAxp209T(){
		itemName = "AXP209 T/°C";
		itemLowerBand = -40;
		itemUpperBand = 130;
	}
	double valCalc(String[] hexStrArray){
		String valBinStr = axp.ByteH70_ByteL30(hexStrArray, 0x5e);
		itemDoubleVal = Integer.valueOf(valBinStr, 2) * 0.1 - 144.7;
		itemStrVal = String.valueOf(String.format("%.1f", itemDoubleVal));
		return itemDoubleVal;		
	}
}

//TS U/mV						62[7:0],63[3:0]		*0.8
class StatusItemTsU extends StatusItemClass{
	StatusItemTsU(){
		itemName = "TS U/mV";
		itemLowerBand = 0;
		itemUpperBand = 0xfff;
	}
	double valCalc(String[] hexStrArray){
		String valBinStr = axp.ByteH70_ByteL30(hexStrArray, 0x62);
		itemDoubleVal = Integer.valueOf(valBinStr, 2) * 0.8;
		itemStrVal = String.valueOf(String.format("%.1f", itemDoubleVal));
		return itemDoubleVal;		
	}
}

//NTC R/KΩ			0.4519~263						TS/40-10
class StatusItemNtcR extends StatusItemClass{
	StatusItemNtcR(){
		itemName = "NTC R/KΩ";
		itemLowerBand = 0.4519;
		itemUpperBand = 263;
	}
	double valCalc(String[] hexStrArray){
		//通过TS U计算
		String valBinStr = axp.ByteH70_ByteL30(hexStrArray, 0x62);
		double ts = Integer.valueOf(valBinStr, 2) * 0.8;
		itemDoubleVal = ts / 40 - 10;		
		itemStrVal = String.valueOf(String.format("%.4f", itemDoubleVal));
		return itemDoubleVal;		
	}
}

//Battery T/℃										查表
class StatusItemBattT extends StatusItemClass{
	StatusItemBattT(){
		itemName = "Batt T/°C";
		itemLowerBand = -20;
		itemUpperBand = 45;
	}
	double valCalc(String[] hexStrArray){
		//通过NTC查表
		String valBinStr = axp.ByteH70_ByteL30(hexStrArray, 0x62);
		double ntc = Integer.valueOf(valBinStr, 2) * 0.8 / 40 - 10;
		itemDoubleVal = NtcToBattT(ntc);
		itemStrVal = String.valueOf(String.format("%.0f", itemDoubleVal));
		return itemDoubleVal;		
	}

	double NtcToBattT(double ntc){
		int index;
		double battT;
		//130*2, [][0]表示温度, [0][1]表示ntc阻值, 阻值降序排列
		double lookuptab[][] = {
			{-40, 228.2376},	{-39, 214.8696},	{-38, 202.3826},	{-37, 190.7126},	{-36, 179.8005},
			{-35, 169.5919}, 	{-34, 160.0366}, 	{-33, 151.0884},	{-32, 142.7046}, 	{-31, 134.8459},
			{-30, 127.4759},	{-29, 120.5608}, 	{-28, 114.0696}, 	{-27, 107.9735}, 	{-26, 102.2459},
			{-25, 96.8620}, 	{-24, 91.7990},		{-23, 87.0357}, 	{-22, 82.5523}, 	{-21, 78.3306},			
			{-20, 74.3538},		{-19, 70.6058}, 	{-18, 67.0723}, 	{-17, 63.7394}, 	{-16, 60.5946},
			{-15, 57.6261}, 	{-14, 54.8228},		{-13, 52.1745}, 	{-12, 49.6717}, 	{-11, 47.3056},		
			{-10, 45.0676},		{-9, 42.9503}, 		{-8, 40.9462}, 		{-7, 39.0487}, 		{-6, 37.2514},
			{-5, 35.5484}, 		{-4, 33.9342},		{-3, 32.4037}, 		{-2, 30.9520}, 		{-1, 29.5745},		
			{0, 28.2671},		{1, 27.0257}, 		{2, 25.8466}, 		{3, 24.7264}, 		{4, 23.6617},
			{5, 22.6495}, 		{6, 21.6869},		{7, 20.7711}, 		{8, 19.8996}, 		{9, 19.0700},
			{10, 18.2801},		{11, 17.5276}, 		{12, 16.8108}, 		{13, 16.1275},		{14, 15.4762},
			{15, 14.8550}, 		{16, 14.2625},		{17, 13.6972}, 		{18, 13.1576}, 		{19, 12.6425},
			{20, 12.1505},		{21, 11.6806}, 		{22, 11.2316}, 		{23, 10.8025}, 		{24, 10.3923},
			{25, 10.0000}, 		{26, 9.6248},		{27, 9.2658}, 		{28, 8.9223}, 		{29, 8.5934},
			{30, 8.2786},		{31, 7.9770}, 		{32, 7.6882}, 		{33, 7.4114}, 		{34, 7.1461},
			{35, 6.8919}, 		{36, 6.6480},		{37, 6.4142}, 		{38, 6.1899}, 		{39, 5.9746},
			{40, 5.7680},		{41, 5.5697}, 		{42, 5.3793}, 		{43, 5.1964}, 		{44, 5.0208},
			{45, 4.8520}, 		{46, 4.6898},		{47, 4.5339}, 		{48, 4.3840}, 		{49, 4.2398},
			{50, 4.1012},		{51, 3.9678}, 		{52, 3.8395}, 		{53, 3.7160}, 		{54, 3.5971},
			{55, 3.4826}, 		{56, 3.3724},		{57, 3.2662}, 		{58, 3.1639}, 		{59, 3.0654},
			{60, 2.9704},		{61, 2.8788}, 		{62, 2.7905}, 		{63, 2.7054}, 		{64, 2.6233},
			{65, 2.5442},		{66, 2.4678},		{67, 2.3940}, 		{68, 2.3229}, 		{69, 2.2542},
			{70, 2.1879},		{71, 2.1239}, 		{72, 2.0620}, 		{73, 2.0023}, 		{74, 1.9446},
			{75, 1.8888}, 		{76, 1.8349},		{77, 1.7828}, 		{78, 1.7324}, 		{79, 1.6837},
			{80, 1.6366},		{81, 1.5910}, 		{82, 1.5469}, 		{83, 1.5043}, 		{84, 1.4630},
			{85, 1.4231}, 		{86, 1.3844},		{87, 1.3470}, 		{88, 1.3107}, 		{89, 1.2756},
		};

		index = 0;
		if (ntc > lookuptab[index][1]){
			battT = lookuptab[index][0];
			return battT;
		}

		index = lookuptab.length-1;
		if (ntc < lookuptab[index][1]){
			battT = lookuptab[index][0];
			return battT;
		}

		//TODO 2013-09-03需要 修改为快速查表
		for (index=0; index<lookuptab.length; index++){
			if (ntc < lookuptab[index][1]){
				continue;
			}
			if (ntc < ((lookuptab[index-1][1] + lookuptab[index][1]) / 2)){
				battT = lookuptab[index][0];
			} else {
				battT = lookuptab[index-1][0];				
			}
			return battT;
		}
		
		return 0;
	}	
}

//Battery U/V		3~4.2		78[7:0],79[3:0]		*1.1/1000
class StatusItemBattU extends StatusItemClass{
	StatusItemBattU(){
		itemName = "Batt U/V";
		itemLowerBand = 3;
		itemUpperBand = 4.2;
	}
	double valCalc(String[] hexStrArray){
		String valBinStr = axp.ByteH70_ByteL30(hexStrArray, 0x78);
		itemDoubleVal = Integer.valueOf(valBinStr, 2) * 1.1 / 1000;
		itemStrVal = String.valueOf(String.format("%.2f", itemDoubleVal));
		return itemDoubleVal;		
	}
}

//Batt Cha I/mA	0~500		7A[7:0],7B[3:0]		*0.5
class StatusItemBattCha extends StatusItemClass{
	StatusItemBattCha(){
		itemName = "Batt Cha I/mA";
		itemLowerBand = 0;
		itemUpperBand = 500;
	}
	double valCalc(String[] hexStrArray){
		String valBinStr = axp.ByteH70_ByteL30(hexStrArray, 0x7a);
		itemDoubleVal = Integer.valueOf(valBinStr, 2) * 0.5;
		itemStrVal = String.valueOf(String.format("%.2f", itemDoubleVal));
		return itemDoubleVal;		
	}
}

//Batt DiscI/mA	0~1000		7C[7:0],7D[3:0]		*0.5
class StatusItemBattDisCha extends StatusItemClass{
	StatusItemBattDisCha(){
		itemName = "Batt Dis I/mA";
		itemLowerBand = 0;
		itemUpperBand = 1000;
	}
	double valCalc(String[] hexStrArray){
		String valBinStr = axp.ByteH70_ByteL30(hexStrArray, 0x7c);
		itemDoubleVal = Integer.valueOf(valBinStr, 2) * 0.5;
		itemStrVal = String.valueOf(String.format("%.2f", itemDoubleVal));
		return itemDoubleVal;		
	}
}

//IPSOUT U/V		3.4~5.25	7E[7:0],7F[3:0]		*1.4/1000
class StatusItemIpsoutU extends StatusItemClass{
	StatusItemIpsoutU(){
		itemName = "IPSOUT U/V";
		itemLowerBand = 3.4;
		itemUpperBand = 5.25;
	}
	double valCalc(String[] hexStrArray){
		String valBinStr = axp.ByteH70_ByteL30(hexStrArray, 0x7e);
		itemDoubleVal = Integer.valueOf(valBinStr, 2) * 1.4 / 1000;
		itemStrVal = String.valueOf(String.format("%.3f", itemDoubleVal));
		return itemDoubleVal;		
	}
}