package plot;

import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.Sequence;

public abstract class InSpace implements DataSource, Sequence {
    public Object execute() {
        return getSValue();
    }
}
