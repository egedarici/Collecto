package test;

import game.BallSet;
import game.Board;
import game.Move;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void setUpBoardTest() {
    	for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                assertEquals(0, board.getBall(row, col));
            }
        }
    	
    	board.setUpBoard();
        assertEquals(0, board.getBall(3, 3));
        assertEquals(1, board.emptySpaces());
        BallSet removed = board.removeSame();
        assertNotNull(removed);
        assertEquals(0, removed.getTotalBalls());
    }

    @Test
    void isSameTest() {
        board.setBall(3, 3, 1);
        board.setBall(3, 2, 2);
        board.setBall(3, 4, 3);
        board.setBall(2, 3, 4);
        board.setBall(4, 3, 5);
        assertFalse(board.isSame(3, 3));
        for (int k = 2; k <= 5; k++) {
            board.setBall(3, 3, k);
            assertTrue(board.isSame(3, 3));
        }
    }

    @Test
    void checkValidSingleMoveTest() {
        board.setBall(3, 3, 6);
        assertFalse(board.checkValidSingleMove(0));
        assertFalse(board.checkValidSingleMove(3));
        board.setBall(0, 3, 6);
        assertTrue(board.checkValidSingleMove(17));
    }
    
    @Test
    void checkValidSingleMoveTestIlegalPush() {
    	board.setBall(3, 3, 6);
    	assertFalse(board.checkValidSingleMove(-1));
    	assertFalse(board.checkValidSingleMove(28));
    }

    @Test
    void checkAnyValidSingleMoveTest() {
       assertFalse(board.checkAnyValidSingleMove());
       board.setBall(0,0,1);
       board.setBall(1,1,2);
       board.setBall(2,2,2);
       assertFalse(board.checkAnyValidSingleMove());
       board.setBall(0,4,1);
       assertTrue(board.checkAnyValidSingleMove());
    }

    @Test
    void checkValidDoubleMoveTest(){
        board.setBall(0,0,1);
        board.setBall(1,1,2);
        board.setBall(2,2,2);
        assertFalse(board.checkValidDoubleMove(2,21));
        assertTrue(board.checkValidDoubleMove(2,14));
        board.setBall(5,1,2);
        assertFalse(board.checkValidDoubleMove(2,14));
    }
    
    @Test
    void checkValidDoubleMoveTestIllegalPush() {
    	board.setBall(0,0,1);
        board.setBall(1,1,2);
        board.setBall(2,2,2);
        assertFalse(board.checkValidDoubleMove(-1,21));
        assertFalse(board.checkValidDoubleMove(28,21));
        assertFalse(board.checkValidDoubleMove(2,-1));
        assertFalse(board.checkValidDoubleMove(2,28));
    }


    @Test
    void checkAnyValidDoubleMoveTest(){
        assertFalse(board.checkAnyValidDoubleMove());
        board.setBall(0,0,1);
        board.setBall(1,1,2);
        board.setBall(2,2,3);
        assertFalse(board.checkAnyValidDoubleMove());
        board.setBall(2,2,2);
        assertTrue(board.checkAnyValidDoubleMove());
        board.setBall(5,1,2);
        assertFalse(board.checkAnyValidDoubleMove());
    }
    
    @Test
    void checkValidMoveTest() {
    	board.setBall(3, 3, 6);
    	board.setBall(0, 3, 6);
    	
    	Move move = new Move(17,-1);
    	assertTrue(board.checkValidMove(move));
    	
    	move = new Move(4,-1);
    	assertFalse(board.checkValidMove(move));
    	
    	move = new Move(-1,-1);
    	assertFalse(board.checkValidMove(move));
    	
    	move = new Move(28,-1);
    	assertFalse(board.checkValidMove(move));
    	
    	board.setBall(3, 3, 0);
        board.setBall(0,0,1);
        board.setBall(1,1,2);
        board.setBall(2,2,2);
        
        move = new Move(2,21);
        assertFalse(board.checkValidMove(move));
        
        move = new Move(2,14);
        assertTrue(board.checkValidMove(move));
        
        move = new Move(-1,14);
        assertFalse(board.checkValidMove(move));
        
        move = new Move(28,14);
        assertFalse(board.checkValidMove(move));
        
        move = new Move(2,-2);
        assertFalse(board.checkValidMove(move));
        
        move = new Move(2,28);
        assertFalse(board.checkValidMove(move));
        
        board.setBall(5,1,2);
        
        move = new Move(2,14);
        assertFalse(board.checkValidMove(move));  
    }
}