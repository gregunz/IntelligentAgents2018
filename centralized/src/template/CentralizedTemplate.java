package template;

//the list of imports

import algo.SLS;
import logist.LogistSettings;
import logist.agent.Agent;
import logist.behavior.CentralizedBehavior;
import logist.config.Parsers;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import models.ActionSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 */
@SuppressWarnings("unused")
public class CentralizedTemplate implements CentralizedBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;

    private Algorithm algorithm;

    @Override
    public void setup(Topology topology, TaskDistribution distribution,
                      Agent agent) {

        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_default.xml");
        } catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }

        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        String algorithmChosen = agent.readProperty("algorithm", String.class, "NAIVE").toUpperCase();
        algorithm = Algorithm.valueOf(algorithmChosen);

        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();

        System.out.println("Agent " + agent.id() + " has tasks " + tasks);

        List<Plan> plans = new ArrayList<Plan>();
        switch (algorithm) {
            case NAIVE:
                Plan planVehicle1 = naivePlan(vehicles.get(0), tasks);
                plans.add(planVehicle1);
                while (plans.size() < vehicles.size()) {
                    plans.add(Plan.EMPTY);
                }
                break;
            case SLS:
                plans = slsPlans(vehicles, tasks, time_start);
                break;
        }

        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");


        float totalCost = 0;
        for (int i = 0; i < plans.size(); i++) {
            totalCost += plans.get(i).totalDistance() * vehicles.get(i).costPerKm();
        }
        System.out.println("The plan costs a total of " + totalCost);

        return plans;
    }

    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity)) {
                plan.appendMove(city);
            }

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path()) {
                plan.appendMove(city);
            }

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }

    private List<Plan> slsPlans(List<Vehicle> vehicles, TaskSet tasks, long startTime) {

        double exploitationRate = 0.99;
        int exploitationDeepness = (int) 1e5;
        long maxDuration = timeout_plan - (long) 1e3; // we stop 1 seconds before timeout
        long seed = System.currentTimeMillis();
        boolean oneInitWithAStar = true;

        System.out.println("PARAMETERS: \texploitationRate=" + exploitationRate + " \texploitationDeepness=" + exploitationDeepness + " \tSEED=" + seed);
        System.out.println("Initializing SLS algorithm");
        SLS sls = new SLS(exploitationRate, seed);
        sls.init(vehicles, tasks, oneInitWithAStar);
        int numOfInit = 1;

        double minCost = Double.MAX_VALUE;
        List<Plan> bestPlans = null;

        System.out.println("Starting SLS convergence");
        while (sls.durationStoppingCriterion(startTime, maxDuration)) {
            while (sls.numIterStoppingCriterion(exploitationDeepness) && sls.durationStoppingCriterion(startTime, maxDuration)) {
                Set<List<ActionSequence>> neighbors = sls.chooseNeighbours();
                sls.localChoice(neighbors);

                double cost = sls.getActualCost();
                if (cost < minCost) {
                    minCost = cost;
                    bestPlans = sls.actualLogistPlans();
                    System.out.println("BEST COST UPDATED = " + minCost);
                }
            }
            sls = new SLS(exploitationRate, seed + numOfInit);
            sls.init(vehicles, tasks, false);
            numOfInit += 1;
        }
        System.out.println("#RESTART = " + numOfInit);
        System.out.println("SLS has converged!");
        System.out.println("PARAMETERS: \texploitationRate=" + exploitationRate + " \texploitationDeepness=" + exploitationDeepness + " \tSEED=" + seed);

        return bestPlans;
    }

    enum Algorithm {NAIVE, SLS}
}
