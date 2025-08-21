package net.lwenstrom.cooklion.auth.repository;

import java.util.Optional;
import java.util.UUID;
import net.lwenstrom.cooklion.auth.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByEmail(String email);

    @Query("SELECT u FROM UserAccount u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<UserAccount> findByUsernameOrEmail(
            @Param("identifier") String username, @Param("identifier") String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
