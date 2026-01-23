package ch.goodone.angularai.backend.service;

import ch.goodone.angularai.backend.model.Priority;
import ch.goodone.angularai.backend.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TaskParserServiceTest {

    private final TaskParserService service = new TaskParserService();

    @Test
    void shouldParseGermanSimple() {
        var result = service.parse("kaufe milch heute");
        assertThat(result.title()).isEqualTo("kaufe milch");
        assertThat(result.dueDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldParseGermanWithSeparators() {
        var result = service.parse("Geburtstag Jürg, kaufe kuchen, 25.10.2026");
        assertThat(result.title()).isEqualTo("Geburtstag Jürg");
        assertThat(result.description()).isEqualTo("kaufe kuchen");
        assertThat(result.dueDate()).isEqualTo(LocalDate.of(2026, 10, 25));
    }

    @Test
    void shouldHandleCommaInTitleWithSpaceHeuristic() {
        var result = service.parse("Kaufe Brot, Milch morgen");
        assertThat(result.title()).isEqualTo("Kaufe Brot, Milch");
        assertThat(result.dueDate()).isEqualTo(LocalDate.now().plusDays(1));
    }

    @Test
    void shouldHandleCommaInTitleWithCsvMetadata() {
        var result = service.parse("Meeting, Important project, tomorrow, HIGH, OPEN");
        assertThat(result.title()).isEqualTo("Meeting");
        assertThat(result.description()).isEqualTo("Important project");
        assertThat(result.dueDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(result.priority()).isEqualTo(Priority.HIGH);
        assertThat(result.status()).isEqualTo(TaskStatus.OPEN);
    }

    @Test
    void shouldParseComplexGermanSpaceHeuristic() {
        var result = service.parse("Präsentation GoodOne Organisiere Teams Meeting 12.1.2026 wichtig archiv");
        assertThat(result.title()).isEqualTo("Präsentation GoodOne Organisiere Teams Meeting");
        assertThat(result.dueDate()).isEqualTo(LocalDate.of(2026, 1, 12));
        assertThat(result.priority()).isEqualTo(Priority.HIGH);
        assertThat(result.status()).isEqualTo(TaskStatus.ARCHIVED);
    }

    @Test
    void shouldParseAnotherComplexGermanSpaceHeuristic() {
        var result = service.parse("Hochzeitstag Schatz kritisch kaufe Blumen in 5 tagen");
        assertThat(result.title()).isEqualTo("Hochzeitstag Schatz kaufe Blumen");
        assertThat(result.dueDate()).isNotNull();
        assertThat(result.priority()).isEqualTo(Priority.CRITICAL);
    }

    @Test
    void shouldSupportNewSynonyms() {
        assertThat(service.parse("task hoch").priority()).isEqualTo(Priority.HIGH);
        assertThat(service.parse("task dringend").priority()).isEqualTo(Priority.HIGH);
        assertThat(service.parse("task wichtig").priority()).isEqualTo(Priority.HIGH);
        assertThat(service.parse("task tief").priority()).isEqualTo(Priority.LOW);
        assertThat(service.parse("task unwichtig").priority()).isEqualTo(Priority.LOW);
        assertThat(service.parse("task niedrig").priority()).isEqualTo(Priority.LOW);
        assertThat(service.parse("task progress").status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(service.parse("task hängig").status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(service.parse("task pendent").status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void shouldParseSimpleTitle() {
        var result = service.parse("Buy milk");
        assertThat(result.title()).isEqualTo("Buy milk");
        assertThat(result.priority()).isEqualTo(Priority.MEDIUM);
        assertThat(result.status()).isEqualTo(TaskStatus.OPEN);
        assertThat(result.dueDate()).isNull();
    }

    @Test
    void shouldParseWithSeparators() {
        var result = service.parse("Buy milk | Grocery store | 2026-01-25 | HIGH | OPEN");
        assertThat(result.title()).isEqualTo("Buy milk");
        assertThat(result.description()).isEqualTo("Grocery store");
        assertThat(result.dueDate()).isEqualTo(LocalDate.of(2026, 1, 25));
        assertThat(result.priority()).isEqualTo(Priority.HIGH);
        assertThat(result.status()).isEqualTo(TaskStatus.OPEN);
    }

    @Test
    void shouldParseSpaceHeuristic() {
        var result = service.parse("Meeting tomorrow HIGH IN_PROGRESS");
        assertThat(result.title()).isEqualTo("Meeting");
        assertThat(result.dueDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(result.priority()).isEqualTo(Priority.HIGH);
        assertThat(result.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void shouldParseRelativeDateXDays() {
        var result = service.parse("Call Bob 5 days");
        assertThat(result.title()).isEqualTo("Call Bob");
        assertThat(result.dueDate()).isEqualTo(LocalDate.now().plusDays(5));
    }

    @Test
    void shouldHandleGermanRelativeDates() {
        var result = service.parse("Kuchen morgen");
        assertThat(result.title()).isEqualTo("Kuchen");
        assertThat(result.dueDate()).isEqualTo(LocalDate.now().plusDays(1));

        var resultUeber = service.parse("Kuchen übermorgen");
        assertThat(resultUeber.title()).isEqualTo("Kuchen");
        assertThat(resultUeber.dueDate()).isEqualTo(LocalDate.now().plusDays(2));
    }

    @Test
    void shouldParseKaufeKuchenMorgen() {
        var result = service.parse("kaufe kuchen morgen");
        assertThat(result.title()).isEqualTo("kaufe kuchen");
        assertThat(result.dueDate()).isEqualTo(LocalDate.now().plusDays(1));
    }

    @Test
    void shouldHandleQuotedInput() {
        var result = service.parse("\"kaufe kuchen morgen\"");
        assertThat(result.title()).isEqualTo("kaufe kuchen");
        assertThat(result.dueDate()).isNotNull();
    }

    @Test
    void shouldInferCurrentYear() {
        int currentYear = LocalDate.now().getYear();
        
        var resultDot = service.parse("Task 25.10.");
        assertThat(resultDot.dueDate()).isEqualTo(LocalDate.of(currentYear, 10, 25));

        var resultDotLong = service.parse("Task 25.10");
        assertThat(resultDotLong.dueDate()).isEqualTo(LocalDate.of(currentYear, 10, 25));
        
        var resultDash = service.parse("Task 10-25");
        assertThat(resultDash.dueDate()).isEqualTo(LocalDate.of(currentYear, 10, 25));
    }
}
