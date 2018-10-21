package algo;

import logist.plan.Plan;
import models.State;

import java.util.HashSet;
import java.util.LinkedList;

public class BFS {
    private BFS() {
    }

    public static Plan run(final State startingState) {
        VisitOnceQueue statesQueue = new VisitOnceQueue(new LinkedList<>(), new HashSet<>());
        statesQueue.visit(startingState);
        statesQueue.addAll(startingState.getNextStates());

        State bestState = null;
        if (startingState.isFinalState()) {
            bestState = startingState;
        }

        int nSteps = 0;

        State state;
        while (!statesQueue.isEmpty()) {
            nSteps += 1;

            state = statesQueue.poll();
            if (!statesQueue.hasVisitedElseVisit(state)) {
                statesQueue.addAll(state.getNextStates());
            }
            if (state.isFinalState() && (bestState == null || bestState.getCost() > state.getCost())) {
                bestState = state;
            }
        }

        System.out.println("BFS converged in " + nSteps + " number of steps");

        if (bestState == null) {
            throw new IllegalStateException("BFS did not find any final state");
        }

        return bestState.toPlan(startingState);
    }

}
