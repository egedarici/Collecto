package game;


public class DumbStrategy implements Strategy{
	/**
	 * This method determines a possible move by comparing the valid moves.
	 * @requires board != [0][0]
	 * @return a possible move by checking the valid moves
	 */
	public Move determineMove(Board board) {
		if (board.checkAnyValidSingleMove()) {
			for (int m = 0; m <= 27; m++) {
				if (board.checkValidSingleMove(m)) {
					return new Move(m, -1);
				}
			}
		} else {
			for (int m1 = 0; m1 <= 27; m1++) {
				for (int m2 = 0; m2 <= 27; m2++) {
					if (board.checkValidDoubleMove(m1, m2)) {
						return new Move(m1, m2);
					}
				}
			}
		}
		return null;
	}
}
