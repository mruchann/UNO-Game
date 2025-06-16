package tr.edu.metu.ceng.uno.history;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import tr.edu.metu.ceng.uno.user.User;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class GameRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private int score;

    @NonNull
    private LocalDate date;

    public GameRecord(User user, int score) {
        this.user = user;
        this.score = score;
        this.date = LocalDate.now();
    }

    public GameRecord(User user, int score, @NonNull LocalDate date) {
        this.user = user;
        this.score = score;
        this.date = date;
    }
}
