package agent;

import algo.Bidder;
import algo.Planner;
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
import random.RandomHandler;

import java.util.Arrays;
import java.util.List;

public class AuctionAgent implements AuctionBehavior {

    //private long timeout_setup;
    private long planTimeout;

    private Bidder bidder;

    @Override
    public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

        System.out.println("we are agent <" + agent + ">");

        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_auction.xml");
        } catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }

        final long PLAN_TIME_MARGIN = (long) 1e3; // we stop 1 second before just to be sure!

        //timeoutSetup = ls.get(LogistSettings.TimeoutKey.SETUP) - PLAN_TIME_MARGIN;
        planTimeout = ls.get(LogistSettings.TimeoutKey.PLAN) - PLAN_TIME_MARGIN;
        long bidTimeout = ls.get(LogistSettings.TimeoutKey.BID) - PLAN_TIME_MARGIN;

        long seed = -9019554669489983951L * agent.vehicles().get(0).hashCode() * agent.id();
        RandomHandler.set(seed);

        this.bidder = new Bidder(topology, distribution, agent, bidTimeout);
    }

    @Override
    public Long askPrice(Task task) {
        Long bid = this.bidder.bid(task);
        System.out.println("task = <" + task + ">, bid = <" + bid + ">");
        return bid;
    }

    @Override
    public void auctionResult(Task lastTask, int lastWinner, Long[] lastOffers) {
        System.out.println("task was = <" + lastTask + ">, winner was = <" + lastWinner + ">, offers were <" + Arrays.toString(lastOffers) + ">");
        this.bidder.addInfoOfLastAuction(lastTask, lastWinner, lastOffers);
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        //TODO: question? do we really need vehicles and tasks as we set the vehicles at first AND added the task when we won in the bidder ?
        Planner planner = this.bidder.getPlanner();
        return planner.findBestPlan(planTimeout).toLogistPlans(tasks);
    }
}
