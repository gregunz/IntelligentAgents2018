import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Value2DDisplay;

import java.awt.*;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {

    private static final int GRIDWIDTH = 20;
    private static final int GRIDHEIGHT = 20;
    private static final int NUMRABBITS = 5;
    private static final int BIRTHTHRESHOLD = 10;
    private static final int GRASSGROWTHRATE = 4;

    private int gridWidth = GRIDWIDTH;
    private int gridHeight = GRIDHEIGHT;
    private int numRabbits = NUMRABBITS;
    private int birthThreshold = BIRTHTHRESHOLD;
    private int grassGrowthRate = GRASSGROWTHRATE;

    private String[] initParams = {
            "GridWidth",
            "GridHeight",
            "NumRabbits",
            "BirthThreshold",
            "GrassGrowthRate"
    };

    private Schedule schedule;

    private RabbitsGrassSimulationSpace space;

    private DisplaySurface displaySurface;

    public static void main(String[] args) {
        System.out.println("Rabbit skeleton");

        SimInit init = new SimInit();
        RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
        init.loadModel(model, "", false);

    }

    public String getName() {
        return "Rabbits Grass Simulation";
    }

    /**
     * This function is called when the button with the two curved arrows is pressed.
     */
    public void setup() {
        System.out.println("Running setup");
        space = null;

        if (displaySurface != null){
            displaySurface.dispose();
        }
        displaySurface = null;

        displaySurface = new DisplaySurface(this, "Carry Drop Model Window 1");

        registerDisplaySurface("Carry Drop Model Window 1", displaySurface);
    }

    /**
     * This function is responsible for initializing the simulation when the ‘Initialize’ button
     * is clicked on the toolbar.
     */
    public void begin() {
        buildModel();
        buildSchedule();
        buildDisplay();


        displaySurface.display();
    }

    public void buildModel() {
        System.out.println("Running BuildModel");
        space = new RabbitsGrassSimulationSpace(gridWidth, gridHeight);
    }

    public void buildSchedule() {
        System.out.println("Running BuildSchedule");
    }

    public void buildDisplay() {
        System.out.println("Running BuildDisplay");

        ColorMap map = new ColorMap();

        for(int i = 1; i<16; i++){
            map.mapColor(i, new Color((int)(i * 8 + 127), 0, 0));
        }
        map.mapColor(0, Color.white);

        Value2DDisplay displayMoney = //TODO: CHANGE NAME (it's from tutorial which is about another application)
                new Value2DDisplay(space.getGrid(), map);

        displaySurface.addDisplayable(displayMoney, "Money"); //TODO: change here too (the string)
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
