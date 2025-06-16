package tr.edu.metu.ceng.uno;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tr.edu.metu.ceng.uno.email.EmailService;

@SpringBootTest
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    void testSendEmail() {
        emailService.sendPasswordResetEmail("korese03@gmail.com", "test-token", "testuser");
    }
}
