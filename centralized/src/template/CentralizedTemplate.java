package template;

//the list of imports

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
import java.util.Random;

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
                plans = slsPlans(vehicles, tasks);
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

    private List<Plan> slsPlans(List<Vehicle> vehicles, TaskSet tasks) {

        // A <- initialSolution(X, D, C, f)
        List<Plan> A = selectInitialSolution(vehicles, tasks);

        // repeat ------

        // Aold <- A
        // N <- ChooseNeighbours(Aold, X, D, C, f)
        // A <- LocalChoice(N,f)

        // until termination condition met ------

        // return A

        return A;
    }

    // Take the vehicle with the largest capacity and plan deliver the task completely at random
    private List<Plan> selectInitialSolution(List<Vehicle> vehicles, TaskSet tasks) {

        Random rand = new Random();
        List<Plan> plans = new ArrayList<>();
        Vehicle largest = vehicles.get(0);
        for (Vehicle v : vehicles) {
            if (v.capacity() > largest.capacity()) {
                largest = v;
            }
        }
        List<Task> taskTaken = new ArrayList<>();
        List<Task> taskNotTaken = new ArrayList<>(tasks);

        City current = largest.getCurrentCity();
        ActionSequence initialPlan = new ActionSequence(largest);
        while (!taskTaken.isEmpty() || !taskNotTaken.isEmpty()) {
            int possibleChoice = taskTaken.size() + taskNotTaken.size();

            int  n = rand.nextInt(possibleChoice);
            if (n >= taskTaken.size()) {
                Task task = taskNotTaken.get(n-taskTaken.size());
                if (initialPlan.addLoadAction(task)) {
                    taskNotTaken.remove(n - taskTaken.size());
                    taskTaken.add(task);
                }
            } else {
                Task task = taskTaken.get(n);
                if (initialPlan.addDropAction(task)) {
                    taskTaken.remove(task);
                }
            }

        }
        for (int i = 0; i < vehicles.size(); i++) {
            if (i == vehicles.indexOf(largest)) {
                plans.add(initialPlan.getPlan());
            } else {
                plans.add(Plan.EMPTY);
            }
        }
        return plans;
    }

    enum Algorithm {NAIVE, SLS}
}
