package game;

import exceptions.*;

public class Game {

    private Player player1;
    private Player player2;
    private Board board;
    private boolean firstPlayersTurn;

    public Game(String username1, String username2) {
        player1 = new Player(username1);
        player2 = new Player(username2);
        board = new Board();
        board.setUpBoard();
    	firstPlayersTurn = true;
    }
    
    public void setStrategy(String username, String strategyName) throws PlayerNotFoundException, StrategyNotFoundException {
    	Strategy strategy = null;
    	if (strategyName.equals("random")) strategy = new RandomStrategy();
    	else if (strategyName.equals("dumb")) strategy = new DumbStrategy();
    	else if (strategyName.equals("smart")) strategy = new SmartStrategy();
    	if (strategy == null) throw new StrategyNotFoundException();
    	
    	if (player1.getUsername().equals(username)) {
    		player1.setStrategy(strategy);
    	} else if (player2.getUsername().equals(username)) {
    		player2.setStrategy(strategy);
    	} else {
    		throw new PlayerNotFoundException();
    	}	
    }
    
    public boolean isPlayersTurn(String username) {
    	if (firstPlayersTurn) {
    		return getPlayer1().getUsername().equals(username);
    	} else {
    		return getPlayer2().getUsername().equals(username);
    	}
    }
    
    /**
     * 
     * @return Returns if the game is over after this move.
     */
    public boolean makeMove(String username, Move move) throws PlayerNotFoundException, WrongTurnException, IllegalMoveException {
    	if (!player1.getUsername().equals(username) && !player2.getUsername().equals(username)) {
    		throw new PlayerNotFoundException();
    	}
    	
    	if (!isPlayersTurn(username)) {
    		throw new WrongTurnException();
    	}
    	
    	if (!board.checkValidMove(move)) {
    		throw new IllegalMoveException();
    	}
    	
    	if (firstPlayersTurn) {
    		player1.makeMove(board, move);
    	} else {
    		player2.makeMove(board, move);
    	}
    	
    	firstPlayersTurn = !firstPlayersTurn;
    	
    	return board.gameOver();
    }
    
    public boolean makeMove(String username) throws PlayerNotFoundException, WrongTurnException {
    	if (!player1.getUsername().equals(username) && !player2.getUsername().equals(username)) {
    		throw new PlayerNotFoundException();
    	}
    	
    	if (!isPlayersTurn(username)) {
    		throw new WrongTurnException();
    	}
    	
    	if (firstPlayersTurn) {
    		player1.makeMove(board);
    	} else {
    		player2.makeMove(board);
    	}
    	
    	firstPlayersTurn = !firstPlayersTurn;
    	
    	return board.gameOver();
    }
    
    /**
     * 
     * @return Returns 0 for draw, 1 or 2 for the winning player's number.
     */
    public int getWinner() {
    	int points1 = player1.getPoints();
    	int points2 = player2.getPoints();
    	
    	if (points1 > points2) return 1;
    	else if (points2 > points1) return 2;
    	else {
    		int balls1 = player1.getTotalBalls();
    		int balls2 = player2.getTotalBalls();
    		
    		if (balls1 > balls2) return 1;
    		else if (balls2 > balls1) return 2;
    		else return 0;
    	}
    }
    
    public Player getPlayer1() {
    	return player1;
    }
    
    public Player getPlayer2() {
    	return player2;
    }
    
    public boolean isFirstPlayersTurn() {
    	return firstPlayersTurn;
    }
    
    public Board getBoard() {
    	return board;
    }
    
    public int[] getBoardArray() {
    	return board.convertToArray();
    }
    
    public static void main(String[] args) {
    	Game game = new Game("Alice", "Bob");
    	Board board = game.getBoard();
    	Strategy strategy1 = game.getPlayer1().getStrategy();
    	Strategy strategy2 = game.getPlayer2().getStrategy();

    	try {
    		boolean gameOver = false;
        	while(!gameOver) {
        		if (game.isFirstPlayersTurn()) {
        			Move move = strategy1.determineMove(board);
        			System.out.println(move);
        			gameOver = game.makeMove("Alice");
        		} else {
        			Move move = strategy2.determineMove(board);
        			System.out.println(move);
        			gameOver = game.makeMove("Bob");
        		}
        		board.printBoard();
        	}
    	} catch (Exception e) {
    		System.out.println("Unexpected error.");
    	}
    }
}
