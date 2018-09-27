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


    private Discrete2DSpace grassSpace;
    private Discrete2DSpace rabbitsSpace;
    private int grassStep;

    public RabbitsGrassSimulationSpace(int gridWidth, int gridHeight, int grassStep) {
        this.rabbitsSpace = new Object2DTorus(gridWidth, gridHeight);
        this.grassSpace = createGrassSpace(gridWidth, gridHeight);
        this.grassStep = grassStep;
    }

    public Discrete2DSpace createGrassSpace(int width, int height) {
        Discrete2DSpace grassSpace = new Object2DTorus(width, height);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grassSpace.putObjectAt(i, j, 0L);
            }
        }
        return grassSpace;
    }

    public void growGrass(int grassQuantity) {
        for (int i = 0; i < grassQuantity; i++) {
            Position2D pos = Position2D.random(grassSpace.getSizeX(), grassSpace.getSizeY());
            grassSpace.putObjectAt(pos.getX(), pos.getY(), getGrassAt(pos) + grassStep);
        }
    }

    public long getGrassAt(Position2D pos) {
        Object grassObject = grassSpace.getObjectAt(pos.getX(), pos.getY());
        if (grassObject != null) {
            return (long) grassObject;
        }
        return 0L;
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

    public long eatGrassAt(Position2D pos) {
        long value = (long) grassSpace.getObjectAt(pos.getX(), pos.getY());
        grassSpace.putObjectAt(pos.getX(), pos.getY(), 0L);
        return value;
    }

    public void addRabbitAt(Position2D pos, RabbitsGrassSimulationAgent rabbit) {
        rabbitsSpace.putObjectAt(pos.getX(), pos.getY(), rabbit);
    }

    public void removeRabbitAt(Position2D pos) {
        rabbitsSpace.putObjectAt(pos.getX(), pos.getY(), null);
    }

    public void moveRabbitAt(Position2D oldPos, Position2D newPos) {
        RabbitsGrassSimulationAgent rabbit =
                (RabbitsGrassSimulationAgent) rabbitsSpace.getObjectAt(oldPos.getX(), oldPos.getY());
        removeRabbitAt(oldPos);
        rabbit.setPos(newPos);
        rabbitsSpace.putObjectAt(newPos.getX(), newPos.getY(), rabbit);
    }


    public Displayable getGrassDisplayable() {

        ColorMap map = new ColorMap();

        final int grassMaxSteps = 16;
        for (int i = 0; i < grassMaxSteps + 1; i++) {
            map.mapColor(i, new Color(0, i * (255 / grassMaxSteps), 0));
        }
        map.mapColor(0, Color.black);

        return new Value2DDisplay(grassSpace, map);
    }

    public Displayable getRabbitsDisplayable() {
        return new Object2DDisplay(rabbitsSpace);
    }

    public long getTotalGrass() {
        long totalGrass = 0;
        for (int x = 0; x < grassSpace.getSizeX(); x++) {
            for (int y = 0; y < grassSpace.getSizeY(); y++) {
                totalGrass += getGrassAt(new Position2D(x, y));
            }
        }
        return totalGrass;
    }
}
