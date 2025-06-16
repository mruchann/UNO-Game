package tr.edu.metu.ceng.uno.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tr.edu.metu.ceng.uno.service.AuthService;
import tr.edu.metu.ceng.uno.service.MultiplayerService;

import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

@Component
@Slf4j
public class MenuViewController {

    @FXML private Label usernameLabel;
    @FXML private Button playButton;
    @FXML private Button multiplayerButton;
    @FXML private Button leaderboardButton;
    @FXML private Button logoutButton;

    @Value("classpath:/singleplayer-game-view.fxml")
    private Resource singleplayerGameViewResource;

    @Value("classpath:/multiplayer-game-view.fxml")
    private Resource multiplayerGameViewResource;

    @Value("classpath:/waiting-view.fxml")
    private Resource waitingViewResource;

    @Value("classpath:/leaderboard-view.fxml")
    private Resource leaderboardResource;

    @Value("classpath:/login-view.fxml")
    private Resource loginResource;

    private final AuthService authService;
    private final MultiplayerService multiplayerService;
    private final ApplicationContext applicationContext;

    public MenuViewController(AuthService authService, MultiplayerService multiplayerService, ApplicationContext applicationContext) {
        log.info("Creating MenuViewController instance");
        this.authService = authService;
        this.multiplayerService = multiplayerService;
        this.applicationContext = applicationContext;
        log.debug("MenuViewController dependencies injected: authService, multiplayerService, applicationContext");
    }

    @FXML
    public void initialize() {
        log.info("Initializing menu view");
        // Set the username label to the current user's username
        if (authService.isAuthenticated()) {
            String username = authService.getCurrentUsername();
            log.debug("Setting welcome message for user: {}", username);
            usernameLabel.setText("Welcome, " + username + "!");
        } else {
            log.warn("User not authenticated in menu view");
        }
    }

    @FXML
    public void handlePlay(ActionEvent event) {
        log.info("User starting singleplayer game");
        try {
            log.debug("Loading singleplayer game view");
            FXMLLoader loader = new FXMLLoader(singleplayerGameViewResource.getURL());
            loader.setControllerFactory(applicationContext::getBean);
            Parent gameView = loader.load();

            // Get current stage from the event source instead of the label
            // This is more reliable as it uses the component that triggered the event
            Button sourceButton = (Button) event.getSource();
            Stage stage = (Stage) sourceButton.getScene().getWindow();
            Scene currentScene = stage.getScene();

            // Set background image
            String style = "-fx-background-image: url('" + "/images/background.png" + "');" +
                "-fx-background-size: cover;" +
                "-fx-background-position: center;";
            gameView.setStyle(style);

            // Replace the root of the existing scene instead of creating a new one
            currentScene.setRoot(gameView);
            log.info("Successfully loaded singleplayer game view");

        } catch (IOException e) {
            log.error("Error loading singleplayer game view", e);
            showError("Error loading game view");
        }
    }

