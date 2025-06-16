package tr.edu.metu.ceng.uno.leaderboard;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LeaderboardRecord {
    private String username;
    private long score;

    public LeaderboardRecord(String username, long score) {
        this.username = username;
        this.score = score;
    }

    // Constructor that accepts UUID and username
    public LeaderboardRecord(UUID userId, String username, long score) {
        this.username = username;
        this.score = score;
    }
}
