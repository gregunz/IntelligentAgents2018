package template;

import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.ArrayList;
import java.util.List;

public class StateRepresentation implements State {

    private Topology.City currentCity;
    private TaskSet taskTaken;
    private int capacityRemaining;
    private TaskSet taskNotTaken;
    private double currentReward;

    public StateRepresentation(Topology.City currentCity, TaskSet taskTaken, int capacityRemaining, TaskSet taskNotTaken) {
        this.currentCity = currentCity;
        this.taskTaken = taskTaken;
        this.capacityRemaining = capacityRemaining;
        this.taskNotTaken = taskNotTaken;
        this.currentReward = 0d;
    }

    @Override
    public boolean isFinalState() {
        return taskTaken.isEmpty() && getTaskNotTaken().isEmpty();
    }

    @Override
    public List<Action> getAllPossibleActions() {

        List<Action> possibleActions = new ArrayList<>();
        List<Topology.City> destinationInTaskPath = new ArrayList<>();

        for (Task task : taskNotTaken) {
            if (task.weight <= capacityRemaining) {
                if (task.pickupCity == currentCity) { // Add all "take task in current city" actions
                    possibleActions.add(new PickupAction(task));
                } else { // Add all "move in direction to city to pickup task" actions
                    destinationInTaskPath.add(currentCity.pathTo(task.pickupCity).get(0));
                }
            }
        }

        for (Task task : taskTaken) {
            if (task.deliveryCity == currentCity) { // Add all "drop task in current city" actions
                possibleActions.add(new DeliveryAction(task));
            } else { // Add all "move in direction to city to drop task" actions
                destinationInTaskPath.add(currentCity.pathTo(task.deliveryCity).get(0));
            }
        }

        // Add all "move to neighbor city which are in path to drop or take a task" actions
        for (Topology.City city : destinationInTaskPath) {
            possibleActions.add(new MoveAction(currentCity, city));
        }

        return possibleActions;
    }

    @Override
    public State getNextState(Action action) {
        return action.getNextState(this);
    }

    @Override
    public Topology.City getCurrentCity() {
        return this.currentCity;
    }

    @Override
    public TaskSet getTaskTaken() {
        return this.taskTaken;
    }

    @Override
    public int getCapacityRemaining() {
        return this.capacityRemaining;
    }

    @Override
    public TaskSet getTaskNotTaken() {
        return this.taskNotTaken;
    }

    @Override
    public double getCurrentReward() {
        return currentReward;
    }

    @Override
    public void addReward(double reward) {
        this.currentReward += reward;
    }
}
