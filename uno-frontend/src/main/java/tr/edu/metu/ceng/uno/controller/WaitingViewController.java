package tr.edu.metu.ceng.uno.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tr.edu.metu.ceng.uno.service.MultiplayerService;

import java.io.IOException;

@Component
public class WaitingViewController {

    @FXML
    private Button cancelButton;

    @Value("classpath:/menu-view.fxml")
    private Resource menuViewResource;

    private final MultiplayerService multiplayerService;
    private final ApplicationContext applicationContext;

    public WaitingViewController(MultiplayerService multiplayerService, ApplicationContext applicationContext) {
        this.multiplayerService = multiplayerService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        // Initialize the controller
    }

    @FXML
    public void handleCancelButton(ActionEvent event) {
        // Send ROOM_QUIT message
        multiplayerService.leaveGame();
        
        // Return to menu view
        try {
            FXMLLoader loader = new FXMLLoader(menuViewResource.getURL());
            loader.setControllerFactory(applicationContext::getBean);
            Parent menuView = loader.load();
            
            // Get the current stage and set the menu view
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            
            // Keep the same window size and settings
            Scene currentScene = cancelButton.getScene();
            boolean isFullScreen = stage.isFullScreen();
            
            // Apply the same styling
            String style = "-fx-background-image: url('" + "/images/background.png" + "');" +
                "-fx-background-size: cover;" +
                "-fx-background-position: center;";
            menuView.setStyle(style);
            
            // Replace the root of the existing scene instead of creating a new one
            currentScene.setRoot(menuView);
            
            // Maintain fullscreen state
            stage.setFullScreen(isFullScreen);
            
        } catch (IOException ex) {
            ex.printStackTrace();
            // Handle error
        }
    }
} 