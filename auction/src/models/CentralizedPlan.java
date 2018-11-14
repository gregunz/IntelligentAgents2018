package models;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CentralizedPlan {
    private List<Vehicle> vehicles;
    private List<Task> tasks;
    private Map<Vehicle, VehiclePlan> plans;
    private int cost;

    public CentralizedPlan(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
        this.tasks = new ArrayList<>();

        this.plans = new HashMap<>();
        vehicles.forEach(v -> {
            this.plans.put(v, new VehiclePlan(v));
        });
        this.cost = 0;
    }

    private CentralizedPlan(List<Vehicle> vehicles, Map<Vehicle, VehiclePlan> plans, List<Task> tasks, int cost) {
        this.vehicles = vehicles;
        this.tasks = tasks;
        this.plans = plans;
        this.cost = cost;
    }

    public CentralizedPlan copy() {
        Map<Vehicle, VehiclePlan> newPlans = new HashMap<>();
        vehicles.forEach(v -> {
            newPlans.put(v, this.plans.get(v).copy());
        });
        return new CentralizedPlan(this.vehicles, newPlans, this.tasks, this.cost);
    }

    public int getCost() {
        return cost;
    }

    public List<Plan> toLogistPlans() {
        return this.vehicles.stream().map(v -> this.plans.get(v).getPlan()).collect(Collectors.toList());
    }

    public void addTask(Task t) {
        this.tasks.add(t);
        //TODO give task to a vehicle such that the plan is ideally optimal
    }

    public CentralizedPlan tryToImprovePlan() { // sls algo here (I guess)

        //TODO iteration improvement of plan
        //this will update "this.cost"

        return null; //TODO
    }
}
