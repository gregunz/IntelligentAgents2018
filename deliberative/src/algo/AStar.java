package algo;

import logist.plan.Plan;
import models.State;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class AStar {
    private AStar() {
    }

    public static Plan run(final State startingState) {

        Comparator<State> statesComparator = (s1, s2) -> {
            if (s2.getCurrentReward() - s1.getCurrentReward() > 0) {
                return 1;
            } else if (s2.getCurrentReward() - s1.getCurrentReward() < 0) {
                return -1;
            } else {
                return 0;
            }
        };

        State state = startingState;
        PriorityQueue<State> statesQueue = new PriorityQueue<>(statesComparator);
        statesQueue.addAll(startingState.getNextStates());
        Set<State> visitedStates = new HashSet<>();

        while (!state.isFinalState() && !statesQueue.isEmpty()) {
            state = statesQueue.poll();
            if (visitedStates.contains(state)) {
                continue;
            }
            visitedStates.add(state);
            statesQueue.addAll(state.getNextStates());
        }

        return state.toPlan(startingState);
    }
}
