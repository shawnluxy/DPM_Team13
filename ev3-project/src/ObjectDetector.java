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
	private int deltarealdis;
	private int lastdis;
	private int lastcolor2;
	private int deltacolor2;
	private int filter;
	private int filter_out=10;
	
	public ObjectDetector(SampleProvider usSensor, float[] usData, SampleProvider colorSensor, float[] colorData,SampleProvider colorSensor2, float[] colorData2){
		this.usSensor=usSensor;
		this.usData=usData;
		this.colorSensor=colorSensor;		// front color sensor
		this.colorData=colorData;
		this.colorSensor2=colorSensor2;		// back color sensor
		this.colorData2=colorData2;
		this.realdistance=getdistance();
		this.lastdis=realdistance;
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
			if(diff>10 && filter<filter_out){
				filter++;
			}else{
				realdistance=currentrealdis;
				filter=0;
			}
		}
		// Calculate delta distance
		deltarealdis=this.getrealdis()-lastdis;
		lastdis=this.getrealdis();
		// Calculate the delta value of color sensor in the back  
		deltacolor2=this.getcolor2()-lastcolor2;
		lastcolor2=this.getcolor2();
	}
	
	// get raw distance value
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
	
	// color sensor in the front
	public int[] getcolor1(){
		synchronized (this){
			colorSensor.fetchSample(colorData, 0);
			int[] color=new int[]{(int)(colorData[0]*1000),(int)(colorData[1]*1000),(int)(colorData[2]*1000)};
			return color;
		}
	}
	
	//light blue:1 ; red:2 ; yellow:3 ; white:4 ; dark blue:5 
	public int getcolornumber(){
		synchronized (this){
			int all=this.getcolor1()[0]+this.getcolor1()[1]+this.getcolor1()[2];
			if(all==0)
				all=1;
			double r=(double)this.getcolor1()[0]/all;
			double g=(double)this.getcolor1()[1]/all;
			double b=(double)this.getcolor1()[2]/all;
			if(r>0.65 && g<0.2)
				return 2;
			else if(r>0.4 && g>0.38)
				return 3;
			else if(b>0.4 && r<0.18)
				return 5;
			else if(r>0.3)
				return 4;
			else
				return 1;
		}
	}
	
	// color sensor in the back
	public int getcolor2(){
		synchronized (this){
			colorSensor2.fetchSample(colorData2, 0);
			int color=(int)(colorData2[0]);	
			return color;
		}
	}
	
	//get filtered distance value
	public int getrealdis(){
		synchronized (this){
			return realdistance;}
	}
	
	public int getdeltarealdis(){
		synchronized (this){
			return deltarealdis;}
	}
	// get delta value of second color sensor
	public int getdeltacolor2(){
		synchronized (this){
			return deltacolor2;}
	}
}
