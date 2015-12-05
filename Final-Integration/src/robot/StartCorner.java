/**
 * 
 * Enum that defines the Start Corner
 * 
* @author Sean Lawlor
* @date November 3, 2011
* @class ECSE 211 - Design Principle and Methods
* 
* Modified by F.P. Ferrie
* February 28, 2014
* Changed parameters for W2014 competition

*/
package robot;

public enum StartCorner {
	BOTTOM_LEFT(1,0,0, "BL"),
	BOTTOM_RIGHT(2,300,0, "BR"),
	TOP_RIGHT(3,300,300, "TR"),
	TOP_LEFT(4,0,300, "TL"),
	NULL(0,0,0, "NULL");
	
	private int id, x, y;
	private String name;
	
	/**
	 * Default constructor
	 * @param id
	 * @param x
	 * @param y
	 * @param name
	 */
	private StartCorner(int id, int x, int y, String name) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.name = name;
	}
	
	/**
	 * Method that returns the name of the corner as a String
	 */
	public String toString() {
		return this.name;
	}
	
	/**
	 * Method that returns coordinates
	 * @return array of integers that define the coordinates
	 */
	public int[] getCooridinates() {
		return new int[] {this.x, this.y};
	}
	
	/**
	 * Method that returns the X-pos
	 * @return integer that defines x-pos
	 */
	public int getX() {
		return this.x;
	}
	
	/**
	 * Method that returns y pos
	 * @return y pos integer
	 */
	public int getY() {
		return this.y;
	}
	
	/**
	 * Method that returns the id
	 * @return integer that represents the id
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Method that returns a StartCorner
	 * @param cornerId to look up
	 * @return the StartCorner
	 */
	public static StartCorner lookupCorner(int cornerId) {
		for (StartCorner corner : StartCorner.values())
			if (corner.id == cornerId)
				return corner;
		return NULL;
	}
}
