package is.hi.hbv501g.loot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LootApplicationTests {

    @Test
    void contextLoads() {
    }

}


/*
Add tests for your service and controller layers. Expand the LootApplicationTests.java file or create new test files. You can use JUnit and Mockito for this.

@SpringBootTest
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    public void testFindAllCards() {
        List<Card> mockCards = Arrays.asList(new Card("Magic Card"));
        when(cardRepository.findAll()).thenReturn(mockCards);

        List<Card> cards = cardService.findAllCards();
        assertEquals(1, cards.size());
    }
}

*/