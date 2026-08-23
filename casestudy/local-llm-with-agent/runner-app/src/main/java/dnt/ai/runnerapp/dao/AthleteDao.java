package dnt.ai.runnerapp.dao;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

public class AthleteDao
{
    private final NamedParameterJdbcTemplate namedJdbc;

    public AthleteDao(NamedParameterJdbcTemplate namedJdbc)
    {
        this.namedJdbc = namedJdbc;
    }

    public void getAthletesById(int id)
    {
    }

    public List<Athlete> getAthleteByName(String name)
    {
        return namedJdbc.query("""
                SELECT name, athlete_id
                FROM athlete
                WHERE name like '%:name%'
                """,
            new MapSqlParameterSource("name", name),
            (rs, rowNum) -> {
                // Map the result set to an Athlete object
                return new Athlete(
                        rs.getString("name"),
                        rs.getLong("athlete_id")
                );
            }
        );
    }
}
