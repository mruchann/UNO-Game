package tr.edu.metu.ceng.uno.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import tr.edu.metu.ceng.uno.model.CardDTO;
import tr.edu.metu.ceng.uno.model.GameStateDTO;
import tr.edu.metu.ceng.uno.model.PlayerDTO;
import tr.edu.metu.ceng.uno.service.GameService;
import tr.edu.metu.ceng.uno.view.CardImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

@Component
@Slf4j
public class SingleplayerGameViewController {

    private final GameService gameService;
    private final ApplicationContext applicationContext;

    @Value("classpath:/menu-view.fxml")
    private Resource mainMenuResource;

    // FXML injected UI components
    @FXML private Label player1Name;
    @FXML private Label player2Name;
    @FXML private Label player3Name;
    @FXML private Label player4Name;

    // UNO indicators for each player
    @FXML private Label player1UnoIndicator;
    @FXML private Label player2UnoIndicator;
    @FXML private Label player3UnoIndicator;
    @FXML private Label player4UnoIndicator;

    @FXML private HBox player1DeckArea;
    @FXML private VBox player2DeckArea;
    @FXML private HBox player3DeckArea;
    @FXML private VBox player4DeckArea;

    @FXML private StackPane lastPlayedCardArea;
    @FXML private StackPane drawDeckArea;
    @FXML private StackPane currentColorArea;
    @FXML private StackPane directionArea;

    // Cheat action buttons
    @FXML private Button reverseButton;
    @FXML private Button skipButton;
    @FXML private Button drawTwoButton;
    @FXML private Button wildButton;
    @FXML private Button wildDrawFourButton;
    @FXML private Button skipEveryoneOnceButton;

    // Color picker components
    @FXML private GridPane colorPickerPane;
    @FXML private StackPane redColorButton;
    @FXML private StackPane blueColorButton;
    @FXML private StackPane greenColorButton;
    @FXML private StackPane yellowColorButton;

    private CompletableFuture<String> colorSelectionFuture;

    public SingleplayerGameViewController(GameService gameService, ApplicationContext applicationContext) {
        log.info("Creating SingleplayerGameViewController instance");
        this.gameService = gameService;
        this.applicationContext = applicationContext;
        log.debug("SingleplayerGameViewController dependencies injected: gameService, applicationContext");
    }

    /**
     * Initialize method called by JavaFX after FXML is loaded
     */
    @FXML
    public void initialize() {
        log.info("Initializing singleplayer game view");

        // Set this controller instance in the GameService for callbacks
        log.debug("Setting main view controller in GameService");
        gameService.setMainViewController(this);

        // Initialize color picker buttons
        log.debug("Initializing color picker buttons");
        initializeColorPicker();

        // Initialize cheat action buttons
        log.debug("Initializing cheat action buttons");
        initializeCheatButtons();

        // Fetch game state once when the view is initialized
        try {
            log.info("Starting new singleplayer game");
            GameStateDTO gameState = gameService.startGame();
            if (gameState != null) {
                log.debug("Initial game state received, displaying state");
                displayState(gameState);
                refreshUI(gameState);
            } else {
                log.warn("Received null game state during initialization");
            }
        } catch (RestClientException e) {
            log.error("Failed to connect to the game server", e);
            showErrorAlert("Failed to connect to the game server: " + e.getMessage());
        }
    }

    /**
     * Handles the quit button click, returns to the main menu
     *
     * @param event The action event
     */
    @FXML
    public void handleQuit(ActionEvent event) {
        log.info("User is quitting the singleplayer game");

        try {
            log.debug("Loading main menu view");
            FXMLLoader loader = new FXMLLoader(mainMenuResource.getURL());
            loader.setControllerFactory(applicationContext::getBean);
            Parent mainMenuView = loader.load();

            // Get current stage and scene
            Stage stage = (Stage) player1DeckArea.getScene().getWindow();
            Scene currentScene = stage.getScene();

            // Set background image
            String style = "-fx-background-image: url('" + "/images/background.png" + "');" +
                "-fx-background-size: cover;" +
                "-fx-background-position: center;";
            mainMenuView.setStyle(style);

            // Replace the root of the existing scene instead of creating a new one
            currentScene.setRoot(mainMenuView);
            log.info("Successfully navigated back to main menu");

        } catch (IOException e) {
            log.error("Error navigating back to main menu", e);
        }
    }

