package models;

import logist.plan.Plan;
import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.List;

public interface State {

    boolean isFinalState();

    List<Action> getPreviousActions();

    List<Action> getAllPossibleActions();

    List<State> getNextStates();

    double getCostPerKM();

    double getCost();

    Topology.City getCurrentCity();
    TaskSet getTaskTaken();
    int getCapacityRemaining();
    TaskSet getTaskNotTaken();

    Plan toPlan(State startingState);
}
