package algo;

import template.State;

import java.util.Comparator;
import java.util.PriorityQueue;

public class AStar {
    private AStar() {
    }

    public static void run(State startingState) {
        Comparator<State> statesComparator = new Comparator<>() {
            @Override
            public int compare(State s1, State s2) {
                if (s2.getCurrentReward() - s1.getCurrentReward() > 0) {
                    return 1;
                } else if (s2.getCurrentReward() - s1.getCurrentReward() < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
        PriorityQueue<State> queue = new PriorityQueue<>(statesComparator);
        startingState.getAllPossibleActions().forEach(a -> {
            State s = startingState.getNextState(a);
            s.addReward(a.getReward());
            queue.add(s);
        });

    }
}
