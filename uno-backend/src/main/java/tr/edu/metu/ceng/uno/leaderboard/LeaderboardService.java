package tr.edu.metu.ceng.uno.leaderboard;

import org.springframework.stereotype.Service;
import tr.edu.metu.ceng.uno.history.GameHistoryRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaderboardService {
    private final GameHistoryRepository gameHistoryRepository;

    public LeaderboardService(GameHistoryRepository gameHistoryRepository) {
        this.gameHistoryRepository = gameHistoryRepository;
    }

    public List<LeaderboardRecord> getLeaderboard() {
        return gameHistoryRepository.aggregateScores();
    }

    public List<LeaderboardRecord> getWeeklyLeaderboard() {
        return gameHistoryRepository.aggregateWeeklyScores(LocalDate.now().minusWeeks(1));
    }

    public List<LeaderboardRecord> getMonthlyLeaderboard() {
        return gameHistoryRepository.aggregateMonthlyScores(LocalDate.now().minusMonths(1));
    }
}
