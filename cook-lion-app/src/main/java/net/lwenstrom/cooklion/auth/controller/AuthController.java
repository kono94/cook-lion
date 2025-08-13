package net.lwenstrom.cooklion.auth.controller;

import net.lwenstrom.cooklion.auth.dto.AuthRequest;
import net.lwenstrom.cooklion.auth.dto.AuthResponse;
import net.lwenstrom.cooklion.auth.dto.RefreshTokenRequest;
import net.lwenstrom.cooklion.auth.dto.RegisterRequest;
import net.lwenstrom.cooklion.auth.model.RefreshToken;
import net.lwenstrom.cooklion.auth.model.Role;
import net.lwenstrom.cooklion.auth.model.UserAccount;
import net.lwenstrom.cooklion.auth.service.JwtService;
import net.lwenstrom.cooklion.auth.service.RefreshTokenService;
import net.lwenstrom.cooklion.auth.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserAccountService userAccountService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        if (userAccountService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().build();
        }

        if (userAccountService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().build();
        }

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword()); // Will be encoded in service
        user.setRoles(Set.of(Role.USER));

        UserAccount savedUser = userAccountService.createUser(user);

        String accessToken = jwtService.generateAccessToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                savedUser, 
                getClientIP(httpRequest), 
                httpRequest.getHeader("User-Agent")
        );

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getTokenHash()) // This contains the plain token
                .tokenType("Bearer")
                .expiresIn((long) (15 * 60)) // 15 minutes in seconds
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UserAccount user = userAccountService.findByUsername(userDetails.getUsername()).orElseThrow();

            userAccountService.updateLastLogin(user);

            String accessToken = jwtService.generateAccessToken(userDetails);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    user, 
                    getClientIP(httpRequest), 
                    httpRequest.getHeader("User-Agent")
            );

            return ResponseEntity.ok(AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getTokenHash()) // This contains the plain token
                    .tokenType("Bearer")
                    .expiresIn((long) (15 * 60)) // 15 minutes in seconds
                    .build());

        } catch (BadCredentialsException e) {
            userAccountService.incrementFailedLoginAttempts(request.getUsernameOrEmail());
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        return refreshTokenService.findByTokenHash(request.getRefreshToken())
                .filter(RefreshToken::isValid)
                .map(refreshToken -> {
                    RefreshToken newRefreshToken = refreshTokenService.rotateToken(
                            refreshToken,
                            getClientIP(httpRequest),
                            httpRequest.getHeader("User-Agent")
                    );

                    String accessToken = jwtService.generateAccessToken(refreshToken.getUser());

                    return ResponseEntity.ok(AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(newRefreshToken.getTokenHash()) // This contains the plain token
                            .tokenType("Bearer")
                            .expiresIn((long) (15 * 60)) // 15 minutes in seconds
                            .build());
                })
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.findByTokenHash(request.getRefreshToken())
                .ifPresent(refreshTokenService::invalidateToken);
        return ResponseEntity.ok().build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
