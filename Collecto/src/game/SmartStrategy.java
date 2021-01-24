package game;

import java.util.ArrayList;
import java.util.List;

public class SmartStrategy implements Strategy{
	
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
			int maxRemoved = 0;
			Move bestMove = null;
			for (int i = 0; i < possibleMoves.size(); i++) {
				Move move = possibleMoves.get(i);
				Board copy = new Board(board);
				BallSet removed = copy.makeMove(move);
				int removedCount = removed.getTotalBalls();
				if (removedCount > maxRemoved) {
					maxRemoved = removedCount;
					bestMove = move;
				}
			}
			return bestMove;
		} else {
			return null;
		}
	}
}
