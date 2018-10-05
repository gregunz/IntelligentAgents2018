package template;

import java.util.*;

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

public class ReactiveTemplate implements ReactiveBehavior {

	private int numActions;
	private Agent myAgent;

	private Map<State, Double> v = new HashMap<>();
    private Map<State, City> bestAction = new HashMap<>();

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.numActions = 0;
		this.myAgent = agent;

		List<City> possibleDestinationOfTask = new ArrayList<>(topology.cities());

		// TODO: define a "good enough" condition, it iterates non-stops as of now
		while(true){
            // Go through all possible states, and if the taskDest == currentCity, change the task destination to null
            for (City destination : possibleDestinationOfTask) {
                for (City currentCity : topology.cities()) {
                    State state = null;

                    List<City> actions = new ArrayList<>(currentCity.neighbors());

                    if (currentCity.equals(destination)) {
                        City noTask = null;
                        state = new State(currentCity, noTask);
                    } else {
                        state = new State(currentCity, destination);
                        actions.add(destination);
                    }

                    // Store best action for current state and the associated Q value
                    Double currentBestQ = 0.0;
                    City currentBestAction = null;

                    // For all possible actions, compute the Q(s,a)
                    for (City action : actions) {

                        double r = getReward(state, action, td); // R(s,a)

                        // Go one step further and evaluate for future possible actions
                        for (City destinationPrime : possibleDestinationOfTask) {

                            State statePrime = null;
                            if (action.equals(destinationPrime)) {
                                City noTask = null;
                                statePrime = new State(action, noTask);
                            } else {
                                statePrime = new State(action, destinationPrime);
                            }

                            r += discount * td.probability(state.city, statePrime.city) * v.getOrDefault(statePrime, 0.0);

                        }

                        // Store best action
                        if (r >= currentBestQ) {
                            currentBestQ = r;
                            currentBestAction = action;
                        }
                    }

                    // Update S(s) if Q(s,a) is better
                    if (currentBestQ > v.getOrDefault(state, 0.0)) {
                        v.put(state, currentBestQ);
                        bestAction.put(state, currentBestAction);
                    }

                }
            }
        }
	}

	private double getReward(State state, City action, TaskDistribution td) {
	    double gain = action.equals(state.destinationOfTask) ? td.reward(state.city, action) : 0.0;
	    double cost = state.city.distanceTo(action);

	    return gain - cost;
    }

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {

		if (numActions >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
        City destinationOfTask = availableTask.equals(null) ? null : availableTask.deliveryCity;
		State currentState = new State(vehicle.getCurrentCity(), destinationOfTask);

		City nextDestination = bestAction.get(currentState);

		return nextDestination.equals(destinationOfTask) ? new Pickup(availableTask) : new Move(nextDestination);
	}

	class State {
		public City city;
		public City destinationOfTask;

        public State(City city, City destinationOfTask) {
            this.city = city;
            this.destinationOfTask = destinationOfTask;
        }
	}
}
