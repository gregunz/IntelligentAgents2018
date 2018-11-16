package models;

import logist.simulation.Vehicle;
import logist.task.Task;
import random.RandomHandler;

import java.util.*;

public class SLS {
    private static final int EXPLOITATION_DEEPNESS = 100 * 1000;
    private static final double EXPLOITATION_RATE_FROM = 0.0;
    private static final double EXPLOITATION_RATE_TO = 1.0;

    private SLS() {
    }

    public static CentralizedPlan optimize(CentralizedPlan plan, long timeLimit) { // SLS ALGO WHERE CHOOSE_NEIGHBORS AND LOCAL CHOICE ARE INSIDE "nextPlan(rate)"
        CentralizedPlan bestPlan = plan;
        double bestCost = bestPlan.getCost();
        System.out.println(bestCost);

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeLimit) { // loop on every local minima

            int iterWithoutImprove = 0;
            double bestLocalCost = plan.getCost();

            double exploitationRate = EXPLOITATION_RATE_FROM + RandomHandler.get().nextDouble() * (EXPLOITATION_RATE_TO - EXPLOITATION_RATE_FROM);
            System.out.println("EXPLOITATION_RATE = " + exploitationRate);
            while (iterWithoutImprove < EXPLOITATION_DEEPNESS && System.currentTimeMillis() - startTime < timeLimit) { // loop on improving one local plan
                plan = nextPlan(plan, exploitationRate);
                double cost = plan.getCost();
                if (cost < bestLocalCost) {

                    iterWithoutImprove = 0;
                    bestLocalCost = cost;

                    if (cost < bestCost) {
                        bestPlan = plan;
                        bestCost = cost;
                        System.out.println(bestCost);
                    }
                } else { // not improving
                    iterWithoutImprove += 1;
                }
            }
            plan = PlanGenerator.generate(plan.getVehicles(), plan.getTasks(), InitStrategy.RANDOM);
        }

