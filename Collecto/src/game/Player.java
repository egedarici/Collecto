package game;

public class Player {
	/**
	 * Username of this player.
	 */
    private String username;
    
    /**
     * Set of collectedBalls
     */
    private BallSet collectedBalls;
    
    /**
     * Strategy variable
     */
    private Strategy strategy;
    
    /**
     * Constructor for class Player
     * @requires username != null
     * @param username
     */
    public Player(String username) {
        this.username = username;
        collectedBalls = new BallSet();
        strategy = new SmartStrategy();
    }
    
    /**
     * @ensures a getter for username.
     * @return username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * @ensures a setter for username.
     * @requires username != null
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * @ensures a getter for strategy.
     * @return strategy
     */
    public Strategy getStrategy() {
    	return strategy;
    }
    
    /**
     * @ensures a setter for strategy.
     * @requires strategy != null
     */
    public void setStrategy(Strategy strategy) {
    	this.strategy = strategy;
    }

    /**
     * @requires board != [0][0] && move != null
     * @ensures adds the collected balls which are calculated by board.makeMove(), to Set collectededBalls.
     */
    public void makeMove(Board board, Move move) {
        BallSet collected = board.makeMove(move);
        collectedBalls.collectBalls(collected);
    }
    
    /**
     * @requires board != [0][0]
     * @ensures puts determined move to a Move move object and uses move to makeMove.
     */
    public void makeMove(Board board) {
    	Move move = strategy.determineMove(board);
    	makeMove(board, move);
    }
    
    /**
     * @requires board != [0][0]
     * @ensures returns a specific move calculated by determineMove in order to give a valid move hint to the user.
     * @return a specific move calculated by determineMove
     */
    public Move getHint(Board board) {
    	return strategy.determineMove(board);
    }
    
    /**
     * @requires balls != 0
     * @ensures adds the balls determined by collectBalls to the set collectedBalls
     * @param balls
     */
    public void collectBalls(BallSet balls) {
    	collectedBalls.collectBalls(balls);
    }
    
    /**
     * @ensures a getter for total ball count in collectedBalls
     * @return total number of balls collected
     */
    public int getTotalBalls() {
        return collectedBalls.getTotalBalls();
    }
    
    /**
     * @ensures a getter for total points in collectedBalls
     * @return total number of points collected
     */
    public int getPoints() {
        return collectedBalls.getPoints();
    }
}
