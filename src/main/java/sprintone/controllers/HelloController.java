package sprintone.controllers;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import sprintone.entities.Credentials;
import sprintone.entities.CustomUser;
import sprintone.services.EmailSender;
import sprintone.services.JwtService;
import sprintone.services.UserService;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class HelloController {
    private final UserService userService ;
    private final AuthenticationManager authenticationManager ;
    private final JwtService jwtService ;
    private final PasswordEncoder passwordEncoder ;
    private final EmailSender emailSender ;

    @PostMapping("/register")
    public CustomUser register(@RequestBody CustomUser user)
    {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return this.userService.addUser(user);
    }
   @PostMapping("/authenticate")
   public ResponseEntity<Map<String, String>> AuthenticateAndGenerateToken(@RequestBody Credentials credentials) {
       Authentication authentication = authenticationManager
               .authenticate(new UsernamePasswordAuthenticationToken(credentials.username(), credentials.password()));
       if (authentication.isAuthenticated()) {
           UserDetails userDetails = userService.loadUserByUsername(credentials.username());
           Map<String, String> tokens = jwtService.generateTokens(userDetails); // Call generateTokens method
           return ResponseEntity.ok(tokens);
       } else {
           return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Return 403 Forbidden for invalid credentials
       }
   }
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody String refreshToken) {
        String username = jwtService.extractUsername(refreshToken); // Extract username from refresh token
        if (username != null && jwtService.isTokenValid(refreshToken)) { // Validate refresh token
            Map<String, String> NewTokens = jwtService.generateTokens(userService.loadUserByUsername(username)); // Generate new access token
            return ResponseEntity.ok(NewTokens);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Return 403 Forbidden for invalid refresh token
        }
    }

    @GetMapping("/sendMail")
    public void sendMail() {
        emailSender.sendEmail("firas.eljary@esprit.tn", "Title", "Massage");
    }

    @PostMapping("reset/resetPassword")
    public String resetPassword(HttpServletRequest request,
                                         @RequestParam("email") String userEmail) {
        CustomUser user = userService.findByEmail(userEmail);
        if (user == null)
        {
            return "user not found";
        }
        String token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(user, token);
        //String url = "/changePassword?token=" + token;
        try {
            emailSender.sendEmail(userEmail, "Reset Password", token);
        }
        catch (MailException e)
        {
           return "Error while sending mail";
        }
        return "mail sent succesfully";
    }

    @GetMapping("reset/checkResetToken")
    public Boolean checkResetToken(@RequestParam("token") String token,@RequestParam String email)
    {
        return userService.validatePasswordResetToken(token , email);
    }

    @PutMapping("reset/changePassword")
    public String changePassword(@RequestParam("token") String token,@RequestParam String email,@RequestParam String password)
    {
        if ((userService.validatePasswordResetToken(token , email)))
        {
            CustomUser user = userService.findByEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            userService.addUser(user);
            return "password changed successfully";
        }
        return "invalid token";
    }



}
