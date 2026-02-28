package diameter.csv.parser;

import diameter.domain.MessageType;
import diameter.csv.model.CsvRow;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CsvParser {
    private static final String                  DELIMITER = ",";
    private final        Map<CsvColumn, Integer> headerMap = new EnumMap<>(CsvColumn.class);

    public List<CsvRow> parse(List<String> lines) {
        List<CsvRow> rows = new ArrayList<>();
        String[] headers = lines.getFirst().split(DELIMITER, -1);

        for (int i = 0; i < headers.length; i++) {
            headerMap.put(CsvColumn.valueOf(headers[i].toUpperCase()), i);
        }

        for (String line : lines.subList(1, lines.size())) {
            rows.add(parseLine(line));
        }

        return rows;
    }

    private CsvRow parseLine(String line) {
        // message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code
        String[] parts = line == null ? new String[0] : line.split(DELIMITER, -1);

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
}
