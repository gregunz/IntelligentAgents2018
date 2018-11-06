package algo.astar;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;
import models.ActionSequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


public class StateRepresentation implements State {
    private static final Task[] EMPTY_TASK_ARRAY = {};

    private final Topology.City currentCity;
    private final List<Task> taskTaken;
    private final int capacityRemaining;
    private final List<Task> taskNotTaken;
    private final double currentCost;
    private final List<Action> previousActions;
    private final double costPerKM;


    public StateRepresentation(Vehicle vehicle, List<Task> taskset) {
        this(vehicle.getCurrentCity(), new ArrayList<>(vehicle.getCurrentTasks()), vehicle.capacity(), taskset, vehicle.costPerKm());
    }

    public StateRepresentation(Topology.City currentCity, List<Task> taskTaken, int capacityRemaining,
                               List<Task> taskNotTaken, double costPerKM) {
        this(currentCity, taskTaken, capacityRemaining, taskNotTaken, costPerKM, 0d, new ArrayList<>());
    }

    public StateRepresentation(Topology.City currentCity,
                               List<Task> taskTaken,
                               int capacityRemaining,
                               List<Task> taskNotTaken,
                               double costPerKM,
                               double currentCost,
                               List<Action> previousActions) {
        this.currentCity = currentCity;
        this.taskTaken = taskTaken;
        this.capacityRemaining = capacityRemaining;
        this.taskNotTaken = taskNotTaken;
        this.currentCost = currentCost;
        this.previousActions = Collections.unmodifiableList(previousActions);
        this.costPerKM = costPerKM;
    }

    @Override
    public boolean isFinalState() {
        return taskTaken.isEmpty() && getTaskNotTaken().isEmpty();
    }

    @Override
    public List<Action> getAllPossibleActions() {

        List<Action> possibleActions = new ArrayList<>();
        List<Topology.City> destinationInTaskPath = new ArrayList<>();

        for (Task task : taskTaken) {
            if (task.deliveryCity == currentCity) { // Add all "drop task in current city" actions
                possibleActions.add(new DeliveryAction(task));
                return possibleActions;
            } else { // Add all "move in direction to city to drop task" actions
                destinationInTaskPath.add(currentCity.pathTo(task.deliveryCity).get(0));
            }
        }

        for (Task task : taskNotTaken) {
            if (task.weight <= capacityRemaining) {
                if (task.pickupCity == currentCity) { // Add all "take task in current city" actions
                    possibleActions.add(new PickupAction(task));
                } else { // Add all "move in direction to city to pickup task" actions
                    destinationInTaskPath.add(currentCity.pathTo(task.pickupCity).get(0));
                }
            }
        }

        // Add all "move to neighbor city which are in path to drop or take a task" actions
        for (Topology.City city : destinationInTaskPath) {
            possibleActions.add(new MoveAction(currentCity, city, costPerKM));
        }

        return possibleActions;
    }

    @Override
    public List<State> getNextStates() {
        return this.getAllPossibleActions().stream()
                .map(a -> a.getNextState(this))
                .collect(Collectors.toList());
    }

    @Override
    public Topology.City getCurrentCity() {
        return this.currentCity;
    }

    @Override
    public List<Task> getTaskTaken() {
        return this.taskTaken;
    }

    @Override
    public int getCapacityRemaining() {
        return this.capacityRemaining;
    }

    @Override
    public List<Task> getTaskNotTaken() {
        return this.taskNotTaken;
    }

    @Override
    public Plan toPlan(State startingState) {
        Plan plan = new Plan(startingState.getCurrentCity());

        this.getPreviousActions().stream()
                .map(Action::getAction)
                .collect(Collectors.toCollection(LinkedList::new))
                .forEach(plan::append);

        return plan;
    }

    @Override
    public ActionSequence toActionSequence(Vehicle v) {
        ActionSequence actionSeq = new ActionSequence(v);
        previousActions.stream().forEach(action -> {
            if (action.isPickup()) {
                actionSeq.addLoadAction(action.getTask().get());
            } else if (action.isDeliver()) {
                actionSeq.addDropAction(action.getTask().get());
            }
        });
        return actionSeq;
    }

    @Override
    public double getCurrentCost() {
        return currentCost;
    }

    @Override
    public double getCostPerKM() {
        return costPerKM;
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
