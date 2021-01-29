package game;

import exceptions.*;

public class Game {

    private Player player1;
    private Player player2;
    private Board board;
    private boolean firstPlayersTurn;
    
    /**
     * Constructor of Game
     * @requires username1 != null 
     * @requires username2 != null
     * @ensures player1, player2, board and firstPlayersTurn is initialized
     * @param username1
     * @param username2
     */
    public Game(String username1, String username2) {
        player1 = new Player(username1);
        player2 = new Player(username2);
        board = new Board();
        board.setUpBoard();
    	firstPlayersTurn = true;
    }
    
    /**
     * @requires username1 != null 
     * @requires username2 != null
     * @requires initialBoard.length == 49 
     * @requires initialBoard's values must be between 0 and 6.
     * @ensures player1, player2, board and firstPlayersTurn is initialized
     * @param username1
     * @param username2
     * @param initialBoard
     */
    public Game(String username1, String username2, int[] initialBoard) {
    	player1 = new Player(username1);
        player2 = new Player(username2);
        board = new Board();
        board.setUpBoard(initialBoard);
    	firstPlayersTurn = true;
    }
    
    /**
     * @requires username != null
     * @requires strategyName can be dumb, random or smart
     * @ensures selected strategy is set to player's strategy
     * @param username
     * @param strategyName
     * @throws PlayerNotFoundException
     * @throws StrategyNotFoundException
     */
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
    
    /**
     * @requires username != null
     * @ensures if it's player's turn or not
     * @param username
     * @return
     */
    public boolean isPlayersTurn(String username) {
    	if (firstPlayersTurn) {
    		return getPlayer1().getUsername().equals(username);
    	} else {
    		return getPlayer2().getUsername().equals(username);
    	}
    }
    
    /**
     * @ensures to get the current player who has to play
     * @return player who has to play
     */
    public Player getCurrentPlayer() {
    	if (isFirstPlayersTurn()) {
    		return player1;
    	} else {
    		return player2;
    	}
    }
    
    /**
    * 
    * @param username
    * @param move
    * @requires username != null
    * @requires move >= 0 && move <= 27 
    * @return Returns if the game is over after this move.
    * @ensures the player who has the turn makes move
    * @throws PlayerNotFoundException
    * @throws WrongTurnException
    * @throws IllegalMoveException
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
    
    /**
     * @requires username != null
     * @param username
     * @return Returns if the game is over after this move.
     * @ensures the player who has the turn makes move
     * @throws PlayerNotFoundException
     * @throws WrongTurnException
     */
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
     * @requires move >= 0 && move <= 27 
     * @param move
     * @return move of the current player
     * @ensures the player who has the turn makes move
     * @throws PlayerNotFoundException
     * @throws WrongTurnException
     * @throws IllegalMoveException
     */
    public boolean makeMove(Move move) throws PlayerNotFoundException, WrongTurnException, IllegalMoveException {
    	String currentPlayer = getCurrentPlayer().getUsername();
    	return makeMove(currentPlayer, move);
    }
    
    /**
     * @ensures to get the player who has most points
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
    
    /**
     * @ensures a getter for player1
     * @return player1
     */
    public Player getPlayer1() {
    	return player1;
    }
    
    /**
     * @ensures a getter for player2
     * @return player2
     */
    public Player getPlayer2() {
    	return player2;
    }
    
    /**
     * @requires username != null
     * @ensures a getter for specific player
     * @param username
     * @return the specific player
     */
    public Player getPlayer(String username) {
    	if (username.equals(player1.getUsername())) return player1;
    	else if (username.equals(player2.getUsername())) return player2;
    	else return null;
    }
    
    /**
     * @ensures to check if it's first player's turn
     * @return
     */
    public boolean isFirstPlayersTurn() {
    	return firstPlayersTurn;
    }
    
    /**
     * @requires username != null
     * @ensures to get the opponent of a specific player
     * @param username
     * @return specific opponent
     */
    public String getOpponent(String username) {
    	if (username.equals(player1.getUsername())) return player2.getUsername();
    	else if (username.equals(player2.getUsername())) return player1.getUsername();
    	else return null;
    }
    
    /**
     * @ensures a getter for board
     * @return board
     */
    public Board getBoard() {
    	return board;
    }
    
    /**
     * @ensures a getter for integer array version of board
     * @return integer array version of board
     */
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
