package models;

import algo.astar.AStar;
import algo.astar.Heuristic;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import random.RandomHandler;

import java.util.*;
import java.util.stream.Collectors;

public class CentralizedPlan {

    private static final boolean DISPLAY_PRINT = false;
    private static final double EXPLOITATION_RATE = 1;
    private static final int EXPLOITATION_DEEPNESS = (int) 1e5;

    private List<Vehicle> vehicles;
    private List<Task> tasks;
    private Map<Vehicle, VehiclePlan> plans;

    public CentralizedPlan(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
        this.tasks = new ArrayList<>();

        this.plans = new HashMap<>();
        vehicles.forEach(v -> {
            this.plans.put(v, new VehiclePlan(v));
        });
    }

    private CentralizedPlan(List<Vehicle> vehicles, Map<Vehicle, VehiclePlan> plans, List<Task> tasks) {
        this.vehicles = vehicles;
        this.tasks = tasks;
        this.plans = plans;
    }

    public static double computeCostOf(CentralizedPlan plan) {
        double cost = 0;
        for (VehiclePlan p : plan.plans.values()) {
            cost += p.getCost();
        }
        return cost;
    }

    public CentralizedPlan copy() {
        Map<Vehicle, VehiclePlan> newPlans = new HashMap<>();
        vehicles.forEach(v -> {
            newPlans.put(v, this.plans.get(v).copy());
        });
        return new CentralizedPlan(this.vehicles, newPlans, this.tasks);
    }

    public List<Plan> toLogistPlans() {
        return this.vehicles.stream().map(v -> this.plans.get(v).getPlan()).collect(Collectors.toList());
    }

    public void addTask(Task t, Initialization init) {

        switch (init) {
            case ASTAR:
            case RANDOM:
                this.tasks.add(t);
                this.initialize(init);
                break;
            case NONE:
            default:
                //TODO, could give the task to a random vehicle (= no need to initialize again)
                throw new UnsupportedOperationException("need to add the task somewhere!!!!");
        }

    }

    public CentralizedPlan nextPlan(long timeLimit, Initialization init) {
        long startTime = System.currentTimeMillis();
        int numIter = 0;

        CentralizedPlan bestLocalPlan = this.copy();
        double minCost = bestLocalPlan.getCost();

        this.initialize(init);

        while (numIter < EXPLOITATION_DEEPNESS && durationStoppingCriterion(startTime, timeLimit)) {
            Set<CentralizedPlan> neighbors = chooseNeighbours();
            CentralizedPlan nextPlan = localChoice(neighbors);
            if (nextPlan.getCost() < minCost) {
                bestLocalPlan = nextPlan.copy();
                minCost = nextPlan.getCost();
            }
            setNewPlans(nextPlan); // we always set nextPlan as newPlan because it might lead to local minima
            numIter += 1;
        }
        return bestLocalPlan; // but we only return the best plan
    }

    public double getCost() {
        return computeCostOf(this);
    }

    private boolean durationStoppingCriterion(long startTime, long timeLimit) {
        return (System.currentTimeMillis() - startTime) < timeLimit;
    }

    private void setNewPlans(CentralizedPlan plan) {
        assert this.vehicles == plan.vehicles;
        assert this.tasks == plan.tasks;
        this.setNewPlans(plan.plans);
    }

    private void setNewPlans(Map<Vehicle, VehiclePlan> plans) {
        this.plans = plans;
    }

