package tr.edu.metu.ceng.uno.history;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.edu.metu.ceng.uno.history.request.CreateGameRecordRequest;

import java.util.List;

@RestController
@RequestMapping(path = "/game-history")
public class GameHistoryController {
    private final GameHistoryService gameHistoryService;

    public GameHistoryController(GameHistoryService gameHistoryService) {
        this.gameHistoryService = gameHistoryService;
    }

    @PostMapping(path = "/create")
    public ResponseEntity<String> createGameRecord(@Valid @RequestBody CreateGameRecordRequest request) {
        gameHistoryService.createGameRecord(request);
        return ResponseEntity.ok("Game record created successfully");
    }

    @GetMapping
    public ResponseEntity<List<GameRecord>> getAllGameRecords() {
        return ResponseEntity.ok(gameHistoryService.getAllGameRecords());
    }

    @GetMapping(path = "/weekly")
    public ResponseEntity<List<GameRecord>> getWeeklyGameRecords() {
        return ResponseEntity.ok(gameHistoryService.getWeeklyGameRecords());
    }

    @GetMapping(path = "/monthly")
    public ResponseEntity<List<GameRecord>> getMonthlyGameRecords() {
        return ResponseEntity.ok(gameHistoryService.getMonthlyGameRecords());
    }

    @PostMapping(path = "/populate")
    public ResponseEntity<String> populateGameHistory() {
        gameHistoryService.populateGameHistory();
        return ResponseEntity.ok("Game history populated successfully");
    }
}
