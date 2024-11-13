package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.Entity.Deck;
import is.hi.hbv501g.loot.Entity.DeckCard;
import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.CardService;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

@Controller
public class DeckController {

    @Autowired
    private UserService userService;

    @Autowired
    private CardService cardService;

    /**
     * Displays the user's deck.
     *
     * @param userId  The ID of the user.
     * @param deckId  The ID of the deck.
     * @param model   The model to pass data to the view.
     * @return The view template for displaying the deck.
     */
    @GetMapping("/user/{userId}/deck/{deckId}")
    public String viewDeck(@PathVariable Long userId, @PathVariable Long deckId, Model model) {
        Optional<UserEntity> userOptional = userService.findById(userId);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();
        Deck deck = user.getDecks().stream()
                .filter(d -> d.getId().equals(deckId))
                .findFirst()
                .orElse(null);

        if (deck == null) {
            model.addAttribute("error", "Deck not found.");
            return "error";
        }

        model.addAttribute("user", user);
        model.addAttribute("deck", deck);
        return "user_deck"; // This is the view template for displaying the deck
    }

    /**
     * Adds a card to the user's deck.
     *
     * @param cardId  The ID of the card to add.
     * @param userId  The ID of the user.
     * @param model   The model to pass data to the view.
     * @return Redirects to the user's inventory page.
     */
    @PostMapping("/addCardToDeck")
    public String addCardToDeck(@RequestParam("cardId") String cardId, @RequestParam("userId") Long userId, Model model) {
        Optional<UserEntity> userOptional = userService.findById(userId);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();

        // Assuming you have a single deck or a default deck for now
        Deck deck = user.getDecks().isEmpty() ? null : user.getDecks().get(0);
        if (deck == null) {
            model.addAttribute("error", "No deck found.");
            return "error";
        }

        // Add card logic
        Optional<Card> cardOptional = cardService.findById(cardId);
        if (cardOptional.isPresent()) {
            Card card = cardOptional.get();
            deck.addCard(card);
            userService.save(user);
        }

        return "redirect:/user/" + userId + "/inventory";
    }

    /**
     * Creates a new deck for the user.
     *
     * @param userId  The ID of the user.
     * @param model   The model to pass data to the view.
     * @return Redirects to the user's inventory page.
     */
    @PostMapping("/createDeck")
    public String createDeck(@RequestParam("userId") Long userId, Model model) {
        Optional<UserEntity> userOptional = userService.findById(userId);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();

        // Create a new deck for the user if none exists
        if (user.getDecks().isEmpty()) {
            Deck newDeck = new Deck("New Deck", user);
            user.addDeck(newDeck);
            userService.save(user);
        }

        return "redirect:/user/" + userId + "/inventory";
    }

    /**
     * Removes a card from the user's deck.
     *
     * @param userId  The ID of the user.
     * @param deckId  The ID of the deck.
     * @param cardId  The ID of the card to remove.
     * @param model   The model to pass data to the view.
     * @return Redirects to the deck page.
     */
    @PostMapping("/user/{userId}/deck/{deckId}/removeCard")
    public String removeCardFromDeck(@PathVariable Long userId, @PathVariable Long deckId, @RequestParam("cardId") String cardId, Model model) {
        Optional<UserEntity> userOptional = userService.findById(userId);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();
        Deck deck = user.getDecks().stream()
                .filter(d -> d.getId().equals(deckId))
                .findFirst()
                .orElse(null);

        if (deck == null) {
            model.addAttribute("error", "Deck not found.");
            return "error";
        }

        // Find and remove the DeckCard
        Optional<DeckCard> deckCardOptional = deck.getDeckCards().stream()
                .filter(deckCard -> deckCard.getCard().getId().equals(cardId))
                .findFirst();

        if (deckCardOptional.isPresent()) {
            DeckCard deckCard = deckCardOptional.get();
            deck.getDeckCards().remove(deckCard);
            userService.save(user);
        } else {
            model.addAttribute("error", "Card not found in the deck.");
            return "error";
        }

        return "redirect:/user/" + userId + "/deck/" + deckId;
    }

    /**
     * Verifies if the deck has at least 5 cards.
     *
     * @param userId  The ID of the user.
     * @param deckId  The ID of the deck.
     * @param model   The model to pass data to the view.
     * @return The view template for the deck with the verification message.
     */
    @PostMapping("/user/{userId}/deck/{deckId}/verify")
    public String verifyDeck(@PathVariable Long userId, @PathVariable Long deckId, Model model) {
        Optional<UserEntity> userOptional = userService.findById(userId);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();
        Deck deck = user.getDecks().stream()
                .filter(d -> d.getId().equals(deckId))
                .findFirst()
                .orElse(null);

        if (deck == null) {
            model.addAttribute("error", "Deck not found.");
            return "error";
        }

        // Verify if the deck has at least 30 cards
        int totalCards = deck.getDeckCards().stream().mapToInt(DeckCard::getCount).sum();
        if (totalCards >= 5) {
            model.addAttribute("verificationMessage", "Deck is valid. It has at least 5 cards.");
        } else {
            model.addAttribute("verificationMessage", "Deck is invalid. It must have at least 5 cards.");
        }

        // Re-add user and deck information to the model for the view
        model.addAttribute("user", user);
        model.addAttribute("deck", deck);

        return "user_deck"; // Return to the deck view with the verification message
    }


}

