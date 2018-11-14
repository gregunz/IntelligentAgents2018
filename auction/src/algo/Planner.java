package algo;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import models.CentralizedPlan;
import models.Initialization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Planner {

    private CentralizedPlan bestPlan;
    private Map<Task, CentralizedPlan> betToPlan;

    private boolean didAStarInit = false;

    public Planner(List<Vehicle> vehicles) {
        this.bestPlan = new CentralizedPlan(vehicles);
        this.betToPlan = new HashMap<>();
    }

    public long computeMarginalCost(Task task, long timeLimit) {
        long startTime = System.currentTimeMillis();

        double oldCost = this.bestPlan.getCost();

        CentralizedPlan nextPlan = this.bestPlan.copy();
        nextPlan.addTask(task, Initialization.RANDOM /*TODO might change this*/);
        nextPlan = this.findBestPlan(nextPlan, timeLimit - (System.currentTimeMillis() - startTime));

        betToPlan.put(task, nextPlan);

        //System.out.println("old cost = " + oldCost + " new cost = " + nextPlan.getCost());
        return (long) (nextPlan.getCost() - oldCost); // marginal actualCost
    }

    /**
     * add a task
     */
    public void addTask(Task task) {
        bestPlan = betToPlan.get(task);
    }

    public List<Plan> toLogistPlans() {
        return this.bestPlan.toLogistPlans();
    }

    /**
     * Return the best plan for all vehicles within a time limit
     */
    public CentralizedPlan findBestPlan(long timeLimit) {
        CentralizedPlan bestLocalPlan = findBestPlan(this.bestPlan.copy(), timeLimit);
        if (bestLocalPlan.getCost() < this.bestPlan.getCost()) {
            this.bestPlan = bestLocalPlan.copy();
        }
        return this.bestPlan;
    }

    private CentralizedPlan findBestPlan(CentralizedPlan plan, long timeLimit) {
        this.didAStarInit = false; // force doing one astar init

        CentralizedPlan bestLocalPlan = plan.copy();

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeLimit) {
            long timeLimitRemaining = timeLimit - (System.currentTimeMillis() - startTime);
            plan = plan.nextPlan(timeLimitRemaining, nextInit());
            if (plan.getCost() < bestLocalPlan.getCost()) {
                bestLocalPlan = plan.copy();
            }
        }

        return bestLocalPlan;
    }


    private Initialization nextInit() {
        if (!didAStarInit) {
            didAStarInit = true;
            return Initialization.ASTAR;
        }
        return Initialization.RANDOM;
    }
}