    /**
     * Sets up the event handlers for the color picker buttons
     */
    private void initializeColorPicker() {
        log.debug("Initializing color picker buttons in singleplayer game view");

        log.debug("Setting up RED color button click handler");
        redColorButton.setOnMouseClicked(e -> selectColor("RED"));

        log.debug("Setting up BLUE color button click handler");
        blueColorButton.setOnMouseClicked(e -> selectColor("BLUE"));

        log.debug("Setting up GREEN color button click handler");
        greenColorButton.setOnMouseClicked(e -> selectColor("GREEN"));

        log.debug("Setting up YELLOW color button click handler");
        yellowColorButton.setOnMouseClicked(e -> selectColor("YELLOW"));

        // Add hover effects for a better user experience
        log.debug("Setting up hover effects for color buttons");
        String hoverStyle = "-fx-border-color: black; -fx-border-width: 3; -fx-border-radius: 8; -fx-cursor: hand;";

        redColorButton.setOnMouseEntered(e -> 
            redColorButton.setStyle(redColorButton.getStyle() + hoverStyle));
        redColorButton.setOnMouseExited(e -> 
            redColorButton.setStyle(redColorButton.getStyle().replace(hoverStyle, "")));

        blueColorButton.setOnMouseEntered(e -> 
            blueColorButton.setStyle(blueColorButton.getStyle() + hoverStyle));
        blueColorButton.setOnMouseExited(e -> 
            blueColorButton.setStyle(blueColorButton.getStyle().replace(hoverStyle, "")));

        greenColorButton.setOnMouseEntered(e -> 
            greenColorButton.setStyle(greenColorButton.getStyle() + hoverStyle));
        greenColorButton.setOnMouseExited(e -> 
            greenColorButton.setStyle(greenColorButton.getStyle().replace(hoverStyle, "")));

        yellowColorButton.setOnMouseEntered(e -> 
            yellowColorButton.setStyle(yellowColorButton.getStyle() + hoverStyle));
        yellowColorButton.setOnMouseExited(e -> 
            yellowColorButton.setStyle(yellowColorButton.getStyle().replace(hoverStyle, "")));

        log.debug("Color picker buttons initialization complete");
    }

    /**
     * Shows the inline color picker and returns a CompletableFuture that will be completed when a color is selected
     * 
     * @param playerIndex The player index who is playing the wild card
     * @param cardIndex The card index in the player's hand
     * @return A CompletableFuture that will complete with the selected color
     */
    private CompletableFuture<String> showColorPicker(int playerIndex, int cardIndex) {
        log.info("Showing color picker for player {} and card index {}", playerIndex, cardIndex);

        // Store the selected card info for when a color is picked
        // Variables to store selected card information when showing the color picker
        this.colorSelectionFuture = new CompletableFuture<>();
        log.debug("Created new CompletableFuture for color selection");

        // Show the color picker
        log.debug("Making color picker visible");
        colorPickerPane.setVisible(true);
        colorPickerPane.setManaged(true);

        log.debug("Returning color selection future");
        return colorSelectionFuture;
    }

    /**
     * Called when a color is selected from the inline color picker
     * 
     * @param color The selected color
     */
    private void selectColor(String color) {
        log.info("Player selected color: {}", color);

        // Hide the color picker
        log.debug("Hiding color picker");
        colorPickerPane.setVisible(false);
        colorPickerPane.setManaged(false);

        // Complete the future with the selected color
        log.debug("Completing color selection future with color: {}", color);
        if (colorSelectionFuture != null && !colorSelectionFuture.isDone()) {
            colorSelectionFuture.complete(color);
            log.debug("Color selection future completed successfully");
        } else {
            log.warn("Color selection future is null or already completed");
            if (colorSelectionFuture == null) {
                log.debug("Color selection future is null");
            } else if (colorSelectionFuture.isDone()) {
                log.debug("Color selection future is already completed");
            }
        }

        log.debug("Color selection process complete");
    }

