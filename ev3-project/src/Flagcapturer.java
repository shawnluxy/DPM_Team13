import lejos.hardware.motor.EV3LargeRegulatedMotor;


public class Flagcapturer {
	private EV3LargeRegulatedMotor arm; 
	
	public Flagcapturer(EV3LargeRegulatedMotor armMotor){
		this.arm=armMotor;
		arm.setStallThreshold(6, 20);
	}
	
	public synchronized void up(){
		arm.setAcceleration(450);
		
		arm.rotate(-180,true);
		while(!arm.isStalled()){
			arm.rotate(-3,true);}
		arm.rotate(15,true);
		
//		arm.rotateTo(-50);
	}
	
	public synchronized void down(){
		arm.setAcceleration(450);
		
		arm.rotate(180,true);
		while(!arm.isStalled()){
			arm.rotate(6,true);}
		arm.rotate(-15,true);
		
//		arm.rotateTo(200);
//		arm.flt(true);
	}
	
	public synchronized void throwaway(){
		arm.setAcceleration(1500);
		
		arm.rotate(-180,true);
//		arm.setStallThreshold(8, 20);
		while(!arm.isStalled()){
			arm.rotate(-8,true);}
		arm.rotate(15,true);
		
//		arm.rotateTo(-160);
//		arm.flt(true);
	}
}
