package models;

import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.List;
import java.util.Set;

public interface State {

    boolean isFinalState();

    List<Action> getPreviousActions();

    Set<Action> getAllPossibleActions();

    Set<State> getNextStates();

    double getCurrentReward();

    Topology.City getCurrentCity();
    TaskSet getTaskTaken();
    int getCapacityRemaining();
    TaskSet getTaskNotTaken();

}
