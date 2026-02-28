package diameter.csv.parser;

import diameter.csv.CsvColumn;
import diameter.domain.MessageType;
import diameter.csv.model.CsvRow;
import diameter.exception.csv.CsvValidationException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CsvParserImpl implements CsvParser {
    private static final String                  DELIMITER = ",";
    private final        Map<CsvColumn, Integer> headerMap = new EnumMap<>(CsvColumn.class);

    @Override
    public List<CsvRow> parse(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new CsvValidationException("CSV file is empty");
        }

        headerMap.clear();
        validateHeader(lines.getFirst());

        List<CsvRow> rows = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            try {
                String line = lines.get(i);
                rows.add(parseLine(line, i + 1)); // line number is 1-based for error reporting
            }
            catch (CsvValidationException e) {
                System.err.println("Skipping invalid line " + (i + 1) + ": " + e.getMessage());
            }
        }

        return rows;
    }

    private CsvRow parseLine(String line, int lineNumber) {
        String[] parts = line == null ? new String[0] : line.split(DELIMITER, -1);

        validateLine(parts, lineNumber);

        MessageType messageType   = MessageType.valueOf(get(parts, headerMap.get(CsvColumn.MESSAGE_TYPE)));
        boolean     isRequest     = Boolean.parseBoolean(get(parts, headerMap.get(CsvColumn.IS_REQUEST)));
        String      sessionId     = emptyToNull(get(parts, headerMap.get(CsvColumn.SESSION_ID)));
        String      originHost    = emptyToNull(get(parts, headerMap.get(CsvColumn.ORIGIN_HOST)));
        String      originRealm   = emptyToNull(get(parts, headerMap.get(CsvColumn.ORIGIN_REALM)));
        String      userName      = emptyToNull(get(parts, headerMap.get(CsvColumn.USER_NAME)));
        String      visitedPlmnId = emptyToNull(get(parts, headerMap.get(CsvColumn.VISITED_PLMN_ID)));
        String      resultCode    = emptyToNull(get(parts, headerMap.get(CsvColumn.RESULT_CODE)));

        return new CsvRow(
                messageType,
                isRequest,
                sessionId,
                originHost,
                originRealm,
                userName,
                visitedPlmnId,
                resultCode
        );
    }

    private static String get(String[] parts, int idx) {
        if (parts == null || idx < 0 || idx >= parts.length) return "";
        return parts[idx] == null ? "" : parts[idx].trim();
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void validateHeader(String headerLine) {
        if (headerLine == null || headerLine.isBlank()) {
            throw new CsvValidationException("CSV header is missing or empty");
        }

        String[] headers = headerLine.split(DELIMITER, -1);

        for (int i = 0; i < headers.length; i++) {
            try {
                CsvColumn column = CsvColumn.valueOf(headers[i].trim().toUpperCase());
                headerMap.put(column, i);
            }
            catch (IllegalArgumentException e) {
                throw new CsvValidationException("Unknown CSV column: " + headers[i]);
            }
        }

        for (CsvColumn required : CsvColumn.values()) {
            if (!headerMap.containsKey(required)) {
                throw new CsvValidationException("Missing required column: " + required);
            }
        }
    }

    private void validateLine(String[] parts, int lineNumber) {
        if (parts.length < headerMap.size()) {
            throw new CsvValidationException("Line " + lineNumber + " has fewer columns than expected");
        }

        String messageTypeStr = get(parts, headerMap.get(CsvColumn.MESSAGE_TYPE));
        try {
            MessageType.valueOf(messageTypeStr);
        }
        catch (Exception e) {
            throw new CsvValidationException("Invalid message_type at line " + lineNumber + ": " + messageTypeStr);
        }

        String isRequestStr = get(parts, headerMap.get(CsvColumn.IS_REQUEST));
        if (!isRequestStr.equalsIgnoreCase("true") && !isRequestStr.equalsIgnoreCase("false")) {
            throw new CsvValidationException("Invalid is_request value at line " + lineNumber + ": " + isRequestStr);
        }
    }
}
