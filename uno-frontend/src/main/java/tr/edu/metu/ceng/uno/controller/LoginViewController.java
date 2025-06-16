package tr.edu.metu.ceng.uno.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tr.edu.metu.ceng.uno.service.AuthService;

import java.io.IOException;

@Component
public class LoginViewController {

    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Label loginErrorLabel;

    @FXML private TextField registerUsername;
    @FXML private TextField registerEmail;
    @FXML private PasswordField registerPassword;
    @FXML private PasswordField confirmPassword;
    @FXML private Label registerErrorLabel;
    
    @FXML private TextField forgotPasswordEmail;
    @FXML private Label forgotPasswordMessage;
    
    @FXML private Button loginTabButton;
    @FXML private Button registerTabButton;
    @FXML private VBox loginForm;
    @FXML private VBox registerForm;
    @FXML private VBox forgotPasswordForm;
    @FXML private Hyperlink forgotPasswordLink;

    @Value("classpath:/menu-view.fxml")
    private Resource mainMenuResource;

    private final AuthService authService;
    private final ApplicationContext applicationContext;

    public LoginViewController(AuthService authService, ApplicationContext applicationContext) {
        this.authService = authService;
        this.applicationContext = applicationContext;
    }
    
    @FXML
    public void switchToLogin(ActionEvent event) {
        loginForm.setVisible(true);
        registerForm.setVisible(false);
        forgotPasswordForm.setVisible(false);
        loginTabButton.setStyle("-fx-background-color: #d82020; -fx-text-fill: white; " +
                              "-fx-font-weight: bold; -fx-font-size: 16px; " +
                              "-fx-background-radius: 15 15 0 0; -fx-padding: 12 30;");
        registerTabButton.setStyle("-fx-background-color: #444444; -fx-text-fill: white; " +
                                 "-fx-font-weight: bold; -fx-font-size: 16px; " +
                                 "-fx-background-radius: 15 15 0 0; -fx-padding: 12 30;");
    }
    
    @FXML
    public void switchToRegister(ActionEvent event) {
        loginForm.setVisible(false);
        registerForm.setVisible(true);
        forgotPasswordForm.setVisible(false);
        loginTabButton.setStyle("-fx-background-color: #444444; -fx-text-fill: white; " +
                              "-fx-font-weight: bold; -fx-font-size: 16px; " +
                              "-fx-background-radius: 15 15 0 0; -fx-padding: 12 30;");
        registerTabButton.setStyle("-fx-background-color: #d82020; -fx-text-fill: white; " +
                                 "-fx-font-weight: bold; -fx-font-size: 16px; " +
                                 "-fx-background-radius: 15 15 0 0; -fx-padding: 12 30;");
    }
    
    @FXML
    public void handleForgotPassword(ActionEvent event) {
        loginForm.setVisible(false);
        registerForm.setVisible(false);
        forgotPasswordForm.setVisible(true);
        loginTabButton.setStyle("-fx-background-color: #444444; -fx-text-fill: white; " +
                              "-fx-font-weight: bold; -fx-font-size: 16px; " +
                              "-fx-background-radius: 15 15 0 0; -fx-padding: 12 30;");
        registerTabButton.setStyle("-fx-background-color: #444444; -fx-text-fill: white; " +
                                 "-fx-font-weight: bold; -fx-font-size: 16px; " +
                                 "-fx-background-radius: 15 15 0 0; -fx-padding: 12 30;");
    }
    
    @FXML
    public void handleForgotPasswordSubmit(ActionEvent event) {
        String email = forgotPasswordEmail.getText().trim();
        
        // Validate email
        if (email.isEmpty()) {
            forgotPasswordMessage.setText("Please enter your email address");
            forgotPasswordMessage.setTextFill(Color.RED);
            forgotPasswordMessage.setVisible(true);
            return;
        }
        
        // Call service to process forgot password
        boolean success = authService.forgotPassword(email);
        
        // Always show success message even if email doesn't exist in system
        // This is for security reasons
        forgotPasswordMessage.setText("A password reset link has been sent to your email address if it exists in our system.");
        forgotPasswordMessage.setTextFill(Color.GREEN);
        forgotPasswordMessage.setVisible(true);
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText().trim();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            loginErrorLabel.setText("Username and password are required");
            loginErrorLabel.setVisible(true);
            return;
        }

        // Attempt login
        boolean success = authService.login(username, password);

        if (success) {
            // Navigate to main menu
            navigateToMainMenu();
        } else {
            loginErrorLabel.setText("Invalid username or password");
            loginErrorLabel.setVisible(true);
        }
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        String username = registerUsername.getText().trim();
        String email = registerEmail.getText().trim();
        String password = registerPassword.getText().trim();
        String confirmPwd = confirmPassword.getText().trim();

        // Validate input
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPwd.isEmpty()) {
            registerErrorLabel.setText("All fields are required");
            registerErrorLabel.setVisible(true);
            return;
        }

        // Basic email validation
        if (!isValidEmail(email)) {
            registerErrorLabel.setText("Please enter a valid email address");
            registerErrorLabel.setVisible(true);
            return;
        }

        if (!password.equals(confirmPwd)) {
            registerErrorLabel.setText("Passwords do not match");
            registerErrorLabel.setVisible(true);
            return;
        }

        // Attempt registration
        boolean success = authService.register(username, email, password);

        if (success) {
            // Auto-login after successful registration
            if (authService.login(username, password)) {
                navigateToMainMenu();
            } else {
                registerErrorLabel.setText("Registration successful, but login failed");
                registerErrorLabel.setVisible(true);
            }
        } else {
            registerErrorLabel.setText("Registration failed. Username or email may already exist.");
            registerErrorLabel.setVisible(true);
        }
    }
    
    /**
     * Basic email validation
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    private void navigateToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(mainMenuResource.getURL());
            loader.setControllerFactory(applicationContext::getBean);
            Parent mainMenuView = loader.load();

            // Get current stage and scene
            Stage stage = (Stage) loginUsername.getScene().getWindow();
            Scene currentScene = stage.getScene();

            // Set background image
            String style = "-fx-background-image: url('" + "/images/background.png" + "');" +
                "-fx-background-size: cover;" +
                "-fx-background-position: center;";
            mainMenuView.setStyle(style);

            // Replace the root of the existing scene instead of creating a new one
            currentScene.setRoot(mainMenuView);

        } catch (IOException e) {
            e.printStackTrace();
            loginErrorLabel.setText("Error navigating to main menu");
            loginErrorLabel.setVisible(true);
        }
    }
}
