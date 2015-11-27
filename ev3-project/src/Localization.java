import java.util.ArrayList;

import lejos.hardware.Sound;
import lejos.utility.Delay;

public class Localization {
	public static final int ROTATION_SPEED = 200;
	private static final double sensordis=11.6;
	private int limitdis=32;
	private Odometer odo;
	private Navigation navigate;
	private ObjectDetector detector;
	private ArrayList<Double> Th=new ArrayList<Double>();
	
	public Localization(Odometer odo, ObjectDetector detector){
		this.odo = odo;
		this.navigate=new Navigation(odo);
		this.detector=detector;
	}
	
	public void doLocalization() {
		double angleA, angleB, deltaT;
		angleA=0;
		angleB=0;
		boolean getangleA=false;
		boolean getangleB=false;
		// rotate the robot until it sees wall
		while(detector.getrealdis()>=limitdis && getangleA==false){
			Delay.msDelay(25);
			navigate.setSpeeds(0-ROTATION_SPEED,ROTATION_SPEED);
		}
		// keep rotating until sees no wall, and record angle A
		while(detector.getrealdis()<limitdis && getangleA==false){
			Delay.msDelay(25);
			navigate.setSpeeds(0-ROTATION_SPEED,ROTATION_SPEED);
			if(detector.getrealdis()>=limitdis){
				angleA=odo.getAng();
				getangleA=true;
			}
		}
		// switch direction and wait until it sees wall
		while(detector.getrealdis()>=limitdis && getangleB==false && getangleA==true){
			Delay.msDelay(25);
			navigate.setSpeeds(ROTATION_SPEED,0-ROTATION_SPEED);
		}
		// keep rotating until sees no wall, and record the angle B
		while(detector.getrealdis()<limitdis && getangleB==false && getangleA==true){
			Delay.msDelay(25);
			navigate.setSpeeds(ROTATION_SPEED,0-ROTATION_SPEED);
			if(detector.getrealdis()>=limitdis){
				angleB=odo.getAng();
				getangleB=true;
			}
		}
		
		if(angleA<angleB){
			deltaT=45-(angleA+angleB)/2;
		}else{
			deltaT=225-(angleA+angleB)/2;
		}
		// update the odometer position
		odo.setPosition(new double [] {0.0, 0.0, odo.getAng()+deltaT}, new boolean [] {false,false,true});
		
		navigate.travelTo(8, 8);
		
		// start rotating and clock all 4 grid lines
		int count=0;
		while(count<4){
			Delay.msDelay(25);
			navigate.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
			if(detector.getdeltacolor2()>5){
				Th.add(odo.getAng()-180.0);
				count++;
				Sound.beep();
			}
		}
		navigate.setSpeeds(0,0);
		double thetaY=Th.get(2)-Th.get(0);
		double thetaX=Th.get(3)-Th.get(1);
		double x=(0-sensordis)*Math.cos(Math.toRadians(thetaY)/2);
		double y=(0-sensordis)*Math.cos(Math.toRadians(thetaX)/2);
		double delta=Th.get(0)+thetaY/2;
		odo.setPosition(new double [] {x, y, odo.getAng()-delta}, new boolean [] {true,true,true});
		// when done travel to (0,0) and turn to 0 degrees
		navigate.travelTo(0.0, 0.0);
		navigate.turnTo(0, true);
	}
	
}
