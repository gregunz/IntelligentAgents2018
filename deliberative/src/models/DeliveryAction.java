package models;

import logist.task.Task;
import logist.task.TaskSet;

import java.util.ArrayList;
import java.util.List;

public class DeliveryAction implements Action {

    private Task task;

    public DeliveryAction(Task task) {
        this.task = task;
    }

    @Override
    public logist.plan.Action getAction() {
        return new logist.plan.Action.Delivery(task);
    }

    @Override
    public State getNextState(State state) {

        TaskSet taken = state.getTaskTaken().clone();
        taken.remove(task);

        List<Action> actions = new ArrayList<>(state.getPreviousActions());
        actions.add(this);

        return new StateRepresentation(
                state.getCurrentCity(),
                taken,
                state.getCapacityRemaining() + task.weight,
                state.getTaskNotTaken(),
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
        return true;
    }

    @Override
    public boolean isPickup() {
        return false;
    }
}
