package ch.goodone.angularai.backend.repository;

import ch.goodone.angularai.backend.model.ActionLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Long>, JpaSpecificationExecutor<ActionLog> {
    List<ActionLog> findAllByOrderByTimestampDesc(Pageable pageable);
    long countByTimestampAfter(LocalDateTime timestamp);
    long countByIpAddressAndActionAndTimestampAfter(String ipAddress, String action, LocalDateTime timestamp);
    long countByLoginAndActionAndTimestampAfter(String login, String action, LocalDateTime timestamp);
}
