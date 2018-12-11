package younes.sportswatch.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.*;
import younes.sportswatch.model.Team;

@Entity // This tells Hibernate to make a table out of this class
@Data
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long userId;
    private String userName;
    private String email;
    private Boolean isBlocked;
    private Boolean isAdmin;
    private ArrayList<Integer> favoriteTeams;

    public User(){}

    public User(Long userId, String userName, String email){
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.isBlocked = false;
        this.isAdmin = false;
    }

//    public boolean getIsAdmin(){
//        return isAdmin;
//    }
//
//    public void setIsAdmin(boolean isAdmin){
//        this.isAdmin = isAdmin;
//    }
}

