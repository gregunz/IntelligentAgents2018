package algo;

import logist.Measures;
import logist.agent.Agent;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;

public class Bidder {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long bidTimout;
    private double ratioMargin = 0.5;
    private double ratioIncrease = 0.25;
    private long fixedCost = 1000;
    private boolean lastTookFixedCost = false;

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
        long bid = Math.round((1 + ratioMargin) * marginalCost);
        //TODO do more here, come up with brilliant ideas using distribution and topology
        if (bid < fixedCost) {
            lastTookFixedCost = true;
            return fixedCost;
        }
        lastTookFixedCost = false;
        return bid;
    }

    /**
     * Improve Bidder by getting information of previous auction results
     */
    public void addInfoOfLastAuction(Task previous, int winner, Long[] bids) {
        if (agent.id() == winner) { // we took the task
            this.planner.addTask(previous);
            if (lastTookFixedCost) {
                fixedCost += fixedCost/2;
            } else {
                ratioMargin += ratioIncrease;
            }
        } else {
            if (lastTookFixedCost) {
                fixedCost /= 2;
            } else {
                ratioMargin -= 2*ratioIncrease;
            }
        }
        //TODO we might improve bidder here by learning the other agents ?
    }


}
