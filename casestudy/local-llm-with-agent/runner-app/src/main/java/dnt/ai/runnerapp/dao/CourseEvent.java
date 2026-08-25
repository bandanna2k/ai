package dnt.ai.runnerapp.dao;

public record CourseEvent(
        int eventNumber,
        String date,
        Athlete maleFirstFinisher,
        Athlete femaleFirstFinisher
) {
}
