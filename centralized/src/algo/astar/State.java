package algo.astar;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskSet;
import logist.topology.Topology;
import models.ActionSequence;

import java.util.List;

public interface State {

    boolean isFinalState();

    List<Action> getPreviousActions();

    List<Action> getAllPossibleActions();

    List<State> getNextStates();

    double getCostPerKM();

    double getCurrentCost();

    Topology.City getCurrentCity();

    TaskSet getTaskTaken();

    int getCapacityRemaining();

    TaskSet getTaskNotTaken();

    Plan toPlan(State startingState);

    ActionSequence toActionSequence(Vehicle v);
}
