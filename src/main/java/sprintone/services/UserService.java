package sprintone.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sprintone.entities.CustomUser;
import sprintone.entities.PasswordResetToken;
import sprintone.repos.PasswordResetTokenRepository;
import sprintone.repos.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordTokenRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CustomUser user = this.userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found"));
        // convert the string roles to list of strings , they are separated by comma
        String[] roles = user.getRoles().split(",");
        return User.builder()
                .username(user.getUserName())
                .password(user.getPassword())
                .roles(roles)
                .build();
    }

    public CustomUser addUser(CustomUser user) {
        return this.userRepository.save(user);
    }

    @Override
    public CustomUser findByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    @Override
    public void createPasswordResetTokenForUser(CustomUser user, String token) {
        // set expire date to 5 minutes
        PasswordResetToken myToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();
        passwordTokenRepository.save(myToken);

    }

    @Override
    public Boolean validatePasswordResetToken(String token, String email) {
        PasswordResetToken passToken = passwordTokenRepository.findByToken(token);
        if (passToken == null) {
            return false;
        }
        if (passToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }
        return passToken.getUser().getEmail().equals(email);
    }




}
