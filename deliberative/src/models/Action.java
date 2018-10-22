package models;

public interface Action {

    logist.plan.Action getAction();

    State getNextState(State state);

    double getCost();

    boolean isMove();

    boolean isDeliver();

    boolean isPickup();
}
