package test;

import game.BallSet;
import game.Player;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;
    private final String USERNAME = "Ege Darici";
    private final BallSet BALLS1 = new BallSet(5,2,4,2,0,1);
    private final BallSet BALLS2 = new BallSet(4,2,4,0,0,6);

    @BeforeEach
    void setUp() {
        player = new Player(USERNAME);
    }

    @Test
    void setUpTest(){
        assertEquals(USERNAME,player.getUsername());
        assertEquals(0,player.getTotalBalls());
        assertEquals(0,player.getPoints());
    }

    @Test
    void collectBallsTest(){
        player.collectBalls(BALLS1);
        assertEquals(14,player.getTotalBalls());
        assertEquals(2,player.getPoints());
        player.collectBalls(BALLS2);
        assertEquals(30,player.getTotalBalls());
        assertEquals(8,player.getPoints());
    }

}