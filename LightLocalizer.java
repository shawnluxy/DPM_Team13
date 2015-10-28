import java.util.ArrayList;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;	
	private Navigation navigate;
	private ArrayList<Double> Th=new ArrayList<Double>();
	private static final double sensordis=12.5;
	
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.navigate=new Navigation(odo);
	}
	
	public void doLocalization() {
		// drive to location listed in tutorial
		navigate.travelTo(10, 10);
		navigate.setFloat();
		// start rotating and clock all 4 grid lines
		int count=0;
		while(count<4){
			Delay.msDelay(20);
			navigate.setSpeeds(-80, 80);
			if(this.getcolordata()<45){
				Th.add(odo.getAng()-180.0);
				count++;
				Sound.beep();
			}
		}
		navigate.setFloat();
		// do trig to compute (0,0) and 0 degrees
		double thetaY=Th.get(2)-Th.get(0);
		double thetaX=Th.get(3)-Th.get(1);
		double x=(0-sensordis)*Math.cos(Math.toRadians(thetaY)/2);
		double y=(0-sensordis)*Math.cos(Math.toRadians(thetaX)/2);
		double deltaT=Th.get(0)+thetaY/2;
		odo.setPosition(new double [] {x, y, odo.getAng()-deltaT}, new boolean [] {true,true,true});
		// when done travel to (0,0) and turn to 0 degrees
		navigate.travelTo(0.0, 0.0);
		navigate.turnTo(0, true);
	}
	public int getcolordata(){
		colorSensor.fetchSample(colorData, 0);
		int color=(int)(colorData[0]*100);
		return color;
	}
}
