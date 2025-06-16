package tr.edu.metu.ceng.uno.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tr.edu.metu.ceng.uno.service.LeaderboardService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class LeaderboardViewController {

    @FXML private TableView<LeaderboardEntry> allTimeTable;
    @FXML private TableColumn<LeaderboardEntry, Integer> allTimeRankColumn;
    @FXML private TableColumn<LeaderboardEntry, String> allTimeUsernameColumn;
    @FXML private TableColumn<LeaderboardEntry, Integer> allTimeScoreColumn;

    @FXML private TableView<LeaderboardEntry> monthlyTable;
    @FXML private TableColumn<LeaderboardEntry, Integer> monthlyRankColumn;
    @FXML private TableColumn<LeaderboardEntry, String> monthlyUsernameColumn;
    @FXML private TableColumn<LeaderboardEntry, Integer> monthlyScoreColumn;

    @FXML private TableView<LeaderboardEntry> weeklyTable;
    @FXML private TableColumn<LeaderboardEntry, Integer> weeklyRankColumn;
    @FXML private TableColumn<LeaderboardEntry, String> weeklyUsernameColumn;
    @FXML private TableColumn<LeaderboardEntry, Integer> weeklyScoreColumn;

    @Value("classpath:/menu-view.fxml")
    private Resource mainMenuResource;

    private final LeaderboardService leaderboardService;
    private final ApplicationContext applicationContext;

    public LeaderboardViewController(LeaderboardService leaderboardService, ApplicationContext applicationContext) {
        this.leaderboardService = leaderboardService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        // Initialize table columns
        initializeTableColumns();

        // Apply custom styling to tables
        styleTableViews();

        // Load leaderboard data
        loadLeaderboardData();
    }

    private void styleTableViews() {
        // Apply row styling to all three tables
        styleTableRows(allTimeTable, "#ff3333");
        styleTableRows(monthlyTable, "#ff9900");
        styleTableRows(weeklyTable, "#3399ff");

        // Apply custom cell formatting for rank columns
        customizeRankColumn(allTimeRankColumn);
        customizeRankColumn(monthlyRankColumn);
        customizeRankColumn(weeklyRankColumn);

        // Apply custom formatting for score columns
        customizeScoreColumn(allTimeScoreColumn);
        customizeScoreColumn(monthlyScoreColumn);
        customizeScoreColumn(weeklyScoreColumn);

        // Apply custom formatting for username columns
        customizeUsernameColumn(allTimeUsernameColumn);
        customizeUsernameColumn(monthlyUsernameColumn);
        customizeUsernameColumn(weeklyUsernameColumn);
    }

    private void styleTableRows(TableView<LeaderboardEntry> tableView, String highlightColor) {
        tableView.setRowFactory(tv -> {
            TableRow<LeaderboardEntry> row = new TableRow<LeaderboardEntry>() {
                @Override
                protected void updateItem(LeaderboardEntry item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setStyle("");
                        return;
                    }

                    // Special styling for top 3 ranks
                    if (item.getRank() <= 3) {
                        String color = String.format("linear-gradient(to right, %s10, transparent)", highlightColor);
                        setStyle("-fx-background-color: " + color + ";");
                    } else {
                        setStyle("");
                    }
                }
            };

            // Add hover effect
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1);");
                }
            });

            row.setOnMouseExited(event -> {
                if (!row.isEmpty()) {
                    LeaderboardEntry item = row.getItem();
                    if (item.getRank() <= 3) {
                        String color = String.format("linear-gradient(to right, %s10, transparent)", highlightColor);
                        row.setStyle("-fx-background-color: " + color + ";");
                    } else {
                        row.setStyle("");
                    }
                }
            });

            return row;
        });
    }

    private void customizeRankColumn(TableColumn<LeaderboardEntry, Integer> column) {
        column.setCellFactory(col -> new TableCell<LeaderboardEntry, Integer>() {
            @Override
            protected void updateItem(Integer rank, boolean empty) {
                super.updateItem(rank, empty);

                if (empty || rank == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }

                if (rank == 1) {
                    HBox container = new HBox();
                    container.setAlignment(Pos.CENTER);

                    Text rankText = new Text("\uD83E\uDD47 #" + rank);
                    rankText.setStyle("-fx-fill: white;");
                    container.getChildren().add(rankText);

                    setGraphic(container);
                    setText(null);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #FFD700; -fx-alignment: CENTER;");
                } 
                else if (rank == 2) {
                    HBox container = new HBox();
                    container.setAlignment(Pos.CENTER);

                    Text rankText = new Text("\uD83E\uDD48 #" + rank);
                    rankText.setStyle("-fx-fill: white;");
                    container.getChildren().add(rankText);

                    setGraphic(container);
                    setText(null);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #C0C0C0; -fx-alignment: CENTER;");
                } 
                else if (rank == 3) {
                    HBox container = new HBox();
                    container.setAlignment(Pos.CENTER);

                    Text rankText = new Text("\uD83E\uDD49 #" + rank);
                    rankText.setStyle("-fx-fill: white;");
                    container.getChildren().add(rankText);

                    setGraphic(container);
                    setText(null);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #CD7F32; -fx-alignment: CENTER;");
                } 
                else {
                    setText("#" + rank.toString());
                    setGraphic(null);
                    setStyle("-fx-text-fill: white; -fx-alignment: CENTER;");
                }

                setAlignment(Pos.CENTER);
            }
        });

        // Set column alignment to center
        column.setStyle("-fx-alignment: CENTER;");
    }

    private void customizeScoreColumn(TableColumn<LeaderboardEntry, Integer> column) {
        column.setCellFactory(col -> new TableCell<LeaderboardEntry, Integer>() {
            @Override
            protected void updateItem(Integer score, boolean empty) {
                super.updateItem(score, empty);

                if (empty || score == null) {
                    setText(null);
                    return;
                }

                setText(String.format("%,d pts", score));
                setAlignment(Pos.CENTER);

                TableRow<?> row = getTableRow();
                if (row != null && row.getItem() != null) {
                    LeaderboardEntry entry = (LeaderboardEntry) row.getItem();
                    if (entry.getRank() == 1) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #FFD700; -fx-alignment: CENTER;");
                    } else if (entry.getRank() == 2) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #C0C0C0; -fx-alignment: CENTER;");
                    } else if (entry.getRank() == 3) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #CD7F32; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: white; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        // Set column alignment to center
        column.setStyle("-fx-alignment: CENTER;");
    }

    private void customizeUsernameColumn(TableColumn<LeaderboardEntry, String> column) {
        column.setCellFactory(col -> new TableCell<LeaderboardEntry, String>() {
            @Override
            protected void updateItem(String username, boolean empty) {
                super.updateItem(username, empty);

                if (empty || username == null) {
                    setText(null);
                    return;
                }

                setText(username);
                setAlignment(Pos.CENTER);

                TableRow<?> row = getTableRow();
                if (row != null && row.getItem() != null) {
                    LeaderboardEntry entry = (LeaderboardEntry) row.getItem();
                    if (entry.getRank() == 1) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: CENTER;");
                    } else if (entry.getRank() == 2) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: CENTER;");
                    } else if (entry.getRank() == 3) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: white; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        // Set column alignment to center
        column.setStyle("-fx-alignment: CENTER;");
    }

    private void initializeTableColumns() {
        // All-time leaderboard
        allTimeRankColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getRank()).asObject());
        allTimeUsernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        allTimeScoreColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getScore()).asObject());

        // Monthly leaderboard
        monthlyRankColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getRank()).asObject());
        monthlyUsernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        monthlyScoreColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getScore()).asObject());

        // Weekly leaderboard
        weeklyRankColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getRank()).asObject());
        weeklyUsernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        weeklyScoreColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getScore()).asObject());
    }

    private void loadLeaderboardData() {
        // Load data asynchronously to avoid blocking the UI thread
        CompletableFuture.runAsync(() -> {
            // All-time leaderboard
            List<Map<String, Object>> allTimeData = leaderboardService.getAllTimeLeaderboard();
            ObservableList<LeaderboardEntry> allTimeEntries = convertToLeaderboardEntries(allTimeData);

            // Monthly leaderboard
            List<Map<String, Object>> monthlyData = leaderboardService.getMonthlyLeaderboard();
            ObservableList<LeaderboardEntry> monthlyEntries = convertToLeaderboardEntries(monthlyData);

            // Weekly leaderboard
            List<Map<String, Object>> weeklyData = leaderboardService.getWeeklyLeaderboard();
            ObservableList<LeaderboardEntry> weeklyEntries = convertToLeaderboardEntries(weeklyData);

            // Update UI on JavaFX application thread
            Platform.runLater(() -> {
                allTimeTable.setItems(allTimeEntries);
                monthlyTable.setItems(monthlyEntries);
                weeklyTable.setItems(weeklyEntries);

                // Apply fade-in animation to tables
                applyFadeInAnimation(allTimeTable);
                applyFadeInAnimation(monthlyTable);
                applyFadeInAnimation(weeklyTable);
            });
        });
    }

    private void applyFadeInAnimation(TableView<?> tableView) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), tableView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private ObservableList<LeaderboardEntry> convertToLeaderboardEntries(List<Map<String, Object>> data) {
        ObservableList<LeaderboardEntry> entries = FXCollections.observableArrayList();

        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> entry = data.get(i);
            String username = (String) entry.get("username");
            Integer score = entry.get("score") instanceof Integer ? 
                (Integer) entry.get("score") : 
                Integer.parseInt(entry.get("score").toString());

            entries.add(new LeaderboardEntry(i + 1, username, score));
        }

        return entries;
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(mainMenuResource.getURL());
            loader.setControllerFactory(applicationContext::getBean);
            Parent mainMenuView = loader.load();

            // Get current stage and scene
            Stage stage = (Stage) allTimeTable.getScene().getWindow();
            Scene currentScene = stage.getScene();

            // Set background image
            String style = "-fx-background-image: url('" + "/images/background.png" + "');" +
                "-fx-background-size: cover;" +
                "-fx-background-position: center;";
            mainMenuView.setStyle(style);

            // Replace the root of the existing scene instead of creating a new one
            currentScene.setRoot(mainMenuView);

        } catch (IOException e) {
            log.error("Error navigating back to main menu", e);
        }
    }

    // Inner class to represent a leaderboard entry
    public static class LeaderboardEntry {
        private final int rank;
        private final String username;
        private final int score;

        public LeaderboardEntry(int rank, String username, int score) {
            this.rank = rank;
            this.username = username;
            this.score = score;
        }

        public int getRank() {
            return rank;
        }

        public String getUsername() {
            return username;
        }

        public int getScore() {
            return score;
        }
    }
}
