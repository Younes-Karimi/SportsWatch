package younes.sportswatch.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.ArrayList;

@Data
class team{
    @JsonProperty("ID")
    String ID;
    @JsonProperty("City")
    String City;
    @JsonProperty("Name")
    String Name;
    @JsonProperty("Abbreviation")
    String Abbreviation;
}

@Data
class teamstandingsentry{
    team team;
    Long rank;
}

@Data
class overallteamstandings{
    String lastUpdatedOn;
    @JsonProperty("teamstandingsentry")
    ArrayList<teamstandingsentry> teamstandingsentries;
}

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NBATeamStanding {
    overallteamstandings overallteamstandings;

}