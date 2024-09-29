package is.hi.hbv501g.loot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LootApplication {

    public static void main(String[] args) {
        SpringApplication.run(LootApplication.class, args);
    }

}

/*TODO :Create Models (Entities)   If you're working with a database, define your data structure. 
 //you could create entities like Card, Player, or Inventory.
@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String rarity;
    // Getters and Setters
}

*/

/* TODO :Set Up Repositories
Create repository interfaces for your entities to handle database operations. Repositories extend Spring Data’s CrudRepository or JpaRepository.

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByName(String name);
}
*/


/* TODO :Create Services
Define your service layer, where business logic resides. Services call repositories and handle the core functionality.
@Service
public class CardService {
    @Autowired
    private CardRepository cardRepository;

    public List<Card> findAllCards() {
        return cardRepository.findAll();
    }

    public Card saveCard(Card card) {
        return cardRepository.save(card);
    }
}  
*/


/* TODO :Create Controllers
Set up RESTful controllers that will handle HTTP requests and call services to execute application logic. 
@RestController
@RequestMapping("/cards")
public class CardController {
    @Autowired
    private CardService cardService;

    @GetMapping
    public List<Card> getAllCards() {
        return cardService.findAllCards();
    }

    @PostMapping
    public Card createCard(@RequestBody Card card) {
        return cardService.saveCard(card);
    }
}
*/
/*
 Database Configuration
Ensure that our application.properties file in src/main/resources/ is configured to point to your database (e.g., PSQL).

spring.datasource.url=jdbc:mysql://localhost:3306/loot   #railway.app database config needed
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
*/