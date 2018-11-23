package agent;

import algo.*;
import logist.LogistSettings;
import logist.agent.Agent;
import logist.behavior.AuctionBehavior;
import logist.config.Parsers;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import print.PrintHandler;
import random.RandomHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuctionAgent implements AuctionBehavior {

    private long planTimeout;

    private Bidder bidder;

    @Override
    public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

        PrintHandler.setVerbosityLevel(agent.readProperty("verbosity", Integer.class, 2));
        PrintHandler.println("[START] we are agent <" + agent.id() + ">", 1);

        BidderParameters parameters = new BidderParameters(agent);

        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_auction.xml");
        } catch (Exception exc) {
            PrintHandler.println("[FAIL] There was a problem loading the configuration file.");
        }

        TaskImportanceEstimator taskImpEst = new TaskImportanceEstimator(
                agent,
                topology,
                distribution,
                parameters.posWeight,
                parameters.probWeight,
                parameters.weightWeight,
                parameters.marginalWeight
        );

        final long PLAN_TIME_MARGIN = (long) 0.5e3; // we stop half a second before just to be sure!

        long timeForAdvPlanner = 0;
        if (parameters.useImportanceStrategy && taskImpEst.mustComputeMarginalDif()) {
            timeForAdvPlanner = parameters.timeForAdvPlanner;
        }
        long bidTimeout = ls.get(LogistSettings.TimeoutKey.BID) - PLAN_TIME_MARGIN - timeForAdvPlanner;
        planTimeout = ls.get(LogistSettings.TimeoutKey.PLAN) - 2 * PLAN_TIME_MARGIN;

        long seed = -901955466948998391L * agent.vehicles().get(0).hashCode() * agent.id();
        RandomHandler.set(seed);

        if (false) {
        bidder = new SmartBidder(
                agent,
                topology,
                distribution,
                bidTimeout,
                taskImpEst,
                agent.readProperty("bidRate", Double.class, 1.0),
                parameters
        );} else {
            Long setupTimeout = ls.get(LogistSettings.TimeoutKey.SETUP);
            int trials = 5;
            Double[] costs = new Double[trials];
            for (int i = 0; i < trials; i++) {
                Planner planner = new Planner(agent.vehicles());
                for (int j = 0; j < 10; j++){
                    Topology.City c1 = topology.randomCity(RandomHandler.get());
                    Topology.City c2 = topology.randomCity(RandomHandler.get());
                    if (c1 == c2) {
                        j--;
                    } else {
                        Task task = new Task(0, c1, c2, distribution.reward(c1, c2), distribution.weight(c1, c2));
                        planner.addTask(task);
                    }
                }
                System.out.println((long) (setupTimeout * (1./trials)));
                costs[i] = planner.findBestPlan((long) (setupTimeout * (1./trials))).getCost();

            }

            Double value = 0.;
            for (int i = 0; i < trials; i++){
                if (costs[i] > value) {
                    value = costs[i];
                }
            }
            bidder = new DumbBidder(agent,
                    topology,
                    distribution,
                    value / 10.
            );
        }
    }

    @Override
    public Long askPrice(Task task) {
        Long bid = this.bidder.bid(task);
        PrintHandler.println("[AGT] bid = <" + bid + ">, task = <" + task + ">", 2);
        return bid;
    }

    @Override
    public void auctionResult(Task lastTask, int lastWinner, Long[] lastOffers) {
        PrintHandler.println("[AGT] task was = <" + lastTask + ">, winner was = <" + lastWinner + ">, offers were <" + Arrays.toString(lastOffers) + ">", 2);
        this.bidder.addInfoOfLastAuction(lastTask, lastWinner, lastOffers);
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        Planner planner = this.bidder.getOurPlanner();
        return planner.findBestPlan(planTimeout).toLogistPlans(tasks);
    }
}
