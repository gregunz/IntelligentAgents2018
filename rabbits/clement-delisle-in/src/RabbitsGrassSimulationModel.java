import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;
import utils.Position2D;

import java.util.ArrayList;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @authors Maxime Delisle & Grégoire Clément
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {


    private static final int GRID_WIDTH = 50;
    private static final int GRID_HEIGHT = 50;
    private static final int NUM_RABBITS = 5;
    private static final int BIRTH_THRESHOLD = 200;
    private static final int GRASS_GROWTH_RATE = 50;
    private static final int STEP_COST = 1;
    private static final int INITIAL_ENERGY = 100;
    private static final int INITIAL_GRASS = 100;
    private static final int BIRTH_COST = 100;

    private int gridWidth = GRID_WIDTH;
    private int gridHeight = GRID_HEIGHT;
    private int numRabbits = NUM_RABBITS;
    private int birthThreshold = BIRTH_THRESHOLD;
    private int grassGrowthRate = GRASS_GROWTH_RATE;
    private int stepCost = STEP_COST;
    private int initialEnergy = INITIAL_ENERGY;
    private int initialGrass = INITIAL_GRASS;
    private int birthCost = BIRTH_COST;

    private String[] initParams = {
            "GridWidth",
            "GridHeight",
            "NumRabbits",
            "BirthThreshold",
            "GrassGrowthRate",
            "StepCost",
            "InitialEnergy",
            "BirthCost"
    };

    private Schedule schedule;

    private RabbitsGrassSimulationSpace rgsSpace;

    private ArrayList<RabbitsGrassSimulationAgent> rabbits;

    private DisplaySurface displaySurf;

    public static void main(String[] args) {
        SimInit init = new SimInit();
        RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
        init.loadModel(model, "", false);
    }

    public void setup() {
        System.out.println("Running setup");
        rgsSpace = null;
        rabbits = null;
        schedule = null;

        displaySurf = null;

    }

    public void begin() {
        System.out.println("Building model");
        buildModel();
        System.out.println("Building schedule");
        buildSchedule();
        System.out.println("Building display");
        buildDisplay();

        displaySurf.display();
    }

    public String getName() {
        return "Rabbit simulation for IA course";
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public String[] getInitParam() {
        return initParams;
    }

    public int getNumRabbits() {
        return numRabbits;
    }

    public void setNumRabbits(int na) {
        numRabbits = na;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(int wxs) {
        gridWidth = wxs;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public void setGridHeight(int wys) {
        gridHeight = wys;
    }

    public int getBirthThreshold() {
        return birthThreshold;
    }

    public void setBirthThreshold(int i) {
        birthThreshold = i;
    }

    public int getGrassGrowthRate() {
        return grassGrowthRate;
    }

    public void setGrassGrowthRate(int i) {
        grassGrowthRate = i;
    }

    public int getStepCost() {
        return stepCost;
    }

    public void setStepCost(int stepCost) {
        this.stepCost = stepCost;
    }

    public int getInitialEnergy() {
        return initialEnergy;
    }

    public void setInitialEnergy(int initialEnergy) {
        this.initialEnergy = initialEnergy;
    }

    public int getInitialGrass() {
        return initialGrass;
    }

    public void setInitialGrass(int initialGrass) {
        this.initialGrass = initialGrass;
    }

    public int getBirthCost() {
        return birthCost;
    }

    public void setBirthCost(int birthCost) {
        this.birthCost = birthCost;
    }

    public void buildModel() {


        rgsSpace = new RabbitsGrassSimulationSpace(getGridWidth(), getGridHeight(), getInitialGrass());

        rabbits = new ArrayList<>();
        int rabbitsToAdd = getNumRabbits();
        rabbitsToAdd = Math.min(rabbitsToAdd, getGridWidth() * getGridHeight());
        while (rabbitsToAdd > 0) {
            Position2D pos = new Position2D(
                    (int) Math.floor(Math.random() * getGridWidth()),
                    (int) Math.floor(Math.random() * getGridHeight())
            );
            if (rgsSpace.isCellFree(pos)) {
                RabbitsGrassSimulationAgent agent = new RabbitsGrassSimulationAgent(
                        pos, getInitialEnergy(), rgsSpace);
                rabbits.add(agent);
                rabbitsToAdd--;
            }
        }
    }

    private void buildSchedule() {
        schedule = new Schedule();

        schedule.scheduleActionAtInterval(1, new BasicAction() {
            @Override
            public void execute() {
                rgsSpace.growGrass(getGrassGrowthRate());
                nextStep();
                displaySurf.updateDisplay();
            }
        });
    }

    private void buildDisplay() {
        displaySurf = new DisplaySurface(rgsSpace.getCurrentGrassSpace().getSize(), this, "Display");
        displaySurf.addDisplayable(rgsSpace.getGrassDisplayable(), "Grass");
        displaySurf.addDisplayable(rgsSpace.getRabbitsDisplayable(), "Rabbits");

        registerDisplaySurface("World", displaySurf);
    }

    private void nextStep() {

        ArrayList<RabbitsGrassSimulationAgent> newRabbits = new ArrayList<>();
        ArrayList<RabbitsGrassSimulationAgent> deadRabbits = new ArrayList<>();

        for (RabbitsGrassSimulationAgent rabbit : rabbits) {

            RabbitsGrassSimulationAgent newRabbit = rabbit.step(getStepCost(), getBirthThreshold(), getInitialEnergy(), getBirthCost());

            if (newRabbit != null) {
                newRabbits.add(newRabbit);
            }

            if (rabbit.hasDied()) {
                deadRabbits.add(rabbit);
            }
        }

        rabbits.addAll(newRabbits);
        rabbits.removeAll(deadRabbits);

    }

}
