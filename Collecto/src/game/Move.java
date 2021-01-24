package game;

import protocols.ProtocolMessages;

public class Move {
	/**
	 * First move of this Move. This should always be a valid move number.
	 */
	private int firstMove;
	/**
	 * Second move of this Move. If Move is a double move, this should be a valid move number. If Move is a single move, this should be -1.
	 */
	private int secondMove;
	
	/**
	 * @requires move1 >= 0 && move1 <= 27
	 * @requires (move2 >= 0 && move2 <= 27) || move2 == -1
	 * @param move1 
	 * @param move2
	 */
	public Move(int move1, int move2) {
		firstMove = move1;
		secondMove = move2;
	}
	
	public int getFirstMove() {
		return firstMove;
	}
	
	public int getSecondMove() {
		return secondMove;
	}
	
	public boolean isSingleMove() {
		return secondMove == -1;
	}
	
	public String toString() {
		if (secondMove == -1) return ProtocolMessages.DELIM + firstMove;
		return ProtocolMessages.DELIM + firstMove + ProtocolMessages.DELIM + secondMove;
	}
	

}
