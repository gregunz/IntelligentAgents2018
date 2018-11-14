package algo.astar;

import java.util.*;

public class VisitOnceQueue implements Queue<State> {
    private Map<State, State> visitedStates;
    private Queue<State> statesQueue;

    public VisitOnceQueue(Queue<State> statesQueue) {
        this(statesQueue, new HashSet<>());
    }

    public VisitOnceQueue(Queue<State> statesQueue, Set<State> visitedStates) {
        this.visitedStates = new HashMap<>();
        this.statesQueue = statesQueue;
    }

    public void visit(State state) {
        if (!visitedStates.containsKey(state) || state.getCurrentCost() < visitedStates.get(state).getCurrentCost()) {
            visitedStates.put(state, state);
        }
    }

    public boolean hasNotVisited(State state) {
        return !visitedStates.containsKey(state);
    }

    @Override
    public int size() {
        return statesQueue.size();
    }

    @Override
    public boolean isEmpty() {
        return statesQueue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return statesQueue.contains(o);
    }

    @Override
    public Iterator<State> iterator() {
        return statesQueue.iterator();
    }

    @Override
    public Object[] toArray() {
        return statesQueue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return statesQueue.toArray(a);
    }

    @Override
    public boolean add(State state) {
        if (!visitedStates.containsKey(state) || state.getCurrentCost() < visitedStates.get(state).getCurrentCost()) {
            return statesQueue.add(state);
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return statesQueue.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return statesQueue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends State> c) {
        int sizeBefore = statesQueue.size();
        c.forEach(this::add);
        return sizeBefore == statesQueue.size();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return statesQueue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return statesQueue.retainAll(c);
    }

    @Override
    public void clear() {
        statesQueue.clear();
    }

    @Override
    public boolean offer(State state) {
        return statesQueue.offer(state);
    }

    @Override
    public State remove() {
        return statesQueue.remove();
    }

    @Override
    public State poll() {
        State state = statesQueue.poll();
        if (visitedStates.containsKey(state) && state.getCurrentCost() < visitedStates.get(state).getCurrentCost()) {
            visitedStates.remove(state);
        }
        return state;
    }

    @Override
    public State element() {
        return statesQueue.element();
    }

    @Override
    public State peek() {
        return statesQueue.peek();
    }

}
