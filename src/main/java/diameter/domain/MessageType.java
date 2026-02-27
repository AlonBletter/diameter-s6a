package diameter.domain;

public enum MessageType {
    AIR("AIR"),
    AIA("AIA"),
    ULR("ULR"),
    ULA("ULA");

    private final String type;

    MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
