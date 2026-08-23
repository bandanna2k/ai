package dnt.ai.runnerapp.dao;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import dnt.ai.runnerapp.model.Athlete;
import java.util.List;

public class AthleteDao
{
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
                """,
            new MapSqlParameterSource("ids", ids),
            (rs, rowNum) -> {
                return new Athlete()
                        .name(rs.getString("name"))
                        .athleteId(rs.getLong("athlete_id"));
            }
        );
    }

    public List<Athlete> findByName(String name)
    {
        return namedJdbc.query("""
                SELECT name, athlete_id
                FROM athlete
                WHERE name like '%:name%'
                """,
            new MapSqlParameterSource("name", name),
            (rs, rowNum) -> {
                return new Athlete()
                        .name(rs.getString("name"))
                        .athleteId(rs.getLong("athlete_id"));
            }
        );
    }
}
