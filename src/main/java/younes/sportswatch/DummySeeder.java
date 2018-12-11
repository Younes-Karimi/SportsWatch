package younes.sportswatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.io.IOException;
import younes.sportswatch.model.User;
import younes.sportswatch.model.Team;
import younes.sportswatch.repository.UserRepository;
import younes.sportswatch.repository.TeamRepository;

@Component
public class DummySeeder implements CommandLineRunner {

    private TeamRepository teamRepository;
    private UserRepository userRepository;
    private String tokenPass = "95aecd0b-7284-4bd4-8a0d-336b1f:I9t3kMuslj@9q8Rr";

    public DummySeeder(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadTeams();
        loadUsers();
    }

    //#### Add Dummy Data ####
    private void loadTeams(){
        String url ="https://api.mysportsfeeds.com/v1.2/pull/nba/2018-2019-regular/overall_team_standings.json";
        String encoding = Base64.getEncoder().encodeToString(tokenPass.getBytes());
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
                    System.out.println(team.getTeamId() + team.getTeamName() + team.getCity() + team.getAbbreviation());
                    teamRepository.save(team);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUsers(){
        User admin1 = new User(101942267522225L, "Maria Alcaaiceeejgg Seligsteinberg", "qzbdufdjjw_1544498619@tfbnw.net");
        admin1.setIsAdmin(true);
        userRepository.save(admin1);

        User admin2 = new User(104399223940474L, "Rick Alcaaaahiifcj Narayananson", "frfrgghmdh_1544498608@tfbnw.net");
        admin2.setIsAdmin(true);
        userRepository.save(admin2);

        User user1 = new User(110026253372652L, "Daniel Alcjiehcdjdjc Smithman", "zyhoknnyeu_1544498614@tfbnw.net");
        userRepository.save(user1);

        User user2 = new User(112942529745642L, "Karen Alcjhifhgbdei Vijayvergiyason", "qltxqkpxwo_1544498625@tfbnw.net");
        userRepository.save(user2);

        User user3 = new User(120373335655706L, "Open Graph Test User", "open_jhjaasq_user@tfbnw.net");
        userRepository.save(user3);

        User Younes = new User(1L, "Younes Karimi", "uka.fbook@gmail.com");
        userRepository.save(Younes);
    }
    //#### Add Dummy Data END ####
}
