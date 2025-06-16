package tr.edu.metu.ceng.uno.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tr.edu.metu.ceng.uno.leaderboard.LeaderboardRecord;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameRecord, Long> {
    List<GameRecord> findAllByDateBetween(LocalDate dateAfter, LocalDate dateBefore);

    @Query("""
        SELECT new tr.edu.metu.ceng.uno.leaderboard.LeaderboardRecord(g.user.username, SUM(g.score))
        FROM GameRecord g
        GROUP BY g.user.username
        ORDER BY SUM(g.score) DESC
        LIMIT 100
        """)
    List<LeaderboardRecord> aggregateScores();

    @Query("""
        SELECT new tr.edu.metu.ceng.uno.leaderboard.LeaderboardRecord(g.user.username, SUM(g.score))
        FROM GameRecord g
        WHERE g.date >= :weekStart
        GROUP BY g.user.username
        ORDER BY SUM(g.score) DESC
        LIMIT 100
        """)
    List<LeaderboardRecord> aggregateWeeklyScores(LocalDate weekStart);

    @Query("""
        SELECT new tr.edu.metu.ceng.uno.leaderboard.LeaderboardRecord(g.user.username, SUM(g.score))
        FROM GameRecord g
        WHERE g.date >= :monthStart
        GROUP BY g.user.username
        ORDER BY SUM(g.score) DESC
        LIMIT 100
        """)
    List<LeaderboardRecord> aggregateMonthlyScores(LocalDate monthStart);

}
