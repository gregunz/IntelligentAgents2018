package models;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.*;
import java.util.stream.Collectors;



public class StateRepresentation implements State {
    private static final Task[] EMPTY_TASK_ARRAY = {};

    private final Topology.City currentCity;
    private final TaskSet taskTaken;
    private final int capacityRemaining;
    private final TaskSet taskNotTaken;
    private final double currentReward;
    private final List<Action> previousActions;


    public StateRepresentation(Vehicle vehicle, TaskSet taskset) {
        this(vehicle.getCurrentCity(), TaskSet.create(EMPTY_TASK_ARRAY), vehicle.capacity(), taskset);
    }

    public StateRepresentation(Topology.City currentCity, TaskSet taskTaken, int capacityRemaining, TaskSet taskNotTaken) {
        this(currentCity, taskTaken, capacityRemaining, taskNotTaken, 0d, new ArrayList<>());
    }

    public StateRepresentation(Topology.City currentCity, TaskSet taskTaken, int capacityRemaining, TaskSet taskNotTaken, double currentReward, List<Action> previousActions) {
        this.currentCity = currentCity;
        this.taskTaken = taskTaken;
        this.capacityRemaining = capacityRemaining;
        this.taskNotTaken = taskNotTaken;
        this.currentReward = currentReward;
        this.previousActions = Collections.unmodifiableList(previousActions);
    }

    @Override
    public boolean isFinalState() {
        return taskTaken.isEmpty() && getTaskNotTaken().isEmpty();
    }

    @Override
    public Set<Action> getAllPossibleActions() {

        Set<Action> possibleActions = new HashSet<>();
        Set<Topology.City> destinationInTaskPath = new HashSet<>();

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
    public Set<State> getNextStates() {
        return this.getAllPossibleActions().stream()
                .map(a -> a.getNextState(this))
                .collect(Collectors.toSet());
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
    public Plan toPlan(State startingState) {
        Plan plan = new Plan(startingState.getCurrentCity());

        this.getPreviousActions().stream()
                .map(models.Action::getAction)
                .collect(Collectors.toCollection(LinkedList::new))
                .descendingIterator()
                .forEachRemaining(plan::append);

        return plan;
    }

    @Override
    public double getCurrentReward() {
        return currentReward;
    }

    @Override
    public List<Action> getPreviousActions() {
        return Collections.unmodifiableList(previousActions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateRepresentation that = (StateRepresentation) o;

        if (capacityRemaining != that.capacityRemaining) return false;
        if (currentCity != null ? !currentCity.equals(that.currentCity) : that.currentCity != null) return false;
        if (taskTaken != null ? !taskTaken.equals(that.taskTaken) : that.taskTaken != null) return false;
        return taskNotTaken != null ? taskNotTaken.equals(that.taskNotTaken) : that.taskNotTaken == null;
    }

    @Override
    public int hashCode() {
        int result = currentCity != null ? currentCity.hashCode() : 0;
        result = 31 * result + (taskTaken != null ? taskTaken.hashCode() : 0);
        result = 31 * result + capacityRemaining;
        result = 31 * result + (taskNotTaken != null ? taskNotTaken.hashCode() : 0);
        return result;
    }
}
