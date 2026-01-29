package ch.goodone.angularai.backend.service;

import ch.goodone.angularai.backend.model.Priority;
import ch.goodone.angularai.backend.model.TaskStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskParserService {

    public record ParsedTask(
            String title,
            String description,
            LocalDate dueDate,
            Priority priority,
            TaskStatus status,
            List<String> tags
    ) {}

    public ParsedTask parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ParsedTask("", "", null, Priority.MEDIUM, TaskStatus.OPEN, List.of());
        }

        String value = sanitizeInput(input);
        
        if (value.contains("|") || value.contains(";")) {
            return parseDelimited(value, "[|;]");
        } else if (value.contains(",")) {
            String[] parts = value.split(",");
            if (isLikelyCsv(parts)) {
                return parseCsv(parts);
            }
        }
        return parseSpaceHeuristic(value);
    }

    private String sanitizeInput(String input) {
        String value = input.trim();
        // Handle JSON-quoted strings from frontend
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1).trim();
        }
        return value;
    }

    private ParsedTask parseDelimited(String value, String delimiter) {
        String[] parts = value.split(delimiter);
        String title = parts.length > 0 ? parts[0].trim() : "";
        String description = parts.length > 1 ? parts[1].trim() : "";
        LocalDate dueDate = parts.length > 2 ? parseDate(parts[2].trim()) : null;
        Priority priority = parts.length > 3 ? parsePriority(parts[3].trim(), Priority.MEDIUM) : Priority.MEDIUM;
        TaskStatus status = parts.length > 4 ? parseStatus(parts[4].trim(), TaskStatus.OPEN) : TaskStatus.OPEN;
        return new ParsedTask(title, description, dueDate, priority, status, List.of());
    }

    private boolean isLikelyCsv(String[] parts) {
        if (parts.length >= 3) return true;
        if (parts.length == 2) {
            String secondPart = parts[1].trim();
            return parseDate(secondPart) != null || parsePriority(secondPart, null) != null || parseStatus(secondPart, null) != null;
        }
        return false;
    }

    private ParsedTask parseCsv(String[] parts) {
        String title = parts[0].trim();
        String description = parts.length > 1 ? parts[1].trim() : "";
        LocalDate dueDate = null;
        Priority priority = Priority.MEDIUM;
        TaskStatus status = TaskStatus.OPEN;

        if (parts.length >= 3) {
            dueDate = parseDate(parts[2].trim());
            if (parts.length > 3) priority = parsePriority(parts[3].trim(), Priority.MEDIUM);
            if (parts.length > 4) status = parseStatus(parts[4].trim(), TaskStatus.OPEN);
        } else if (parts.length == 2) {
            return parseTwoPartCsv(title, description, parts[1].trim());
        }
        return new ParsedTask(title, description, dueDate, priority, status, List.of());
    }

    private ParsedTask parseTwoPartCsv(String title, String description, String secondPart) {
        LocalDate d = parseDate(secondPart);
        Priority p = parsePriority(secondPart, null);
        TaskStatus s = parseStatus(secondPart, null);
        
        LocalDate dueDate = d;
        Priority priority = (p != null) ? p : Priority.MEDIUM;
        TaskStatus status = (s != null) ? s : TaskStatus.OPEN;
        String desc = (d != null || p != null || s != null) ? "" : description;
        
        return new ParsedTask(title, desc, dueDate, priority, status, List.of());
    }

    private int findStatusIndex(String[] tokens) {
        String lastToken = tokens[tokens.length - 1];
        TaskStatus sFound = parseStatus(lastToken, null);
        return (sFound != null) ? tokens.length - 1 : -1;
    }

    private int findPriorityIndex(String[] tokens, int excludeIdx1, int excludeIdx2, int excludeEnd2) {
        for (int i = tokens.length - 1; i >= 0; i--) {
            if (shouldSkipToken(i, excludeIdx1, excludeIdx2, excludeEnd2)) {
                continue;
            }
            if (parsePriority(tokens[i], null) != null) {
                return i;
            }
        }
        return -1;
    }

    private boolean shouldSkipToken(int i, int excludeIdx1, int excludeIdx2, int excludeEnd2) {
        if (i == excludeIdx1) {
            return true;
        }
        return excludeIdx2 != -1 && i >= excludeIdx2 && i <= excludeEnd2;
    }

    private int[] findDateIndices(String[] tokens, int statusIdx, int priorityIdx) {
        for (int i = tokens.length - 1; i >= 0; i--) {
            if (i == statusIdx || i == priorityIdx) {
                continue;
            }
            
            int[] relativeIndices = checkRelativeDateIndices(tokens, i, statusIdx, priorityIdx);
            if (relativeIndices.length > 0) {
                return relativeIndices;
            }

            if (isSingleTokenDate(tokens[i])) {
                return new int[]{i, i};
            }
        }
        return new int[]{-1, -1};
    }

    private boolean isSingleTokenDate(String token) {
        return parseRelativeDate(token) != null || parseDate(token) != null;
    }

    private int[] checkRelativeDateIndices(String[] tokens, int i, int statusIdx, int priorityIdx) {
        // Check for "in X days" (three tokens)
        int[] result = checkThreeTokenRelativeDate(tokens, i, statusIdx, priorityIdx);
        if (result.length > 0) {
            return result;
        }

        // Check for "X days" (two tokens)
        return checkTwoTokenRelativeDate(tokens, i, statusIdx, priorityIdx);
    }

    private int[] checkThreeTokenRelativeDate(String[] tokens, int i, int statusIdx, int priorityIdx) {
        if (i > 1 && isNotExcluded(i, statusIdx, priorityIdx) && isNotExcluded(i - 1, statusIdx, priorityIdx) && isNotExcluded(i - 2, statusIdx, priorityIdx)
                && parseRelativeDate(tokens[i - 2] + " " + tokens[i - 1] + " " + tokens[i]) != null) {
            return new int[]{i - 2, i};
        }
        return new int[0];
    }

    private int[] checkTwoTokenRelativeDate(String[] tokens, int i, int statusIdx, int priorityIdx) {
        if (i > 0 && isNotExcluded(i, statusIdx, priorityIdx) && isNotExcluded(i - 1, statusIdx, priorityIdx)
                && parseRelativeDate(tokens[i - 1] + " " + tokens[i]) != null) {
            return new int[]{i - 1, i};
        }
        return new int[0];
    }

    private boolean isNotExcluded(int i, int statusIdx, int priorityIdx) {
        return i != statusIdx && i != priorityIdx;
    }

    private ParsedTask parseSpaceHeuristic(String value) {
        String[] tokens = value.split("\\s+");
        
        int statusIdx = findStatusIndex(tokens);
        TaskStatus status = (statusIdx != -1) ? parseStatus(tokens[statusIdx], TaskStatus.OPEN) : TaskStatus.OPEN;

        int priorityIdx = findPriorityIndex(tokens, statusIdx, -1, -1);
        
        int[] dateIndices = findDateIndices(tokens, statusIdx, priorityIdx);
        int dateIdx = dateIndices[0];
        int dateEndIdx = dateIndices[1];
        LocalDate dueDate = null;
        if (dateIdx != -1) {
            dueDate = resolveDueDate(tokens, dateIdx, dateEndIdx);
        }

        if (priorityIdx == -1) {
            priorityIdx = findPriorityIndex(tokens, statusIdx, dateIdx, dateEndIdx);
        }
        Priority priority = (priorityIdx != -1) ? parsePriority(tokens[priorityIdx], Priority.MEDIUM) : Priority.MEDIUM;

        String title = buildTitle(value, tokens, statusIdx, priorityIdx, dateIdx, dateEndIdx);
        return new ParsedTask(title, "", dueDate, priority, status, List.of());
    }

    private LocalDate resolveDueDate(String[] tokens, int dateIdx, int dateEndIdx) {
        String dateStr;
        if (dateIdx == dateEndIdx) {
            dateStr = tokens[dateIdx];
            LocalDate rd = parseRelativeDate(dateStr);
            return (rd != null) ? rd : parseDate(dateStr);
        } else if (dateEndIdx - dateIdx == 1) {
            dateStr = tokens[dateIdx] + " " + tokens[dateIdx + 1];
        } else {
            dateStr = tokens[dateIdx] + " " + tokens[dateIdx + 1] + " " + tokens[dateIdx + 2];
        }
        return parseRelativeDate(dateStr);
    }

    private String buildTitle(String originalValue, String[] tokens, int statusIdx, int priorityIdx, int dateIdx, int dateEndIdx) {
        if (statusIdx == -1 && priorityIdx == -1 && dateIdx == -1) {
            return originalValue;
        }
        List<String> titleTokens = new java.util.ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            boolean isDateToken = (dateIdx != -1 && i >= dateIdx && i <= dateEndIdx);
            if (i != statusIdx && i != priorityIdx && !isDateToken) {
                titleTokens.add(tokens[i]);
            }
        }
        return String.join(" ", titleTokens).trim();
    }

    private LocalDate parseDate(String input) {
        if (input.matches("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$")) {
            return parseSwissDate(input);
        }
        if (input.matches("^\\d{1,2}\\.\\d{1,2}\\.?$")) {
            return parseSwissShortDate(input);
        }
        if (input.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return parseIsoDate(input);
        }
        if (input.matches("^\\d{2}-\\d{2}$")) {
            return parseIsoShortDate(input);
        }
        return parseRelativeDate(input);
    }

    private LocalDate parseSwissDate(String input) {
        try {
            String[] parts = input.split("\\.");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseSwissShortDate(String input) {
        try {
            String[] parts = input.split("\\.");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            return adjustYear(day, month);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseIsoDate(String input) {
        try {
            return LocalDate.parse(input);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseIsoShortDate(String input) {
        try {
            String[] parts = input.split("-");
            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);
            return adjustYear(day, month);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate adjustYear(int day, int month) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        LocalDate targetDate = LocalDate.of(year, month, day);

        // If date is more than 6 months in the past, assume next year
        if (targetDate.isBefore(today.minusMonths(6))) {
            targetDate = targetDate.plusYears(1);
        }
        return targetDate;
    }

    private LocalDate parseRelativeDate(String input) {
        String text = input.toLowerCase().trim();
        LocalDate today = LocalDate.now();

        switch (text) {
            case "today", "heute", "heut" -> { return today; }
            case "tomorrow", "morgen" -> { return today.plusDays(1); }
            case "übermorgen" -> { return today.plusDays(2); }
            case "yesterday", "gestern" -> { return today.minusDays(1); }
            case "week", "woche", "nächste woche", "next week" -> { return today.plusWeeks(1); }
            default -> { return parseXDays(text, today); }
        }
    }

    private LocalDate parseXDays(String text, LocalDate today) {
        String[] parts = text.split("\\s+");
        if (parts.length < 2 || parts.length > 3) {
            return null;
        }

        String daysStr = extractDaysString(parts);
        String unit = extractUnitString(parts);

        if (daysStr != null && isDayUnit(unit)) {
            try {
                int days = Integer.parseInt(daysStr);
                return today.plusDays(days);
            } catch (NumberFormatException e) {
                // Not a number
            }
        }
        return null;
    }

    private String extractDaysString(String[] parts) {
        if (parts.length == 2) {
            return parts[0];
        } else if (parts[0].equals("in")) {
            return parts[1];
        }
        return null;
    }

    private String extractUnitString(String[] parts) {
        if (parts.length == 2) {
            return parts[1];
        } else if (parts[0].equals("in")) {
            return parts[2];
        }
        return null;
    }

    private boolean isDayUnit(String unit) {
        return unit != null && (unit.startsWith("day") || unit.startsWith("tag"));
    }

    private Priority parsePriority(String input, Priority defaultVal) {
        String s = input.toUpperCase();
        switch (s) {
            case "HOCH", "DRINGEND", "WICHTIG", "HIGH":
                return Priority.HIGH;
            case "TIEF", "UNWICHTIG", "NIEDRIG", "LOW":
                return Priority.LOW;
            case "MITTEL", "MEDIUM":
                return Priority.MEDIUM;
            case "KRITISCH", "CRITICAL":
                return Priority.CRITICAL;
            default:
                try {
                    return Priority.valueOf(s);
                } catch (Exception e) {
                    return defaultVal;
                }
        }
    }

    private TaskStatus parseStatus(String input, TaskStatus defaultVal) {
        String s = input.toUpperCase().replace(" ", "_");
        switch (s) {
            case "PROGRESS", "HÄNGIG", "PENDENT", "IN_PROGRESS":
                return TaskStatus.IN_PROGRESS;
            case "OFFEN", "OPEN":
                return TaskStatus.OPEN;
            case "ERLEDIGT", "FERTIG", "DONE":
                return TaskStatus.DONE;
            case "ARCHIV", "ARCHIVIERT", "ARCHIVED":
                return TaskStatus.ARCHIVED;
            default:
                try {
                    return TaskStatus.valueOf(s);
                } catch (Exception e) {
                    return defaultVal;
                }
        }
    }
}
