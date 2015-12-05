package robot;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class allows the robot to manipulate the arm
 * @author Scott Conrad, Shawn Lu
 * @version 3.0
 */

public class FlagCapturer {
	private EV3LargeRegulatedMotor arm; 
	
	/**
	 * Default Constructor
	 * @param armMotor the motor that controls the arm.
	 */
	public FlagCapturer(EV3LargeRegulatedMotor armMotor){
		this.arm=armMotor;
	}
	
	/**
	 * Method that raises the arm (and also causes the grasping motion)
	 */
	public synchronized void up(){
		arm.setAcceleration(200);
		arm.rotateTo(-160);
	}
	
	/**
	 * Method to lower the arm (and also causes the releasing motion)
	 */
	public synchronized void down(){
		arm.setAcceleration(250);
		arm.rotateTo(190);
		arm.flt(true);
	}
	
	/**
	 * Method to raise the arm at a fast acceleration to grasp an object and "throw" it behind the robot
	 */
	public synchronized void throwaway(){
		arm.setAcceleration(1200);
		arm.rotateTo(-180);
	}
}
