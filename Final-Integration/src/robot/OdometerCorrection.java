package robot;

import lejos.hardware.Sound;

/**
 * Class controlling the correction of the odometer whenever the robot goes past
 * a line The correction is only made if the robot is going along either the x
 * or y axis. The correction is not activated whenever the robot navigates on a
 * coordinates that should represent a continuous line.
 * 
 * @version 3.0
 * @author Vivien Traineau
 */
public class OdometerCorrection {
	private Odometer odo;
	private ObjectDetector detector;

	// FILTERING
	int filterSize = 5;
	int filterPointer = 0;
	double[] lightArray = new double[filterSize];

	private double distSensorCenter = 11.3;
	private double distBtwLines = 30.48;

	/**
	 * Constructor
	 * 
	 * @param odoO Odometer
	 * @param detectorO Objectdetector
	 */
	public OdometerCorrection(Odometer odoO, ObjectDetector detectorO) {
		this.odo = odoO;
		this.detector = detectorO;
	}

	/**
	 * Function executing the correction of the odometer.
	 */
	public void run() {

		double currentX = odo.getX() - distSensorCenter;
		double currentY = odo.getY() - distSensorCenter;
		double currentAng = odo.getAng();
		int errorAngAllowed = 4;
		int errorMarginBefore = 15;
		while (true) {
			currentX = odo.getX() - distSensorCenter;
			currentY = odo.getY() - distSensorCenter;
			currentAng = odo.getAng();
			boolean[] update = { false, false, false };
			double[] position = new double[3];
			if (lineDetected()) {
				if ((isInRangeAngle(currentAng, errorAngAllowed, 90) || isInRangeAngle(
						currentAng, errorAngAllowed, 270))
						&& !isOnALine(currentX, 2)) {
					// y axis case
					if (isInRangeCoo(currentY, errorMarginBefore,
							(int) distBtwLines)) {
						//System.out.println("Great");
						update[1] = true;
						position[1] = adaptValue(currentY, distBtwLines)
								+ distSensorCenter;
						Sound.beep();
						odo.setPosition(position, update);
						sleep(1000);
					}
				}
				if ((isInRangeAngle(currentAng, errorAngAllowed, 180)
						|| isInRangeAngle(currentAng, errorAngAllowed, 0) || isInRangeAngle(
						currentAng, errorAngAllowed, 360))
						&& !isOnALine(currentY, 2)) {
					// x axis case
					if (isInRangeCoo(currentX, errorMarginBefore,
							(int) distBtwLines)) {
					//	System.out.println("Freat");
						update[0] = true;
						position[0] = adaptValue(currentX, distBtwLines)
								+ distSensorCenter;
						Sound.beep();
						odo.setPosition(position, update);
						sleep(1000);
					}
				}
			}
		}
	}

	/**
	 * Function checking if a number is within the range of a target number
	 * 
	 * @param number
	 * @param range
	 * @param target
	 * @return true if the value is within the range, false otherwise
	 */
	private boolean isInRangeAngle(double number, int range, int target) {
		if (Math.abs(number - target) < range)
			return true;
		return false;
	}

	/**
	 * Function checking if a number is within the range of a the multiple of a
	 * target number
	 * 
	 * @param number
	 * @param range
	 * @param target
	 * @return whether a number is within the range.
	 */
	private boolean isInRangeCoo(double number, int range, int target) {
		if (Math.abs((number % target) - target) < range)
			return true;
		return false;
	}

	/**
	 * Function checking if the robot is navigating on a continuous grid line.
	 * 
	 * @param xOrY current x or y coordinates of the robot
	 * @param range small range representing the possible error of the odometer if close to the line
	 * @return whether the robot is navigating continuously on a grid line
	 */
	private boolean isOnALine(double xOrY, int range) {
		if (xOrY % distBtwLines < distBtwLines / 2)
			return xOrY % distBtwLines < range;
		return Math.abs(xOrY % distBtwLines - distBtwLines) < range;
	}

	/**
	 * Changes the value of a number to be the closest multiple of a given
	 * multiplier
	 * 
	 * @param number
	 * @param multiplier
	 * @return the closest value
	 */
	private double adaptValue(double number, double multiplier) {
		if (number % multiplier < multiplier / 2)
			return number - number % multiplier;
		return number + Math.abs(number % multiplier - multiplier);
	}

	/**
	 * Checks if a line is being detected by the light sensor
	 * 
	 * @return true if a line is detected, false otherwise
	 */
	private boolean lineDetected() {
		int value = detector.getdeltacolor2();
		if (value < 25 && value > 5)
			return true;
		return false;
	}

	/**
	 * Function to avoid writing a try/catch everywhere for sleeping.
	 * 
	 * @param length duration of the sleep
	 */
	private void sleep(int length) {
		try {
			Thread.sleep(length);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}