package models;

import logist.simulation.Vehicle;
import logist.task.Task;
import random.RandomHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SLS {
    // these are the hyper parameters, they could be not static and different version of SLS could be tested at the same time
    private static final int NUM_NEIGHBORS = 10;
    private static final int EXPLOITATION_DEEPNESS = 100 * 1000 / NUM_NEIGHBORS;
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
        List<CentralizedPlan> neighbors = chooseNeighbours(plan);
        return localChoice(plan, neighbors, exploitationRate);
    }

    private static List<CentralizedPlan> chooseNeighbours(CentralizedPlan plan) {
        Map<Vehicle, VehiclePlan> plans = plan.getPlans();

        List<CentralizedPlan> neighbours = new ArrayList<>();
        int maxNumTask = 0;
        for (VehiclePlan p : plans.values()) {
            if (p.getLength() > maxNumTask) {
                maxNumTask = p.getLength();
            }
        }

        for (int i = 0; i < NUM_NEIGHBORS; i++) {
            if (maxNumTask >= 1) {
                neighbours.addAll(passTasksAround(plan, getRandomVehicle(plan, 1)));
            }
            if (maxNumTask >= 3) {
                neighbours.addAll(moveTasksInTime(plan, getRandomVehicle(plan, 3)));
                neighbours.addAll(swapTasks(plan, getRandomVehicle(plan, 3)));
            }
        }

        return neighbours;
    }

    private static CentralizedPlan localChoice(CentralizedPlan plan, List<CentralizedPlan> neighbors, double exploitationRate) {
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

        if (plan.getPlans().get(v).getLength() > 0) {
            for (Vehicle other : plan.getVehicles()) {
                if (v != other) {
                    VehiclePlan vPlan = plan.getPlans().get(v).copy();
                    Task task = vPlan.takeOutFirstTask();
                    VehiclePlan otherPlan = plan.getPlans().get(other).copy();

                    if (otherPlan.addLoadAction(task)) {
                        otherPlan.addDropAction(task);
                        CentralizedPlan newPlan = plan
                                .modifyVehiclePlan(v, vPlan)
                                .modifyVehiclePlan(other, otherPlan);
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

        VehiclePlan vPlan = plan.getPlans().get(v).copy();

        if (vPlan.swapActions(t1, t2)) {
            CentralizedPlan newPlan = plan.modifyVehiclePlan(v, vPlan);
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
                VehiclePlan vPlan = plan.getPlans().get(v).copy();
                isValid = vPlan.advanceAction(i);
                if (isValid) {
                    CentralizedPlan newPlan = plan.modifyVehiclePlan(v, vPlan);
                    neighboursPlan.add(newPlan);
                }
                i--;
            } while (isValid);
            i = t;
            do {
                VehiclePlan vPlan = plan.getPlans().get(v).copy();
                isValid = vPlan.postponeAction(i);
                if (isValid) {
                    CentralizedPlan newPlan = plan.modifyVehiclePlan(v, vPlan);
                    neighboursPlan.add(newPlan);
                }
                i++;
            } while (isValid);


        }
        return neighboursPlan;
    }
}
