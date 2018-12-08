package younes.sportswatch.repository;

import org.springframework.data.repository.CrudRepository;

import younes.sportswatch.model.User;
import younes.sportswatch.model.Team;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface TeamRepository extends CrudRepository<Team, Integer> {
    public Team findByTeamId(int id);
}