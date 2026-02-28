package diameter.csv.model;

import diameter.domain.MessageType;

public final class CsvRow {
    private final MessageType messageType;
    private final boolean     isRequest;
    private final String      sessionId;
    private final String      originHost;
    private final String      originRealm;
    private final String      userName;
    private final String      visitedPlmnId;
    private final String      resultCode;

    public CsvRow(
            MessageType messageType,
            boolean isRequest,
            String sessionId,
            String originHost,
            String originRealm,
            String userName,
            String visitedPlmnId,
            String resultCode
    ) {
        this.messageType = messageType;
        this.isRequest = isRequest;
        this.sessionId = sessionId;
        this.originHost = originHost;
        this.originRealm = originRealm;
        this.userName = userName;
        this.visitedPlmnId = visitedPlmnId;
        this.resultCode = resultCode;
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
