package game;

public class BallSet {
	
	/**
	 * This is an array of length 6 which holds how many balls of each color are in this ball set.
	 * e.g. if balls[4] == 10, that means this set contains 10 balls of the 4th color.
	 * @requires balls.length == 6
	 */
	private int[] balls;
	
	public BallSet() {
		balls = new int[6];
	}
	
	public BallSet(BallSet balls2) {
		balls = new int[6];
		for (int i=0; i < 6; i++) {
			balls[i] = balls2.getBallCount(i);
		}
	}
	
	public BallSet(int c1, int c2, int c3, int c4, int c5, int c6) {
		balls = new int[6];
		balls[0] = c1;
		balls[1] = c2;
		balls[2] = c3;
		balls[3] = c4;
		balls[4] = c5;
		balls[5] = c6;
	}
	
	public void collectBalls(BallSet collected) {
		for (int i = 0; i < 6; i++) {
			balls[i] += collected.getBallCount(i);
		}

	}

	public int getTotalBalls() {
		int total = 0;
		for (int i = 0; i < 6; i++) {
			total += balls[i];
		}
		return total;
	}

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
	 * @return
	 */
	public int getBallCount(int colorNum) {
		return balls[colorNum];
	}
	
	public void setBallCount(int colorNum, int ballCount) {
		balls[colorNum] = ballCount;
	}
}
