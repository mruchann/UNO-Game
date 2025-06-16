package tr.edu.metu.ceng.uno;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import tr.edu.metu.ceng.uno.card.*;
import tr.edu.metu.ceng.uno.util.CardUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UnoApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testDeckSize() {
		List<Card> deck = CardUtil.getShuffledDeck();
		assertEquals(123, deck.size()); // one bonus added
	}

	@Test
	void testToString() {
		Card card1 = new ActionCard(CardType.SKIP, CardColor.BLUE);
		Card card2 = new WildCard(CardType.WILD_DRAW_FOUR);
		Card card3 = new NumberCard(CardColor.RED, 3);

		assertEquals("BLUE SKIP", card1.toString());
		assertEquals("WILD_DRAW_FOUR", card2.toString());
		assertEquals("RED 3", card3.toString());
	}

	@Test
	void testIsValidMoveWildCard() {
		Card cardToPlay = new WildCard(CardType.WILD);
		Card lastPlayedCard = new NumberCard(CardColor.RED, 3);
		CardColor currentCardColor = CardColor.RED;

        assertTrue(CardUtil.isValidMove(cardToPlay, lastPlayedCard, currentCardColor));
	}

	@Test
	void testIsValidMoveNumberCard() {
		Card cardToPlay = new NumberCard(CardColor.RED,1);
		Card lastPlayedCard = new NumberCard(CardColor.BLUE, 1);
		CardColor currentCardColor = CardColor.BLUE;

        assertTrue(CardUtil.isValidMove(cardToPlay, lastPlayedCard, currentCardColor));
	}

	@Test
	void testIsValidMoveActionCard() {
		Card cardToPlay = new ActionCard(CardType.SKIP, CardColor.BLUE);
		Card lastPlayedCard = new ActionCard(CardType.SKIP, CardColor.RED);
		CardColor currentCardColor = CardColor.RED;

		assertTrue(CardUtil.isValidMove(cardToPlay, lastPlayedCard, currentCardColor));
	}
}