    /**
     * Displays the initial game state
     * 
     * @param gameState The game state from backend
     */
    private void displayState(GameStateDTO gameState) {
        log.debug("Displaying game state");
        // Get current player number
        int currentPlayerNo = gameState.getCurrentPlayerNo();
        log.debug("Current turn player number: {}", currentPlayerNo);

        // Reset all player name styles first
        resetPlayerNameStyles();

        //displaying player decks
        List<PlayerDTO> players = gameState.getPlayers();

        if (players != null && !players.isEmpty()) {
            int playerCount = Math.min(players.size(), 4);

            for (int i = 0; i < playerCount; i++) {
                PlayerDTO player = players.get(i);

                switch (i) {
                    case 0: // Bottom player
                        player1Name.setText(player.getUsername() + " (You)");
                        displayHorizontalPlayerCards(0, player, player1DeckArea);
                        stylePlayerName(player1Name, i == currentPlayerNo);
                        updateUnoIndicator(player1UnoIndicator, player.getDeck().size());
                        break;
                    case 1: // Left player
                        player2Name.setText(player.getUsername());
                        displayVerticalPlayerCards(1, player, player2DeckArea);
                        stylePlayerName(player2Name, i == currentPlayerNo);
                        updateUnoIndicator(player2UnoIndicator, player.getDeck().size());
                        break;
                    case 2: // Top player
                        player3Name.setText(player.getUsername());
                        displayHorizontalPlayerCards(2, player, player3DeckArea);
                        stylePlayerName(player3Name, i == currentPlayerNo);
                        updateUnoIndicator(player3UnoIndicator, player.getDeck().size());
                        break;
                    case 3: // Right player
                        player4Name.setText(player.getUsername());
                        displayVerticalPlayerCards(3, player, player4DeckArea);
                        stylePlayerName(player4Name, i == currentPlayerNo);
                        updateUnoIndicator(player4UnoIndicator, player.getDeck().size());
                        break;
                }
            }

            // Hide unused player areas
            hideUnusedPlayerAreas(playerCount);
        }

        // Display last played card
        CardDTO lastPlayedCard = gameState.getLastPlayedCard();
        if (lastPlayedCard != null && lastPlayedCardArea != null) {
            ImageView lastCardView = createCardImageView(-1, lastPlayedCard, -1);
            lastCardView.setFitHeight(160);
            lastCardView.setFitWidth(120);
            lastPlayedCardArea.getChildren().add(lastCardView);
        }

        // Display draw deck
        if (drawDeckArea != null) {
            ImageView drawDeckView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cards/BACK.png"))));
            drawDeckView.setFitHeight(160);
            drawDeckView.setFitWidth(120);
            drawDeckArea.getChildren().add(drawDeckView);

            drawDeckArea.setOnMouseClicked(event -> {
                log.info("Player is drawing a card");
                gameService.drawCardHuman(0);
            });
        }

        // Display game information
        if (currentColorArea != null && gameState.getCurrentColor() != null) {
            styleColorArea(currentColorArea, gameState.getCurrentColor());
        }

        if (directionArea != null && gameState.getDirection() != null) {
            styleDirectionArea(directionArea, gameState.getDirection());
        }

        // Always ensure the color picker is hidden when refreshing the game state
        if (colorPickerPane != null) {
            colorPickerPane.setVisible(false);
            colorPickerPane.setManaged(false);
        }
    }

    /**
     * Resets all player name styles to the default
     */
    private void resetPlayerNameStyles() {
        // Set base style for all player names (larger font)
        String baseStyle = "-fx-font-size: 16px;";

        if (player1Name != null) player1Name.setStyle(baseStyle);
        if (player2Name != null) player2Name.setStyle(baseStyle);
        if (player3Name != null) player3Name.setStyle(baseStyle);
        if (player4Name != null) player4Name.setStyle(baseStyle);
    }

