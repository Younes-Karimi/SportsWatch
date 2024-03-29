package younes.sportswatch.controller;

import com.sun.org.apache.bcel.internal.generic.RET;
import com.sun.org.apache.bcel.internal.generic.RETURN;
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
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TeamRepository teamRepository;
	private String tokenPass = "95aecd0b-7284-4bd4-8a0d-336b1f:I9t3kMuslj@9q8Rr";

    @GetMapping("/")
    public String renderHomepage(HttpSession session, Model model){
//        loadUsers();
//        loadTeams();
        return "homepage";
    }

    //#################################################//
    //#### Access Control
    //#################################################//

    @GetMapping("/adminLogin")
    public ModelAndView renderAdminLogin(HttpSession session) {
        if (session.getAttribute("email") != null) {
            String currentUserEmail = session.getAttribute("email").toString();
            if (userRepository.findByEmail(currentUserEmail).isPresent()) {
                if (userRepository.findByEmail(currentUserEmail).get().getIsAdmin()) {
                    return new ModelAndView("redirect:adminDashboard");
                }
            }
        }
        return new ModelAndView("adminLogin");
    }

    @PostMapping("/adminLogin")
    public ModelAndView adminlogin(
            @RequestParam("userName") String userName,
            @RequestParam("email") String email,
            HttpSession session,
            Model model
    ) {
        if(userRepository.findByEmail(email).isPresent()) {
            if (userRepository.findByEmail(email).get().getIsAdmin()) {
                session.setAttribute("email", email);
                return new ModelAndView("redirect:adminDashboard");
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
            @RequestParam("userId") Long userId,
            @RequestParam("userName") String userName,
            @RequestParam("email") String email
    ) {
        if (!userRepository.findByEmail(email).isPresent()) {
            User newUser = new User(userId, userName, email);
            userRepository.save(newUser);
        }
        return new ModelAndView("redirect:userLogin");
    }

    // #### User Login ####
    @GetMapping("/userLogin")
    public ModelAndView renderUserLogin(HttpSession session) {
        if (session.getAttribute("email") != null) {
            String currentUserEmail = session.getAttribute("email").toString();
            if (userRepository.findByEmail(currentUserEmail).isPresent()) {
                return new ModelAndView("redirect:userDashboard");
            }
        }
        return new ModelAndView("userLogin");
    }

    @PostMapping("/userLogin")
    public ModelAndView userLoginrequest(
            @RequestParam("email") String email,
            HttpSession session,
            Model model
    ) {
        if(userRepository.findByEmail(email).isPresent()) {
            session.setAttribute("email", email);
            return new ModelAndView("redirect:userDashboard");
        }
        String errorMessage = "You have not registered into Sports Watch. You have to register before login.";
        model.addAttribute("errorMessage", errorMessage);
        return new ModelAndView("error");
    }
    // #### User Login END ####

    @GetMapping("/logout")
    public ModelAndView renderLogout(HttpSession session) {
        session.removeAttribute("email");
        return new ModelAndView("redirect:");
    }

    //#################################################//
    //#### Access Control END
    //#################################################//

    //#################################################//
    //#### Dashboards
    //#################################################//

    @GetMapping("/userDashboard")
    public ModelAndView renderUserDashboard(HttpSession session, Model model) {
        if (session.getAttribute("email") != null) {
            String currentUserEmail = session.getAttribute("email").toString();
            if (userRepository.findByEmail(currentUserEmail).isPresent()) {
                User currentUser = userRepository.findByEmail(currentUserEmail).get();
                ModelAndView userDashboard = new ModelAndView("userDashboard");
                userDashboard.addObject("userInfo", currentUser);
                if (getUserFavoriteTeams(currentUser.getFavoriteTeams()) != null) {
                    if (!getUserFavoriteTeams(currentUser.getFavoriteTeams()).isEmpty()) {
                        ArrayList<String> notifications = new ArrayList<>();
                        ArrayList<Team> favoriteTeams = getUserFavoriteTeams(currentUser.getFavoriteTeams());
                        if (favoriteTeams != null) {
                            if (!favoriteTeams.isEmpty()) {
                                for (Team team : favoriteTeams) {
                                    notifications.add(updateWinsAndLosses(fetchGameDetails(team.getTeamId().toString()), team.getTeamId()));
                                }
                            }
                        }
                        userDashboard.addObject("notifications", notifications);
                        userDashboard.addObject("favoriteTeams", favoriteTeams);
                    }
                }
                return userDashboard;
            }
        }
        String errorMessage = "Invalid access, please login!";
        model.addAttribute("errorMessage", errorMessage);
        return new ModelAndView("error");
    }

    private ArrayList<Team> getUserFavoriteTeams(ArrayList<Integer> favoriteTeams) {
        ArrayList<Team> teams = new ArrayList<>();
        if (!(favoriteTeams == null)) {
            for (Integer i : favoriteTeams) {
                teams.add(teamRepository.findByTeamId(i));
            }
            return teams;
        }
        return teams;
    }

    private ArrayList<String> updateMessages(ArrayList<Team> teams) {
        ArrayList<String> messages = new ArrayList<>();
        if (teams != null) {
            if (!teams.isEmpty()) {
                for (Team t : teams) {
                    messages.add(updateWinsAndLosses(fetchGameDetails(t.getTeamId().toString()), t.getTeamId()));
                }
            }
        }
        return messages;
    }

    // Update team info in team repository
    private String updateWinsAndLosses(ArrayList<HashMap<String, String>> gameDetails, int teamId) {
        int numOfWins = 0;
        int numOfLosses = 0;
        for (HashMap<String, String> entry: gameDetails) {
            for (String key : entry.keySet()) {
                if ((key.compareTo("Wins") == 0) && entry.get(key).matches("1")) {
                    numOfWins++;
                } else if ((key.compareTo("Losses") == 0) && entry.get(key).matches("1")) {
                    numOfLosses++;
                }
            }
        }
        Team currentTeam = teamRepository.findByTeamId(teamId);
        if ((currentTeam.getNumOfWins() == numOfWins) && (currentTeam.getNumOfLosses() == numOfLosses)) {
            return null;
        } else {
            currentTeam.setNumOfWins(numOfWins);
            currentTeam.setNumOfLosses(numOfLosses);
            teamRepository.save(currentTeam);
            return currentTeam.getAbbreviation() + " has an update since your last visit! Wins: " + numOfWins + " -- Losses: " + numOfLosses;
        }
    }

    @GetMapping("/adminDashboard")
    public ModelAndView renderAdminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("email") != null) {
            String currentUserEmail = session.getAttribute("email").toString();
            if (userRepository.findByEmail(currentUserEmail).isPresent()) {
                if (userRepository.findByEmail(currentUserEmail).get().getIsAdmin()) {
                    ModelAndView allUsers = new ModelAndView("adminDashboard");
                    allUsers.addObject("allUsers", userRepository.findAll());
                    return allUsers;
                }
            }
        }
        String errorMessage = "Invalid login credentials for an application administrator!";
        model.addAttribute("errorMessage", errorMessage);
        return new ModelAndView("error");
    }

    @GetMapping("/toggleUserStatus")
    public ModelAndView toggleUserStatus(
            @RequestParam("email") String email
    ) {
        if(userRepository.findByEmail(email).isPresent()) {
            User user = userRepository.findByEmail(email).get();
            user.setIsBlocked(!(user.getIsBlocked()));
            userRepository.save(user);
        }
        return new ModelAndView("redirect:adminDashboard");
    }

    //#################################################//
    //#### Dashboards END
    //#################################################//

    //#################################################//
    //#### Favorite Teams
    //#################################################//

	@GetMapping(path="/selectTeams")
	public String selectFavoriteTeam(Model model){

		model.addAttribute("teams", teamRepository.findAll());
		model.addAttribute("userId", 1);
	    return "selectTeams";
	}

    @GetMapping("/selectFavoriteTeams")
    public ModelAndView fetchAllTeams(HttpSession session) {
        if (session.getAttribute("email") != null) {
            String currentUserEmail = session.getAttribute("email").toString();
            if (userRepository.findByEmail(currentUserEmail).isPresent()) {
                ModelAndView allTeams = new ModelAndView("selectFavoriteTeams");
                allTeams.addObject("allTeams", teamRepository.findAll());
                return allTeams;
            }
        }
        return new ModelAndView("redirect:userLogin");
    }

    @PostMapping("/selectFavoriteTeams")
    public ModelAndView addToFavorites(
            HttpSession session,
            @RequestParam String favoriteTeamsString,
            Model model
    ) {
        String splitTeams[] = favoriteTeamsString.split(",");
        ArrayList<Integer> favoriteTeams = new ArrayList<>();
        for (String str: splitTeams) {
            favoriteTeams.add(Integer.parseInt(str));
        }
        if (favoriteTeams.isEmpty() == false) {
            for (int team : favoriteTeams) {
                setNumOfWinsAndLosses(team);
            }
        }

        if (session.getAttribute("email") != null) {
            String currentUserEmail = session.getAttribute("email").toString();
            if (userRepository.findByEmail(currentUserEmail).isPresent()) {
                User user = userRepository.findByEmail(currentUserEmail).get();
                user.setFavoriteTeams(favoriteTeams);
                userRepository.save(user);
                return new ModelAndView("redirect:userDashboard");
            }
        }
        String errorMessage = "You have to login before choosing favorite teams!";
        model.addAttribute("errorMessage", errorMessage);
        return new ModelAndView("error");
    }
