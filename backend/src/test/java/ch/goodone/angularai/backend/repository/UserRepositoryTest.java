package ch.goodone.angularai.backend.repository;

import ch.goodone.angularai.backend.model.Role;
import ch.goodone.angularai.backend.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByLogin_shouldReturnUser_whenUserExists() {
        User user = new User("John", "Doe", "johndoe", "password", "john@example.com", LocalDate.of(1990, 1, 1), "123 Main St", Role.ROLE_USER);
        userRepository.save(user);

        Optional<User> found = userRepository.findByLogin("johndoe");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void findByLogin_shouldReturnEmpty_whenUserDoesNotExist() {
        Optional<User> found = userRepository.findByLogin("nonexistent");

        assertThat(found).isEmpty();
    }
}
