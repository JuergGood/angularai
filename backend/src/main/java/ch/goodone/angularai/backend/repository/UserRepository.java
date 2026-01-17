package ch.goodone.angularai.backend.repository;

import ch.goodone.angularai.backend.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    Optional<User> findByEmail(String email);
    List<User> findAllByOrderByIdDesc(Pageable pageable);
    long countByCreatedAtAfter(java.time.LocalDateTime timestamp);
}
