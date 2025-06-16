package tr.edu.metu.ceng.uno.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import tr.edu.metu.ceng.uno.model.CardDTO;
import tr.edu.metu.ceng.uno.model.GameStateDTO;
import tr.edu.metu.ceng.uno.model.PlayerDTO;
import tr.edu.metu.ceng.uno.service.AuthService;
import tr.edu.metu.ceng.uno.service.GameService;
import tr.edu.metu.ceng.uno.service.MultiplayerService;
import tr.edu.metu.ceng.uno.view.CardImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class MultiplayerGameViewController {

    private final GameService gameService;
    private final MultiplayerService multiplayerService;
    private final ApplicationContext applicationContext;
    private final AuthService authService;

    @Value("classpath:/menu-view.fxml")
    private Resource mainMenuResource;

    private String multiplayerGameId;
    private String currentUsername;
    // Index of current player in the players list
    private int currentPlayerIndex = -1;
    // Index of opponent player in the players list
    private int opponentPlayerIndex = -1;

    // FXML injected UI components
    @FXML private Label bottomPlayerName;
    @FXML private Label topPlayerName;

    // UNO indicators for each player
    @FXML private Label bottomPlayerUnoIndicator;
    @FXML private Label topPlayerUnoIndicator;

    @FXML private HBox bottomPlayerDeckArea;
    @FXML private HBox topPlayerDeckArea;

    @FXML private StackPane lastPlayedCardArea;
    @FXML private StackPane drawDeckArea;
    @FXML private StackPane currentColorArea;
    @FXML private StackPane directionArea;
    @FXML private Button challengeButton;

    // Flag to track if a wild draw four card has been played that can be challenged
    private boolean canChallenge = false;
    
    @FXML private Button rejectButton;

    // Color picker components
    @FXML private GridPane colorPickerPane;
    @FXML private StackPane redColorButton;
    @FXML private StackPane blueColorButton;
    @FXML private StackPane greenColorButton;
    @FXML private StackPane yellowColorButton;

    private CompletableFuture<String> colorSelectionFuture;

    public MultiplayerGameViewController(GameService gameService, 
                                         MultiplayerService multiplayerService, 
                                         ApplicationContext applicationContext,
                                         AuthService authService) {
        log.info("Creating MultiplayerGameViewController instance");
        this.gameService = gameService;
        this.multiplayerService = multiplayerService;
        this.applicationContext = applicationContext;
        this.authService = authService;
        log.debug("MultiplayerGameViewController dependencies injected: gameService, multiplayerService, applicationContext, authService");
    }

    /**
     * Initialize method called by JavaFX after FXML is loaded
     */
    @FXML
    public void initialize() {
        // Get the current user's username
        this.currentUsername = authService.getCurrentUsername();
        log.info("Initializing multiplayer game view for user: {}", currentUsername);

        // Initialize color picker buttons
        log.debug("Initializing color picker buttons");
        initializeColorPicker();

        try {
            // Get the game ID from the multiplayer service
            this.multiplayerGameId = multiplayerService.getGameId();
            log.info("Connected to multiplayer game with ID: {}", multiplayerGameId);

            // Set up game state handler to refresh UI when game state changes
            log.debug("Setting up game state handler");
            multiplayerService.setGameStateHandler(this::refreshUI);

            // Get initial game state
            log.debug("Fetching initial game state");
            GameStateDTO gameState = multiplayerService.getGameState();
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
        log.info("User {} is quitting the multiplayer game", currentUsername);

        // Leave the game before navigating to the main menu
        multiplayerService.leaveGame();
        log.debug("Player {} left the multiplayer game with ID: {}", currentUsername, multiplayerGameId);

        try {
            log.debug("Loading main menu view");
            FXMLLoader loader = new FXMLLoader(mainMenuResource.getURL());
            loader.setControllerFactory(applicationContext::getBean);
            Parent mainMenuView = loader.load();

            Scene currentScene = null;
            Stage stage = null;

            //checking whether bottomPlayerDeckArea is still in a scene
            if (bottomPlayerDeckArea != null && bottomPlayerDeckArea.getScene() != null) {
                stage = (Stage) bottomPlayerDeckArea.getScene().getWindow();
                currentScene = stage.getScene();
            }

            if (stage != null && currentScene != null) {
                // Set background image
                String style = "-fx-background-image: url('" + "/images/background.png" + "');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center;";
                mainMenuView.setStyle(style);

                //instead of creating a new one, replacing root of current
                currentScene.setRoot(mainMenuView);
                log.info("Successfully navigated back to main menu");
            } else {
                log.warn("Could not navigate to main menu - scene or stage is null");
            }


        } catch (IOException e) {
            log.error("Error navigating back to main menu", e);
        }
    }

    /**
     * Sets up the event handlers for the color picker buttons
     */
    private void initializeColorPicker() {
        log.debug("Initializing color picker buttons in multiplayer game view");

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
        log.debug("Color selected: {}", color);
        // Hide the color picker
        colorPickerPane.setVisible(false);
        colorPickerPane.setManaged(false);

        // Complete the future with the selected color
        if (colorSelectionFuture != null && !colorSelectionFuture.isDone()) {
            colorSelectionFuture.complete(color);
        } else {
            log.warn("Color selection future is null or already completed");
        }
    }

    /**
     * Displays the initial game state
     * 
     * @param gameState The game state from backend
     */
    private void displayState(GameStateDTO gameState) {
        log.debug("Displaying game state");
        // Identify current player and opponent
        identifyPlayers(gameState.getPlayers());

        // Get current player number (whose turn it is)
        int currentTurnPlayerNo = gameState.getCurrentPlayerNo();
        log.debug("Current turn player number: {}", currentTurnPlayerNo);

        // Reset all player name styles first
        resetPlayerNameStyles();

        // Displaying player decks
        List<PlayerDTO> players = gameState.getPlayers();

        if (players != null && players.size() >= 2) {
            // Bottom player is always the current user
            PlayerDTO currentPlayer = players.get(currentPlayerIndex);
            bottomPlayerName.setText(currentPlayer.getUsername() + " (You)");
            displayHorizontalPlayerCards(currentPlayerIndex, currentPlayer, bottomPlayerDeckArea);
            stylePlayerName(bottomPlayerName, currentPlayerIndex == currentTurnPlayerNo);
            updateUnoIndicator(bottomPlayerUnoIndicator, currentPlayer.getDeck().size());

            // Top player is the opponent
            PlayerDTO opponentPlayer = players.get(opponentPlayerIndex);
            topPlayerName.setText(opponentPlayer.getUsername());
            displayHorizontalPlayerCards(opponentPlayerIndex, opponentPlayer, topPlayerDeckArea);
            stylePlayerName(topPlayerName, opponentPlayerIndex == currentTurnPlayerNo);
            updateUnoIndicator(topPlayerUnoIndicator, opponentPlayer.getDeck().size());
        }

        if(gameState.isCanChallenge() && gameState.getPlayers().get(gameState.getCurrentPlayerNo()).getUsername().equals(currentUsername)) {
            //activate challenge button for current user
            Platform.runLater(() -> {
                updateChallengeButton(true);
            });
        }
        else {
            // Disable challenge button if no active challenge
            Platform.runLater(this::disableChallengeButton);
            Platform.runLater(this::disableRejectButton);
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
                log.info("Player {} is drawing a card", currentUsername);
                multiplayerService.drawCard(currentPlayerIndex);
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
     * Identifies the current player and the opponent based on username
     * 
     * @param players List of players in the game
     */
    private void identifyPlayers(List<PlayerDTO> players) {
        log.info("Identifying players in multiplayer game");

        if (players == null || players.isEmpty()) {
            log.warn("Player list is null or empty");
            return;
        }

        log.debug("Total players in game: {}", players.size());
        log.debug("Current username: {}", currentUsername);

        for (int i = 0; i < players.size(); i++) {
            String playerUsername = players.get(i).getUsername();
            log.debug("Checking player at index {}: {}", i, playerUsername);

            if (playerUsername.equals(currentUsername)) {
                log.debug("Found current player at index {}", i);
                currentPlayerIndex = i;
            } else {
                log.debug("Found opponent player at index {}", i);
                opponentPlayerIndex = i;
            }
        }

        log.info("Identified players - Current player index: {}, Opponent player index: {}", 
                 currentPlayerIndex, opponentPlayerIndex);

        if (currentPlayerIndex == -1) {
            log.warn("Could not find current player in the player list!");
        }

        if (opponentPlayerIndex == -1) {
            log.warn("Could not find opponent player in the player list!");
        }
    }

    /**
     * Resets all player name styles to the default
     */
    private void resetPlayerNameStyles() {
        // Set base style for all player names (larger font)
        String baseStyle = "-fx-font-size: 16px;";

        if (bottomPlayerName != null) bottomPlayerName.setStyle(baseStyle);
        if (topPlayerName != null) topPlayerName.setStyle(baseStyle);
    }

    /**
     * Styles a player's name label, highlighting it if it's the current player's turn
     * 
     * @param nameLabel The player's name label
     * @param isCurrentTurn Whether it's this player's turn
     */
    private void stylePlayerName(Label nameLabel, boolean isCurrentTurn) {
        log.debug("Styling player name label: {}, isCurrentTurn: {}", nameLabel.getText(), isCurrentTurn);

        if (nameLabel == null) {
            log.warn("Name label is null, cannot style");
            return;
        }

        // Base style with larger font
        StringBuilder style = new StringBuilder("-fx-font-size: 20px; -fx-text-fill: black;");
        log.debug("Applied base style to player name");

        // Add highlight styling for current turn
        if (isCurrentTurn) {
            log.debug("Applying current turn highlight style to player name");
            style.append(" -fx-font-weight: bold;")
                 .append(" -fx-text-fill: bold;")
                 .append(" -fx-background-color: yellow;")
                 .append(" -fx-background-radius: 4px;")
                 .append(" -fx-padding: 2px 8px;");
        }

        String finalStyle = style.toString();
        log.debug("Final style for player name: {}", finalStyle);
        nameLabel.setStyle(finalStyle);
    }

    /**
     * Displays cards for a player in a horizontal container with cards partially stacked
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

            // If this is the opponent's card, rotate it 180 degrees
            if (playerIndex == opponentPlayerIndex) {
                cardView.setRotate(180);
            }

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
     * Creates an ImageView for a card, showing front side for current player's cards and back for opponent's
     * 
     * @param playerIndex The index of the player
     * @param card The card to display
     * @param cardIndex The index of the card in the player's hand
     * @return ImageView of the card
     */
    private CardImageView createCardImageView(int playerIndex, CardDTO card, int cardIndex) {
        CardImageView cardImageView;

        // If this is the opponent's card and not the last played card, show the back side
        if (playerIndex == opponentPlayerIndex) {
            // Show card backs for opponent's cards
            cardImageView = new CardImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cards/BACK.png"))));
            cardImageView.setCardData(card); // Still store the actual card data for potential future use
        } else {
            // Normal card display for current player or last played card
            cardImageView = new CardImageView(card);
        }

        cardImageView.setFitHeight(120);
        cardImageView.setFitWidth(80);

        // Only make cards clickable for current player
        if (playerIndex == currentPlayerIndex) {
            // Add hover effect
            cardImageView.setOnMouseEntered(e -> cardImageView.setTranslateY(-15));
            cardImageView.setOnMouseExited(e -> cardImageView.setTranslateY(0));

            // Add click handler
            cardImageView.setOnMouseClicked(e -> {
                handleCardClick(playerIndex, cardIndex, card);
            });
        } else if (playerIndex == opponentPlayerIndex) {
            // Disable clicking on opponent's cards
            cardImageView.setDisable(true);
        }

        return cardImageView;
    }

    /**
     * Handles card click in multiplayer mode
     */
    private void handleCardClick(int playerIndex, int cardIndex, CardDTO card) {
        log.info("Player {} played card: {} {} at index {}", 
                currentUsername, card.getColor(), card.getType(), cardIndex);
        log.debug("Card details - Color: {}, Type: {}", 
                card.getColor(), card.getType());

        if (card.getType().equals("WILD") || card.getType().equals("WILD_DRAW_FOUR") || card.getType().equals("WILD_SKIP_EVERYONE_ONCE")) {
            // For wild cards, show the color picker and wait for color selection
            log.debug("Wild card played, showing color picker");
            log.debug("Waiting for player to select a color for the wild card");
            CompletableFuture<String> colorFuture = showColorPicker(playerIndex, cardIndex);

            log.debug("Setting up callback for when color is selected");
            colorFuture.thenAccept(selectedColor -> {
                log.info("Player {} selected color: {} for wild card", currentUsername, selectedColor);
                log.debug("Sending play card request to multiplayer service with selected color");
                multiplayerService.playCard(playerIndex, cardIndex, selectedColor);
            });

            log.debug("Color selection future and callback set up complete");
        } else {
            // For non-wild cards, play directly
            log.debug("Playing regular card directly without color selection");
            log.debug("Sending play card request to multiplayer service");
            multiplayerService.playCard(playerIndex, cardIndex, null);
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

        // Check if this is a player quit notification
        if (gameState.getQuittingPlayerName() != null) {
            log.info("Received notification that player {} has quit the game", gameState.getQuittingPlayerName());
            Platform.runLater(() -> {
                showPlayerQuitPopup(gameState.getQuittingPlayerName());
            });
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
            } else {
                log.debug("Game is still in progress");
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
        alertStage.initOwner(bottomPlayerDeckArea.getScene().getWindow());

        // Show and handle result
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                log.debug("Return to menu button clicked in game end popup");

                multiplayerService.leaveGame(); // leave the game first
                log.debug("Successfully left the game with ID: {}", multiplayerGameId);

                handleQuit(new ActionEvent());
            }
        });
    }

    /**
     * Shows a popup when another player quits the game
     * 
     * @param quittingPlayer The username of the player who quit
     */
    private void showPlayerQuitPopup(String quittingPlayer) {
        log.info("Showing player quit popup. Player who quit: {}", quittingPlayer);
        // Create a custom alert
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Player Left Game");
        alert.setHeaderText("Player Left Game");
        alert.setContentText(quittingPlayer + " has quit the game!");

        // Get the dialog pane and style it
        DialogPane dialogPane = alert.getDialogPane();

        // Set size
        dialogPane.setPrefWidth(450);
        dialogPane.setPrefHeight(230);

        // Apply custom styling to the dialog
        dialogPane.setStyle("-fx-background-color: linear-gradient(to bottom, #4b6cb7, #182848); " +
                           "-fx-background-radius: 15px; " +
                           "-fx-border-radius: 15px; " +
                           "-fx-border-color: #ff3333; " +  // Red border instead of gold
                           "-fx-border-width: 3px; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 15, 0, 0, 0);");

        // Style the header text
        Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
        if (headerLabel != null) {
            headerLabel.setStyle("-fx-font-size: 28px; " +
                              "-fx-font-weight: bold; " +
                              "-fx-text-fill: #ff3333; " +  // Red text instead of gold
                              "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 0, 0); " +
                              "-fx-padding: 10px 0 5px 0;");
        }

        // Style the content text
        Label contentLabel = (Label) dialogPane.lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setStyle("-fx-font-size: 18px; " +
                               "-fx-text-fill: white; " +
                               "-fx-padding: 10px 5px;");

            contentLabel.setText("⚠️ " + quittingPlayer + " has quit the game! ⚠️");
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
        alertStage.initOwner(bottomPlayerDeckArea.getScene().getWindow());

        // Show and handle result
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                log.debug("Return to menu button clicked in player quit popup");
                Platform.runLater(()-> handleQuit(new ActionEvent()));
            }
        });
    }

    /**
     * Clears all card displays to prepare for refreshed state
     */
    private void clearCardDisplays() {
        if (bottomPlayerDeckArea != null) bottomPlayerDeckArea.getChildren().clear();
        if (topPlayerDeckArea != null) topPlayerDeckArea.getChildren().clear();
        if (lastPlayedCardArea != null) lastPlayedCardArea.getChildren().clear();
        if (drawDeckArea != null) drawDeckArea.getChildren().clear();
        if (currentColorArea != null) currentColorArea.getChildren().clear();
        if (directionArea != null) directionArea.getChildren().clear();
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
    
    /**
     * Handles the challenge button click
     * Only enabled when a WILD_DRAW_FOUR has been played against the player
     * 
     * @param event The action event
     */
    @FXML
    public void handleChallenge(ActionEvent event) {
        log.info("Player {} is challenging the Wild Draw Four card", currentUsername);
        
        if (!canChallenge) {
            log.warn("Challenge button was clicked but challenging is not currently allowed");
            return;
        }
        
        // Call the multiplayer service to send the challenge
        multiplayerService.challengeWildDrawFour(currentPlayerIndex);
        
        // Disable the button after it's clicked
        disableChallengeButton();
    }
    
    /**
     * Updates the challenge button state based on the current game state
     * 
     * @param canChallengeNow Whether challenging is allowed
     */
    private void updateChallengeButton(boolean canChallengeNow) {
        this.canChallenge = canChallengeNow;
        
        if (challengeButton == null || rejectButton == null) {
            log.warn("Challenge or reject button is null, cannot update state");
            return;
        }
        
        if (canChallengeNow) {
            log.info("Enabling challenge and reject buttons because a Wild Draw Four was played against the current player");
            
            // Enable the challenge button and change its appearance
            challengeButton.setDisable(false);
            challengeButton.setStyle("-fx-background-color: linear-gradient(to bottom, #ffcc00, #ff9900);" +
                                    "-fx-text-fill: black;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-font-size: 12px;" +
                                    "-fx-padding: 5px 10px;" +
                                    "-fx-background-radius: 15;");
            
            // Add hover effect for challenge button
            challengeButton.setOnMouseEntered(e -> 
                challengeButton.setStyle(challengeButton.getStyle().replace(
                    "linear-gradient(to bottom, #ffcc00, #ff9900)",
                    "linear-gradient(to bottom, #ff9900, #ffcc00)"))); 
            
            challengeButton.setOnMouseExited(e -> 
                challengeButton.setStyle(challengeButton.getStyle().replace(
                    "linear-gradient(to bottom, #ff9900, #ffcc00)",
                    "linear-gradient(to bottom, #ffcc00, #ff9900)")));
                    
            // Enable the reject button and style it differently
            rejectButton.setDisable(false);
            rejectButton.setStyle("-fx-background-color: linear-gradient(to bottom, #cccccc, #999999);" +
                                 "-fx-text-fill: black;" +
                                 "-fx-font-weight: bold;" +
                                 "-fx-font-size: 12px;" +
                                 "-fx-padding: 5px 10px;" +
                                 "-fx-background-radius: 15;");
            
            // Add hover effect for reject button
            rejectButton.setOnMouseEntered(e -> 
                rejectButton.setStyle(rejectButton.getStyle().replace(
                    "linear-gradient(to bottom, #cccccc, #999999)",
                    "linear-gradient(to bottom, #999999, #cccccc)"))); 
            
            rejectButton.setOnMouseExited(e -> 
                rejectButton.setStyle(rejectButton.getStyle().replace(
                    "linear-gradient(to bottom, #999999, #cccccc)",
                    "linear-gradient(to bottom, #cccccc, #999999)")));
        } else {
            disableChallengeButton();
            disableRejectButton();
        }
    }
    
    /**
     * Disables the challenge button and resets its appearance
     */
    private void disableChallengeButton() {
        if (challengeButton == null) return;
        
        // Reset challenge state
        this.canChallenge = false;
        
        // Disable the button and reset its appearance
        challengeButton.setDisable(true);
        challengeButton.setStyle("-fx-background-color: #888888;" +
                                "-fx-text-fill: #dddddd;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 12px;" +
                                "-fx-padding: 5px 10px;" +
                                "-fx-background-radius: 15;");
                                
        // Remove hover effects
        challengeButton.setOnMouseEntered(null);
        challengeButton.setOnMouseExited(null);
    }

    /**
     * Shows a popup with the result of a Wild Draw Four challenge
     * 
     * @param message The challenge result message
     * @param successful Whether the challenge was successful
     */
    private void showChallengeResultPopup(String message, boolean successful) {
        //TODO: we may not need
        log.info("Showing challenge result popup. Success: {}, Message: {}", successful, message);
        
        // Create a custom alert
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Challenge Result");
        alert.setHeaderText(successful ? "Challenge Successful!" : "Challenge Failed");
        alert.setContentText(message);

        // Get the dialog pane and style it
        DialogPane dialogPane = alert.getDialogPane();

        // Set size
        dialogPane.setPrefWidth(400);
        dialogPane.setPrefHeight(200);

        // Apply custom styling to the dialog based on success/failure
        String gradientColors = successful ? 
            "linear-gradient(to bottom, #43c6ac, #191654)" :  // Success - blue-green
            "linear-gradient(to bottom, #cb2d3e, #ef473a)";   // Failure - red
            
        dialogPane.setStyle("-fx-background-color: " + gradientColors + "; " +
                           "-fx-background-radius: 15px; " +
                           "-fx-border-radius: 15px; " +
                           "-fx-border-color: " + (successful ? "#43c6ac" : "#ef473a") + "; " +
                           "-fx-border-width: 3px; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 15, 0, 0, 0);");

        // Style the header text
        Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
        if (headerLabel != null) {
            headerLabel.setStyle("-fx-font-size: 22px; " +
                              "-fx-font-weight: bold; " +
                              "-fx-text-fill: white; " +
                              "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 0, 0); " +
                              "-fx-padding: 10px 0 5px 0;");
        }

        // Style the content text
        Label contentLabel = (Label) dialogPane.lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setStyle("-fx-font-size: 16px; " +
                               "-fx-text-fill: white; " +
                               "-fx-padding: 10px 5px;");

            // Add emojis to make the message more visually interesting
            contentLabel.setText((successful ? "✅ " : "❌ ") + message);
        }

        // Style the OK button
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Continue");
        okButton.setStyle("-fx-background-color: " + (successful ? "#43c6ac" : "#ef473a") + "; " +
                         "-fx-text-fill: white; " +
                         "-fx-font-weight: bold; " +
                         "-fx-font-size: 14px; " +
                         "-fx-padding: 8px 20px; " +
                         "-fx-background-radius: 15px; " +
                         "-fx-cursor: hand;");

        // Add a container for button to center it
        HBox buttonBox = (HBox) dialogPane.lookup(".button-bar .container");
        if (buttonBox != null) {
            buttonBox.setStyle("-fx-alignment: center; -fx-padding: 10px 0 15px 0;");
        }

        // Set owner to main window
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.initOwner(bottomPlayerDeckArea.getScene().getWindow());

        // Show alert
        alert.show(); // Non-blocking
    }
    
    /**
     * Handles the reject button click
     * Only enabled when a WILD_DRAW_FOUR has been played against the player
     * 
     * @param event The action event
     */
    @FXML
    public void handleReject(ActionEvent event) {
        log.info("Player {} is rejecting the Wild Draw Four challenge", currentUsername);
        
        if (!canChallenge) {
            log.warn("Reject button was clicked but challenge option is not currently available");
            return;
        }
        
        // Call the multiplayer service to reject the challenge
        multiplayerService.rejectWildDrawFourChallenge(currentPlayerIndex);
        
        // Disable both buttons after rejecting
        disableChallengeButton();
        disableRejectButton();
    }
    
    /**
     * Disables the reject button and resets its appearance
     */
    private void disableRejectButton() {
        if (rejectButton == null) return;
        
        // Disable the button and reset its appearance
        rejectButton.setDisable(true);
        rejectButton.setStyle("-fx-background-color: #888888;" +
                             "-fx-text-fill: #dddddd;" +
                             "-fx-font-weight: bold;" +
                             "-fx-font-size: 12px;" +
                             "-fx-padding: 5px 10px;" +
                             "-fx-background-radius: 15;");
                             
        // Remove hover effects
        rejectButton.setOnMouseEntered(null);
        rejectButton.setOnMouseExited(null);
    }
}
