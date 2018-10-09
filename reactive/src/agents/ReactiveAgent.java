package agents;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;
import models.AlgoAction;
import models.AlgoState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ReactiveAgent implements ReactiveBehavior {

	private int numActions;
	private Agent myAgent;
	private TaskDistribution myTaskDist;

    private Map<AlgoState, AlgoAction> bestActions;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

        // Reads the discount factor from the agents.xml file.
        // If the property is not present it defaults to 0.95
        Double discount = agent.readProperty("discount-factor", Double.class,
                0.95);

        this.numActions = 0;
        this.myAgent = agent;
        this.myTaskDist = td;
        this.bestActions = new HashMap<>();

        Map<AlgoState, Set<AlgoAction>> stateActions = new HashMap<>();
        Map<AlgoState, Double> v = new HashMap<>();

        for (City currentCity : topology.cities()) {
            for (City destCity : topology.cities()) {
                Set<AlgoAction> actions =
                        currentCity.neighbors().stream().map(AlgoAction::new).collect(Collectors.toSet());
                if (currentCity.equals(destCity)) {
                    stateActions.put(new AlgoState(currentCity, null), actions);
                } else {
                    actions.add(new AlgoAction(destCity));
                    stateActions.put(new AlgoState(currentCity, destCity), actions);
                }

            }
        }

        // TODO: define a better "good enough" condition
        double threshold = 0.0001;
        boolean hasImproved = true;

        while (hasImproved) {
            hasImproved = false;
            // Go through all possible states, and if the taskDest == currentCity, change the task destination to null
            for (AlgoState state : stateActions.keySet()) {

                // Store best action for current state and the associated Q value
                Double bestQ = 0.0;
                AlgoAction bestAction = null;

                // For all possible actions, compute the Q(s,a)
                for (AlgoAction action : stateActions.get(state)) {

                    Set<AlgoState> nextStates = stateActions.keySet();

                    double newQ = reward(state, action); // R(s,a)

                    // Go one step further and evaluate for future possible actions
                    // γ􏰀 * sum_{s'}{ T(s,a,s′) * V(s′) }
                    for (AlgoState nextState : stateActions.keySet()) {
                        if (nextState.getCity().equals(action.getCity())) { // here we skip the state with probability 0
                            newQ += discount * transition(state, action, nextState) * v.getOrDefault(nextState, 0.0);
                        }
                    }

                    // Store best action and bestQ
                    if (newQ >= bestQ) {
                        bestAction = action;
                        bestQ = newQ;
                    }

                }

                // Update S(s) if Q(s,a) is better
                if (bestQ > v.getOrDefault(state, 0.0)) {
                    if (bestQ - v.getOrDefault(state, 0.0) > threshold) {
                        hasImproved = true;
                    }
                    v.put(state, bestQ);
                    bestActions.put(state, bestAction);
                }

            }
        }
    }

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
        if (bestActions == null){
            throw new IllegalStateException("bestActions must be initialized when act is called");
        }
        if (myAgent == null){
            throw new IllegalStateException("myAgent must be initialized when act is called");
        }

		if (numActions % 50 == 0 && numActions >= 1) {
            System.out.println("The total profit after " + numActions + " actions is " + myAgent.getTotalProfit() +
                    " (average profit: " + (myAgent.getTotalProfit() / (double) numActions) + ")");
		}
		numActions++;
        City destinationOfTask = availableTask == null ? null : availableTask.deliveryCity;
		AlgoState currentState = new AlgoState(vehicle.getCurrentCity(), destinationOfTask);
		City nextDestination = bestActions.get(currentState).getCity();

        return nextDestination == destinationOfTask ? new Action.Pickup(availableTask) : new Action.Move(nextDestination);
	}

    private double reward(AlgoState state, AlgoAction action) {
        if (myAgent == null){
            throw new IllegalStateException("myAgent must be initialized when reward is called");
        }
        if (myTaskDist == null){
            throw new IllegalStateException("myTaskDist must be initialized when reward is called");
        }
        if (isImpossible(state, action)) {
            System.out.println("Our implementation should not go through this because it's too smart!");
            return Double.NEGATIVE_INFINITY;
        }
        double gain = isTakingTask(state, action) ? myTaskDist.reward(state.getCity(), action.getCity()) : 0.0;
        double cost = state.getCity().distanceTo(action.getCity()) * myAgent.vehicles().get(0).costPerKm();

        return gain - cost;
    }

    private double transition(AlgoState state, AlgoAction action, AlgoState nextState) {
        if (myTaskDist == null) {
            throw new IllegalStateException("myTaskDist must be initialized when reward is called");
        }
        if (isImpossible(state, action) || nextStateImpossible(action, nextState)) {
            System.out.println("Our implementation should not go through this because it's too smart!");
            return 0;
        }
        // The probability to transit to state `nextState` is just the probability that the next city holds the next task
        return myTaskDist.probability(nextState.getCity(), nextState.getTaskDestination());
    }

    private boolean isImpossible(AlgoState state, AlgoAction action) {
        // we cannot have action destinations which are neither neighbor nor task destination
        return !state.getCity().neighbors().contains(action.getCity()) && !action.getCity().equals(state.getTaskDestination());
    }

    private boolean isTakingTask(AlgoState state, AlgoAction action) {
        // this is true when the action we took is the task (delivery)
        return action.getCity().equals(state.getTaskDestination());
    }

    private boolean nextStateImpossible(AlgoAction action, AlgoState nextState) {
        // we cannot go to a city that was not the destination of our action
        return !action.getCity().equals(nextState.getCity());
    }
}
