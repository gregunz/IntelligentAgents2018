package algo;

import logist.plan.Plan;
import models.State;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public class AStar {
    private AStar() {
    }

    private static double weightOfTasksNotTaken(State s) {
        double factor = 1000d;
        return factor * s.getTaskNotTaken().weightSum();
    }

    private static double capacityRemaining(State s) {
        double factor = 1d;
        return factor * s.getCapacityRemaining();
    }

    public static Plan run(final State startingState, Heuristic h) {

        Comparator<State> statesComparator = (s1, s2) -> {
            switch (h) {
                case CAPACITY:
                    return Double.compare(s1.getCost() + capacityRemaining(s1),
                            s2.getCost() + capacityRemaining(s2));
                case WEIGHT_NOT_TAKEN:
                    return Double.compare(s1.getCost() + weightOfTasksNotTaken(s1),
                            s2.getCost() + weightOfTasksNotTaken(s2));
                case ZERO:
                default:
                    return Double.compare(s1.getCost(), s2.getCost());
            }

        };

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

        return state.toPlan(startingState);
    }
}

