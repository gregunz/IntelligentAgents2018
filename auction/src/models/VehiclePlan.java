package models;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;
import print.PrintHandler;
import random.RandomHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class VehiclePlan {

    private final Vehicle vehicle;
    private final List<BasicAction> actionSequence;
    private int currentLoad;
    private boolean canMutate;
    private boolean withChecks = false;

    public VehiclePlan(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.actionSequence = new ArrayList<>(200);
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


    public double getCost() {
        if (getLength() == 0) {
            return 0;
        }
        double cost = 0;
        Topology.City lastCity = vehicle.getCurrentCity();
        for (BasicAction action : actionSequence) {
            if (action.event == Event.LOAD) {
                cost += lastCity.distanceTo(action.task.pickupCity);
                lastCity = action.task.pickupCity;
            } else {
                cost += lastCity.distanceTo(action.task.deliveryCity);
                lastCity = action.task.deliveryCity;
            }
        }
        cost *= this.vehicle.costPerKm();
        return cost;
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

    public boolean isInvalid() {
        if (isOverloaded()) {
            PrintHandler.println("[FAIL] vehicle is overloaded");
            return true;
        }
        for (int i = 0; i < actionSequence.size(); i++) {
            Task task = actionSequence.get(i).task;
            if (actionSequence.indexOf(new BasicAction(Event.DROP, task)) < actionSequence.indexOf(new BasicAction(Event.LOAD, task))) {
                PrintHandler.println("[FAIL] tasks not in right order");
                return true;
            }
        }
        return false;
    }

    private void checkMutation() {
        if (!canMutate) {
            throw new IllegalStateException("only copy can be mutated");
        }
    }

    private void checkValidity() {
        if (withChecks && isInvalid()) {
            throw new IllegalStateException("plan not valid");
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

    public void addTaskRandomly(Task task) {
        checkMutation();
        checkValidity();
        int load = 0;
        boolean moved = false;
        boolean dropped = false;
        int numOfActions = actionSequence.size();
        for (int i = 0; i < numOfActions; i++) {
            BasicAction action = actionSequence.get(i);
            if (action.event == Event.LOAD) {
                load += action.task.weight;
            } else {
                load -= action.task.weight;
            }
            if ((RandomHandler.get().nextDouble() < 1. / numOfActions) && !moved && load + task.weight <= vehicle.capacity()) {
                moved = true;
                load += task.weight;
                actionSequence.add(i + 1, new BasicAction(Event.LOAD, task));
            } else if (moved && !action.task.equals(task)) {
                if (load > vehicle.capacity()) {
                    actionSequence.add(i, new BasicAction(Event.DROP, task));
                    dropped = true;
                    break;
                } else if (RandomHandler.get().nextDouble() < 1. / numOfActions) {
                    actionSequence.add(i + 1, new BasicAction(Event.DROP, task));
                    dropped = true;
                    break;
                }
            }
        }
        if (!moved) {
            actionSequence.add(new BasicAction(Event.LOAD, task));
        }
        if (!dropped) {
            actionSequence.add(new BasicAction(Event.DROP, task));
        }
        checkValidity();
    }

    public boolean advanceAction(int i) {
        checkMutation();
        checkValidity();
        if (i == 0 || i > actionSequence.size()) {
            return false;
        }
        BasicAction action = actionSequence.get(i);
        if (action.event == Event.DROP) {
            if (actionSequence.get(i - 1).task != action.task) {
                Collections.swap(actionSequence, i, i - 1);
                checkValidity();
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
                checkValidity();
                return true;
            }

            return false;
        }
    }

    public boolean postponeAction(int i) {
        checkMutation();
        checkValidity();
        if (i >= actionSequence.size() - 1) {
            return false;
        }
        BasicAction action = actionSequence.get(i);
        if (action.event == Event.LOAD) {
            if (actionSequence.get(i + 1).task != action.task) {
                Collections.swap(actionSequence, i, i + 1);
                checkValidity();
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
                checkValidity();
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

    public Task takeOutOneTask() {
        checkMutation();
        checkValidity();
        int idx = RandomHandler.get().nextInt(actionSequence.size());
        BasicAction action = actionSequence.get(idx);
        actionSequence.remove(idx);
        if (!actionSequence.remove(new BasicAction(action.event == Event.DROP ? Event.LOAD : Event.DROP, action.task))) {
            PrintHandler.println("[FAIL] should remove DROP as well");
        }
        checkValidity();
        return action.task;
    }

    public Plan getPlan() {
        return getPlan(Function.identity());
    }

    public Plan getPlan(Function<Task, Task> oldToNewTasks) {
        if (actionSequence.isEmpty()) {
            return Plan.EMPTY;
        }
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

    public void swapTwoTasks() {
        checkMutation();
        checkValidity();
        if (actionSequence.size() < 4) {
            PrintHandler.println("[FAIL] cannot swap two tasks if less are present");
        }

        Task task1 = actionSequence.get(RandomHandler.get().nextInt(actionSequence.size())).task;
        Task task2 = task1;
        while (task1.equals(task2)) {
            task2 = actionSequence.get(RandomHandler.get().nextInt(actionSequence.size())).task;
        }
        int l1 = -1,
                l2 = -1,
                d1 = -1,
                d2 = -1;
        int index = 0;
        for (BasicAction action : actionSequence) {
            if (action.task.equals(task1)) {
                if (action.event.equals(Event.LOAD)) {
                    l1 = index;
                } else {
                    d1 = index;
                }
            } else if (action.task.equals(task2)) {
                if (action.event.equals(Event.LOAD)) {
                    l2 = index;
                } else {
                    d2 = index;
                }
            }
            index++;
        }
        Collections.swap(actionSequence, l1, l2);
        Collections.swap(actionSequence, d1, d2);
        checkValidity();
    }

}
