package algo;

import logist.plan.Plan;
import models.State;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class BFS {
    private BFS() {
    }

    public static Plan run(final State startingState) {
        State state = startingState;
        Queue<State> statesQueue = new LinkedList<>(startingState.getNextStates());
        Set<State> visitedStates = new HashSet<>();

        int nSteps = 0;
        while (!state.isFinalState() && !statesQueue.isEmpty()) {
            nSteps += 1;
            state = statesQueue.poll();
            if (!visitedStates.contains(state)) {
                visitedStates.add(state);
                statesQueue.addAll(state.getNextStates());
            }
        }

        System.out.println("BFS converged in " + nSteps + " number of steps");

        if (!state.isFinalState()) {
            throw new IllegalStateException("BFS did not find any final state");
        }

        return state.toPlan(startingState);
    }

}
