package tr.edu.metu.ceng.uno.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tr.edu.metu.ceng.uno.email.EmailService;
import tr.edu.metu.ceng.uno.exception.InvalidCredentialsException;
import tr.edu.metu.ceng.uno.exception.InvalidTokenException;
import tr.edu.metu.ceng.uno.util.JwtUtil;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public void register(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User(username, email, passwordEncoder.encode(password));
        userRepository.save(user);
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!checkPassword(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return jwtUtil.generateToken(username);
    }

    public void logout() {

    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email"));

        String token = jwtUtil.generateToken(user.getUsername());
        emailService.sendPasswordResetEmail(email, token, user.getUsername());
    }

    public void resetPassword(String newPassword, String token) {
        String username = jwtUtil.extractUsername(token).orElseThrow(() -> new InvalidTokenException("Username not found"));

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
