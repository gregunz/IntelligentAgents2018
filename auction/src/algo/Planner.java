package algo;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import models.CentralizedPlan;
import models.Initialization;
import random.RandomHandler;

import java.util.List;
import java.util.Optional;

public class Planner {

    private static final int EXPLOITATION_DEEPNESS = 100 * 1000;

    private static final double EXPLOITATION_RATE_FROM = 0.1;
    private static final double EXPLOITATION_RATE_TO = 1.0;

    private CentralizedPlan bestPlan;
    private Optional<CentralizedPlan> planIfBetIsWon;

    private boolean didAStarInit = false;

    public Planner(List<Vehicle> vehicles) {
        this.bestPlan = new CentralizedPlan(vehicles);
        this.planIfBetIsWon = Optional.empty();
    }

    public long estimateMarginalCost(Task task, long timeLimit) {

        double oldCost = this.bestPlan.getCost();

        CentralizedPlan nextPlan = this.createPlanWithNewTask(task, timeLimit);
        planIfBetIsWon = Optional.of(nextPlan);

        //System.out.println("old cost = " + oldCost + " new cost = " + nextPlan.getCost());
        return (long) (nextPlan.getCost() - oldCost); // marginal actualCost
    }

    private CentralizedPlan createPlanWithNewTask(Task task, long timeLimit) {
        long startTime = System.currentTimeMillis();
        CentralizedPlan nextPlan = this.bestPlan.copy();
        nextPlan.addTask(task, Initialization.ASTAR /*TODO might change this*/);
        nextPlan = this.findBestPlan(nextPlan, timeLimit - (System.currentTimeMillis() - startTime));
        return nextPlan;
    }

    public void addTask(Task task) {
        if (planIfBetIsWon.isPresent()) {
            System.out.println("adding the task we had a bet on");
            bestPlan = planIfBetIsWon.get();
            planIfBetIsWon = Optional.empty();
        } else {
            bestPlan = createPlanWithNewTask(task, 0);
        }
    }

    public List<Plan> toLogistPlans(TaskSet tasks) {
        return this.bestPlan.toLogistPlans(tasks);
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
        //this.didAStarInit = false; // force doing one astar init

        CentralizedPlan bestPlan = plan.copy();
        double bestCost = bestPlan.getCost();
        System.out.println(bestCost);

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeLimit) { // loop on every local minima

            int iterWithoutImprove = 0;
            double bestLocalCost = plan.getCost();

            double exploitationRate = EXPLOITATION_RATE_FROM + RandomHandler.get().nextDouble() * (EXPLOITATION_RATE_TO - EXPLOITATION_RATE_FROM);
            while (iterWithoutImprove < EXPLOITATION_DEEPNESS && System.currentTimeMillis() - startTime < timeLimit) { // loop on improving one local plan
                plan = plan.nextPlan(exploitationRate); // this does NOT mutate the plan
                double cost = plan.getCost();
                if (cost < bestLocalCost) {

                    iterWithoutImprove = 0;
                    bestLocalCost = cost;

                    if (cost < bestCost) {
                        bestPlan = plan.copy();
                        bestCost = cost;
                        System.out.println(bestCost);
                    }
                } else { // not improving
                    iterWithoutImprove += 1;
                }
            }
            plan.initialize(Initialization.RANDOM);//nextInit()); // this mutates the plan
        }

        return bestPlan;
    }


    private Initialization nextInit() {
        if (!didAStarInit) {
            didAStarInit = true;
            return Initialization.ASTAR;
        }
        return Initialization.RANDOM;
    }
}
