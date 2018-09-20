import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {

    private Schedule schedule;

    private int gridWidth;
    private int gridHeight;
    private int numRabbits;
    private int birthThreshold;
    private int grassGrowthRate;

    private String[] initParams = {
            "GridWidth",
            "GridHeight",
            "NumRabbits",
            "BirthThreshold",
            "GrassGrowthRate"
    };

    public static void main(String[] args) {

        System.out.println("Rabbit skeleton");

    }

    public String getName() {
        return "Rabbits Grass Simulation";
    }

    /**
     * This function is called when the button with the two curved arrows is pressed.
     */
    public void setup() {
        // TODO Auto-generated method stub

    }

    /**
     * This function is responsible for initializing the simulation when the ‘Initialize’ button
     * is clicked on the toolbar.
     */
    public void begin() {
        buildModel();
        buildSchedule();
        buildDisplay();
    }

    public void buildModel() {
    }

    public void buildSchedule() {
    }

    public void buildDisplay() {
    }

    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * This function returns an array of String variables, each one listing the name of a particular
     * parameter that you want to be available to vary using the RePast control panel.
     *
     * @return Array of String variables for control panel
     */
    public String[] getInitParam() {
        return initParams;
    }


    public int getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public void setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }

    public int getNumRabbits() {
        return numRabbits;
    }

    public void setNumRabbits(int numRabbits) {
        this.numRabbits = numRabbits;
    }

    public int getBirthThreshold() {
        return birthThreshold;
    }

    public void setBirthThreshold(int birthThreshold) {
        this.birthThreshold = birthThreshold;
    }

    public int getGrassGrowthRate() {
        return grassGrowthRate;
    }

    public void setGrassGrowthRate(int grassGrowthRate) {
        this.grassGrowthRate = grassGrowthRate;
    }
}
