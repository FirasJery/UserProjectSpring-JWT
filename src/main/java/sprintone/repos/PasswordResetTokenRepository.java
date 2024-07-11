package sprintone.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import sprintone.entities.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);
}