package dnt.ai.runnerapp.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "Volunteer",
        description = "A volunteer assigned to an event"
)
public class Volunteer {
    @Schema(
            description = "The unique volunteer identifier",
            example = "1"
    )
    public long volunteerId;

    @Schema(
            description = "Volunteer name",
            example = "Sam Wilson"
    )
    public String name;

    @Schema(
            description = "Role fulfilled by the volunteer",
            example = "Water Station"
    )
    public String role;

    public Volunteer() {
    }

    public Volunteer volunteerId(long volunteerId) {
        this.volunteerId = volunteerId;
        return this;
    }

    public Volunteer name(String name) {
        this.name = name;
        return this;
    }

    public Volunteer role(String role) {
        this.role = role;
        return this;
    }
}
