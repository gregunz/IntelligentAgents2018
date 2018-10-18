package template;

import logist.task.Task;
import logist.task.TaskSet;

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

        return new StateRepresentation(state.getCurrentCity(), taken,
                state.getCapacityRemaining()+task.weight, state.getTaskNotTaken());
    }

    @Override
    public double getReward() {
        return task.reward;
    }
}
