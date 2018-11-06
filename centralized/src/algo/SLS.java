package algo;

import algo.astar.AStar;
import algo.astar.Heuristic;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import models.ActionSequence;

import java.util.*;
import java.util.stream.Collectors;

public class SLS extends ISLS<List<ActionSequence>> {
    private static final boolean DISPLAY_PRINT = true;
    private static final boolean INIT_WITH_ASTAR = true;

    private double prob;
    private Random random;
    private boolean isInit;
    private int numIter;
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
            Vehicle largest = vehicles.get(0);
            for (Vehicle v : vehicles) {
                if (v.capacity() > largest.capacity()) {
                    largest = v;
                }
            }
            ActionSequence initialPlan;
            if (INIT_WITH_ASTAR) {
                initialPlan = AStar.run(largest, tasks, Heuristic.WEIGHT_NOT_TAKEN);
            } else {
                List<Task> taskTaken = new ArrayList<>();
                List<Task> taskNotTaken = new ArrayList<>(tasks);

                initialPlan = new ActionSequence(largest);
                while (!taskTaken.isEmpty() || !taskNotTaken.isEmpty()) {
                    int possibleChoice = taskTaken.size() + taskNotTaken.size();

                    int n = random.nextInt(possibleChoice);
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
            }

            if (initialPlan.isValid()) {
                System.out.println("The initial plan is indeed valid");
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

        int n = random.nextInt(actualPlans.size());
        if (actualPlans.get(n).getLength() == 0) {
            return chooseNeighbours();
        }

        neighbours.addAll(passTasksAround(n));

        neighbours.addAll(moveTasksInTime(n));

        return neighbours;
    }

    @Override
    public void localChoice(Set<List<ActionSequence>> neighbors) {
        if (neighbors.isEmpty()) {
            System.out.println("NO NEIGHBORS");
        }
        double minObj = Double.MAX_VALUE;
        List<List<ActionSequence>> choices = new ArrayList<>();

        for (List<ActionSequence> plans : neighbors) {
            double obj = objectiveOf(plans);
            if (obj < minObj) {
                choices = new ArrayList<>();
                choices.add(plans);
                minObj = obj;
            } else if (obj == minObj) {
                choices.add(plans);
            }
        }

        // with probability p we take the best neighbor, otherwise TAKE ONE AT RANDOM
        if (random.nextDouble() < this.prob) {
            int idx = 0;
            if (choices.size() > 1) {
                idx = random.nextInt(choices.size());
            }
            List<ActionSequence> bestNeighbor = choices.get(idx);
            if (DISPLAY_PRINT) {
                System.out.println(numIter + "\t(best)\t=\t" + getActualCost() +
                        "\t->\t" + objectiveOf(bestNeighbor));
            }
            this.actualPlans = bestNeighbor;
        } else {
            List<ActionSequence> rdmNeighbor = new ArrayList<>(neighbors).get(random.nextInt(neighbors.size()));
            if (DISPLAY_PRINT) {
                System.out.println(numIter + "\t(random)\t=\t" + getActualCost() +
                        "\t->\t" + objectiveOf(rdmNeighbor));
            }
            this.actualPlans = rdmNeighbor;

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

    public double getActualCost() {
        return objectiveOf(this.actualPlans);
    }

    public boolean numIterStoppingCriterion(int maxNumIter) {
        return numIter < maxNumIter;
    }

    public boolean durationStoppingCriterion(long startTime, long maxDuration) {
        return (System.currentTimeMillis() - startTime) < maxDuration;
    }

    private List<List<ActionSequence>> passTasksAround(int n){
        List<List<ActionSequence>> neighbours = new ArrayList<>();

        if (actualPlans.get(n).getLength() > 0) {
            for (int j = 0; j < actualPlans.size(); j++) {
                if (n != j) {
                    List<ActionSequence> newPlans = getCopyOfPlans(actualPlans);
                    Task task = newPlans.get(n).takeOutFirstTask();
                    if (newPlans.get(j).addLoadAction(task)){
                        newPlans.get(j).addDropAction(task);
                        neighbours.add(newPlans);
                    }
                }
            }
        }

        return neighbours;
    }

    private List<List<ActionSequence>> moveTasksInTime(int n){
        List<List<ActionSequence>> neighbors = new ArrayList<>();

        if (actualPlans.get(n).getLength() > 2) {
            int t = random.nextInt(actualPlans.get(n).getLength());

                boolean isValid;
                int i = t;
                do {
                    List<ActionSequence> newPlans = getCopyOfPlans(actualPlans);
                    isValid = newPlans.get(n).advanceAction(i);
                    if (isValid) {
                        neighbors.add(newPlans);
                    }
                    i--;
                } while (isValid);
                i = t;
                do {
                    List<ActionSequence> newPlans = getCopyOfPlans(actualPlans);
                    isValid = newPlans.get(n).postponeAction(i);
                    if (isValid) {
                        neighbors.add(newPlans);
                    }
                    i++;
                } while (isValid);


        }
        return neighbors;
    }

    private List<ActionSequence> getCopyOfPlans(List<ActionSequence> plans) {
        List<ActionSequence> copy = new ArrayList<>();
        for (ActionSequence plan : plans) {
            copy.add(new ActionSequence(plan));
        }
        return copy;
    }
}
