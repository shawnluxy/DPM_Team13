import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.geometry.Point2D;
import lejos.utility.Delay;


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
	
	public void searching(Point2D point1,Point2D point2,Point2D point3,int colornum,int flagnum){
		odo.setPosition(new double [] {0, 85, 90}, new boolean [] {true,true,true});
		boolean left=point1.getX()>odo.getX();
		int deg=left ? 95 : -95;
		int degrotate=deg>0 ? 90 : -90;
		
		while(odo.getX()<(point2.getX()-10)){
			double x=odo.getX();
			navigate.turnTo(90,true);
			sensorMotor.rotate(-deg);
			scan(point1,point3,-deg,-degrotate,colornum);
			navigate.travelTo(x+20, odo.getY());
			navigate.turnTo(90,true);
			sensorMotor.rotate(deg);
			while(odo.getY()<(point2.getY()-30)){
				navigate.setSpeeds(speed,speed);
				Delay.msDelay(25);
				if(detector.getrealdis()<4){
					if(detector.getcolornumber()!=1){
						Sound.beep();grab();
						navigate.travelBackwards(10);
						double x0=odo.getX();
						double y0=odo.getY();
						navigate.travelTo(x0,point1.getY()-10);
						// Pathfinder.findPathTo(point3.getX(),point3.getY());putdown();Pathfinder.findPathTo(x0,point1.getY()-10));
						navigate.travelTo(x0,y0);
					}
					else{
						navigate.travelBackwards(10);
						sensorMotor.rotate(-95);
						arm.down();
						navigate.travelForwards(12);
//						arm.grab();
//						navigate.turnTo(odo.getAng()-degrotate,true);
						arm.throwaway();
//						navigate.turnTo(degrotate,true);
						sensorMotor.rotate(95);}
				}
			}
			navigate.travelTo(x+20,point2.getY()-25);
		}
		
	}
	
	public void scan(Point2D point1,Point2D point3,int deg,int degrotate,int colornum){
		boolean found=false;
		while(odo.getY()>point1.getY()-10){
			navigate.setSpeeds(-speed,-speed);
			Delay.msDelay(25);
			if(detector.getrealdis()<limit){
				navigate.travelForwards(2);
				navigate.turnTo(odo.getAng()+degrotate,true);
				sensorMotor.rotate(-deg);
				found=this.check(odo.getX(),odo.getY(),colornum);
				if(found){
					double x=odo.getX();
					double y=odo.getY();
					navigate.travelTo(odo.getX(),point1.getY()-10);
					// Pathfinder.findPathTo(point3.getX(),point3.getY());putdown();Pathfinder.findPathTo(x,y);
					navigate.travelTo(x,y);
				}
				sensorMotor.rotate(deg);
			}
		}
		navigate.setSpeeds(0, 0);
	}
	
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
		double dis=Math.sqrt(Math.pow(odo.getX()-x,2)+Math.pow(odo.getY()-y,2));
		navigate.travelBackwards(dis);
		navigate.turnTo(90,true);
		if(!checked)
			navigate.travelBackwards(5);
		else
			navigate.travelBackwards(14);
		return found;
	}
	
	public void grab(){
		navigate.travelBackwards(10);
		sensorMotor.rotate(-95);
		arm.down();
		navigate.travelForwards(12);
		arm.up();
		sensorMotor.rotate(95);
	}
	
	public void putdown(){
		sensorMotor.rotate(-95);
		arm.down();
		navigate.travelBackwards(10);
		arm.up();
		sensorMotor.rotate(95);
	}
}
