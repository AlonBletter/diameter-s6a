package diameter.model;

import diameter.domain.MessageType;

public final class CsvRow {
    private static final String delimiter = ",";

    private final MessageType messageType;
    private final boolean     isRequest;
    private final String      sessionId;
    private final String      originHost;
    private final String      originRealm;
    private final String      userName;
    private final String      visitedPlmnId;
    private final String      resultCode;

    public CsvRow(String line) {
        // message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code
        String[] parts = line == null ? new String[0] : line.split(delimiter, -1);

        this.messageType = MessageType.valueOf(get(parts, 0));
        this.isRequest = Boolean.parseBoolean(get(parts, 1));
        this.sessionId = emptyToNull(get(parts, 2));
        this.originHost = emptyToNull(get(parts, 3));
        this.originRealm = emptyToNull(get(parts, 4));
        this.userName = emptyToNull(get(parts, 5));
        this.visitedPlmnId = emptyToNull(get(parts, 6));
        this.resultCode = emptyToNull(get(parts, 7));
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

    public MessageType getMessageType() {
        return messageType;
    }

    public boolean getIsRequest() {
        return isRequest;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getOriginHost() {
        return originHost;
    }

    public String getOriginRealm() {
        return originRealm;
    }

    public String getUserName() {
        return userName;
    }

    public String getVisitedPlmnId() {
        return visitedPlmnId;
    }

    public String getResultCode() {
        return resultCode;
    }
}
