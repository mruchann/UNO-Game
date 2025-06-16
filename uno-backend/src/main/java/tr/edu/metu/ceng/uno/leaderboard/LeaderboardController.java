package tr.edu.metu.ceng.uno.leaderboard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public ResponseEntity<List<LeaderboardRecord>> getLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getLeaderboard());
    }

    @GetMapping(path = "/weekly")
    public ResponseEntity<List<LeaderboardRecord>> getWeeklyLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getWeeklyLeaderboard());
    }

    @GetMapping(path = "/monthly")
    public ResponseEntity<List<LeaderboardRecord>> getMonthlyLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getMonthlyLeaderboard());
    }
}
