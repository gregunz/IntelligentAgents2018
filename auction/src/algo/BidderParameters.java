package algo;

import logist.agent.Agent;
import print.PrintHandler;

public class BidderParameters {
    public final double minBidRate;
    public final double inLearningRate; // should be in [0, +inf] range
    public final double deLearningRate; // should be in [0, +inf] range
    public final long smallestBid;
    public final int useCostUpperBound;
    public final boolean useEarlyBidStrategy;
    public final double earlyRate;
    public final int numEarlyBids;
    public final int maxDiscount;
    public final boolean useImportanceStrategy;
    public final double importanceLR;
    public final long timeForAdvPlanner;
    public final double posWeight;
    public final double probWeight;
    public final double weightWeight;
    public final double marginalWeight;
    public final boolean useMinOfAdvBidsStrategy;
    public final int numOfAdvLatestBids;
    public final int numOfOurLatestBids;
    public final int difWithLatestBids;


    public BidderParameters(Agent agent) {
        minBidRate =
                agent.readProperty("inLearningRate", Double.class, 0.0);
        inLearningRate =
                agent.readProperty("inLearningRate", Double.class, 0.1);
        deLearningRate =
                agent.readProperty("deLearningRate", Double.class, 0.15);
        smallestBid =
                agent.readProperty("smallestBid", Integer.class, 1);
        useCostUpperBound =
                agent.readProperty("smallestBid", Integer.class, 0);
        useEarlyBidStrategy =
                agent.readProperty("useEarlyBidStrategy", Boolean.class, true);
        earlyRate =
                agent.readProperty("earlyRate", Double.class, 0.5);
        numEarlyBids =
                agent.readProperty("numEarlyBids", Integer.class, 5);
        maxDiscount =
                agent.readProperty("maxDiscount", Integer.class, 300);
        useImportanceStrategy =
                agent.readProperty("useImportanceStrategy", Boolean.class, true);
        importanceLR =
                agent.readProperty("importanceLR", Double.class, 0.2);
        timeForAdvPlanner =
                agent.readProperty("timeForAdvPlanner", Integer.class, 500);
        final double numOfWeights = 4;
        posWeight =
                agent.readProperty("posWeight", Double.class, 1. / numOfWeights);
        probWeight =
                agent.readProperty("probWeight", Double.class, 1. / numOfWeights);
        weightWeight =
                agent.readProperty("weightWeight", Double.class, 1. / numOfWeights);
        marginalWeight =
                agent.readProperty("marginalWeight", Double.class, 1. / numOfWeights);
        useMinOfAdvBidsStrategy =
                agent.readProperty("useMinOfAdvBidsStrategy", Boolean.class, true);
        numOfAdvLatestBids =
                agent.readProperty("numOfAdvLatestBids", Integer.class, 5);
        numOfOurLatestBids =
                agent.readProperty("numOfOurLatestBids", Integer.class, 5);
        difWithLatestBids =
                agent.readProperty("difWithLatestBids", Integer.class, 2);

        PrintHandler.println(
                "(" + inLearningRate + ", " +
                        deLearningRate + ", " +
                        smallestBid + ", " +
                        useEarlyBidStrategy + ", " +
                        earlyRate + ", " +
                        numEarlyBids + ", " +
                        maxDiscount + ", " +
                        useImportanceStrategy + ", " +
                        importanceLR + ", " +
                        timeForAdvPlanner + ", " +
                        posWeight + ", " +
                        probWeight + ", " +
                        weightWeight + ", " +
                        marginalWeight + ", " +
                        useMinOfAdvBidsStrategy + ", " +
                        numOfAdvLatestBids + ", " +
                        numOfOurLatestBids + ", " +
                        difWithLatestBids + ")");
    }
}
