package dnt.ai.runnerapp.command;

import java.util.List;

public record GetAthletesByIdCommand(List<Long> ids)
{
}