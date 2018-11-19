package algo;

import logist.agent.Agent;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;

public class Bidder {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private int bidCounter;
    private long bidTimeout;
    private double ratioMargin = 0.5;
    private double ratioIncrease = 0.1;
    private long minBidAdv = -1;
    private boolean lastTookFixedCost = false;

    private Planner planner;

    public Bidder(Topology topology, TaskDistribution distribution, Agent agent, long bidTimeout) {
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
        this.bidTimeout = bidTimeout;

        this.planner = new Planner(agent.vehicles());
    }

    public Planner getPlanner() {
        return planner;
    }

    /**
     * Make a bid for the given task
     */
    public Long bid(Task task) {

        //TODO do more here, come up with brilliant ideas using distribution and topology

        double marginalCost = Math.max(0, this.planner.estimateMarginalCost(task, bidTimeout)); // lower bound marginal cost by 0
        double bid = (1 + ratioMargin) * marginalCost;

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

        if (isEarlyBid()) { // first 5 will have lower bids
            bid *= (bidCounter + 5.0) / 10.0; // we want first tasks, hence first is 50% of real bid, then 60, 70, 80, 90, and finally 100% for the remaining
        }

        bidCounter += 1;
        return (long) bid;
    }

    private boolean isEarlyBid() {
        return bidCounter < 5;
    }

    /**
     * Improve Bidder by getting information of previous auction results
     */
    public void addInfoOfLastAuction(Task previous, int winner, Long[] bids) {
        if (agent.id() == winner) { // we took the task
            this.planner.addTask(previous);
            if (!isEarlyBid() && !lastTookFixedCost) {
                ratioMargin *= (1 + ratioIncrease);
            }
        } else {
            if (!isEarlyBid() && !lastTookFixedCost) {
                ratioMargin /= (1 + 2 * ratioIncrease);
            }
        }
        if (bids.length > 1) {
            int advBidIndex = (agent.id() + 1) % 2;
            Long advBid = bids[advBidIndex];
            if (minBidAdv == -1) {
                minBidAdv = advBid;
            } else {
                minBidAdv = Math.min(minBidAdv, advBid);
            }
        }
    }


}
