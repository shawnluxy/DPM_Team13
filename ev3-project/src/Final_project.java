import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;


public class Final_project {
	
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
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
		SampleProvider colorValue = colorSensor.getMode("Red");
		float[] colorData = new float[colorValue.sampleSize()];
		
		SensorModes colorSensor2 = new EV3ColorSensor(colorPort2);
		SampleProvider colorValue2 = colorSensor2.getMode("ColorID");
		float[] colorData2 = new float[colorValue2.sampleSize()];
		
		if(Button.waitForAnyPress()==Button.ID_ENTER){
			Odometer odo = new Odometer(leftMotor, rightMotor, true);			
			ObjectDetector detector=new ObjectDetector(usValue, usData,colorValue,colorData,colorValue2,colorData2);
			LCDInfo lcd = new LCDInfo(odo, detector);
			Flagcapturer capture=new Flagcapturer(armMotor);
			
//			capture.up();
			Localization localize=new Localization(odo,detector);
			localize.doLocalization();
//			test t=new test(odo,detector);
//			t.testing();
//			capture.down();
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}

}
