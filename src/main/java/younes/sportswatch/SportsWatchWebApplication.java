package younes.sportswatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.util.*;
import younes.sportswatch.model.Team;
import younes.sportswatch.model.User;
import younes.sportswatch.repository.TeamRepository;
import younes.sportswatch.repository.UserRepository;

@SpringBootApplication
public class SportsWatchWebApplication {

    @Autowired
    private TeamRepository teamRepository;
    private UserRepository userRepository;


    public static void main(String[] args) {
        SpringApplication.run(SportsWatchWebApplication.class, args);
    }

    public void run(String... args) throws Exception {
        loadTeams();
        loadUsers();
    }

    private void loadTeams(){
        String url ="https://api.mysportsfeeds.com/v1.2/pull/nba/2018-2019-regular/overall_team_standings.json";
        String encoding = Base64.getEncoder().encodeToString("95aecd0b-7284-4bd4-8a0d-336b1f:I9t3kMuslj@9q8Rr".getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encoding);
        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        String str = response.getBody();
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(str);
            JsonNode teamstandings = root.get("overallteamstandings").get("teamstandingsentry");
            if (teamstandings.isArray()) {
                teamstandings.forEach(teamDetails -> {
                    JsonNode teamInfo = teamDetails.get("team");
                    Team team = new Team();
                    team.setTeamId(Integer.parseInt(teamInfo.get("ID").asText()));
                    team.setTeamName(teamInfo.get("Name").asText());
                    team.setCity(teamInfo.get("City").asText());
                    team.setAbbreviation(teamInfo.get("Abbreviation").asText());
                    teamRepository.save(team);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUsers(){
        User admin1 = new User(0L, "admin1", "admin1@gmail.com");
        admin1.setIsAdmin(true);
        userRepository.save(admin1);

        User admin2 = new User(1L, "admin2", "admin2@gmail.com");
        admin2.setIsAdmin(true);
        userRepository.save(admin2);

        User user1 = new User(2L, "user1", "user1@gmail.com");
        userRepository.save(user1);

        User user2 = new User(2L, "user2", "user2@gmail.com");
        userRepository.save(user2);
    }
}