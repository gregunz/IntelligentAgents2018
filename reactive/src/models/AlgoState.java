package models;

import logist.agent.Agent;
import logist.task.TaskDistribution;
import logist.topology.Topology;

public class AlgoState {

    private final Topology.City city;
    private final Topology.City taskDestination;

    public AlgoState(Topology.City city, Topology.City taskDestination) {
        if (city == null){
            throw new IllegalArgumentException("City cannot be null");
        }
        this.city = city;
        this.taskDestination = taskDestination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlgoState state = (AlgoState) o;

        if (!city.equals(state.city)) return false;
        return taskDestination != null ? taskDestination.equals(state.taskDestination) : state.taskDestination == null;
    }

    @Override
    public int hashCode() {
        int result = city.hashCode();
        result = 31 * result + (taskDestination != null ? taskDestination.hashCode() : 0);
        return result;
    }

    public Topology.City getCity() {
        return city;
    }

    public Topology.City getTaskDestination() {
        return taskDestination;
    }

}
