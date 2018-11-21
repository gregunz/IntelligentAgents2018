package algo;

import logist.agent.Agent;
import logist.task.Task;
import print.PrintHandler;

import java.util.ArrayList;
import java.util.List;

public class Bidder {

    private final Agent agent;
    private final long bidTimeout;

    private final Planner ourPlanner;
    private final Planner advPlanner;
    private final TaskImportanceEstimator taskImpEst;

    private final BidderParameters p;
    private double bidRate;

    private int bidsWonCounter = 0;
    private int bidsLostCounter = 0;

    private List<Long> advBids = new ArrayList<>();
    private List<Long> ourBids = new ArrayList<>();


    private boolean updateBidRateForNextBid = false;

    public Bidder(Agent agent, long bidTimeout, TaskImportanceEstimator taskImpEst, double bidRate, BidderParameters p) {
        this.agent = agent;
        this.ourPlanner = new Planner(agent.vehicles());
        this.advPlanner = new Planner(agent.vehicles());
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
            bid *= earlyRate;
        }

        if (p.useImportanceStrategy) {
            double marginalDif = 0;
            if (taskImpEst.mustComputeMarginalDif()) {
                marginalDif = this.advPlanner.estimateMarginalCost(task, p.timeForAdvPlanner) - marginalCost;
            }
            double importance = taskImpEst.get(task, marginalDif);
            double newBid = bid / (1 + p.importanceLR * importance);
            PrintHandler.println("[BID] = bid / (1 + importanceLR * importance) = "
                    + bid + " / ( 1 + " + p.importanceLR + " * " + importance + ") = " + newBid, 1);
            bid = newBid;
        }

        if (p.useMinOfAdvBidsStrategy) {
            long minBid = minOfLatestBids() - p.difWithLatestBids;
            if (bid < minBid) {
                updateBidRateForNextBid = false;
                long finalBid = Math.max(p.smallestBid, minBid);
                PrintHandler.println("[BID] < " + minBid + ", returning finalBid = " + finalBid, 0);
                return finalBid;
            }
        }

        long finalBid = (long) Math.max(p.smallestBid, bid);
        PrintHandler.println("[BID] returning finalBid = " + finalBid, 0);
        return finalBid;
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
                double newBidRate = bidRate * (1 + p.deLearningRate);
                PrintHandler.println("[UPT] increasing bidRate: " + bidRate + " -> " + newBidRate, 2);
                bidRate = newBidRate;
            }
        }
    }

    private void decreaseBidRate() {
        if (updateBidRateForNextBid) {
            if (p.deLearningRate != 0) {
                double newBidRate = bidRate / (1 + p.inLearningRate);
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
