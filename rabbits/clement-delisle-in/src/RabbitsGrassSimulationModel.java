import plot.InSpace;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;
import utils.Position2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @authors Maxime Delisle & Grégoire Clément
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {


    //* default seed=1538487159028
    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 20;
    private static final int NUM_RABBITS = 300;
    private static final int BIRTH_THRESHOLD = 200;
    private static final int GRASS_GROWTH_RATE = 200;
    private static final int MOVE_ENERGY_COST = 1;
    private static final int INITIAL_ENERGY = 50;
    private static final int BIRTH_COST = 50;
    private static final int GRASS_STEP = 1;
    private static final int GRASS_MAX_VALUE = 100;
    private static final int RABBIT_MAX_EAT = 20;
    //*/


    /* stable seed=1538485249374
    private static final int GRID_WIDTH = 100;
    private static final int GRID_HEIGHT = 100;
    private static final int NUM_RABBITS = 50;
    private static final int BIRTH_THRESHOLD = 200;
    private static final int GRASS_GROWTH_RATE = 110;
    private static final int MOVE_ENERGY_COST = 3;
    private static final int INITIAL_ENERGY = 200;
    private static final int BIRTH_COST = 150;
    private static final int GRASS_STEP = 4;
    private static final int GRASS_MAX_VALUE = 64;
    private static final int RABBIT_MAX_EAT = 8;
    //*/



    /* unstable seed=1538488171992
    private static final int GRID_WIDTH = 100;
    private static final int GRID_HEIGHT = 100;
    private static final int NUM_RABBITS = 50;
    private static final int BIRTH_THRESHOLD = 200;
    private static final int GRASS_GROWTH_RATE = 110;
    private static final int MOVE_ENERGY_COST = 3;
    private static final int INITIAL_ENERGY = 200;
    private static final int BIRTH_COST = 100;
    private static final int GRASS_STEP = 4;
    private static final int GRASS_MAX_VALUE = 64;
    private static final int RABBIT_MAX_EAT = 8;
    //*/

    private int gridWidth = GRID_WIDTH;
    private int gridHeight = GRID_HEIGHT;
    private int numRabbits = NUM_RABBITS;
    private int birthThreshold = BIRTH_THRESHOLD;
    private int grassGrowthRate = GRASS_GROWTH_RATE;
    private int moveEnergyCost = MOVE_ENERGY_COST;
    private int initialEnergy = INITIAL_ENERGY;
    private int birthCost = BIRTH_COST;
    private int grassStep = GRASS_STEP;
    private int grassMaxValue = GRASS_MAX_VALUE;
    private int rabbitMaxEat = RABBIT_MAX_EAT;


    private String[] initParams = {
            "GridWidth",
            "GridHeight",
            "NumRabbits",
            "BirthThreshold",
            "GrassGrowthRate",
            "MoveEnergyCost",
            "InitialEnergy",
            "BirthCost",
            "GrassMaxValue",
            "GrassStep",
            "RabbitMaxEat"
    };

    private Schedule schedule;

    private RabbitsGrassSimulationSpace space;

    private List<RabbitsGrassSimulationAgent> rabbits;

    private DisplaySurface surface;

    private OpenSequenceGraph amountOfEachPopInSpace;

    public static void main(String[] args) {
        SimInit init = new SimInit();
        RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
        init.loadModel(model, "", false);
    }

    /**
     * This function is called when the button with the two curved arrows is pressed.
     */
    public void setup() {
        System.out.println("Running setup");
        space = null;
        rabbits = new ArrayList<>();
        schedule = null;
        surface = null;

        if (amountOfEachPopInSpace != null) {
            amountOfEachPopInSpace.dispose();
        }
        surface = new DisplaySurface(this, "Rabbits grass simulation Model Window 1");
        amountOfEachPopInSpace = new OpenSequenceGraph("Amount of rabbit and grass in space", this);

        // Register Displays
        registerDisplaySurface("Rabbits grass simulation Model Window 1", surface);
        this.registerMediaProducer("Plot", amountOfEachPopInSpace);

    }

    /**
     * This function is responsible for initializing the simulation when the ‘Initialize’ button
     * is clicked on the toolbar.
     */
    public void begin() {
        System.out.println("Building model");
        buildModel();
        System.out.println("Building schedule");
        buildSchedule();
        System.out.println("Building display");
        buildDisplay();

        surface.display();
        amountOfEachPopInSpace.display();
    }

    public void buildModel() {
        space = new RabbitsGrassSimulationSpace(getGridWidth(), getGridHeight(), getGrassStep(), getGrassMaxValue(), getRabbitMaxEat());
        rabbits = generateRabbits(getNumRabbits());
    }

    private void buildSchedule() {
        schedule = new Schedule(1);

        schedule.scheduleActionBeginning(0, new BasicAction() {
            @Override
            public void execute() {
                space.growGrass(getGrassGrowthRate(), getGrassMaxValue());
                evolveRabbits();
                surface.updateDisplay();
            }
        });

        schedule.scheduleActionAtInterval(50, new BasicAction() {
            @Override
            public void execute() {
                amountOfEachPopInSpace.step();
            }
        });
    }

    public void buildDisplay() {
        surface = new DisplaySurface(space.getCurrentGrassSpace().getSize(), this, "Display");
        surface.addDisplayable(space.getGrassDisplayable(), "Grass");
        surface.addDisplayable(space.getRabbitsDisplayable(), "Rabbits");

        registerDisplaySurface("World", surface);

        amountOfEachPopInSpace.addSequence("Rabbits In Space", new InSpace() {
            @Override
            public double getSValue() {
                return (double) rabbits.size();
            }
        });
        amountOfEachPopInSpace.addSequence("Grass In Space", new InSpace() {
            @Override
            public double getSValue() {
                return (double) space.getTotalGrass();
            }
        });
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

    public String getName() {
        return "Rabbit simulation for IA course";
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public int getNumRabbits() {
        return numRabbits;
    }

    public void setNumRabbits(int numRabbits) {
        this.numRabbits = numRabbits;
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

    public int getMoveEnergyCost() {
        return moveEnergyCost;
    }

    public void setMoveEnergyCost(int moveEnergyCost) {
        this.moveEnergyCost = moveEnergyCost;
    }

    public int getInitialEnergy() {
        return initialEnergy;
    }

    public void setInitialEnergy(int initialEnergy) {
        this.initialEnergy = initialEnergy;
    }

    public int getBirthCost() {
        return birthCost;
    }

    public void setBirthCost(int birthCost) {
        this.birthCost = birthCost;
    }

    public int getGrassMaxValue() {
        return grassMaxValue;
    }

    public void setGrassMaxValue(int grassMaxValue) {
        this.grassMaxValue = grassMaxValue;
    }

    private List<RabbitsGrassSimulationAgent> generateRabbits(int numRabbits) {
        List<RabbitsGrassSimulationAgent> rabbits = new ArrayList<>();
        int rabbitsToAdd = Math.min(numRabbits, getGridWidth() * getGridHeight() - rabbits.size());
        while (rabbitsToAdd > 0) {
            Position2D pos = Position2D.random(getGridWidth(), getGridHeight());
            if (space.isCellFree(pos)) {
                RabbitsGrassSimulationAgent agent = new RabbitsGrassSimulationAgent(pos, getInitialEnergy(), space);
                rabbits.add(agent);
                rabbitsToAdd--;
            }
        }
        return rabbits;
    }

    private void evolveRabbits() {

        ArrayList<RabbitsGrassSimulationAgent> newRabbits = new ArrayList<>();
        ArrayList<RabbitsGrassSimulationAgent> deadRabbits = new ArrayList<>();

        for (RabbitsGrassSimulationAgent rabbit : rabbits) {

            rabbit.moveThenEat(getMoveEnergyCost());

            if (rabbit.isDead()) {
                deadRabbits.add(rabbit);
                space.removeRabbitAt(rabbit.getPos());
            }

            Optional<RabbitsGrassSimulationAgent> maybeNewRabbit =
                    rabbit.tryToReproduce(getBirthThreshold(), getInitialEnergy(), getBirthCost());

            maybeNewRabbit.ifPresent(newRabbits::add);
        }

        rabbits.addAll(newRabbits);
        rabbits.removeAll(deadRabbits);

    }

    public int getGrassStep() {
        return grassStep;
    }

    public void setGrassStep(int grassStep) {
        this.grassStep = grassStep;
    }

    public int getRabbitMaxEat() {
        return rabbitMaxEat;
    }

    public void setRabbitMaxEat(int rabbitMaxEat) {
        this.rabbitMaxEat = rabbitMaxEat;
    }
}
