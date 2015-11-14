import java.util.ArrayList;
import java.util.Collections;

import lejos.hardware.lcd.LCD;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.utility.Timer;
import lejos.utility.TimerListener;


public class ObjectDetector implements TimerListener{
	private final int PERIOD = 25;
	private Timer timer;
	private SampleProvider usSensor;
	private float[] usData;
	private SampleProvider colorSensor;
	private float[] colorData;
	private SampleProvider colorSensor2;
	private float[] colorData2;
	private ArrayList<Integer> Data=new ArrayList<Integer>();
	private int realdistance;
	private int lastcolor2;
	private int deltacolor2;
	private int filter;
	private int filter_out=10;
	
	public ObjectDetector(SampleProvider usSensor, float[] usData, SampleProvider colorSensor, float[] colorData,SampleProvider colorSensor2, float[] colorData2){
		this.usSensor=usSensor;
		this.usData=usData;
		this.colorSensor=colorSensor;
		this.colorData=colorData;
		this.colorSensor2=colorSensor2;
		this.colorData2=colorData2;
		this.realdistance=getdistance();
		this.lastcolor2=getcolor2();
		this.timer = new Timer(PERIOD, this);
		this.timer.start();
	}
	
	@Override
	public void timedOut() {
		int lastrealdis=realdistance;
		Data.clear();
		for(int i=0;i<46;i++){
			Data.add(this.getdistance());
			Delay.usDelay(500);
		}
		Collections.sort(Data);
		int currentrealdis=Data.get(Data.size()/2);
		synchronized (this){
			int diff=Math.abs(currentrealdis-lastrealdis);
			if(diff>20 && filter<filter_out){
				filter++;
			}else{
				realdistance=currentrealdis;
				filter=0;
			}
		}
		
		deltacolor2=this.getcolor2()-lastcolor2;
		lastcolor2=this.getcolor2();
	}
	
	public int getdistance(){
		synchronized (this){
			usSensor.fetchSample(usData, 0);
			int distance = (int)(usData[0]*100.0);
			// filter infinity
			if(distance==Integer.MAX_VALUE){
				distance=255;}
			return distance;
		}
	}
	
	public int getcolor1(){
		synchronized (this){
			colorSensor.fetchSample(colorData, 0);
			int color=(int)(colorData[0]*100);	
			return color;
		}
	}
	
	public int getcolor2(){
		synchronized (this){
			colorSensor2.fetchSample(colorData2, 0);
			int color=(int)(colorData2[0]);	
			return color;
		}
	}
	
	public int getrealdis(){
		synchronized (this){
			return realdistance;}
	}
	
	public int getdeltacolor2(){
		synchronized (this){
			return deltacolor2;}
	}
}
