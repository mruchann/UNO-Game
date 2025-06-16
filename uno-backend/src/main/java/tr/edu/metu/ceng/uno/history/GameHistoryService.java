package tr.edu.metu.ceng.uno.history;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tr.edu.metu.ceng.uno.history.request.CreateGameRecordRequest;
import tr.edu.metu.ceng.uno.user.User;
import tr.edu.metu.ceng.uno.user.UserRepository;
import tr.edu.metu.ceng.uno.util.Random;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class GameHistoryService {

    private final GameHistoryRepository gameHistoryRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public GameHistoryService(GameHistoryRepository gameHistoryRepository, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.gameHistoryRepository = gameHistoryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<GameRecord> getAllGameRecords() {
        return gameHistoryRepository.findAll();
    }

    public List<GameRecord> getWeeklyGameRecords() {
        return gameHistoryRepository.findAllByDateBetween(LocalDate.now().minusWeeks(1), LocalDate.now());
    }

    public List<GameRecord> getMonthlyGameRecords() {
        return gameHistoryRepository.findAllByDateBetween(LocalDate.now().minusMonths(1), LocalDate.now());
    }

    public void populateGameHistory() {
        List<GameRecord> gameRecords = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            int userId1 = Random.getRandomNumber(50), userId2 = Random.getRandomNumber(50);
            int daysBefore = Random.getRandomNumber(50);

            // Create or find users
            User user1 = createOrFindUser(userId1);
            User user2 = createOrFindUser(userId2);

            GameRecord gameRecord1 = new GameRecord(user1, 1, LocalDate.now().minusDays(daysBefore));
            GameRecord gameRecord2 = new GameRecord(user2, -1, LocalDate.now().minusDays(daysBefore));

            gameRecords.add(gameRecord1);
            gameRecords.add(gameRecord2);
        }

        gameHistoryRepository.saveAll(gameRecords);
    }

    private User createOrFindUser(int userId) {
        // Try to find an existing user with a username that matches the pattern
        String username = "user" + userId;
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    // Create a new user if not found
                    User newUser = new User(username, username + "@example.com", passwordEncoder.encode("password"));
                    return userRepository.save(newUser);
                });
    }

    public void createGameRecord(CreateGameRecordRequest request) {
        Optional<User> user = null;
        try {
            // Try to parse the userId as a UUID
            String username = request.getUsername();
            user = userRepository.findByUsername(username);
        } catch (IllegalArgumentException e) {
            log.error("User not found for creating game record: {}", request.getUsername());
        }

        GameRecord gameRecord = new GameRecord(
            user.get(),
            request.getScore(),
            LocalDate.now()
        );

        gameHistoryRepository.save(gameRecord);
    }
}
