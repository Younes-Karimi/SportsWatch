package hello;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity // This tells Hibernate to make a table out of this class
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int userId;
    private String userName;
    private String email;
    private int[] favoriteTeams;

    public User(){}

    public User(String userName, String email){
        this.userName = userName;
        this.email = email;
    }

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int[] getFavoriteTeams() {
		return favoriteTeams;
	}

	public void setFavoriteTeams(int[] favoriteTeams) {
		this.favoriteTeams = favoriteTeams;
	}
    
}

