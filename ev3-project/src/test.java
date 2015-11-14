import lejos.utility.Delay;


public class test {
	private Odometer odo;
	private ObjectDetector detector;
	private Navigation navigate;
	
	public test( Odometer odo,ObjectDetector detector){
		this.odo=odo;
		this.detector=detector;
		this.navigate=new Navigation(odo);
	} 
	
	public void testing(){
		odo.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {false,false,true});
		while(odo.getAng()<330){
			navigate.setSpeeds(-120,120);
			System.out.println(detector.getcolor2());
			Delay.msDelay(25);
		}
		navigate.setSpeeds(0,0);
		
//		navigate.turnTo(90, false);

	}
	
}
