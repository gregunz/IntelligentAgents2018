package models;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ActionSequence {


    private Vehicle vehicle;
    private List<BasicAction> sequence;
    private int currentLoad = 0;

    public ActionSequence(Vehicle vehicle){
        this.vehicle = vehicle;
        this.sequence = new ArrayList<>();
    }

    public ActionSequence(ActionSequence actionSequence){
        this.vehicle = actionSequence.vehicle;
        this.sequence = new ArrayList<>(actionSequence.sequence);
        this.currentLoad = actionSequence.currentLoad;
    }

    public boolean addLoadAction(Task task) {
        if (currentLoad + task.weight > vehicle.capacity()) {
            return false;
        } else {
            currentLoad += task.weight;
            sequence.add(new BasicAction(Event.LOAD, task));
            return true;
        }
    }

    public boolean advanceAction(int i) {
        if (i == 0 || i > sequence.size()) {
            return false;
        }
        BasicAction action = sequence.get(i);
        if (action.event == Event.DROP) {
            if (sequence.get(i-1).task != action.task) {
                Collections.swap(sequence, i, i-1);
                return true;
            } else {
                return false;
            }
        } else {
            int load = 0;
            for (int j = 0; j < i; j++) {
                BasicAction action2 = sequence.get(j);
                if (action2.event == Event.DROP) {
                    load -= action2.task.weight;
                } else {
                    load += action2.task.weight;
                }
                if (load + action.task.weight <= vehicle.capacity()) {
                    Collections.swap(sequence, i, i-1);
                    return true;
                }
            }
            return false;
        }
    }

    public boolean postponeAction(int i) {
        if (i >= sequence.size()-1) {
            return false;
        }
        BasicAction action = sequence.get(i);
        if (action.event == Event.LOAD) {
            if (sequence.get(i+1).task != action.task) {
                Collections.swap(sequence, i, i+1);
                return true;
            } else {
                return false;
            }
        } else {
            int load = 0;
            for (int j = 0; j < i; j++) {
                BasicAction action2 = sequence.get(j);
                if (action2.event == Event.DROP) {
                    load -= action2.task.weight;
                } else {
                    load += action2.task.weight;
                }
                if (load + action.task.weight <= vehicle.capacity()) {
                    Collections.swap(sequence, i, i+1);
                    return true;
                }
            }
            return false;
        }
    }

    public boolean addDropAction(Task task) {
        if (sequence.contains(new BasicAction(Event.LOAD, task))) {
            currentLoad -= task.weight;
            sequence.add(new BasicAction(Event.DROP, task));
            return true;
        }
        return false;
    }

    public Task takeOutFirstTask() {
        Task task = sequence.get(0).task;
        sequence.remove(0);
        sequence.remove(new BasicAction(Event.DROP, task));
        return task;
    }

    public Plan getPlan() {

        Topology.City currentCity = this.vehicle.getCurrentCity();
        Plan plan = new Plan(currentCity);

        for (BasicAction action : sequence) {
            if (action.event == Event.LOAD) {
                if (action.task.pickupCity != currentCity) {
                    for (Topology.City city : currentCity.pathTo(action.task.pickupCity)) {
                        plan.appendMove(city);
                    }
                }
                currentCity = action.task.pickupCity;
                plan.appendPickup(action.task);
            } else {
                if (action.task.deliveryCity != currentCity) {
                    for (Topology.City city : currentCity.pathTo(action.task.deliveryCity)) {
                        plan.appendMove(city);
                    }
                }
                currentCity = action.task.deliveryCity;
                plan.appendDelivery(action.task);
            }
        }
        return plan;
    }

    public double getCost() {
        return this.getPlan().totalDistance() * this.vehicle.costPerKm();
    }

    public int getLength() {
        return sequence.size();
    }

    private boolean isOverloaded() {
        int load = 0;
        for (BasicAction action : sequence) {
            if (action.event == Event.LOAD) {
                load += action.task.weight;
            } else {
                load -= action.task.weight;
            }
            if (load >= vehicle.capacity()) {
                return true;
            }
        }
        return false;
    }


    private class BasicAction {

        private final Task task;
        private final Event event;

        private BasicAction(Event event, Task task) {
            this.task = task;
            this.event = event;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BasicAction that = (BasicAction) o;
            return Objects.equals(task, that.task) &&
                    event == that.event;
        }

        @Override
        public int hashCode() {

            return Objects.hash(task, event);
        }
    }

    enum Event {LOAD, DROP}
}
