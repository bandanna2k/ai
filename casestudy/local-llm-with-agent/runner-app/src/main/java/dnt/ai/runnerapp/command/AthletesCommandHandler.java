package dnt.ai.runnerapp.command;

import dnt.ai.runnerapp.dao.AthleteDao;
import dnt.ai.runnerapp.model.Athlete;
import java.util.List;

public class AthletesCommandHandler
{
    private final AthleteDao athleteDao;

    public AthletesCommandHandler(AthleteDao athleteDao)
    {
        this.athleteDao = athleteDao;
    }

    public List<Athlete> handle(GetAthletesByIdCommand command)
    {
        return athleteDao.findByIds(command.ids());
    }

    public List<Athlete> handle(GetAthletesByNameCommand command)
    {
        return athleteDao.findByName(command.name());
    }
}