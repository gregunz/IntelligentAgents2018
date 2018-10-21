package algo;

import logist.plan.Plan;
import models.State;

import java.util.*;

public class AStar {
    private AStar() {
    }

    private static double weightOfTasksNotTaken(State s) {
        return (double) s.getTaskNotTaken().weightSum();
    }

    public static Plan run(final State startingState, Heuristic h) {

        Comparator<State> statesComparator = (s1, s2) -> {
            switch (h) {
                case WeightNotTaken:
                    return Double.compare(s1.getCost() + weightOfTasksNotTaken(s1),
                            s2.getCost() + weightOfTasksNotTaken(s2));
                case Zero:
                default:
                    return Double.compare(s1.getCost(), s2.getCost());
            }

        };

        State state = startingState;
        Queue<State> statesQueue = new PriorityQueue<>(statesComparator);
        statesQueue.addAll(startingState.getNextStates());
        Set<State> visitedStates = new HashSet<>();

        int nSteps = 0;
        while (!state.isFinalState() && !statesQueue.isEmpty()) {
            nSteps += 1;
            state = statesQueue.poll();
            if (!visitedStates.contains(state)) {
                visitedStates.add(state);
                state.getNextStates().forEach(s -> {
                    if (!visitedStates.contains(s)) {
                        statesQueue.add(s);
                    }
                });
            }
        }

        System.out.println("ASTAR converged in " + nSteps + " number of steps");

        if (!state.isFinalState()) {
            throw new IllegalStateException("ASTAR did not find any final state");
        }

        return state.toPlan(startingState);
    }
}

