package dnt.ai.runnerapp.dao;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

public class CourseDao
{
    private static final int LIMIT = 10;
    private final NamedParameterJdbcTemplate namedJdbc;

    public CourseDao(NamedParameterJdbcTemplate namedJdbc)
    {
        this.namedJdbc = namedJdbc;
    }

    public List<Course> findByLongName(String longName)
    {
        return namedJdbc.query("""
                SELECT course_name, course_long_name, country_code
                FROM course
                WHERE course_long_name LIKE CONCAT('%', :longName, '%')
                LIMIT :limit
                """,
            new MapSqlParameterSource()
                    .addValue("longName", longName)
                    .addValue("limit", LIMIT),
                athleteRowMapper()
        );
    }

    private static RowMapper<Course> athleteRowMapper() {
        return (rs, rowNum) -> new Course(
                rs.getString("course_name"),
                rs.getString("course_long_name"),
                new Country(null, rs.getString("country_code")));
    }
}
