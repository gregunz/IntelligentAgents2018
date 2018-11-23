package algo;

import logist.agent.Agent;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import print.PrintHandler;
import random.RandomHandler;

import java.util.ArrayList;
import java.util.List;

public abstract class Bidder {


    public Bidder() {

    }

    public abstract Planner getOurPlanner();


    /**
     * Make a bid for the given task
     */
    public abstract Long bid(Task task);

    /**
     * Improve Bidder by getting information of previous auction results
     */
    public abstract void addInfoOfLastAuction(Task previous, int winner, Long[] bids);


}
