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

    private final long PLAN_TIME_MARGIN = (long) 1e3; // we stop 1 second before just to be sure!

    //private long timeout_setup;
    private long timeout_plan;

    private Bidder bidder;

    @Override
    public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

        this.bidder = new Bidder(topology, distribution, agent);

        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_auction.xml");
        } catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        // the setup method cannot last more than timeout_setup milliseconds
        //timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);

        long seed = -9019554669489983951L * agent.vehicles().get(0).hashCode() * agent.id();
        RandomHandler.set(seed);

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
        Planner planner = new Planner(vehicles, tasks);
        return planner.findBestPlan(timeout_plan - PLAN_TIME_MARGIN);
    }
}