    /**
     * Styles a player's name label, highlighting it if it's the current player's turn
     * 
     * @param nameLabel The player's name label
     * @param isCurrentTurn Whether it's this player's turn
     */
    private void stylePlayerName(Label nameLabel, boolean isCurrentTurn) {
        if (nameLabel == null) return;

        // Base style with larger font
        StringBuilder style = new StringBuilder("-fx-font-size: 20px; -fx-text-fill: black;");

        // Add highlight styling for current turn
        if (isCurrentTurn) {
            style.append(" -fx-font-weight: bold;")
                 .append(" -fx-text-fill: bold;")
                 .append(" -fx-background-color: yellow;")
                 .append(" -fx-background-radius: 4px;")
                 .append(" -fx-padding: 2px 8px;");
        }

        nameLabel.setStyle(style.toString());
    }

    /**
     * Displays cards for a player in a horizontal container (for top/bottom players)
     * with cards partially stacked on top of each other
     * 
     * @param playerIndex The index of the player
     * @param player The player whose cards to display
     * @param container The UI container to display cards in
     */
    private void displayHorizontalPlayerCards(int playerIndex, PlayerDTO player, HBox container) {
        if (container == null || player == null) return;

        List<CardDTO> cards = player.getDeck();
        if (cards == null) return;

        // Clear previous cards
        container.getChildren().clear();

        // Calculate dynamic spacing based on number of cards, more cards-> more overlap
        int cardCount = cards.size();
        double spacing = calculateDynamicHorizontalSpacing(cardCount);

        // Set up container properties for stacked layout with dynamic sizing
        container.setSpacing(spacing);
        container.setAlignment(javafx.geometry.Pos.CENTER);

        for (int i = 0; i < cards.size(); i++) {
            CardImageView cardView = createCardImageView(playerIndex, cards.get(i), i);
            cardView.setFitHeight(120);
            cardView.setFitWidth(80);
            container.getChildren().add(cardView);
        }
    }

    /**
     * Calculates appropriate card spacing for horizontal layout based on card count
     * 
     * @param cardCount Number of cards in the deck
     * @return Appropriate spacing value (negative for overlap)
     */
    private double calculateDynamicHorizontalSpacing(int cardCount) {
        if (cardCount <= 5) {
            return 5; // Small number of cards, show them fully
        } else if (cardCount <= 10) {
            return -20; // Medium number of cards, moderate overlap
        } else if (cardCount <= 15) {
            return -40; // Large number of cards, significant overlap
        } else {
            return -50; // Very large number of cards, maximum overlap
        }
    }

    /**
     * Displays cards for a player in a vertical container (for left/right players)
     * with cards partially stacked on top of each other
     * 
     * @param playerIndex The index of the player
     * @param player The player whose cards to display
     * @param container The UI container to display cards in
     */
    private void displayVerticalPlayerCards(int playerIndex, PlayerDTO player, VBox container) {
        if (container == null || player == null) return;

        List<CardDTO> cards = player.getDeck();
        if (cards == null) return;

        // Clear previous cards
        container.getChildren().clear();

        // Calculate dynamic spacing based on number of cards
        int cardCount = cards.size();
        double spacing = calculateDynamicVerticalSpacing(cardCount);

        // Set up container properties for dynamic display
        container.setSpacing(spacing);
        container.setAlignment(javafx.geometry.Pos.CENTER);

        for (int i = 0; i < cards.size(); i++) {
            CardImageView cardView = createCardImageView(playerIndex, cards.get(i), i);

            cardView.setFitHeight(110);
            cardView.setFitWidth(80);
            container.getChildren().add(cardView);
        }
    }

    /**
     * Calculates appropriate card spacing for vertical layout based on card count
     * 
     * @param cardCount Number of cards in the deck
     * @return Appropriate spacing value (negative for overlap)
     */
    private double calculateDynamicVerticalSpacing(int cardCount) {
        if (cardCount <= 3) {
            return 10;           // Very few cards, minimal overlap
        } else if (cardCount <= 6) {
            return -40;          // Few cards, slight overlap
        } else if (cardCount <= 10) {
            return -65;          // Medium number, moderate overlap
        } else if (cardCount <= 15) {
            return -80;          // Large number, significant overlap
        } else {
            return -90;          // Very large number, maximum overlap
        }
    }

