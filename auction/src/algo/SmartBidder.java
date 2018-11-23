package algo;

import logist.agent.Agent;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import print.PrintHandler;
import random.RandomHandler;

import java.util.ArrayList;
import java.util.List;

public class SmartBidder extends Bidder {
    private final Agent agent;
    private final long bidTimeout;

    private final Planner ourPlanner;
    private final Planner advPlanner;
    private final Topology topology;
    private final TaskDistribution distribution;
    private final TaskImportanceEstimator taskImpEst;

    private final BidderParameters p;
    private double bidRate;

    private int bidsWonCounter = 0;
    private int bidsLostCounter = 0;

    private List<Long> advBids = new ArrayList<>();
    private List<Long> ourBids = new ArrayList<>();

    private boolean updateBidRateForNextBid = false;
    
    public SmartBidder(Agent agent, Topology topology, TaskDistribution distribution, long bidTimeout, TaskImportanceEstimator taskImpEst, double bidRate, BidderParameters p) {
        super();
        this.agent = agent;
        this.ourPlanner = new Planner(agent.vehicles());
        this.advPlanner = new Planner(agent.vehicles());
        this.topology = topology;
        this.distribution = distribution;
        this.taskImpEst = taskImpEst;
        this.bidTimeout = bidTimeout;
        this.bidRate = bidRate;
        this.p = p;

    }

    public Planner getOurPlanner() {
        return ourPlanner;
    }


    /**
     * Make a bid for the given task
     */
    public Long bid(Task task) {
        // default, we will update (either increase or decrease bidRate)
        updateBidRateForNextBid = p.deLearningRate != 0 || p.inLearningRate != 0;

        double marginalCost = this.ourPlanner.estimateMarginalCost(task, bidTimeout);
        double bid = bidRate * marginalCost;
        PrintHandler.println("[BID] = bidRate * marginalCost = " + bidRate + " * " + marginalCost + " = " + bid, 1);

        if (p.useEarlyBidStrategy && isEarlyBid()) {
            double earlyRate = p.earlyRate + ((double) bidsWonCounter / p.numEarlyBids) * (1 - p.earlyRate);
            PrintHandler.println("[BID] = earlyRate * bid = " + bid + " * " + earlyRate + " = " + (bid * earlyRate), 1);
            double newBid = bid * earlyRate;
            bid = Math.max(newBid, bid - p.maxDiscount);
        }

        if (p.useImportanceStrategy) {
            double marginalDif = 0;
            if (taskImpEst.mustComputeMarginalDif()) {
                marginalDif = marginalCost - this.advPlanner.estimateMarginalCost(task, p.timeForAdvPlanner);
            }
            double importance = taskImpEst.get(task, marginalDif);
            double newBid = bid * (1 - p.importanceLR * importance);
            PrintHandler.println("[BID] = bid * (1 - importanceLR * importance) = "
                    + bid + " * ( 1 - " + p.importanceLR + " * " + importance + ") = " + newBid, 1);
            bid = newBid;
        }

        if (p.useMinOfAdvBidsStrategy) {
            long minBid = minOfLatestBids() - p.difWithLatestBids;
            if (bid < minBid) {
                updateBidRateForNextBid = false;
                long finalBid = Math.max(p.smallestBid, minBid - RandomHandler.get().nextInt(10));
                PrintHandler.println("[BID] bid < " + minBid + ", returning finalBid = " + finalBid, 0);
                return applyCostUpperBound(task, finalBid, 1);
            }
        }

        long finalBid = (long) Math.max(p.smallestBid, bid);
        PrintHandler.println("[BID] returning finalBid = " + finalBid, 0);
        return applyCostUpperBound(task, finalBid, 2);
    }

    private long costOfTask(Task task) {
        return (long) (task.pickupCity.distanceTo(task.deliveryCity) * agent.vehicles().get(0).costPerKm());
    }

    private long applyCostUpperBound(Task task, long bid, int useIf) {
        if (p.useCostUpperBound >= useIf) {
            bid = Math.min(bid, costOfTask(task));
            updateBidRateForNextBid = false;
            PrintHandler.println("[BID] applying cost upper-bound = " + bid, 0);
        }
        return bid;
    }

    private boolean isEarlyBid() {
        return bidsWonCounter < p.numEarlyBids;
    }

    private long minOfLatestBids() {
        if (advBids.isEmpty() && ourBids.isEmpty()) {
            return 0;
        }
        long min = Long.MAX_VALUE;
        for (int i = 0; i < Math.min(advBids.size(), p.numOfAdvLatestBids); i++) {
            min = Math.min(min, advBids.get(advBids.size() - 1 - i));
        }
        for (int i = 0; i < Math.min(ourBids.size(), p.numOfOurLatestBids); i++) {
            min = Math.min(min, ourBids.get(ourBids.size() - 1 - i));
        }
        return min;
    }

    private void increaseBidRate() {
        if (updateBidRateForNextBid) {
            if (p.inLearningRate != 0) {
                double newBidRate = bidRate * (1 + p.inLearningRate);
                PrintHandler.println("[UPT] increasing bidRate: " + bidRate + " -> " + newBidRate, 2);
                bidRate = newBidRate;
            }
        }
    }

    private void decreaseBidRate() {
        if (updateBidRateForNextBid) {
            if (p.deLearningRate != 0) {
                double newBidRate = Math.max(bidRate * (1 - p.deLearningRate), p.minBidRate);
                PrintHandler.println("[UPT] decreasing bidRate: " + bidRate + " -> " + newBidRate, 2);
                bidRate = newBidRate;
            }
        }
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
            if (p.useImportanceStrategy && taskImpEst.mustComputeMarginalDif()) {
                advPlanner.addTask(previous);
            }
            decreaseBidRate();
            bidsLostCounter += 1;
        }

        ourBids.add(bids[agent.id()]);
        // keep trace of adversary bids
        if (bids.length > 1) {
            int advBidIndex = (agent.id() + 1) % 2;
            advBids.add(bids[advBidIndex]);
        }
    }
}
