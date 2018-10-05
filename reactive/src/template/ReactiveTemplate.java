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
        int i = 0;
		while(i < 10){
            i ++;
            // Go through all possible states, and if the taskDest == currentCity, change the task destination to null
            for (City destination : possibleDestinationOfTask) {
                for (City currentCity : topology.cities()) {
                    State state;

                    List<City> actions = new ArrayList<>(currentCity.neighbors());

                    if (currentCity.equals(destination)) {
                        state = new State(currentCity, null);
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

                            State statePrime;
                            if (action.equals(destinationPrime)) {
                                statePrime = new State(action, null);
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
	    double cost = state.city.distanceTo(action) * myAgent.vehicles().get(0).costPerKm();

	    return gain - cost;
    }

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {

		if (numActions >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
        City destinationOfTask = availableTask == null ? null : availableTask.deliveryCity;
		State currentState = new State(vehicle.getCurrentCity(), destinationOfTask);

		City nextDestination = bestAction.get(currentState);

		return nextDestination == destinationOfTask ? new Pickup(availableTask) : new Move(nextDestination);
	}

	private class State {
        private City city;
        private City destinationOfTask;

        private State(City city, City destinationOfTask) {
            this.city = city;
            this.destinationOfTask = destinationOfTask;
        }

        @Override
        public int hashCode() {
            int prime = 11;
            int cityHash = city == null ? 0 : city.hashCode();
            int taskHash = destinationOfTask == null ? 0 : destinationOfTask.hashCode();

            return prime * (prime + prime*cityHash) + taskHash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (this == obj) return true;
            if (getClass() != obj.getClass()) return false;
            State otherState = (State)obj;
            if (this.city != otherState.city) return false;
            if (this.destinationOfTask != otherState.destinationOfTask) return false;
            return true;

        }
    }
}
