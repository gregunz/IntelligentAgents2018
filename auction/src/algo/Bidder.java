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
    private long minBidAdv = -1;
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
        double usefulness = 0;
        for (Topology.City city : topology.cities()) {
            usefulness += distribution.probability(city, task.deliveryCity);
        }
        System.out.println(bid +" --- "+minBidAdv);
        if (bid < minBidAdv) {
            lastTookFixedCost = true;
            return minBidAdv - 1;
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
            if (!lastTookFixedCost) {
                ratioMargin += ratioIncrease;
            }
        } else {
            if (!lastTookFixedCost) {
                ratioMargin -= 2*ratioIncrease;
            }
        }
        Long advBid = bids[(agent.id()+1)%2];
        if (minBidAdv == -1)
            minBidAdv = advBid;
        minBidAdv = Math.min(minBidAdv, advBid);
    }


}