    /**
     * Creates an ImageView for a card, showing front side
     * 
     * @param card The card to display
     * @return ImageView of the card
     */
    private CardImageView createCardImageView(int playerIndex, CardDTO card, int cardIndex) {
        CardImageView cardImageView = new CardImageView(card);
        cardImageView.setFitHeight(120);
        cardImageView.setFitWidth(80);

        // Only make cards clickable for human player (index 0)
        if (playerIndex == 0) {
            // Add hover effect
            cardImageView.setOnMouseEntered(e -> cardImageView.setTranslateY(-15));
            cardImageView.setOnMouseExited(e -> cardImageView.setTranslateY(0));

            // Add click handler
            cardImageView.setOnMouseClicked(e -> {
                handleCardClickSingleplayer(playerIndex, cardIndex, card);
            });
        }

        return cardImageView;
    }

    /**
     * Handles card click in single-player mode
     */
    private void handleCardClickSingleplayer(int playerIndex, int cardIndex, CardDTO card) {
        log.info("Player played card: {} {} at index {}", card.getColor(), card.getType(), cardIndex);
        log.debug("Card details - Color: {}, Type: {}", card.getColor(), card.getType());

        if (card.getType().equals("WILD") || card.getType().equals("WILD_DRAW_FOUR") || card.getType().equals("WILD_SKIP_EVERYONE_ONCE")) {
            // For wild cards, show the color picker and wait for color selection
            log.debug("Wild card played, showing color picker");
            log.debug("Waiting for player to select a color for the wild card");
            CompletableFuture<String> colorFuture = showColorPicker(playerIndex, cardIndex);

            log.debug("Setting up callback for when color is selected");
            colorFuture.thenAccept(selectedColor -> {
                log.info("Player selected color: {} for wild card", selectedColor);
                log.debug("Sending play card request to game service with selected color");
                gameService.playCardHuman(playerIndex, cardIndex, selectedColor);
                log.debug("Play card request sent for wild card with color: {}", selectedColor);
            });

            log.debug("Color selection future and callback set up complete");
        } else {
            // For non-wild cards, play directly
            log.debug("Playing regular card directly without color selection");
            log.debug("Sending play card request to game service");
            gameService.playCardHuman(playerIndex, cardIndex, null);
            log.debug("Play card request sent for regular card");
        }
    }

    /**
     * Applies color styling to a label based on the current color
     * 
     * @param stackPane The StackPane to style
     * @param color The color to apply
     */
    private void styleColorArea(StackPane stackPane, String color) {
        String backgroundColor;

        switch (color) {
            case "RED":
                backgroundColor = "red";
                break;
            case "BLUE":
                backgroundColor = "blue";
                break;
            case "GREEN":
                backgroundColor = "green";
                break;
            case "YELLOW":
                backgroundColor = "gold";
                break;
            default:
                backgroundColor = "black";
                break;
        }

        stackPane.setStyle("-fx-background-color: " + backgroundColor + "; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: black; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 10;");
    }

    private void styleDirectionArea(StackPane directionArea, String direction) {
        String imagePath = "/images/" + (direction.equals("CLOCKWISE") ?
            "clockwise.png" : "counter_clockwise.png");

        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        if (imageStream != null) {
            Image directionImage = new Image(imageStream);
            ImageView imageView = new ImageView(directionImage);

            // Size the image appropriately
            imageView.setFitHeight(80);
            imageView.setFitWidth(80);
            imageView.setPreserveRatio(true);

            directionArea.getChildren().add(imageView);

            directionArea.setStyle("-fx-background-color: transparent;");
        }
    }

