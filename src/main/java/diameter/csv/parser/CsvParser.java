package diameter.csv.parser;

import diameter.domain.MessageType;
import diameter.csv.model.CsvRow;

import java.util.ArrayList;
import java.util.List;

public class CsvParser {
    private static final String DELIMITER = ",";

    public List<CsvRow> parse(List<String> lines) {
        List<CsvRow> rows = new ArrayList<>();

        boolean isFirstLine = true;
        for (String line : lines) {
            if (isFirstLine) {
                isFirstLine = false;
                continue;
            }

            rows.add(parseLine(line));
        }

        return rows;
    }

    private CsvRow parseLine(String line) {
        // message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code
        String[] parts = line == null ? new String[0] : line.split(DELIMITER, -1);

        MessageType messageType = MessageType.valueOf(get(parts, 0));
        boolean isRequest = Boolean.parseBoolean(get(parts, 1));

        String sessionId = emptyToNull(get(parts, 2));
        String originHost = emptyToNull(get(parts, 3));
        String originRealm = emptyToNull(get(parts, 4));
        String userName = emptyToNull(get(parts, 5));
        String visitedPlmnId = emptyToNull(get(parts, 6));
        String resultCode = emptyToNull(get(parts, 7));

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
}
