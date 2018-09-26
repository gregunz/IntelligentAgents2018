import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Displayable;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.space.Discrete2DSpace;
import uchicago.src.sim.space.Object2DTorus;
import utils.Position2D;

import java.awt.*;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 *
 * @authors Maxime Delisle & Grégoire Clément
 */

public class RabbitsGrassSimulationSpace {

    private final int MAX_GRASS_VALUE = 50;

    private Discrete2DSpace grassSpace;
    private Discrete2DSpace rabbitsSpace;

    public RabbitsGrassSimulationSpace(int xSize, int ySize, int initGrass) {
        grassSpace = new Object2DTorus(xSize, ySize);
        rabbitsSpace = new Object2DTorus(xSize, ySize);

        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                grassSpace.putObjectAt(i, j, 0);
            }
        }

        growGrass(initGrass);
    }

    public void growGrass(int grass) {
        // Randomly place grass in grassSpace
        for (int i = 0; i < grass; i++) {

            // Choose coordinates
            int x = (int) (Math.random() * (grassSpace.getSizeX()));
            int y = (int) (Math.random() * (grassSpace.getSizeY()));

            // Get the value of the object at those coordinates
            int currentValue = getGrassAt(x, y);
            // Replace the Integer object with another one with the new value
            grassSpace.putObjectAt(x, y, Math.min(MAX_GRASS_VALUE, currentValue + 1));
        }
    }

    public int getGrassAt(int x, int y) {
        if (grassSpace.getObjectAt(x, y) != null) {
            return (int) grassSpace.getObjectAt(x, y);
        }
        return 0;
    }

    public Discrete2DSpace getCurrentGrassSpace() {
        return grassSpace;
    }

    public boolean isCellFree(Position2D pos) {
        return isCellFree(pos.getX(), pos.getY());
    }

    public boolean isCellFree(int x, int y) {
        return rabbitsSpace.getObjectAt(x, y) == null;
    }

    public void addRabbit(Position2D pos, RabbitsGrassSimulationAgent rabbit) {
        rabbitsSpace.putObjectAt(pos.getX(), pos.getY(), rabbit);
    }

    public void removeRabbitAt(Position2D pos) {
        rabbitsSpace.putObjectAt(pos.getX(), pos.getY(), null);
    }

    public void moveRabbitAt(Position2D oldPos, Position2D newPos) {
        RabbitsGrassSimulationAgent cda =
                (RabbitsGrassSimulationAgent) rabbitsSpace.getObjectAt(oldPos.getX(), oldPos.getY());
        removeRabbitAt(oldPos);
        cda.setPos(newPos);
        rabbitsSpace.putObjectAt(newPos.getX(), newPos.getY(), cda);
    }

    public int getEnergy(Position2D pos) {
        Integer value = (Integer) grassSpace.getObjectAt(pos.getX(), pos.getY());
        grassSpace.putObjectAt(pos.getX(), pos.getY(), 0);
        return value;
    }

    public Displayable getGrassDisplayable() {

        ColorMap map = new ColorMap();

        for (int i = 0; i < MAX_GRASS_VALUE + 1; i++) {
            map.mapColor(i, new Color(0, i * (255 / MAX_GRASS_VALUE), 0));
        }
        map.mapColor(0, Color.black);

        return new Value2DDisplay(grassSpace, map);
    }

    public Displayable getRabbitsDisplayable() {
        return new Object2DDisplay(rabbitsSpace);
    }

    public int getTotalGrass() {
        int totalGrass = 0;
        for (int i = 0; i < grassSpace.getSizeX(); i++) {
            for (int j = 0; j < grassSpace.getSizeY(); j++) {
                if (getGrassAt(i, j)!=0)
                    totalGrass += 1;
            }
        }
        return totalGrass;
    }
}
