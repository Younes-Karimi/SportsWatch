package younes.sportswatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestOperations;
import java.util.*;
import younes.sportswatch.model.Team;
import younes.sportswatch.repository.TeamRepository;

@SpringBootApplication
public class SportsWatchWebApplication {

    @Autowired
    private TeamRepository teamRepository;


    public static void main(String[] args) {
        SpringApplication.run(SportsWatchWebApplication.class, args);
    }

    public void run(String... args) {

        List<Team> teams =  new ArrayList<>();

        teams.add(new Team("Washington Wizards", "WAS"));
        teams.add(new Team("Miami Heat", "MIA"));
        teams.add(new Team("Los Angeles Clippers", "LAC"));

        teamRepository.saveAll(teams);
    }
}