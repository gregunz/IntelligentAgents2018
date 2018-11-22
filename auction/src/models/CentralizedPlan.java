package models;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

import java.util.*;
import java.util.stream.Collectors;

public class CentralizedPlan {

    private final List<Vehicle> vehicles;
    private final List<Task> tasks;
    private final Map<Vehicle, VehiclePlan> plans;
    private final double cost;

    public CentralizedPlan(List<Vehicle> vehicles, Map<Vehicle, VehiclePlan> plans, List<Task> tasks) {
        this.vehicles = new ArrayList<>(vehicles);
        this.tasks = new ArrayList<>(tasks);
        this.plans = plans;

        double cost = 0;
        for (VehiclePlan p : plans.values()) {
            p.setCanMutate(false);
            cost += p.getCost();
        }
        this.cost = cost;
    }

    public List<Vehicle> getVehicles() {
        return Collections.unmodifiableList(vehicles);
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public Map<Vehicle, VehiclePlan> getPlans() {
        return Collections.unmodifiableMap(plans);
    }

    public double getCost() {
        return this.cost;
    }

    public List<Plan> toLogistPlans() {
        return this.vehicles.stream().map(v -> this.plans.get(v).getPlan()).collect(Collectors.toList());
    }

    public List<Plan> toLogistPlans(TaskSet trueTasks) {
        List<Plan> plans = new ArrayList<>();
        if (trueTasks.isEmpty()) {
            for (Vehicle v : vehicles) {
                plans.add(Plan.EMPTY);
            }
            return plans;
        }
        Map<Task, Task> oldToNewTasksMap = new HashMap<>();
        for (Task t1 : trueTasks) {
            for (Task t2 : this.tasks) {
                if (t1.id == t2.id) {
                    oldToNewTasksMap.put(t2, t1);
                }
            }
        }
        for (Vehicle v : vehicles) {
            plans.add(this.plans.get(v).getPlan(oldToNewTasksMap::get));
        }
        return plans;
    }

    public CentralizedPlan copy() {
        Map<Vehicle, VehiclePlan> newPlans = new HashMap<>();
        vehicles.forEach(v -> {
            newPlans.put(v, this.plans.get(v).copy());
        });
        return new CentralizedPlan(this.vehicles, newPlans, this.tasks);
    }

    public CentralizedPlan modifyVehiclePlan(Vehicle v, VehiclePlan vPlan) {
        Map<Vehicle, VehiclePlan> newPlans = new HashMap<>(plans);
        newPlans.put(v, vPlan);
        return new CentralizedPlan(vehicles, newPlans, tasks);
    }
}
