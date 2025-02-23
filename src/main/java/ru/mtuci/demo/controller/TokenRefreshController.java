package ru.mtuci.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.demo.model.*;
import ru.mtuci.demo.service.impl.TokenService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TokenRefreshController {

    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<?> login(@RequestBody TokenRefreshRequest request) {
        try {

            TokenResponse tokenResponse = tokenService.refreshTokenPair(request.getDeviceId(), request.getRefreshToken());

            return ResponseEntity.ok(tokenResponse);
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }
}