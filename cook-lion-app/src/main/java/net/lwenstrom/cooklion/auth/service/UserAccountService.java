package net.lwenstrom.cooklion.auth.service;

import net.lwenstrom.cooklion.auth.model.UserAccount;
import net.lwenstrom.cooklion.auth.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAccountService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userAccountRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public Optional<UserAccount> findByUsername(String username) {
        return userAccountRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<UserAccount> findByEmail(String email) {
        return userAccountRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userAccountRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userAccountRepository.existsByEmail(email);
    }

    public UserAccount createUser(UserAccount userAccount) {
        userAccount.setPasswordHash(passwordEncoder.encode(userAccount.getPasswordHash()));
        return userAccountRepository.save(userAccount);
    }

    public void updateLastLogin(UserAccount user) {
        user.setLastLoginAt(Instant.now());
        user.setFailedLoginAttempts(0);
        userAccountRepository.save(user);
    }

    public void incrementFailedLoginAttempts(String username) {
        userAccountRepository.findByUsernameOrEmail(username, username)
                .ifPresent(user -> {
                    user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
                    if (user.getFailedLoginAttempts() >= 5) {
                        user.setAccountNonLocked(false);
                    }
                    userAccountRepository.save(user);
                });
    }
}
