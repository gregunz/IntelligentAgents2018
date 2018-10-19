package models;

import logist.topology.Topology;

public class MoveAction implements Action {

    private Topology.City fromCity;
    private Topology.City toCity;
    private double costPerKM = 1; // <-- this needs to be setup differently

    public MoveAction(Topology.City fromCity, Topology.City toCity) {
        this.fromCity = fromCity;
        this.toCity = toCity;
    }

    @Override
    public logist.plan.Action getAction() {
        return new logist.plan.Action.Move(toCity);
    }

    @Override
    public State getNextState(State state) {
        return new StateRepresentation(toCity, state.getTaskTaken(), state.getCapacityRemaining(), state.getTaskNotTaken());
    }

    @Override
    public double getReward() {
        return - fromCity.distanceTo(toCity) * costPerKM;
    }
}
