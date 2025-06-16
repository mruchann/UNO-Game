package tr.edu.metu.ceng.uno.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tr.edu.metu.ceng.uno.user.User;
import tr.edu.metu.ceng.uno.user.UserRepository;
import tr.edu.metu.ceng.uno.user.UserService;
import tr.edu.metu.ceng.uno.user.request.LoginRequest;
import tr.edu.metu.ceng.uno.user.request.RegisterRequest;
import tr.edu.metu.ceng.uno.util.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserTest { //fully loaded application, integration tests
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    
    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("testUser2");
        validRegisterRequest.setEmail("testUser2@gmail.com");
        validRegisterRequest.setPassword("password123");
        
        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("ruchan"); //existing user
        validLoginRequest.setPassword("ruchan");
    }

    @Test
    void registerSuccess() throws Exception {
        //user shouldn't exist before
        assertFalse(userRepository.existsByUsername(validRegisterRequest.getUsername()));

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));

        // verify that the user was saved in the DB
        User savedUser = userRepository.findByUsername(validRegisterRequest.getUsername()).orElse(null);
        assertNotNull(savedUser);
        assertEquals(validRegisterRequest.getEmail(), savedUser.getEmail());
    }
    
    @Test
    void registerFailWhenUsernameExists() throws Exception {
        validRegisterRequest.setUsername("ruchan");

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest())  // Expect a 400 Bad Request
                .andExpect(content().string("Username already exists"));
    }

    @Test
    void registerFailWhenEmailExists() throws Exception {
        validRegisterRequest.setEmail("ruchannyavuzdemir@gmail.com");

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest())  // Expect a 400 Bad Request
                .andExpect(content().string("Email already exists"));
    }
    
    @Test
    void registerFailWithInvalidRequest() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest();
        // Leave all fields empty to trigger validation errors
        mockMvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void loginSuccess() throws Exception {
        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

    }
    
    @Test
    void loginFailWithInvalidCredentials() throws Exception {
        validLoginRequest.setPassword("2");
        
        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }
    
    @Test
    void loginFailWithInvalidRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        
        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

    }


}
