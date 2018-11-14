package algo;

import logist.agent.Agent;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;

public class Bidder {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long bidTimout;

    private Planner planner;

    public Bidder(Topology topology, TaskDistribution distribution, Agent agent, long bidTimeout) {
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
        this.bidTimout = bidTimeout;

        this.planner = new Planner(agent.vehicles());
    }

    public Planner getPlanner() {
        return planner;
    }

    /**
     * Make a bid for the given task
     */
    public Long bid(Task task) {
        double marginalCost = this.planner.addTask(task, bidTimout);
        return null; //TODO
    }

    /**
     * Improve Bidder by getting information of previous auction results
     */
    public void addInfoOfLastAuction(Task previous, int winner, Long[] bids) {
        if (agent.id() == winner) { // we took the task
            this.planner.addTask(previous, /*TODO: what time here*/ bidTimout);
        }
        //TODO
    }


}
