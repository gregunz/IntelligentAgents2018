package template;

/* import table */

import algo.AStar;
import algo.BFS;
import algo.Heuristic;
import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import models.State;
import models.StateRepresentation;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeTemplate implements DeliberativeBehavior {

    /* Environment */
    Topology topology;
    TaskDistribution td;
    /* the properties of the agent */
    Agent agent;
    /* the planning class */
    Algorithm algorithm;
    Heuristic heuristic;

    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {
        this.topology = topology;
        this.td = td;
        this.agent = agent;

        // initialize the planner
        String algorithmName = agent.readProperty("algorithm", String.class, "NAIVE").toUpperCase();

        // Throws IllegalArgumentException if algorithm is unknown
        algorithm = Algorithm.valueOf(algorithmName);
        if (algorithm == Algorithm.ASTAR) {
            String heuristicName = agent.readProperty("heuristic", String.class, "ZERO").toUpperCase();
            heuristic = Heuristic.valueOf(heuristicName);
        }

        // ...
    }

    @Override
    public Plan plan(Vehicle vehicle, TaskSet tasks) {
        Plan plan;
        State startingState = new StateRepresentation(vehicle, tasks);

        // Compute the plan with the selected algorithm.
        switch (algorithm) {
            case ASTAR:
                // ...
                System.out.println("ASTAR algorithm (with heuristic " + heuristic + ") starting...");
                plan = AStar.run(startingState, heuristic);
                break;
            case BFS:
                // ...
                System.out.println("BFS algorithm starting...");
                plan = BFS.run(startingState);
                break;
            case NAIVE:
                plan = naivePlan(vehicle, tasks);
                break;
            default:
                throw new AssertionError("Should not happen.");
        }
        System.out.println("Done!");
        System.out.println("Plan has total distance of: " + plan.totalDistance() + " km");
        return plan;
    }

    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity))
                plan.appendMove(city);

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path())
                plan.appendMove(city);

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }

    @Override
    public void planCancelled(TaskSet carriedTasks) {

        if (!carriedTasks.isEmpty()) {
            // This cannot happen for this simple agent, but typically
            // you will need to consider the carriedTasks when the next
            // plan is computed.
        }
    }

    enum Algorithm {BFS, ASTAR, NAIVE}
}
