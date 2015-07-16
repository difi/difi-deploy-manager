package domain;

public class Status {
    private StatusCode statusCode;
    private String description;

    public Status(StatusCode statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public String getDescription() {
        return description;
    }
}
