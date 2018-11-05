package algo;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology;
import models.ActionSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class SLS implements ISLS {
    private double prob;
    private boolean isInit;
    private int numIter;
    private List<ActionSequence> actualPlans;

    public SLS(double prob) {
        this.prob = prob;
        this.isInit = false;
        this.numIter = 0;
        this.actualPlans = new ArrayList<>();
    }

    @Override
    // Take the vehicle with the largest capacity and plan deliver the task completely at random
    public void init(List<Vehicle> vehicles, TaskSet tasks) {
        if (isInit) {
            throw new UnsupportedOperationException("cannot init twice");
        } else {
            isInit = true;
            Random rand = new Random();
            Vehicle largest = vehicles.get(0);
            for (Vehicle v : vehicles) {
                if (v.capacity() > largest.capacity()) {
                    largest = v;
                }
            }
            List<Task> taskTaken = new ArrayList<>();
            List<Task> taskNotTaken = new ArrayList<>(tasks);

            Topology.City current = largest.getCurrentCity();
            ActionSequence initialPlan = new ActionSequence(largest);
            while (!taskTaken.isEmpty() || !taskNotTaken.isEmpty()) {
                int possibleChoice = taskTaken.size() + taskNotTaken.size();

                int n = rand.nextInt(possibleChoice);
                if (n >= taskTaken.size()) {
                    Task task = taskNotTaken.get(n - taskTaken.size());
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

            // create plan for each vehicles
            List<ActionSequence> plans = new ArrayList<>();
            for (int i = 0; i < vehicles.size(); i++) {
                if (i == vehicles.indexOf(largest)) {
                    plans.add(initialPlan);
                } else {
                    plans.add(new ActionSequence(vehicles.get(i)));
                }
            }
            this.actualPlans = plans;

        }
    }

    @Override
    public Set<List<ActionSequence>> chooseNeighbours() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void localChoice(Set<List<ActionSequence>> neighbors) {
        numIter += 1;
        throw new UnsupportedOperationException();
    }

    public boolean numIterStoppingCriterion(int maxNumIter) {
        return numIter < maxNumIter;
    }

    public List<Plan> getActualPlans() {
        return actualPlans.stream().map(ActionSequence::getPlan).collect(Collectors.toUnmodifiableList());
    }
}
