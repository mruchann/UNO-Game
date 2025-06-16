package tr.edu.metu.ceng.uno;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tr.edu.metu.ceng.uno.JavaFXApplication.StageReadyEvent;

import java.io.IOException;
import java.util.Objects;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    @Value("classpath:/login-view.fxml")
    private Resource resource;

    private String applicationTitle;
    private ApplicationContext applicationContext;

    public StageInitializer(@Value("${spring.application.ui.title}") String applicationTitle,
                            ApplicationContext applicationContext) {
        this.applicationTitle = applicationTitle;
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(resource.getURL());
            loader.setControllerFactory(applicationContext::getBean);
            Parent parent = loader.load();

            // No need to set background image for the login screen
            // as we've designed it with its own background

            Stage stage = event.getStage();
            Scene scene = new Scene(parent, 1200, 800);
            stage.setScene(scene);
            stage.setTitle(applicationTitle);
            
            // Set application icon
            Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/uno.jpg")));
            stage.getIcons().add(appIcon);
            
            // Set stage properties
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            
            // Set fullscreen
            stage.setFullScreen(true);
            stage.setFullScreenExitHint(""); // Remove the exit fullscreen hint

            stage.show();
        }

        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }

    private void setBackgroundImage(Parent parent) {
        String style = "-fx-background-image: url('" + "/images/background.png" + "');" +
            "-fx-background-size: cover;" +
            "-fx-background-position: center;";

        parent.setStyle(style);
    }
}
