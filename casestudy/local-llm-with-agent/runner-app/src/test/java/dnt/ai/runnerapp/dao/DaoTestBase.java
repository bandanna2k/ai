package dnt.ai.runnerapp.dao;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public abstract class DaoTestBase
{
    protected static NamedParameterJdbcTemplate getNamedJdbc()
    {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/parkrun_stats");
        dataSource.setUsername("root");
        dataSource.setPassword("rootpass");

        NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(dataSource);
        return namedJdbc;
    }

}
