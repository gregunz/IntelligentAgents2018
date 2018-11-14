package algo.astar;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;
import models.VehiclePlan;

import java.util.List;

public interface State {

    boolean isFinalState();

    List<Action> getPreviousActions();

    List<Action> getAllPossibleActions();

    List<State> getNextStates();

    double getCostPerKM();

    double getCurrentCost();

    Topology.City getCurrentCity();

    List<Task> getTaskTaken();

    int getCapacityRemaining();

    List<Task> getTaskNotTaken();

    Plan toPlan(State startingState);

    VehiclePlan toVehiclePlan(Vehicle v);
}
