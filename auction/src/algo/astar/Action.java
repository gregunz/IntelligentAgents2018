package algo.astar;

import logist.task.Task;

import java.util.Optional;

public interface Action {

    logist.plan.Action getAction();

    State getNextState(State state);

    double getCost();

    boolean isMove();

    boolean isDeliver();

    boolean isPickup();

    Optional<Task> getTask();
}
