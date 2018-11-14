package algo;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import models.CentralizedPlan;

import java.util.List;

public class Planner {

    private CentralizedPlan actualPlan;
    private CentralizedPlan bestPlan;

    public Planner(List<Vehicle> vehicles) {
        this.actualPlan = new CentralizedPlan(vehicles);
        this.setBestPlan();
    }


    /**
     * add a task and return marginal cost
     */
    public double addTask(Task task, long timeLimit) {
        long startTime = System.currentTimeMillis();

        double oldCost = this.bestPlan.getCost();

        this.actualPlan.addTask(task);
        this.setBestPlan();

        this.findBestPlan(timeLimit);

        return this.bestPlan.getCost() - oldCost; // marginal actualCost
    }

    public List<Plan> toLogistPlans() {
        return this.bestPlan.toLogistPlans();
    }

    /**
     * Return the best plan for all vehicles within a time limit
     */
    public void findBestPlan(long timeLimit) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeLimit) {
            this.actualPlan = this.actualPlan.tryToImprovePlan();
            if (this.actualPlan.getCost() < this.bestPlan.getCost()) {
                this.setBestPlan();
            }
        }
    }

    private void setBestPlan() {
        this.bestPlan = this.actualPlan.copy();
    }
}
