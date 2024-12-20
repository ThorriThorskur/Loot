package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.Entity.Deck;
import is.hi.hbv501g.loot.Entity.DeckCard;
import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.CardService;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class DeckController {

    @Autowired
    private UserService userService;

    @Autowired
    private CardService cardService;

    /**
     * Displays the user's deck.
     *
     * @param deckId  The ID of the deck.
     * @param model   The model to pass data to the view.
     * @return The view template for displaying the deck.
     */
    @GetMapping("/user/deck/{deckId}")
    public String viewDeck(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long deckId,
            Model model) {
        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
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

        String base64Picture = deck.getPicture() != null
                ? Base64.getEncoder().encodeToString(deck.getPicture())
                : null;

        model.addAttribute("user", user);
        model.addAttribute("deck", deck);
        model.addAttribute("base64Picture", base64Picture);

        return "user_deck";
    }



    private List<DeckCard> filterDeckCards(Deck deck, String color, String type) {
        return deck.getDeckCards().stream()
                .filter(deckCard -> {
                    Card card = deckCard.getCard();
                    boolean matches = true;


                    if (color != null && !color.isEmpty()) {
                        matches &= card.getManaCost() != null && card.getManaCost().contains(color);
                    }

                    if (type != null && !type.isEmpty()) {
                        matches &= card.getTypeLine() != null && card.getTypeLine().toLowerCase().contains(type.toLowerCase());
                    }

                    return matches;
                })
                .collect(Collectors.toList());
    }


    /**
     * Adds a card to the user's deck.
     *
     * @param cardId  The ID of the card to add.
     * @param model   The model to pass data to the view.
     * @return Redirects to the user's inventory page.
     */
    @PostMapping("/addCardToDeck")
    public String addCardToDeck(@RequestParam("cardId") String cardId, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
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

        return "redirect:/user/inventory";
    }

    /**
     * Creates a new deck for the user.
     *
     * @param userId  The ID of the user.
     * @param model   The model to pass data to the view.
     * @return Redirects to the user's inventory page.
     */
    @PostMapping("/createDeck")
    public String createDeck(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
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

        return "redirect:/user/inventory";
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
    @PostMapping("/user/deck/{deckId}/removeCard")
    public String removeCardFromDeck(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long deckId, @RequestParam("cardId") String cardId, Model model) {
        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
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

        return "redirect:/user/deck/" + deckId;
    }

    /**
     * Verifies if the deck has at least 5 cards.
     *
     * @param deckId  The ID of the deck.
     * @param model   The model to pass data to the view.
     * @return The view template for the deck with the verification message.
     */
    @PostMapping("/user/deck/{deckId}/verify")
    public String verifyDeck(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long deckId, Model model) {
        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
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

    /**
     * Searches for cards within the user's deck based on provided filters.
     *
     * @param userId The ID of the user.
     * @param deckId The ID of the deck.
     * @param query The search query.
     * @param color The card color filter.
     * @param rarity The card rarity filter.
     * @param isLegendary Filter to include only legendary cards.
     * @param isLand Filter to include only land cards.
     * @param model The model to pass data to the view.
     * @return The view template for displaying the filtered deck.
     */
    @GetMapping("/user/deck/{deckId}/search")
    public String searchDeck(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long deckId,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "rarity", required = false) String rarity,
            @RequestParam(value = "isLegendary", required = false) Boolean isLegendary,
            @RequestParam(value = "isLand", required = false) Boolean isLand,
            Model model) {

        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
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

        // Apply search filters to the deck cards
        List<DeckCard> filteredDeckCards = deck.getDeckCards().stream()
                .filter(deckCard -> {
                    Card card = deckCard.getCard();
                    boolean matches = true;

                    // Check card name matches query if query is provided
                    if (query != null && !query.isEmpty()) {
                        matches &= card.getName() != null && card.getName().toLowerCase().contains(query.toLowerCase());
                    }

                    // Check mana color if provided
                    if (color != null && !color.isEmpty()) {
                        matches &= card.getManaCost() != null && card.getManaCost().contains(color);
                    }

                    // Check type if provided
                    if (type != null && !type.isEmpty()) {
                        matches &= card.getTypeLine() != null && card.getTypeLine().toLowerCase().contains(type.toLowerCase());
                    }


                    // Check if card is legendary
                    if (isLegendary != null && isLegendary) {
                        matches &= card.isLegendary();
                    }

                    // Check if card is a land
                    if (isLand != null && isLand) {
                        matches &= card.isLand();
                    }

                    return matches;
                })
                .collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("deck", deck);
        model.addAttribute("deckCards", filteredDeckCards); // Make sure to add the filtered list to the model

        return "user_deck";
    }


    @PostMapping("/user/deleteDeck")
    public String deleteDeck(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();

        // Assuming a user can only have one deck
        if (!user.getDecks().isEmpty()) {
            Deck deck = user.getDecks().get(0); // Get the single deck
            user.getDecks().remove(deck); // Remove it from the user's decks
            userService.save(user); // Save the user without the deck
        } else {
            model.addAttribute("error", "No deck found to delete.");
            return "error";
        }

        return "redirect:/user/inventory"; // Redirect back to the user's inventory
    }

    @PostMapping("/user/deck/{deckId}/uploadPicture")
    public String uploadDeckPicture(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long deckId,
            @RequestParam("deckPicture") MultipartFile deckPicture,
            Model model) {
        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
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

        try {
            deck.setPicture(deckPicture.getBytes());
            userService.save(user); // Assuming deck is saved via user
        } catch (IOException e) {
            model.addAttribute("error", "Failed to upload picture.");
            return "error";
        }

        return "redirect:/user/deck/" + deckId;
    }

    @PostMapping("/user/deck/{deckId}/removePicture")
    public String removeDeckPicture(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long deckId,
            Model model) {
        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
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

        deck.setPicture(null);
        userService.save(user); // Assuming deck is saved via user

        return "redirect:/user/deck/" + deckId;
    }





}

