package dnt.ai.runnerapp.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "Athlete",
        description = "An athlete registered in the runner system"
)
public class Athlete {
    @Schema(
            description = "The unique athlete identifier",
            example = "1"
    )
    public long athleteId;

    @Schema(
            description = "Athlete name",
            example = "Alice Smith"
    )
    public String name;

    @Schema(
            description = "ISO country code the athlete represents",
            example = "GB"
    )
    public String country;

    public Athlete athleteId(long athleteId) {
        this.athleteId = athleteId;
        return this;
    }

    public Athlete name(String name) {
        this.name = name;
        return this;
    }

    public Athlete country(String country) {
        this.country = country;
        return this;
    }
}
