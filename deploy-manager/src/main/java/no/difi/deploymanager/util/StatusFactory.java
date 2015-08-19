package no.difi.deploymanager.util;

import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.domain.StatusCode;

public class StatusFactory {
    public static Status statusSuccess(String description) {
        return new Status(StatusCode.SUCCESS, description);
    }

    public static Status statusError(String description) {
        return new Status(StatusCode.ERROR, description);
    }

    public static Status statusCritical(String description) {
        return new Status(StatusCode.CRITICAL, description);
    }
}
