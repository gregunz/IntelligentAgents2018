package algo.astar;

import logist.task.Task;
import logist.task.TaskSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PickupAction implements Action {

    private Task task;

    public PickupAction(Task task) {
        this.task = task;
    }

    @Override
    public logist.plan.Action getAction() {
        return new logist.plan.Action.Pickup(task);
    }

    @Override
    public State getNextState(State state) {

        TaskSet taken = state.getTaskTaken().clone();
        taken.add(task);

        TaskSet notTaken = state.getTaskNotTaken().clone();
        notTaken.remove(task);

        List<Action> actions = new ArrayList<>(state.getPreviousActions());
        actions.add(this);

        return new StateRepresentation(
                state.getCurrentCity(),
                taken,
                state.getCapacityRemaining() - task.weight,
                notTaken,
                state.getCostPerKM(),
                state.getCurrentCost() + this.getCost(),
                actions
        );
    }

    @Override
    public double getCost() {
        return 0;
    }

    @Override
    public boolean isMove() {
        return false;
    }

    @Override
    public boolean isDeliver() {
        return false;
    }

    @Override
    public boolean isPickup() {
        return true;
    }

    @Override
    public Optional<Task> getTask() {
        return Optional.of(task);
    }
}
