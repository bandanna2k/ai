package dnt.ai.runnerapp.dao;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

public class AthleteDao
{
    private static final int LIMIT = 10;
    private final NamedParameterJdbcTemplate namedJdbc;

    public AthleteDao(NamedParameterJdbcTemplate namedJdbc)
    {
        this.namedJdbc = namedJdbc;
    }

    public List<Athlete> findByIds(List<Long> ids)
    {
        return namedJdbc.query("""
                SELECT name, athlete_id
                FROM athlete
                WHERE athlete_id IN (:ids)
                LIMIT :limit
                """,
            new MapSqlParameterSource()
                    .addValue("ids", ids)
                    .addValue("limit", LIMIT),
                athleteRowMapper()
        );
    }

    public List<Athlete> findByName(String name)
    {
        return namedJdbc.query("""
                SELECT name, athlete_id
                FROM athlete
                WHERE name LIKE CONCAT('%', :name, '%')
                LIMIT :limit
                """,
            new MapSqlParameterSource()
                    .addValue("name", name)
                    .addValue("limit", LIMIT),
                athleteRowMapper()
        );
    }

    private static RowMapper<Athlete> athleteRowMapper() {
        return (rs, rowNum) -> new Athlete(
                rs.getString("name"),
                rs.getLong("athlete_id"));
    }
}
