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
    }
}