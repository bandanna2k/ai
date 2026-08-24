package dnt.ai.runnerapp.dao;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AthleteDaoTest {

    private AthleteDao buildDao() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/parkrun_stats");
        dataSource.setUsername("root");
        dataSource.setPassword("rootpass");

        NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(dataSource);
        return new AthleteDao(namedJdbc);
    }

    @Test
    void findByName() {
        AthleteDao dao = buildDao();

        List<Athlete> results = dao.findByName("Richard FOX");
        System.out.println(results);
        assertThat(results)
                .isNotEmpty()
                .allSatisfy(athlete ->
                        assertThat(athlete.name()).containsIgnoringCase("Richard FOX"));
    }
}