package game;

public class BallSet {
	
	/**
	 * This is an array of length 6 which holds how many balls of each color are in this ball set.
	 * e.g. if balls[4] == 10, that means this set contains 10 balls of the 4th color.
	 * @requires balls.length == 6
	 */
	private int[] balls;
	
	/**
	 * Constructor of BallSet
	 */
	public BallSet() {
		balls = new int[6];
	}
	
	/**
	 * Constructor of BallSet
	 * @requires balls2 != 0
	 */
	public BallSet(BallSet balls2) {
		balls = new int[6];
		for (int i=0; i < 6; i++) {
			balls[i] = balls2.getBallCount(i);
		}
	}
	
	/**
	 * Constructor of BallSet
	 * @requires c1 = 1, c2 = 2, c3 = 3, c4 = 4, c5 = 5, c6 = 6
	 * @param c1
	 * @param c2
	 * @param c3
	 * @param c4
	 * @param c5
	 * @param c6
	 */
	public BallSet(int c1, int c2, int c3, int c4, int c5, int c6) {
		balls = new int[6];
		balls[0] = c1;
		balls[1] = c2;
		balls[2] = c3;
		balls[3] = c4;
		balls[4] = c5;
		balls[5] = c6;
	}
	
	/**
	 * This method adds the collected ball count to the balls array.
	 * @requires collected != 0
	 * @param collected
	 */
	public void collectBalls(BallSet collected) {
		for (int i = 0; i < 6; i++) {
			balls[i] += collected.getBallCount(i);
		}

	}
	
	/**
	 * This method adds the total number of balls to total
	 * @return the total number of balls in the balls array
	 */
	public int getTotalBalls() {
		int total = 0;
		for (int i = 0; i < 6; i++) {
			total += balls[i];
		}
		return total;
	}
	
	/**
	 * This method adds 1 point when there are 3 of the same numbered balls.
	 * @return the total number of points 
	 */
	public int getPoints() {
		int points = 0;
		for (int i = 0; i < 6; i++) {
			points += balls[i] / 3;
		}
		return points;
	}
	
	/**
	 * @requires colorNum >= 0 && colorNum <= 5
	 * @param colorNum
	 * @return the ball count for a specific color of balls
	 */
	public int getBallCount(int colorNum) {
		return balls[colorNum];
	}
	
	/**
	 * @requires colorNum >= 0 && colorNum <= 5
	 * @param colorNum
	 * @param ballCount
	 */
	public void setBallCount(int colorNum, int ballCount) {
		balls[colorNum] = ballCount;
	}
}
