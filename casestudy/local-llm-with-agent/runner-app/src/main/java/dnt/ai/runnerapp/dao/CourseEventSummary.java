package dnt.ai.runnerapp.dao;

import java.util.List;

public record CourseEventSummary(Course course, List<CourseEvent> events) {
}