    @FXML
    public void handleMultiplayer(ActionEvent event) {
        log.info("User starting multiplayer game");
        try {
            // Load waiting view
            log.debug("Loading waiting view");
            FXMLLoader loader = new FXMLLoader(waitingViewResource.getURL());
            loader.setControllerFactory(applicationContext::getBean);
            Parent waitingView = loader.load();

            // Get the controller
            WaitingViewController waitingController = loader.getController();

            // Get current stage from the event source
            Button sourceButton = (Button) event.getSource();
            Stage stage = (Stage) sourceButton.getScene().getWindow();

            // Store the current dimensions to keep them consistent
            boolean isFullScreen = stage.isFullScreen();

            // Set background image 
            String style = "-fx-background-image: url('" + "/images/background.png" + "');" +
                "-fx-background-size: cover;" +
                "-fx-background-position: center;";
            waitingView.setStyle(style);

            // Replace the root of the existing scene instead of creating a new one
            Scene currentScene = stage.getScene();
            currentScene.setRoot(waitingView);

            // Maintain fullscreen state
            stage.setFullScreen(isFullScreen);
            log.info("Successfully loaded waiting view, joining multiplayer room");

            // Join the multiplayer room
            multiplayerService.joinRoom().thenAccept(gameId -> {
                Platform.runLater(() -> {
                    if (gameId != null) {
                        log.info("Game joined with ID: {}", gameId);
                        // Game started, load the game view
                        try {
                            log.debug("Loading multiplayer game view");
                            FXMLLoader gameLoader = new FXMLLoader(multiplayerGameViewResource.getURL());
                            gameLoader.setControllerFactory(applicationContext::getBean);
                            Parent gameView = gameLoader.load();

                            // Get the controller and tell it this is a multiplayer game
                            MultiplayerGameViewController controller = gameLoader.getController();

                            gameView.setStyle(style);

                            // Replace the root of the existing scene
                            currentScene.setRoot(gameView);

                            // Maintain fullscreen state
                            stage.setFullScreen(isFullScreen);
                            log.info("Successfully loaded multiplayer game view");
                        } catch (IOException e) {
                            log.error("Error loading multiplayer game view", e);
                            showError("Error loading multiplayer game view");
                        }
                    } else {
                        log.warn("Received null game ID when joining multiplayer room");
                    }
                });
            }).exceptionally(ex -> {
                log.error("Error joining multiplayer game", ex);
                Platform.runLater(() -> {
                    showError("Error joining multiplayer game: " + ex.getMessage());
                    // Return to the menu
                    try {
                        log.debug("Returning to login view after multiplayer error");
                        FXMLLoader menuLoader = new FXMLLoader(loginResource.getURL());
                        menuLoader.setControllerFactory(applicationContext::getBean);
                        Parent menuView = menuLoader.load();
                        menuView.setStyle(style);

                        // Replace the root of the existing scene
                        currentScene.setRoot(menuView);

                        // Maintain fullscreen state
                        stage.setFullScreen(isFullScreen);
                        log.info("Successfully returned to login view");
                    } catch (IOException e) {
                        log.error("Error returning to login view", e);
                        showError("Error returning to menu");
                    }
                });
                return null;
            });
        } catch (IOException ex) {
            log.error("Error loading waiting view", ex);
            showError("Error loading waiting view");
        }
    }

    @FXML
    public void handleLeaderboard(ActionEvent event) {
        log.info("User viewing leaderboard");
        try {
            log.debug("Loading leaderboard view");
            FXMLLoader loader = new FXMLLoader(leaderboardResource.getURL());
            loader.setControllerFactory(applicationContext::getBean);
            Parent leaderboardView = loader.load();

            // Get current stage from the event source
            Button sourceButton = (Button) event.getSource();
            Stage stage = (Stage) sourceButton.getScene().getWindow();
            Scene currentScene = stage.getScene();

            // Set background image
            String style = "-fx-background-image: url('" + "/images/background.png" + "');" +
                "-fx-background-size: cover;" +
                "-fx-background-position: center;";
            leaderboardView.setStyle(style);

            // Replace the root of the existing scene instead of creating a new one
            currentScene.setRoot(leaderboardView);
            log.info("Successfully loaded leaderboard view");

        } catch (IOException e) {
            log.error("Error loading leaderboard view", e);
            showError("Error loading leaderboard view");
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        log.info("User logging out");
        // Log out the user
        authService.logout();
        log.debug("User logged out successfully from auth service");

        try {
            log.debug("Loading login view");
            FXMLLoader loader = new FXMLLoader(loginResource.getURL());
            loader.setControllerFactory(applicationContext::getBean);
            Parent loginView = loader.load();

            // Get current stage and scene
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            Scene currentScene = stage.getScene();

            // Set background image
            String style = "-fx-background-image: url('" + "/images/background.png" + "');" +
                "-fx-background-size: cover;" +
                "-fx-background-position: center;";
            loginView.setStyle(style);

            // Replace the root of the existing scene instead of creating a new one
            currentScene.setRoot(loginView);
            log.info("Successfully navigated to login view after logout");

        } catch (IOException e) {
            log.error("Error navigating to login view after logout", e);
            showError("Error logging out");
        }
    }

    private void showError(String message) {
        log.error("Showing error alert: {}", message);
        // Create an error alert dialog
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
