package tr.edu.metu.ceng.uno.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String email, String token, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Your Password Reset Request");

        message.setText("Hi " + username + " !\n\n" + "To reset your password, please click the link below:\n" +
            "https://ceng453-20242-group1-backend.onrender.com/user/reset-password-form?token=" + token);

//        message.setText("Hi " + username + " !\n\n" + "To reset your password, please click the link below:\n" +
//                "http://localhost:8080/user/reset-password-form?token=" + token);

        mailSender.send(message);
    }
}
