package models;

import logist.task.Task;
import logist.task.TaskSet;

import java.util.ArrayList;
import java.util.List;

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
                state.getCurrentReward() + this.getReward(),
                actions
        );
    }

    @Override
    public double getReward() {
        return 0;
    }
}
