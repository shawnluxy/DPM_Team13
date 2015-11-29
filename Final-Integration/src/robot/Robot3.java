package robot;

import java.io.IOException;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import lejos.robotics.geometry.Point2D;

/**
 * An autonomous robot capable of finding and manipulating Styrofoam blocks, 
 * while navigating within an enclosed area populated with known obstacles randomly placed with a 12 x 12 enclosure.
 * 
 * This is the main robot class from which the program will be executed and all the operations will be called.
 * 
 * @version 2.0
 * @author Solvie Lee
 *
 */
public class Robot3 {
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3LargeRegulatedMotor armMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	private static final EV3LargeRegulatedMotor sensorMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final Port usPort = LocalEV3.get().getPort("S1");		
	private static final Port colorPort = LocalEV3.get().getPort("S2"); //front color sensor
	private static final Port colorPort2 = LocalEV3.get().getPort("S3"); //bottom color sensor
	private static Display display;
	private static Navigator navi;
	private static Odometer odo;
	private static ObjectDetector obDetector;
	private static Localizer loca;
	private static PathFinder pathFinder;
	private static Search search;
	private static Flagcapturer flagCapturer;
	private static Point2D[] obstacles, landmarks;
	private static final int NUM_OBSTACLES = 20;
	private static final String SERVER_IP = "192.168.43.6";
	private static final int TEAM_NUMBER = 13;
	private static double homeZoneBL_X, homeZoneBL_Y, opponentHomeZoneBL_X, opponentHomeZoneBL_Y,opponentHomeZoneTR_X, opponentHomeZoneTR_Y,
	dropZone_X, dropZone_Y;
	private static int flagType, opponentFlagType;
	

	public static void main(String[] args){
		//Set up the ultrasonic sensor
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];
		
		//Set up the color sensor for object detection
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getMode("RGB");//TODO: change mode?
		float[] colorData = new float[colorValue.sampleSize()];
		
		//Set up light sensor for odo
		SensorModes colorSensor2 = new EV3ColorSensor(colorPort2);
		SampleProvider colorValue2 = colorSensor2.getMode("ColorID");//TODO: change mode?
		float[] colorData2 = new float[colorValue2.sampleSize()];
	

		
		odo = new Odometer(leftMotor, rightMotor);
		navi = new Navigator(odo, sensorMotor);
		obDetector = new ObjectDetector(navi, usValue, usData, colorValue, colorData, colorValue2, colorData2, true);
		loca = new Localizer(obDetector);
		pathFinder = new PathFinder(obDetector);
		flagCapturer = new Flagcapturer(armMotor);
		obstacles = new Point2D[NUM_OBSTACLES];
		
		
		
	/*//-----------------------SET UP WIFI-------------------------//
		WifiConnection conn = null;
		try {
			conn = new WifiConnection(SERVER_IP, TEAM_NUMBER);
		} catch (IOException e) {
			LCD.drawString("Connection failed", 0, 8);
		}
		
		// example usage of Transmission class
		Transmission t = conn.getTransmission();
		if (t == null) {
			LCD.drawString("Failed to read transmission", 0, 5);
		} else {
			t.coordinatesTransfo(8);
			StartCorner corner = t.getStartingCorner();
			homeZoneBL_X = t.homeZoneBL_X*30.48;
			homeZoneBL_Y = t.homeZoneBL_Y*30.48;
			opponentHomeZoneBL_X = t.opponentHomeZoneBL_X*30.48;
			opponentHomeZoneBL_Y = t.opponentHomeZoneBL_Y*30.48;
			opponentHomeZoneTR_X = t.opponentHomeZoneTR_X*30.48;
			opponentHomeZoneTR_Y = t.opponentHomeZoneTR_Y*30.48;
			dropZone_X = t.dropZone_X*30.48;
			dropZone_Y = t.dropZone_Y*30.48;
			flagType = t.flagType;
			opponentFlagType = t.opponentFlagType;
			
			// print out the transmission information
			conn.printTransmission();
		}
		// stall until user decides to end program
		//Button.ESCAPE.waitForPress(); 

		//------------------------------------------------------------//*/
		LCD.clear();
		display = new Display();
		display.setPart(0);
		
		/*
		int option = 0;
		while (option == 0)
			option = Button.waitForAnyPress(); // ID Right executes
		
		switch (option) {
		case Button.ID_RIGHT:
			execute();
			break;
		default:
			System.out.println("Error - invalid button");
			System.exit(-1);
			break;
		}
		*/
		
		opponentHomeZoneBL_X = 65;
		opponentHomeZoneBL_Y = 60;
		opponentHomeZoneTR_X = 95;
		opponentHomeZoneTR_Y = 90;
		
		execute();
	}
	/**
	 * This method executes the main program. The robot will wait until it is given its coordinates and its 
	 * Destination, and once it determines a coordinate system relative to its being in a 0,0 position,
	 * it will perform the localization and block finding routines in sequence.
	 */
	public static void execute(){
		//(package for Wifi communications not available yet on mycourses.) 
		
		//TODO: wait for information from computer about its coordinates and enemy base, etc. 
		localize();
		findEnemyBase();
		//findFlag();
		//captureFlag();
		//returnHomeBase();
		//execute the rest of the program.
	}
	
	/**
	 * Localizes to the coordinates given
	 */
	public static void localize(){
		display.setPart(1);
		loca.doLocalization();
		return;
	}
	/**
	 * This method uses the navigator to go to the enemy base while avoiding obstacles along the way
	 */
	public static void findEnemyBase(){
		//odo.startOdoCorrection(obDetector);
		display.setPart(2);
		double x, y; //Hardcoded to 60,60 for now
		x = opponentHomeZoneBL_X;
		y = opponentHomeZoneBL_Y;
		
		pathFinder.findPathTo(x, y, obstacles);
		return;
		
	}
	/**
	 * This method allows the robot to search the blocks in the enemy zone to find the flag
	 */
	public static void findFlag(){
		display.setPart(3);
		// search enemy zone for the flag
		navi.travelTo(opponentHomeZoneBL_X-15, opponentHomeZoneTR_Y-25);
		
		Point2D point1=new Point2D.Double(opponentHomeZoneBL_X,opponentHomeZoneBL_Y);
		Point2D point2=new Point2D.Double(opponentHomeZoneTR_X,opponentHomeZoneTR_Y);
		Point2D point3=new Point2D.Double(0,0);
		
		
		Search search=new Search(obDetector,flagCapturer,sensorMotor);
		search.searching(point1, point2, point3, opponentFlagType, 3);
	}
	
	/**
	 * This method uses the robot's arm to capture the flag
	 */
	public static void captureFlag(){
		display.setPart(4);
		//capture the flag with the arm

	}
	
	/**
	 * This method allows the robot to return to the home base and drop the block
	 */
	public static void returnHomeBase(){
		display.setPart(2);
		//return the flag to the destination
		double x=0, y=0;
		//TODO set x and y to the coordinates of the home base
		pathFinder.findPathTo(x,y, obstacles);
		
	}
	
}
