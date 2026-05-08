package com.academy.authservice.service;

import com.academy.authservice.config.JwtProperties;
import com.academy.authservice.dto.*;
import com.academy.authservice.model.RefreshToken;
import com.academy.authservice.model.RevokedToken;
import com.academy.authservice.model.User;
import com.academy.authservice.repository.RefreshTokenRepository;
import com.academy.authservice.repository.RevokedTokenRepository;
import com.academy.authservice.repository.RoleRepository;
import com.academy.authservice.repository.UserRepository;
import com.academy.authservice.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       RevokedTokenRepository revokedTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuthenticationManager authenticationManager,
                       JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("Email already registered");
        }

        var role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Default role not found"));

        var user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(role);
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        refreshTokenRepository.revokeAllByUserId(user.getId());

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        var stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (stored.isRevoked() || stored.isExpired()) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildAuthResponse(stored.getUser());
    }

    @Transactional
    public void logout(String email, String accessToken) {
        userRepository.findByEmail(email).ifPresent(user ->
                refreshTokenRepository.revokeAllByUserId(user.getId()));
        if (accessToken != null) {
            revokedTokenRepository.save(new RevokedToken(
                    tokenProvider.getJtiFromToken(accessToken),
                    tokenProvider.getExpirationFromToken(accessToken)
            ));
        }
    }

    public UserDto me(String email) {
        return userRepository.findByEmail(email)
                .map(UserDto::from)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private AuthResponse buildAuthResponse(User user) {
        var accessToken = tokenProvider.generateAccessToken(user.getEmail(), user.getRole().getName());
        var refreshToken = createRefreshToken(user);
        return AuthResponse.of(
                accessToken,
                refreshToken.getToken(),
                tokenProvider.getAccessTokenExpirationMs() / 1000,
                UserDto.from(user)
        );
    }

    private RefreshToken createRefreshToken(User user) {
        var rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiresAt(Instant.now().plusMillis(jwtProperties.refreshTokenExpirationMs()));
        return refreshTokenRepository.save(rt);
    }
}
