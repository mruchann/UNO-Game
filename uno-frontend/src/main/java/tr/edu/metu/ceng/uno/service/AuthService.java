package tr.edu.metu.ceng.uno.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service to handle user authentication and registration with backend API
 */
@Service
@Slf4j
public class AuthService {

    private final RestTemplate restTemplate;
    private String currentUsername;
    private String jwtToken;
    private boolean authenticated = false;

    @Value("${api.base-url}")
    private String apiBaseUrl;

    public AuthService() {
        log.debug("Initializing AuthService");
        log.info("Creating new AuthService instance");
        this.restTemplate = new RestTemplate();
        log.debug("RestTemplate initialized for AuthService");
    }

    /**
     * Authenticate a user with username and password
     * 
     * @param username The user's username
     * @param password The user's password
     * @return true if login was successful, false otherwise
     */
    public boolean login(String username, String password) {
        log.info("Attempting login for user: {}", username);
        try {
            // Create login request body
            Map<String, String> requestBody = Map.of(
                "username", username,
                "password", password
            );

            // Make API call to backend
            String loginUrl = apiBaseUrl + "/user/login";
            log.debug("Sending login request to: {}", loginUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                loginUrl,
                requestBody,
                Map.class
            );

            // Extract JWT token from response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                this.jwtToken = (String) response.getBody().get("token");
                this.currentUsername = username;
                this.authenticated = true;
                log.info("User {} successfully logged in", username);
                return true;
            }

            log.warn("Login failed for user: {}, response status: {}", username, response.getStatusCode());
            return false;
        } catch (HttpClientErrorException e) {
            log.error("Login error for user {}: {}", username, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during login for user {}: {}", username, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Register a new user
     * 
     * @param username The new user's username
     * @param email The new user's email
     * @param password The new user's password
     * @return true if registration was successful, false otherwise
     */
    public boolean register(String username, String email, String password) {
        log.info("Attempting to register new user: {}", username);
        try {
            // Create registration request body
            Map<String, String> requestBody = Map.of(
                "username", username,
                "email", email,
                "password", password
            );

            // Make API call to backend
            String registerUrl = apiBaseUrl + "/user/register";
            log.debug("Sending registration request to: {}", registerUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(
                registerUrl,
                requestBody,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("User {} successfully registered", username);
                return true;
            } else {
                log.warn("Registration failed for user: {}, response status: {}", username, response.getStatusCode());
                return false;
            }
        } catch (HttpClientErrorException e) {
            log.error("Registration error for user {}: {}", username, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during registration for user {}: {}", username, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Handle forgot password requests
     * 
     * @param email The email to send password reset instructions to
     * @return true if the request was successful, false otherwise
     */
    public boolean forgotPassword(String email) {
        log.info("Processing forgot password request for email: {}", email);
        try {
            // Create forgot password request body
            Map<String, String> requestBody = Map.of("email", email);

            // Make API call to backend
            String forgotPasswordUrl = apiBaseUrl + "/user/forgot-password";
            log.debug("Sending forgot password request to: {}", forgotPasswordUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(
                forgotPasswordUrl,
                requestBody,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Forgot password request processed successfully for email: {}", email);
                return true;
            } else {
                log.warn("Forgot password request failed for email: {}, response status: {}", email, response.getStatusCode());
                return false;
            }
        } catch (HttpClientErrorException e) {
            log.error("Forgot password error for email {}: {}", email, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during forgot password for email {}: {}", email, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Reset user password
     * 
     * @param newPassword The new password
     * @param token The reset token from email
     * @return true if password reset was successful, false otherwise
     */
    public boolean resetPassword(String newPassword, String token) {
        log.info("Attempting to reset password with token");
        try {
            // Create reset password request body
            Map<String, String> requestBody = Map.of("newPassword", newPassword);

            // Make API call to backend
            String resetPasswordUrl = apiBaseUrl + "/user/reset-password?token=" + token;
            log.debug("Sending reset password request to: {}", resetPasswordUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(
                resetPasswordUrl,
                requestBody,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Password reset successful");
                return true;
            } else {
                log.warn("Password reset failed, response status: {}", response.getStatusCode());
                return false;
            }
        } catch (HttpClientErrorException e) {
            log.error("Reset password error: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during password reset: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Log out the current user
     */
    public void logout() {
        log.info("Logging out user: {}", currentUsername);
        try {
            // Create headers with JWT token
            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null) {
                headers.setBearerAuth(jwtToken);
            }

            // Make API call to backend
            String logoutUrl = apiBaseUrl + "/user/logout";
            log.debug("Sending logout request to: {}", logoutUrl);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            restTemplate.exchange(
                logoutUrl,
                HttpMethod.POST,
                entity,
                String.class
            );
            log.info("Logout request sent successfully");
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
        } finally {
            // Clear local state regardless of API call success
            log.debug("Clearing authentication state");
            this.currentUsername = null;
            this.jwtToken = null;
            this.authenticated = false;
        }
    }

    /**
     * Check if a user is currently authenticated
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isAuthenticated() {
        log.debug("Checking authentication status: {}", authenticated);
        return authenticated;
    }

    /**
     * Get the username of the currently logged in user
     * 
     * @return the current username, or null if no user is logged in
     */
    public String getCurrentUsername() {
        log.debug("Getting current username: {}", currentUsername);
        return currentUsername;
    }

    /**
     * Get the JWT token of the currently logged in user
     * 
     * @return the current JWT token, or null if no user is logged in
     */
    public String getJwtToken() {
        log.debug("Getting JWT token");
        if (jwtToken == null) {
            log.debug("JWT token is null, user may not be authenticated");
        } else {
            log.debug("JWT token is available");
        }
        return jwtToken;
    }

    /**
     * Add authentication header to existing headers
     * 
     * @param headers Existing headers
     * @return Headers with authentication added
     */
    public HttpHeaders addAuthHeader(HttpHeaders headers) {
        log.debug("Adding authentication header");
        if (headers == null) {
            log.debug("Headers object was null, creating new HttpHeaders");
            headers = new HttpHeaders();
        } else {
            log.debug("Using existing headers object with {} existing headers", headers.size());
        }

        if (isAuthenticated() && jwtToken != null) {
            log.debug("Setting Bearer auth header with JWT token for user: {}", currentUsername);
            headers.setBearerAuth(jwtToken);
            log.debug("Authentication header added successfully");
        } else {
            log.debug("No JWT token available, skipping auth header. Authentication status: {}", authenticated);
            if (jwtToken == null) {
                log.debug("JWT token is null");
            }
        }

        log.debug("Returning headers with {} total headers", headers.size());
        return headers;
    }
}
