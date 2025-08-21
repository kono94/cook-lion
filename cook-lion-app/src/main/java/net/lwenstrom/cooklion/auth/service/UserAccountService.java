package net.lwenstrom.cooklion.auth.service;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.lwenstrom.cooklion.auth.model.UserAccount;
import net.lwenstrom.cooklion.auth.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

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
        return userAccountRepository.save(userAccount);
    }

    public void updateLastLogin(UserAccount user) {
        user.setLastLoginAt(Instant.now());
        userAccountRepository.save(user);
    }
}
