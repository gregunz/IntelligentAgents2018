package algo;

import logist.agent.Agent;
import logist.task.Task;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

public class Bidder {

    private final Agent agent;
    private final long bidTimeout;

    private final Planner planner;
    private final boolean useImportance;
    private final TaskImportanceEstimator taskImpEst;

    private int bidCounter = 0;

    private int numOfAdvLatestBids = 5;
    private Queue<Long> advLatestBids = new LinkedList<>();

    private double learningRate = 0.1; // should be in [0, +inf] range
    private double bidRate = 1.0; // will evolve but stays in [0, +inf] range

    private boolean updateBidRateForNextBid = false;

    public Bidder(Agent agent, long bidTimeout, TaskImportanceEstimator taskImpEst, boolean useImportance) {
        this.agent = agent;
        this.planner = new Planner(agent.vehicles());
        this.taskImpEst = taskImpEst;
        this.useImportance = useImportance;
        this.bidTimeout = bidTimeout;
    }

    public Planner getPlanner() {
        return planner;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public void setBidRate(double bidRate) {
        this.bidRate = bidRate;
    }

    public void setNumOfAdvLatestBids(int numOfAdvLatestBids) {
        this.numOfAdvLatestBids = numOfAdvLatestBids;
    }

    /**
     * Make a bid for the given task
     */
    public Long bid(Task task) {
        double marginalCost = this.planner.estimateMarginalCost(task, bidTimeout);
        double bid = bidRate * marginalCost;
        if (useImportance) {
            bid *= taskImpEst.get(task);
        }

        System.out.println(bid + " --- " + minOfAdvLatestBids());

        // default, we will update (either increase or decrease bidRate)
        updateBidRateForNextBid = true;

        if (bid < minOfAdvLatestBids()) { // we never bid too low, if our marginal cost is negative, we end up here also
            updateBidRateForNextBid = false;
            return Math.max(1, minOfAdvLatestBids());
        }

        if (isEarlyBid()) { // first 5 will have lower bids
            bid *= (bidCounter + 5.0) / 10.0; // we want first tasks, hence first is 50% of real bid, then 60, 70, 80, 90, and finally 100% for the remaining
        }

        bidCounter += 1;
        return (long) Math.max(1, bid);
    }

    private boolean isEarlyBid() {
        return bidCounter < 5;
    }

    private long minOfAdvLatestBids() {
        if (advLatestBids.isEmpty()) {
            return 0;
        }
        return advLatestBids.stream().min(Comparator.naturalOrder()).get();
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
            if (advLatestBids.size() >= numOfAdvLatestBids) {
                advLatestBids.poll();
            }
            advLatestBids.offer(advBid);
        }
    }


}
