package algo;

import logist.plan.Action;
import models.State;

import java.util.*;
import java.util.stream.Collectors;

public class BFS {
    private BFS() {
    }

    public static List<Action> run(final State startingState) {
        State state = startingState;
        Queue<State> statesQueue = new LinkedList<>(startingState.getNextStates());
        Set<State> visitedStates = new HashSet<>();

        while (!state.isFinalState() && !statesQueue.isEmpty()) {
            state = statesQueue.poll();
            if (!visitedStates.contains(state)) {
                visitedStates.add(state);
                statesQueue.addAll(state.getNextStates());
            }
        }

        return state.getPreviousActions().stream().map(models.Action::getAction).collect(Collectors.toList());
    }

}
