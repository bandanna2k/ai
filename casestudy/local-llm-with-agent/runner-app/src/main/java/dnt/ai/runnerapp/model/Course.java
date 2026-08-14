package dnt.ai.runnerapp.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "Course",
        description = "A course in the runner system"
)
public class Course {
    @Schema(
            description = "The unique course identifier",
            example = "1"
    )
    public long courseId;

    @Schema(
            description = "Course name",
            example = "Wellington Half Marathon"
    )
    public String name;

    @Schema(
            description = "ISO country code the course is located in",
            example = "NZ"
    )
    public String country;

    public Course() {
    }

    public Course courseId(long courseId) {
        this.courseId = courseId;
        return this;
    }

    public Course name(String name) {
        this.name = name;
        return this;
    }

    public Course country(String country) {
        this.country = country;
        return this;
    }
}
