package algo;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import models.AddStrategy;
import models.CentralizedPlan;
import models.PlanGenerator;
import models.SLS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


public class Planner {

    private CentralizedPlan bestPlan;
    private Optional<CentralizedPlan> planIfBetIsWon;

    public Planner(List<Vehicle> vehicles) {
        this.bestPlan = new CentralizedPlan(vehicles, new HashMap<>(), new ArrayList<>());
        this.planIfBetIsWon = Optional.empty();
    }

    public double estimateMarginalCost(Task task, long timeLimit) {
        long startTime = System.currentTimeMillis();

        double oldCost = this.bestPlan.getCost();

        CentralizedPlan nextPlan = PlanGenerator.addTask(bestPlan, task, AddStrategy.ASTAR_INIT);
        long timeUsedAlready = (System.currentTimeMillis() - startTime);

        nextPlan = SLS.optimize(nextPlan, timeLimit - timeUsedAlready);
        planIfBetIsWon = Optional.of(nextPlan);

        return (nextPlan.getCost() - oldCost); // marginal actualCost
    }

    public void addTask(Task task) {
        if (planIfBetIsWon.isPresent()) {
            System.out.println("adding the task we had a bet on");
            bestPlan = planIfBetIsWon.get();
            planIfBetIsWon = Optional.empty();
        } else {
            bestPlan = PlanGenerator.addTask(this.bestPlan, task, AddStrategy.ASTAR_INIT);
        }
    }

    public List<Plan> toLogistPlans(TaskSet tasks) {
        return this.bestPlan.toLogistPlans(tasks);
    }

    /**
     * Return the best plan for all vehicles within a time limit
     */
    public CentralizedPlan findBestPlan(long timeLimit) {
        CentralizedPlan bestLocalPlan = SLS.optimize(this.bestPlan, timeLimit);
        if (bestLocalPlan.getCost() < this.bestPlan.getCost()) {
            this.bestPlan = bestLocalPlan;
        }
        return this.bestPlan;
    }

}
