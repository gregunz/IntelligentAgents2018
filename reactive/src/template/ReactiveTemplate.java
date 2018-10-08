package template;

import java.util.*;
import java.util.stream.Collectors;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;
import models.AlgoAction;
import models.AlgoState;

public class ReactiveTemplate implements ReactiveBehavior {

	private int numActions;
	private Agent myAgent;
	private TaskDistribution myTaskDist;

	private Map<AlgoState, Double> v;
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
        this.v = new HashMap<>();

		// TODO: define a better "good enough" condition
        double threshold = 0.0001;
        boolean hasImproved = true;

		while(hasImproved){
		    hasImproved = false;
            // Go through all possible states, and if the taskDest == currentCity, change the task destination to null
            for (City destCity : topology.cities()) {
                for (City currentCity : topology.cities()) {
                    AlgoState state;

                    Set<AlgoAction> actions = currentCity.neighbors().stream().map(AlgoAction::new).collect(Collectors.toSet());

                    if (currentCity.equals(destCity)) {
                        state = new AlgoState(currentCity, null);
                    } else {
                        state = new AlgoState(currentCity, destCity);
                        actions.add(new AlgoAction(destCity));
                    }

                    // Store best action for current state and the associated Q value
                    Double currentBestQ = 0.0;
                    AlgoAction currentBestAction = null;

                    // For all possible actions, compute the Q(s,a)
                    for (AlgoAction action : actions) {

                        double r = computeExpectedReward(state, action); // R(s,a)

                        // Go one step further and evaluate for future possible actions
                        for (City nextDest : topology.cities()) {

                            AlgoState statePrime;
                            if (action.getCity().equals(nextDest)) {
                                statePrime = new AlgoState(action.getCity(), null);
                            } else {
                                statePrime = new AlgoState(action.getCity(), nextDest);
                            }

                            r += discount * td.probability(statePrime.getCity(), statePrime.getTaskDestination()) * v.getOrDefault(statePrime, 0.0);

                        }

                        // Store best action
                        if (r >= currentBestQ) {
                            currentBestQ = r;
                            currentBestAction = action;
                        }
                    }

                    // Update S(s) if Q(s,a) is better
                    if (currentBestQ > v.getOrDefault(state, 0.0)) {
                        if (currentBestQ - v.getOrDefault(state,0.0) > threshold) {
                            hasImproved = true;
                        }
                        v.put(state, currentBestQ);
                        bestActions.put(state, currentBestAction);
                    }

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
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
        City destinationOfTask = availableTask == null ? null : availableTask.deliveryCity;
		AlgoState currentState = new AlgoState(vehicle.getCurrentCity(), destinationOfTask);

		City nextDestination = bestActions.get(currentState).getCity();

		/*
		if (nextDestination == destinationOfTask) {
		    System.out.println("Task taken");
        }
        */

		return nextDestination == destinationOfTask ? new Pickup(availableTask) : new Move(nextDestination);
	}

    private double computeExpectedReward(AlgoState state, AlgoAction action) {
        if (myAgent == null){
            throw new IllegalStateException("myAgent must be initialized when computeExpectedReward is called");
        }
        if (myTaskDist == null){
            throw new IllegalStateException("myTaskDist must be initialized when computeExpectedReward is called");
        }
        double gain = action.getCity().equals(state.getTaskDestination()) ? myTaskDist.reward(state.getCity(), action.getCity()) : 0.0;
        double cost = state.getCity().distanceTo(action.getCity()) * myAgent.vehicles().get(0).costPerKm();

        return gain - cost;
    }
}
