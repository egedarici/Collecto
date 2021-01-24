package game;

public class Player {

    private String username;
    private BallSet collectedBalls;
    private Strategy strategy;

    public Player(String username) {
        this.username = username;
        collectedBalls = new BallSet();
        strategy = new SmartStrategy();
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public Strategy getStrategy() {
    	return strategy;
    }
    
    public void setStrategy(Strategy strategy) {
    	this.strategy = strategy;
    }

   
    public void makeMove(Board board, Move move) {
        BallSet collected = board.makeMove(move);
        collectedBalls.collectBalls(collected);
    }
    
    public void makeMove(Board board) {
    	Move move = strategy.determineMove(board);
    	makeMove(board, move);
    }
    
    public Move getHint(Board board) {
    	return strategy.determineMove(board);
    }
    
    public void collectBalls(BallSet balls) {
    	collectedBalls.collectBalls(balls);
    }

    public int getTotalBalls() {
        return collectedBalls.getTotalBalls();
    }

    public int getPoints() {
        return collectedBalls.getPoints();
    }










}
