package younes.sportswatch.controller;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.io.IOException;
import younes.sportswatch.model.User;
import younes.sportswatch.model.Team;
import younes.sportswatch.repository.UserRepository;
import younes.sportswatch.repository.TeamRepository;

@Controller    // This means that this class is a Controller
//@RequestMapping(path="/demo") // This means URL's start with /demo (after Application path)
@RequestMapping(path="") // This means URL's start with /demo (after Application path)
public class MainController {
	@Autowired // This means to get the bean called userRepository
	           // Which is auto-generated by Spring, we will use it to handle the data
	private UserRepository userRepository;

	@Autowired
	private TeamRepository teamRepository;

	private int activeUserId;
	private String tokenPass = "95aecd0b-7284-4bd4-8a0d-336b1f:I9t3kMuslj@9q8Rr";

    @GetMapping("/")
    public String renderHomepage(Model model){
        loadUsers();
        loadTeams();
        return "homepage";
    }

    //#### User Management ####
    @GetMapping("/admin-login")
    public ModelAndView renderAdminLogin(HttpSession session) {
        if (session.getAttribute("email") != null) {
            String currentUserEmail = session.getAttribute("email").toString();
            if (userRepository.findByEmail(currentUserEmail).isPresent()) {
                if (userRepository.findByEmail(currentUserEmail).get().getIsAdmin()) {
                    return new ModelAndView("redirect:admin-dashboard");
                }
            }
        }
        return new ModelAndView("admin-login");
    }

    @PostMapping("/admin-login")
    public ModelAndView adminlogin(
            @RequestParam("userName") String userName,
            @RequestParam("email") String email,
            HttpSession session,
            Model model
    ) {
        if(userRepository.findByEmail(email).isPresent()) {
            if (userRepository.findByEmail(email).get().getIsAdmin()) {
                session.setAttribute("email", email);
                return new ModelAndView("redirect:admin-dashboard");
            }
        }
        String errorMessage = userName + " is not defined as an admin! You can login as a user.";
        model.addAttribute("errorMessage", errorMessage);
        return new ModelAndView("error");
    }

    @GetMapping("/register")
    public ModelAndView renderRegisteration(HttpSession session) {
        return new ModelAndView("register");
    }

    @PostMapping("/register")
    public ModelAndView registerUser(
            @RequestParam("userId") int userId,
            @RequestParam("userName") String userName,
            @RequestParam("email") String email
    ) {
        System.out.println("registered!!!!!!!!!!!!111111");
        if (!userRepository.findByEmail(email).isPresent()) {
            System.out.println("registered!!!!!!!!!!!!22222");

            User newUser = new User(userId, userName, email);
            System.out.println("registered!!!!!!!!!!!!33333");

            userRepository.save(newUser);
            return new ModelAndView("redirect:user-login");
        }
        return new ModelAndView("redirect:user-login");
    }

    // #### User Login ####
    @GetMapping("/user-login")
    public ModelAndView renderUserLogin(HttpSession session) {
        if (session.getAttribute("email") != null) {
            String currentUserEmail = session.getAttribute("email").toString();
            if (userRepository.findByEmail(currentUserEmail).isPresent()) {
                return new ModelAndView("redirect:user-dashboard");
            }
        }
        return new ModelAndView("user-login");
    }

    @PostMapping("/user-login")
    public ModelAndView userLoginrequest(
            @RequestParam("email") String email,
            HttpSession session,
            Model model
    ) {
        if(userRepository.findByEmail(email).isPresent()) {
            session.setAttribute("email", email);
            return new ModelAndView("redirect:user-dashboard");
        }
        String errorMessage = "You have not registered into Sports Watch. You have to register before login.";
        model.addAttribute("errorMessage", errorMessage);
        return new ModelAndView("error");
    }
    // #### User Login End ####

    @GetMapping("/logout")
    public ModelAndView renderLogout(HttpSession session) {
        session.removeAttribute("email");
        return new ModelAndView("redirect:");
    }

    @GetMapping("/user-dashboard")
    public String renderUserDashboard(Model model){

        User activeUser = userRepository.findByUserId(activeUserId);
        model.addAttribute("userName", activeUser.getUserName());
        List <Team> favoriteTeams = new ArrayList <Team>();
        if (activeUser.getFavoriteTeams() != null) {
            for (int teamId : activeUser.getFavoriteTeams()) {
                favoriteTeams.add(teamRepository.findByTeamId(teamId));
            }
            model.addAttribute("activeUserFavoriteTeams", favoriteTeams);
        } else {
            model.addAttribute("activeUserFavoriteTeams", null);
        }
        return "user-dashboard";
    }

// 	@PostMapping("/save-favorite-teams")
// 	public String saveFavorites(@RequestParam int userId, @RequestParam String favoriteTeams){
//
//        String splittedTeams[] = favoriteTeams.split(",");
//        int teamIDs[] = new int[splittedTeams.length];
//        for (int i = 0; i < teamIDs.length; i++){
//            teamIDs[i] = Integer.parseInt(splittedTeams[i]);
//            System.out.println(teamIDs[i]);
//        }
// 		User dbUser = userRepository.findByUserId(userId);
//        dbUser.setFavoriteTeams(teamIDs);
//        userRepository.save(dbUser);
//        int count = dbUser.getFavoriteTeams().length;
//        for (int i = 0; i < count; i++){
//            System.out.println(dbUser.getFavoriteTeams()[i]);
//        }
//        return "redirect:/user-dashboard";
// 	}


