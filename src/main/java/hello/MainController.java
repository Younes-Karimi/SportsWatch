package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import hello.User;
import hello.Team;
import hello.UserRepository;
import org.springframework.web.servlet.ModelAndView;
import java.util.*;

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

    @GetMapping("/")
    public String renderIndex(Model model){

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
        return "index";
    }

 	@PostMapping("/save")
 	public String saveFavorites(@RequestParam int userId, @RequestParam String favoriteTeams){

        String splittedTeams[] = favoriteTeams.split(",");
        int teamIDs[] = new int[splittedTeams.length];
        for (int i = 0; i < teamIDs.length; i++){
            teamIDs[i] = Integer.parseInt(splittedTeams[i]);
            System.out.println(teamIDs[i]);
        }
 		User dbUser = userRepository.findByUserId(userId);
        dbUser.setFavoriteTeams(teamIDs);
        userRepository.save(dbUser);
        int count = dbUser.getFavoriteTeams().length;
        for (int i = 0; i < count; i++){
            System.out.println(dbUser.getFavoriteTeams()[i]);
        }
        return "redirect:/";
 	}

	@GetMapping(path="/sel")
	public String selectFavoriteTeam(Model model){
		model.addAttribute("teams", teamRepository.findAll());
		model.addAttribute("userId", 1);
	    return "sel";
	}

    @GetMapping(path="/add-dummy") // Map ONLY GET Requests
    public String addDummyStuff () {

        User activeUser = new User("Younes","ykarimi@albany.com");
        userRepository.save(activeUser);
        activeUserId = activeUser.getUserId();

        List<Team> teams =  new ArrayList<>();
        teams.add(new Team("Washington Wizards", "WAS"));
        teams.add(new Team("Miami Heat", "MIA"));
        teams.add(new Team("Los Angeles Clippers", "LAC"));
        teamRepository.saveAll(teams);
        return "redirect:/";
    }
	
	@GetMapping(path="/all")
	public @ResponseBody Iterable<User> getAllUsers() {
		// This returns a JSON or XML with the users
		return userRepository.findAll();
	}

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