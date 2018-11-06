package algo.astar;

import logist.simulation.Vehicle;
import logist.task.TaskSet;
import logist.topology.Topology;
import models.ActionSequence;

import java.util.*;

public class AStar {
    private AStar() {
    }

    private static double weightOfTasksNotTaken(State s) {
        double factor = 1e4;
        return factor * s.getTaskNotTaken().size();
    }

    private static double distanceRemaining1(State s) {
        double factor = 1;
        double h = 0;
        if (!s.getTaskNotTaken().isEmpty()) {
            h = Math.max(h, s.getTaskNotTaken().stream()
                    .map(t -> s.getCurrentCity().distanceTo(t.pickupCity))
                    .max(Comparator.comparing(Double::doubleValue))
                    .get());
        }
        if (!s.getTaskTaken().isEmpty()) {
            h = Math.max(h, s.getTaskTaken().stream()
                    .map(t -> s.getCurrentCity().distanceTo(t.deliveryCity))
                    .max(Comparator.comparing(Double::doubleValue))
                    .get());
        }
        return factor * h * s.getCostPerKM();
    }

    private static double distanceRemaining2(State s) {
        double factor = 1;
        double h = 0;
        List<Topology.City> citiesToGoThrough = new ArrayList<>();

        if (!s.getTaskNotTaken().isEmpty()) {
            citiesToGoThrough.add(s.getTaskNotTaken().iterator().next().pickupCity);
        }
        if (!s.getTaskTaken().isEmpty()) {
            citiesToGoThrough.add(s.getTaskTaken().iterator().next().deliveryCity);
        }
        if (citiesToGoThrough.size() == 2) {
            h = Math.min(
                    s.getCurrentCity().distanceTo(citiesToGoThrough.get(0)) +
                            citiesToGoThrough.get(0).distanceTo(citiesToGoThrough.get(1)),
                    s.getCurrentCity().distanceTo(citiesToGoThrough.get(1)) +
                            citiesToGoThrough.get(1).distanceTo(citiesToGoThrough.get(0))
            );
        } else if (citiesToGoThrough.size() == 1) {
            h = s.getCurrentCity().distanceTo(citiesToGoThrough.get(0));
        }
        return factor * h * s.getCostPerKM();
    }

    private static double distanceRemaining3(State s) {
        double factor = 1;
        double minDistance = 40; // this is hardcoded for Switzerland Topology
        Set<Topology.City> citiesToGoThrough = new HashSet<>();
        s.getTaskNotTaken().forEach(t ->
                citiesToGoThrough.add(t.pickupCity)
        );
        s.getTaskTaken().forEach(t ->
                citiesToGoThrough.add(t.deliveryCity)
        );
        return factor * citiesToGoThrough.size() * minDistance * s.getCostPerKM();
    }

    public static ActionSequence run(final Vehicle v, final TaskSet ts, final Heuristic h) {

        Comparator<State> statesComparator = (s1, s2) -> {
            switch (h) {
                case DISTANCE_REMAINING1:
                    return Double.compare(s1.getCurrentCost() + distanceRemaining1(s1),
                            s2.getCurrentCost() + distanceRemaining1(s2));
                case DISTANCE_REMAINING2:
                    return Double.compare(s1.getCurrentCost() + distanceRemaining2(s1),
                            s2.getCurrentCost() + distanceRemaining2(s2));
                case DISTANCE_REMAINING3:
                    return Double.compare(s1.getCurrentCost() + distanceRemaining3(s1),
                            s2.getCurrentCost() + distanceRemaining3(s2));
                case WEIGHT_NOT_TAKEN:
                    return Double.compare(s1.getCurrentCost() + weightOfTasksNotTaken(s1),
                            s2.getCurrentCost() + weightOfTasksNotTaken(s2));
                case ZERO:
                default:
                    return Double.compare(s1.getCurrentCost(), s2.getCurrentCost());
            }

        };

        State startingState = new StateRepresentation(v, ts);
        VisitOnceQueue statesQueue = new VisitOnceQueue(new PriorityQueue<>(statesComparator), new HashSet<>());
        statesQueue.visit(startingState);
        statesQueue.addAll(startingState.getNextStates());

        int nSteps = 0;
        State state = startingState;
        while (!state.isFinalState() && !statesQueue.isEmpty()) {
            nSteps += 1;
            state = statesQueue.poll();
            if (statesQueue.hasNotVisited(state)) {
                statesQueue.visit(state);
                statesQueue.addAll(state.getNextStates());
            }
        }

        System.out.println("ASTAR converged in " + nSteps + " number of steps");

        if (!state.isFinalState()) {
            throw new IllegalStateException("ASTAR did not find any final state");
        }

        return state.toActionSequence(v);
    }
}

