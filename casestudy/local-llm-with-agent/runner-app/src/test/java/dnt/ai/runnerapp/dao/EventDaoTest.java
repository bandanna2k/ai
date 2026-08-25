package dnt.ai.runnerapp.dao;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventDaoTest extends DaoTestBase
{
    EventDao dao = new EventDao(getNamedJdbc());

    @Test
    void findEventHistory_WithValidCourseId_ReturnsListOfCourseHistory() {

        long courseId = 1L;
        CourseEventSummary result = dao.findEventHistory(courseId);
        System.out.println(result.course());
        result.events().forEach(System.out::println);

        assertThat(result)
                .isNotNull();

        assertThat(result.course())
                .isNotNull();

        assertThat(result.events())
                .isNotNull()
                .isNotEmpty();
    }
}
