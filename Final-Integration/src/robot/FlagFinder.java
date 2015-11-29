package robot;

import lejos.robotics.geometry.Point2D;

/**
 * This class contains all the methods to allow for searching the enemy base for the flag. Once the flag is found and recognized
 * using the object detection information from the ObstacleDetector the FlagCapturer class is responsible for manipulating the arm of the robot to grab the flag. 
 * 
 * @version 0.0
 * 
 */
public class FlagFinder {

	private Navigator navi;
	private Odometer odo;
	private ObjectDetector obDetector;
	private boolean objectdetected, flagDetected;
	
	/** Default constructor
	 * @param obDetector the ObjectDetector
	 */
	public FlagFinder(ObjectDetector obDetector){
		this.obDetector = obDetector;
		this.navi = obDetector.getNavi();
		this.odo = navi.getOdo();
		
	}
	
	/** The search method will have the robot find and examine all of the blocks inside the enemy's base camp 
	 * until it finds the block with the correct color, the flag. 
	 * @param corners that describe the rectangle
	 */
	public void search(Point2D[] corners){
		//execute a search sequence for a rectangle defined by the corners given in the parameter. Will likely have many sub methods inside. 
	
		
	}
	

	
}
