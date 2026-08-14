package dnt.ai.runnerapp.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "EventHistoryEntry",
        description = "A single event held on a course"
)
public class EventHistoryEntry {
    @Schema(
            description = "The course the event was held on",
            example = "1"
    )
    public long courseId;

    @Schema(
            description = "The event number within the course",
            example = "1"
    )
    public int eventNumber;

    @Schema(
            description = "Event name",
            example = "Wellington Half Marathon 2025"
    )
    public String name;

    @Schema(
            description = "Event date",
            example = "2025-03-15"
    )
    public String date;

    @Schema(
            description = "Event status",
            example = "COMPLETED"
    )
    public String status;

    public EventHistoryEntry() {
    }

    public EventHistoryEntry courseId(long courseId) {
        this.courseId = courseId;
        return this;
    }

    public EventHistoryEntry eventNumber(int eventNumber) {
        this.eventNumber = eventNumber;
        return this;
    }

    public EventHistoryEntry name(String name) {
        this.name = name;
        return this;
    }

    public EventHistoryEntry date(String date) {
        this.date = date;
        return this;
    }

    public EventHistoryEntry status(String status) {
        this.status = status;
        return this;
    }
}
