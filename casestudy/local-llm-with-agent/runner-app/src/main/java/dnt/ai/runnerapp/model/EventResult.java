package dnt.ai.runnerapp.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "EventResult",
        description = "A single athlete result for an event"
)
public class EventResult {
    @Schema(
            description = "The event number",
            example = "1"
    )
    public int eventNumber;

    @Schema(
            description = "Final position in the event",
            example = "1"
    )
    public int position;

    @Schema(
            description = "The athlete identifier",
            example = "1"
    )
    public long athleteId;

    @Schema(
            description = "Athlete name",
            example = "Alice Smith"
    )
    public String athleteName;

    @Schema(
            description = "Recorded finish time",
            example = "1:04:32"
    )
    public String time;

    public EventResult() {
    }

    public EventResult eventNumber(int eventNumber) {
        this.eventNumber = eventNumber;
        return this;
    }

    public EventResult position(int position) {
        this.position = position;
        return this;
    }

    public EventResult athleteId(long athleteId) {
        this.athleteId = athleteId;
        return this;
    }

    public EventResult athleteName(String athleteName) {
        this.athleteName = athleteName;
        return this;
    }

    public EventResult time(String time) {
        this.time = time;
        return this;
    }
}
