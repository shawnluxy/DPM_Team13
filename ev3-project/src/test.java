import lejos.utility.Delay;


public class test {
	private Odometer odo;
	private ObjectDetector detector;
	private Navigation navigate;
	private Flagcapturer arm;
	
	public test( Odometer odo,ObjectDetector detector,Flagcapturer arm){
		this.odo=odo;
		this.detector=detector;
		this.navigate=new Navigation(odo);
		this.arm=arm;
	} 
	
	public void testing(){
		arm.down();
		navigate.travelForwards(3);
		arm.up();
		
//		navigate.travelTo(0, 30);
//		navigate.travelTo(30, 30);
//		navigate.travelTo(30, 0);
//		navigate.travelTo(0, 0);
		
	}
	
}
