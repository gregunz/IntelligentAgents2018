package models;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;

import java.util.ArrayList;
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

    public boolean addLoadAction(Task task) {
        if (currentLoad + task.weight > vehicle.capacity()) {
            return false;
        } else {
            currentLoad += task.weight;
            sequence.add(new BasicAction(Event.LOAD, task));
            return true;
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

        Plan plan = Plan.EMPTY;

        Topology.City currentCity = this.vehicle.getCurrentCity();
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
        double cost = 0;
        if (sequence.size() > 0) {
            assert sequence.get(0).event == Event.LOAD;
            cost += vehicle.getCurrentCity().distanceTo(sequence.get(0).task.pickupCity) * vehicle.costPerKm();
        }
        for (BasicAction action : sequence) {
            cost += action.task.pathLength() * vehicle.costPerKm();
        }
        return cost;
    }

    public int getLength() {
        return sequence.size();
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
