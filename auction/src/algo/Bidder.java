package algo;

import logist.agent.Agent;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;

public class Bidder {

    private final Agent agent;
    private final long bidTimeout;

    private final Planner planner;
    private final TaskImportanceEstimator taskImpEst;

    private final double learningRate = 0.1; // should be in [0, +inf] range
    private int bidCounter = 0;
    private double bidRate = 1.0; // will evolve but stays in [0, +inf] range
    private long minBidAdv = 0;
    private boolean updateBidRateForNextBid = false;

    public Bidder(Topology topology, TaskDistribution distribution, Agent agent, long bidTimeout) {
        this.agent = agent;
        this.bidTimeout = bidTimeout;
        this.planner = new Planner(agent.vehicles());
        this.taskImpEst = new TaskImportanceEstimator(agent, topology, distribution);
    }

    public Planner getPlanner() {
        return planner;
    }

    /**
     * Make a bid for the given task
     */
    public Long bid(Task task) {
        //TODO do more here, come up with brilliant ideas using distribution and topology

        double marginalCost = this.planner.estimateMarginalCost(task, bidTimeout);
        double bid = bidRate * marginalCost;

        System.out.println(bid +" --- "+minBidAdv);

        // default, we will update (either increase or decrease bidRate)
        updateBidRateForNextBid = true;

        if (bid < minBidAdv) { // if our marginal cost is negative, we end up here also
            updateBidRateForNextBid = false;
            return minBidAdv - 1;
        }

        if (isEarlyBid()) { // first 5 will have lower bids
            bid *= (bidCounter + 5.0) / 10.0; // we want first tasks, hence first is 50% of real bid, then 60, 70, 80, 90, and finally 100% for the remaining
        }

        bidCounter += 1;
        return (long) bid;
    }

    private boolean isEarlyBid() {
        return bidCounter < 5;
    }

    private void increaseBidRate() {
        if (updateBidRateForNextBid) {
            bidRate *= (1 + learningRate);
        }
    }

    private void decreaseBidRate() {
        if (updateBidRateForNextBid) {
            bidRate /= (1 + 2 * learningRate);
        }
    }

    /**
     * Improve Bidder by getting information of previous auction results
     */
    public void addInfoOfLastAuction(Task previous, int winner, Long[] bids) {
        if (agent.id() == winner) { // we took the task
            this.planner.addTask(previous);
            increaseBidRate();
        } else {
            decreaseBidRate();
        }

        // update minimum bid of adversary
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
