package models;

import logist.simulation.Vehicle;
import logist.task.Task;
import print.PrintHandler;
import random.RandomHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SLS {
    // these are the hyper parameters, they could be not static and different version of SLS could be tested at the same time
    private static final int NUM_NEIGHBORS = 1;
    private static final int EXPLOITATION_DEEPNESS = 200 * 1000 / NUM_NEIGHBORS;
    private static final double EXPLOITATION_RATE_FROM = 0.0;
    private static final double EXPLOITATION_RATE_TO = 1.0;
    private static final double RATE_OF_ASTAR_INIT = 1.0;

    private SLS() {
    }

    public static CentralizedPlan optimize(CentralizedPlan plan, long timeLimit) { // SLS ALGO WHERE CHOOSE_NEIGHBORS AND LOCAL CHOICE ARE INSIDE "nextPlan(rate)"
        long startTime = System.currentTimeMillis();

        CentralizedPlan bestPlan = plan;
        double bestCost = bestPlan.getCost();
        double latestBestLocalCost = 0;
        double rateOfAstarInit = RATE_OF_ASTAR_INIT;

        PrintHandler.println("starting optimization with: " + bestCost, 2);

        while (System.currentTimeMillis() - startTime < timeLimit) { // loop on every local minima

            int iterWithoutImprove = 0;
            double bestLocalCost = plan.getCost();
            double exploitationRate = EXPLOITATION_RATE_FROM + RandomHandler.get().nextDouble() * (EXPLOITATION_RATE_TO - EXPLOITATION_RATE_FROM);

            PrintHandler.println("EXPLOITATION_RATE = " + exploitationRate, 3);

            while (iterWithoutImprove < EXPLOITATION_DEEPNESS && System.currentTimeMillis() - startTime < timeLimit) { // loop on improving one local plan
                plan = nextPlan(plan, exploitationRate);
                double cost = plan.getCost();
                if (cost < bestLocalCost) {

                    iterWithoutImprove = 0;
                    bestLocalCost = cost;

                    if (cost < bestCost) {
                        bestPlan = plan;
                        bestCost = cost;
                        PrintHandler.println("best plan improved: " + bestCost, 4);
                    }
                } else { // not improving
                    iterWithoutImprove += 1;
                }
            }
            PrintHandler.println("best local improvement: " + bestLocalCost, 3);
            if (Math.abs(bestLocalCost - latestBestLocalCost) < 0.01) { // to avoid too much astar which are sometimes easily stuck
                rateOfAstarInit -= 0.2;
            }
            latestBestLocalCost = bestLocalCost;
            InitStrategy nextInit = RandomHandler.get().nextDouble() < rateOfAstarInit ? InitStrategy.ASTAR : InitStrategy.RANDOM;
            if (nextInit == InitStrategy.ASTAR) {
                rateOfAstarInit -= 0.1;
            } else {
                rateOfAstarInit += 0.1;
            }
            plan = PlanGenerator.generate(plan.getVehicles(), plan.getTasks(), nextInit);
        }

        PrintHandler.println("ending optimization with: " + bestCost, 2);
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
                int numActions = p.getLength();
                if (numActions % 2 != 0) {
                    PrintHandler.println("[FAIL] Num of actions should be pair (tasks * 2)");
                }
                maxNumTask = numActions / 2;
            }
        }
        for (int i = 0; i < NUM_NEIGHBORS; i++) {
            if (maxNumTask >= 1) {
                neighbours.addAll(passTasksAround(plan, getRandomVehicle(plan, 1)));
            }
            if (maxNumTask >= 2) {
                neighbours.addAll(moveTasksInTime(plan, getRandomVehicle(plan, 2)));
                neighbours.addAll(swapTasks(plan, getRandomVehicle(plan, 2)));
            }
        }

        return neighbours;
    }

    private static CentralizedPlan localChoice(CentralizedPlan plan, List<CentralizedPlan> neighbors, double exploitationRate) {
        if (neighbors.isEmpty()) {
            PrintHandler.println("NO NEIGHBORS", 4);
            return plan;
        }

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
        int numTasks = plans.get(v).getLength() / 2;
        while (numTasks < minNumTasks || numTasks > maxNumTasks) {
            v = vehicles.get(RandomHandler.get().nextInt(vehicles.size()));
            numTasks = plans.get(v).getLength() / 2;
        }
        return v;
    }

    private static List<CentralizedPlan> passTasksAround(CentralizedPlan plan, Vehicle v) {
        List<CentralizedPlan> neighboursPlan = new ArrayList<>();

        if (plan.getPlans().get(v).getLength() > 0) {
            for (Vehicle other : plan.getVehicles()) {
                if (v != other) {
                    VehiclePlan vPlan = plan.getPlans().get(v).copy();
                    Task task = vPlan.takeOutOneTask();
                    VehiclePlan otherPlan = plan.getPlans().get(other).copy();
                    otherPlan.addTaskRandomly(task);
                    CentralizedPlan newPlan = plan
                            .modifyVehiclePlan(v, vPlan)
                            .modifyVehiclePlan(other, otherPlan);
                    neighboursPlan.add(newPlan);
                }
            }
        }

        return neighboursPlan;
    }

    private static List<CentralizedPlan> swapTasks(CentralizedPlan plan, Vehicle v) {
        List<CentralizedPlan> neighboursPlan = new ArrayList<>();
        Map<Vehicle, VehiclePlan> plans = plan.getPlans();
        VehiclePlan vPlan = plan.getPlans().get(v).copy();
        vPlan.swapTwoTasks();
        CentralizedPlan newPlan = plan.modifyVehiclePlan(v, vPlan);
        neighboursPlan.add(newPlan);
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
