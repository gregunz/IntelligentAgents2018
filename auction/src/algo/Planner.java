package algo;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskSet;

import java.util.List;

public class Planner {
    public Planner(List<Vehicle> vehicles, TaskSet tasks) {
        // initializer the planner
    }

    public List<Plan> findBestPlan(long timeLimit) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeLimit) {
            improvePlan();
        }
        return null; //TODO: update this
    }

    private void improvePlan() {
        // do iteration over plan
    }

}
