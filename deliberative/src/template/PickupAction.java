package template;

import logist.task.Task;
import logist.task.TaskSet;

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

        return new StateRepresentation(state.getCurrentCity(), taken,
                state.getCapacityRemaining()-task.weight, notTaken);
    }

    @Override
    public double getReward() {
        return 0;
    }
}
