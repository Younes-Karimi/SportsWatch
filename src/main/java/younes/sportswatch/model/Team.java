package younes.sportswatch.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

@Entity // This tells Hibernate to make a table out of this class
@Data
public class Team {
    @Id
//    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer teamId;
    private String teamName;
    private String city;
    private String abbreviation;
    private int numOfWins;
    private int numOfLosses;

    public Team() {}

    public Team(Integer teamId, String teamName, String city, String abbreviation) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.city = city;
        this.abbreviation = abbreviation;
    }

//    public Integer getTeamId() {
//        return teamId;
//    }
//
//    public void setTeamId(Integer teamId) {
//        this.teamId = teamId;
//    }
//
//    public String getTeamName() {
//        return teamName;
//    }
//
//    public void setTeamName(String teamName) {
//        this.teamName = teamName;
//    }
//
//    public String getAbbreviation() {
//        return abbreviation;
//    }
//
//    public void setAbbreviation(String abbreviation) {
//        this.abbreviation = abbreviation;
//    }
}