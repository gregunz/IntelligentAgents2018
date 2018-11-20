package algo;

import logist.agent.Agent;
import logist.task.Task;
import print.PrintHandler;

import java.util.*;

public class Bidder {

    private final Agent agent;
    private final long bidTimeout;

    private final Planner ourPlanner;
    private final Planner advPlanner;
    private final boolean useImportanceStrategy;
    private final TaskImportanceEstimator taskImpEst;

    private final boolean useEarlyBidStrategy;
    private final boolean useMinOfAdvBidsStrategy;

    private final List<Double> marginalCostsDif;
    private final boolean useMarginalCostsDifStrategy;

    private int bidsWonCounter = 0;
    private int bidsLostCounter = 0;

    private int numOfAdvLatestBids = 5;
    private Queue<Long> advLatestBids = new LinkedList<>();

    private double learningRate = 0.1; // should be in [0, +inf] range
    private double bidRate = 1.0; // will evolve but stays in [0, +inf] range

    private boolean updateBidRateForNextBid = false;

    public Bidder(Agent agent, long bidTimeout, TaskImportanceEstimator taskImpEst,
                  boolean useImportance, boolean useEarlyBid, boolean useMinOfAdvBids, boolean useMarginalCostsDif) {
        this.agent = agent;
        this.ourPlanner = new Planner(agent.vehicles());
        this.advPlanner = new Planner(agent.vehicles());
        this.marginalCostsDif = new ArrayList<>();
        this.marginalCostsDif.add(100d); // in order to have values for max and min when computing dif
        this.marginalCostsDif.add(-100d);
        this.taskImpEst = taskImpEst;
        this.useImportanceStrategy = useImportance;
        this.bidTimeout = bidTimeout;
        this.useEarlyBidStrategy = useEarlyBid;
        this.useMinOfAdvBidsStrategy = useMinOfAdvBids;
        this.useMarginalCostsDifStrategy = useMarginalCostsDif;

        PrintHandler.println("Bidder( useImportance=" + useImportance + ", useEarlyBid=" + useEarlyBid +
                ", useMinOfAdvBids=" + useMinOfAdvBids + ", useMarginalCostsDif=" + useMarginalCostsDif + " ) setup", 0);
    }

    public Planner getOurPlanner() {
        return ourPlanner;
    }

    public void setLearningRate(double learningRate) {
        PrintHandler.println("SET learningRate = " + learningRate, 1);
        this.learningRate = learningRate;
    }

    public void setBidRate(double bidRate) {
        PrintHandler.println("SET bidRate = " + bidRate, 1);
        this.bidRate = bidRate;
    }

    public void setNumOfAdvLatestBids(int numOfAdvLatestBids) {
        PrintHandler.println("SET numOfAdvLatestBids = " + numOfAdvLatestBids, 1);
        this.numOfAdvLatestBids = numOfAdvLatestBids;
    }

    /**
     * Make a bid for the given task
     */
    public Long bid(Task task) {
        // default, we will update (either increase or decrease bidRate)
        updateBidRateForNextBid = true;

        double marginalCost = this.ourPlanner.estimateMarginalCost(task, bidTimeout);
        double bid = bidRate * marginalCost;
        PrintHandler.println("bid = bidRate * marginalCost = " + bidRate + " * " + marginalCost + " = " + bid, 1);

        if (useImportanceStrategy) {
            double importance = taskImpEst.get(task);
            double newBid = bid / (1 + 2 * learningRate * importance);
            PrintHandler.println("bid = bid / (1 + 2 * learningRate * importance) = "
                    + bid + " / ( 1 + " + 2 * learningRate + " * " + importance + ") = " + newBid, 1);
            bid = newBid;
        }

        if (useMarginalCostsDifStrategy) {
            double advMarginalCost = this.advPlanner.estimateMarginalCost(task, 500);
            double marginalDifNorm = marginCostDifNormalized(marginalCost, advMarginalCost); // this is between -1 and +1
            double newBid = bid * (1 + 2 * learningRate * marginalDifNorm);
            PrintHandler.println("bid = bid * (1 + 2 * learningRate * marginalDifNorm) = "
                    + bid + " * ( 1 + " + 2 * learningRate + " * " + marginalDifNorm + ") = " + newBid, 1);
            bid = newBid;
        }

        if (useMinOfAdvBidsStrategy) {
            long minBid = minOfAdvLatestBids() - 2; // minus 2 if adv has same strategy
            if (bid < minBid) { // we never bid too low, if our marginal cost is negative, we end up here also
                updateBidRateForNextBid = false;
                advLatestBids.offer(minBid);
                long finalBid = Math.max(1, minBid);
                PrintHandler.println("bid is smaller than " + minBid + ", returning finalBid = " + finalBid, 0);
                return finalBid;
            }
        }

        if (useEarlyBidStrategy && isEarlyBid()) { // first 5 bids will have lower bids (until 5 are won)
            double earlyRate = (bidsWonCounter + 5.0) / 10.0;
            PrintHandler.println("early bids have a secondary discount rate: bid = earlyRate * bid = " + bid + " * " + earlyRate + " = " + (bid * earlyRate), 1);
            bid *= earlyRate; // we want first tasks, hence first is 50% of real bid, then 60, 70, 80, 90, and finally 100% for the remaining
        }

        long finalBid = (long) Math.max(1, bid);
        PrintHandler.println("returning finalBid = " + finalBid, 0);
        return finalBid;
    }

    private boolean isEarlyBid() {
        return bidsWonCounter < 5;
    }

    private long minOfAdvLatestBids() {
        if (advLatestBids.isEmpty()) {
            return 0;
        }
        return advLatestBids.stream().min(Comparator.naturalOrder()).get();
    }

    private void increaseBidRate() {
        if (updateBidRateForNextBid) {
            if (learningRate != 0) {
                double newBidRate = bidRate * (1 + learningRate);
                PrintHandler.println("increasing bidRate: " + bidRate + " -> " + newBidRate, 2);
                bidRate = newBidRate;
            }
        }
    }

    private void decreaseBidRate() {
        if (updateBidRateForNextBid) {
            if (learningRate != 0) {
                double newBidRate = bidRate / (1 + 2 * learningRate);
                PrintHandler.println("decreasing bidRate: " + bidRate + " -> " + newBidRate, 2);
                bidRate = newBidRate;
            }
        }
    }

    private double marginCostDifNormalized(double ourCost, double advCost) {
        double dif = advCost - ourCost;
        marginalCostsDif.add(dif);
        double maxDif = marginalCostsDif.stream().max(Comparator.naturalOrder()).get();
        double minDif = marginalCostsDif.stream().min(Comparator.naturalOrder()).get();
        return 2 * ((dif - minDif) / (maxDif - minDif)) - 1;
    }

    /**
     * Improve Bidder by getting information of previous auction results
     */
    public void addInfoOfLastAuction(Task previous, int winner, Long[] bids) {
        if (agent.id() == winner) { // we took the task
            ourPlanner.addTask(previous);
            increaseBidRate();
            bidsWonCounter += 1;
        } else {
            advPlanner.addTask(previous);
            decreaseBidRate();
            bidsLostCounter += 1;
        }

        // keep trace of adversary bids
        if (bids.length > 1) {
            int advBidIndex = (agent.id() + 1) % 2;
            Long advBid = bids[advBidIndex];
            if (advLatestBids.size() >= 2 * numOfAdvLatestBids) {
                advLatestBids.poll();
            }
            advLatestBids.offer(advBid);
        }
    }


}
