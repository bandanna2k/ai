package dnt.ai.runnerapp.dao;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

public class EventDao
{
    private final NamedParameterJdbcTemplate namedJdbc;

    public EventDao(NamedParameterJdbcTemplate namedJdbc)
    {
        this.namedJdbc = namedJdbc;
    }

    public CourseEventSummary findEventHistory(long courseId) {

        Course course = fetchCourseById(courseId);
        List<CourseEvent> events = fetchEventsByCourseId(courseId);

        return new CourseEventSummary(course, events);
    }

    private Course fetchCourseById(long courseId) {
        List<Course> results = namedJdbc.query("""
                SELECT course_name, course_long_name, country_code
                FROM parkrun_stats.course
                WHERE course_id = :courseId
                """,
                new MapSqlParameterSource().addValue("courseId", courseId),
                courseRowMapper()
        );
        return results.isEmpty() ? null : results.getFirst();
    }

    private List<CourseEvent> fetchEventsByCourseId(long courseId) {
        return namedJdbc.query("""
                SELECT event_number, date
                FROM parkrun_stats_NZ.course_event_summary
                WHERE course_id = :courseId 
                ORDER BY event_number ASC
                """,
                new MapSqlParameterSource().addValue("courseId", courseId),
                (rs, rowNum) -> new CourseEvent(
                        rs.getInt("event_number"),
                        rs.getString("date"),
                        null,  // maleFirstFinisher - to be implemented
                        null   // femaleFirstFinisher - to be implemented
                )
        );
    }

    private static RowMapper<Course> courseRowMapper() {
        return (rs, rowNum) -> new Course(
                rs.getString("course_name"),
                rs.getString("course_long_name"),
                new Country(null, rs.getString("country_code")));
    }
}
