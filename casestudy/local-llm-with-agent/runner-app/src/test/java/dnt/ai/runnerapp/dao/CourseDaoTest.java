package dnt.ai.runnerapp.dao;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CourseDaoTest extends DaoTestBase
{
    CourseDao dao = new CourseDao(getNamedJdbc());

    @Test
    void findByLongName() {
        List<Course> results = dao.findByLongName("Cornwall Park");
        results.forEach(System.out::println);
        assertThat(results)
                .isNotEmpty()
                .allSatisfy(course ->
                        assertThat(course.name()).isEqualTo("cornwallpark"));
    }
}