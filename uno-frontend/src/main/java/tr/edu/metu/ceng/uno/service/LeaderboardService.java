package tr.edu.metu.ceng.uno.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LeaderboardService {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl;

    public LeaderboardService(RestTemplate restTemplate, @Value("${api.base-url}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
    }

    /**
     * Fetches the all-time leaderboard data
     * 
     * @return A list of leaderboard entries, or an empty list if the request fails
     */
    public List<Map<String, Object>> getAllTimeLeaderboard() {
        String url = apiBaseUrl + "/leaderboard";
        return fetchLeaderboardData(url);
    }

    /**
     * Fetches the weekly leaderboard data
     * 
     * @return A list of leaderboard entries, or an empty list if the request fails
     */
    public List<Map<String, Object>> getWeeklyLeaderboard() {
        String url = apiBaseUrl + "/leaderboard/weekly";
        return fetchLeaderboardData(url);
    }

    /**
     * Fetches the monthly leaderboard data
     * 
     * @return A list of leaderboard entries, or an empty list if the request fails
     */
    public List<Map<String, Object>> getMonthlyLeaderboard() {
        String url = apiBaseUrl + "/leaderboard/monthly";
        return fetchLeaderboardData(url);
    }

    /**
     * Helper method to fetch leaderboard data from a given URL
     * 
     * @param url The URL to fetch data from
     * @return A list of leaderboard entries, or an empty list if the request fails
     */
    private List<Map<String, Object>> fetchLeaderboardData(String url) {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed to fetch leaderboard data: {}", e.getMessage(), e);
        }

        return Collections.emptyList();
    }
}
