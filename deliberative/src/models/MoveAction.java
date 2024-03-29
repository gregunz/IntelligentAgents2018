package models;

import logist.topology.Topology;

import java.util.ArrayList;
import java.util.List;

public class MoveAction implements Action {

    private Topology.City fromCity;
    private Topology.City toCity;
    private double costPerKM;

    public MoveAction(Topology.City fromCity, Topology.City toCity, double costPerKM) {
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.costPerKM = costPerKM;
    }

    @Override
    public logist.plan.Action getAction() {
        return new logist.plan.Action.Move(toCity);
    }

    @Override
    public State getNextState(State state) {

        List<Action> actions = new ArrayList<>(state.getPreviousActions());
        actions.add(this);

        return new StateRepresentation(
                toCity,
                state.getTaskTaken().clone(),
                state.getCapacityRemaining(),
                state.getTaskNotTaken().clone(),
                costPerKM,
                state.getCurrentCost() + this.getCost(),
                actions
        );
    }

    @Override
    public double getCost() {
        return fromCity.distanceTo(toCity) * costPerKM;
    }

    @Override
    public boolean isMove() {
        return true;
    }

    @Override
    public boolean isDeliver() {
        return false;
    }

    @Override
    public boolean isPickup() {
        return false;
    }
}
