package no.difi.deploymanager.domain;

public class Status {
    private final StatusCode statusCode;
    private final String description;

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
