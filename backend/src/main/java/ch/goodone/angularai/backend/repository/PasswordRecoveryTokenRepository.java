package ch.goodone.angularai.backend.repository;

import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.model.PasswordRecoveryToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordRecoveryTokenRepository extends JpaRepository<PasswordRecoveryToken, Long> {
    Optional<PasswordRecoveryToken> findByToken(String token);
    void deleteByUser(User user);
}
