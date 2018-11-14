package algo;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskSet;

import java.util.List;

public class Planner {

    List<Vehicle> vehicles;
    TaskSet tasks;

    public Planner(List<Vehicle> vehicles, TaskSet tasks) {
        this.vehicles = vehicles;
        this.tasks = tasks;
    }

    /**
     * Return the best plan for all vehicles within a time limit
     */
    public List<Plan> findBestPlan(long timeLimit) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeLimit) {
            this.improvePlan();
        }
        return null; //TODO: update this //it must be quick because we must not timeout!
    }

    private void improvePlan() {
        //TODO iteration improvement of plan
    }

}