	@GetMapping(path="/select-teams")
	public String selectFavoriteTeam(Model model){

		model.addAttribute("teams", teamRepository.findAll());
		model.addAttribute("userId", 1);
	    return "select-teams";
	}

//    @GetMapping(path="/add-dummy") // Map ONLY GET Requests
//    public String addDummyStuff () {
//
//        loadTeams();
//        loadUsers();
//
////        User activeUser = new User(0, "Younes","ykarimi@albany.com");
////        userRepository.save(activeUser);
////        activeUserId = activeUser.getUserId();
////
////        List<Team> teams =  new ArrayList<>();
////        teams.add(new Team("Washington Wizards", "WAS"));
////        teams.add(new Team("Miami Heat", "MIA"));
////        teams.add(new Team("Los Angeles Clippers", "LAC"));
////        teamRepository.saveAll(teams);
//        return "redirect:/";
//    }
	
	@GetMapping(path="/all")
	public @ResponseBody Iterable<User> getAllUsers() {
		// This returns a JSON or XML with the users
		return userRepository.findAll();
	}

    //#### Teams API ####
    //Using PoJo Classes
    @GetMapping("/teams")
    public ModelAndView getTeams() {
        ModelAndView showTeams = new ModelAndView("show-teams");
        showTeams.addObject("name", "Younes");

        //Endpoint to call
        String url ="https://api.mysportsfeeds.com/v1.2/pull/nba/2018-2019-regular/overall_team_standings.json";
        //Encode Username and Password
        String encoding = Base64.getEncoder().encodeToString(tokenPass.getBytes());
        //Add headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic "+encoding);
        HttpEntity<String> request = new HttpEntity<String>(headers);

        //Make the call
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<NBATeamStanding> response = restTemplate.exchange(url, HttpMethod.GET, request, NBATeamStanding.class);
        NBATeamStanding ts = response.getBody();
        System.out.println(ts.toString());
        //Send the object to view
        showTeams.addObject("teamStandingEntries", ts.getOverallteamstandings().getTeamstandingsentries());

        return showTeams;
    }