    /**
     * Hides unused player areas based on number of players
     * 
     * @param playerCount The number of players in the game
     */
    private void hideUnusedPlayerAreas(int playerCount) {
        // Show or hide player areas based on player count
        if (player2DeckArea != null && player2DeckArea.getParent() != null) {
            player2DeckArea.getParent().setVisible(playerCount > 1);
            player2DeckArea.getParent().setManaged(playerCount > 1);
        }
        if (player2Name != null) {
            player2Name.setVisible(playerCount > 1);
            player2Name.setManaged(playerCount > 1);
        }

        if (player3DeckArea != null && player3DeckArea.getParent() != null) {
            player3DeckArea.getParent().setVisible(playerCount > 2);
            player3DeckArea.getParent().setManaged(playerCount > 2);
        }
        if (player3Name != null) {
            player3Name.setVisible(playerCount > 2);
            player3Name.setManaged(playerCount > 2);
        }

        if (player4DeckArea != null && player4DeckArea.getParent() != null) {
            player4DeckArea.getParent().setVisible(playerCount > 3);
            player4DeckArea.getParent().setManaged(playerCount > 3);
        }
        if (player4Name != null) {
            player4Name.setVisible(playerCount > 3);
            player4Name.setManaged(playerCount > 3);
        }
    }

