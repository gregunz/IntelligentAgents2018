package template;

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
import models.CentralizedPlan;
import models.InitStrategy;
import models.PlanGenerator;
import models.SLS;
import print.PrintHandler;

import java.util.ArrayList;
import java.util.List;

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

        PrintHandler.setVerbosityLevel(4);
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
        CentralizedPlan plan = PlanGenerator.generate(vehicles, new ArrayList<>(tasks), InitStrategy.ASTAR);
        plan = SLS.optimize(plan, timeout_plan - (long) 1e3);
        return plan.toLogistPlans();
    }

    enum Algorithm {NAIVE, SLS}
}
