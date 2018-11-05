package algo;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import models.ActionSequence;

import java.util.*;
import java.util.stream.Collectors;

public class SLS extends ISLS<List<ActionSequence>> {
    private double prob;
    private Random random;
    private boolean isInit;
    private int numIter;
    private long startTime;
    private List<ActionSequence> actualPlans;

    public SLS(double prob) {
        this.prob = prob;
        this.isInit = false;
        this.numIter = 0;
        this.actualPlans = new ArrayList<>();
        this.random = new Random();
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
    public double objectiveOf(List<ActionSequence> actionSequences) {
        double cost = 0;
        for (ActionSequence seq : actionSequences) {
            cost += seq.getCost();
        }
        return cost;
    }

    @Override
    public Set<List<ActionSequence>> chooseNeighbours() {
        Set<List<ActionSequence>> neighbours = new HashSet<>();

        neighbours.addAll(passTasksAround());

        neighbours.addAll(moveTasksInTime());

        return neighbours;
    }

    @Override
    public void localChoice(Set<List<ActionSequence>> neighbors) {
        double minObj = Double.MAX_VALUE;
        List<List<ActionSequence>> choices = new ArrayList<>();

        for (List<ActionSequence> plans : neighbors) {
            double obj = objectiveOf(plans);
            if (obj < minObj) {
                choices = new ArrayList<>();
                choices.add(plans);
            } else if (obj == minObj) {
                choices.add(plans);
            }
        }
        int idx = 0;
        if (choices.size() > 1) {
            idx = random.nextInt(choices.size());
        }

        if (random.nextDouble() < this.prob) {
            if (objectiveOf(this.actualPlans) != objectiveOf(choices.get(idx))) {
                System.out.println(numIter + "\t=\t" + objectiveOf(this.actualPlans()) +
                        "\t->\t" + objectiveOf(choices.get(idx)));
            }
            this.actualPlans = choices.get(idx);
        }
        numIter += 1;
    }

    @Override
    public List<ActionSequence> actualPlans() {
        return Collections.unmodifiableList(actualPlans);
    }

    @Override
    public List<Plan> actualLogistPlans() {
        return actualPlans.stream().map(ActionSequence::getPlan).collect(Collectors.toList());
    }

    public boolean numIterStoppingCriterion(int maxNumIter) {
        return numIter < maxNumIter;
    }

    public boolean durationStoppingCriterion(long startTime, long maxDuration) {
        return (System.currentTimeMillis() - startTime) < maxDuration;
    }

    private List<List<ActionSequence>> passTasksAround(){
        List<List<ActionSequence>> neighbours = new ArrayList<>();

        for (int i = 0; i < actualPlans.size(); i++) {
            if (actualPlans.get(i).getLength() > 0) {
                for (int j = 0; j < actualPlans.size(); j++) {
                    if (i != j) {
                        List<ActionSequence> newPlans = getCopyOfPlans(actualPlans);
                        Task task = newPlans.get(i).takeOutFirstTask();
                        if (newPlans.get(j).addLoadAction(task)){
                            newPlans.get(j).addDropAction(task);
                            neighbours.add(newPlans);
                        }
                    }
                }
            }
        }
        return neighbours;
    }

    private List<List<ActionSequence>> moveTasksInTime(){
        List<List<ActionSequence>> neighbours = new ArrayList<>();

        for (int i = 0; i < actualPlans.size(); i++) {
            if (actualPlans.get(i).getLength() > 2) {
                for (int j = 0; j < actualPlans.get(i).getLength(); j++) {
                    List<ActionSequence> newPlans = getCopyOfPlans(actualPlans);
                    if(newPlans.get(i).advanceAction(j)){
                        neighbours.add(newPlans);
                    }
                    newPlans = getCopyOfPlans(actualPlans);
                    if(newPlans.get(i).postponeAction(j)){
                        neighbours.add(newPlans);
                    }
                }

            }
        }
    return neighbours;
    }

    private List<ActionSequence> getCopyOfPlans(List<ActionSequence> plans) {
        List<ActionSequence> copy = new ArrayList<>();
        for (ActionSequence plan : plans) {
            copy.add(new ActionSequence(plan));
        }
        return copy;
    }
}
