/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weiyi.i2ctesttool;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.widget.LinearLayout;

public class Activity_axpDrawGrap extends Activity
{
    private static GraphicalView mChartView;

    //日期，作为x轴
    private static List<Date[]> xList = new ArrayList<Date[]>();
    //数据，做为y轴
    private static List<int[]> yList = new ArrayList<int[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_axp_drawgraph);

        //新建XY Multiple Series Renderer 渲染器
        String[] titles = new String[] { "AxpBatt%", "SystemBatt%", "BattT/°C" };
        int[] colors = new int[] { Color.GREEN, Color.BLUE, Color.RED};
        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.TRIANGLE, PointStyle.DIAMOND};
        XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);

        //设置renderer属性
        int length = renderer.getSeriesRendererCount();        
        for (int i = 0; i < length; i++) {
            ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
        }
        renderer.setXLabels(10);
        renderer.setYLabels(10);
        renderer.setShowGrid(true);
        renderer.setXLabelsAlign(Align.CENTER);
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setBackgroundColor(Color.BLACK);
        renderer.setApplyBackgroundColor(true);
        renderer.setZoomButtonsVisible(true);

        //设置表格标题、横坐标标题、纵坐标标题、纵坐标范围、坐标轴颜色、标题颜色
        setChartSettings(renderer, "Battery & Temperature", "Time", "",
                         -20, 100, Color.LTGRAY, Color.LTGRAY);

        //建立XY Multiple Series Dataset List
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        //从文件File_battery中读取time、axpbatt、systembatt、axpbattT
        getBatterData(Activity_axpmfd.File_battery.getPath());
        dataset = buildDateDataset(titles, xList, yList);
        
        //Creates a time chart view
        //@4 parameter: the date format pattern to be used for displaying the X axis date labels.
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        mChartView = ChartFactory.getTimeChartView(this, dataset, renderer, "dd/HH:mm");
        layout.addView(mChartView);

        //启动"动态添加数据线程", 如果启动，从该activity退出后，如果没有清理，会造成app死掉。
        //addDataThread.start();
    }//end of onCreate()


    //http://wiki.eoe.cn/page/Stopping_and_Restarting_an_Activity
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        //从该activity退出后，如果没有清理list, 下次再画图，曲线并没有更新，除非重启整个app
        xList.clear();
        yList.clear();
    }

    

    //"动态添加数据线程"
    public static Thread addDataThread = new Thread(new Runnable()
    {
        public void run() {
            while (true) {
                try {
                    Thread.currentThread();
                    Thread.sleep(2 * 1000);
                    //mChartView.repaint();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });


    /**
     * Builds an XY multiple series renderer.
     *
     * @param colors the series rendering colors
     * @param styles the series point styles
     * @return the XY multiple series renderers
     */
    protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles)
    {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        setRenderer(renderer, colors, styles);
        return renderer;
    }

    protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles)
    {
        renderer.setAxisTitleTextSize(16);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(15);
        renderer.setLegendTextSize(15);
        renderer.setPointSize(3f);
        renderer.setMargins(new int[] { 20, 30, 15, 20 });
        int length = colors.length;
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(colors[i]);
            r.setPointStyle(styles[i]);
            renderer.addSeriesRenderer(r);
        }
    }


    /**
     * Sets a few of the series renderer settings.
     *
     * @param renderer the renderer to set the properties to
     * @param title the chart title
     * @param xTitle the title for the X axis
     * @param yTitle the title for the Y axis
     * @param xMin the minimum value on the X axis
     * @param xMax the maximum value on the X axis
     * @param yMin the minimum value on the Y axis
     * @param yMax the maximum value on the Y axis
     * @param axesColor the axes color
     * @param labelsColor the labels color
     */
    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
                                    String yTitle, double yMin, double yMax, int axesColor,
                                    int labelsColor)
    {
        renderer.setChartTitle(title);
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        //renderer.setXAxisMin(xMin);
        //renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
    }

    /**
     * Builds an XY multiple time dataset using the provided values.
     * 
     * @param titles the series titles
     * @param xValues the values for the X axis
     * @param yValues the values for the Y axis
     * @return the XY multiple time dataset
     */
    protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<Date[]> xValues,
        List<int[]> yValues) {
      XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
      int length = titles.length;    
      for (int i = 0; i < length; i++) {
        TimeSeries series = new TimeSeries(titles[i]);
        Date[] xV = xValues.get(i);
        int[] yV = yValues.get(i);
        int seriesLength = xV.length;
        for (int k = 0; k < seriesLength; k++) {
          series.add(xV[k], yV[k]);
        }
        dataset.addSeries(series);
      }
      return dataset;
    }

    
    //从文件File_battery中读取time、axpbatt、systembatt、axpbattTem
    public static void getBatterData(String fileName){
    	List<String> ListStr_Time = new ArrayList<String>(); 
    	List<String> ListStr_AxpBatt = new ArrayList<String>(); 
    	List<String> ListStr_SysBatt = new ArrayList<String>(); 
    	List<String> ListStr_AxpBattT = new ArrayList<String>(); 
    	
    	String[] s = {new String("")};
    	ReadFileFunc.readFileByLines(fileName, s);    	
		String[] s_split = s[0].split("\r\n");
		//System.out.println("s_split[] length=" + s_split.length);
		for (int i=0; i<s_split.length; i++){
			//System.out.println(s_split[i]);
			String[] s_split2 = s_split[i].split(",");
			ListStr_Time.add(s_split2[0]);
			ListStr_AxpBatt.add(s_split2[1]);
			ListStr_SysBatt.add(s_split2[2]);
			ListStr_AxpBattT.add(s_split2[3]);
		}
		
		//打包date
		StringToTime(ListStr_Time, xList);	
		
		//打包axp battery
		int[] axp_batt = new int[ListStr_AxpBatt.size()];
		for (int i=0; i<ListStr_AxpBatt.size(); i++){
			axp_batt[i] = Integer.valueOf(ListStr_AxpBatt.get(i));
		}
		//打包system battery
		int[] sys_batt = new int[ListStr_SysBatt.size()];
		for (int i=0; i<ListStr_SysBatt.size(); i++){
			sys_batt[i] = Integer.valueOf(ListStr_SysBatt.get(i));
		}		
		//打包axp battery temperature
		int[] axp_battT = new int[ListStr_AxpBattT.size()];
		for (int i=0; i<ListStr_AxpBattT.size(); i++){
			axp_battT[i] = Integer.valueOf(ListStr_AxpBattT.get(i));
		}
		
		//xList也要同步增加
		yList.add(axp_batt);
		yList.add(sys_batt);
		yList.add(axp_battT);
    }
    
    //字符串格式时间转化为Date格式
    //因为statusBatteryPercent()中获取的是yyyyMMdd-HHmmss格式的时间，所以这里必须按同样的格式解析
    //否则出错
    public static void StringToTime(List<String> strTimeList, List<Date[]> dateList){
		try {
			Date date[] = new Date[strTimeList.size()];
			for (int i=0; i<strTimeList.size(); i++){
				date[i] = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).parse(strTimeList.get(i));	    		
			}
    		xList.add(date);
    		xList.add(date);
    		xList.add(date);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
    }
    
}//end of MainActivity