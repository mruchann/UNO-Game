package tr.edu.metu.ceng.uno.history.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGameRecordRequest {
    private String username;
    private int score;

    public CreateGameRecordRequest(String username, int score) {
        this.username = username;
        this.score = score;
    }
}