        return bestPlan;
    }

    private static CentralizedPlan nextPlan(CentralizedPlan plan, double exploitationRate) {
        Set<CentralizedPlan> neighbors = chooseNeighbours(plan);
        return localChoice(plan, neighbors, exploitationRate);
    }

    private static Set<CentralizedPlan> chooseNeighbours(CentralizedPlan plan) {
        Map<Vehicle, VehiclePlan> plans = plan.getPlans();

        Set<CentralizedPlan> neighbours = new HashSet<>();
        int maxNumTask = 0;
        for (VehiclePlan p : plans.values()) {
            if (p.getLength() > maxNumTask) {
                maxNumTask = p.getLength();
            }
        }

        if (maxNumTask >= 1) {
            neighbours.addAll(passTasksAround(plan, getRandomVehicle(plan, 1)));
        }
        if (maxNumTask >= 3) {
            neighbours.addAll(moveTasksInTime(plan, getRandomVehicle(plan, 3)));
            neighbours.addAll(swapTasks(plan, getRandomVehicle(plan, 3)));
        }

        return neighbours;
    }

    private static CentralizedPlan localChoice(CentralizedPlan plan, Set<CentralizedPlan> neighbors, double exploitationRate) {
        if (neighbors.isEmpty()) {
            System.out.println("NO NEIGHBORS");
            return plan;
        }

        // with probability p we take the best neighbor, otherwise TAKE ONE AT RANDOM
        double minCost = Double.MAX_VALUE;
        List<CentralizedPlan> choices = new ArrayList<>();

        for (CentralizedPlan p : neighbors) {
            double cost = p.getCost();
            if (cost < minCost) {
                choices = new ArrayList<>();
                choices.add(p);
                minCost = cost;
            } else if (cost == minCost) {
                choices.add(p);
            }
        }

        int idx = 0;
        if (choices.size() > 1) {
            idx = RandomHandler.get().nextInt(choices.size());
        }
        CentralizedPlan bestNeighborPlan = choices.get(idx);
        if (minCost < plan.getCost()) {
            return bestNeighborPlan;
        } else if (RandomHandler.get().nextDouble() < exploitationRate) {
            return plan;
        } else {
            return bestNeighborPlan;
        }
    }

    private static Vehicle getRandomVehicle(CentralizedPlan plan, int minNumTasks) {
        return getRandomVehicle(plan, minNumTasks, Integer.MAX_VALUE);
    }

    private static Vehicle getRandomVehicle(CentralizedPlan plan, int minNumTasks, int maxNumTasks) {
        Map<Vehicle, VehiclePlan> plans = plan.getPlans();
        List<Vehicle> vehicles = plan.getVehicles();

        Vehicle v = vehicles.get(RandomHandler.get().nextInt(vehicles.size()));
        int length = plans.get(v).getLength();
        while (length < minNumTasks || length > maxNumTasks) {
            v = vehicles.get(RandomHandler.get().nextInt(vehicles.size()));
            length = plans.get(v).getLength();
        }
        return v;
    }

    private static List<CentralizedPlan> passTasksAround(CentralizedPlan plan, Vehicle v) {
        List<CentralizedPlan> neighboursPlan = new ArrayList<>();
        Map<Vehicle, VehiclePlan> plans = plan.getPlans();
        List<Vehicle> vehicles = plan.getVehicles();

        if (plans.get(v).getLength() > 0) {
            for (Vehicle other : vehicles) {
                if (v != other) {
                    CentralizedPlan newPlan = plan.copy();
                    Map<Vehicle, VehiclePlan> newPlans = newPlan.getPlans();
                    Task task = newPlans.get(v).takeOutFirstTask();
                    if (newPlans.get(other).addLoadAction(task)) {
                        newPlans.get(other).addDropAction(task);
                        neighboursPlan.add(newPlan);
                    }
                }
            }
        }

        return neighboursPlan;
    }

    private static List<CentralizedPlan> swapTasks(CentralizedPlan plan, Vehicle v) {
        List<CentralizedPlan> neighboursPlan = new ArrayList<>();
        Map<Vehicle, VehiclePlan> plans = plan.getPlans();

        int numTasks = plans.get(v).getLength();
        int t1 = RandomHandler.get().nextInt(numTasks);
        int t2 = RandomHandler.get().nextInt(numTasks);
        while (t1 == t2)
            t2 = RandomHandler.get().nextInt(numTasks);
        CentralizedPlan newPlan = plan.copy();
        Map<Vehicle, VehiclePlan> newPlans = newPlan.getPlans();
        if (newPlans.get(v).swapActions(t1, t2)) {
            neighboursPlan.add(newPlan);
        }
        return neighboursPlan;
    }

    private static List<CentralizedPlan> moveTasksInTime(CentralizedPlan plan, Vehicle v) {
        List<CentralizedPlan> neighboursPlan = new ArrayList<>();
        Map<Vehicle, VehiclePlan> plans = plan.getPlans();

        if (plans.get(v).getLength() > 2) {
            int t = RandomHandler.get().nextInt(plans.get(v).getLength());

            boolean isValid;
            int i = t;
            do {
                CentralizedPlan newPlan = plan.copy();
                Map<Vehicle, VehiclePlan> newPlans = newPlan.getPlans();
                isValid = newPlans.get(v).advanceAction(i);
                if (isValid) {
                    neighboursPlan.add(newPlan);
                }
                i--;
            } while (isValid);
            i = t;
            do {
                CentralizedPlan newPlan = plan.copy();
                Map<Vehicle, VehiclePlan> newPlans = newPlan.getPlans();
                isValid = newPlans.get(v).postponeAction(i);
                if (isValid) {
                    neighboursPlan.add(newPlan);
                }
                i++;
            } while (isValid);


        }
        return neighboursPlan;
    }
}
