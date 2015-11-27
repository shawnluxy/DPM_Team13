import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import lejos.robotics.geometry.Point2D;
import lejos.utility.Delay;


public class Final_Project {
	
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3LargeRegulatedMotor sensorMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final EV3LargeRegulatedMotor armMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	private static final Port usPort = LocalEV3.get().getPort("S1");
	private static final Port colorPort1 = LocalEV3.get().getPort("S2");
	private static final Port colorPort2 = LocalEV3.get().getPort("S3");
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];
		
		SensorModes colorSensor = new EV3ColorSensor(colorPort1);
		SampleProvider colorValue = colorSensor.getMode("RGB");
		float[] colorData = new float[colorValue.sampleSize()];
		
		SensorModes colorSensor2 = new EV3ColorSensor(colorPort2);
		SampleProvider colorValue2 = colorSensor2.getMode("ColorID");
		float[] colorData2 = new float[colorValue2.sampleSize()];
		
		if(Button.waitForAnyPress()==Button.ID_ENTER){
			Odometer odo = new Odometer(leftMotor, rightMotor, true);			
			ObjectDetector detector=new ObjectDetector(usValue, usData,colorValue,colorData,colorValue2,colorData2);
			LCDInfo lcd = new LCDInfo(odo, detector);
			Flagcapturer arm=new Flagcapturer(armMotor);
			
//			arm.up();
//			Localization localize=new Localization(odo,detector);
//			localize.doLocalization();
			
			Point2D point1=new Point2D.Double(15,45);
			Point2D point2=new Point2D.Double(75,105);
			Point2D point3=new Point2D.Double(0,0);
			Search search=new Search(odo,detector,arm,sensorMotor);
			search.searching(point1, point2, point3, 5, 3);
			
//			test testing=new test(odo,detector,arm);
//			testing.testing();
			
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}

}