    // Take the vehicle with the largest capacity and plan deliver the task completely at random
    private void initialize(Initialization init) {

        Vehicle largest = vehicles.get(0);
        for (Vehicle v : vehicles) {
            if (v.capacity() > largest.capacity()) {
                largest = v;
            }
        }
        VehiclePlan initialPlan;

        switch (init) {
            case ASTAR:
                initialPlan = AStar.run(largest, tasks, Heuristic.WEIGHT_NOT_TAKEN);
                break;
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
            case NONE:
            default:
                return;
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
        setNewPlans(plans);

    }


    private Vehicle getRandomVehicle() {
        return getRandomVehicle(0, Integer.MAX_VALUE);
    }

    private Vehicle getRandomVehicle(int numTasks) {
        return getRandomVehicle(numTasks, numTasks);
    }

    private Vehicle getRandomVehicle(int minNumTasks, int maxNumTasks) {
        Vehicle v = this.vehicles.get(RandomHandler.get().nextInt(this.vehicles.size()));
        int length = plans.get(v).getLength();
        while (length < minNumTasks || length > maxNumTasks) {
            v = this.vehicles.get(RandomHandler.get().nextInt(this.vehicles.size()));
            length = plans.get(v).getLength();
        }
        return v;
    }

    private Set<CentralizedPlan> chooseNeighbours() {
        Set<CentralizedPlan> neighbours = new HashSet<>();

        neighbours.addAll(passTasksAround(getRandomVehicle(0)));
        neighbours.addAll(moveTasksInTime(getRandomVehicle(0, 2)));
        neighbours.addAll(swapTasks(getRandomVehicle(0, 2)));

        return neighbours;
    }

    private CentralizedPlan localChoice(Set<CentralizedPlan> neighbors) {
        if (neighbors.isEmpty()) {
            System.out.println("NO NEIGHBORS");
            return this;
        }

        // with probability p we take the best neighbor, otherwise TAKE ONE AT RANDOM
        if (RandomHandler.get().nextDouble() < this.EXPLOITATION_RATE) {
            double minCost = Double.MAX_VALUE;
            List<CentralizedPlan> choices = new ArrayList<>();

            for (CentralizedPlan plan : neighbors) {
                double cost = computeCostOf(plan);
                if (cost < minCost) {
                    choices = new ArrayList<>();
                    choices.add(plan);
                    minCost = cost;
                } else if (cost == minCost) {
                    choices.add(plan);
                }
            }

            int idx = 0;
            if (choices.size() > 1) {
                idx = RandomHandler.get().nextInt(choices.size());
            }
            CentralizedPlan bestNeighborPlan = choices.get(idx);
            return bestNeighborPlan;
        } else {
            return new ArrayList<>(neighbors).get(RandomHandler.get().nextInt(neighbors.size()));
        }
    }

    private List<CentralizedPlan> passTasksAround(Vehicle v) {
        List<CentralizedPlan> neighboursPlan = new ArrayList<>();

        if (plans.get(v).getLength() > 0) {
            for (Vehicle other : this.vehicles) {
                if (v != other) {
                    CentralizedPlan newPlan = this.copy();
                    Task task = newPlan.plans.get(v).takeOutFirstTask();
                    if (newPlan.plans.get(other).addLoadAction(task)) {
                        newPlan.plans.get(other).addDropAction(task);
                        neighboursPlan.add(newPlan);
                    }
                }
            }
        }

        return neighboursPlan;
    }

    private List<CentralizedPlan> swapTasks(Vehicle v) {

        List<CentralizedPlan> neighboursPlan = new ArrayList<>();

        int numTasks = this.plans.get(v).getLength();
        int t1 = RandomHandler.get().nextInt(numTasks);
        int t2 = RandomHandler.get().nextInt(numTasks);
        while (t1 == t2)
            t2 = RandomHandler.get().nextInt(numTasks);
        CentralizedPlan newPlan = this.copy();
        if (newPlan.plans.get(v).swapActions(t1, t2)) {
            neighboursPlan.add(newPlan);
        }

        return neighboursPlan;
    }

    private List<CentralizedPlan> moveTasksInTime(Vehicle v) {
        List<CentralizedPlan> neighboursPlan = new ArrayList<>();

        if (plans.get(v).getLength() > 2) {
            int t = RandomHandler.get().nextInt(plans.get(v).getLength());

            boolean isValid;
            int i = t;
            do {
                CentralizedPlan newPlan = this.copy();
                isValid = newPlan.plans.get(v).advanceAction(i);
                if (isValid) {
                    neighboursPlan.add(newPlan);
                }
                i--;
            } while (isValid);
            i = t;
            do {
                CentralizedPlan newPlan = this.copy();
                isValid = newPlan.plans.get(v).postponeAction(i);
                if (isValid) {
                    neighboursPlan.add(newPlan);
                }
                i++;
            } while (isValid);


        }
        return neighboursPlan;
    }
}
