package ru.mtuci.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.demo.model.ApplicationRole;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.model.RegisterRequest;
import ru.mtuci.demo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            String email = request.getEmail();

            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("This email is already in use.");
            }

            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            ApplicationUser newUser = new ApplicationUser();
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setRole(ApplicationRole.USER);
            newUser.setUsername(request.getUsername());

            userRepository.save(newUser);

            return ResponseEntity.status(HttpStatus.CREATED).body("Successful registration.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Oops, something went wrong....");
        }
    }
}
