package younes.sportswatch.repository;

import org.springframework.data.repository.CrudRepository;
import younes.sportswatch.model.User;
import java.util.*;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface UserRepository extends CrudRepository<User, Integer> {
    User findByUserId(int userId);
    User findByUserName(String userName);
    Optional<User> findByEmail(String userEmail);
}
