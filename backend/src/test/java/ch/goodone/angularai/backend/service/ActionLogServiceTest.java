package ch.goodone.angularai.backend.service;

import ch.goodone.angularai.backend.dto.ActionLogDTO;
import ch.goodone.angularai.backend.model.ActionLog;
import ch.goodone.angularai.backend.repository.ActionLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionLogServiceTest {

    @Mock
    private ActionLogRepository actionLogRepository;

    @InjectMocks
    private ActionLogService actionLogService;

    @Mock
    private IpLocationService ipLocationService;

    @Test
    void logLogin_shouldSaveActionLog() {
        IpLocationService.GeoLocation loc = new IpLocationService.GeoLocation();
        loc.setCountry("CH");
        loc.setCity("Zurich");
        loc.setLatitude(47.0);
        loc.setLongitude(8.0);
        when(ipLocationService.lookup(anyString())).thenReturn(loc);
        actionLogService.logLogin("user", "127.0.0.1", "Mozilla/5.0");
        verify(actionLogRepository).save(any(ActionLog.class));
    }

    @Test
    void log_shouldSaveActionLog() {
        actionLogService.log("user", "ACTION", "Details");
        verify(actionLogRepository).save(any(ActionLog.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"login", "task", "user admin", "all"})
    void getLogs_shouldFilterByTypes(String type) {
        Pageable pageable = PageRequest.of(0, 10);
        when(actionLogRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        actionLogService.getLogs(pageable, type, null, null);
        verify(actionLogRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getLogs_withSpecificTypes() {
        Pageable pageable = PageRequest.of(0, 10);
        when(actionLogRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        actionLogService.getLogs(pageable, "login", null, null);
        actionLogService.getLogs(pageable, "task", null, null);
        actionLogService.getLogs(pageable, "user admin", null, null);
        
        verify(actionLogRepository, times(3)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getLogs_shouldFilterByDate() {
        Pageable pageable = PageRequest.of(0, 10);
        when(actionLogRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        LocalDateTime now = LocalDateTime.now();
        actionLogService.getLogs(pageable, "all", now, now.plusDays(1));
        verify(actionLogRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void clearLogs_shouldDeleteAll() {
        actionLogService.clearLogs();
        verify(actionLogRepository).deleteAll();
    }

    @Test
    void createLog_shouldSaveAndReturnDTO() {
        ActionLogDTO dto = new ActionLogDTO(null, LocalDateTime.now(), "user", "ACTION", "Details");
        ActionLog savedLog = new ActionLog("user", "ACTION", "Details");
        savedLog.setId(1L);
        savedLog.setTimestamp(dto.getTimestamp());

        when(actionLogRepository.save(any(ActionLog.class))).thenReturn(savedLog);

        ActionLogDTO result = actionLogService.createLog(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLogin()).isEqualTo("user");
    }
}
