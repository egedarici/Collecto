package game;

import java.util.ArrayList;
import java.util.List;

public class RandomStrategy implements Strategy{
	/**
	 * This method determines a random move by comparing the valid moves 
	 * @requires board != [0][0]
	 * @return a random move by checking the valid moves
	 */
	public Move determineMove(Board board) {
		List<Move> possibleMoves = new ArrayList<Move>();
		
		if (board.checkAnyValidSingleMove()) {
			for (int m = 0; m <= 27; m++) {
				if (board.checkValidSingleMove(m)) {
					possibleMoves.add(new Move(m, -1));
				}
			}
		} else {
			for (int m1 = 0; m1 <= 27; m1++) {
				for (int m2 = 0; m2 <= 27; m2++) {
					if (board.checkValidDoubleMove(m1, m2)) {
						possibleMoves.add(new Move(m1, m2));
					}
				}
			}
		}
		
		if (possibleMoves.size() > 0) {
			int randomNum = (int) (Math.random() * possibleMoves.size());
			return possibleMoves.get(randomNum);
		} else {
			return null;
		}
	}
}
