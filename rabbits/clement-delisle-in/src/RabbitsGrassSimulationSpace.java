import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 *
 * @author
 */

public class RabbitsGrassSimulationSpace {
    private Object2DGrid grid;

    public RabbitsGrassSimulationSpace(int xSize, int ySize){
        grid = new Object2DGrid(xSize, ySize);
        for(int i = 0; i < xSize; i++){
            for(int j = 0; j < ySize; j++){
                grid.putObjectAt(i,j,new Integer(0));
            }
        }
    }

    public Object2DGrid getGrid() {
        return grid;
    }
}
