package tr.edu.metu.ceng.uno.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.edu.metu.ceng.uno.exception.InvalidCredentialsException;
import tr.edu.metu.ceng.uno.template.HtmlTemplates;
import tr.edu.metu.ceng.uno.user.request.LoginRequest;
import tr.edu.metu.ceng.uno.user.request.RegisterRequest;
import tr.edu.metu.ceng.uno.user.request.ForgotPasswordRequest;
import tr.edu.metu.ceng.uno.user.request.ResetPasswordRequest;

import java.util.Map;

@RestController
@RequestMapping(path = "/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            userService.register(registerRequest.getUsername(), registerRequest.getEmail(), registerRequest.getPassword());
            return ResponseEntity.ok("User registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping(path = "/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String token = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /*
    TODO: Frontend should handle logout:
        Delete the JWT from local storage, session storage, or cookies.
        Redirect the user to the login page.
    */
    @PostMapping(path = "/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping(path = "/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        userService.forgotPassword(forgotPasswordRequest.getEmail());
        return ResponseEntity.ok("Confirmation email has been successfully sent");
    }

    @PostMapping(path = "/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest, @RequestParam String token) {
        userService.resetPassword(resetPasswordRequest.getNewPassword(), token);
        return ResponseEntity.ok("Your password has been successfully reset");
    }

    @GetMapping(path = "/reset-password-form")
    public ResponseEntity<String> showResetPasswordForm(@RequestParam String token) {
        String htmlForm = HtmlTemplates.getPasswordResetForm(token);
        
        return ResponseEntity.ok()
            .header("Content-Type", "text/html")
            .body(htmlForm);
    }
}
