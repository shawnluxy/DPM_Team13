package robot;
import lejos.hardware.motor.EV3LargeRegulatedMotor;


public class Flagcapturer {
	private EV3LargeRegulatedMotor arm; 
	
	public Flagcapturer(EV3LargeRegulatedMotor armMotor){
		this.arm=armMotor;
	}
	
	public synchronized void up(){
		arm.setAcceleration(25);
		
		arm.rotateTo(-60);
	}
	
	public synchronized void down(){
		arm.setAcceleration(50);

		arm.rotateTo(200);
		arm.flt(true);
	}
	
	public synchronized void grab(){
		arm.setAcceleration(50);
		arm.rotateTo(-15);
	}
	
	public synchronized void throwaway(){
		arm.setAcceleration(1000);

		arm.rotateTo(-180);
		arm.flt(true);
	}
}
