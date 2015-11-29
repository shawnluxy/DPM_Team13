package robot;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.utility.Timer;
import lejos.utility.TimerListener;
/**
 * This class will display data such as the robot's position and whether there is an object in front of it.
 * 
 * @version 0.5
 * @author Solvie Lee
 */
public class Display implements TimerListener {

	public static final int LCD_REFRESH = 200;
	private Odometer odo;
	private Timer lcdTimer;
	private TextLCD LCD = LocalEV3.get().getTextLCD();
	private double[] pos;
	private boolean objectDetected;
	private int pt, blockDetected, distance, color;
	
	/**
	 * Default constructor
	 */
	public Display() {
		this.odo = null;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		this.objectDetected = false;
		this.blockDetected = -1;
		this.pos = new double[3];
		this.pt = 0;
		lcdTimer.start();
	}

	/**
	 * Displays the proper screen for each part.
	 */
	public void timedOut() {
		// do case switch with this. 
	switch(pt){	
		case (0):
			displayOne();
			break;	
		case (1):
			displayLocalization();
			break;
		case (2):
			displayNavigation();
			break;
		case (3):
			displayBlockFinding();
			break;
		case (4):
			displayBlockCapturing();
			break;
		default:
			System.exit(-1);
			break;
		}
	}
	
	/**
	 * Method that sets what part of the game the robot is in, so that it can display the relevant information.
	 * @param pt 0 for main screen, 1 for localization, 2 for navigation, 3 for block finding, 4 for capture
	 */
	public void setPart(int pt){
		this.pt = pt;
	}
	
	/**
	 * Displays the main screen before the robot has begun executing the block finding sequence
	 */
	public void displayOne(){
		LCD.clear();
		LCD.drawString("Press Right to Begin", 0, 0);
	}
	
	/**
	 * Displays the odometry values for localization
	 */
	public void displayLocalization(){ //Displays information
		//TODO
		LCD.drawString("Localizing", 0, 0);
		
	}
	
	/**
	 * Displays the odometry values
	 */
	public void displayNavigation(){ //Displays information
		//TODO
		
	}
	
	/**
	 * Displays whether there is an object in front of it and what color it is.
	 */
	public void displayBlockFinding(){ //Displays information
		//TODO
		
	}
	
	/**
	 * Display screen for when block is being captured.
	 */
	public void displayBlockCapturing(){ //Displays information
		//TODO
		
	}
	
	
}