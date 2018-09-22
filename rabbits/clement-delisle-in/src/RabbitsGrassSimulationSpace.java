import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Displayable;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.space.Discrete2DSpace;
import uchicago.src.sim.space.Object2DTorus;

import java.awt.*;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 *
 * @author
 */

public class RabbitsGrassSimulationSpace {

    private Discrete2DSpace grassSpace;
    private Discrete2DSpace rabbitsSpace;

    public RabbitsGrassSimulationSpace(int xSize, int ySize){
        grassSpace = new Object2DTorus(xSize, ySize);
        rabbitsSpace = new Object2DTorus(xSize, ySize);

        for(int i = 0; i < xSize; i++){
            for(int j = 0; j < ySize; j++){
                grassSpace.putObjectAt(i,j,new Integer(0));
            }
        }
    }

    public void growGrass(int grass){
        // Randomly place grass in grassSpace
        for(int i = 0; i < grass; i++){

            // Choose coordinates
            int x = (int)(Math.random()*(grassSpace.getSizeX()));
            int y = (int)(Math.random()*(grassSpace.getSizeY()));

            // Get the value of the object at those coordinates
            int currentValue = getGrassAt(x, y);
            // Replace the Integer object with another one with the new value
            grassSpace.putObjectAt(x,y,new Integer(currentValue + 1));
        }
    }

    public int getGrassAt(int x, int y){
        int i;
        if(grassSpace.getObjectAt(x,y)!= null){
            i = ((Integer)grassSpace.getObjectAt(x,y)).intValue();
        }
        else{
            i = 0;
        }
        return i;
    }

    public Discrete2DSpace getCurrentGrassSpace(){
        return grassSpace;
    }

    public boolean isCellOccupied(int x, int y){
        boolean retVal = false;
        if(rabbitsSpace.getObjectAt(x, y)!=null) retVal = true;
        return retVal;
    }

    public void addRabbit(int x, int y, RabbitsGrassSimulationAgent rabbit){
        rabbitsSpace.putObjectAt(x, y, rabbit);
    }

    public void removeRabbitAt(int x, int y){
        rabbitsSpace.putObjectAt(x, y, null);
    }

    public boolean moveRabbitAt(int x, int y, int newX, int newY){
        boolean retVal = false;
        if(!isCellOccupied(newX, newY)){
            RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)rabbitsSpace.getObjectAt(x, y);
            removeRabbitAt(x,y);
            cda.setXY(newX, newY);
            rabbitsSpace.putObjectAt(newX, newY, cda);
            retVal = true;
        }
        return retVal;
    }

    public int getEnergy(int x, int y) {
        Integer value = (Integer) grassSpace.getObjectAt(x, y);
        grassSpace.putObjectAt(x, y, 0);

        return value;
    }

    public Displayable getGrassDisplayable() {

        ColorMap map = new ColorMap();

        for(int i = 1; i<32; i++){
            map.mapColor(i, new Color(0, (int)(i * 8), 0));
        }
        map.mapColor(0, Color.black);

        return new Value2DDisplay(grassSpace, map);
    }

    public Displayable getRabbitsDisplayable() {
        return new Object2DDisplay(rabbitsSpace);
    }

}
