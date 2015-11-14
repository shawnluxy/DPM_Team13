import lejos.hardware.motor.EV3LargeRegulatedMotor;


public class Flagcapturer {
	private EV3LargeRegulatedMotor arm; 
	
	public Flagcapturer(EV3LargeRegulatedMotor armMotor){
		this.arm=armMotor;
		arm.setAcceleration(500);
		arm.setStallThreshold(8, 20);
	}
	
	public void up(){
		arm.rotate(-180,true);
		while(!arm.isStalled()){
			arm.rotate(-6,true);}
		
	}
	
	public void down(){
		arm.rotate(180,true);
		while(!arm.isStalled()){
			arm.rotate(6,true);}
	}
	
}
