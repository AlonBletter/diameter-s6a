package diameter.reporter;

public final class ProcessingResult {
    private final boolean success;
    private final boolean valid;
    private final String  errorMessage;

    private ProcessingResult(boolean success, boolean valid, String errorMessage) {
        this.success = success;
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public static ProcessingResult success() {
        return new ProcessingResult(true, true, null);
    }

    public static ProcessingResult validationFailure() {
        return new ProcessingResult(false, false, null);
    }

    public static ProcessingResult error(String message) {
        return new ProcessingResult(false, false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