    //#### Show Team Profile ####
    //Using objectMapper
    @GetMapping("/team")
    public ModelAndView getTeamInfo(
            @RequestParam("id") String teamID
    ) {
        ModelAndView teamInfo = new ModelAndView("team-info");
        ArrayList<HashMap<String, String>> gameDetails = new ArrayList<HashMap<String, String>>();
        String url = "https://api.mysportsfeeds.com/v1.2/pull/nba/2018-2019-regular/team_gamelogs.json?team=" + teamID;
        String encoding = Base64.getEncoder().encodeToString(tokenPass.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic "+encoding);
        HttpEntity<String> request = new HttpEntity<String>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        String str = response.getBody();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(str);
            System.out.println(str);
            System.out.println(root.get("teamgamelogs").get("lastUpdatedOn").asText());
            System.out.println(root.get("teamgamelogs").get("gamelogs").getNodeType());
            JsonNode gamelogs = root.get("teamgamelogs").get("gamelogs");

            if(gamelogs.isArray()) {
                gamelogs.forEach(gamelog -> {
                    JsonNode game = gamelog.get("game");
                    HashMap<String,String> gameDetail = new HashMap<String, String>();
                    gameDetail.put("id", game.get("id").asText());
                    gameDetail.put("date", game.get("date").asText());
                    gameDetail.put("time", game.get("time").asText());
                    gameDetail.put("awayTeam", game.get("awayTeam").get("Abbreviation").asText());
                    gameDetail.put("wins", gamelog.get("stats").get("Wins").get("#text").asText());
                    gameDetail.put("losses", gamelog.get("stats").get("Losses").get("#text").asText());
                    gameDetails.add(gameDetail);

                });
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        teamInfo.addObject("gameDetails", gameDetails);
        teamInfo.addObject("teamDetails", teamRepository.findByTeamId(Integer.parseInt(teamID)));
        return teamInfo;
    }

    @GetMapping("/ranking")
    public ModelAndView showRanking() {
        ModelAndView showRanking = new ModelAndView("ranking");
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

        ArrayList<HashMap<String, String>> rankings = new ArrayList<>();
        try {
            JsonNode root = mapper.readTree(str);
            JsonNode overallTeamStandings = root.get("overallteamstandings").get("teamstandingsentry");
            if (overallTeamStandings.isArray()) {
                overallTeamStandings.forEach(teamDetails -> {
                            JsonNode team = teamDetails.get("team");
                            JsonNode rank = teamDetails.get("rank");
                            HashMap<String,String> teamDetail = new HashMap<>();
                            teamDetail.put("ID",team.get("ID").asText());
                            teamDetail.put("City",team.get("City").asText());
                            teamDetail.put("Name",team.get("Name").asText());
                            teamDetail.put("Abbreviation",team.get("Abbreviation").asText());
                            teamDetail.put("rank", rank.asText());
                            rankings.add(teamDetail);
                        }
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        showRanking.addObject("rankings", rankings);
        return showRanking;
    }
    //#### Show Team Profile End ####

    //####Scoreboard ####
    @GetMapping("/scoreboard")
    public ModelAndView showScoreboard() {

        String forDate = "20181207";
        String url = "https://api.mysportsfeeds.com/v1.2/pull/nba/2018-2019-regular/scoreboard.json?fordate=" + forDate;
        String encoding = Base64.getEncoder().encodeToString(tokenPass.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encoding);
        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        String str = response.getBody();
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<HashMap<String, String>> gameDetails = new ArrayList<>();
        try {
            JsonNode root = mapper.readTree(str);
            JsonNode gameScores = root.get("scoreboard").get("gameScore");
            if (gameScores.isArray()) {
                gameScores.forEach(gameLog -> {
                    JsonNode game = gameLog.get("game");
                    HashMap<String, String> gameDetail = new HashMap<>();
                    gameDetail.put("date", game.get("date").asText());
                    gameDetail.put("time", game.get("time").asText());
                    gameDetail.put("awayTeam", game.get("awayTeam").get("Abbreviation").asText());
                    gameDetail.put("homeTeam", game.get("homeTeam").get("Abbreviation").asText());
                    gameDetail.put("location", game.get("location").asText());
                    if (gameLog.get("isUnplayed").asBoolean()) {
                        gameDetail.put("homeScore", "0");
                        gameDetail.put("awayScore", "0");
                        gameDetail.put("status", "Unplayed");
                    } else {
                        gameDetail.put("homeScore", gameLog.get("homeScore").asText());
                        gameDetail.put("awayScore", gameLog.get("awayScore").asText());
                        if (gameLog.get("isInProgress").asBoolean()) {
                            gameDetail.put("status", "In-progress");
                        } else gameDetail.put("status", "Completed");
                    }
                    gameDetails.add(gameDetail);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ModelAndView scoreboard = new ModelAndView("scoreboard");
        scoreboard.addObject("scoreboard", gameDetails);
        return scoreboard;
    }
    //#### Scoreboard End ####

    //#### Teams API End ####

//    #### Helper Functions ####
//    #### Fetch list of favorite teams using teams ID ####
    public ArrayList<Team> fetchFavoriteTeams(ArrayList<Integer> favoriteTeams) {
        ArrayList<Team> teams = new ArrayList<>();
        if (favoriteTeams != null) {
            for (Integer i : favoriteTeams) {
                teams.add(teamRepository.findByTeamId(i));
            }
            return teams;
        }
        return teams;
    }
//    #### Fetch Favorite Teams End ####

    //#### Add Dummy Data ####
    private void loadTeams(){
        System.out.println("QQQQQQQQQQQQQQQQQQQLoading teams");
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
        User admin1 = new User(0, "admin1", "admin1@gmail.com");
        admin1.setIsAdmin(true);
        userRepository.save(admin1);

        User admin2 = new User(1, "admin2", "admin2@gmail.com");
        admin2.setIsAdmin(true);
        userRepository.save(admin2);

        User user1 = new User(2, "user1", "user1@gmail.com");
        userRepository.save(user1);

        User user2 = new User(2, "user2", "user2@gmail.com");
        userRepository.save(user2);
    }
    //#### Add Dummy Data End ####




//	@GetMapping("/")
//	public ModelAndView renderIndex(){
//
//		User activeUser = userRepository.findByUserId(activeUserId);
//        ModelAndView m = new ModelAndView();
//        m.setViewName("index");
//        m.addObject("userName", activeUser.getUserName());
//        m.addObject("activeUserFavoriteTeams", activeUser.getFavoriteTeams());
//		return m;
//	}

//	@GetMapping(path="/add") // Map ONLY GET Requests
//	public @ResponseBody String addNewUser (@RequestParam String name
//			, @RequestParam String email) {
//		// @ResponseBody means the returned String is the response, not a view name
//		// @RequestParam means it is a parameter from the GET or POST request
//
//		User n = new User();
//		n.setUserName(name);
//		n.setEmail(email);
//		userRepository.save(n);
//		return "Saved";
//	}
}