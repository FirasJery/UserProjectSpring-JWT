package sprintone.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import sprintone.entities.CustomUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<CustomUser, Long> {
    Optional<CustomUser> findByUserName(String username);

    CustomUser findByEmail(String email);
}