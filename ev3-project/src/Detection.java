import lejos.hardware.lcd.LCD;
import lejos.robotics.SampleProvider;
import lejos.utility.Timer;
import lejos.utility.TimerListener;


public class Detection implements TimerListener{
	private static final int PERIOD=25;
	private Timer timer;
	private SampleProvider usSensor;
	private float[] usData;
	private SampleProvider colorSensor;
	private float[] colorData;
	
	public Detection(SampleProvider usSensor, float[] usData, SampleProvider colorSensor, float[] colorData){
		this.usSensor=usSensor;
		this.usData=usData;
		this.colorSensor=colorSensor;
		this.colorData=colorData;
		
		this.timer=new Timer(PERIOD,this);
		this.timer.start();
	}
	// functions to start/stop the timerlistener
	public void stop() {
		if (this.timer != null)
			this.timer.stop();
	}
	public void start() {
		if (this.timer != null)
			this.timer.start();
	}
	
	@Override
	public void timedOut() {
		// display distance and color values
		LCD.clear();
		LCD.drawString("D: "+this.getdistance(), 0, 3);
		LCD.drawString("C: "+this.getcolor(), 0, 4);
		
		if(this.getdistance()<=25){
			LCD.drawString("Object Detected!", 0, 5);
			if(this.getdistance()==3 && this.getcolor()<=8){
				LCD.drawString("Block", 0, 6);
			}else if(this.getdistance()==3 && this.getcolor()>8){
				LCD.drawString("Not Block", 0, 6);
			}
		}
	}
	
	private int getdistance(){
		usSensor.fetchSample(usData, 0);
		int distance = (int)(usData[0]*100.0);
		// filter infinity
		if(distance==Integer.MAX_VALUE){
			distance=255;}
		return distance;
	}
	private int getcolor(){
		colorSensor.fetchSample(colorData, 0);
		int color=(int)(colorData[0]*100);	
		return color;
	}
}
