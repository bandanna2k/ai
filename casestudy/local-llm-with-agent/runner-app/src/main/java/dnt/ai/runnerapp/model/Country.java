package dnt.ai.runnerapp.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "Country",
        description = "A country in the runner system"
)
public class Country {
    @Schema(
            description = "ISO country code",
            example = "GB"
    )
    public String code;

    @Schema(
            description = "Country name",
            example = "United Kingdom"
    )
    public String name;

    public Country() {
    }

    public Country code(String code) {
        this.code = code;
        return this;
    }

    public Country name(String name) {
        this.name = name;
        return this;
    }
}
