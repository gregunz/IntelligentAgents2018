package template;

import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.List;

public interface State {

    boolean isFinalState();

    List<Action> getAllPossibleActions();

    State getNextState(Action action);

    double getCurrentReward();

    void addReward(double reward);

    Topology.City getCurrentCity();
    TaskSet getTaskTaken();
    int getCapacityRemaining();
    TaskSet getTaskNotTaken();

}