    /**
     * Displays an error alert with the specified message
     * 
     * @param message The error message to display
     */
    private void showErrorAlert(String message) {
        log.error("Showing error alert: {}", message);
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Connection Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Refreshes the UI with updated game state
     * This method is called when the game state changes, such as after playing a card
     * 
     * @param gameState The updated game state from the backend
     */
    public void refreshUI(GameStateDTO gameState) {
        log.info("Refreshing UI with updated game state");

        if (gameState == null) {
            log.warn("Attempted to refresh UI with null game state");
            return;
        }

        log.debug("Game state details - Current player: {}, Direction: {}, Current color: {}", 
                gameState.getCurrentPlayerNo(), gameState.getDirection(), gameState.getCurrentColor());

        if (gameState.getPlayers() != null) {
            log.debug("Number of players in game: {}", gameState.getPlayers().size());
            for (int i = 0; i < gameState.getPlayers().size(); i++) {
                PlayerDTO player = gameState.getPlayers().get(i);
                log.debug("Player {} ({}) has {} cards", 
                        i, player.getUsername(), player.getDeck() != null ? player.getDeck().size() : 0);
            }
        }

        log.debug("Scheduling UI update on JavaFX Application Thread");
        Platform.runLater(() -> {
            log.debug("Clearing previous card displays");
            clearCardDisplays();

            log.debug("Displaying updated game state");
            displayState(gameState);

            // Check if the game is finished
            if (gameState.isGameFinished()) {
                log.info("Game finished. Winner: {}", gameState.getWinner());
                log.debug("Showing game end popup for winner: {}", gameState.getWinner());
                showGameEndPopup(gameState.getWinner());
                return;
            } else {
                log.debug("Game is still in progress");
            }

            if (gameState.getCurrentPlayerNo() != 0) {
                log.debug("Current player is computer (player {})", gameState.getCurrentPlayerNo());
                log.debug("Scheduling computer player {} turn in 2 seconds", gameState.getCurrentPlayerNo());
                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            log.debug("Computer player timer triggered, executing turn on JavaFX thread");
                            Platform.runLater(() -> {
                                log.info("Computer player {} is taking their turn", gameState.getCurrentPlayerNo());
                                gameService.playCardComputer(gameState.getCurrentPlayerNo());
                                log.debug("Computer player turn executed");
                            });
                        }
                    },
                    2000
                );
            } else {
                log.debug("Current player is human (player 0), waiting for user input");
            }

            log.debug("UI refresh complete");
        });
    }

    /**
     * Shows a popup when the game is finished
     * 
     * @param winner The username of the winning player
     */
    private void showGameEndPopup(String winner) {
        log.info("Showing game end popup. Winner: {}", winner);
        // Create a custom alert
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Game Over!");
        alert.setContentText(winner + " has won the game!");

        // Get the dialog pane and style it
        DialogPane dialogPane = alert.getDialogPane();

        // Set size
        dialogPane.setPrefWidth(450);
        dialogPane.setPrefHeight(230);

        // Apply custom styling to the dialog
        dialogPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4b6cb7, #182848); " +
                           "-fx-background-radius: 15px; " +
                           "-fx-border-radius: 15px; " +
                           "-fx-border-color: #ffd700; " +  // Gold border
                           "-fx-border-width: 3px; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 15, 0, 0, 0);");

        // Style the header text
        Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
        if (headerLabel != null) {
            headerLabel.setStyle("-fx-font-size: 28px; " +
                              "-fx-font-weight: bold; " +
                              "-fx-text-fill: #ffd700; " +  // Gold text
                              "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 0, 0); " +
                              "-fx-padding: 10px 0 5px 0;");
        }

        // Style the content text
        Label contentLabel = (Label) dialogPane.lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setStyle("-fx-font-size: 18px; " +
                               "-fx-text-fill: white; " +
                               "-fx-padding: 10px 5px;");

            contentLabel.setText("🏆 " + winner + " 🏆 has won the game!");
        }

        // Style the OK button
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Return to Menu");
        okButton.setStyle("-fx-background-color: linear-gradient(to bottom, #ff4b1f, #ff9068); " +
                         "-fx-text-fill: white; " +
                         "-fx-font-weight: bold; " +
                         "-fx-font-size: 16px; " +
                         "-fx-padding: 10px 25px; " +
                         "-fx-background-radius: 20px; " +
                         "-fx-cursor: hand; " +
                         "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 0, 0);");

        // Add hover effect for button
        okButton.setOnMouseEntered(e -> 
            okButton.setStyle(okButton.getStyle().replace("linear-gradient(to bottom, #ff4b1f, #ff9068)", 
                                                          "linear-gradient(to bottom, #ff9068, #ff4b1f)")));
        okButton.setOnMouseExited(e -> 
            okButton.setStyle(okButton.getStyle().replace("linear-gradient(to bottom, #ff9068, #ff4b1f)", 
                                                         "linear-gradient(to bottom, #ff4b1f, #ff9068)")));

        // Add a container for button to center it
        HBox buttonBox = (HBox) dialogPane.lookup(".button-bar .container");
        if (buttonBox != null) {
            buttonBox.setStyle("-fx-alignment: center; -fx-padding: 10px 0 15px 0;");
        }

        // Set owner to main window
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.initOwner(player1DeckArea.getScene().getWindow());
        
        // Replace the standard button action with our own to ensure clean popup closing
        okButton.setOnAction(event -> {
            log.debug("Return to menu button clicked in game end popup");
            
            // First close the dialog
            alert.close();
            
            // Then execute navigation to main menu in a separate thread
            // to avoid interference with dialog closing
            new Thread(() -> {
                // Navigate to main menu on the JavaFX thread
                Platform.runLater(() -> handleQuit(new ActionEvent()));
            }).start();
        });

        alert.show(); // Use show() instead of showAndWait() to avoid blocking
    }

    /**
     * Clears all card displays to prepare for refreshed state
     */
    private void clearCardDisplays() {
        if (player1DeckArea != null) player1DeckArea.getChildren().clear();
        if (player2DeckArea != null) player2DeckArea.getChildren().clear();
        if (player3DeckArea != null) player3DeckArea.getChildren().clear();
        if (player4DeckArea != null) player4DeckArea.getChildren().clear();
        if (lastPlayedCardArea != null) lastPlayedCardArea.getChildren().clear();
        if (drawDeckArea != null) drawDeckArea.getChildren().clear();
        if (currentColorArea != null) currentColorArea.getChildren().clear();
        if (directionArea != null) directionArea.getChildren().clear();
    }

    /**
     * Sets up the event handlers for the cheat action buttons
     */
    private void initializeCheatButtons() {
        if (reverseButton != null) {
            reverseButton.setOnAction(e -> {
                log.info("Player used cheat: REVERSE card");
                gameService.useCheatCard("REVERSE");
            });

            // Add hover effect
            String reverseHoverStyle = "-fx-background-color: #ff7733; -fx-cursor: hand;";
            reverseButton.setOnMouseEntered(e -> reverseButton.setStyle(reverseButton.getStyle() + reverseHoverStyle));
            reverseButton.setOnMouseExited(e -> reverseButton.setStyle(reverseButton.getStyle().replace(reverseHoverStyle, "")));
        }

        if (skipButton != null) {
            skipButton.setOnAction(e -> {
                log.info("Player used cheat: SKIP card");
                gameService.useCheatCard("SKIP");
            });

            // Add hover effect
            String skipHoverStyle = "-fx-background-color: #3366ff; -fx-cursor: hand;";
            skipButton.setOnMouseEntered(e -> skipButton.setStyle(skipButton.getStyle() + skipHoverStyle));
            skipButton.setOnMouseExited(e -> skipButton.setStyle(skipButton.getStyle().replace(skipHoverStyle, "")));
        }

        if (drawTwoButton != null) {
            drawTwoButton.setOnAction(e -> {
                log.info("Player used cheat: DRAW_TWO card");
                gameService.useCheatCard("DRAW_TWO");
            });

            // Add hover effect
            String drawTwoHoverStyle = "-fx-background-color: #33aa33; -fx-cursor: hand;";
            drawTwoButton.setOnMouseEntered(e -> drawTwoButton.setStyle(drawTwoButton.getStyle() + drawTwoHoverStyle));
            drawTwoButton.setOnMouseExited(e -> drawTwoButton.setStyle(drawTwoButton.getStyle().replace(drawTwoHoverStyle, "")));
        }

        if (wildButton != null) {
            wildButton.setOnAction(e -> {
                log.info("Player used cheat: WILD card");
                gameService.useCheatCard("WILD");
            });

            // Add hover effect
            String wildHoverStyle = "-fx-background-color: #7744aa; -fx-cursor: hand;";
            wildButton.setOnMouseEntered(e -> wildButton.setStyle(wildButton.getStyle() + wildHoverStyle));
            wildButton.setOnMouseExited(e -> wildButton.setStyle(wildButton.getStyle().replace(wildHoverStyle, "")));
        }

        if (wildDrawFourButton != null) {
            wildDrawFourButton.setOnAction(e -> {
                log.info("Player used cheat: WILD_DRAW_FOUR card");
                gameService.useCheatCard("WILD_DRAW_FOUR");
            });

            // Add hover effect
            String wildDrawFourHoverStyle = "-fx-background-color: #aa4477; -fx-cursor: hand;";
            wildDrawFourButton.setOnMouseEntered(e -> wildDrawFourButton.setStyle(wildDrawFourButton.getStyle() + wildDrawFourHoverStyle));
            wildDrawFourButton.setOnMouseExited(e -> wildDrawFourButton.setStyle(wildDrawFourButton.getStyle().replace(wildDrawFourHoverStyle, "")));
        }
        
        if (skipEveryoneOnceButton != null) {
            skipEveryoneOnceButton.setOnAction(e -> {
                log.info("Player used cheat: WILD_SKIP_EVERYONE_ONCE card");
                gameService.useCheatCard("WILD_SKIP_EVERYONE_ONCE");
            });
        
            // Add hover effect
            String skipEveryoneOnceHoverStyle = "-fx-background-color: #22aacc; -fx-cursor: hand;";
            skipEveryoneOnceButton.setOnMouseEntered(e -> skipEveryoneOnceButton.setStyle(skipEveryoneOnceButton.getStyle() + skipEveryoneOnceHoverStyle));
            skipEveryoneOnceButton.setOnMouseExited(e -> skipEveryoneOnceButton.setStyle(skipEveryoneOnceButton.getStyle().replace(skipEveryoneOnceHoverStyle, "")));
        }
    }

    /**
     * Updates the UNO indicator for a player based on their card count
     * 
     * @param unoIndicator The UNO indicator label
     * @param cardCount The number of cards the player has
     */
    private void updateUnoIndicator(Label unoIndicator, int cardCount) {
        if (unoIndicator == null) return;

        // Always make the UNO indicator visible
        unoIndicator.setVisible(true);
        unoIndicator.setManaged(true);

        if (cardCount == 1) {
            log.info("Player has UNO! Card count: 1");
            // Highlight the UNO indicator when player has one card
            unoIndicator.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;" +
                                 "-fx-background-color: #ff3333; -fx-background-radius: 5;" +
                                 "-fx-padding: 2 8 2 8;");
        } else {
            // Gray out the UNO indicator when player has more than one card
            unoIndicator.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #888888;" +
                                 "-fx-background-color: #eeeeee; -fx-background-radius: 5;" +
                                 "-fx-padding: 2 8 2 8;");
        }
        log.debug("Updated UNO indicator for player with {} cards", cardCount);
    }
}
