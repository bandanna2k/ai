package dnt.ai.runnerapp.command;

import dnt.ai.runnerapp.dao.Athlete;
import dnt.ai.runnerapp.dao.AthleteDao;
import dnt.ai.runnerapp.handlers.AthletesByIdHandler;
import dnt.ai.runnerapp.handlers.AthletesByNameHandler;

import java.util.List;

public class AthletesCommandHandler
{
    private final AthleteDao athleteDao;

    public AthletesCommandHandler(AthleteDao athleteDao)
    {
        this.athleteDao = athleteDao;
    }

    public List<Athlete> handle(AthletesByIdHandler.GetAthletesByIdCommand command)
    {
        return athleteDao.findByIds(command.ids());
    }

    public List<Athlete> handle(AthletesByNameHandler.GetAthletesByNameCommand command)
    {
        return athleteDao.findByName(command.name());
    }
}