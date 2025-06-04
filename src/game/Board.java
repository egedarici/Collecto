package game;

import java.util.ArrayList;
import java.util.List;

public class Board {
	
	/**
	 * This is an array of length 49 which holds how many spaces are on the board.
	 * @requires board.length == 49
	 */
    private int[][] board;
    
    /**
     * Constructor of Board
     * @ensures board is initialized
     */
    public Board() {
        board = new int[7][7];
    }

    /**
     * Constructor of Board
     * @requires board2 == [7][7]
     * @ensures board is initialized
     */
    public Board(Board board2) {
        board = new int[7][7];
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                board[row][col] = board2.getBall(row, col);
            }
        }
    }
    
    /**
     * @ensures Sets the 7x7 board with random numbers between 0 and 6. Middle of the board (3x3) is 0.
     */
    public void setUpBoard() {
    	for (int row = 0; row < 7; row++) {
             for (int col = 0; col < 7; col++) {
                 if (row == 3 && col == 3) continue;
                 boolean selected = false;
                 int selectedNum = 0;
                 while(!selected) {
                     selectedNum = 1 + (int) (Math.random() * 6);
                     selected = true;
                     if (row != 0 && board[row - 1][col] == selectedNum) {
                         selected = false;
                     } else if (col != 0 && board[row][col - 1] == selectedNum) {
                         selected = false;
                     }
                 }
                 board[row][col] = selectedNum;
             }
         }
    }
    
    /**
     * @requires initialBoard.length == 49 
     * @requires initialBoard's values must be between 0 and 6.
     * @ensures board is set with initialBoard
     * @param initialBoard
     */
    public void setUpBoard(int[] initialBoard) {
    	for (int i = 0; i < initialBoard.length; i++) {
           int row = i / 7;
           int col = i % 7;
           board[row][col] = initialBoard[i];
    	}
    }

    /**
     * @param row
     * @param col
     * @requires row > 0 && row <= 7
     * @requires col > 0 && col <= 7
     * @ensures Gets a specific ball on the board
     * @return the number of the specific ball from the board
     */
    public int getBall(int row, int col) {
        return board[row][col];
    }

    /**
     * @param row
     * @param col
     * @param colorNum
     * @requires row > 0 && row <= 7
     * @requires col > 0 && col <= 7
     * @requires colorNum >= 0 && colorNum <= 6
     * @ensures Sets a specific ball on the board
     */
    public void setBall(int row, int col, int colorNum) {
        board[row][col] = colorNum;
    }

    /**
     * @param row
     * @param col
     * @requires row > 0 && row <= 7
     * @requires col > 0 && col <= 7
     * @ensures checks if balls are the same
     * @return true if balls are same, false otherwise
     */
    public boolean isSame(int row, int col) {
        int currentBall = board[row][col];
        if (row != 0 && board[row - 1][col] == currentBall) return true;
        if (col != 0 && board[row][col - 1] == currentBall) return true;
        if (row != 6 && board[row + 1][col] == currentBall) return true;
        if (col != 6 && board[row][col + 1] == currentBall) return true;
        return false;
    }

    /**
     * @return removed ball set
     * @ensures removes the same colored balls that are next to each other.
     */
    public BallSet removeSame() {
        List<Integer> markedBalls = new ArrayList<Integer>();
        BallSet removed = new BallSet();
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                if (board[row][col] == 0) continue;
                if (isSame(row, col)){
                    markedBalls.add(row);
                    markedBalls.add(col);
                    int colorNum = board[row][col];
                    int count = removed.getBallCount(colorNum - 1);
                    removed.setBallCount(colorNum - 1 , count + 1);

                }
            }
        }
        for (int i = 0; i < markedBalls.size(); i+=2) {
            int row = markedBalls.get(i);
            int col = markedBalls.get(i + 1);
            board[row][col] = 0;
        }
        return removed;
    }

    /**
     * @param board2
     * @return true if same, false otherwise
     * @ensures checks if a board has the same components with a board2
     */
    public boolean equals(Board board2) {
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                if (board[row][col] != board2.getBall(row, col)) return false;
            }
        }
        return true;
    }

    /**
     * @return int array version of board
     * @ensures converts the 7x7 board into one array of numbers
     */
    public int[] convertToArray() {
        int[] converted = new int[49];
        int index = 0;
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                converted[index] = board[row][col];
                index++;
            }
        }
        return converted;
    }

    /**
     * @return total number of empty spaces
     * @ensures returns the total number of empty spaces on the board
     */
    public int emptySpaces() {
        int total = 0;
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                if (board[row][col] == 0) total++;
            }
        }
        return total;
    }

    /**
     * @param move
     * @requires move >= 0 && move <= 27
     * @return true if valid, false otherwise
     * @ensures checks if a move is considered legal or not
     */
    public boolean isLegalMove(int move) {
        return move >= 0 && move <= 27;
    }
    
    /**
     * This method prints the board's toString to the console
     */
    public void printBoard() {
    	System.out.println(toString());
    }
    
    /**
     * This method is the String format of board.
     * @return the String version of board
     */
    public String toString() {
    	String boardString = "";
    	for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                boardString += board[row][col] + "  ";
            }
            boardString += "\n";
        }
    	boardString += "\n";
    	boardString += "\n";
    	return boardString;
    }

    /**
     * @param move
     * @return a move
     * @requires move > 0 && move <= 27
     * @ensures makes a move by checking the first move and the second move options
     */
    public BallSet makeMove(Move move) {
    	if (move.isSingleMove()) {
    		return singleMove(move.getFirstMove());
    	} else {
    		return doubleMove(move.getFirstMove(), move.getSecondMove());
    	}
    }

    /**
     * @requires moveNum > 0 && moveNum <= 27
     * @param moveNum
     */
    public BallSet singleMove(int moveNum) {
        return singleMove(moveNum / 7 + 1, moveNum % 7);
    }

    /**
     * @requires direction >= 1 && direction <= 4
     * @requires k >=0 && k <= 6
     * @param direction an integer which represents the direction of the move
     *                  Definitions:
     *                  1 means from the right
     *                  2 means from the left
     *                  3 means from down
     *                  4 means from up
     * @param k an integer which represents the row or column of the move
     * @returns the set of removed balls
     */
    public BallSet singleMove(int direction, int k) {
        if (direction == 1) {
            moveLeft(k);
        } else if (direction == 2) {
            moveRight(k);
        } else if (direction == 3) {
            moveUp(k);
        } else if (direction == 4) {
            moveDown(k);
        }
        return removeSame();
    }

    /**
     * @requires k >=0 && k <= 6
     * @ensures moves a row up
     * @param k
     */
    public void moveUp(int k) {
        for (int row = 0; row < 7; row++) {
            if (board[row][k] != 0) continue;
            int rowToMove = row;
            for (int r = row + 1; r < 7; r++) {
                if (board[r][k] != 0) {
                    rowToMove = r;
                    break;
                }
            }
            if (rowToMove != row) {
                int ball = board[rowToMove][k];
                board[row][k] = ball;
                board[rowToMove][k] = 0;
            } else break;
        }
    }

    /**
     * @requires k >=0 && k <= 6
     * @ensures moves a row down
     * @param k
     */
    public void moveDown(int k) {
        for (int row = 6; row >= 0; row--) {
            if (board[row][k] != 0) continue;
            int rowToMove = row;
            for (int r = row - 1; r >= 0; r--) {
                if (board[r][k] != 0) {
                    rowToMove = r;
                    break;
                }
            }
            if (rowToMove != row) {
                int ball = board[rowToMove][k];
                board[row][k] = ball;
                board[rowToMove][k] = 0;
            } else break;
        }
    }

    /**
     * @requires k >=0 && k <= 6
     * @ensures moves a column to left
     * @param k
     */
    public void moveLeft(int k) {
        for (int col = 0; col < 7; col++) {
            if (board[k][col] != 0) continue;
            int colToMove = col;
            for (int c = col + 1; c < 7; c++) {
                if (board[k][c] != 0) {
                    colToMove = c;
                    break;
                }
            }
            if (colToMove != col) {
                int ball = board[k][colToMove];
                board[k][col] = ball;
                board[k][colToMove] = 0;
            } else break;
        }
    }

    /**
     * @requires k >=0 && k <= 6
     * @ensures moves a column to right
     * @param k
     */
    public void moveRight(int k) {
        for (int col = 6; col >= 0; col--) {
            if (board[k][col] != 0) continue;
            int colToMove = col;
            for (int c = col - 1; c >= 0; c--) {
                if (board[k][c] != 0) {
                    colToMove = c;
                    break;
                }
            }
            if (colToMove != col) {
                int ball = board[k][colToMove];
                board[k][col] = ball;
                board[k][colToMove] = 0;
            } else break;
        }
    }

    /**
     * @requires moveNum1 > 0 && moveNum1 <= 27
     * @requires moveNum2 > 0 && moveNum2 <= 27
     * @ensures makes a double move by getting two moves
     * @param moveNum1
     * @param moveNum2
     */
    public BallSet doubleMove(int moveNum1, int moveNum2) {
        BallSet removed1 = singleMove(moveNum1);
        BallSet removed2 = singleMove(moveNum2);
        removed1.collectBalls(removed2);
        return removed1;
    }
    
    /**
     * @requires move > 0 && move <= 27
     * @ensures checks if a move is valid or not.
     * @param move
     * @return true if valid, false otherwise
     */
    public boolean checkValidMove(Move move) {
    	if(move.isSingleMove()) {
    		return checkValidSingleMove(move.getFirstMove());
    	} else {
    		return checkValidDoubleMove(move.getFirstMove(), move.getSecondMove());
    	}
    }

    /**
     * @requires moveNum > 0 && moveNum <= 27
     * @ensures checks if a move is a valid single move
     * @param moveNum
     * @return true if valid, false otherwise
     */
    public boolean checkValidSingleMove(int moveNum) {
    	if (moveNum < 0 || moveNum > 27) return false;
    	
        Board copy = new Board(this);
        BallSet removed = copy.singleMove(moveNum);
        return removed.getTotalBalls() != 0;
    }

    /**
     * @return true if valid, false otherwise
     * @ensures checks if there are any valid single moves
     */
    public boolean checkAnyValidSingleMove() {
        for (int move = 0; move <= 27; move++) {
            if (checkValidSingleMove(move)) return true;
        }
        return false;
    }

    /**
     * @requires moveNum1 > 0 && moveNum1 <= 27
     * @requires moveNum2 > 0 && moveNum2 <= 27
     * @ensures checks if a double move is a valid double move
     * @param moveNum1
     * @param moveNum2
     * @return true if valid, false otherwise
     */
    public boolean checkValidDoubleMove(int moveNum1, int moveNum2) {
    	if (moveNum1 < 0 || moveNum1 > 27) return false;
    	if (moveNum2 < 0 || moveNum2 > 27) return false;
    	
        if (checkAnyValidSingleMove()) return false;
        Board copy = new Board(this);
        copy.singleMove(moveNum1);
        if (this.equals(copy)) return false;
        BallSet removed = copy.singleMove(moveNum2);
        return removed.getTotalBalls() != 0;
    }
    
    /**
     * @ensures checks if there are any valid double moves
     * @return true if valid, false otherwise
     */
    public boolean checkAnyValidDoubleMove(){
        if(checkAnyValidSingleMove()) return false;
        for (int move1 = 0; move1<=27;move1++){
            for(int move2 = 0; move2<=27;move2++){
                if (checkValidDoubleMove(move1, move2)) return true;
            }
        }
        return false;
    }
    
    /**
     * @return true if over, false otherwise 
     * @ensures checks if the game is over
     */
    public boolean gameOver(){
        if(checkAnyValidSingleMove() || checkAnyValidDoubleMove()) return false;
        return true;
    }
}


