package tr.edu.metu.ceng.uno;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tr.edu.metu.ceng.uno.card.*;
import tr.edu.metu.ceng.uno.game.Direction;
import tr.edu.metu.ceng.uno.game.Game;
import tr.edu.metu.ceng.uno.player.ComputerPlayer;
import tr.edu.metu.ceng.uno.player.HumanPlayer;
import tr.edu.metu.ceng.uno.player.Player;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameTest {
    
    private Game game;
    private List<Player> players;
    private Player player1;
    private Player player2;
    private Player player3;
    
    @BeforeEach
    void setUp() {
        //mixture of players
        player1 = new HumanPlayer("Player1");
        player2 = new ComputerPlayer("Player2");
        player3 = new HumanPlayer("Player3");
        players = List.of(player1, player2, player3);
        
        // Create and configure game for testing
        game = new Game(players);
    }
    //STRUCTURE: setup _one space_ execution _one space_ asserts

    @Test
    void testDeckSizeAfterRefilling(){
        List<Card> drawDeck = game.getDrawDeck();
        List<Card> discardDeck = game.getDiscardDeck();
        while(!drawDeck.isEmpty()){
            game.setLastPlayedCard(drawDeck.getFirst());
            discardDeck.add(drawDeck.getFirst());
            drawDeck.removeFirst();
        }
        for(Player player: game.getPlayers()){
            List<Card> cardList = player.getDeck();
            while(!cardList.isEmpty()){
                game.setLastPlayedCard(cardList.getFirst());
                discardDeck.add(cardList.getFirst());
                cardList.removeFirst();
            }
        }
        game.refillDrawDeck();
        assertEquals(122, game.getDrawDeck().size());
    }

    @Test
    void testNormalPlayerProgressionFromFirst() {

        game.setCurrentPlayer(0);
        game.setLastPlayedCard(new NumberCard(CardColor.RED, 5));

        game.setNextPlayerConditionally();

        assertEquals(1, game.getCurrentPlayer());
    }

    @Test
    void testNormalPlayerProgressionFromLast() {

        game.setCurrentPlayer(2);
        game.setLastPlayedCard(new NumberCard(CardColor.RED, 5));

        game.setNextPlayerConditionally();

        assertEquals(0, game.getCurrentPlayer());
    }

    @Test
    void testReverseCardChangesDirectionFromClockwiseToCounterClockwise() {
        game.setDirection(Direction.CLOCKWISE);
        game.setCurrentPlayer(0);

        game.setLastPlayedCard(new ActionCard(CardType.REVERSE, CardColor.RED));
        game.setDirection(game.getDirection().reverse());

        game.setNextPlayerUnconditionally();

        assertEquals(Direction.COUNTER_CLOCKWISE, game.getDirection());
        assertEquals(2, game.getCurrentPlayer());
    }

    @Test
    void testReverseCardChangesDirectionFromCounterClockwiseToClockwise() {
        game.setDirection(Direction.COUNTER_CLOCKWISE);
        game.setCurrentPlayer(0);

        game.setLastPlayedCard(new ActionCard(CardType.REVERSE, CardColor.BLUE));
        game.setDirection(game.getDirection().reverse());

        game.setNextPlayerUnconditionally();

        assertEquals(Direction.CLOCKWISE, game.getDirection());
        assertEquals(1, game.getCurrentPlayer());
    }

    @Test
    void testSkipCardClockwiseFromFirstPlayer() {
        game.setCurrentPlayer(0);
        game.setLastPlayedCard(new ActionCard(CardType.SKIP, CardColor.RED));

        game.setNextPlayerConditionally();

        assertEquals(2, game.getCurrentPlayer(), "Skip card should skip player2 and set player3 as current");
    }

    @Test
    void testSkipCardClockwiseFromSecondPlayer() {
        game.setCurrentPlayer(1);
        game.setLastPlayedCard(new ActionCard(CardType.SKIP, CardColor.RED));

        game.setNextPlayerConditionally();

        assertEquals(0, game.getCurrentPlayer(), "Skip card should skip player3 and set player1 as current");
    }
    
    @Test
    void testSkipCardCounterClockwiseFromThirdPlayer() {
        game.setDirection(Direction.COUNTER_CLOCKWISE);
        game.setCurrentPlayer(2);
        game.setLastPlayedCard(new ActionCard(CardType.SKIP, CardColor.RED));

        game.setNextPlayerConditionally();

        assertEquals(0, game.getCurrentPlayer());
    }

    @Test
    void testSkipCardCounterClockwiseFromSecondPlayer(){
        game.setDirection(Direction.COUNTER_CLOCKWISE);
        game.setCurrentPlayer(1);
        game.setLastPlayedCard(new ActionCard(CardType.SKIP, CardColor.RED));

        game.setNextPlayerConditionally();

        assertEquals(2, game.getCurrentPlayer());
    }


    //TODO: not asserted yet, just printing the content
    @Test
    void testPlayerCurrentDeckContentInitialForm(){
        game.distributeCards();
        game.getPlayers().forEach(Player::stringifyDeck);
    }
}