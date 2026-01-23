package ch.goodone.angularai.backend.service;

import ch.goodone.angularai.backend.model.Priority;
import ch.goodone.angularai.backend.model.TaskStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        String value = input.trim();
        // Handle JSON-quoted strings from frontend
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1).trim();
        }
        
        String title = "";
        String description = "";
        LocalDate dueDate = null;
        Priority priority = Priority.MEDIUM;
        TaskStatus status = TaskStatus.OPEN;

        if (value.contains("|") || value.contains(";")) {
            String[] parts = value.split("[|;]");
            title = parts.length > 0 ? parts[0].trim() : "";
            description = parts.length > 1 ? parts[1].trim() : "";
            
            if (parts.length > 2) {
                dueDate = parseDate(parts[2].trim());
            }
            if (parts.length > 3) {
                priority = parsePriority(parts[3].trim(), Priority.MEDIUM);
            }
            if (parts.length > 4) {
                status = parseStatus(parts[4].trim(), TaskStatus.OPEN);
            }
        } else if (value.contains(",")) {
            String[] parts = value.split(",");
            // If it's just one comma and it's not followed by typical metadata, 
            // maybe it's just a title with a comma?
            // "Buy bread, milk tomorrow" vs "Meeting, Project X, tomorrow"
            
            boolean looksLikeCsv = parts.length >= 3;
            if (parts.length == 2) {
                // Check if the second part is a date/prio/status
                String secondPart = parts[1].trim();
                LocalDate d = parseDate(secondPart);
                Priority p = parsePriority(secondPart, null);
                TaskStatus s = parseStatus(secondPart, null);
                if (d != null || p != null || s != null) {
                    looksLikeCsv = true;
                }
            }

            if (looksLikeCsv) {
                // Highly likely to be CSV-style
                title = parts[0].trim();
                description = parts.length > 1 ? parts[1].trim() : "";
                if (parts.length > 2) {
                    dueDate = parseDate(parts[2].trim());
                } else if (parts.length == 2) {
                    // Title, Metadata case handled above by looksLikeCsv
                    String secondPart = parts[1].trim();
                    LocalDate d = parseDate(secondPart);
                    Priority p = parsePriority(secondPart, null);
                    TaskStatus s = parseStatus(secondPart, null);
                    if (d != null) dueDate = d;
                    if (p != null) priority = p;
                    if (s != null) status = s;
                    
                    if (d != null || p != null || s != null) {
                        description = ""; // Reset description if it was metadata
                    }
                }
                if (parts.length > 3) priority = parsePriority(parts[3].trim(), Priority.MEDIUM);
                if (parts.length > 4) status = parseStatus(parts[4].trim(), TaskStatus.OPEN);
            } else {
                // Treat as potential space-based heuristic but with a comma in title
                return parseSpaceHeuristic(value);
            }
        } else {
            // Space-based heuristic parsing
            return parseSpaceHeuristic(value);
        }

        return new ParsedTask(title, description, dueDate, priority, status, List.of());
    }

    private ParsedTask parseSpaceHeuristic(String value) {
        String title = "";
        String description = "";
        LocalDate dueDate = null;
        Priority priority = Priority.MEDIUM;
        TaskStatus status = TaskStatus.OPEN;

        String[] tokens = value.split("\\s+");
        
        int statusIdx = -1;
        int priorityIdx = -1;
        int dateIdx = -1;

        // Look for status (last token)
        String lastToken = tokens[tokens.length - 1];
        TaskStatus sFound = parseStatus(lastToken, null);
        if (sFound != null) {
            statusIdx = tokens.length - 1;
            status = sFound;
        }

        // Look for priority
        for (int i = tokens.length - 1; i >= 0; i--) {
            if (i == statusIdx) continue;
            Priority pFound = parsePriority(tokens[i], null);
            if (pFound != null) {
                priorityIdx = i;
                priority = pFound;
                break;
            }
        }

        // Look for date
        int dateEndIdx = -1;
        for (int i = tokens.length - 1; i >= 0; i--) {
            if (i == statusIdx || i == priorityIdx) continue;
            
            // Check for relative date first
            // Check for "in X days" (three tokens)
            if (i > 1 && (i-1 != statusIdx && i-1 != priorityIdx) && (i-2 != statusIdx && i-2 != priorityIdx)) {
                String threeTokenDate = tokens[i - 2] + " " + tokens[i - 1] + " " + tokens[i];
                LocalDate d = parseRelativeDate(threeTokenDate);
                if (d != null) {
                    dateIdx = i - 2;
                    dateEndIdx = i;
                    dueDate = d;
                    break;
                }
            }
            // Check for "X days" (two tokens)
            if (i > 0 && (i-1 != statusIdx && i-1 != priorityIdx)) {
                String twoTokenDate = tokens[i - 1] + " " + tokens[i];
                LocalDate d = parseRelativeDate(twoTokenDate);
                if (d != null) {
                    dateIdx = i - 1;
                    dateEndIdx = i;
                    dueDate = d;
                    break;
                }
            }

            // Single token relative date or absolute date
            LocalDate dRel = parseRelativeDate(tokens[i]);
            if (dRel != null) {
                dueDate = dRel;
                dateIdx = i;
                dateEndIdx = i;
                break;
            }

            LocalDate dAbs = parseDate(tokens[i]);
            if (dAbs != null) {
                dueDate = dAbs;
                dateIdx = i;
                dateEndIdx = i;
                break;
            }
        }

        // Look for priority if not found yet
        if (priorityIdx == -1) {
            for (int i = tokens.length - 1; i >= 0; i--) {
                boolean isDateToken = (dateIdx != -1 && i >= dateIdx && i <= dateEndIdx);
                if (i != statusIdx && !isDateToken) {
                    Priority pFound = parsePriority(tokens[i], null);
                    if (pFound != null) {
                        priorityIdx = i;
                        priority = pFound;
                        break;
                    }
                }
            }
        }

        if (statusIdx == -1 && priorityIdx == -1 && dateIdx == -1) {
            title = value;
        } else {
            List<String> titleTokens = new java.util.ArrayList<>();
            for (int i = 0; i < tokens.length; i++) {
                boolean isDateToken = (dateIdx != -1 && i >= dateIdx && i <= dateEndIdx);
                if (i != statusIdx && i != priorityIdx && !isDateToken) {
                    titleTokens.add(tokens[i]);
                }
            }
            title = String.join(" ", titleTokens).trim();
        }
        return new ParsedTask(title, description, dueDate, priority, status, List.of());
    }

    private LocalDate parseDate(String input) {
        if (input.matches("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$")) {
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
        if (input.matches("^\\d{1,2}\\.\\d{1,2}\\.?$")) {
            try {
                String[] parts = input.split("\\.");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                LocalDate today = LocalDate.now();
                int year = today.getYear();
                LocalDate targetDate = LocalDate.of(year, month, day);
                
                // If date is more than 6 months in the past, assume next year
                if (targetDate.isBefore(today.minusMonths(6))) {
                    targetDate = targetDate.plusYears(1);
                }
                
                return targetDate;
            } catch (Exception e) {
                return null;
            }
        }
        if (input.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            try {
                return LocalDate.parse(input);
            } catch (Exception e) {
                return null;
            }
        }
        if (input.matches("^\\d{2}-\\d{2}$")) {
            try {
                String[] parts = input.split("-");
                int month = Integer.parseInt(parts[0]);
                int day = Integer.parseInt(parts[1]);
                LocalDate today = LocalDate.now();
                int year = today.getYear();
                LocalDate targetDate = LocalDate.of(year, month, day);

                // If date is more than 6 months in the past, assume next year
                if (targetDate.isBefore(today.minusMonths(6))) {
                    targetDate = targetDate.plusYears(1);
                }

                return targetDate;
            } catch (Exception e) {
                return null;
            }
        }
        return parseRelativeDate(input);
    }

    private LocalDate parseRelativeDate(String input) {
        String text = input.toLowerCase().trim();
        LocalDate today = LocalDate.now();

        if (text.equals("today") || text.equals("heute") || text.equals("heut")) return today;
        if (text.equals("tomorrow") || text.equals("morgen")) return today.plusDays(1);
        if (text.equals("übermorgen")) return today.plusDays(2);
        if (text.equals("yesterday") || text.equals("gestern")) return today.minusDays(1);
        if (text.equals("week") || text.equals("woche")) return today.plusWeeks(1);

        // More robust parsing for "in X days" or "X days"
        String[] parts = text.split("\\s+");
        if (parts.length >= 2 && parts.length <= 3) {
            String daysStr = null;
            String unit = null;
            if (parts.length == 2) {
                daysStr = parts[0];
                unit = parts[1];
            } else if (parts[0].equals("in")) {
                daysStr = parts[1];
                unit = parts[2];
            }
            
            if (daysStr != null && (unit.startsWith("day") || unit.startsWith("tag"))) {
                try {
                    int days = Integer.parseInt(daysStr);
                    return today.plusDays(days);
                } catch (NumberFormatException e) {
                    // Not a number
                }
            }
        }

        return null;
    }

    private Priority parsePriority(String input, Priority defaultVal) {
        String s = input.toUpperCase();
        switch (s) {
            case "HOCH":
            case "DRINGEND":
            case "WICHTIG":
            case "HIGH":
                return Priority.HIGH;
            case "TIEF":
            case "UNWICHTIG":
            case "NIEDRIG":
            case "LOW":
                return Priority.LOW;
            case "MITTEL":
            case "MEDIUM":
                return Priority.MEDIUM;
            case "KRITISCH":
            case "CRITICAL":
                return Priority.CRITICAL;
        }
        try {
            return Priority.valueOf(s);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private TaskStatus parseStatus(String input, TaskStatus defaultVal) {
        String s = input.toUpperCase().replace(" ", "_");
        switch (s) {
            case "PROGRESS":
            case "HÄNGIG":
            case "PENDENT":
            case "IN_PROGRESS":
                return TaskStatus.IN_PROGRESS;
            case "OFFEN":
            case "OPEN":
                return TaskStatus.OPEN;
            case "ERLEDIGT":
            case "FERTIG":
            case "DONE":
                return TaskStatus.DONE;
            case "ARCHIV":
            case "ARCHIVIERT":
            case "ARCHIVED":
                return TaskStatus.ARCHIVED;
        }
        try {
            return TaskStatus.valueOf(s);
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
