package algo;

import logist.agent.Agent;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;

public class Bidder {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long bidTimout;

    private Planner planner;

    public Bidder(Topology topology, TaskDistribution distribution, Agent agent, long bidTimeout) {
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
        this.bidTimout = bidTimeout;

        this.planner = new Planner(agent.vehicles());
    }

    public Planner getPlanner() {
        return planner;
    }

    /**
     * Make a bid for the given task
     */
    public Long bid(Task task) {
        long marginalCost = this.planner.estimateMarginalCost(task, bidTimout);
        long expectedProfit = 100;
        //TODO do more here, come up with brilliant ideas
        return marginalCost + expectedProfit;
    }

    /**
     * Improve Bidder by getting information of previous auction results
     */
    public void addInfoOfLastAuction(Task previous, int winner, Long[] bids) {
        if (agent.id() == winner) { // we took the task
            this.planner.betIsWon(previous);
        }
        //TODO
    }


}
