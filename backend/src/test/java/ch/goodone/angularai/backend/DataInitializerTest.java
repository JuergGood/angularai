package ch.goodone.angularai.backend;

import ch.goodone.angularai.backend.repository.TaskRepository;
import ch.goodone.angularai.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataInitializerTest {

    @Test
    void shouldInitializeDataWhenEmpty() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        TaskRepository taskRepository = mock(TaskRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        
        DataInitializer initializer = new DataInitializer();
        CommandLineRunner runner = initializer.initData(userRepository, taskRepository, passwordEncoder);
        
        runner.run();
        
        verify(userRepository, atLeast(2)).save(any());
        verify(taskRepository, atLeast(2)).save(any());
    }

    @Test
    void shouldNotInitializeDataWhenNotEmpty() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        TaskRepository taskRepository = mock(TaskRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(userRepository.count()).thenReturn(10L);

        DataInitializer initializer = new DataInitializer();
        CommandLineRunner runner = initializer.initData(userRepository, taskRepository, passwordEncoder);

        runner.run();

        verify(userRepository, never()).save(any());
        verify(taskRepository, never()).save(any());
    }
}
