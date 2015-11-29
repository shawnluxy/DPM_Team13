package robot;


import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.geometry.Point2D;
/**
 * This class is used to find the enemy base without colliding into any of the obstacles, and to return to the home base.
 * 
 * @version 2.0
 * @author Solvie Lee
 *
 */
public class PathFinder {

	private static final double DEG_ERR = 0.5, BACK_DIST = 15;
	private int blockedCount;
	private Navigator navi;
	private Odometer odo;
	private ObjectDetector obDetector;
	private String flag;
	private Point2D[]wayPoints;

	/**
	 * Default constructor
	 * @param obDetector
	 */
	public PathFinder(ObjectDetector obDetector){
		this.navi = obDetector.getNavi();
		this.odo = navi.getOdo();
		this.obDetector = obDetector;
		this.blockedCount = 0;
		this.flag = "";
	}

	/**
	 * This method will make the robot go from its current point to the destination point travelling entirely along lines 
	 * parallel to the x and y axis. At its base, it should travel from start to end the two tangent lines of the shortest distance vector
	 * unless a block gets in its way. It should also keep track of where all the blocks were detected.
	 * @param x X coordinate of destination
	 * @param y Y coordinate of destination
	 * @param obstacles coordinates of the centers of all the detected obstacles
	 * @return Point2D[]
	 */
	public Point2D[] findPathTo(double x, double y, Point2D[] obstacles){
		double currX, currY, currTheta, horizl, vertl;
		wayPoints = new Point2D[2];
		currX = odo.getX();
		currY = odo.getY();
		currTheta = odo.getAng();
		
		horizl = Math.abs(x - currX);
		vertl = Math.abs(y - currY);
		
		//set up wayPoints to travel the longer way first, UNLESS there is no obstacle immediately in front, in which case, travel to where there is no obstacle.
		if (horizl-vertl>0){
			wayPoints[0] = new Point2D.Double(x, currY);
			wayPoints[1] = new Point2D.Double(x, y);
	
		}
		else{
			wayPoints[0] = new Point2D.Double(currX, y);
			wayPoints[1] = new Point2D.Double(x, y);	
		}
		
		
		//TODO: modify travel to method so that if the destination it wants to travel to is within a centimeter of the current angle and whatever
		//you stay put.
		
		boolean [] pathBlocked1, pathBlocked2;
		if (!(((odo.getX()<x+DEG_ERR)&&(odo.getX()>x-DEG_ERR)) && ((odo.getY()<y+DEG_ERR)&&(odo.getY()>y-DEG_ERR)))){ //While we've not reached the destination, this happens. //TODO: modify so that there's more room for error.
			//travel to the first point. If we try to travel there and don't reach the correct place, the same method is called again. 
			setOdoFlag(0);
			pathBlocked1 = navi.travelToWithAvoidance(wayPoints[0].getX(), wayPoints[0].getY());
			while (pathBlocked1[0]){
				 // if there is an obstacle immediately in front, flip the waypoints
					blockedCount ++;
					if (wayPoints[0].getX() ==x && wayPoints[0].getY()==currY)
						wayPoints[0] = new Point2D.Double(currX, y);
					else
						wayPoints[0] = new Point2D.Double(x, currY);
				pathBlocked1 = navi.travelToWithAvoidance(wayPoints[0].getX(), wayPoints[0].getY());
				if (blockedCount>1)
					break;
			}
			
			if (pathBlocked1[1]){
				Sound.beepSequence();
				setOdoFlag(1);
				if (blockedCount>1){
					//reset blockedCount.
					blockedCount = 0;
					Sound.beepSequence();
					boolean emptyFound[] = new boolean[2];
					emptyFound = navi.scan();
					
					if (emptyFound[0]){
						this.escapeCorner(emptyFound[1]);
						obstacles = findPathTo(x, y, obstacles);
					}
					else{
						this.backOutOfCorner();
						obstacles = findPathTo(x,y, obstacles);
					}
					//TODO: Check which direction it is facing when it gets to this point. 
				}
				obstacles = findPathTo(x, y, obstacles);	
			}
			else{
				setOdoFlag(1);
				pathBlocked2 =navi.travelToWithAvoidance(wayPoints[1].getX(), wayPoints[1].getY());
				
				if (pathBlocked2[1]){ //hard case! 
					boolean emptyFound[] = new boolean[2];
					emptyFound = navi.scan();
					
					if (emptyFound[0]){ //TODO: make sure to never try to retrace your steps and get caught in an infinite loop
						//escape
						this.escapeCorner(emptyFound[1]);
						obstacles = findPathTo(x, y, obstacles);
					}
					else{
						//back up out of corner, then escape.
						this.backOutOfCorner();
						obstacles = findPathTo(x,y, obstacles);
					}
					obstacles = findPathTo(x, y, obstacles);
				}
				else{
					navi.stopMotors();
					blockedCount = 0;
					return obstacles;
				}
			}	
		}
		blockedCount = 0;
		return obstacles;
	}
	
	/**
	 * Escape corner routine
	 * @param direction The direction to escape out of. True is left.
	 */
	private void escapeCorner(boolean direction){
		navi.travelUntilNoObstacle(direction); //scan returns emptyFound[1] == true if empty path is left
	}
	
	/**
	 * Method to back out of a corner case
	 */
	private void backOutOfCorner(){
		//Sound.beepSequence();
		boolean[] emptyFound = new boolean[2];
		navi.travelBackwards((int) BACK_DIST);
		emptyFound = navi.scan();
		while(!emptyFound[0]){
			navi.travelBackwards((int)BACK_DIST);
			emptyFound = navi.scan();
		}
		navi.travelUntilNoObstacle(emptyFound[1]); 
	}

	
	/**
	 * This method is for testing purposes, will likely be removed in final integration.
	 * @param i waypoint index
	 */
	private void setOdoFlag(int i){
		flag = "to (" + wayPoints[i].getX() + ", " + wayPoints[i].getY() + ")"; 
		odo.setFlag(flag);
	}
	
}
