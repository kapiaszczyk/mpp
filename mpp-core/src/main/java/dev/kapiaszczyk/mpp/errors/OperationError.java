package dev.kapiaszczyk.mpp.errors;

import org.springframework.http.HttpStatus;

/**
 * Represents an error that occurred during an operation in the system.
 */
public class OperationError {

    private HttpStatus httpStatus;
    private String message;

    public OperationError httpStatusCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public OperationError message(String message) {
        this.message = message;
        return this;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public static OperationError notFound(String message) {
        return new OperationError().httpStatusCode(HttpStatus.NOT_FOUND).message(message);
    }

    public static OperationError badRequest(String message) {
        return new OperationError().httpStatusCode(HttpStatus.BAD_REQUEST).message(message);
    }

    public static OperationError internalServerError(String message) {
        return new OperationError().httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR).message(message);
    }

    public static OperationError unauthorized(String message) {
        return new OperationError().httpStatusCode(HttpStatus.UNAUTHORIZED).message(message);
    }

    public static OperationError forbidden(String message) {
        return new OperationError().httpStatusCode(HttpStatus.FORBIDDEN).message(message);
    }


}
