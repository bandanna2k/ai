package dnt.ai.runnerapp.handlers;

import dnt.ai.runnerapp.Main;
import dnt.ai.runnerapp.model.Course;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

import static dnt.ai.runnerapp.Main.respondJson;

public class CoursesApiHandler implements Main.CoursesApi
{
    private final List<Course> courses;

    public CoursesApiHandler(List<Course> courses)
    {
        this.courses = courses;
    }

    @Override
    public void handle(RoutingContext ctx)
    {
        String country = ctx.request().getParam("country");
        List<Course> result = (country == null || country.isBlank())
                ? courses
                : courses.stream()
                        .filter(course -> course.country.equalsIgnoreCase(country))
                        .toList();
        respondJson(ctx, 200, result);
    }
}
