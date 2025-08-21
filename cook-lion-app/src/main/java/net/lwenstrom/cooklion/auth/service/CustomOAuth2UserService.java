package net.lwenstrom.cooklion.auth.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lwenstrom.cooklion.auth.model.Role;
import net.lwenstrom.cooklion.auth.model.UserAccount;
import net.lwenstrom.cooklion.auth.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String EMAIL_REQUIRED_ERROR = "email_required";
    private static final String USER_CREATION_ERROR = "user_creation_failed";

    private final UserAccountRepository userAccountRepository;
    private final UserAccountService userAccountService;

    @Value("${app.admin.emails:}")
    private String adminEmails;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            return processOAuth2User(oauth2User);
        } catch (Exception ex) {
            log.error("Failed to load OAuth2 user", ex);
            throw new OAuth2AuthenticationException(new OAuth2Error(USER_CREATION_ERROR), ex.getMessage(), ex);
        }
    }

    private OAuth2User processOAuth2User(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String username = oauth2User.getAttribute("preferred_username");
        String name = oauth2User.getAttribute("name");

        if (!StringUtils.hasText(email)) {
            log.warn("OAuth2 authentication failed: email not provided");
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(EMAIL_REQUIRED_ERROR), "Email address is required for authentication");
        }

        UserAccount user = findOrCreateUser(email, username, name);
        userAccountService.updateLastLogin(user);

        // Convert roles to Spring Security authorities
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());

        log.info(
                "OAuth2 user authenticated: {} with roles: {}",
                email,
                user.getRoles().stream().map(Role::name).collect(Collectors.toList()));

        return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), "email");
    }

    private UserAccount findOrCreateUser(String email, String username, String name) {
        return userAccountRepository.findByEmail(email).orElseGet(() -> createNewUser(email, username, name));
    }

    private UserAccount createNewUser(String email, String username, String name) {
        try {
            UserAccount user = new UserAccount();
            user.setEmail(email);
            user.setUsername(generateUniqueUsername(username, email));
            user.setEnabled(true);

            // Assign roles based on configuration
            assignUserRoles(user, email);

            UserAccount savedUser = userAccountRepository.save(user);

            log.info(
                    "Created new user account: {} with roles: {}",
                    savedUser.getEmail(),
                    savedUser.getRoles().stream().map(Role::name).collect(Collectors.toList()));

            return savedUser;

        } catch (Exception ex) {
            log.error("Failed to create new user for email: {}", email, ex);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(USER_CREATION_ERROR), "Failed to create user account", ex);
        }
    }

    private String generateUniqueUsername(String preferredUsername, String email) {
        String baseUsername =
                StringUtils.hasText(preferredUsername) ? preferredUsername : extractUsernameFromEmail(email);

        // Ensure username uniqueness
        String username = baseUsername;
        int counter = 1;
        while (userAccountRepository.existsByUsername(username)) {
            username = baseUsername + counter++;
        }

        return username;
    }

    private void assignUserRoles(UserAccount user, String email) {
        // Default role for all users
        user.getRoles().add(Role.USER);

        // Admin role assignment based on configuration
        if (isAdminEmail(email)) {
            user.getRoles().add(Role.ADMIN);
            log.info("Admin role assigned to user: {}", email);
        }
    }

    private String extractUsernameFromEmail(String email) {
        return email.substring(0, email.indexOf('@')).replaceAll("[^a-zA-Z0-9]", "");
    }

    private boolean isAdminEmail(String email) {
        if (!StringUtils.hasText(adminEmails)) {
            return false;
        }

        List<String> adminEmailList = Arrays.asList(adminEmails.split(","));
        return adminEmailList.stream().map(String::trim).anyMatch(adminEmail -> adminEmail.equalsIgnoreCase(email));
    }
}
