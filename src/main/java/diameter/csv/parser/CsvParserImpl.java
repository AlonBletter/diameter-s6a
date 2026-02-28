package diameter.csv.parser;

import diameter.csv.CsvColumn;
import diameter.domain.MessageType;
import diameter.csv.model.CsvRow;
import diameter.exception.csv.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CsvParserImpl implements CsvParser {
    private static final Logger LOG = LoggerFactory.getLogger(CsvParserImpl.class);
    private static final String                  DELIMITER = ",";
    private final        Map<CsvColumn, Integer> headerMap = new EnumMap<>(CsvColumn.class);

    @Override
    public List<CsvRow> parse(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            LOG.error("CSV parsing failed: file is empty or null");
            throw new CsvValidationException("CSV file is empty");
        }

        headerMap.clear();
        validateHeader(lines.getFirst());
        LOG.debug("CSV header validated successfully: columns = {}", headerMap.keySet());

        List<CsvRow> rows = new ArrayList<>();
        StringBuilder errorLogBuilder = new StringBuilder();
        int skippedLines = 0;

        for (int i = 1; i < lines.size(); i++) {
            try {
                String line = lines.get(i);
                rows.add(parseLine(line));
            }
            catch (CsvValidationException e) {
                errorLogBuilder.append(String.format("\t- Line %d: %s", i + 1, e.getMessage()));
                skippedLines++;
            }
        }

        if (!errorLogBuilder.isEmpty()) {
            LOG.warn("CSV parsing completed with {} errors:\n{}", skippedLines, errorLogBuilder);
        }

        return rows;
    }

    private CsvRow parseLine(String line) {
        String[] parts = line == null ? new String[0] : line.split(DELIMITER, -1);

        validateLine(parts);

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
            LOG.error("CSV header validation failed: header is missing or empty");
            throw new CsvValidationException("CSV header is missing or empty");
        }

        String[] headers = headerLine.split(DELIMITER, -1);

        for (int i = 0; i < headers.length; i++) {
            try {
                CsvColumn column = CsvColumn.valueOf(headers[i].trim().toUpperCase());
                headerMap.put(column, i);
            }
            catch (IllegalArgumentException e) {
                LOG.error("CSV header validation failed: unknown column '{}' at position {}", headers[i], i);
                throw new CsvValidationException("Unknown CSV column: " + headers[i]);
            }
        }

        for (CsvColumn required : CsvColumn.values()) {
            if (!headerMap.containsKey(required)) {
                LOG.error("CSV header validation failed: missing required column '{}'", required);
                throw new CsvValidationException("Missing required column: " + required);
            }
        }
    }

    private void validateLine(String[] parts) {
        if (parts.length < headerMap.size()) {
            throw new CsvValidationException("Line has fewer columns than expected");
        }

        String messageTypeStr = get(parts, headerMap.get(CsvColumn.MESSAGE_TYPE));
        try {
            MessageType.valueOf(messageTypeStr);
        }
        catch (Exception e) {
            throw new CsvValidationException("Invalid message_type: " + messageTypeStr);
        }

        String isRequestStr = get(parts, headerMap.get(CsvColumn.IS_REQUEST));
        if (!isRequestStr.equalsIgnoreCase("true") && !isRequestStr.equalsIgnoreCase("false")) {
            throw new CsvValidationException("Invalid is_request value: " + isRequestStr);
        }
    }
}
