package dnt.ai.runnerapp.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "ErrorResponse",
        description = "Error information returned when a request fails"
)
public class ErrorResponse {
    @Schema(
            description = "Machine readable error code",
            example = "NOT_FOUND"
    )
    public String code;

    @Schema(
            description = "Human readable error message",
            example = "Course not found"
    )
    public String message;

    public ErrorResponse() {
    }

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorResponse code(String code) {
        this.code = code;
        return this;
    }

    public ErrorResponse message(String message) {
        this.message = message;
        return this;
    }
}
