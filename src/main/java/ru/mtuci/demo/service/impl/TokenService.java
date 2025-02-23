package ru.mtuci.demo.service.impl;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.model.SessionStatus;
import ru.mtuci.demo.model.TokenResponse;
import ru.mtuci.demo.model.UserSession;
import ru.mtuci.demo.repository.UserRepository;
import ru.mtuci.demo.repository.UserSessionRepository;

import java.util.*;

@Service
public class TokenService {

    private final UserSessionRepository userSessionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public TokenService(UserSessionRepository userSessionRepository, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.userSessionRepository = userSessionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    public TokenResponse issueTokenPair(String email, Long deviceId, Set<GrantedAuthority> authorities) {
        String accessToken = jwtTokenProvider.createAccessToken(email, authorities);
        String refreshToken = jwtTokenProvider.createRefreshToken(email, deviceId);

        Long currentTimeMillis = System.currentTimeMillis();
        Date accessTokenExpiry = new Date(currentTimeMillis + 5 * 60 * 1000);
        Date refreshTokenExpiry = new Date(currentTimeMillis + 24 * 60 * 60 * 1000);

        UserSession newSession = new UserSession();
        newSession.setEmail(email);
        newSession.setDeviceId(deviceId);
        newSession.setAccessToken(accessToken);
        newSession.setRefreshToken(refreshToken);
        newSession.setAccessTokenExpiry(accessTokenExpiry);
        newSession.setRefreshTokenExpiry(refreshTokenExpiry);
        newSession.setStatus(SessionStatus.ACTIVE);

        userSessionRepository.save(newSession);

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refreshTokenPair(Long deviceId, String refreshToken) {
        UserSession session = userSessionRepository.findByRefreshToken(refreshToken).orElse(null);

        if (session == null || session.getStatus() != SessionStatus.ACTIVE
                || !Objects.equals(session.getDeviceId(), deviceId)) {
            blockAllSessionsForUser(session.getEmail());
            return null;
        }

        session.setStatus(SessionStatus.USED);
        userSessionRepository.save(session);

        ApplicationUser user = userRepository.findByEmail(session.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return issueTokenPair(session.getEmail(), deviceId, user.getRole().getGrantedAuthorities());
    }

    public void blockAllSessionsForUser(String email) {
        List<UserSession> sessions = userSessionRepository.findAllByEmail(email);

        for (UserSession session : sessions) {
            if (session.getStatus() == SessionStatus.ACTIVE) {
                session.setStatus(SessionStatus.REVOKED);
                userSessionRepository.save(session);
            }
        }
    }
}

