package models;

import algo.astar.AStar;
import algo.astar.Heuristic;
import logist.simulation.Vehicle;
import logist.task.Task;
import print.PrintHandler;
import random.RandomHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanGenerator {
    private PlanGenerator() {
    }

    public static CentralizedPlan generate(List<Vehicle> vehicles, List<Task> tasks, InitStrategy initStrategy) {

        PrintHandler.println("initialization with " + initStrategy, 2);
        Vehicle largest = vehicles.get(0);
        for (Vehicle v : vehicles) {
            if (v.capacity() > largest.capacity()) {
                largest = v;
            }
        }
        VehiclePlan initialPlan;

        switch (initStrategy) {
            case ASTAR:
                initialPlan = AStar.run(largest, tasks, Heuristic.WEIGHT_NOT_TAKEN);
                break;
            // Take the vehicle with the largest capacity and plan deliver the task completely at random
            case RANDOM:
                List<Task> taskTaken = new ArrayList<>();
                List<Task> taskNotTaken = new ArrayList<>(tasks);

                initialPlan = new VehiclePlan(largest);
                while (!taskTaken.isEmpty() || !taskNotTaken.isEmpty()) {
                    int possibleChoice = taskTaken.size() + taskNotTaken.size();

                    int n = RandomHandler.get().nextInt(possibleChoice);
                    if (n >= taskTaken.size()) {
                        Task task = taskNotTaken.get(n - taskTaken.size());
                        if (initialPlan.addLoadAction(task)) {
                            taskNotTaken.remove(n - taskTaken.size());
                            taskTaken.add(task);
                        }
                    } else {
                        Task task = taskTaken.get(n);
                        if (initialPlan.addDropAction(task)) {
                            taskTaken.remove(task);
                        }
                    }
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }

        if (!initialPlan.isValid()) {
            System.out.println("The initial plan is NOT valid");
        }
        // create plan for each vehicles
        Map<Vehicle, VehiclePlan> plans = new HashMap<>();
        for (Vehicle v : vehicles) {
            if (v == largest) {
                plans.put(v, initialPlan);
            } else {
                plans.put(v, new VehiclePlan(v));
            }
        }
        return new CentralizedPlan(vehicles, plans, tasks);
    }

    public static CentralizedPlan addTask(CentralizedPlan plan, Task t, AddStrategy addStrategy) {

        List<Vehicle> vehicles = plan.getVehicles();
        List<Task> tasks = new ArrayList<>(plan.getTasks());
        tasks.add(t);

        switch (addStrategy) {
            case RANDOM_INIT:
                return generate(vehicles, tasks, InitStrategy.ASTAR);
            case ASTAR_INIT:
                return generate(vehicles, tasks, InitStrategy.ASTAR);
            //case ?:
            //TODO, could give the task to a random vehicle (= no need to initialize again)
            default:
                throw new UnsupportedOperationException();
        }

    }
}
