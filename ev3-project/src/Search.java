import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.geometry.Point2D;
import lejos.utility.Delay;

/**
 * Search class does jobs to searching the enemy zone, grab the flags and put flags down
 * 
 * Searching starts beside the top left corner of enemy zone and move backwards alone the y-axis with sensor facing the zone.   
 * if it detects something, robot goes closer to check it. if not the target, throw it to back, if it is the target,then capture it.
 * after it cleans up a certain range of area, it will go 20cm close and scan again in the zone until it covers all area.
 * 
 * @author Shawn Lu
*/

public class Search {
	private static final int limit=35;
	private static final int speed=180;
	private EV3LargeRegulatedMotor sensorMotor;
	private Flagcapturer arm;
	private Odometer odo;
	private Navigation navigate;
	private ObjectDetector detector;
	
	public Search(Odometer odo,ObjectDetector detector,Flagcapturer arm,EV3LargeRegulatedMotor sensorMotor){
		this.odo=odo;
		this.navigate=new Navigation(odo);
		this.detector=detector;
		this.arm=arm;
		this.sensorMotor=sensorMotor;
		sensorMotor.setAcceleration(600);
	}
	
	/**
	 * main searching method
	 * @param point1: the bottom left point of the enemy zone
	 * @param point2: the top right point of the enemy zone
	 * @param point3: the point of the home zone
	 * @param colornum: the number of color of the target flag
	 * @param flagnum: the amount of target flag
	 */
	public void searching(Point2D point1,Point2D point2,Point2D point3,int colornum,int flagnum){
		odo.setPosition(new double [] {point1.getX()-15, point2.getY()-25, 90}, new boolean [] {true,true,true});
		/**
		 * check if the enemy zone is in the left or right side of robot, and set sensor rotate "deg" and robot "degrotate" based on that 
		 */
		boolean left=point1.getX()>odo.getX();
		int deg=left ? 95 : -95;
		int degrotate=deg>0 ? 90 : -90;
		/**
		 * do search inside the enemy zone and move closer to the edge of zone until it goes beyond the edge-10cm 
		 * "edge" means the line of top right point's y-axis
		 */
		while(odo.getX()<(point2.getX()-10)){
			double x=odo.getX();
			navigate.turnTo(90,true);
			sensorMotor.rotate(-deg);
			// do scan along the y-axis
			boolean found=scan(point1,point3,-deg,-degrotate,colornum);
			if(found)
				break;
			// if the robot goes beyond edge-20cm, stop
			if(odo.getX()>point2.getX()-20)
				break;
			// travel closer to the edge by 20cm every time
			navigate.travelTo(x+20, odo.getY());
			navigate.turnTo(90,true);
			sensorMotor.rotate(deg);
			// goes down to the bottom line-30cm, and detect the blocks if it's in the way
			while(odo.getY()<(point2.getY()-30)){
				navigate.setSpeeds(speed,speed);
				Delay.msDelay(25);
				if(detector.getrealdis()<4){
					// if sees the target flag, capture it
					if(detector.getcolornumber()!=1){
						Sound.beep();grab();
						navigate.travelBackwards(10);
						double x0=odo.getX();
						double y0=odo.getY();
						navigate.travelTo(x0,point1.getY()-10);
						// Pathfinder.findPathTo(point3.getX(),point3.getY());putdown();Pathfinder.findPathTo(x0,point1.getY()-10));
						navigate.travelTo(x0,y0);
					}
					// if detects as not target, throw it away
					else{
						navigate.travelBackwards(10);
						sensorMotor.rotate(-95);
						arm.down();
						navigate.travelForwards(12);
						arm.throwaway();
						sensorMotor.rotate(95);}
				}
			}
			navigate.travelTo(x+20,point2.getY()-25);
		}
		
	}
	/**
	 * scan method: go backwards along the y-axis to scan. if sensor sees a block, turn to facing block and go close to check it. 
	 * the robot keeps scanning until it moves out the enemy zone
	 */
	public boolean scan(Point2D point1,Point2D point3,int deg,int degrotate,int colornum){
		boolean found=false;
		while(odo.getY()>point1.getY()-10){
			navigate.setSpeeds(-speed,-speed);
			Delay.msDelay(25);
			if(detector.getrealdis()<limit){
				navigate.travelForwards(2);
				navigate.turnTo(odo.getAng()+degrotate,true);
				sensorMotor.rotate(-deg);
				// call check method to do the color detection
				found=this.check(odo.getX(),odo.getY(),colornum);
				if(found){
					double x=odo.getX();
					double y=odo.getY();
					navigate.travelTo(odo.getX(),point1.getY()-10);
					// Pathfinder.findPathTo(point3.getX(),point3.getY());putdown();Pathfinder.findPathTo(x,y);
//					navigate.travelTo(x,y);
					break;
				}
				sensorMotor.rotate(deg);
			}
		}
		navigate.setSpeeds(0, 0);
		return found;
	}
	/**
	 * check method: go close to check the color of block, if the block disappeared, move 3cm back along y-axis and scan again,
	 * if it actually checked the block, move 12cm back along y-axis to avoid duplicated checking and continue scan   
	 * @param x,y: the position where robot should go back after color detection 
	 * @param colornum: the number of color of the target flag
	 */
	public boolean check(double x,double y,int colornum){
		boolean found=false;
		boolean checked=false;
		while(detector.getrealdis()>4 && detector.getdeltarealdis()<10){
			navigate.setSpeeds(speed,speed);Delay.msDelay(25);}
		navigate.setSpeeds(0,0);
		if(detector.getrealdis()<=4){
			checked=true;
			if(detector.getcolornumber()!=1){
				Sound.beep();grab();found=true;}
			else{
				navigate.travelBackwards(10);
				sensorMotor.rotate(-95);
				arm.down();
				navigate.travelForwards(12);
				arm.throwaway();
				sensorMotor.rotate(95);
			}
		}
		// go back to point(x,y)
		double dis=Math.sqrt(Math.pow(odo.getX()-x,2)+Math.pow(odo.getY()-y,2));
		navigate.travelBackwards(dis);
		navigate.turnTo(90,true);
		if(!checked)
			navigate.travelBackwards(5);
		else
			navigate.travelBackwards(14);
		return found;
	}
	/**
	 * method to capture the flag: move backwards a bit, put down the arm, and move forwards a bit, then lift arm up to capture flag
	 */
	public void grab(){
		navigate.travelBackwards(10);
		sensorMotor.rotate(-95);
		arm.down();
		navigate.travelForwards(12);
		arm.up();
		sensorMotor.rotate(95);
	}
	/**
	 * 
	 */
	public void putdown(){
		sensorMotor.rotate(-95);
		arm.down();
		navigate.travelBackwards(10);
		arm.up();
		sensorMotor.rotate(95);
	}
}
