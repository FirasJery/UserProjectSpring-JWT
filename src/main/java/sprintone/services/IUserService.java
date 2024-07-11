package sprintone.services;

import org.springframework.security.core.userdetails.UserDetailsService;
import sprintone.entities.CustomUser;

public interface IUserService extends UserDetailsService {

    public CustomUser addUser(CustomUser user);

    CustomUser findByEmail(String email);

    void createPasswordResetTokenForUser(CustomUser user, String token);

    Boolean validatePasswordResetToken(String token, String email);



}
