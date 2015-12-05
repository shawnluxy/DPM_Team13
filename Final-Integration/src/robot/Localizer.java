package robot;

import java.util.ArrayList;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
import lejos.robotics.geometry.Point2D;
import lejos.utility.Delay;

/**
 * This class performs the localization routine that will allow the robot to be aware of where in the
 * field it is, and provide the accurate initial information to the odometer. To be modified and filled in.
 * 
 * @version 2.0
 * @author Shawn Lu
 */
public class Localizer {
	
	private static final int ROTATION_SPEED = 160;
	private static final double SENSORDIS = 11.2;
	private int limitdis = 32;
	private ObjectDetector detector;
	private Odometer odo;
	private Navigator navigate;
	private ArrayList<Double> Th = new ArrayList<Double>();
	
	/**
	 * Default constructor
	 * @param obDetector the ObjectDetector
	 */
	public Localizer(ObjectDetector obDetector){
		this.detector = obDetector;
		this.navigate = obDetector.getNavi();
		this.odo = navigate.getOdo();
	}
	
	/**
	 * This method performs the localization routine using an ultrasonic sensor. By the end of it, the robot should have moved to its 0,0 coordinate 
	 * (based on the new coordinate system as defined in the previous method)
	 */
	public void doLocalization(){
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
		
		// travel closer to the 0,0 on the real grid for the light localization sequence.
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
		double x=(0-SENSORDIS)*Math.cos(Math.toRadians(thetaY)/2);
		double y=(0-SENSORDIS)*Math.cos(Math.toRadians(thetaX)/2);
		double delta=Th.get(0)+thetaY/2;
		odo.setPosition(new double [] {x, y, odo.getAng()-delta}, new boolean [] {true,true,true});
		// when done travel to (0,0) and turn to 0 degrees
		navigate.travelTo(0.0, 0.0);
		navigate.turnTo(0, true);
	}
	
}
