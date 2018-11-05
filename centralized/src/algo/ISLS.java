package algo;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskSet;

import java.util.List;
import java.util.Set;

public abstract class ISLS<PLANS> {
    abstract void init(List<Vehicle> vehicles, TaskSet tasks);

    abstract double objectiveOf(PLANS plans);

    abstract Set<PLANS> chooseNeighbours();

    abstract void localChoice(Set<PLANS> neighbors);

    abstract PLANS actualPlans();

    abstract List<Plan> actualLogistPlans();

    double actualObjective() {
        return objectiveOf(actualPlans());
    }

}
