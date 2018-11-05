package algo;

import logist.simulation.Vehicle;
import logist.task.TaskSet;
import models.ActionSequence;

import java.util.List;
import java.util.Set;

public interface ISLS {
    void init(List<Vehicle> vehicles, TaskSet tasks);

    Set<List<ActionSequence>> chooseNeighbours();

    void localChoice(Set<List<ActionSequence>> neighbors);
}