//
    @GetMapping("/addToFavorites")
    public ModelAndView addTeamToFavorites(
            @RequestParam("id") String teamID,
            HttpSession session, Model model
    ) {
        if (session.getAttribute("email") != null) {
            String currentUserEmail = session.getAttribute("email").toString();
            if (userRepository.findByEmail(currentUserEmail).isPresent()) {
                User currentUser = userRepository.findByEmail(currentUserEmail).get();
                if (currentUser.getFavoriteTeams() != null) {
                    ArrayList<Integer> oldFavTeams = currentUser.getFavoriteTeams();
                    setNumOfWinsAndLosses(Integer.parseInt(teamID));
                    oldFavTeams.add(Integer.parseInt(teamID));
                    currentUser.setFavoriteTeams(oldFavTeams);
                    userRepository.save(currentUser);
                } else {
                    ArrayList<Integer> newFavTeams = new ArrayList<>();
                    newFavTeams.add(Integer.parseInt(teamID));
                    currentUser.setFavoriteTeams(newFavTeams);
                    userRepository.save(currentUser);
                }
                return new ModelAndView("redirect:userDashboard");
            }
        }
        String errorMessage = "You have to login to be able to add a team to your favorites!";
        model.addAttribute("errorMessage", errorMessage);
        return new ModelAndView("error");
    }

    private void setNumOfWinsAndLosses(int teamId) {
        Team team = teamRepository.findByTeamId(teamId);
        ArrayList<HashMap<String, String>> gameDetails = fetchGameDetails(team.getTeamId().toString());
        int numOfWins = 0;
        int numOfLosses = 0;
        for (HashMap<String,String> entry: gameDetails) {
            for (String key : entry.keySet()) {
                if ((key.compareTo("Wins") == 0) && entry.get(key).matches("1")) {
                    numOfWins++;
                } else if((key.compareTo("Losses") == 0) && entry.get(key).matches("1")) {
                    numOfLosses++;
                }
            }
        }
        team.setNumOfWins(numOfWins);
        team.setNumOfLosses(numOfLosses);
    }

    //#################################################//
    //#### Favorite Teams END
    //#################################################//

    //#################################################//
    //#### Teams API
    //#################################################//
    //Using PoJo Classes
    @GetMapping("/teams")
    public ModelAndView getTeams() {
        ModelAndView showTeams = new ModelAndView("showTeams");
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
        ModelAndView teamInfo = new ModelAndView("teamInfo");
        ArrayList<HashMap<String, String>> gameDetails = fetchGameDetails(teamID);
        teamInfo.addObject("gameDetails", gameDetails);
        teamInfo.addObject("teamDetails", teamRepository.findByTeamId(Integer.parseInt(teamID)));
        return teamInfo;
    }

    private ArrayList<HashMap<String, String>> fetchGameDetails(String teamID){
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
            JsonNode gamelogs = root.get("teamgamelogs").get("gamelogs");

            if(gamelogs.isArray()) {
                gamelogs.forEach(gamelog -> {
                    JsonNode game = gamelog.get("game");
                    HashMap<String,String> gameDetail = new HashMap<String, String>();
                    gameDetail.put("id", game.get("id").asText());
                    gameDetail.put("date", game.get("date").asText());
                    gameDetail.put("time", game.get("time").asText());
                    gameDetail.put("awayTeam", game.get("awayTeam").get("Abbreviation").asText());
                    gameDetail.put("Wins", gamelog.get("stats").get("Wins").get("#text").asText());
                    gameDetail.put("Losses", gamelog.get("stats").get("Losses").get("#text").asText());
                    gameDetails.add(gameDetail);
                });
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return gameDetails;
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
    //#### Show Team Profile END ####

    //#### Scoreboard ####
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
    //#### Scoreboard END ####

    //#################################################//
    //#### Teams API END ####
    //#################################################//

    //#################################################//
    //#### Helper Functions
    //#################################################//

    //#### Fetch list of favorite teams using teams ID ####
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
    //#### Fetch Favorite Teams END ####


    //#################################################//
    //#### Helper Functions END
    //#################################################//

    //#################################################//
    //#### Old Functions
    //#################################################//

//    @GetMapping(path="/all")
//    public @ResponseBody Iterable<User> getAllUsers() {
//        // This returns a JSON or XML with the users
//        return userRepository.findAll();
//    }

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