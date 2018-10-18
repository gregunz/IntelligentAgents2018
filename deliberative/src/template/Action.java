package template;

public interface Action {

    logist.plan.Action getAction();

    State getNextState(State state);

    double getReward();

}
