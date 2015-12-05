package robot;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.geometry.Point2D;
import lejos.utility.Delay;

/**
 * This is the class that allows the robot to search the enemy zone for the flag.
 * @author Shawn Lu
 * @version 3.0
 *
 */
public class Search {
	private static final int limit=32;
	private static final int speed=180;
	private EV3LargeRegulatedMotor sensorMotor;
	private FlagCapturer arm;
	private Odometer odo;
	private Navigator navigate;
	private ObjectDetector detector;
	
	/**
	 * Default constructor
	 * @param detector the ObjectDetector
	 * @param arm the FlagCapturer
	 * @param sensorMotor the motor that controls the movement of the sensor arm.
	 */
	public Search(ObjectDetector detector,FlagCapturer arm,EV3LargeRegulatedMotor sensorMotor){
		
		this.detector=detector;
		this.navigate= detector.getNavi();
		this.odo=navigate.getOdo();
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
	 */
	public void searching(Point2D point1,Point2D point2,Point2D point3,int colornum){
		// check if the enemy zone is in the left or right side of robot, and set sensor rotate "deg" and robot "degrotate" based on that 
		boolean left=point1.getX()>odo.getX();
		int deg=left ? 90 : -90;
		int degrotate=deg>0 ? 90 : -90;
		//do search inside the enemy zone and move closer to the edge of zone until it goes beyond the edge-10cm 
		//"edge" means the line of top right point's y-axis
		while(odo.getX()<(point2.getX()-10)){
			double x=odo.getX();
			navigate.turnTo(90,true);
			sensorMotor.rotate(-deg);
			// do scan along the y-axis
			boolean found=scan(point1,point3,-deg,-degrotate,colornum);
			if(found){break;}
			// if the robot goes beyond edge-20cm, stop
			if(odo.getX()>point2.getX()-20)
				break;
			// travel closer to the edge by 20cm every time
			navigate.travelTo(x+20, odo.getY());
			navigate.turnTo(90,true);
			sensorMotor.rotate(deg);
			// goes down to the bottom line-30cm, and detect the blocks if it's in the way
			while(odo.getY()<(point2.getY()-28)){
				navigate.setSpeeds(speed,speed);
				Delay.msDelay(25);
				if(detector.getrealdis()<4){
					// if sees the target flag, capture it
					if(detector.getcolornumber()==colornum){
						Sound.beep();grab();
						found=true;
						navigate.travelBackwards(10);
						double x0=odo.getX();
						navigate.travelTo(x0,point1.getY()-10);
						break;
					}
					// if detects as not target, throw it away
					else{
						navigate.travelBackwards(10);
						sensorMotor.rotate(-95);
						arm.down();
						navigate.travelForwards(13);
						arm.up();
						navigate.turnTo(180,true);
						arm.down();
						navigate.travelForwards(10);
						navigate.travelBackwards(10);
						arm.throwaway();
						sensorMotor.rotate(95);
						navigate.turnTo(90,true);
					}
				}
			}
			if(found){break;}
			navigate.travelTo(x+20,point2.getY()-25);
		}
		
	}
	/**
	 * scan method: go backwards along the y-axis to scan. if sensor sees a block, turn to facing block and go close to check it. 
	 * the robot keeps scanning until it moves out the enemy zone
	 * @param point1 the coordinates of the bottom left corner of the enemy zone
	 * @param point3 the coordinates of the top right corner of the enemy zone
	 * @param deg the amount of degrees the sensor motor is expected to turn
	 * @param degrotate the amount of degrees to add to get the robot to rotate towards
	 * @param colornum the color number of the flag to scan for. (Color indices defined in ObjectDetector class)
	 * @return
	 */
	public boolean scan(Point2D point1,Point2D point3,int deg,int degrotate,int colornum){
		boolean found=false;
		while(odo.getY()>point1.getY()-10){
			navigate.setSpeeds(-speed,-speed);
			Delay.msDelay(25);
			if(detector.getrealdis()<limit){
				navigate.setSpeeds(0,0);
				navigate.travelBackwards(2);
				navigate.turnTo(odo.getAng()+degrotate,true);
				sensorMotor.rotate(-deg);
				// call the check method to do the color detection
				found=this.check(odo.getX(),odo.getY(),colornum);
				if(found){
					navigate.travelTo(odo.getX(),point1.getY()-10);
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
		while(detector.getrealdis()>4 && detector.getdeltarealdis()<10 && detector.getrealdis()!=255){
			navigate.setSpeeds(speed,speed);Delay.msDelay(25);}
		navigate.setSpeeds(0,0);
		if(detector.getrealdis()<=4){
			checked=true;
			if(detector.getcolornumber()==colornum){
				Sound.beep();grab();found=true;}
			else{
				drop();}
		}
		if(detector.getrealdis()==255){
			navigate.travelBackwards(3);
			if(detector.getcolornumber()==colornum){
				Sound.beep();grab();found=true;}
			else if(detector.getcolornumber()!=1){
				drop();}
			else{
				navigate.travelForwards(3);}
		}
		// go back to point(x,y)
		double dis=Math.sqrt(Math.pow(odo.getX()-x,2)+Math.pow(odo.getY()-y,2));
		navigate.travelBackwards(dis);
		navigate.turnTo(90,true);
		if(!checked)
			navigate.travelBackwards(2);
		else
			navigate.travelBackwards(9);
		return found;
	}
	/**
	 * method to capture the flag: move backwards a bit, put down the arm, and move forwards a bit, then lift arm up to capture flag
	 */
	public void grab(){
		navigate.travelBackwards(10);
		sensorMotor.rotate(-95);
		arm.down();
		navigate.travelForwards(15);
		arm.up();
		sensorMotor.rotate(95);
	}
	
	/**
	 * Method to place the captured block down
	 */
	public void putdown(){
		sensorMotor.rotate(-95);
		arm.down();
		navigate.travelForwards(10);
		navigate.travelBackwards(10);
		arm.throwaway();
		sensorMotor.rotate(95);
	}
	/**
	 * Method to place the captured block down behind the robot.
	 */
	public void drop(){
		navigate.travelBackwards(10);
		sensorMotor.rotate(-95);
		arm.down();
		navigate.travelForwards(15);
		arm.up();
		navigate.turnTo(180,true);
		arm.down();
		navigate.travelForwards(10);
		navigate.travelBackwards(10);
		arm.throwaway();
		sensorMotor.rotate(95);
		navigate.turnTo(0,true);
	}
}
