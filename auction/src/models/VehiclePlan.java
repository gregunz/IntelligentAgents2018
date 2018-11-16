package models;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class VehiclePlan {

    private final Vehicle vehicle;
    private final List<BasicAction> actionSequence;
    private int currentLoad;
    private boolean canMutate;

    public VehiclePlan(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.actionSequence = new ArrayList<>();
        this.currentLoad = 0;
        this.canMutate = true;
    }

    private VehiclePlan(Vehicle vehicle, List<BasicAction> actionSequence, int currentLoad) {
        this.vehicle = vehicle;
        this.actionSequence = actionSequence;
        this.currentLoad = currentLoad;
        this.canMutate = true;
    }

    public VehiclePlan copy() {
        return new VehiclePlan(this.vehicle, new ArrayList<>(this.actionSequence), this.currentLoad);
    }

    public void setCanMutate(boolean canMutate) {
        this.canMutate = canMutate;
    }


    private void checkMutation() {
        if (!canMutate) {
            throw new IllegalStateException("only copy can be mutated");
        }
    }

    public boolean addLoadAction(Task task) {
        checkMutation();
        if (currentLoad + task.weight > vehicle.capacity()) {
            return false;
        } else {
            currentLoad += task.weight;
            actionSequence.add(new BasicAction(Event.LOAD, task));
            return true;
        }
    }

    public boolean advanceAction(int i) {
        checkMutation();
        if (i == 0 || i > actionSequence.size()) {
            return false;
        }
        BasicAction action = actionSequence.get(i);
        if (action.event == Event.DROP) {
            if (actionSequence.get(i - 1).task != action.task) {
                Collections.swap(actionSequence, i, i - 1);
                return true;
            } else {
                return false;
            }
        } else {
            int load = 0;
            for (int j = 0; j < i - 1; j++) {
                BasicAction action2 = actionSequence.get(j);
                if (action2.event == Event.DROP) {
                    load -= action2.task.weight;
                } else {
                    load += action2.task.weight;
                }
            }
            if (load + action.task.weight <= vehicle.capacity()) {
                Collections.swap(actionSequence, i, i - 1);
                return true;
            }

            return false;
        }
    }

    public boolean postponeAction(int i) {
        checkMutation();
        if (i >= actionSequence.size() - 1) {
            return false;
        }
        BasicAction action = actionSequence.get(i);
        if (action.event == Event.LOAD) {
            if (actionSequence.get(i + 1).task != action.task) {
                Collections.swap(actionSequence, i, i + 1);
                return true;
            } else {
                return false;
            }
        } else {
            int load = 0;
            for (int j = 0; j <= i + 1; j++) {
                BasicAction action2 = actionSequence.get(j);
                if (action2.event == Event.DROP) {
                    load -= action2.task.weight;
                } else {
                    load += action2.task.weight;
                }
            }
            if (load + action.task.weight <= vehicle.capacity()) {
                Collections.swap(actionSequence, i, i + 1);
                return true;
            }

            return false;
        }
    }

    public boolean addDropAction(Task task) {
        checkMutation();
        if (actionSequence.contains(new BasicAction(Event.LOAD, task))) {
            currentLoad -= task.weight;
            actionSequence.add(new BasicAction(Event.DROP, task));
            return true;
        }
        return false;
    }

    public Task takeOutFirstTask() {
        checkMutation();
        Task task = actionSequence.get(0).task;
        actionSequence.remove(0);
        actionSequence.remove(new BasicAction(Event.DROP, task));
        return task;
    }

    public Plan getPlan() {
        return getPlan(Function.identity());
    }

    public Plan getPlan(Function<Task, Task> oldToNewTasks) {

        Topology.City currentCity = this.vehicle.getCurrentCity();
        Plan plan = new Plan(currentCity);

        for (BasicAction action : actionSequence) {
            if (action.event == Event.LOAD) {
                if (action.task.pickupCity != currentCity) {
                    for (Topology.City city : currentCity.pathTo(action.task.pickupCity)) {
                        plan.appendMove(city);
                    }
                }
                currentCity = action.task.pickupCity;
                plan.appendPickup(oldToNewTasks.apply(action.task));
            } else {
                if (action.task.deliveryCity != currentCity) {
                    for (Topology.City city : currentCity.pathTo(action.task.deliveryCity)) {
                        plan.appendMove(city);
                    }
                }
                currentCity = action.task.deliveryCity;
                plan.appendDelivery(oldToNewTasks.apply(action.task));
            }
        }
        return plan;
    }

    public double getCost() {
        if (getLength() == 0) {
            return 0;
        }
        return this.getPlan().totalDistance() * this.vehicle.costPerKm();
    }

    public int getLength() {
        return actionSequence.size();
    }

    private boolean isOverloaded() {
        int load = 0;
        for (BasicAction action : actionSequence) {
            if (action.event == Event.LOAD) {
                load += action.task.weight;
            } else {
                load -= action.task.weight;
            }
            if (load > vehicle.capacity()) {
                return true;
            }
        }
        return false;
    }

    public boolean isValid() {
        if (isOverloaded()) {
            return false;
        }
        for (int i = 0; i < actionSequence.size(); i++) {
            Task task = actionSequence.get(i).task;
            if (actionSequence.indexOf(new BasicAction(Event.DROP, task)) < actionSequence.indexOf(new BasicAction(Event.LOAD, task))) {
                return false;
            }
        }
        return true;
    }

    public boolean swapActions(int t1, int t2) {
        checkMutation();
        BasicAction action1 = actionSequence.get(t1);
        BasicAction action2 = actionSequence.get(t2);
        if (action1.task == action2.task) {
            return false;
        } else {
            int l1 = actionSequence.indexOf(new BasicAction(Event.LOAD, action1.task));
            int l2 = actionSequence.indexOf(new BasicAction(Event.LOAD, action2.task));
            int d1 = actionSequence.indexOf(new BasicAction(Event.DROP, action1.task));
            int d2 = actionSequence.indexOf(new BasicAction(Event.DROP, action2.task));
            Collections.swap(actionSequence, l1, l2);
            Collections.swap(actionSequence, d1, d2);
            return isValid();
        }
    }

}
