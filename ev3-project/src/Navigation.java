import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation {
	final static int FAST = 200, SLOW = 100, ACCELERATION = 3000;
	final static double DEG_ERR = 0.5, CM_ERR = 1.0;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	public Navigation(Odometer odo) {
		this.odometer = odo;

		EV3LargeRegulatedMotor[] motors = this.odometer.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];

		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}

	/*
	 * Functions to set the motor speeds jointly
	 */
	public void setSpeeds(float lSpd, float rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	public void setSpeeds(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	/*
	 * Float the two motors jointly
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(false);
	}

	/*
	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
	 * constantly updating it's heading
	 */
	public void travelTo(double x, double y) {
		double minAng;
		minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
		if (minAng < 0)
			minAng += 360.0;
		this.turnTo(minAng, false);
		double currX = odometer.getX();
		double currY = odometer.getY();
		double distance = Math.sqrt(Math.pow((y - currY), 2) + Math.pow((x - currX), 2));
		while(Math.sqrt(Math.pow(odometer.getX() - currX, 2) + Math.pow(odometer.getY() - currY, 2)) < distance){
			this.setSpeeds(FAST, FAST);
		}
		this.setSpeeds(0, 0);
	}
	
	public void tracelToback(double x, double y){
		
	}
	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 */
	public void turnTo(double angle, boolean stop) {
		double error = normalize(angle) - this.odometer.getAng();
		while (Math.abs(error) > DEG_ERR) {
			error = normalize(angle) - this.odometer.getAng();
			if (error < -180.0) {
				this.setSpeeds(-SLOW, SLOW);
			} else if (error < 0.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else if (error > 180.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else {
				this.setSpeeds(-SLOW, SLOW);
			}
		}

		if (stop) {
			this.setSpeeds(0, 0);
		}
	}
	
	public void travelBackwards(double distance){
		double currX, currY;
		currX = odometer.getX();
		currY = odometer.getY();
		while(Math.sqrt(Math.pow(odometer.getX() - currX, 2) + Math.pow(odometer.getY() - currY, 2)) < distance){
			this.setSpeeds(-FAST,-FAST);}
		this.setSpeeds(0,0);
		
	}
	
	public void travelForwards(double distance){
		double currX, currY;
		currX = odometer.getX();
		currY = odometer.getY();
		while(Math.sqrt(Math.pow(odometer.getX() - currX, 2) + Math.pow(odometer.getY() - currY, 2)) < distance){
			this.setSpeeds(FAST,FAST);}
		this.setSpeeds(0,0);
	}
	
	public double normalize(double deg){
		double normal=deg % 360;
		if(normal<0)
			normal+=360;
		return normal;
	}
	
}
