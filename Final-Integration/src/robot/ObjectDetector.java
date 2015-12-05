package robot;

import java.util.ArrayList;
import java.util.Collections;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.utility.TimerListener;
import lejos.utility.Timer;

/**
 * The object detector class is responsible for using the ultrasonic sensor and the light sensor to detect
 * objects in front of the robot. 
 * 
 * @version 2.5
 * @author Shawn Lu, Solvie Lee
 *
 */

public class ObjectDetector implements TimerListener {
	
	public static final int errorMargin = 2, DEFAULT_INTERVAL = 25, offFactor = 20;
	private static final int OBJECT_FAR = 60, OBJECT_CLOSE = 6, OBJECT_CLOSEISH = 15, MAX_RANGE = 120;
	private boolean objectDetected, objectCloseish, objectClose, objectColorSeen, flagDetected;
	private SampleProvider usValue, colorSensor, colorSensor2;
	private int realdistance, lastcolor2, deltacolor2, filter, filter_out = 10;
	private float[] usData, colorData, colorData2;
	private int deltarealdis;
	private int lastdis;
	private Timer timer;
	private Navigator navi;
	private ArrayList<Integer> Data = new ArrayList<Integer>();
	
	/**
	 * Default constructor
	 * @param navi the Navigator 
	 * @param usValue the ultrasonic sensor
	 * @param usData the data from the ultrasonic sensor
	 * @param colorSensor the forwards facing color sensor
	 * @param colorData the data from the forwards facing color sensor
	 * @param colorSensor2 the downwards facing color sensor
	 * @param colorData2 the data from the downwards facing color sensor
	 * @param autostart whether the ObjectDetector should autostart
	 */
	public ObjectDetector(Navigator navi, SampleProvider usValue, float[] usData,
			SampleProvider colorSensor, float[] colorData, SampleProvider colorSensor2, float[] colorData2, boolean autostart){
		this.objectDetected = false;
		this.objectCloseish = false;
		this.usValue = usValue;
		this.usData = usData;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.colorSensor2 = colorSensor2;
		this.colorData2 = colorData2;
		this.realdistance = getdistance();
		this.lastdis=realdistance;
		this.navi = navi;
		this.timer = new Timer(DEFAULT_INTERVAL, this);
		if (autostart)
			timer.start();
	}
	
	
	/**
	 * This method checks the sensor data whenever it is called, and puts it through the filter.  It also tells the navigator whether an object/flag was seen or not. 
	 */
	@Override
	public void timedOut(){
		int lastrealdis = realdistance;
		Data.clear();
		for(int i =0; i<46; i++){
			Data.add(this.getdistance());
			Delay.usDelay(500);
		}
		Collections.sort(Data);
		int currentrealdis = Data.get(Data.size()/2);
		synchronized(this){
			int diff = Math.abs(currentrealdis - lastrealdis);
			if (diff>20 && filter<filter_out){
				filter++;
			}else{
				realdistance = currentrealdis;
				filter = 0;
			}
			deltarealdis=this.getrealdis()-lastdis;
			lastdis=this.getrealdis();
			
			deltacolor2 = this.getcolor2() - lastcolor2;
			lastcolor2 = this.getcolor2();
			
			//distance = filter(distance);
		
		}
			//Tell the navi the information.
		updateNavi(OBJECT_FAR, OBJECT_CLOSEISH, OBJECT_CLOSE);
		
	}
	
	
	
	/**
	 * A method to get the raw distance detected by the ultrasonic sensor.
	 * @return raw distance in cm.
	 */
	public int getdistance(){
		synchronized (this){
						usValue.fetchSample(usData, 0);
						int rawdistance = (int)(usData[0]*100.0);
						// filter infinity
						if(rawdistance==Integer.MAX_VALUE){
							rawdistance=255;}
						return rawdistance;
					}
	}
	

	/**
	 * This method allows the object detector to tell the navigator the state of the field of vision.
	 * 
	 * @param farDist Distance within which the object detector will say an object has been detected
	 * @param closeishDist Distance within which the object detector will say that an object is somewhat close ("closeish")
	 * @param closeDist Distance within which the object detector will say an object is close
	 */
	public void updateNavi(double farDist, double closeishDist, double closeDist){
		if (realdistance <= farDist)
			objectDetected = true;
		else
			objectDetected = false;
		if (realdistance <= closeishDist)
			objectCloseish = true;
		else
			objectCloseish = false;
		if(realdistance<= closeDist)
			objectClose = true;
		else
			objectClose = false;
		//notify the Navigator of the above information
		navi.setDetectionInfo(new boolean[]{objectDetected, objectCloseish, objectClose, false},new boolean[]{true, true, true, false});
		navi.setObjectDist(realdistance);
	}
	
	/**
	 * This method allows outside classes to get all the information from the objectDetector, whether an object was seen, and whether it's close.
	 * @return array of boolean values that say (index 0) if an object was detected in front of the robot, and (index 1), if the object detected is close
	 */
	public boolean[] getVisionStatus(){
		boolean[] status = new boolean[4];
		status[0] = objectDetected;
		status[1] = objectClose;
		return status;
	}
	
	/**
	 * Method that returns the color value from the front color sensor
	 * @return an array of integers that define the color value
	 */
	public int[] getcolor1(){
		synchronized (this){
			colorSensor.fetchSample(colorData, 0);
			int[] color=new int[]{(int)(colorData[0]*1000),(int)(colorData[1]*1000),(int)(colorData[2]*1000)};
			return color;
		}
	}
	
	/**
	 * Method that returns the color value from the back color sensor
	 * @return an integer value that defines the color value.
	 */
	public int getcolor2(){
		synchronized (this){
			colorSensor2.fetchSample(colorData2, 0);
			int color=(int)(colorData2[0]);	
			return color;
		}
	}

	/**
	 * Method that maps a color number (as defined light blue:1 ; red:2 ; yellow:3 ; white:4 ; dark blue:5 )
	 * to the detected color of an object
	 * @return integer value of a color
	 */
	public int getcolornumber(){
		synchronized (this){
			int all=this.getcolor1()[0]+this.getcolor1()[1]+this.getcolor1()[2];
			if(all==0)
				all=1;
			double r=(double)this.getcolor1()[0]/all;
			double g=(double)this.getcolor1()[1]/all;
			double b=(double)this.getcolor1()[2]/all;
			if(r>0.65 && g<0.2)
				return 2;
			else if(r>0.4 && g>0.38)
				return 3;
			else if(b>0.4 && r<0.18)
				return 5;
			else if(r>0.3)
				return 4;
			else
				return 1;
		}
	}
	
	/**
	 * Gets the filtered real distance value
	 * @return integer value of the real distance in cm.
	 */
	public int getrealdis(){
		synchronized (this){
			return realdistance;}
	}
	
	/**
	 * Get delta value of second color sensor
	 * @return an integer value of the delta of the color sensor value.
	 */
	public int getdeltacolor2(){
		synchronized (this){
			return deltacolor2;}
	}
	
	/**
	 * Get delta value of the real distance detected by ultrasonic
	 * @return an integer value of the delta of the real distance value in cm.
	 */
	public int getdeltarealdis(){
		synchronized (this){
			return deltarealdis;}
	}
	
	/**
	 * This method allows outside classes to access the ObjectDetector's navigator.
	 * @return Navigator
	 */
	public Navigator getNavi(){
		return navi;
	}
	
	/**
	 * Method to start the timer
	 */
	public void start(){
		timer.start();
	}

	/**
	 * Method to stop the timer
	 */
	public void stop(){
		timer.stop();
	}

}
