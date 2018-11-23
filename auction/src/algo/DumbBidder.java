package algo;

import logist.agent.Agent;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import print.PrintHandler;
import random.RandomHandler;

import java.util.ArrayList;
import java.util.List;

public class DumbBidder extends Bidder {
    private final Agent agent;

    private final Planner ourPlanner;
    private final Planner advPlanner;
    private final Topology topology;
    private final TaskDistribution distribution;
    private Double avgKm;


    public DumbBidder(Agent agent, Topology topology, TaskDistribution distribution, Double avgKm) {
        super();
        this.agent = agent;
        this.ourPlanner = new Planner(agent.vehicles());
        this.advPlanner = new Planner(agent.vehicles());
        this.topology = topology;
        this.distribution = distribution;
        this.avgKm = avgKm;
    }

    public Planner getOurPlanner() {
        return ourPlanner;
    }


    /**
     * Make a bid for the given task
     */
    public Long bid(Task task) {
        return ((Long) Math.round(Math.min(avgKm, task.pickupCity.distanceTo(task.deliveryCity)* agent.vehicles().get(0).costPerKm())));
    }


    /**
     * Improve Bidder by getting information of previous auction results
     */
    public void addInfoOfLastAuction(Task previous, int winner, Long[] bids) {
        if (agent.id() == winner) { // we took the task
            ourPlanner.addTask(previous);
        }
    }
}
